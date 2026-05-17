package com.constructions.blocks;

import com.constructions.ConstructionsMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Регистрация всех блоков мода
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
            DeferredRegister.create(ForgeRegistries.BLOCKS, ConstructionsMod.MODID);

    // Блоки для структур строений
    public static final RegistryObject<Block> STRUCTURE_BLOCK = BLOCKS.register("structure_block",
            () -> new StructureBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(3.0f, 6.0f)
                    .isValidSpawn((state, level, pos, entity) -> false)));

    public static final RegistryObject<Block> FOUNDATION_BASE = BLOCKS.register("foundation_base",
            () -> new FoundationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(4.0f, 8.0f)));

    public static final RegistryObject<Block> DOOR_BLOCK = BLOCKS.register("door_block",
            () -> new DoorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(3.0f, 6.0f)));
}
