package org.vivecraft.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class TextScrollWidget extends AbstractWidget {

    private int maxLines = 0;
    private int currentLine = 0;
    private int scrollBarSize = 0;
    private int scrollBarOffset = 0;
    private int scrollSteps = 0;

    private boolean scrollDragActive;

    private final int scrollBarWidth = 5;
    private final int padding = 5;
    private final List<FormattedCharSequence> formattedChars;

    public TextScrollWidget(int x, int y, int width, int height, String text) {
        super(x, y, width, height, new TextComponent(""));

        formattedChars = Minecraft.getInstance().font.split(new TextComponent(text), width - scrollBarWidth * 2);

        initScroll();
    }

    public TextScrollWidget(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, new TextComponent(""));

        formattedChars = Minecraft.getInstance().font.split(text, width - scrollBarWidth * 2);
        initScroll();
    }

    private void initScroll() {

        maxLines = (height - 2 - padding + 3) / 12;
        currentLine = 0;
        scrollSteps = formattedChars.size() - maxLines;
        scrollSteps = Math.max(scrollSteps, 0);
        scrollBarSize = scrollSteps == 0 ? height - 2 : (int) (Math.max(formattedChars.size(), maxLines) / (float) (scrollSteps) * 12);
        scrollBarOffset = height - scrollBarSize - 2;
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        // draw box outline
        fill(poseStack,
            x,
            y,
            x + width,
            y + this.height,
            -6250336);
        // draw box inside
        fill(poseStack,
            x + 1,
            y + 1,
            x + width - 1,
            y + this.height - 1,
            -16777216);

        // draw text
        for (int line = 0; line + currentLine < formattedChars.size() && line < maxLines; line++) {
            drawString(poseStack, Minecraft.getInstance().font, formattedChars.get(line + currentLine), x + padding, y + padding + line * 12, 16777215);
        }

        float scrollbarStart = scrollSteps == 0 ? 0 : currentLine / (float) scrollSteps * scrollBarOffset;

        if (isFocused() || isHovered) {
            // draw scroll bar outline
            fill(poseStack,
                x + width - scrollBarWidth - 2,
                (int) (y + 1 + scrollbarStart),
                x + width - 1,
                (int) (y + 1 + scrollbarStart + scrollBarSize),
                -1);
        }

        // draw scroll bar
        fill(poseStack,
            x + width - scrollBarWidth - (isFocused() || isHovered ? 1 : 2),
            (int) (y + (isFocused() || isHovered ? 2 : 1) + scrollbarStart),
            x + width - (isFocused() || isHovered ? 2 : 1),
            (int) (y + (isFocused() || isHovered ? 0 : 1) + scrollbarStart + scrollBarSize),
            -6250336);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(double x, double y) {
        if (x >= this.x + width - scrollBarWidth && x <= this.x + width && y >= this.y && y <= this.y + height) {
            scrollDragActive = true;
            if (maxLines < formattedChars.size()) {
                // update scroll position
                setCurrentLineFromYPos(y);
            }
        } else if (this.clicked(x, y)) {
            Style style = getMouseoverStyle(x, y);
            if (style != null && style.getClickEvent() != null) {
                Minecraft.getInstance().screen.handleComponentClicked(style);
            }
        }
    }

    @Override
    public void onRelease(double x, double y) {
        scrollDragActive = false;
        super.onRelease(x, y);
    }

    @Override
    public void onDrag(double x, double y, double xRel, double yRel) {
        if (visible && active && scrollDragActive) {
            setCurrentLineFromYPos(y);
        }
    }

    private void setCurrentLineFromYPos(double y) {
        if (y < this.y + scrollBarSize * 0.5) {
            currentLine = 0;
        } else if (y > this.y + height - scrollBarSize * 0.5) {
            currentLine = scrollSteps;
        } else {
            currentLine = (int) ((y - this.y - scrollBarSize * 0.5) / (height - scrollBarSize) * scrollSteps);
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollAmountY) {
        if (scrollAmountY < 0.0 && currentLine < scrollSteps) {
            currentLine++;
        } else if (scrollAmountY > 0.0 && currentLine > 0) {
            currentLine--;
        } else {
            // scroll bar on limit, didn't consume the input
            return false;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
            if (mouseScrolled(0, 0, key == GLFW.GLFW_KEY_UP ? 1 : -1)) {
                return true;
            }
        }
        return super.keyPressed(key, scancode, mods);
    }

    public Style getMouseoverStyle(double x, double y) {
        int lineIndex = this.getLineIndex(x, y);
        if (lineIndex >= 0 && lineIndex < this.formattedChars.size()) {
            FormattedCharSequence line = this.formattedChars.get(lineIndex);
            return Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(line, Mth.floor(x - this.x));
        }
        return null;
    }

    private int getLineIndex(double x, double y) {
        if (!this.clicked(x, y)) {
            return -1;
        } else {
            return (int) ((y - this.y - padding * 0.5) / 12.0);
        }
    }

    public Style getMouseover(int x, int y) {
        Style style = this.getMouseoverStyle(x, y);
        if (style != null && style.getHoverEvent() != null) {
            return style;
        }
        return null;
    }
}
