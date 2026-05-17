package com.constructions.items;

import com.constructions.networking.ModNetworking;
import com.constructions.networking.PlaceStructurePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

/**
 * Предмет для установки структуры (фундамент, стена, дверь и т.д.)
 * При использовании показывает превью структуры и позволяет её разместить
 */
public class StructureItem extends Item {
    private final String structureType;

    public StructureItem(Item.Properties properties, String structureType) {
        super(properties);
        this.structureType = structureType;
    }

    public String getStructureType() {
        return this.structureType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            sendPlacementPacket(player, context.getClickedPos(), context.getClickedFace().name(), context.getHand().name());
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            return InteractionResultHolder.pass(itemStack);
        }

        HitResult hitResult = player.pick(5.0D, 0.0F, false);
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos hitPos = blockHitResult.getBlockPos();
            sendPlacementPacket(player, hitPos, blockHitResult.getDirection().name(), hand.name());
            return InteractionResultHolder.sidedSuccess(itemStack, true);
        }

        return InteractionResultHolder.pass(itemStack);
    }

    private void sendPlacementPacket(Player player, BlockPos position, String face, String hand) {
        ModNetworking.INSTANCE.sendToServer(new PlaceStructurePacket(
                player.getUUID(),
                position,
                structureType,
                player.getYRot(),
                face,
                hand
        ));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }
}
