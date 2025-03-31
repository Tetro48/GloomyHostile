package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.difficulty.Difficulties;
import btw.world.util.WorldUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow public WorldServer[] worldServers;

    @Unique
    private int oldWorldState;

    @Unique private int checksUntilRandomState = 0;
    @Unique private int cancerState = 0;

    @Inject(method = "initialWorldChunkLoad", at = @At("RETURN"))
    private void initialWorldChunkLoadMixin(CallbackInfo ci) {
        if (this.worldServers[0].worldInfo.getDifficulty() == Difficulties.HOSTILE || GloomyHostile.enableGloomEverywhere){
            if (WorldUtils.gameProgressHasEndDimensionBeenAccessedServerOnly() && !GloomyHostile.keepGloomPostDragon) {
                GloomyHostile.worldState = 3;
            }
            else if (WorldUtils.gameProgressHasWitherBeenSummonedServerOnly()) {
                GloomyHostile.worldState = 2;
            }
            else if (WorldUtils.gameProgressHasNetherBeenAccessedServerOnly()) {
                GloomyHostile.worldState = 1;
            }
            else
            {
                GloomyHostile.worldState = 0;
            }
        }
        else
        {
            GloomyHostile.worldState = 0;
        }
        if (GloomyHostile.worldState == 1 || GloomyHostile.worldState == 2) {
            GloomyHostile.postWitherSunTicks = 999;
            GloomyHostile.postNetherMoonTicks = 999;
        }
        GloomyHostile.worldState = Math.max(GloomyHostile.worldState, GloomyHostile.challengeWorldState);
        oldWorldState = GloomyHostile.worldState;
    }
    
    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        GloomyHostile.forcedStateDuration--;
        GloomyHostile.postNetherMoonDelay--;
        GloomyHostile.postWitherSunDelay--;
        if (this.worldServers[0].worldInfo.getDifficulty() == Difficulties.HOSTILE || GloomyHostile.enableGloomEverywhere) {
            if (GloomyHostile.postNetherMoonDelay == 0) {
                for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                    if (player instanceof EntityPlayerMP entityPlayerMP) {
                        ChatMessageComponent text2 = new ChatMessageComponent();
                        text2.addText("You feel a bit chilly, and gloomy...");
                        text2.setColor(EnumChatFormatting.GRAY);
                        text2.setItalic(true);
                        entityPlayerMP.sendChatToPlayer(text2);
                    }
                }
            }
            if (GloomyHostile.postWitherSunDelay == 0) {
                for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                    if (player instanceof EntityPlayerMP entityPlayerMP) {
                        ChatMessageComponent text2 = new ChatMessageComponent();
                        text2.addText("You feel that something's off with the Sun...");
                        text2.setColor(EnumChatFormatting.GRAY);
                        text2.setItalic(true);
                        entityPlayerMP.sendChatToPlayer(text2);
                    }
                }
            }
        }
        if (MinecraftServer.getIsServer()) {
            if (GloomyHostile.worldState == 2) {
                GloomyHostile.postWitherSunTicks++;
            }
            else GloomyHostile.postWitherSunTicks = 0;
            if (GloomyHostile.worldState == 1 || GloomyHostile.worldState == 2) {
                GloomyHostile.postNetherMoonTicks++;
            }
            else GloomyHostile.postNetherMoonTicks = 0;
        }
        if (this.worldServers[0].getTotalWorldTime() % 20 != 0) return;
        if (this.worldServers[0].worldInfo.getDifficulty() == Difficulties.HOSTILE || GloomyHostile.enableGloomEverywhere){
            if (WorldUtils.gameProgressHasEndDimensionBeenAccessedServerOnly() && !GloomyHostile.keepGloomPostDragon) {
                GloomyHostile.worldState = 3;
            }
            else if (WorldUtils.gameProgressHasWitherBeenSummonedServerOnly() && GloomyHostile.postWitherSunDelay <= 0) {
                GloomyHostile.worldState = 2;
            }
            else if (WorldUtils.gameProgressHasNetherBeenAccessedServerOnly() && GloomyHostile.postNetherMoonDelay <= 0) {
                GloomyHostile.worldState = 1;
            }
            else
            {
                GloomyHostile.worldState = 0;
            }
        }
        else
        {
            GloomyHostile.worldState = 0;
        }
        if (GloomyHostile.isCancerMode) {
            checksUntilRandomState--;
            if (checksUntilRandomState <= 0) {
                checksUntilRandomState = (int) Math.floor(4d + Math.random() * 8d);
                //guaranteed to be at most 2
                cancerState = (int)(Math.random() * 3d);
            }
        }
        GloomyHostile.worldState = Math.max(GloomyHostile.worldState, GloomyHostile.challengeWorldState);
        GloomyHostile.worldState = Math.max(GloomyHostile.worldState, cancerState);
        if (GloomyHostile.forcedStateDuration > 0) 
        {
            GloomyHostile.worldState = GloomyHostile.forcedWorldState;
        }
        if (GloomyHostile.worldState != oldWorldState) {
            GloomyHostile.sendWorldStateToAllPlayers();
        }
        oldWorldState = GloomyHostile.worldState;
    }
}