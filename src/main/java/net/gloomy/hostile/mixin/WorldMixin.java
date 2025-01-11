package net.gloomy.hostile.mixin;

import btw.BTWMod;
import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.data.DataEntry;
import btw.world.util.data.DataProvider;
import btw.world.util.difficulty.Difficulties;
import btw.world.util.WorldUtils;
import java.util.Collection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public WorldInfo worldInfo;

    @Shadow public abstract boolean isDaytime();
    
    @Inject(method = "computeOverworldSunBrightnessWithMoonPhases", at = @At("RETURN"),remap = false, cancellable = true)
    private void manageLightLevels(CallbackInfoReturnable<Float> cir){
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 3) {
            //Nothing.
        }
        else if (GloomyHostile.worldState == 2 || GloomyHostile.worldState == 1) {
            if (getIsNight(thisObj)) cir.setReturnValue(0f);
            else if (GloomyHostile.worldState == 2) cir.setReturnValue((15f-thisObj.skylightSubtracted)/25f);
        }
		    // System.out.println("sunlight subtracted:"+thisObj.skylightSubtracted);
        // cir.setReturnValue(0f); // this is temporary, this sets to permanent gloom
    }
    // @Inject(method = "initialWorldChunkLoad", at = @At("RETURN"))
    // private void initialWorldChunkLoadMixin(CallbackInfo ci) {
    //     if (GloomyHostile.worldState == 2 || GloomyHostile.worldState == 1) {
    //         RenderGlobalMixin.postWitherSunTicks = 99;
    //         moonTransitionTicks = 99;
    //     }
    //     else {
    //         RenderGlobalMixin.postWitherSunTicks = 99;
    //         moonTransitionTicks = 99;
    //     }
    // }
    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci){
        World thisObj = (World)(Object)this;
        if (thisObj.provider.dimensionId == 0 && !(thisObj instanceof WorldServer)){
            if (GloomyHostile.worldState == 2) {
                GloomyHostile.postWitherSunTicks++;
                if (GloomyHostile.postWitherSunTicks == 240) {
                    Minecraft.getMinecraft().thePlayer.playSound("mob.wither.spawn",2.0F,0.5F);
                }
            }
            else GloomyHostile.postWitherSunTicks = 0;
            if (GloomyHostile.worldState == 1 || GloomyHostile.worldState == 2) {
                GloomyHostile.moonTransitionTicks++;
                if (GloomyHostile.moonTransitionTicks == 240 && GloomyHostile.worldState == 1) {
                    Minecraft.getMinecraft().thePlayer.playSound("mob.wither.death",2.0F,0.5F);
                }
            }
            else GloomyHostile.moonTransitionTicks = 0;
        }
    }

    @Inject(method = "getMoonPhase", at = @At("RETURN"), cancellable = true)
    private void forceNewMoonPostNether(CallbackInfoReturnable<Integer> cir){
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 3) {
            //Nothing.
        }
        else if (GloomyHostile.worldState == 2 || GloomyHostile.worldState == 1) {
            if (MinecraftServer.getIsServer()) cir.setReturnValue(4);
            else cir.setReturnValue((int)lerp((float)cir.getReturnValue(), 4f, Math.min(GloomyHostile.moonTransitionTicks/240f, 1f)));
        }
    }
    private float lerp(float a, float b, float f) 
    {
        return (a * (1.0f - f)) + (b * f);
    }

    /* ! WARNING ! This will modify how isDaytime works! ! WARNING ! */
    @Inject(method = "calculateSkylightSubtracted", at = @At("RETURN"), cancellable = true)
    private void subtractSkylight(CallbackInfoReturnable<Integer> cir){
        if (GloomyHostile.worldState == 2) {
            cir.setReturnValue(Math.min(cir.getReturnValue() + 11, 15));
        }
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void darkenSky(CallbackInfoReturnable<Vec3> cir){
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 2) {
            double darkness = 0.15d - (thisObj.skylightSubtracted / 15d) * 0.1d;
            Vec3 skyColor = cir.getReturnValue();
            skyColor.scale(darkness);
            cir.setReturnValue(skyColor);
        }
    }
    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void darkenFog(CallbackInfoReturnable<Vec3> cir){
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 2) {
            double darkness = 0.1d - (thisObj.skylightSubtracted / 15d) * 0.1d;
            Vec3 fogColor = cir.getReturnValue();
            fogColor.scale(darkness);
            cir.setReturnValue(fogColor);
        }
    }
    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true)
    private void showStars(CallbackInfoReturnable<Float> cir){
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 2) {
            cir.setReturnValue(thisObj.skylightSubtracted / 30f);
        }
    }

    @Unique private boolean getIsNight(World world){
        return world.getWorldTime() % 24000 >= 13200 && world.getWorldTime() % 24000 <= 23159;
    }
}
