package org.vivecraft.client.gui.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.SettingsList;

import java.util.List;

public abstract class GuiListScreen extends Screen {

    protected final Screen lastScreen;

    protected SettingsList list;

    protected boolean reinit = false;

    public GuiListScreen(Component title, Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        clearWidgets();
        double scrollAmount = list != null ? list.getScrollAmount() : 0.0D;

        this.list = new SettingsList(this, minecraft, getEntries());
        list.setScrollAmount(scrollAmount);
        this.addWidget(this.list);
        this.addRenderableWidget(new Button(
            this.width / 2 - 100, this.height - 27,
            200, 20,
            CommonComponents.GUI_DONE,
            button -> this.minecraft.setScreen(this.lastScreen)));
    }

    protected abstract List<SettingsList.BaseEntry> getEntries();

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (reinit) {
            init();
            reinit = false;
        }
        this.renderBackground(poseStack);
        list.render(poseStack, i, j, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, i, j, f);
        GuiEventListener widget = list.getChildAt(i, j).orElse(null);
        if (widget instanceof ContainerEventHandler container
            && !container.children().isEmpty()
            && container.children().get(0) instanceof TooltipAccessor
            && container.children().get(0).isMouseOver(i, j)) {
            renderTooltip(poseStack, ((TooltipAccessor) container.children().get(0)).getTooltip(), i, j);
        }
    }
}
