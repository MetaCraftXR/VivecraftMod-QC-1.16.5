package org.vivecraft.mixin.client_vr.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(Gui.class)
public abstract class GuiVRMixin extends GuiComponent implements GuiExtension {

    @Unique
    public boolean vivecraft$showPlayerList;
    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;
    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    public void vivecraft$cancelRenderVignette(Entity entity, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            RenderSystem.enableDepthTest();
            ci.cancel();
        }
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    public void vivecraft$cancelRenderOverlay(ResourceLocation resourceLocation, float f, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void vivecraft$cancelRenderPortalOverlay(float f, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderSpyglassOverlay", cancellable = true)
    public void vivecraft$cancelRenderSpyglassOverlay(float f, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    public void vivecraft$cancelRenderCrosshair(PoseStack poseStack, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I"), method = "render")
    public int vivecraft$noSleepOverlay(LocalPlayer instance) {
        return VRState.vrRunning ? 0 : instance.getSleepTimer();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"), method = "render")
    public boolean vivecraft$toggleableTabList(KeyMapping instance) {
        return instance.isDown() || vivecraft$showPlayerList;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 1, shift = At.Shift.AFTER), method = "renderHotbar")
    public void vivecraft$hotbarContext(float f, PoseStack poseStack, CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar >= 0 && ClientDataHolderVR.getInstance().interactTracker.hotbar < 9 && this.getCameraPlayer().getInventory().selected != ClientDataHolderVR.getInstance().interactTracker.hotbar && ClientDataHolderVR.getInstance().interactTracker.isActive(minecraft.player)) {
            int i = this.screenWidth / 2;
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 1.0F);
            blit(poseStack, i - 91 - 1 + ClientDataHolderVR.getInstance().interactTracker.hotbar * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0), method = "renderHotbar")
    public boolean vivecraft$slotSwap(ItemStack instance) {
        return !(!instance.isEmpty() || (VRState.vrRunning && ClientDataHolderVR.getInstance().vrSettings.vrTouchHotbar));
    }

    @Inject(at = @At("HEAD"), method = "renderHotbar", cancellable = true)
    public void vivecraft$notHotbarOnScreens(float f, PoseStack poseStack, CallbackInfo ci) {
        if (VRState.vrRunning && minecraft.screen != null) {
            ci.cancel();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 2, shift = At.Shift.BEFORE), method = "renderHotbar")
    public void vivecraft$renderVRHotbarLeft(float f, PoseStack poseStack, CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar == 9 && ClientDataHolderVR.getInstance().interactTracker.isActive(minecraft.player)) {
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 2, shift = At.Shift.AFTER), method = "renderHotbar")
    public void vivecraft$renderVRHotbarLeftReset(float f, PoseStack poseStack, CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar == 9) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 3, shift = At.Shift.BEFORE), method = "renderHotbar")
    public void vivecraft$renderVRHotbarRight(float f, PoseStack poseStack, CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar == 9 && ClientDataHolderVR.getInstance().interactTracker.isActive(minecraft.player)) {
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 3, shift = At.Shift.AFTER), method = "renderHotbar")
    public void vivecraft$renderVRHotbarRightReset(float f, PoseStack poseStack, CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar == 9) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    // do remap because of forge
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"), method = "renderHotbar")
    public void vivecraft$renderVive(float f, PoseStack poseStack, CallbackInfo ci) {
        if (VRState.vrRunning) {
            this.vivecraft$renderViveHudIcons(poseStack);
        }
    }

    @Unique
    private void vivecraft$renderViveHudIcons(PoseStack poseStack) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            int k = 0;
            MobEffect mobeffect = null;

            if (player.isSprinting()) {
                mobeffect = MobEffects.MOVEMENT_SPEED;
            }

            if (player.isVisuallySwimming()) {
                mobeffect = MobEffects.DOLPHINS_GRACE;
            }

            if (player.isShiftKeyDown()) {
                mobeffect = MobEffects.BLINDNESS;
            }

            if (player.isFallFlying()) {
                k = -1;
            }
            if (ClientDataHolderVR.getInstance().crawlTracker.crawling) {
                k = -2;
            }

            int x = this.minecraft.getWindow().getGuiScaledWidth() / 2 - 109;
            int y = this.minecraft.getWindow().getGuiScaledHeight() - 39;

            if (k == -1) {
                this.minecraft.getItemRenderer().renderGuiItem(new ItemStack(Items.ELYTRA), x, y);
                mobeffect = null;
            } else if (k == -2) {
                int x2 = x;
                if (player.isShiftKeyDown()) {
                    x2 -= 19;
                } else {
                    mobeffect = null;
                }
                this.minecraft.getItemRenderer().renderGuiItem(new ItemStack(Items.RABBIT_FOOT), x2, y);
            }
            if (mobeffect != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getMobEffectTextures().get(mobeffect);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
                GuiComponent.blit(poseStack, x, y, 0, 18, 18, textureatlassprite);
            }
        }
    }

    @Override
    @Unique
    public boolean vivecraft$getShowPlayerList() {
        return this.vivecraft$showPlayerList;
    }

    @Override
    @Unique
    public void vivecraft$setShowPlayerList(boolean showPlayerList) {
        this.vivecraft$showPlayerList = showPlayerList;
    }

    @Override
    @Unique
    public void vivecraft$drawMouseMenuQuad(int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        //uhhhh //RenderSystem.disableLighting();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Screen.GUI_ICONS_LOCATION);
        float f = 16.0F * ClientDataHolderVR.getInstance().vrSettings.menuCrosshairScale;
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        this.vivecraft$drawCentredTexturedModalRect(mouseX, mouseY, f, f, 0, 0, 15F / 256F, 15F / 256F);
        RenderSystem.disableBlend();
    }

    @Unique
    public void vivecraft$drawCentredTexturedModalRect(int centreX, int centreY, float width, float height, float uMin, float vMin, float uMax, float vMax) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex((float) centreX - width / 2.0F, (float) centreY + height / 2.0F, this.getBlitOffset())
            .uv(uMin, vMin).endVertex();
        bufferbuilder.vertex((float) centreX + width / 2.0F, (float) centreY + height / 2.0F, this.getBlitOffset())
            .uv(uMin, vMax).endVertex();
        bufferbuilder.vertex((float) centreX + width / 2.0F, (float) centreY - height / 2.0F, this.getBlitOffset())
            .uv(uMax, vMax).endVertex();
        bufferbuilder.vertex((float) centreX - width / 2.0F, (float) centreY - height / 2.0F, this.getBlitOffset())
            .uv(uMax, vMin).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }
}
