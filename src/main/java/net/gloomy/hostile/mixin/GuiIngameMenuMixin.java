package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiIngameMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class GuiIngameMenuMixin {
    @Inject(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/Minecraft;loadWorld(Lnet/minecraft/src/WorldClient;)V", ordinal = 0))
    public void onLeaveWorld(GuiButton par1GuiButton, CallbackInfo ci) {
        GloomyHostile.worldState = 0;
        GloomyHostile.forcedStateDuration = 0;
        GloomyHostile.forcedWorldState = 0;
    }
}
