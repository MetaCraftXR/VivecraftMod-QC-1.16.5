package org.vivecraft.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;
import org.vivecraft.client_vr.provider.nullvr.NullVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

public class VRState {

    public static boolean vrRunning = false;
    public static boolean vrEnabled = false;
    public static boolean vrInitialized = false;

    public static void initializeVR() {
        if (vrInitialized) {
            return;
        }
        try {
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isAntialiasing()) {
                throw new RenderConfigException(new TranslatableComponent("vivecraft.messages.incompatiblesettings").getString(), new TranslatableComponent("vivecraft.messages.optifineaa"));
            }

            vrInitialized = true;
            ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
            if (dh.vrSettings.stereoProviderPluginID == VRSettings.VRProvider.OPENVR) {
                dh.vr = new MCOpenVR(Minecraft.getInstance(), dh);
            } else {
                dh.vr = new NullVR(Minecraft.getInstance(), dh);
            }
            if (!dh.vr.init()) {
                throw new RenderConfigException("VR Init Error", new TranslatableComponent("vivecraft.messages.rendersetupfailed", dh.vr.initStatus, dh.vr.getName()));
            }

            dh.vrRenderer = dh.vr.createVRRenderer();
            dh.vrRenderer.lastGuiScale = Minecraft.getInstance().options.guiScale;

            dh.vrRenderer.setupRenderConfiguration();
            RenderPassManager.setVanillaRenderPass();

            dh.vrPlayer = new VRPlayer();
            dh.vrPlayer.registerTracker(dh.backpackTracker);
            dh.vrPlayer.registerTracker(dh.bowTracker);
            dh.vrPlayer.registerTracker(dh.climbTracker);
            dh.vrPlayer.registerTracker(dh.autoFood);
            dh.vrPlayer.registerTracker(dh.jumpTracker);
            dh.vrPlayer.registerTracker(dh.rowTracker);
            dh.vrPlayer.registerTracker(dh.runTracker);
            dh.vrPlayer.registerTracker(dh.sneakTracker);
            dh.vrPlayer.registerTracker(dh.swimTracker);
            dh.vrPlayer.registerTracker(dh.swingTracker);
            dh.vrPlayer.registerTracker(dh.interactTracker);
            dh.vrPlayer.registerTracker(dh.teleportTracker);
            dh.vrPlayer.registerTracker(dh.horseTracker);
            dh.vrPlayer.registerTracker(dh.vehicleTracker);
            dh.vrPlayer.registerTracker(dh.crawlTracker);
            dh.vrPlayer.registerTracker(dh.cameraTracker);

            dh.vr.postinit();

            dh.menuWorldRenderer = new MenuWorldRenderer();

            dh.menuWorldRenderer.init();
        } catch (RenderConfigException renderConfigException) {
            vrEnabled = false;
            destroyVR(true);
            Minecraft.getInstance().setScreen(new ErrorScreen(renderConfigException.title, renderConfigException.error));
        } catch (Throwable e) {
            vrEnabled = false;
            destroyVR(true);
            e.printStackTrace();
            MutableComponent component = new TextComponent(e.getClass().getName() + (e.getMessage() == null ? "" : ": " + e.getMessage()));
            for (StackTraceElement element : e.getStackTrace()) {
                component.append(new TextComponent("\n" + element.toString()));
            }
            Minecraft.getInstance().setScreen(new ErrorScreen("VR Init Error", component));
        }
    }

    public static void startVR() {
        GLFW.glfwSwapInterval(0);
    }

    public static void destroyVR(boolean disableVRSetting) {
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        if (dh.vr != null) {
            dh.vr.destroy();
        }
        dh.vr = null;
        dh.vrPlayer = null;
        if (dh.vrRenderer != null) {
            dh.vrRenderer.destroy();
        }
        dh.vrRenderer = null;
        if (dh.menuWorldRenderer != null) {
            dh.menuWorldRenderer.completeDestroy();
            dh.menuWorldRenderer = null;
        }
        vrEnabled = false;
        vrInitialized = false;
        vrRunning = false;
        if (disableVRSetting) {
            dh.vrSettings.vrEnabled = false;
            dh.vrSettings.saveOptions();
        }
    }

    public static void pauseVR() {
        //        GLFW.glfwSwapInterval(bl ? 1 : 0);
    }
}
