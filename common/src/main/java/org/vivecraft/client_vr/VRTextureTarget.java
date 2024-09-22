package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;

public class VRTextureTarget extends RenderTarget {

    private final String name;

    public VRTextureTarget(String name, int width, int height, boolean usedepth, boolean onMac, int texid, boolean depthtex, boolean linearFilter, boolean useStencil) {
        super(usedepth);
        this.name = name;
        RenderSystem.assertOnRenderThreadOrInit();
        ((RenderTargetExtension) this).vivecraft$setTextid(texid);
        ((RenderTargetExtension) this).vivecraft$isLinearFilter(linearFilter);

        // need to set this first, because the forge/neoforge stencil enabled does a resize
        this.viewWidth = width;
        this.viewHeight = height;

        if (useStencil && !Xplat.enableRenderTargetStencil(this)) {
            // use our stencil only if the modloader doesn't support it
            ((RenderTargetExtension) this).vivecraft$setUseStencil(true);
        }
        this.resize(width, height, onMac);
        this.setClearColor(0, 0, 0, 0);
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("\n");
        if (this.name != null) {
            stringbuilder.append("Name:   " + this.name).append("\n");
        }
        stringbuilder.append("Size:   " + this.viewWidth + " x " + this.viewHeight).append("\n");
        stringbuilder.append("FB ID:  " + this.frameBufferId).append("\n");
        stringbuilder.append("Tex ID: " + this.colorTextureId).append("\n");
        return stringbuilder.toString();
    }
}
