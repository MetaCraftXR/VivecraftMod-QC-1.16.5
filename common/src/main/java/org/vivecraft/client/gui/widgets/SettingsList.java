package org.vivecraft.client.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.vivecraft.client.gui.framework.GuiVROptionSlider;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.server.config.ConfigBuilder;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

public class SettingsList extends ContainerObjectSelectionList<SettingsList.BaseEntry> {
    final Screen parent;
    int maxNameWidth;

    public SettingsList(Screen parent, Minecraft minecraft, List<SettingsList.BaseEntry> entries) {
        super(minecraft, parent.width + 45, parent.height, 20, parent.height - 32, 20);
        this.parent = parent;
        for (SettingsList.BaseEntry entry : entries) {
            int i;
            if ((i = minecraft.font.width(entry.name)) > this.maxNameWidth) {
                this.maxNameWidth = i;
            }
            this.addEntry(entry);
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 8;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    public static BaseEntry ConfigToEntry(ConfigBuilder.ConfigValue<?> configValue, Component name) {
        AbstractWidget widget = configValue.getWidget(ResettableEntry.valueButtonWidth, 20).get();
        return new ResettableEntry(name, widget, configValue);
    }

    public static BaseEntry vrOptionToEntry(VRSettings.VrOptions option) {
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        String optionString = "vivecraft.options." + option.name();
        String tooltipString = optionString + ".tooltip";
        Component tooltip;
        // check if it has a tooltip
        if (I18n.exists(tooltipString)) {
            String tooltipPrefix = "";
            if (dh.vrSettings.overrides.hasSetting(option)) {
                // add override prefix
                VRSettings.ServerOverrides.Setting setting = dh.vrSettings.overrides.getSetting(option);
                if (setting.isValueOverridden()) {
                    tooltipPrefix = I18n.get("vivecraft.message.overriddenbyserver");
                } else if (setting.isFloat() && (setting.isValueMinOverridden() || setting.isValueMaxOverridden())) {
                    tooltipPrefix = I18n.get("vivecraft.message.limitedbyserver", setting.getValueMin(), setting.getValueMax());
                }
            }
            tooltip = new TextComponent(tooltipPrefix + I18n.get(tooltipString, (Object) null));
        } else {
            tooltip = null;
        }

        AbstractWidget widget;

        if (option.getEnumFloat()) {
            // slider button
            widget = new GuiVROptionSlider(option.returnEnumOrdinal(),
                0, 0,
                WidgetEntry.valueButtonWidth, 20,
                option, true) {
                @Override
                public void renderButton(PoseStack poseStack, int x, int y, float f) {
                    super.renderButton(poseStack, x, y, f);
                    if (this.isHovered && tooltip != null) {
                        Minecraft.getInstance().screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(tooltip, 200), x, y);
                    }
                }
            };
        } else {
            // regular button
            widget = new Button(
                0, 0, WidgetEntry.valueButtonWidth, 20,
                new TextComponent(dh.vrSettings.getButtonDisplayString(option, true)),
                button -> {
                    dh.vrSettings.setOptionValue(option);
                    button.setMessage(new TextComponent(dh.vrSettings.getButtonDisplayString(option, true)));
                },
                (button, poseStack, x, y) -> {
                    if (tooltip != null) {
                        Minecraft.getInstance().screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(tooltip, 200), x, y);
                    }
                });
        }

        BaseEntry entry = new WidgetEntry(new TranslatableComponent(optionString), widget);
        if (dh.vrSettings.overrides.hasSetting(option) && dh.vrSettings.overrides.getSetting(option).isValueOverridden()) {
            entry.setActive(false);
        }
        return entry;
    }

    public static class CategoryEntry extends BaseEntry {
        private final int width;

        public CategoryEntry(Component name) {
            super(name);
            this.width = Minecraft.getInstance().font.width(this.name);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            drawString(poseStack, Minecraft.getInstance().font, this.name, Minecraft.getInstance().screen.width / 2 - this.width / 2, j + m - Minecraft.getInstance().font.lineHeight - 1, 0xFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput narrationElementOutput) {
                    narrationElementOutput.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }
    }

    public static class ResettableEntry extends WidgetEntry {
        private final Button resetButton;
        private final BooleanSupplier canReset;

        public static final int valueButtonWidth = 125;

        public ResettableEntry(Component name, AbstractWidget valueWidget, ConfigBuilder.ConfigValue<?> configValue) {
            super(name, valueWidget);

            this.canReset = () -> !configValue.isDefault();
            this.resetButton = new Button(0, 0, 20, 20, new TextComponent("X"), button -> {
                configValue.reset();
                this.valueWidget = configValue.getWidget(valueWidget.getWidth(), valueWidget.getHeight()).get();
            }, (button, poseStack, x, y) -> Minecraft.getInstance().screen.renderTooltip(poseStack, new TranslatableComponent("controls.reset"), x, y));
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            super.render(poseStack, i, j, k, l, m, n, o, bl, f);
            this.resetButton.x = k + 230;
            this.resetButton.y = j;
            this.resetButton.active = canReset.getAsBoolean();
            this.resetButton.render(poseStack, n, o, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.resetButton.active = active;
        }
    }

    public static class WidgetEntry extends BaseEntry {
        protected AbstractWidget valueWidget;

        public static final int valueButtonWidth = 145;

        public WidgetEntry(Component name, AbstractWidget valueWidget) {
            super(name);
            this.valueWidget = valueWidget;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            drawString(poseStack, Minecraft.getInstance().font, this.name, k + 90 - 140, j + m / 2 - Minecraft.getInstance().font.lineHeight / 2, 0xFFFFFF);
            this.valueWidget.x = k + 105;
            this.valueWidget.y = j;
            this.valueWidget.render(poseStack, n, o, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.valueWidget.active = active;
        }
    }

    public static abstract class BaseEntry extends Entry<BaseEntry> {

        protected final Component name;
        private boolean active = true;

        public BaseEntry(Component name) {
            this.name = name;
        }


        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}

