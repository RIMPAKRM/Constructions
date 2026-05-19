package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Полустена (5 в ширину, 1 в высоту)
 */
public class HalfWallStructure extends Structure {
    private static final int WIDTH = 5;
    private static final int HEIGHT = 1;
    private static final double DEFAULT_MAX_HEALTH = 40.0;

    private WallStructure.WallOrientation orientation;

    public HalfWallStructure(BlockPos basePosition, UUID owner, WallStructure.WallOrientation orientation) {
        super("half_wall", basePosition, owner);
        this.orientation = orientation;
        refreshBlockPositions();
    }

    public WallStructure.WallOrientation getOrientation() {
        return orientation;
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        for (int x = 0; x < WIDTH; x++) {
            BlockPos pos;
            switch (orientation) {
                case NORTH -> pos = basePosition.offset(x, 0, 0);
                case SOUTH -> pos = basePosition.offset(-x, 0, 0);
                case EAST -> pos = basePosition.offset(0, 0, x);
                case WEST -> pos = basePosition.offset(0, 0, -x);
                default -> {
                    continue;
                }
            }
            blocks.add(pos);
        }

        return blocks;
    }

    public List<BlockPos> getSupportBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        BlockPos leftSupport;
        BlockPos rightSupport;
        switch (orientation) {
            case NORTH -> {
                leftSupport = basePosition.offset(0, 0, 0);
                rightSupport = basePosition.offset(WIDTH - 1, 0, 0);
            }
            case SOUTH -> {
                leftSupport = basePosition.offset(-(WIDTH - 1), 0, 0);
                rightSupport = basePosition.offset(0, 0, 0);
            }
            case EAST -> {
                leftSupport = basePosition.offset(0, 0, 0);
                rightSupport = basePosition.offset(0, 0, WIDTH - 1);
            }
            case WEST -> {
                leftSupport = basePosition.offset(0, 0, -(WIDTH - 1));
                rightSupport = basePosition.offset(0, 0, 0);
            }
            default -> {
                return blocks;
            }
        }
        blocks.add(leftSupport);
        blocks.add(rightSupport);

        return blocks;
    }

    public boolean isSupportBlock(BlockPos pos) {
        return getSupportBlocks().contains(pos);
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + " - Half wall 5x1 facing " + orientation;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putString("orientation", orientation.name());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("orientation")) {
            this.orientation = WallStructure.WallOrientation.valueOf(tag.getString("orientation"));
            refreshBlockPositions();
        }
    }
}
