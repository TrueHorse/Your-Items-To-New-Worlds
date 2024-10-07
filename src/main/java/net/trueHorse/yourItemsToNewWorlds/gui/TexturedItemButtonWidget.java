package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import org.jetbrains.annotations.NotNull;

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
    public void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        if(toggled){
            this.renderTexture(context, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
        }else{
            this.renderTexture(context, this.resourceLocation, this.getX(), this.getY(), this.xTexStart+this.width, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
        }

        //Geckolib crash workaround, because I don't know Geckolib
        try{
            context.renderItem(itemStack,this.getX()+5,this.getY()+4);
        }catch (Exception e){
            YourItemsToNewWorlds.LOGGER.error(itemStack.getItem().getDescription().getString()+" : "+ e.getMessage());
            context.renderItem(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft","barrier")).getDefaultInstance(),this.getX()+5,this.getY()+4);
        }
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
