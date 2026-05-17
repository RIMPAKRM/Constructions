package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;

import java.util.*;

/**
 * Базовый класс для всех структур в моде
 * Структура - это набор блоков, которые образуют единое целое (фундамент, стена и т.д.)
 */
public abstract class Structure {
    protected UUID owner;
    protected UUID structureId;
    protected String structureType;
    protected double maxHealth;
    protected double currentHealth;
    protected BlockPos basePosition;
    protected Set<BlockPos> blockPositions;
    protected Set<UUID> attachedStructures;
    protected long creationTime;

    public Structure(String structureType, BlockPos basePosition, UUID owner) {
        this.structureType = structureType;
        this.basePosition = basePosition;
        this.owner = owner;
        this.structureId = UUID.randomUUID();
        this.blockPositions = new HashSet<>();
        this.attachedStructures = new HashSet<>();
        this.creationTime = System.currentTimeMillis();
        this.maxHealth = getDefaultMaxHealth();
        this.currentHealth = this.maxHealth;
    }

    /**
     * Получить тип структуры
     */
    public String getStructureType() {
        return structureType;
    }

    /**
     * Получить владельца структуры
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * Получить ID структуры
     */
    public UUID getStructureId() {
        return structureId;
    }

    /**
     * Получить базовую позицию (опорную точку) структуры
     */
    public BlockPos getBasePosition() {
        return basePosition;
    }

    /**
     * Получить все блоки, занимаемые структурой
     */
    public Set<BlockPos> getBlockPositions() {
        return new HashSet<>(blockPositions);
    }

    /**
     * Получить текущий уровень здоровья структуры
     */
    public double getCurrentHealth() {
        return currentHealth;
    }

    /**
     * Получить максимальный уровень здоровья структуры
     */
    public double getMaxHealth() {
        return maxHealth;
    }

    /**
     * Применить урон к структуре
     */
    public void takeDamage(double damage) {
        this.currentHealth = Math.max(0, currentHealth - damage);
    }

    /**
     * Проверить, разрушена ли структура
     */
    public boolean isDestroyed() {
        return currentHealth <= 0;
    }

    /**
     * Получить структуры, прикреплённые к этой структуре
     */
    public Set<UUID> getAttachedStructures() {
        return new HashSet<>(attachedStructures);
    }

    /**
     * Прикрепить другую структуру к этой
     */
    public void attachStructure(UUID structureId) {
        this.attachedStructures.add(structureId);
    }

    /**
     * Отсоединить структуру от этой
     */
    public void detachStructure(UUID structureId) {
        this.attachedStructures.remove(structureId);
    }

    /**
     * Обновить набор блоков структуры после изменения её формы
     */
    protected void refreshBlockPositions() {
        this.blockPositions.clear();
        this.blockPositions.addAll(getStructureBlocks());
    }

    /**
     * Получить блоки структуры в правильной ориентации
     * Переопределяется в подклассах для разных типов структур
     */
    public abstract List<BlockPos> getStructureBlocks();

    /**
     * Получить максимальное здоровье для этого типа структуры
     */
    protected abstract double getDefaultMaxHealth();

    /**
     * Сохранить структуру в NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", structureType);
        tag.putString("id", structureId.toString());
        tag.putString("owner", owner.toString());
        tag.putInt("x", basePosition.getX());
        tag.putInt("y", basePosition.getY());
        tag.putInt("z", basePosition.getZ());
        tag.putDouble("health", currentHealth);
        tag.putDouble("maxHealth", maxHealth);
        tag.putLong("creationTime", creationTime);
        return tag;
    }

    /**
     * Загрузить структуру из NBT
     */
    public void deserializeNBT(CompoundTag tag) {
        this.structureId = UUID.fromString(tag.getString("id"));
        this.owner = UUID.fromString(tag.getString("owner"));
        this.currentHealth = tag.getDouble("health");
        this.maxHealth = tag.getDouble("maxHealth");
        this.creationTime = tag.getLong("creationTime");
    }

    @Override
    public String toString() {
        return String.format("Structure{type='%s', id=%s, owner=%s, health=%.1f/%.1f}", 
                structureType, structureId, owner, currentHealth, maxHealth);
    }
}
