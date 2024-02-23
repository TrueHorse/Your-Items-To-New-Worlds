package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class TexturedItemButtonWidget extends TexturedButtonWidget {

    private ItemStack itemStack;
    private boolean toggled;
    private final ButtonTextures toggledTextures;

    public TexturedItemButtonWidget(int x, int y, int width, int height, ButtonTextures textures, ButtonTextures toggledTextures, PressAction pressAction, ItemStack itemStack) {
        super(x, y, width, height, textures, pressAction);
        this.toggledTextures = toggledTextures;
        setItemStack(itemStack);
    }

    public TexturedItemButtonWidget(int x, int y, int width, int height, ButtonTextures textures, ButtonTextures toggledTextures, PressAction pressAction, Text message, ItemStack itemStack) {
        super(x, y, width, height, textures, pressAction, message);
        this.toggledTextures = toggledTextures;
        setItemStack(itemStack);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if(toggled){
            context.drawGuiTexture(this.toggledTextures.get(this.active,this.isSelected()), this.getX(), this.getY(), this.width, this.height);
        }else{
            context.drawGuiTexture(this.textures.get(this.active,this.isSelected()), this.getX(), this.getY(), this.width, this.height);
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

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
}
