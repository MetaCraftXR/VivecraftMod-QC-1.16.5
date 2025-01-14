package org.vivecraft.client.gui.settings;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.server.config.ConfigBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiListValueEditScreen extends GuiListScreen {

    protected final ConfigBuilder.ListValue<String> listValue;
    private List<String> elements;

    public GuiListValueEditScreen(Component title, Screen lastScreen, ConfigBuilder.ListValue<String> listValue) {
        super(title, lastScreen);
        this.listValue = listValue;
    }

    @Override
    protected void init() {
        clearWidgets();
        double scrollAmount = list != null ? list.getScrollAmount() : 0.0D;

        this.list = new SettingsList(this, minecraft, getEntries());
        list.setScrollAmount(scrollAmount);
        this.addWidget(this.list);

        this.addRenderableWidget(new Button(
            this.width / 2 - 155, this.height - 27,
            150, 20,
            CommonComponents.GUI_DONE,
            button -> {
                listValue.set(getCurrentValues());
                this.minecraft.setScreen(this.lastScreen);
            }));

        this.addRenderableWidget(new Button(
            this.width / 2 + 5, this.height - 27,
            150, 20,
            CommonComponents.GUI_CANCEL,
            button -> this.minecraft.setScreen(this.lastScreen)));
    }

    private List<String> getCurrentValues() {
        return list.children().stream().map(entry -> {
            if (entry instanceof ListValueEntry listValueEntry) {
                return listValueEntry.getString();
            } else {
                return "";
            }
        }).filter(string -> !string.isEmpty()).collect(Collectors.toList());
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        if (elements == null) {
            elements = new ArrayList<>(listValue.get());
        }
        int i = 0;
        for (String item : elements) {
            EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, ListValueEntry.valueButtonWidth - 1, 20, new TextComponent(item));
            box.setMaxLength(1000);
            box.setValue(item);
            int index = i++;
            entries.add(new ListValueEntry(TextComponent.EMPTY, box, button -> {
                elements.remove(index);
                reinit = true;
            }));
        }
        entries.add(new SettingsList.WidgetEntry(new TranslatableComponent("vivecraft.options.addnew"),
            new Button(
                0, 0, 20, 20,
                new TextComponent("+"),
                button -> {
                    elements = getCurrentValues();
                    elements.add("");
                    reinit = true;
                })));
        return entries;
    }

    private static class ListValueEntry extends SettingsList.WidgetEntry {
        public static final int valueButtonWidth = 280;

        private final Button deleteButton;

        public ListValueEntry(Component name, EditBox valueWidget, Button.OnPress deleteAction) {
            super(name, valueWidget);

            this.deleteButton = new Button(
                0, 0, 20, 20,
                new TextComponent("-"),
                deleteAction,
                (button, poseStack, x, y) -> Minecraft.getInstance().screen.renderTooltip(poseStack, new TranslatableComponent("selectWorld.delete"), x, y));
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.valueWidget.x = k + -50;
            this.valueWidget.y = j;
            this.valueWidget.render(poseStack, n, o, f);
            this.deleteButton.x = k + 230;
            this.deleteButton.y = j;
            this.deleteButton.render(poseStack, n, o, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget, this.deleteButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget, this.deleteButton);
        }

        public String getString() {
            return ((EditBox) valueWidget).getValue();
        }
    }
}
