package com.constructions.items;

import com.constructions.structures.ExplosiveManager;
import com.constructions.structures.Structure;
import com.constructions.structures.StructureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Предмет взрывчатки, который может крепиться к структуре и наносить ей урон.
 */
public class ExplosiveItem extends Item {
    private final ExplosiveManager.ExplosiveType explosiveType;

    public ExplosiveItem(Properties properties, ExplosiveManager.ExplosiveType explosiveType) {
        super(properties);
        this.explosiveType = explosiveType;
    }

    public ExplosiveManager.ExplosiveType getExplosiveType() {
        return explosiveType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        StructureManager structureManager = StructureManager.get(level);
        BlockPos targetPos = context.getClickedPos();
        Structure structure = structureManager.getStructureAtPositionPrecise(targetPos);

        if (structure == null) {
            structure = structureManager.getStructureAtPosition(targetPos);
        }

        if (structure == null) {
            return InteractionResult.FAIL;
        }

        ExplosiveManager explosiveManager = ExplosiveManager.getInstance();
        explosiveManager.attachExplosive(structure.getStructureId(), targetPos, explosiveType);
        explosiveManager.detonateExplosive(level, structure.getStructureId(), targetPos, explosiveType, structureManager);

        ItemStack stack = player.getItemInHand(context.getHand());
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}