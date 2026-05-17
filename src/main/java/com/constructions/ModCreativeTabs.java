package com.constructions;

import com.constructions.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Регистрация творческих вкладок (Creative Tabs) для мода
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ConstructionsMod.MODID);

    public static final RegistryObject<CreativeModeTab> CONSTRUCTIONS_TAB = CREATIVE_MODE_TABS.register("constructions_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Constructions"))
                    .icon(() -> new ItemStack(ModItems.BUILDER_HAMMER.get()))
                    .displayItems((parameters, output) -> {
                                                output.accept(ModItems.BUILDER_HAMMER.get());
                                                output.accept(ModItems.WEAK_EXPLOSIVE.get());
                                                output.accept(ModItems.MEDIUM_EXPLOSIVE.get());
                                                output.accept(ModItems.STRONG_EXPLOSIVE.get());
                                                output.accept(ModItems.FOUNDATION_ITEM.get());
                                                output.accept(ModItems.WALL_ITEM.get());
                                                output.accept(ModItems.DOOR_FRAME_ITEM.get());
                                                output.accept(ModItems.ROOF_ITEM.get());
                                                output.accept(ModItems.ROOF_HOLE_ITEM.get());
                                                output.accept(ModItems.ROOF_HOLE_TRAPDOOR_ITEM.get());
                                                output.accept(ModItems.FLOOR_LADDER_ITEM.get());
                                                output.accept(ModItems.FLOOR_LADDER_NO_SUPPORT_ITEM.get());
                                                output.accept(ModItems.WOODEN_DOOR_ITEM.get());
                                                output.accept(ModItems.IRON_DOOR_ITEM.get());
                                                output.accept(ModItems.STORAGE_CHEST_ITEM.get());
                                                output.accept(ModItems.AUTH_CABINET_ITEM.get());
                                                output.accept(ModItems.CAMPFIRE_ITEM.get());
                                                output.accept(ModItems.STRUCTURE_BLOCK_ITEM.get());
                                                output.accept(ModItems.FOUNDATION_BASE_ITEM.get());
                                                output.accept(ModItems.DOOR_BLOCK_ITEM.get());
                    }).build());
}
