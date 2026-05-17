package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;

/**
 * Структура шкафа авторизации
 * Используется для управления доступом в базу
 * Находится в радиусе 40 блоков от своей позиции
 * Может быть только 1 в радиусе 40 блоков
 */
public class AuthCabinetStructure extends Structure {
    private static final double DEFAULT_MAX_HEALTH = 50.0;
    private static final int AUTH_RADIUS = 40;
    
    private Set<UUID> authorizedPlayers;
    private long creationTime;
    private boolean isActive = true;  // Флаг активации шкафа

    public AuthCabinetStructure(BlockPos basePosition, UUID owner) {
        super("auth_cabinet", basePosition, owner);
        this.authorizedPlayers = new java.util.HashSet<>();
        this.creationTime = System.currentTimeMillis();
        this.isActive = true;
        refreshBlockPositions();
    }

    public Set<UUID> getAuthorizedPlayers() {
        return new java.util.HashSet<>(authorizedPlayers);
    }

    public void addAuthorizedPlayer(UUID playerId) {
        this.authorizedPlayers.add(playerId);
    }

    public void removeAuthorizedPlayer(UUID playerId) {
        this.authorizedPlayers.remove(playerId);
    }

    public boolean isPlayerAuthorized(UUID playerId) {
        if (!isActive) {
            return false;  // Если шкаф неактивен, никто не авторизирован
        }
        return this.authorizedPlayers.contains(playerId) || playerId.equals(this.owner);
    }

    public boolean isPlayerInAuthList(UUID playerId) {
        return this.authorizedPlayers.contains(playerId);
    }

    public void clearAllAuthorizedPlayers() {
        this.authorizedPlayers.clear();
        this.isActive = false;  // Деактивируем при очистке
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public int getAuthRadius() {
        return AUTH_RADIUS;
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
        return super.toString() + " - Auth Cabinet (authorized: " + authorizedPlayers.size() + ")";
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        
        ListTag authorizedTag = new ListTag();
        for (UUID playerId : authorizedPlayers) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putString("uuid", playerId.toString());
            authorizedTag.add(playerTag);
        }
        tag.put("authorizedPlayers", authorizedTag);
        tag.putBoolean("isActive", this.isActive);
        
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        
        this.authorizedPlayers.clear();
        ListTag authorizedTag = tag.getList("authorizedPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < authorizedTag.size(); i++) {
            CompoundTag playerTag = authorizedTag.getCompound(i);
            if (playerTag.contains("uuid")) {
                this.authorizedPlayers.add(UUID.fromString(playerTag.getString("uuid")));
            }
        }
        this.isActive = tag.getBoolean("isActive");
    }
}
