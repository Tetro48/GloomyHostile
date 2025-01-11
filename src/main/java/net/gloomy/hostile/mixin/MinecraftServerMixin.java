package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.difficulty.Difficulties;
import btw.world.util.WorldUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow public WorldServer[] worldServers;

    private int oldWorldState;

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

        oldWorldState = GloomyHostile.worldState;
    }
    
    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        GloomyHostile.forcedStateDuration--;
        if (this.worldServers[0].getTotalWorldTime() % 20 != 0) return;
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