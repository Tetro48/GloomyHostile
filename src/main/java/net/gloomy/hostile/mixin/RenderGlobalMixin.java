package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderGlobal.class)
public class RenderGlobalMixin {
    @Unique
    private static final ResourceLocation blotSun = new ResourceLocation("textures/blotsun.png");

    @ModifyArg(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/prupe/mcpatcher/sky/SkyRenderer;setupCelestialObject(Lnet/minecraft/src/ResourceLocation;)Lnet/minecraft/src/ResourceLocation;",ordinal = 0))
    private ResourceLocation manageSunTexture(ResourceLocation defaultTexture){
        if(GloomyHostile.worldState == 2){
            return blotSun;
        }
        return defaultTexture;
    }
}