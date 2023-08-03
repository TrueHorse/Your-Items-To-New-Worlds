package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.screenHandlers.ImportWorldSelectionScreenHandler;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ImportWorldSelectionScreen extends Screen {

    private final ImportWorldSelectionScreenHandler handler = new ImportWorldSelectionScreenHandler();
    private TextFieldWidget searchBox;
    private ImportWorldListWidget levelList;
    private ButtonWidget selectButton;
    private final Consumer<Path> applier;
    private final Screen parent;

    public ImportWorldSelectionScreen(Text title, Screen parent, Consumer<Path> applier) {
        super(title);
        this.applier = applier;
        this.parent = parent;
    }

    @Override
    protected void init(){
        super.init();
        this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox, Text.translatable("selectWorld.search"));
        this.searchBox.setChangedListener(search -> this.levelList.search(search));
        this.levelList = new ImportWorldListWidget(this, handler, this.client, this.width, this.height, 48, this.height - 64, 36, this.searchBox.getText(), this.levelList);
        this.addSelectableChild(this.searchBox);
        this.addSelectableChild(this.levelList);
        this.selectButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.select"), button -> YourItemsToNewWorlds.LOGGER.info("select")).dimensions(this.width / 2 - 154, this.height - 52, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.levelList.render(context, mouseX, mouseY, delta);
        this.searchBox.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close(){
        this.client.setScreen(parent);
    }

    public void applyAndClose(int worldIndex){
        applier.accept(handler.getPathOfWorld(worldIndex));
        close();
    }
}
