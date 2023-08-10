package net.trueHorse.yourItemsToNewWorlds.gui.handlers;

import net.minecraft.util.math.ChunkPos;

import java.nio.file.Path;

public record ItemSearchConfig(Path worldPath, String playerName, ChunkPos searchChunk, int searchRadius) {
}
