package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {

    // needs remap because of forge
    @Accessor
    static int[] getShaderTextures() {
        return null;
    }

    @Accessor
    static Vector3f[] getShaderLightDirections() {
        return null;
    }
}
