package org.vivecraft.mixin.client_vr.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TutorialToast.class)
public abstract class TutorialToastVRMixin implements Toast {

    @Shadow
    @Final
    private Component title;

    @Shadow
    @Final
    private Component message;

    @Unique
    private int vivecraft$offset;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", shift = At.Shift.AFTER), method = "render")
    private void vivecraft$extendToast(PoseStack poseStack, ToastComponent toastComponent, long l, CallbackInfoReturnable<Visibility> cir) {
        int width = Math.max(toastComponent.getMinecraft().font.width(this.title), message != null ? toastComponent.getMinecraft().font.width(this.message) : 0) + 34;
        vivecraft$offset = Math.min(this.width() - width, 0);
        if (vivecraft$offset < 0) {
            // draw a bigger toast from right to left, to override the left border
            for (int i = vivecraft$offset - (this.width() - 8) * (vivecraft$offset / (this.width() - 8)); i >= vivecraft$offset; i -= this.width() - 8) {
                toastComponent.blit(poseStack, i, 0, 0, 96, this.width() - 4, this.height());
            }
        }
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/GuiComponent;II)V"), method = "render", index = 2)
    private int vivecraft$offsetIcon(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;draw(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I"), method = "render", index = 2)
    private float vivecraft$offsetText(float x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"), method = "render", index = 1)
    private int vivecraft$offsetProgressStart(int x) {
        return x + vivecraft$offset;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 1), method = "render", index = 3)
    private int vivecraft$offsetProgressEnd(int x) {
        return x + vivecraft$offset - (int) ((float) x / TutorialToast.PROGRESS_BAR_WIDTH * vivecraft$offset);
    }
}
