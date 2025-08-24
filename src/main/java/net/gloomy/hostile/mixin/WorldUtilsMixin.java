package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.WorldUtils;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldUtils.class)
public abstract class WorldUtilsMixin {
    @Inject(method = "gameProgressSetNetherBeenAccessedServerOnly", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/WorldServer;setData(Lbtw/world/util/data/DataEntry$WorldDataEntry;Ljava/lang/Object;)V"))
    private static void onNetherPortalOpen(CallbackInfo ci) {
        if (MinecraftServer.getServer() != null) {
            if (!WorldUtils.gameProgressHasNetherBeenAccessedServerOnly()) {
                long overworldTime = MinecraftServer.getServer().worldServers[0].getWorldTime();
                if (MinecraftServer.getServer().worldServers[0].isDaytime()) {
                    GloomyHostile.postNetherMoonDelay = 13500 - overworldTime % 24000;
                } else {
                    GloomyHostile.postNetherMoonDelay = 1;
                }
            }
        }
    }
    @Inject(method = "gameProgressSetWitherHasBeenSummonedServerOnly", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/WorldServer;setData(Lbtw/world/util/data/DataEntry$WorldDataEntry;Ljava/lang/Object;)V"))
    private static void onWitherSummon(CallbackInfo ci) {
        if (MinecraftServer.getServer() != null) {
            if (!WorldUtils.gameProgressHasWitherBeenSummonedServerOnly()) {
                long overworldTime = MinecraftServer.getServer().worldServers[0].getWorldTime();
                if (MinecraftServer.getServer().worldServers[0].isDaytime()) {
                    GloomyHostile.postWitherSunDelay = 1;
                } else {
                    GloomyHostile.postWitherSunDelay = 25000 - overworldTime % 24000;
                }
            }
        }
    }
}
