package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.difficulty.Difficulties;
import btw.world.util.WorldUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockPortal.class)
public class BlockPortalMixin {

    @Inject(method = "tryToCreatePortal", at = @At("TAIL"), cancellable = true)
    private void onCreatePortal(World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir){
        if (world.worldInfo.getDifficulty() != Difficulties.HOSTILE && !GloomyHostile.enableGloomEverywhere) return;
        world.playSoundEffect(x,y,z,"portal.travel",2.0F,0.75F);
        if (MinecraftServer.getServer() != null && GloomyHostile.worldState < 1) {
            if (MinecraftServer.getIsServer()){
                for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                    if (player instanceof EntityPlayerMP) {
                        ChatMessageComponent text2 = new ChatMessageComponent();
                        text2.addText("You feel a bit chilly, and gloomy...");
                        text2.setColor(EnumChatFormatting.GRAY);
                        text2.setItalic(true);
                        ((EntityPlayerMP) player).sendChatToPlayer(text2);
                    }
                }
            } else {
                EntityPlayer nearestPlayer = world.getClosestPlayer(x, y, z, -1);
                ChatMessageComponent text1 = new ChatMessageComponent();
                text1.addText("You feel a bit chilly, and gloomy...");
                text1.setColor(EnumChatFormatting.GRAY);
                text1.setItalic(true);
                nearestPlayer.sendChatToPlayer(text1);
                GloomyHostile.worldState = 1;
            }
        }
    }
}
