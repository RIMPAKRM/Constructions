package com.constructions.structures;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Крыша/потолок с центральным проёмом под подъём на следующий этаж.
 */
public class RoofHoleStructure extends Structure {
    private static final int WIDTH = 5;
    private static final int LENGTH = 5;
    private static final double DEFAULT_MAX_HEALTH = 100.0;

    public RoofHoleStructure(BlockPos basePosition, UUID owner) {
        super("roof_hole", basePosition, owner);
        refreshBlockPositions();
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
                if (x == 2 && z == 2) {
                    continue;
                }

                blocks.add(basePosition.offset(x, 0, z));
            }
        }

        return blocks;
    }

    public List<BlockPos> getSupportBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        // Опоры по периметру крыши (5x5)
        // Периметр: все позиции где x=0 или x=4 или z=0 или z=4
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
                if (x == 0 || x == WIDTH - 1 || z == 0 || z == LENGTH - 1) {
                    blocks.add(basePosition.offset(x, 0, z));
                }
            }
        }

        return blocks;
    }

    public boolean isSupportBlock(BlockPos pos) {
        List<BlockPos> supports = getSupportBlocks();
        return supports.contains(pos);
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + " - Roof with center hole";
    }
}