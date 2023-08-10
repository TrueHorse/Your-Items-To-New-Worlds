package net.trueHorse.yourItemsToNewWorlds.gui.handlers;

import net.minecraft.util.math.BlockPos;
import net.trueHorse.yourItemsToNewWorlds.io.ItemImporter;

import java.nio.file.Path;

public record ItemSearchConfig(Path worldPath, String playerName, ItemImporter.SearchLocationDeterminationMode searchLocationDeterminationMode, BlockPos searchCoords, int searchRadius) {
}
