package net.trueHorse.yourItemsToNewWorlds.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.trueHorse.yourItemsToNewWorlds.screenHandlers.ImportWorldSelectionScreenHandler;

import java.nio.file.Path;
import java.util.List;

public class InstanceListWidget extends ElementListWidget<InstanceListWidget.Entry> {

    private final ImportWorldSelectionScreen parent;

    public InstanceListWidget(MinecraftClient client, ImportWorldSelectionScreen parent, ImportWorldSelectionScreenHandler handler) {
        super(client, parent.width + 45, parent.height, 45, parent.height - 32, 20);
        this.setRenderBackground(false);
        this.setRenderHorizontalShadows(false);
        this.parent = parent;
        this.addEntry(new InstanceEntry(client.runDirectory.toPath(),handler));
    }

    public void pclearEntries(){
       this.clearEntries();
    }

    public void search(String search){

    }

    //TODO path still exists

    public final class InstanceEntry
            extends InstanceListWidget.Entry{

        private final ButtonWidget instanceButton;

        public InstanceEntry(Path instancePath, ImportWorldSelectionScreenHandler handler){
            String instanceName = instancePath.getFileName().toString();
            instanceButton = ButtonWidget.builder(Text.of(instanceName),button -> handler.onInstanceSelected(instancePath)).dimensions(0,0,150,20).build();
            instanceButton.setMessage(Text.of(instanceName));
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            instanceButton.setX(x);
            instanceButton.setY(y);
            instanceButton.render(context,mouseX,mouseY,tickDelta);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(instanceButton);
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(instanceButton);
        }
    }

    public static abstract class Entry
            extends ElementListWidget.Entry<InstanceListWidget.Entry> {

    }
}
