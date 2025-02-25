package org.vivecraft.mixin.client_vr.tutorial;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.tutorial.OpenInventoryTutorialStep;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.MCVR;

@Mixin(OpenInventoryTutorialStep.class)
public class OpenInventoryTutorialStepVRMixin {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V"), index = 2, method = "tick")
    private Component vivecraft$alterDescription(Component component) {
        if (!VRState.vrRunning) {
            return component;
        }
        if (!ClientDataHolderVR.getInstance().vrSettings.seated && MCVR.get().getInputAction(Minecraft.getInstance().options.keyInventory).isActive()) {
            return new TranslatableComponent("tutorial.open_inventory.description", new TextComponent(MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyInventory).getLastOrigin())).withStyle(ChatFormatting.BOLD));
        }
        return component;
    }
}
