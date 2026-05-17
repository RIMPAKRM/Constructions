package com.constructions.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/**
 * Утилиты для работы с ландшафтом и размещением структур
 */
public class TerrainUtils {
    
    /**
     * Проверить, может ли блок быть использован как опорная точка
     */
    public static boolean isSolidBlock(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // TODO: Полная реализация проверки твёрдости блока
        return !state.isAir();
    }

    /**
     * Найти твёрдую поверхность для фундамента на данной позиции
     * 
     * @return высота, на которой можно разместить фундамент, или -1 если невозможно
     */
    public static int findFoundationHeight(BlockGetter level, BlockPos basePos, int maxHeight) {
        // Ищем первый твёрдый блок ниже базовой позиции
        for (int y = basePos.getY(); y >= basePos.getY() - maxHeight; y--) {
            BlockPos checkPos = new BlockPos(basePos.getX(), y, basePos.getZ());
            if (isSolidBlock(level, checkPos)) {
                return basePos.getY() - y;
            }
        }
        return -1;
    }

    /**
     * Получить все блоки, которые нужно удалить для установки структуры
     * (листва, деревянные заборы и т.д.)
     */
    public static Set<BlockPos> getBlocksToRemove(BlockGetter level, Set<BlockPos> structureBlocks) {
        Set<BlockPos> toRemove = new HashSet<>();
        
        for (BlockPos pos : structureBlocks) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            
            // Удалять листву и деревянные заборы
            if (block instanceof LeavesBlock || 
                block instanceof FenceBlock) {
                toRemove.add(pos);
            }
        }
        
        return toRemove;
    }

    /**
     * Проверить, есть ли воздух или жидкость в структурной позиции
     */
    public static boolean isAirOrLiquid(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // TODO: Полная реализация проверки жидкости
        return state.isAir();
    }
}
