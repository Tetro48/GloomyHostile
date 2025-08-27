package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import com.prupe.mcpatcher.cc.ColorizeWorld;
import com.prupe.mcpatcher.cc.Colorizer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow public abstract float getRainStrength(float par1);
    @Shadow public abstract float getWeightedThunderStrength(float par1);
    @Shadow public abstract float getCelestialAngle(float par1);

    @Shadow public boolean isRemote;

    @Shadow private static double[] moonBrightnessByPhase;

    @Shadow @Final public WorldProvider provider;

    @Shadow public abstract long getWorldTime();

    @Shadow public abstract BiomeGenBase getBiomeGenForCoords(int par1, int par2);

    @Shadow public int lastLightningBolt;

    @Shadow public abstract Vec3Pool getWorldVec3Pool();

    @Shadow private long cloudColour;

    @Inject(method = "computeOverworldSunBrightnessWithMoonPhases", at = @At("HEAD"), remap = false, cancellable = true)
    public void manageLightLevels(CallbackInfoReturnable<Float> cir) {
        if (GloomyHostile.worldState == 1 || GloomyHostile.worldState == 2) {
            float moonTransitionPoint = Math.min((float) GloomyHostile.postNetherMoonTicks / GloomyHostile.moonTransitionTime, 1f);
            float sunTransitionPoint = Math.min((float) GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1f);
            long lOffsetWorldTime = this.worldInfo.getWorldTime() - 12000L;
            if (lOffsetWorldTime < 0L) {
                lOffsetWorldTime = 0L;
            }

            int iMoonPhase = (int) (lOffsetWorldTime / 24000L % 8L);
            double dMoonBrightness = lerp(moonBrightnessByPhase[iMoonPhase], 0.0D, moonTransitionPoint);
            float fCelestialAngle = this.getCelestialAngle(1.0F);
            float fSunInvertedBrightness = 1.0F - (MathHelper.cos(fCelestialAngle * (float) Math.PI * 2.0F) * 2.0F + 0.25F);
            if (fSunInvertedBrightness < 0.0F) {
                fSunInvertedBrightness = 0.0F;
            } else if (fSunInvertedBrightness > 1.0F) {
                fSunInvertedBrightness = 1.0F;
            }

            double dSunBrightness = lerp(1.0D - (double) fSunInvertedBrightness, (1.0D - (double) fSunInvertedBrightness) / 8D, sunTransitionPoint);
            double dRainBrightnessModifier = (double) 1.0F - (double) (this.getRainStrength(1.0F) * 5.0F) / (double) 16.0F;
            double dStormBrightnessModifier = (double) 1.0F - (double) (this.getWeightedThunderStrength(1.0F) * 5.0F) / (double) 16.0F;
            dSunBrightness = dSunBrightness * dRainBrightnessModifier * dStormBrightnessModifier;
            double dMinBrightness = 0.2;
            dMinBrightness *= dMoonBrightness * dRainBrightnessModifier * dStormBrightnessModifier;
            if (dMinBrightness < 0.05) {
                dMinBrightness = 0;
            }

            cir.setReturnValue((float) (dSunBrightness * (1.0D - dMinBrightness) + dMinBrightness));
        }
    }
    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci){
        World thisObj = (World)(Object)this;
        if (thisObj.provider.dimensionId == 0 && this.isRemote){
            if (GloomyHostile.worldState == 2) {
                GloomyHostile.postWitherSunTicks++;
                if (GloomyHostile.postWitherSunTicks == 1 && GloomyHostile.windNoises) {
                    Minecraft.getMinecraft().thePlayer.playSound("gloomyhostile:wind",3.4E38F,0.5F);
                }
                if (GloomyHostile.postWitherSunTicks == GloomyHostile.sunTransitionTime && GloomyHostile.celestialNoises) {
                    Minecraft.getMinecraft().thePlayer.playSound("mob.wither.spawn",3.4E38F,0.5F);
                }
            }
            else GloomyHostile.postWitherSunTicks = 0;
            if (GloomyHostile.worldState == 1 || GloomyHostile.worldState == 2) {
                GloomyHostile.postNetherMoonTicks++;
                if (GloomyHostile.postNetherMoonTicks == 1 && GloomyHostile.worldState == 1 && GloomyHostile.windNoises) {
                    Minecraft.getMinecraft().thePlayer.playSound("gloomyhostile:wind",3.4E38F,0.75F);
                }
                if (GloomyHostile.postNetherMoonTicks == GloomyHostile.moonTransitionTime && GloomyHostile.worldState == 1 && GloomyHostile.celestialNoises) {
                    Minecraft.getMinecraft().thePlayer.playSound("mob.wither.death",3.4E38F,0.5F);
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
            cir.setReturnValue((int) lerp((float) cir.getReturnValue(), 4f,
                 Math.min((float) GloomyHostile.postNetherMoonTicks / GloomyHostile.moonTransitionTime, 1f)));
            long days = (this.worldInfo.getWorldTime()) / 24000L;
            if (GloomyHostile.isNightmareModeInstalled && !this.isRemote) {
                if (days % 16 == 8) cir.setReturnValue(0); // just for bloodmoon to exist, whilst also being visually new moon.
            }
        }
    }
    @Unique private float lerp(float a, float b, float f) { return (a * (1.0f - f)) + (b * f); }
    @Unique private double lerp(double a, double b, double f)
    {
        return (a * (1.0d - f)) + (b * f);
    }

    /* ! WARNING ! This will modify how isDaytime works! ! WARNING ! */
    @Inject(method = "calculateSkylightSubtracted", at = @At("RETURN"), cancellable = true)
    private void subtractSkylight(CallbackInfoReturnable<Integer> cir){
        if (GloomyHostile.worldState == 2) {
            cir.setReturnValue(Math.min(cir.getReturnValue() + 11, 15));
        }
    }

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void darkenSky(Entity par1Entity, float par2, CallbackInfoReturnable<Vec3> cir) {
        if (GloomyHostile.worldState == 2) {
            float transitionPoint = Math.min((float) GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1f);
            float var3 = this.getCelestialAngle(par2);
            float var4 = MathHelper.cos(var3 * (float) Math.PI * 2.0F) * 2.0F + 0.5F;
            if (var4 < 0.0F) {
                var4 = 0.0F;
            }

            if (var4 > 1.0F) {
                var4 = 1.0F;
            }

            var4 = lerp(var4, 0f, transitionPoint);

            int var5 = MathHelper.floor_double(par1Entity.posX);
            int var6 = MathHelper.floor_double(par1Entity.posZ);
            BiomeGenBase var7 = this.getBiomeGenForCoords(var5, var6);
            float var8 = var7.getFloatTemperature();
            int var9 = var7.getSkyColorByTemp(var8);
            ColorizeWorld.setupForFog(par1Entity);
            float var10;
            float var11;
            float var12;
            if (ColorizeWorld.computeSkyColor((World) (Object) this, par2)) {
                var10 = Colorizer.setColor[0];
                var11 = Colorizer.setColor[1];
                var12 = Colorizer.setColor[2];
            } else {
                var10 = (float) (var9 >> 16 & 255) / 255.0F;
                var11 = (float) (var9 >> 8 & 255) / 255.0F;
                var12 = (float) (var9 & 255) / 255.0F;
            }

            var10 *= var4;
            var11 *= var4;
            var12 *= var4;
            float var13 = this.getRainStrength(par2);
            if (var13 > 0.0F) {
                float var14 = (var10 * 0.3F + var11 * 0.59F + var12 * 0.11F) * 0.6F;
                float var15 = 1.0F - var13 * 0.75F;
                var10 = var10 * var15 + var14 * (1.0F - var15);
                var11 = var11 * var15 + var14 * (1.0F - var15);
                var12 = var12 * var15 + var14 * (1.0F - var15);
            }

            float var14 = this.getWeightedThunderStrength(par2);
            if (var14 > 0.0F) {
                float var15 = (var10 * 0.3F + var11 * 0.59F + var12 * 0.11F) * 0.2F;
                float var16 = 1.0F - var14 * 0.75F;
                var10 = var10 * var16 + var15 * (1.0F - var16);
                var11 = var11 * var16 + var15 * (1.0F - var16);
                var12 = var12 * var16 + var15 * (1.0F - var16);
            }

            if (this.lastLightningBolt > 0) {
                float var15 = (float) this.lastLightningBolt - par2;
                if (var15 > 1.0F) {
                    var15 = 1.0F;
                }

                var15 *= 0.45F;
                var10 = var10 * (1.0F - var15) + 0.8F * var15;
                var11 = var11 * (1.0F - var15) + 0.8F * var15;
                var12 = var12 * (1.0F - var15) + 1.0F * var15;
            }

            cir.setReturnValue(this.getWorldVec3Pool().getVecFromPool((double) var10, (double) var11, (double) var12));
        }
    }

    @Inject(method = "getCloudColour", at = @At("HEAD"), cancellable = true)
    private void darkenClouds(float par1, CallbackInfoReturnable<Vec3> cir){
        if (GloomyHostile.worldState == 2) {
            float transitionPoint = Math.min((float) GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1f);
            float var2 = this.getCelestialAngle(par1);
            float var3 = MathHelper.cos(var2 * (float) Math.PI * 2.0F) * 2.0F + 0.5F;
            if (var3 < 0.0F) {
                var3 = 0.0F;
            }

            if (var3 > 1.0F) {
                var3 = 1.0F;
            }

            var3 = lerp(var3, 0f, transitionPoint);

            float var4 = (float) (this.cloudColour >> 16 & 255L) / 255.0F;
            float var5 = (float) (this.cloudColour >> 8 & 255L) / 255.0F;
            float var6 = (float) (this.cloudColour & 255L) / 255.0F;
            float var7 = this.getRainStrength(par1);
            if (var7 > 0.0F) {
                float var8 = (var4 * 0.3F + var5 * 0.59F + var6 * 0.11F) * 0.6F;
                float var9 = 1.0F - var7 * 0.95F;
                var4 = var4 * var9 + var8 * (1.0F - var9);
                var5 = var5 * var9 + var8 * (1.0F - var9);
                var6 = var6 * var9 + var8 * (1.0F - var9);
            }

            var4 *= var3 * 0.9F + 0.1F;
            var5 *= var3 * 0.9F + 0.1F;
            var6 *= var3 * 0.85F + 0.15F;
            float var8 = this.getWeightedThunderStrength(par1);
            if (var8 > 0.0F) {
                float var9 = (var4 * 0.3F + var5 * 0.59F + var6 * 0.11F) * 0.2F;
                float var10 = 1.0F - var8 * 0.95F;
                var4 = var4 * var10 + var9 * (1.0F - var10);
                var5 = var5 * var10 + var9 * (1.0F - var10);
                var6 = var6 * var10 + var9 * (1.0F - var10);
            }

            cir.setReturnValue(this.getWorldVec3Pool().getVecFromPool((double) var4, (double) var5, (double) var6));
        }
    }

    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true)
    private void showStars(float par1, CallbackInfoReturnable<Float> cir){
        float transitionPoint = Math.min((float) GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1f);
        cir.setReturnValue(lerp(cir.getReturnValue(), 0.5f, transitionPoint));
    }

    @Inject(method = "isDaytime", at = @At("RETURN"), cancellable = true)
    private void changeIsDaytime(CallbackInfoReturnable<Boolean> cir) {
        World thisObj = (World)(Object)this;
        if (GloomyHostile.worldState == 2) {
            cir.setReturnValue(thisObj.skylightSubtracted < 15);
        }
    }
}
