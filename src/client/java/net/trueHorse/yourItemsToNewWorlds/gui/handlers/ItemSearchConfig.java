package net.trueHorse.yourItemsToNewWorlds.gui.handlers;

import net.minecraft.util.math.ChunkPos;
import net.trueHorse.yourItemsToNewWorlds.io.ItemImporter;

import java.nio.file.Path;

public record ItemSearchConfig(Path worldPath, String playerName, ItemImporter.SearchLocationDeterminationMode searchLocationDeterminationMode, ChunkPos searchChunk, int searchRadius) {
}
