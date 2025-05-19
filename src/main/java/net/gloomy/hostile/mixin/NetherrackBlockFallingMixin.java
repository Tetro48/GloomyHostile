package net.gloomy.hostile.mixin;

import btw.block.blocks.NetherrackBlockFalling;
import btw.community.gloomyhostile.GloomyHostile;
import net.minecraft.src.Block;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NetherrackBlockFalling.class)
public class NetherrackBlockFallingMixin extends Block {
	protected NetherrackBlockFallingMixin(int par1, Material par2Material) {
		super(par1, par2Material);
	}

	public boolean canEndermenPickUpBlock(World world, int x, int y, int z) {
		return GloomyHostile.worldState == 1 || GloomyHostile.worldState == 2;
	}
}
