package com.constructions.structures;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/**
 * Структура фундамента (5x5 блоков)
 * Основа для размещения стен и других структур
 * Может подстраиваться под ландшафт (максимум 3 блока высоты на краях)
 */
public class FoundationStructure extends Structure {
    private static final int WIDTH = 5;
    private static final int LENGTH = 5;
    private static final double DEFAULT_MAX_HEALTH = 170.0;

    // Глубина опор по углам (вниз от платформы), максимум 3 блока
    private final int[] pillarHeights = new int[4];

    public FoundationStructure(BlockPos basePosition, UUID owner) {
        super("foundation", basePosition, owner);
        // По умолчанию все опоры на уровне 0
        for (int i = 0; i < 4; i++) {
            pillarHeights[i] = 0;
        }
        refreshBlockPositions();
    }

    /**
     * Установить высоты столбов для адаптации к ландшафту
     */
    public void setPillarHeights(int[] heights) {
        if (heights.length != 4) {
            throw new IllegalArgumentException("Must provide exactly 4 heights");
        }
        // Проверка, что ни один столб не выше 3 блоков
        for (int height : heights) {
            if (height < 0 || height > 3) {
                throw new IllegalArgumentException("Pillar height must be between 0 and 3");
            }
        }
        System.arraycopy(heights, 0, this.pillarHeights, 0, 4);
        updateBlockPositions();
    }

    /**
     * Получить высоты столбов
     */
    public int[] getPillarHeights() {
        return pillarHeights.clone();
    }

    public boolean isEdgePosition(BlockPos pos) {
        int relativeX = pos.getX() - basePosition.getX();
        int relativeZ = pos.getZ() - basePosition.getZ();
        return relativeX >= 0 && relativeX < WIDTH && relativeZ >= 0 && relativeZ < LENGTH
                && (relativeX == 0 || relativeX == WIDTH - 1 || relativeZ == 0 || relativeZ == LENGTH - 1);
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        // Верхняя платформа 5x5
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
                blocks.add(basePosition.offset(x, 0, z));
            }
        }

        // Четыре угловые опоры, направленные вниз
        int[][] cornerOffsets = new int[][] {
                {0, 0},
                {WIDTH - 1, 0},
                {0, LENGTH - 1},
                {WIDTH - 1, LENGTH - 1}
        };

        for (int i = 0; i < cornerOffsets.length; i++) {
            int depth = pillarHeights[i];
            if (depth <= 0) {
                continue;
            }

            int cornerX = cornerOffsets[i][0];
            int cornerZ = cornerOffsets[i][1];
            for (int d = 1; d <= depth; d++) {
                blocks.add(basePosition.offset(cornerX, -d, cornerZ));
            }
        }

        return blocks;
    }

    private void updateBlockPositions() {
        refreshBlockPositions();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putIntArray("pillarHeights", pillarHeights);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        int[] heights = tag.getIntArray("pillarHeights");
        if (heights.length == 4) {
            System.arraycopy(heights, 0, this.pillarHeights, 0, 4);
            refreshBlockPositions();
        }
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" - Foundation 5x5 with support depths: [%d, %d, %d, %d]",
                pillarHeights[0], pillarHeights[1], pillarHeights[2], pillarHeights[3]);
    }
}
