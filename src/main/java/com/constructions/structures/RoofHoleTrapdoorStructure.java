package com.constructions.structures;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Люк, размещаемый над отверстием в крыше.
 * Ставится ровно на 1 блок выше отверстия RoofHoleStructure.
 */
public class RoofHoleTrapdoorStructure extends Structure {
    private static final double DEFAULT_MAX_HEALTH = 60.0;

    public RoofHoleTrapdoorStructure(BlockPos basePosition, UUID owner) {
        super("roof_hole_trapdoor", basePosition, owner);
        refreshBlockPositions();
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();
        // Люк занимает только одну позицию в центре над отверстием
        blocks.add(basePosition);
        return blocks;
    }

    public List<BlockPos> getSupportBlocks() {
        // Люк не имеет дополнительных опорных блоков
        return new ArrayList<>();
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + " - Roof hole trapdoor";
    }
}
