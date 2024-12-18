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
public class WorldMixin {
    @Shadow public WorldInfo worldInfo;
    
    @Inject(method = "computeOverworldSunBrightnessWithMoonPhases", at = @At("RETURN"),remap = false, cancellable = true)
    private void manageLightLevels(CallbackInfoReturnable<Float> cir){
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 3) {
            //Nothing.
        }
        else if (GloomyHostile.worldState == 2 || GloomyHostile.worldState == 1) {
            if (getIsNight(thisObj)) cir.setReturnValue(0f);
            else cir.setReturnValue(4f/15f);
        }
		    // System.out.println("sunlight subtracted:"+thisObj.skylightSubtracted);
        // cir.setReturnValue(0f); // this is temporary, this sets to permanent gloom
    }

    @Inject(method = "getMoonPhase", at = @At("RETURN"), cancellable = true)
    private void forceNewMoonPostNether(CallbackInfoReturnable<Integer> cir){
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 3) {
            //Nothing.
        }
        else if (GloomyHostile.worldState == 2 || GloomyHostile.worldState == 1) {
            cir.setReturnValue(4);
        }
    }

    /* ! WARNING ! This will modify how isDaytime works! ! WARNING ! */
    @Inject(method = "calculateSkylightSubtracted", at = @At("RETURN"), cancellable = true)
    private void subtractSkylight(CallbackInfoReturnable<Integer> cir){
        if (GloomyHostile.worldState == 2) {
            cir.setReturnValue(Math.min(cir.getReturnValue() + 11, 15));
        }
    }

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void darkenSky(CallbackInfoReturnable<Vec3> cir){
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 2) {
            double grayness = 0.1d - (thisObj.skylightSubtracted/15) * 0.1d;
            cir.setReturnValue(thisObj.getWorldVec3Pool().getVecFromPool(grayness, grayness, grayness));
        }
    }

    @Unique private boolean getIsNight(World world){
        return world.getWorldTime() % 24000 >= 12541 && world.getWorldTime() % 24000 <= 23459;
    }
}
