package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ToggleableTexturedButtonWidget extends TexturedButtonWidget {

    private boolean toggled;

    public ToggleableTexturedButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, ButtonWidget.PressAction pressAction) {
        super(x, y, width, height, u, v, height, texture, 256, 256, pressAction);
    }

    public ToggleableTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, ButtonWidget.PressAction pressAction) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, 256, 256, pressAction);
    }

    public ToggleableTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, ButtonWidget.PressAction pressAction) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, ScreenTexts.EMPTY);
    }

    public ToggleableTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, ButtonWidget.PressAction pressAction, Text message) {
        super(x,y,width,height,u,v,hoveredVOffset,texture,textureWidth,textureHeight,pressAction,message);
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
}
