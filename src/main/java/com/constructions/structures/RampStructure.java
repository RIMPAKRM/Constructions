package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Рампа из ступенек (5 в ширину, 1 в высоту)
 */
public class RampStructure extends Structure {
    private static final int WIDTH = 5;
    private static final double DEFAULT_MAX_HEALTH = 50.0;

    private Direction facing;

    public RampStructure(BlockPos basePosition, UUID owner, Direction facing) {
        super("ramp", basePosition, owner);
        this.facing = facing;
        refreshBlockPositions();
    }

    public Direction getFacing() {
        return facing;
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        for (int offset = 0; offset < WIDTH; offset++) {
            BlockPos pos;
            switch (facing) {
                case NORTH -> pos = basePosition.offset(offset, 0, 0);
                case SOUTH -> pos = basePosition.offset(-offset, 0, 0);
                case EAST -> pos = basePosition.offset(0, 0, offset);
                case WEST -> pos = basePosition.offset(0, 0, -offset);
                default -> {
                    continue;
                }
            }
            blocks.add(pos);
        }

        return blocks;
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + " - Ramp facing " + facing;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putString("facing", facing.name());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("facing")) {
            this.facing = Direction.valueOf(tag.getString("facing"));
            refreshBlockPositions();
        }
    }
}
