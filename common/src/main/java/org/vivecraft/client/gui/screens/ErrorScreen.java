package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.widgets.TextScrollWidget;

public class ErrorScreen extends Screen {

    private final Screen lastScreen;
    private final Component error;
    private TextScrollWidget text;

    public ErrorScreen(String title, Component error) {
        super(new TextComponent(title));
        lastScreen = Minecraft.getInstance().screen;
        this.error = error;
    }

    protected void init() {

        text = this.addRenderableWidget(new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 36, error));

        this.addRenderableWidget(new Button(
            this.width / 2 + 5, this.height - 32,
            150, 20,
            new TranslatableComponent("gui.back"),
            (p) -> Minecraft.getInstance().setScreen(this.lastScreen)));
        this.addRenderableWidget(new Button(
            this.width / 2 - 155, this.height - 32,
            150, 20,
            new TranslatableComponent("chat.copy.click"),
            (p) -> Minecraft.getInstance().keyboardHandler.setClipboard(this.title.getString() + "\n" + error.getString())));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);

        super.render(poseStack, i, j, f);

        Style style = text.getMouseover(i, j);
        if (style != null) {
            renderComponentHoverEffect(poseStack, style, i, j);
        }
    }
}
