package net.gloomy.hostile.mixin;

import net.gloomy.hostile.TransitionalTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.ResourceLocation;

import java.util.List;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    @Unique private static final TransitionalTextureManager eclipseSunTextures = new TransitionalTextureManager(List.of(
            new ResourceLocation("textures/environment/sun.png"),
            new ResourceLocation("gloomyhostile", "textures/suneclipse/eclipsingsun1.png"),
            new ResourceLocation("gloomyhostile", "textures/suneclipse/eclipsingsun2.png"),
            new ResourceLocation("gloomyhostile", "textures/suneclipse/eclipsingsun3.png"),
            new ResourceLocation("gloomyhostile", "textures/suneclipse/eclipsingsun4.png"),
            new ResourceLocation("gloomyhostile", "textures/suneclipse/eclipsedsun.png")
    ));
    @Unique private static final TransitionalTextureManager fadingSunTextures = new TransitionalTextureManager(List.of(
            new ResourceLocation("textures/environment/sun.png"),
            new ResourceLocation("gloomyhostile", "textures/sunfade/fadingsun1.png"),
            new ResourceLocation("gloomyhostile", "textures/sunfade/fadingsun2.png"),
            new ResourceLocation("gloomyhostile", "textures/sunfade/fadingsun3.png"),
            new ResourceLocation("gloomyhostile", "textures/sunfade/fadingsun4.png"),
            new ResourceLocation("gloomyhostile", "textures/sunfade/fadedsun.png")
    ));
    @Unique private static final TransitionalTextureManager legacyBlottingSunTextures = new TransitionalTextureManager(List.of(
            new ResourceLocation("textures/environment/sun.png"),
            new ResourceLocation("gloomyhostile", "textures/legacy/blottingsun1.png"),
            new ResourceLocation("gloomyhostile", "textures/legacy/blottingsun2.png"),
            new ResourceLocation("gloomyhostile", "textures/legacy/blottingsun3.png"),
            new ResourceLocation("gloomyhostile", "textures/legacy/blottingsun4.png"),
            new ResourceLocation("gloomyhostile", "textures/legacy/blotsun.png")
    ));

    @ModifyArg(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/prupe/mcpatcher/sky/SkyRenderer;setupCelestialObject(Lnet/minecraft/src/ResourceLocation;)Lnet/minecraft/src/ResourceLocation;",ordinal = 0))
    private ResourceLocation manageSunTexture(ResourceLocation defaultTexture){
        if(GloomyHostile.worldState == 2){
            double scaledTime = (double) GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime;
            return switch (GloomyHostile.visualSunType) {
                case 1 -> eclipseSunTextures.getTextureAtSpecificPoint(scaledTime);
                case 2 -> fadingSunTextures.getTextureAtSpecificPoint(scaledTime);
	            default -> legacyBlottingSunTextures.getTextureAtSpecificPoint(scaledTime);
            };
        }
        return defaultTexture;
    }
}