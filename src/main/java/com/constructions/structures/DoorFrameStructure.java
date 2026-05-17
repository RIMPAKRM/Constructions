package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Структура дверного проёма (5x3 блока)
 * Может размещаться только на краях фундамента
 * Может быть скреплена с другими структурами
 */
public class DoorFrameStructure extends Structure {
    private static final int WIDTH = 5;
    private static final int HEIGHT = 3;
    private static final double DEFAULT_MAX_HEALTH = 60.0;
    
    // Ориентация дверного проёма (NORTH, SOUTH, EAST, WEST)
    private WallStructure.WallOrientation orientation;

    public DoorFrameStructure(BlockPos basePosition, UUID owner, WallStructure.WallOrientation orientation) {
        super("door_frame", basePosition, owner);
        this.orientation = orientation;
        refreshBlockPositions();
    }

    public WallStructure.WallOrientation getOrientation() {
        return orientation;
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();

        // Генерируем 5x3 блоки рамку с щелью 1x2 по центру (x=2, y=0..1)
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (x == 2 && y <= 1) {
                    continue;
                }

                BlockPos pos;
                switch (orientation) {
                    case NORTH:
                        pos = basePosition.offset(x, y, 0);
                        break;
                    case SOUTH:
                        pos = basePosition.offset(-x, y, 0);
                        break;
                    case EAST:
                        pos = basePosition.offset(0, y, x);
                        break;
                    case WEST:
                        pos = basePosition.offset(0, y, -x);
                        break;
                    default:
                        continue;
                }
                blocks.add(pos);
            }
        }
        
        return blocks;
    }

    public List<BlockPos> getSupportBlocks() {
        List<BlockPos> blocks = new ArrayList<>();
        
        // Боковые опоры (слева и справа от проёма, 3 блока высотой)
        // Это x=0 и x=4 позиции в структуре
        for (int y = 0; y < HEIGHT; y++) {
            BlockPos leftSupport, rightSupport;
            switch (orientation) {
                case NORTH:
                    leftSupport = basePosition.offset(0, y, 0);
                    rightSupport = basePosition.offset(WIDTH - 1, y, 0);
                    break;
                case SOUTH:
                    leftSupport = basePosition.offset(-(WIDTH - 1), y, 0);
                    rightSupport = basePosition.offset(0, y, 0);
                    break;
                case EAST:
                    leftSupport = basePosition.offset(0, y, 0);
                    rightSupport = basePosition.offset(0, y, WIDTH - 1);
                    break;
                case WEST:
                    leftSupport = basePosition.offset(0, y, -(WIDTH - 1));
                    rightSupport = basePosition.offset(0, y, 0);
                    break;
                default:
                    continue;
            }
            blocks.add(leftSupport);
            blocks.add(rightSupport);
        }
        
        return blocks;
    }

    public boolean isSupportBlock(BlockPos pos) {
        List<BlockPos> supports = getSupportBlocks();
        return supports.contains(pos);
    }

    @Override
    protected double getDefaultMaxHealth() {
        return DEFAULT_MAX_HEALTH;
    }

    @Override
    public String toString() {
        return super.toString() + " - DoorFrame 5x3 facing " + orientation;
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
