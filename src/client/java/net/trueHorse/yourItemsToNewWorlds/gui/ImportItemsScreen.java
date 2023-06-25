package net.trueHorse.yourItemsToNewWorlds.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ImportItemsScreen extends Screen {

    private final Consumer<Optional<ArrayList>> applier;
    private final List<String> playerNames = new ArrayList<>();
    private final String[] searchLocationDeterminationModeIDs = {"transfer_items.your_items_to_new_worlds.spawn_point",
            "transfer_items.your_items_to_new_worlds.most_item_containers",
            "transfer_items.your_items_to_new_worlds.longest_inhabitation",
            "transfer_items.your_items_to_new_worlds.coordinates"};
    private TextFieldWidget worldPathWidget;
    private CyclingButtonWidget<String> playerNameWidget;
    private CyclingButtonWidget<String> searchLocationModeWidget;
    private final TextFieldWidget[] coordFields = new TextFieldWidget[3];
    private final Screen parent;

    public ImportItemsScreen(Screen parent, Consumer<Optional<ArrayList>> applier){
        super(Text.translatable(("editGamerule.title")));
        this.applier = applier;
        this.parent = parent;
    }

    @Override
    protected void init(){
        super.init();

        int margin = 10;
        int minDistanceFromEdge = 25;

        ArrayList<ClickableWidget> widgets = new ArrayList<>();

        worldPathWidget = new TextFieldWidget(this.textRenderer,minDistanceFromEdge,margin,this.width-2*minDistanceFromEdge,20,Text.of("tempPathText"));
        worldPathWidget.setMaxLength(200);
        widgets.add(worldPathWidget);

        //test names
        playerNames.add("Horse");
        playerNames.add("Jo");

        playerNameWidget = CyclingButtonWidget.builder(Text::of).values(playerNames).build(this.width/2-50,worldPathWidget.getY()+worldPathWidget.getHeight()+margin,100,20,Text.of("tempPlayerNameText"));
        widgets.add(playerNameWidget);

        int coordRowY = playerNameWidget.getY()+playerNameWidget.getHeight()+margin;
        searchLocationModeWidget = CyclingButtonWidget.<String>builder(Text::translatable).values(searchLocationDeterminationModeIDs).build(minDistanceFromEdge,coordRowY,150,20,Text.of("tempLocationModeText"));
        widgets.add(searchLocationModeWidget);
        coordFields[0] = new TextFieldWidget(this.textRenderer,searchLocationModeWidget.getX()+searchLocationModeWidget.getWidth()+margin,coordRowY,50,20,Text.of("tempXText"));
        coordFields[1] = new TextFieldWidget(this.textRenderer,coordFields[0].getX()+coordFields[0].getWidth()+margin,coordRowY,50,20,Text.of("tempYText"));
        coordFields[2] = new TextFieldWidget(this.textRenderer,coordFields[1].getX()+coordFields[1].getWidth()+margin,coordRowY,50,20,Text.of("tempZText"));
        widgets.addAll(List.of(coordFields));

        widgets.add(ButtonWidget.builder(Text.of("tempSearchText"),button-> YourItemsToNewWorlds.LOGGER.warn("search")).dimensions(this.width/2-75,searchLocationModeWidget.getY()+searchLocationModeWidget.getHeight()+margin,150,20).build());

        widgets.add(addDrawableChild(ButtonWidget.builder(Text.translatable("controls.resetAll"), button -> close()).dimensions(this.width / 2 - 155, this.height - 29, 150, 20).build()));
        widgets.add(ButtonWidget.builder(ScreenTexts.DONE, button -> applyAndClose()).dimensions(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());

        widgets.forEach(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        renderBackground(context);
        super.render(context,mouseX,mouseY,delta);
    }

    @Override
    public void tick(){
        worldPathWidget.tick();
    }

    @Override
    public void close(){
        client.setScreen(parent);
    }

    public void applyAndClose(){
        applier.accept(Optional.empty());
        close();
    }
}
