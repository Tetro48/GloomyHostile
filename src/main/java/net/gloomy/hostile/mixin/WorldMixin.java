package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public WorldInfo worldInfo;

    @Shadow public abstract boolean isDaytime();

    @Shadow public abstract float getRainStrength(float par1);
    @Shadow public abstract float getWeightedThunderStrength(float par1);
    @Shadow public abstract float getCelestialAngle(float par1);

    @Shadow public boolean isRemote;

    private float calculateSkyBrightnessWithNewMoon(float sunBrightnessMultiplier) {
        float fCelestialAngle = this.getCelestialAngle(1.0F);
        float fSunInvertedBrightness = 1.0F - (MathHelper.cos(fCelestialAngle * (float)Math.PI * 2.0F) * 2.0F + 0.25F);
        fSunInvertedBrightness = Math.min(Math.max(fSunInvertedBrightness, 0), 1);

        double dSunBrightness = 1.0d - (double)fSunInvertedBrightness;
        double dRainBrightnessModifier = 1.0d - (double)(this.getRainStrength(1.0F) * 5.0F) / 16.0d;
        double dStormBrightnessModifier = 1.0d - (double)(this.getWeightedThunderStrength(1.0F) * 5.0F) / 16.0d;
        dSunBrightness *= dRainBrightnessModifier * dStormBrightnessModifier * sunBrightnessMultiplier;

        return (float)(dSunBrightness);
    }
    @Inject(method = "computeOverworldSunBrightnessWithMoonPhases", at = @At("RETURN"),remap = false, cancellable = true)
    private void manageLightLevels(CallbackInfoReturnable<Float> cir){
        if (GloomyHostile.worldState == 3) {
            //Nothing.
        }
        else if (GloomyHostile.worldState == 2 || GloomyHostile.worldState == 1) {
            float moonTransitionPoint = Math.min((float)GloomyHostile.postNetherMoonTicks / GloomyHostile.moonTransitionTime, 1f);
            float sunTransitionPoint = Math.min((float)GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1f);
            cir.setReturnValue(lerp(cir.getReturnValue(),
                    calculateSkyBrightnessWithNewMoon(lerp(1f, 0.25f, sunTransitionPoint)),
                    moonTransitionPoint));
        }
    }
    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci){
        World thisObj = (World)(Object)this;
        if (thisObj.provider.dimensionId == 0 && this.isRemote){
            if (GloomyHostile.worldState == 2) {
                GloomyHostile.postWitherSunTicks++;
                if (GloomyHostile.postWitherSunTicks == GloomyHostile.sunTransitionTime && !GloomyHostile.isCancerMode) {
                    Minecraft.getMinecraft().thePlayer.playSound("mob.wither.spawn",2.0F,0.5F);
                }
            }
            else GloomyHostile.postWitherSunTicks = 0;
            if (GloomyHostile.worldState == 1 || GloomyHostile.worldState == 2) {
                GloomyHostile.postNetherMoonTicks++;
                if (GloomyHostile.postNetherMoonTicks == GloomyHostile.moonTransitionTime && GloomyHostile.worldState == 1 && !GloomyHostile.isCancerMode) {
                    Minecraft.getMinecraft().thePlayer.playSound("mob.wither.death",2.0F,0.5F);
                }
            }
            else GloomyHostile.postNetherMoonTicks = 0;
        }
    }

    @Inject(method = "getMoonPhase", at = @At("RETURN"), cancellable = true)
    private void forceNewMoonPostNether(CallbackInfoReturnable<Integer> cir){
        if (GloomyHostile.worldState == 3) {
            //Nothing.
        }
        else if (GloomyHostile.worldState == 2 || GloomyHostile.worldState == 1) {
            if (MinecraftServer.getIsServer()) cir.setReturnValue(4);
            else {
                cir.setReturnValue((int) lerp((float) cir.getReturnValue(), 4f,
                     Math.min((float) GloomyHostile.postNetherMoonTicks / GloomyHostile.moonTransitionTime, 1f)));
                long days = (this.worldInfo.getWorldTime()) / 24000L;
                if (GloomyHostile.isNightmareModeInstalled && !this.isRemote) {
                    if (days % 16 == 8) cir.setReturnValue(0); // just for bloodmoon to exist, whilst also being visually new moon.
                }
            }
        }
    }
    private float lerp(float a, float b, float f) { return (a * (1.0f - f)) + (b * f); }
    private double lerp(double a, double b, double f)
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
        double transitionPoint = Math.min((double)GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1d);
        if (GloomyHostile.worldState == 2) {
            double darkness = 0.15d - (thisObj.skylightSubtracted / 15d) * 0.1d;
            Vec3 skyColor = cir.getReturnValue();
            skyColor.scale(lerp(1, darkness, transitionPoint));
            cir.setReturnValue(skyColor);
        }
    }
    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void darkenFog(CallbackInfoReturnable<Vec3> cir){
        World thisObj = (World)(Object)this;
        double transitionPoint = Math.min((double)GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1d);
        if (GloomyHostile.worldState == 2) {
            double darkness = 0.1d - (thisObj.skylightSubtracted / 15d) * 0.1d;
            Vec3 fogColor = cir.getReturnValue();
            fogColor.scale(lerp(1, darkness, transitionPoint));
            cir.setReturnValue(fogColor);
        }
    }
    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true)
    private void showStars(CallbackInfoReturnable<Float> cir){
        World thisObj = (World)(Object)this;
        float transitionPoint = Math.min((float)GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1f);
        if (GloomyHostile.worldState == 2) {
            float brightness = lerp(cir.getReturnValue(), thisObj.skylightSubtracted / 30f, transitionPoint);
            cir.setReturnValue(brightness);
        }
    }

    @Inject(method = "isDaytime", at = @At("RETURN"), cancellable = true)
    private void changeIsDaytime(CallbackInfoReturnable<Boolean> cir) {
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 2) {
            cir.setReturnValue(thisObj.skylightSubtracted < 15);
        }
    }
}
