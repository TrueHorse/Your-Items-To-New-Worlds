package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TexturedItemButtonWidget extends ImageButton {

    private ItemStack itemStack;
    private boolean toggled;

    public TexturedItemButtonWidget(int x, int y, int width, int height, int u, int v, ResourceLocation texture, OnPress pressAction, ItemStack itemStack) {
        super(x, y, width, height, u, v, texture, pressAction);
        setItemStack(itemStack);
    }

    public TexturedItemButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, ResourceLocation texture, OnPress pressAction, ItemStack itemStack) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, pressAction);
        setItemStack(itemStack);
    }

    public TexturedItemButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, ResourceLocation texture, int textureWidth, int textureHeight, OnPress pressAction, ItemStack itemStack) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction);
        setItemStack(itemStack);
    }

    public TexturedItemButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, ResourceLocation texture, int textureWidth, int textureHeight, OnPress pressAction, Component message, ItemStack itemStack) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, message);
        setItemStack(itemStack);
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if(toggled){
            this.renderTexture(context, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
        }else{
            this.renderTexture(context, this.resourceLocation, this.getX(), this.getY(), this.xTexStart+this.width, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
        }
        context.renderItem(itemStack,this.getX()+5,this.getY()+4);
        context.renderItemDecorations(Minecraft.getInstance().font,itemStack,this.getX()+5,this.getY()+4);
    }

    public void toggle(){
        toggled = !toggled;
    }
    
    public void setItemStack(ItemStack itemStack){
        this.itemStack = itemStack;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
}
