package net.gloomy.hostile.mixin;

import btw.block.tileentity.UnfiredBrickTileEntity;
import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.src.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UnfiredBrickTileEntity.class)
public abstract class UnfiredBrickTileEntityMixin extends TileEntity {
    @Unique private int innerTickCounter;
    @Shadow private int cookCounter;
    @Shadow private boolean isCooking;

    @ModifyVariable(method = "updateCooking", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/World;getBlockId(III)I", ordinal = 0), remap = false)
    private boolean cookOnPostWitherSun(boolean original) {
        if (GloomyHostile.worldState == 2) {
            int iBlockMaxNaturalLight = this.worldObj.getBlockNaturalLightValueMaximum(this.xCoord, this.yCoord, this.zCoord);
            int iBlockCurrentNaturalLight = iBlockMaxNaturalLight - this.worldObj.skylightSubtracted;
            return iBlockCurrentNaturalLight >= 4;
        }
        return original;
    }

    @Inject(method = "updateCooking", at = @At(value = "FIELD", target = "Lbtw/block/tileentity/UnfiredBrickTileEntity;cookCounter:I", ordinal = 0), remap = false)
    public void modifyCookingCode(CallbackInfo ci){
        if (GloomyHostile.worldState == 2 && isCooking) {
            innerTickCounter++;
            if (innerTickCounter % 4 != 0) cookCounter--;
            System.out.println(cookCounter + ", innercount: " + innerTickCounter);
        }
    }
}