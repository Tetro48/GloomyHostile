package net.gloomy.hostile;

import net.minecraft.src.ResourceLocation;

import java.util.List;

public class TransitionalTextureManager {
	private final List<ResourceLocation> textures;
	private final int amount;
	public TransitionalTextureManager(List<ResourceLocation> textures) {
		this.textures = textures;
		this.amount = textures.size();
	}
	public ResourceLocation getTextureAtSpecificPoint(double time) {
		int textureID = (int) Math.min(Math.max((amount-1) * time, 0), amount-1);
		return textures.get(textureID);
	}
}
