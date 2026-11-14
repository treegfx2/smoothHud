package dev.smoothhud.mixin;

import dev.smoothhud.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Shadow
	@Nullable
	protected abstract PlayerEntity getCameraPlayer();

	@Shadow
	@Final
	private MinecraftClient client;
	@Unique
	private float currentX = 0;
	@Unique
	private long lastTickTime = -1;
	@Unique
	private boolean reset = true;
	@Unique
	private static PlayerEntity lastPlayer = null;

	@Inject(
			method = "renderHotbar",
			at = @At("HEAD")
	)
	private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		// todo: move non-immediate lookups like this to onTick or something
		Entity cameraEntity = this.client.getCameraEntity();
		if (cameraEntity instanceof PlayerEntity player) {
			// reset stale variables after player recreation
			if (player != lastPlayer) { reset = true; lastPlayer = player; }

			long now = System.currentTimeMillis();
			float targetX = Math.clamp(player.getInventory().getSelectedSlot(), 0, 8) * 20;

			if (reset) {
				lastTickTime = now;
				currentX = targetX;
				reset = false;
			}

			float deltaTime = Math.min((now - lastTickTime) / 1000f, 0.1f);
			lastTickTime = now;

			float deltaX = targetX - currentX;

			if (Math.abs(deltaX) < 0.1f) {
				currentX = targetX;
			} else {
				float t = Math.min(deltaTime * ConfigManager.getConfig().speed, 1f);
				currentX += deltaX * t;
			}
		}
	}

	@ModifyArgs(
			method = "renderHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",
					ordinal = 1
			)
	)
	private void mod(Args args) {
		int baseX = (MinecraftClient.getInstance().getWindow().getScaledWidth() / 2) - 91 - 1;
		args.set(2, Math.round(baseX + currentX));
	}
}
