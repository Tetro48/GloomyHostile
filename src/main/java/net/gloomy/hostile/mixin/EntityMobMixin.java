package net.gloomy.hostile.mixin;

import net.minecraft.src.EntityCreature;
import net.minecraft.src.EntityMob;
import net.minecraft.src.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityMob.class)
public class EntityMobMixin {

    //An attempt of preventing mobs from burning when sun is dark enough
    @Redirect(method = "checkForCatchFireInSun", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;isDaytime()Z"))
    public boolean keepOriginalIsDaytimeCheck(World instance) {
        return instance.skylightSubtracted < 4;
    }
}
