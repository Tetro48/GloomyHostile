package net.gloomy.hostile.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.ResourceLocation;

@Mixin(RenderGlobal.class)
public class RenderGlobalMixin {
    @Unique private static final ResourceLocation blotSun = new ResourceLocation("textures/blotsun.png");
    @Unique private static final ResourceLocation blottingSun1 = new ResourceLocation("textures/blottingsun1.png");
    @Unique private static final ResourceLocation blottingSun2 = new ResourceLocation("textures/blottingsun2.png");
    @Unique private static final ResourceLocation blottingSun3 = new ResourceLocation("textures/blottingsun3.png");

    @ModifyArg(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/prupe/mcpatcher/sky/SkyRenderer;setupCelestialObject(Lnet/minecraft/src/ResourceLocation;)Lnet/minecraft/src/ResourceLocation;",ordinal = 0))
    private ResourceLocation manageSunTexture(ResourceLocation defaultTexture){
        if(GloomyHostile.worldState == 2){
            if (GloomyHostile.postWitherSunTicks < GloomyHostile.sunTransitionTime * 0.25) return defaultTexture;
            else if (GloomyHostile.postWitherSunTicks < GloomyHostile.sunTransitionTime * 0.5) return blottingSun1;
            else if (GloomyHostile.postWitherSunTicks < GloomyHostile.sunTransitionTime * 0.75) return blottingSun2;
            else if (GloomyHostile.postWitherSunTicks < GloomyHostile.sunTransitionTime) return blottingSun3;
            return blotSun;
        }
        return defaultTexture;
    }
}