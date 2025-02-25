package org.vivecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.client.gui.widgets.TextScrollWidget;
import org.vivecraft.client.utils.UpdateChecker;


public class UpdateScreen extends Screen {

    private final Screen lastScreen;
    private TextScrollWidget text;

    public UpdateScreen() {
        super(new TranslatableComponent("vivecraft.messages.updateTitle"));
        lastScreen = Minecraft.getInstance().screen;
    }

    protected void init() {

        text = this.addRenderableWidget(new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 60, UpdateChecker.changelog));

        this.addRenderableWidget(new Button(
            this.width / 2 - 155, this.height - 56, 150, 20,
            new TranslatableComponent("vivecraft.gui.downloadfrom", new TextComponent("Modrinth")),
            button ->
                this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
                    if (bl) {
                        Util.getPlatform().openUri("https://modrinth.com/mod/vivecraft");
                    }
                    this.minecraft.setScreen(this);
                }, "https://modrinth.com/mod/vivecraft", true))
        ));

        this.addRenderableWidget(new Button(
            this.width / 2 + 5, this.height - 56, 150, 20,
            new TranslatableComponent("vivecraft.gui.downloadfrom", new TextComponent("CurseForge")),
            button ->
                this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
                    if (bl) {
                        Util.getPlatform().openUri("https://www.curseforge.com/minecraft/mc-mods/vivecraft");
                    }
                    this.minecraft.setScreen(this);
                }, "https://www.curseforge.com/minecraft/mc-mods/vivecraft", true))
        ));

        this.addRenderableWidget(new Button(
            this.width / 2 - 75, this.height - 32,
            150, 20,
            new TranslatableComponent("gui.back"),
            (p) -> Minecraft.getInstance().setScreen(this.lastScreen)));
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

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }
}
