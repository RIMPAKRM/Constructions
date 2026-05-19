package com.constructions.items;

import com.constructions.ConstructionsMod;
import com.constructions.blocks.ModBlocks;
import com.constructions.structures.ExplosiveManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Регистрация всех предметов мода
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, ConstructionsMod.MODID);

    // Инструменты для строительства
    public static final RegistryObject<Item> BUILDER_HAMMER = ITEMS.register("builder_hammer",
            () -> new BuilderHammerItem(new Item.Properties()
                    .stacksTo(1)));

    // Взрывчатка
    public static final RegistryObject<Item> WEAK_EXPLOSIVE = ITEMS.register("weak_explosive",
            () -> new ExplosiveItem(new Item.Properties(), ExplosiveManager.ExplosiveType.WEAK));
    
    public static final RegistryObject<Item> MEDIUM_EXPLOSIVE = ITEMS.register("medium_explosive",
            () -> new ExplosiveItem(new Item.Properties(), ExplosiveManager.ExplosiveType.MEDIUM));
    
    public static final RegistryObject<Item> STRONG_EXPLOSIVE = ITEMS.register("strong_explosive",
            () -> new ExplosiveItem(new Item.Properties(), ExplosiveManager.ExplosiveType.STRONG));

    // Предметы-блоки для внутренних строительных блоков
    public static final RegistryObject<Item> STRUCTURE_BLOCK_ITEM = ITEMS.register("structure_block",
            () -> new BlockItem(ModBlocks.STRUCTURE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> FOUNDATION_BASE_ITEM = ITEMS.register("foundation_base",
            () -> new BlockItem(ModBlocks.FOUNDATION_BASE.get(), new Item.Properties()));

    public static final RegistryObject<Item> DOOR_BLOCK_ITEM = ITEMS.register("door_block",
            () -> new BlockItem(ModBlocks.DOOR_BLOCK.get(), new Item.Properties()));

    // Предметы для строительства базы
    public static final RegistryObject<Item> FOUNDATION_ITEM = ITEMS.register("foundation_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "foundation"));
    
    public static final RegistryObject<Item> WALL_ITEM = ITEMS.register("wall_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "wall"));
    
    public static final RegistryObject<Item> DOOR_FRAME_ITEM = ITEMS.register("door_frame_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "door_frame"));

    public static final RegistryObject<Item> WINDOW_FRAME_ITEM = ITEMS.register("window_frame_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "window_frame"));

    public static final RegistryObject<Item> WINDOW_GRILLE_ITEM = ITEMS.register("window_grille_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "window_grille"));

    public static final RegistryObject<Item> HALF_WALL_ITEM = ITEMS.register("half_wall_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "half_wall"));
    
    public static final RegistryObject<Item> ROOF_ITEM = ITEMS.register("roof_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "roof"));

    public static final RegistryObject<Item> ROOF_HOLE_ITEM = ITEMS.register("roof_hole_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "roof_hole"));

    public static final RegistryObject<Item> ROOF_HOLE_TRAPDOOR_ITEM = ITEMS.register("roof_hole_trapdoor_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "roof_hole_trapdoor"));

    public static final RegistryObject<Item> RAMP_ITEM = ITEMS.register("ramp_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "ramp"));

    public static final RegistryObject<Item> FLOOR_LADDER_ITEM = ITEMS.register("floor_ladder_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "floor_ladder"));

    public static final RegistryObject<Item> FLOOR_LADDER_NO_SUPPORT_ITEM = ITEMS.register("floor_ladder_no_support_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "floor_ladder_no_support"));
    
    public static final RegistryObject<Item> WOODEN_DOOR_ITEM = ITEMS.register("wooden_door_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "wooden_door"));
    
    public static final RegistryObject<Item> IRON_DOOR_ITEM = ITEMS.register("iron_door_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "iron_door"));
    
    public static final RegistryObject<Item> STORAGE_CHEST_ITEM = ITEMS.register("storage_chest_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "storage_chest"));
    
    public static final RegistryObject<Item> AUTH_CABINET_ITEM = ITEMS.register("auth_cabinet_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "auth_cabinet"));
    
    public static final RegistryObject<Item> CAMPFIRE_ITEM = ITEMS.register("campfire_item",
            () -> new StructureItem(new Item.Properties()
                    .stacksTo(64), "campfire"));
}
