package dev.smoothhud.mixin;

import dev.smoothhud.ConfigManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Hud.class)
public abstract class InGameHudMixin {
	@Final
	@Shadow
	private Minecraft minecraft;

	@Unique
	private float currentX = 0;
	@Unique
	private long lastTickTime = -1;
	@Unique
	private boolean reset = true;
	@Unique
	private static Player lastPlayer = null;

	@Inject(
			method = "extractItemHotbar",
			at = @At("HEAD")
	)
	private void onRenderHotbar(CallbackInfo info) {
		// todo: move non-immediate lookups like this to onTick or something
		Player player = (Player) this.minecraft.getCameraEntity();
		if (player != null) {
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

			float diffX = targetX - currentX;

			if (Math.abs(diffX) < 0.1f) {
				currentX = targetX;
			} else {
				float t = Math.min(deltaTime * ConfigManager.getConfig().speed, 1f);
				currentX += diffX * t;
			}
		}
	}

	@ModifyArgs(
			method = "extractItemHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
					ordinal = 1
			)
	)
	private void mod(Args args, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		int baseX = (graphics.guiWidth() / 2) - 91 - 1;

		args.set(2, Math.round(baseX + currentX));
	}
}