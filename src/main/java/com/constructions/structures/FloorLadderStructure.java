package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Вертикальный подъём между этажами.
 * Используется как компактная лестница/шахта для доступа через проём в крыше.
 */
public class FloorLadderStructure extends Structure {
    private static final int HEIGHT = 4;
    private static final double DEFAULT_MAX_HEALTH = 25.0;
    private final Direction facing;
    private final boolean withSupport;

    public FloorLadderStructure(BlockPos basePosition, UUID owner) {
        this(basePosition, owner, Direction.NORTH, true);
    }

    public FloorLadderStructure(BlockPos basePosition, UUID owner, Direction facing) {
        this(basePosition, owner, facing, true);
    }

    public FloorLadderStructure(BlockPos basePosition, UUID owner, Direction facing, boolean withSupport) {
        super("floor_ladder", basePosition, owner);
        this.facing = facing;
        this.withSupport = withSupport;
        refreshBlockPositions();
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            blocks.add(basePosition.above(y));
        }

        return blocks;
    }

    public List<BlockPos> getLadderBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            blocks.add(basePosition.above(y));
        }

        return blocks;
    }

    public List<BlockPos> getSupportBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        if (!withSupport) {
            return blocks; // Пустой список если опоры отключены
        }

        for (int y = 0; y < HEIGHT; y++) {
            blocks.add(basePosition.above(y).relative(facing));
        }

        return blocks;
    }

    public Direction getFacing() {
        return facing;
    }

    public boolean isWithSupport() {
        return withSupport;
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + " - Floor access ladder";
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putString("facing", facing.name());
        tag.putBoolean("withSupport", withSupport);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("facing")) {
            Direction loadedFacing = Direction.valueOf(tag.getString("facing"));
            // Reflection-free restore is not needed here because facing is final in runtime placement,
            // but we keep NBT for forward compatibility.
        }
    }
}