package com.constructions.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * Предмет для строительства (молоток строителя)
 * При использовании активирует режим предварительного просмотра структуры
 */
public class BuilderHammerItem extends Item {
    
    public BuilderHammerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player.isShiftKeyDown()) {
            // Сервер обрабатывает удаление строения
            // TODO: Реализовать логику удаления строения
            return InteractionResultHolder.success(itemStack);
        }
        
        if (level.isClientSide) {
            // Клиент активирует режим строительства
            // TODO: Отправить пакет на сервер для активации режима предварительного просмотра
        }
        
        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false; // Молоток нельзя чинить
    }
}
