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
 * Структура двери (деревянная или железная)
 * Может быть открыта только авторизованными игроками
 * Может быть открыта с помощью ПКМ
 */
public class DoorStructure extends Structure {
    private static final double WOOD_DOOR_HEALTH = 50.0;
    private static final double IRON_DOOR_HEALTH = 80.0;
    
    private DoorType doorType;
    private boolean isOpen;
    private Set<UUID> authorizedPlayers;

    public enum DoorType {
        WOODEN, IRON
    }

    public DoorStructure(BlockPos basePosition, UUID owner, DoorType doorType) {
        super(doorType == DoorType.WOODEN ? "wooden_door" : "iron_door", basePosition, owner);
        this.doorType = doorType;
        this.isOpen = false;
        this.authorizedPlayers = new java.util.HashSet<>();
        refreshBlockPositions();
    }

    public DoorType getDoorType() {
        return doorType;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    public void addAuthorizedPlayer(UUID playerId) {
        this.authorizedPlayers.add(playerId);
    }

    public void removeAuthorizedPlayer(UUID playerId) {
        this.authorizedPlayers.remove(playerId);
    }

    public boolean isPlayerAuthorized(UUID playerId) {
        return this.authorizedPlayers.contains(playerId) || playerId.equals(this.owner);
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();
        blocks.add(basePosition);
        blocks.add(basePosition.above());
        return blocks;
    }

    @Override
    protected double getDefaultMaxHealth() {
        return doorType == DoorType.WOODEN ? WOOD_DOOR_HEALTH : IRON_DOOR_HEALTH;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putString("doorType", doorType.name());
        tag.putBoolean("isOpen", isOpen);

        ListTag authorizedTag = new ListTag();
        for (UUID playerId : authorizedPlayers) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putString("uuid", playerId.toString());
            authorizedTag.add(playerTag);
        }
        tag.put("authorizedPlayers", authorizedTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("doorType")) {
            this.doorType = DoorType.valueOf(tag.getString("doorType"));
        }
        if (tag.contains("isOpen")) {
            this.isOpen = tag.getBoolean("isOpen");
        }

        this.authorizedPlayers.clear();
        ListTag authorizedTag = tag.getList("authorizedPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < authorizedTag.size(); i++) {
            CompoundTag playerTag = authorizedTag.getCompound(i);
            if (playerTag.contains("uuid")) {
                this.authorizedPlayers.add(UUID.fromString(playerTag.getString("uuid")));
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + " - " + doorType + " Door (open: " + isOpen + ")";
    }
}
