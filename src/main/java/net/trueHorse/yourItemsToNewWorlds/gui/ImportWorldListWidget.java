package net.trueHorse.yourItemsToNewWorlds.gui;

import com.ibm.icu.text.SimpleDateFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.gui.handlers.ImportWorldSelectionScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ImportWorldListWidget extends ObjectSelectionList<ImportWorldListWidget.Entry> {

    private String search;
    private final ImportWorldSelectionScreen parent;
    private final ImportWorldSelectionScreenHandler handler;
    private boolean listRendered = false;

    public ImportWorldListWidget(ImportWorldSelectionScreen parent, ImportWorldSelectionScreenHandler handler, Minecraft Minecraft, int width, int height, int top, int bottom, int itemHeight, String search) {
        super(Minecraft, width, height, top, bottom, itemHeight);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.parent = parent;
        this.handler = handler;
        this.search = search;
        this.showSummaries(search);
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(ImportWorldListWidget.Entry::close);
        super.clearEntries();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Optional<ImportWorldListWidget.WorldEntry> optional = this.getSelectedAsOptional();
        if (CommonInputs.selected(keyCode) && optional.isPresent()) {
            parent.applyAndClose(children().indexOf(optional.get()));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void setSelected(Entry entry){
        super.setSelected(entry);
        handler.setSelectedWorld(((WorldEntry)entry).level);
        parent.onWorldSelected();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if(!listRendered){
            this.showSummaries(this.search);
            listRendered = true;
        }
        super.render(context, mouseX, mouseY, delta);
    }

    public void search(String search) {
        if (handler.getWorlds() != null && !search.equals(this.search)) {
            this.showSummaries(search);
        }
        this.search = search;
    }

    private void showSummaries(String search) {
        this.clearEntries();
        search = search.toLowerCase(Locale.ROOT);
        for (LevelSummary levelSummary : handler.getWorlds()) {
            if (!this.shouldShow(search, levelSummary)) continue;
            this.addEntry(new ImportWorldListWidget.WorldEntry(this, levelSummary));
        }
        this.narrateScreenIfNarrationEnabled();
    }

    private boolean shouldShow(String search, LevelSummary summary) {
        return summary.getLevelName().toLowerCase(Locale.ROOT).contains(search) || summary.getLevelId().toLowerCase(Locale.ROOT).contains(search);
    }

    private void narrateScreenIfNarrationEnabled() {
        this.parent.triggerImmediateNarration(true);
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public Optional<ImportWorldListWidget.WorldEntry> getSelectedAsOptional() {
        ImportWorldListWidget.Entry entry = this.getSelected();
        if (entry instanceof WorldEntry worldEntry) {
            return Optional.of(worldEntry);
        }
        return Optional.empty();
    }

    public ImportWorldSelectionScreen getParent(){
        return this.parent;
    }


    public final class WorldEntry
            extends ImportWorldListWidget.Entry
            implements AutoCloseable {
        private final Minecraft client;
        private final ImportWorldSelectionScreen screen;
        private final LevelSummary level;
        private final FaviconTexture icon;
        @Nullable
        private Path iconPath;
        private long time;

        public WorldEntry(ImportWorldListWidget levelList, LevelSummary level) {
            this.client = levelList.minecraft;
            this.screen = levelList.getParent();
            this.level = level;
            this.icon = FaviconTexture.forWorld(this.client.getTextureManager(), level.getLevelId());
            this.iconPath = level.getIcon();
            this.validateIconPath();
            this.loadIcon();
        }

        private void validateIconPath() {
            if (this.iconPath != null) {
                try {
                    BasicFileAttributes basicfileattributes = Files.readAttributes(this.iconPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                    if (basicfileattributes.isSymbolicLink()) {
                        List<ForbiddenSymlinkInfo> list = new ArrayList<>();
                        this.client.getLevelSource().getWorldDirValidator().validateSymlink(this.iconPath, list);
                        if (!list.isEmpty()) {
                            YourItemsToNewWorlds.LOGGER.warn(ContentValidationException.getMessage(this.iconPath, list));
                            this.iconPath = null;
                        } else {
                            basicfileattributes = Files.readAttributes(this.iconPath, BasicFileAttributes.class);
                        }
                    }

                    if (!basicfileattributes.isRegularFile()) {
                        this.iconPath = null;
                    }
                } catch (NoSuchFileException nosuchfileexception) {
                    this.iconPath = null;
                } catch (IOException ioexception) {
                    YourItemsToNewWorlds.LOGGER.error("could not validate symlink", ioexception);
                    this.iconPath = null;
                }

            }
        }

        @Override
        public Component getNarration() {
            MutableComponent text = Component.translatable("narrator.select.world_info", this.level.getLevelName(), new Date(this.level.getLastPlayed()), this.level.getInfo());
            return Component.translatable("narrator.select", text);
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String displayName = this.level.getLevelName();
            String folderName = this.level.getLevelId();
            long lastPlayedEpoch = this.level.getLastPlayed();
            if (lastPlayedEpoch != -1L) {
                folderName = folderName + " (" + new SimpleDateFormat().format(new Date(lastPlayedEpoch)) + ")";

            }
            if (displayName.isEmpty()) {
                displayName = Component.translatable("selectWorld.world") + " " + (index + 1);
            }
            Component text = this.level.getInfo();
            context.drawString(this.client.font, displayName, x + 32 + 3, y + 1, 0xFFFFFF, false);
            context.drawString(this.client.font, folderName, x + 32 + 3, y + this.client.font.lineHeight + 3, 0x808080, false);
            context.drawString(this.client.font, text, x + 32 + 3, y + this.client.font.lineHeight + this.client.font.lineHeight + 3, 0x808080, false);
            RenderSystem.enableBlend();
            context.blit(this.icon.textureLocation(), x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ImportWorldListWidget.this.setSelected(this);
            if (mouseX - ImportWorldListWidget.this.getRowLeft() <= 32.0) {
                parent.applyAndClose(this.level);
                return true;
            }
            if (Util.getMillis() - this.time < 250L) {
                parent.applyAndClose(this.level);
                return true;
            }
            this.time = Util.getMillis();
            return true;
        }

        private void loadIcon() {
            boolean bl = this.iconPath != null && Files.isRegularFile(this.iconPath);
            if (bl) {
                try (InputStream inputStream = Files.newInputStream(this.iconPath)){
                    this.icon.upload(NativeImage.read(inputStream));
                } catch (Throwable throwable) {
                    YourItemsToNewWorlds.LOGGER.error("Invalid icon for world {}", this.level.getLevelId(), throwable);
                    this.iconPath = null;
                }
            } else {
                this.icon.clear();
            }
        }

        @Override
        public void close() {
            this.icon.close();
        }

        public String getLevelDisplayName() {
            return this.level.getLevelName();
        }

        @Override
        public boolean isAvailable() {
            return !this.level.isDisabled();
        }
    }

    public static abstract class Entry
            extends ObjectSelectionList.Entry<ImportWorldListWidget.Entry>
            implements AutoCloseable {
        public abstract boolean isAvailable();

        @Override
        public void close() {
        }
    }

}