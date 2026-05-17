package com.constructions.structures;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Структура костра
 * Может открывать любой игрок для жарки еды
 * Имеет время жизни 2 часа без шкафа авторизации
 */
public class CampfireStructure extends Structure {
    private static final double DEFAULT_MAX_HEALTH = 20.0;
    private static final long CAMPFIRE_LIFETIME = 2 * 60 * 60 * 1000; // 2 часа в миллисекундах

    public CampfireStructure(BlockPos basePosition, UUID owner) {
        super("campfire", basePosition, owner);
        refreshBlockPositions();
    }

    /**
     * Проверить, истёк ли срок жизни костра
     */
    public boolean isExpired(long currentTime) {
        return (currentTime - creationTime) > CAMPFIRE_LIFETIME;
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
        return super.toString() + " - Campfire";
    }
}
