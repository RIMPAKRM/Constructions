package com.constructions.structures;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Металлическая решётка для оконного проёма (1x1 блок)
 */
public class WindowGrilleStructure extends Structure {
    private static final double DEFAULT_MAX_HEALTH = 70.0;

    public WindowGrilleStructure(BlockPos basePosition, UUID owner) {
        super("window_grille", basePosition, owner);
        refreshBlockPositions();
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();
        blocks.add(basePosition);
        return blocks;
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + " - Window grille";
    }
}
