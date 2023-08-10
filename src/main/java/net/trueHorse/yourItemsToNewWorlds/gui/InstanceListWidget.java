package net.trueHorse.yourItemsToNewWorlds.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.Button;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.ImageButton;
import net.minecraft.text.Text;
import net.trueHorse.yourItemsToNewWorlds.gui.handlers.ImportWorldSelectionScreenHandler;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class InstanceListWidget extends ElementListWidget<InstanceListWidget.Entry> {

    private final ImportWorldSelectionScreen parent;
    private String search;
    private final ImportWorldSelectionScreenHandler handler;

    public InstanceListWidget(Minecraft client, ImportWorldSelectionScreen parent, ImportWorldSelectionScreenHandler handler, String search) {
        super(client, parent.width+65, parent.height, 40, parent.height - 39, 20);
        this.setRenderBackground(false);
        this.setRenderHorizontalShadows(false);
        this.parent = parent;
        this.handler = handler;
        this.search = search;
        for(Path path:handler.getInstances()){
            this.addEntry(new InstanceEntry(path,handler));
        }
    }

    public void search(String search){
        if (handler.getInstances() != null && !search.equals(this.search)) {
            this.showInstances(search);
        }
        this.search = search;
    }

    private void showInstances(String search) {
        this.clearEntries();
        search = search.toLowerCase(Locale.ROOT);
        for (Path instancePath : handler.getInstances()) {
            if (instancePath.getFileName().toString().toLowerCase().contains(search)){
                this.addEntry(new InstanceEntry(instancePath,handler));
            }
        }
        this.parent.narrateScreenIfNarrationEnabled(true);
    }

    public final class InstanceEntry
            extends InstanceListWidget.Entry{

        private final Button instanceButton;
        private final ImageButton deleteButton;

        public InstanceEntry(Path instancePath, ImportWorldSelectionScreenHandler handler){
            String instanceName = instancePath.getFileName().toString();
            instanceButton = Button.builder(Component.literal(instanceName),button -> handler.onInstanceSelected(instancePath)).dimensions(0,0,150,20).build();
            instanceButton.setMessage(Component.literal(instanceName));

            deleteButton = new ImageButton(0,0,20,20,20,0,20, ImportWorldSelectionScreen.BUTTON_TEXTURE_SHEET,40,40,
                    button -> handler.removeInstance(instancePath),Component.translatable("narrator.your_items_to_new_worlds.remove_instance",instanceName));
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            instanceButton.setX(x);
            instanceButton.setY(y);
            instanceButton.render(context,mouseX,mouseY,tickDelta);

            if(this.isMouseOver(mouseX,mouseY)){
                deleteButton.setX(x+instanceButton.getWidth()+5);
                deleteButton.setY(y);
                deleteButton.render(context,mouseX,mouseY,tickDelta);
            }
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(instanceButton,deleteButton);
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(instanceButton,deleteButton);
        }
    }

    public static abstract class Entry
            extends ElementListWidget.Entry<InstanceListWidget.Entry> {

    }
}
