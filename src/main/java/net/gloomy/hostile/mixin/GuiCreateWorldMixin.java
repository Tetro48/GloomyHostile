package net.gloomy.hostile.mixin;

import btw.AddonHandler;
import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.difficulty.Difficulties;
import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiCreateWorld.class)
public class GuiCreateWorldMixin extends GuiScreen {

    @Shadow private int difficultyID;

    @Inject(method = "actionPerformed", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/Minecraft;launchIntegratedServer(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/src/WorldSettings;)V"), cancellable = true)
    public void playSoundOnCreate(GuiButton par1GuiButton, CallbackInfo ci) {
        if (GloomyHostile.challengeWorldState == 1) this.mc.sndManager.playSound("mob.wither.death", 0f, 0f, 0f, Float.POSITIVE_INFINITY, 0.5f);
        if (GloomyHostile.challengeWorldState == 2) this.mc.sndManager.playSound("mob.wither.spawn", 0f, 0f, 0f, Float.POSITIVE_INFINITY, 0.5f);
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void drawScreen(int par1, int par2, float par3, CallbackInfo ci) {

        if (GloomyHostile.challengeWorldState > 0) {
            String str = "PITCH BLACK NIGHT ENSUES";
            if (GloomyHostile.challengeWorldState == 2) str = "THE ENVIRONMENTAL NIGHTMARE";
            if (AddonHandler.getModByID("nightmare_mode") != null && difficultyID == Difficulties.HOSTILE.ID) str = "PITCH BLACK DEATH ENSUES";
            drawSpacedOutWavyText(fontRenderer, str, (this.width - str.length() * 5 - fontRenderer.getStringWidth(str)) / 2, 5, 0xffffffff, 0xffff0000, 5, Minecraft.getSystemTime() / 5, 1);
        }
    }

    @Unique
    public void drawSpacedOutWavyText(FontRenderer fontRenderer, String str, int x, int y, int color, int shadowColor, int spacing, long time, int speed) {
        char[] strChars = new char[str.length()];
        str.getChars(0, str.length(), strChars, 0);
        int localX = x;
        for (int i = 0; i < str.length(); i++) {
            String strFromChar = String.valueOf(strChars[i]);
            int charWidth = fontRenderer.getCharWidth(strChars[i]);
            GL11.glPushMatrix();
            double sinInput = speed * (localX / 8d + time / 12d) / 8d;
            GL11.glTranslatef(localX, (float)(y + Math.sin(sinInput) * 3f), 0.0F);
            GL11.glRotatef((float) (Math.sin(sinInput + Math.PI / 2) * 6), 0.0F, 0.0F, 1.0F);
            fontRenderer.drawString(strFromChar, 1, 1, shadowColor);
            fontRenderer.drawString(strFromChar, 0, 0, color);
            GL11.glPopMatrix();
            localX += charWidth + spacing;
        }
    }
}
