package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TexturedItemButtonWidget extends TexturedButtonWidget {
    
    private ItemStack itemStack;

    public TexturedItemButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, PressAction pressAction, ItemStack itemStack) {
        super(x, y, width, height, u, v, texture, pressAction);
        setItemStack(itemStack);
    }

    public TexturedItemButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, PressAction pressAction, ItemStack itemStack) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, pressAction);
        setItemStack(itemStack);
    }

    public TexturedItemButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, PressAction pressAction, ItemStack itemStack) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction);
        setItemStack(itemStack);
    }

    public TexturedItemButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, PressAction pressAction, Text message, ItemStack itemStack) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, message);
        setItemStack(itemStack);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderButton(context,mouseX,mouseY,delta);
        context.drawItem(itemStack,this.getX()+4,this.getX()+4);
    }
    
    public void setItemStack(ItemStack itemStack){
        this.itemStack = itemStack;
    }
}
