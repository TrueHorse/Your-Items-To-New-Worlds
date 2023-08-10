package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelSummary;
import net.trueHorse.yourItemsToNewWorlds.gui.handlers.ImportWorldSelectionScreenHandler;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ImportWorldSelectionScreen extends Screen {

    public static final ResourceLocation BUTTON_TEXTURE_SHEET = new ResourceLocation("your_items_to_new_worlds","textures/gui/buttons.png");
    private final ImportWorldSelectionScreenHandler handler;
    private EditBox searchBox;
    private ImageButton addInstanceButton;
    private ImportWorldListWidget worldList;
    private InstanceListWidget instanceList;
    private Button selectButton;
    private final Consumer<Path> applier;
    private final Screen parent;

    public ImportWorldSelectionScreen(Component title, Screen parent, Consumer<Path> applier) {
        super(title);
        this.applier = applier;
        this.parent = parent;
        this.handler = new ImportWorldSelectionScreenHandler(this);
    }

    public void onSelectedInstanceChanged(){
        rebuildWidgets();
    }

    public void onInstancesChanged(){
        rebuildWidgets();
    }

    @Override
    protected void init(){
        super.init();
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 12, 200, 20, this.searchBox, handler.getSelectedInstancePath()!=null ? Component.translatable("selectWorld.search"):Component.translatable("narrator.your_items_to_new_worlds.instance_search"));

        this.addInstanceButton = new ImageButton(this.width/2+105,12,20,20,0,0, 20, BUTTON_TEXTURE_SHEET,40,40,
                button -> handler.chooseNewInstance(),Component.translatable("transfer_items.your_items_to_new_worlds.add_instance"));
        addInstanceButton.visible = handler.getSelectedInstancePath()==null;
        addInstanceButton.setTooltip(Tooltip.create(Component.translatable("transfer_items.your_items_to_new_worlds.add_instance")));
        this.addRenderableWidget(addInstanceButton);

        this.worldList = new ImportWorldListWidget(this, handler, this.minecraft, this.width, this.height, 38, this.height - 64, 36, this.searchBox.getValue());
        this.instanceList = new InstanceListWidget(this.minecraft,this, this.handler, this.searchBox.getValue());

        if(handler.getSelectedInstancePath()==null){
            this.searchBox.setResponder(search -> this.instanceList.search(search));
            this.addWidget(this.instanceList);
        }else{
            this.searchBox.setResponder(search -> this.worldList.search(search));
            this.addWidget(this.worldList);
        }

        this.addWidget(this.searchBox);
        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.title"), button -> applyWithSelected()).bounds(this.width / 2 - 154, this.height - 29, 150, 20).build());
        this.selectButton.active = handler.getSelectedWorld()!=null;

        Button cancelButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose()).bounds(this.width / 2 + 5, this.height-29, 150, 20).build());
        cancelButton.visible = handler.getSelectedInstancePath()==null;

        Button backButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> handler.onInstanceSelected(null)).bounds(this.width / 2 + 5, this.height-29, 150, 20).build());
        backButton.visible = handler.getSelectedInstancePath()!=null;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
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
    public void onClose(){
        this.minecraft.setScreen(parent);
    }

    public void onWorldSelected(){
        selectButton.active = true;
    }

    public void applyWithSelected(){
        applyAndClose(handler.getSelectedWorld());
    }

    public void applyAndClose(int worldIndex){
        applier.accept(handler.getPathOfWorld(worldIndex));
        onClose();
    }

    public void applyAndClose(LevelSummary worldSummary){
        applier.accept(handler.getPathOfWorld(worldSummary));
        onClose();
    }
}
