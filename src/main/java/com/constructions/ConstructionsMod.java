package com.constructions;

import com.constructions.items.ModItems;
import com.constructions.blocks.ModBlocks;
import com.constructions.networking.ModNetworking;
import com.constructions.events.ModEvents;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Основной класс мода Constructions для Minecraft 1.20.1 Forge
 * Управляет регистрацией всех компонентов мода
 */
@Mod(ConstructionsMod.MODID)
public class ConstructionsMod {
    public static final String MODID = "constructions";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ConstructionsMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Регистрация обработчиков жизненного цикла
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Регистрация Deferred Register для блоков и предметов
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Регистрация конфигурации
        context.registerConfig(ModConfig.Type.SERVER, ConstructionsConfig.SERVER_SPEC);
        context.registerConfig(ModConfig.Type.COMMON, ConstructionsConfig.COMMON_SPEC);

        // Регистрация основного мода для событий
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ModEvents.class);

        LOGGER.info("Constructions mod initialized with MODID: {}", MODID);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup for Constructions mod");
        event.enqueueWork(() -> {
            ModNetworking.register();
            LOGGER.info("Networking channels registered");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup for Constructions mod");
    }
}
