package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Структура стены (5 в ширину, 3 в высоту)
 * Может размещаться только на краях фундамента
 * Может быть скреплена с другими структурами
 */
public class WallStructure extends Structure {
    private static final int WIDTH = 5;
    private static final int HEIGHT = 3;
    private static final double DEFAULT_MAX_HEALTH = 80.0;
    
    // Ориентация стены (NORTH, SOUTH, EAST, WEST)
    private WallOrientation orientation;

    public enum WallOrientation {
        NORTH, SOUTH, EAST, WEST
    }

    public WallStructure(BlockPos basePosition, UUID owner, WallOrientation orientation) {
        super("wall", basePosition, owner);
        this.orientation = orientation;
        refreshBlockPositions();
    }

    public WallOrientation getOrientation() {
        return orientation;
    }

    @Override
    public List<BlockPos> getStructureBlocks() {
        List<BlockPos> blocks = new ArrayList<>();
        
        // Генерируем 5x3 блоки стены (включая боковые опоры) в правильной ориентации
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
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
        
        // Боковые опоры (слева и справа от стены, 3 блока высотой)
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
        return super.toString() + " - Wall 5x3 facing " + orientation;
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
            this.orientation = WallOrientation.valueOf(tag.getString("orientation"));
            refreshBlockPositions();
        }
    }
}
