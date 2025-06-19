package net.gloomy.hostile.mixin;

import btw.block.blocks.TorchBlockBurningBase;
import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.src.Block;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TorchBlockBurningBase.class)
public abstract class TorchBlockBurningBaseMixin extends Block {
	protected TorchBlockBurningBaseMixin(int par1, Material par2Material) {
		super(par1, par2Material);
	}

	public boolean canEndermenPickUpBlock(World world, int x, int y, int z) {
		return GloomyHostile.worldState == 1 || GloomyHostile.worldState == 2;
	}
}
