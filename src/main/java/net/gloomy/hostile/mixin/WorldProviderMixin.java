package net.gloomy.hostile.mixin;

import btw.community.gloomyhostile.GloomyHostile;
import com.prupe.mcpatcher.cc.ColorizeWorld;
import com.prupe.mcpatcher.cc.Colorizer;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraft.src.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldProvider.class)
public abstract class WorldProviderMixin {
	@Shadow public World worldObj;

	@Unique
	private float lerp(float a, float b, float f) { return (a * (1.0f - f)) + (b * f); }

	//this is basically an override.
	@Inject(method = "getFogColor", at = @At("HEAD"), cancellable = true)
	private void darkenFog(float par1, float par2, CallbackInfoReturnable<Vec3> cir){
		if (GloomyHostile.worldState == 2) {
			float transitionPoint = Math.min((float) GloomyHostile.postWitherSunTicks / GloomyHostile.sunTransitionTime, 1f);
			float var3 = MathHelper.cos(par1 * (float) Math.PI * 2.0F) * 2.0F + 0.5F;
			if (var3 < 0.0F) {
				var3 = 0.0F;
			}

			if (var3 > 1.0F) {
				var3 = 1.0F;
			}

			var3 = lerp(var3, 0f, transitionPoint);

			float var4 = 0.7529412F;
			float var5 = 0.84705883F;
			float var6 = 1.0F;
			if (ColorizeWorld.computeFogColor((WorldProvider) (Object) this, par1)) {
				var4 = Colorizer.setColor[0];
				var5 = Colorizer.setColor[1];
				var6 = Colorizer.setColor[2];
			}

			var4 *= var3 * 0.94F + 0.06F;
			var5 *= var3 * 0.94F + 0.06F;
			var6 *= var3 * 0.91F + 0.09F;
			cir.setReturnValue(this.worldObj.getWorldVec3Pool().getVecFromPool((double) var4, (double) var5, (double) var6));
		}
	}
}
