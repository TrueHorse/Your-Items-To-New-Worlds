package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TexturedItemButtonWidget extends TexturedButtonWidget {
    //TODO private
    public ItemStack itemStack;
    private boolean toggled;

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
        if(toggled){
            this.drawTexture(context, this.texture, this.getX(), this.getY(), this.u, this.v, this.hoveredVOffset, this.width, this.height, this.textureWidth, this.textureHeight);
        }else{
            this.drawTexture(context, this.texture, this.getX(), this.getY(), this.u+this.width, this.v, this.hoveredVOffset, this.width, this.height, this.textureWidth, this.textureHeight);
        }
        context.drawItem(itemStack,this.getX()+5,this.getY()+4);
        context.drawItemInSlot(MinecraftClient.getInstance().textRenderer,itemStack,this.getX()+5,this.getY()+4);
    }

    public void toggle(){
        toggled = !toggled;
    }
    
    public void setItemStack(ItemStack itemStack){
        this.itemStack = itemStack;
    }
}
