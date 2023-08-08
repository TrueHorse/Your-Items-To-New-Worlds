package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelSummary;
import net.trueHorse.yourItemsToNewWorlds.gui.handlers.ImportWorldSelectionScreenHandler;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ImportWorldSelectionScreen extends Screen {

    public static final Identifier BUTTON_TEXTURE_SHEET = new Identifier("your_items_to_new_worlds","textures/gui/buttons.png");
    private final ImportWorldSelectionScreenHandler handler;
    private TextFieldWidget searchBox;
    private TexturedButtonWidget addInstanceButton;
    private ImportWorldListWidget worldList;
    private InstanceListWidget instanceList;
    private ButtonWidget selectButton;
    private final Consumer<Path> applier;
    private final Screen parent;

    public ImportWorldSelectionScreen(Text title, Screen parent, Consumer<Path> applier) {
        super(title);
        this.applier = applier;
        this.parent = parent;
        this.handler = new ImportWorldSelectionScreenHandler(this);
    }

    public void onSelectedInstanceChanged(){
        clearAndInit();
    }

    public void onInstancesChanged(){
        clearAndInit();
    }

    @Override
    protected void init(){
        super.init();
        this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 12, 200, 20, this.searchBox, Text.translatable("selectWorld.search"));

        this.addInstanceButton = new TexturedButtonWidget(this.width/2+105,12,20,20,0,0, 20, BUTTON_TEXTURE_SHEET,40,40,
                button -> handler.chooseNewInstance(),Text.translatable("transfer_items.your_items_to_new_worlds.add_instance"));
        addInstanceButton.visible = handler.getSelectedInstancePath()==null;
        addInstanceButton.setTooltip(Tooltip.of(Text.translatable("transfer_items.your_items_to_new_worlds.add_instance")));
        this.addDrawableChild(addInstanceButton);

        this.worldList = new ImportWorldListWidget(this, handler, this.client, this.width, this.height, 38, this.height - 64, 36, this.searchBox.getText());
        this.instanceList = new InstanceListWidget(this.client,this, this.handler, this.searchBox.getText());

        if(handler.getSelectedInstancePath()==null){
            this.searchBox.setChangedListener(search -> this.instanceList.search(search));
            this.addSelectableChild(this.instanceList);
        }else{
            this.searchBox.setChangedListener(search -> this.worldList.search(search));
            this.addSelectableChild(this.worldList);
        }

        this.addSelectableChild(this.searchBox);
        this.selectButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("selectWorld.title"), button -> applyWithSelected()).dimensions(this.width / 2 - 154, this.height - 29, 150, 20).build());
        this.selectButton.active = handler.getSelectedWorld()!=null;

        ButtonWidget cancelButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close()).dimensions(this.width / 2 + 5, this.height-29, 150, 20).build());
        cancelButton.visible = handler.getSelectedInstancePath()==null;

        ButtonWidget backButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), button -> handler.onInstanceSelected(null)).dimensions(this.width / 2 + 5, this.height-29, 150, 20).build());
        backButton.visible = handler.getSelectedInstancePath()!=null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        if(handler.getSelectedInstancePath()==null){
            this.instanceList.render(context, mouseX, mouseY, delta);
        }else{
            this.worldList.render(context, mouseX, mouseY, delta);
        }
        this.searchBox.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close(){
        this.client.setScreen(parent);
    }

    public void onWorldSelected(){
        selectButton.active = true;
    }

    public void applyWithSelected(){
        applyAndClose(handler.getSelectedWorld());
    }

    public void applyAndClose(int worldIndex){
        applier.accept(handler.getPathOfWorld(worldIndex));
        close();
    }

    public void applyAndClose(LevelSummary worldSummary){
        applier.accept(handler.getPathOfWorld(worldSummary));
        close();
    }
}
