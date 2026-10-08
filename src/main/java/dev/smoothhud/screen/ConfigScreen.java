package dev.smoothhud.screen;

import dev.smoothhud.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;


@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {
    private final Screen parent;
    private int selectedSlot = 0;
    private float tempSpeed = ConfigManager.getConfig().speed;
    private long lastTickTime = 0;
    private float currentX;

    public ConfigScreen(Screen parent) {
        super(Text.of("SmoothHud Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(
                new SliderWidget(this.width / 2 - 100, this.height / 4 + 24, 200, 20, Text.of("Speed: " + (int) tempSpeed), ((int) tempSpeed - 2) / 38.0F) {
                    @Override
                    protected void updateMessage() {
                        tempSpeed = (int) (this.value * 38) + 2;
                        this.setMessage(Text.of("Speed: " + (int) tempSpeed));
                    }

                    @Override
                    protected void applyValue() {}
                }
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.of("Save & Exit"), (x) -> {
                            assert this.client != null;
                            ConfigManager.getConfig().speed = tempSpeed;
                            ConfigManager.saveConfig();
                            this.client.setScreen(parent);
                }).dimensions(this.width / 2 - 100, this.height / 4 + 48, 200, 20).build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.of("Cancel"), (button) -> {
                    assert this.client != null;
                    this.client.setScreen(parent);
                }).dimensions(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //simple ass hotkey feature
        if (keyCode >= 49 && keyCode <= 57) {
            selectedSlot = keyCode - 49;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        verticalAmount = MathHelper.clamp(verticalAmount, -1.0F, 1.0F);
        //scrolling
        if (verticalAmount > 0) {
            selectedSlot = (selectedSlot - 1 + 9) % 9;
        } else if (verticalAmount < 0) {
            selectedSlot = (selectedSlot + 1) % 9;
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        float targetX = selectedSlot * 20;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTickTime) / 1000f;
        lastTickTime = currentTime;

        if (Math.abs(targetX - currentX) > 0.1f) {
            float diff = targetX - currentX;
            currentX += diff * deltaTime * tempSpeed;
        }
        int hotbarX = this.width / 2 - 91;
        int hotbarY = this.height / 4 + 100;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                Identifier.of("minecraft", "textures/gui/sprites/hud/hotbar.png"),
                hotbarX, hotbarY,
                0, 0, 182, 22,
                182, 22, 182, 22
        );

        int roundedCurrentX = Math.round(currentX) - 1;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                Identifier.of("minecraft", "textures/gui/sprites/hud/hotbar_selection.png"),
                hotbarX + roundedCurrentX, hotbarY - 1,
                0, 0, 24, 23,
                24, 23, 24, 23
        );

        long time = System.currentTimeMillis();
        String message = "Scroll or press hotkeys to preview";

        int x = (this.width - this.textRenderer.getWidth(message)) / 2;
        int y = hotbarY + 25;

        int color = ((time / 300) % 2 == 0) ? 0xFF555555 : 0xFF666666;

        context.drawText(this.textRenderer, message, x, y, color, true);
    }
}