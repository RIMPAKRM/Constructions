package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Структура стены с оконным проёмом (5x3 блока)
 * Проём в центре 1x1 блока
 */
public class WindowFrameStructure extends Structure {
    private static final int WIDTH = 5;
    private static final int HEIGHT = 3;
    private static final double DEFAULT_MAX_HEALTH = 80.0;

    private WallStructure.WallOrientation orientation;

    public WindowFrameStructure(BlockPos basePosition, UUID owner, WallStructure.WallOrientation orientation) {
        super("window_frame", basePosition, owner);
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
            for (int y = 0; y < HEIGHT; y++) {
                if (x == 2 && y == 1) {
                    continue;
                }

                BlockPos pos;
                switch (orientation) {
                    case NORTH -> pos = basePosition.offset(x, y, 0);
                    case SOUTH -> pos = basePosition.offset(-x, y, 0);
                    case EAST -> pos = basePosition.offset(0, y, x);
                    case WEST -> pos = basePosition.offset(0, y, -x);
                    default -> {
                        continue;
                    }
                }
                blocks.add(pos);
            }
        }

        return blocks;
    }

    public List<BlockPos> getSupportBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        for (int y = 0; y < HEIGHT; y++) {
            BlockPos leftSupport;
            BlockPos rightSupport;
            switch (orientation) {
                case NORTH -> {
                    leftSupport = basePosition.offset(0, y, 0);
                    rightSupport = basePosition.offset(WIDTH - 1, y, 0);
                }
                case SOUTH -> {
                    leftSupport = basePosition.offset(-(WIDTH - 1), y, 0);
                    rightSupport = basePosition.offset(0, y, 0);
                }
                case EAST -> {
                    leftSupport = basePosition.offset(0, y, 0);
                    rightSupport = basePosition.offset(0, y, WIDTH - 1);
                }
                case WEST -> {
                    leftSupport = basePosition.offset(0, y, -(WIDTH - 1));
                    rightSupport = basePosition.offset(0, y, 0);
                }
                default -> {
                    continue;
                }
            }
            blocks.add(leftSupport);
            blocks.add(rightSupport);
        }

        return blocks;
    }

    public boolean isSupportBlock(BlockPos pos) {
        return getSupportBlocks().contains(pos);
    }

    public BlockPos getOpeningPosition() {
        return switch (orientation) {
            case NORTH -> basePosition.offset(2, 1, 0);
            case SOUTH -> basePosition.offset(-2, 1, 0);
            case EAST -> basePosition.offset(0, 1, 2);
            case WEST -> basePosition.offset(0, 1, -2);
        };
    }

    public boolean isOpeningPosition(BlockPos pos) {
        return getOpeningPosition().equals(pos);
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + " - Window frame 5x3 facing " + orientation;
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
