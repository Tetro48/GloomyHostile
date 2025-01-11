package net.gloomy.hostile.mixin;


import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin {

    @Redirect(method = "sleepInBedAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;isDaytime()Z"))
    private boolean stopBedSleep(World world){
        if (GloomyHostile.worldState != 2) return world.isDaytime();
        return world.skylightSubtracted < 15;
    }
}
