package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TextComponent;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import javax.annotation.Nullable;

public class GuiVROptionSlider extends AbstractSliderButton implements GuiVROption {
    @Nullable
    private final VRSettings.VrOptions enumOptions;
    private int id = -1;
    private final boolean valueOnly;

    public GuiVROptionSlider(int id, int x, int y, int width, int height, VRSettings.VrOptions option, boolean valueOnly) {
        super(x, y, width, height,
            new TextComponent(ClientDataHolderVR.getInstance().vrSettings.getButtonDisplayString(option, valueOnly)),
            option.normalizeValue(ClientDataHolderVR.getInstance().vrSettings.getOptionFloatValue(option)));

        this.id = id;
        this.enumOptions = option;
        this.valueOnly = valueOnly;

        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        if (dataholder.vrSettings.overrides.hasSetting(option) && dataholder.vrSettings.overrides.getSetting(option).isValueOverridden()) {
            this.active = false;
        }
    }

    public GuiVROptionSlider(int id, int x, int y, VRSettings.VrOptions option) {
        this(id, x, y, 150, 20, option, false);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(new TextComponent(ClientDataHolderVR.getInstance().vrSettings.getButtonDisplayString(this.enumOptions, valueOnly)));
    }

    @Override
    protected void applyValue() {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        double d0 = this.enumOptions.denormalizeValue((float) this.value);
        dataholder.vrSettings.setOptionFloatValue(this.enumOptions, (float) d0);
        // TODO check if that works right on 1.19.2
        this.value = this.enumOptions.normalizeValue((float) d0);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    @Nullable
    public VRSettings.VrOptions getOption() {
        return this.enumOptions;
    }
}
