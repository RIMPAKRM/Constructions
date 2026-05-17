package com.constructions.structures;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Структура хранилища/сундука
 * Может быть размещена где угодно
 * Может открывать любой игрок
 * Имеет время жизни 5 дней без шкафа авторизации
 */
public class StorageChestStructure extends Structure {
    private static final double DEFAULT_MAX_HEALTH = 30.0;
    private static final long CHEST_LIFETIME = 5 * 24 * 60 * 60 * 1000; // 5 дней в миллисекундах

    public StorageChestStructure(BlockPos basePosition, UUID owner) {
        super("storage_chest", basePosition, owner);
        refreshBlockPositions();
    }

    /**
     * Проверить, истёк ли срок жизни сундука
     */
    public boolean isExpired(long currentTime) {
        return (currentTime - creationTime) > CHEST_LIFETIME;
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
        return super.toString() + " - Storage Chest";
    }
}
