package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import btw.world.util.difficulty.Difficulties;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPortal.class)
public class BlockPortalMixin {

    @Inject(method = "tryToCreatePortal", at = @At("TAIL"))
    private void onCreatePortal(World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir){
        if (world.worldInfo.getDifficulty() != Difficulties.HOSTILE && !GloomyHostile.enableGloomEverywhere) return;
        world.playSoundEffect(x,y,z,"portal.travel",2.0F,0.75F);
    }
}
