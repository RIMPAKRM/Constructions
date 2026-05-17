package com.constructions.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Базовый блок для структур
 * Используется как строительный блок в структурах базы
 */
public class StructureBlock extends Block {
    public StructureBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, net.minecraft.world.level.Level level, 
                       net.minecraft.core.BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // TODO: Обработка установки блока
    }

    @Override
    public void destroy(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        // TODO: Обработка уничтожения блока
    }
}
