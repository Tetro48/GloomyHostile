package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.WorldUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityWither.class)
public class EntityWitherMixin {
    @Inject(method = "summonWitherAtLocation", at = @At("HEAD"))
    private static void sendMessageOnSummon(World world, int i, int j, int k, CallbackInfo ci) {
        if (WorldUtils.gameProgressHasWitherBeenSummonedServerOnly() || GloomyHostile.worldState >= 2) {
            return;
        }
        if (MinecraftServer.getIsServer()) {
            for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                if (player instanceof EntityPlayerMP) {
                    ChatMessageComponent text2 = new ChatMessageComponent();
                    text2.addText("You feel that something's off with the Sun...");
                    text2.setColor(EnumChatFormatting.GRAY);
                    text2.setItalic(true);
                    ((EntityPlayerMP) player).sendChatToPlayer(text2);
                }
            }
        } else {
            EntityPlayer nearestPlayer = world.getClosestPlayer(i, j, k, -1);
            ChatMessageComponent text1 = new ChatMessageComponent();
            text1.addText("You feel that something's off with the Sun...");
            text1.setColor(EnumChatFormatting.GRAY);
            text1.setItalic(true);
            nearestPlayer.sendChatToPlayer(text1);
            GloomyHostile.worldState = 1;
        }
    }
}
