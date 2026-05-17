package com.constructions.structures;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Структура крыши/потолка
 * Устанавливается на верхние края стен
 */
public class RoofStructure extends Structure {
    private static final int WIDTH = 5;
    private static final int LENGTH = 5;
    private static final double DEFAULT_MAX_HEALTH = 75.0;

    public RoofStructure(BlockPos basePosition, UUID owner) {
        super("roof", basePosition, owner);
        refreshBlockPositions();
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        // Генерируем блоки крыши 5x5
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
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
        return super.toString() + " - Roof/Ceiling";
    }
}
