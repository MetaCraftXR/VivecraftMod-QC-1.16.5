package org.vivecraft.client.gui.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.vivecraft.client.gui.framework.GuiVROptionButton;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiRadialConfiguration extends GuiVROptionsBase {
    static VROptionLayout[] options = new VROptionLayout[]{
        new VROptionLayout(VRSettings.VrOptions.RADIAL_MODE_HOLD, VROptionLayout.Position.POS_LEFT, 0.0F, true, "")
    };
    private String[] arr;
    private boolean isShift = false;
    private int selectedIndex = -1;
    private GuiRadialItemsList list;
    private boolean isselectmode = false;

    public GuiRadialConfiguration(Screen guiScreen) {
        super(guiScreen);
    }

    public void setKey(KeyMapping key) {
        if (key != null) {
            this.arr[this.selectedIndex] = key.getName();
        } else {
            this.arr[this.selectedIndex] = "";
        }

        this.selectedIndex = -1;
        this.isselectmode = false;
        this.reinit = true;
        this.visibleList = null;

        if (!this.isShift) {
            this.dataholder.vrSettings.vrRadialItems = ArrayUtils.clone(this.arr);
        } else {
            this.dataholder.vrSettings.vrRadialItemsAlt = ArrayUtils.clone(this.arr);
        }

        this.dataholder.vrSettings.saveOptions();
    }

    public void init() {
        this.vrTitle = "vivecraft.options.screen.radialmenu";
        this.list = new GuiRadialItemsList(this, this.minecraft);
        if (visibleList != null) {
            this.visibleList = this.list;
        }
        this.clearWidgets();

        if (this.isselectmode) {
            this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 25, 150, 20,
                new TranslatableComponent("gui.cancel"), (p) -> {
                this.isselectmode = false;
                this.reinit = true;
                this.visibleList = null;
            }));

            this.addRenderableWidget(new Button(this.width / 2 - 155, 26, 150, 20,
                new TranslatableComponent("vivecraft.gui.clear"), (p) -> this.setKey(null)));
        } else {
            this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / 6 - 10, 150, 20,
                this.isShift ?
                    new TranslatableComponent("vivecraft.gui.radialmenu.mainset") :
                    new TranslatableComponent("vivecraft.gui.radialmenu.alternateset"),
                (p) -> {
                    this.isShift = !this.isShift;
                    this.reinit = true;
                }));

            super.init(options, false);

            int numButtons = this.dataholder.vrSettings.vrRadialButtons;
            int buttonWidthMin = 120;
            // distance from the center, with 14 buttons, move them closer together
            float dist = numButtons * (numButtons >= 14 ? 5F : 5.5F);
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            this.arr = ArrayUtils.clone(this.dataholder.vrSettings.vrRadialItems);
            String[] altSet = ArrayUtils.clone(this.dataholder.vrSettings.vrRadialItemsAlt);

            if (this.isShift) {
                this.arr = altSet;
            }

            for (int i = 0; i < numButtons; i++) {
                KeyMapping keymapping = null;

                for (KeyMapping keymapping1 : this.minecraft.options.keyMappings) {
                    if (i < this.arr.length && keymapping1.getName().equalsIgnoreCase(this.arr[i])) {
                        keymapping = keymapping1;
                    }
                }

                String label = "";

                if (keymapping != null) {
                    label = I18n.get(keymapping.getName());
                }

                int buttonWidth = Math.max(buttonWidthMin, this.font.width(label));

                // coords of the button, button 0 is at the top with x = 0, y = -dist
                float distX = numButtons * 4 + buttonWidth * 0.5F;

                // position buttons on equal y spacing
                float btnIndex = (i < numButtons / 2 ? i : numButtons - i) / (float) (numButtons / 2);
                int y = (int) (2.0F * dist * btnIndex - dist);

                // position x so the buttons produce an ellipse
                int x = (int) (distX * (Math.sqrt(1.0F - (y * y) / (dist * dist))));

                // move in between buttons closer to the middle
                if (Math.abs(y) > 20) {
                    x = (int) (x * 0.87F);
                }

                // second half of buttons should be on the left side
                x *= i > numButtons / 2 ? -1 : 1;

                int index = i;
                this.addRenderableWidget(new Button(centerX + x - buttonWidth / 2, centerY + y,
                    buttonWidth, 20, new TranslatableComponent(label),
                    (p) -> {
                        this.selectedIndex = index;
                        this.isselectmode = true;
                        this.reinit = true;
                        this.visibleList = this.list;
                    }));
            }

            this.addRenderableWidget(
                new GuiVROptionButton(VRSettings.VrOptions.RADIAL_NUMBER.ordinal(),
                    centerX - 10, centerY, 20, 20,
                    VRSettings.VrOptions.RADIAL_NUMBER, "" + this.dataholder.vrSettings.vrRadialButtons,
                    (p) -> {
                        this.dataholder.vrSettings.vrRadialButtons += 2;
                        if (dataholder.vrSettings.vrRadialButtons >
                            VRSettings.VrOptions.RADIAL_NUMBER.getValueMax()) {
                            this.dataholder.vrSettings.vrRadialButtons = (int) VRSettings.VrOptions.RADIAL_NUMBER.getValueMin();
                        }
                        this.reinit = true;
                    }));
            super.addDefaultButtons();
        }
    }

    protected void loadDefaults() {
        super.loadDefaults();
        this.settings.vrRadialItems = this.settings.getRadialItemsDefault();
        this.settings.vrRadialItemsAlt = this.settings.getRadialItemsAltDefault();
    }

    protected boolean onDoneClicked() {
        if (this.isselectmode) {
            this.isselectmode = false;
            this.reinit = true;
            this.visibleList = null;
            return true;
        } else {
            return false;
        }
    }

    public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(poseStack, pMouseX, pMouseY, pPartialTicks);

        if (this.visibleList == null) {
            drawCenteredString(poseStack, this.minecraft.font,
                new TranslatableComponent("vivecraft.messages.radialmenubind.1"), this.width / 2, this.height - 50,
                5635925);

            if (this.isShift) {
                drawCenteredString(poseStack, this.minecraft.font,
                    new TranslatableComponent("vivecraft.messages.radialmenubind.2"), this.width / 2, this.height - 36,
                    13777015);
                drawCenteredString(poseStack, this.minecraft.font,
                    new TranslatableComponent("vivecraft.messages.radialmenubind.3"), this.width / 2, this.height - 22,
                    13777015);
            }
        }
    }
}
