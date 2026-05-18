package com.constructions.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;

/**
 * Предмет для строительства (молоток строителя)
 * При использовании (ПКМ) натягивается как лук за 10 секунд, затем сносит структуру и кладет в инвентарь
 */
public class BuilderHammerItem extends Item {
    private static final int CHARGE_TIME = 200; // 10 секунд = 200 тиков
    
    public BuilderHammerItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return CHARGE_TIME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int timeLeft) {
        if (livingEntity instanceof Player player && !level.isClientSide) {
            // Ничего не делаем при натяжении, только отсчитываем время
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false; // Молоток нельзя чинить
    }
}
