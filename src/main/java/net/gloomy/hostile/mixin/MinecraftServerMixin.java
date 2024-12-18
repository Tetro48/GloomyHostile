package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.difficulty.Difficulties;
import btw.world.util.WorldUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow public WorldServer[] worldServers;

    @Inject(method = "initialWorldChunkLoad", at = @At("RETURN"))
    private void initialWorldChunkLoadMixin(CallbackInfo ci) {
        if (this.worldServers[0].worldInfo.getDifficulty() == Difficulties.HOSTILE || GloomyHostile.enableGloomEverywhere){
            if (WorldUtils.gameProgressHasEndDimensionBeenAccessedServerOnly() && !GloomyHostile.keepGloomPostDragon) {
                this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 3);
            }
            else if (WorldUtils.gameProgressHasWitherBeenSummonedServerOnly()) {
                this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 2);
            }
            else if (WorldUtils.gameProgressHasNetherBeenAccessedServerOnly()) {
                this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 1);
            }
            else
            {
                this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 0);
            }
        }
        else
        {
            this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 0);
        }
        GloomyHostile.worldState = this.worldServers[0].getData(GloomyHostile.WORLD_STATE);
    }
    
    @Inject(method = "tick", at = @At("RETURN"), cancellable = true)
    private void tick(CallbackInfo ci) {
        if (this.worldServers[0].getTotalWorldTime() % 120 != 0) return;
        if (this.worldServers[0].worldInfo.getDifficulty() == Difficulties.HOSTILE || GloomyHostile.enableGloomEverywhere){
            if (WorldUtils.gameProgressHasEndDimensionBeenAccessedServerOnly() && !GloomyHostile.keepGloomPostDragon) {
                this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 3);
            }
            else if (WorldUtils.gameProgressHasWitherBeenSummonedServerOnly()) {
                this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 2);
            }
            else if (WorldUtils.gameProgressHasNetherBeenAccessedServerOnly()) {
                this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 1);
            }
            else
            {
                this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 0);
            }
        }
        else
        {
            this.worldServers[0].setData(GloomyHostile.WORLD_STATE, 0);
        }
        if (GloomyHostile.worldState != this.worldServers[0].getData(GloomyHostile.WORLD_STATE)) {
            GloomyHostile.sendWorldStateToAllPlayers();
        }
        GloomyHostile.worldState = this.worldServers[0].getData(GloomyHostile.WORLD_STATE);
    }
}