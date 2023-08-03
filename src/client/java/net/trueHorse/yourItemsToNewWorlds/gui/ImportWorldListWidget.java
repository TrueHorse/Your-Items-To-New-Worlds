package net.trueHorse.yourItemsToNewWorlds.gui;

import com.ibm.icu.text.SimpleDateFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.path.SymlinkEntry;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.trueHorse.yourItemsToNewWorlds.YourItemsToNewWorlds;
import net.trueHorse.yourItemsToNewWorlds.screenHandlers.ImportWorldSelectionScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ImportWorldListWidget extends AlwaysSelectedEntryListWidget<ImportWorldListWidget.Entry> {

    private CompletableFuture<List<LevelSummary>> levelsFuture;
    @Nullable
    private List<LevelSummary> levels;
    private String search;
    private final ImportWorldSelectionScreen parent;
    private final ImportWorldSelectionScreenHandler handler;

    public ImportWorldListWidget(ImportWorldSelectionScreen parent, ImportWorldSelectionScreenHandler handler, MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight, String search, ImportWorldListWidget oldWidget) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.setRenderBackground(false);
        this.setRenderHorizontalShadows(false);
        this.parent = parent;
        this.handler = handler;
        this.levelsFuture = oldWidget != null ? oldWidget.levelsFuture : this.loadLevels();
        this.search = search;
        this.show(this.tryGet());
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(ImportWorldListWidget.Entry::close);
        super.clearEntries();
    }

    @Nullable
    private List<LevelSummary> tryGet() {
        try {
            return this.levelsFuture.getNow(null);
        } catch (CancellationException | CompletionException runtimeException) {
            return null;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Optional<ImportWorldListWidget.WorldEntry> optional = this.getSelectedAsOptional();
        if (KeyCodes.isToggle(keyCode) && optional.isPresent()) {
            parent.applyAndClose(children().indexOf(optional.get()));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        List<LevelSummary> list = this.tryGet();
        if (list != this.levels) {
            this.show(list);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void show(@Nullable List<LevelSummary> levels) {
        if (levels != null) {
            this.showSummaries(this.search, levels);
        }
        this.levels = levels;
    }

    public void search(String search) {
        if (this.levels != null && !search.equals(this.search)) {
            this.showSummaries(search, this.levels);
        }
        this.search = search;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorage.LevelList levelList;
        try {
            levelList = this.client.getLevelStorage().getLevelList();
        } catch (LevelStorageException levelStorageException) {
            YourItemsToNewWorlds.LOGGER.error("Couldn't load level list", levelStorageException);
            this.showUnableToLoadScreen(levelStorageException.getMessageText());
            return CompletableFuture.completedFuture(List.of());
        }
        if (levelList.isEmpty()) {
            CreateWorldScreen.create(this.client, null);
            return CompletableFuture.completedFuture(List.of());
        }
        return this.client.getLevelStorage().loadSummaries(levelList).exceptionally(throwable -> {
            this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Couldn't load level list"));
            return List.of();
        });
    }

    private void showSummaries(String search, List<LevelSummary> summaries) {
        this.clearEntries();
        search = search.toLowerCase(Locale.ROOT);
        for (LevelSummary levelSummary : summaries) {
            if (!this.shouldShow(search, levelSummary)) continue;
            this.addEntry(new ImportWorldListWidget.WorldEntry(this, levelSummary));
        }
        this.narrateScreenIfNarrationEnabled();
    }

    private boolean shouldShow(String search, LevelSummary summary) {
        return summary.getDisplayName().toLowerCase(Locale.ROOT).contains(search) || summary.getName().toLowerCase(Locale.ROOT).contains(search);
    }

    private void narrateScreenIfNarrationEnabled() {
        this.parent.narrateScreenIfNarrationEnabled(true);
    }

    private void showUnableToLoadScreen(Text message) {
        this.client.setScreen(new FatalErrorScreen(Text.translatable("selectWorld.unable_to_load"), message));
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public Optional<ImportWorldListWidget.WorldEntry> getSelectedAsOptional() {
        ImportWorldListWidget.Entry entry = this.getSelectedOrNull();
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
        private final MinecraftClient client;
        private final ImportWorldSelectionScreen screen;
        private final LevelSummary level;
        private final WorldIcon icon;
        @Nullable
        private Path iconPath;
        private long time;

        public WorldEntry(ImportWorldListWidget levelList, LevelSummary level) {
            this.client = levelList.client;
            this.screen = levelList.getParent();
            this.level = level;
            this.icon = WorldIcon.forWorld(this.client.getTextureManager(), level.getName());
            this.iconPath = level.getIconPath();
            this.validateIconPath();
            this.loadIcon();
        }

        private void validateIconPath() {
            if (this.iconPath == null) {
                return;
            }
            try {
                BasicFileAttributes basicFileAttributes = Files.readAttributes(this.iconPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (basicFileAttributes.isSymbolicLink()) {
                    ArrayList<SymlinkEntry> list = new ArrayList<>();
                    this.client.getLevelStorage().getSymlinkFinder().validate(this.iconPath, list);
                    if (!list.isEmpty()) {
                        YourItemsToNewWorlds.LOGGER.warn(SymlinkValidationException.getMessage(this.iconPath, list));
                        this.iconPath = null;
                    } else {
                        basicFileAttributes = Files.readAttributes(this.iconPath, BasicFileAttributes.class);
                    }
                }
                if (!basicFileAttributes.isRegularFile()) {
                    this.iconPath = null;
                }
            } catch (NoSuchFileException noSuchFileException) {
                this.iconPath = null;
            } catch (IOException iOException) {
                YourItemsToNewWorlds.LOGGER.error("could not validate symlink", iOException);
                this.iconPath = null;
            }
        }

        @Override
        public Text getNarration() {
            MutableText text = Text.translatable("narrator.select.world_info", this.level.getDisplayName(), new Date(this.level.getLastPlayed()), this.level.getDetails());
            return Text.translatable("narrator.select", text);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String displayName = this.level.getDisplayName();
            String folderName = this.level.getName();
            long lastPlayedEpoch = this.level.getLastPlayed();
            if (lastPlayedEpoch != -1L) {
                folderName = folderName + " (" + new SimpleDateFormat().format(new Date(lastPlayedEpoch)) + ")";

            }
            if (displayName.isEmpty()) {
                displayName = Text.translatable("selectWorld.world") + " " + (index + 1);
            }
            Text text = this.level.getDetails();
            context.drawText(this.client.textRenderer, displayName, x + 32 + 3, y + 1, 0xFFFFFF, false);
            context.drawText(this.client.textRenderer, folderName, x + 32 + 3, y + this.client.textRenderer.fontHeight + 3, 0x808080, false);
            context.drawText(this.client.textRenderer, text, x + 32 + 3, y + this.client.textRenderer.fontHeight + this.client.textRenderer.fontHeight + 3, 0x808080, false);
            RenderSystem.enableBlend();
            context.drawTexture(this.icon.getTextureId(), x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.level.isUnavailable()) {
                return true;
            }
            ImportWorldListWidget.this.setSelected(this);
            this.time = Util.getMeasuringTimeMs();
            return true;
        }

        private void loadIcon() {
            boolean bl = this.iconPath != null && Files.isRegularFile(this.iconPath);
            if (bl) {
                try (InputStream inputStream = Files.newInputStream(this.iconPath)){
                    this.icon.load(NativeImage.read(inputStream));
                } catch (Throwable throwable) {
                    YourItemsToNewWorlds.LOGGER.error("Invalid icon for world {}", this.level.getName(), throwable);
                    this.iconPath = null;
                }
            } else {
                this.icon.destroy();
            }
        }

        @Override
        public void close() {
            this.icon.close();
        }

        public String getLevelDisplayName() {
            return this.level.getDisplayName();
        }

        @Override
        public boolean isAvailable() {
            return !this.level.isUnavailable();
        }
    }

    public static abstract class Entry
            extends AlwaysSelectedEntryListWidget.Entry<ImportWorldListWidget.Entry>
            implements AutoCloseable {
        public abstract boolean isAvailable();

        @Override
        public void close() {
        }
    }

}