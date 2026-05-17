package com.constructions.networking;

import com.constructions.ConstructionsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Управление сетевыми каналами для синхронизации между клиентом и сервером
 */
public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ConstructionsMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    /**
     * Регистрировать все сетевые пакеты
     */
    public static void register() {
        // Клиент -> Сервер пакеты
        INSTANCE.messageBuilder(PlaceStructurePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PlaceStructurePacket::decode)
                .encoder(PlaceStructurePacket::toBytes)
                .consumerMainThread(PlaceStructurePacket::handlePacket)
                .add();

        INSTANCE.messageBuilder(RemoveStructurePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(RemoveStructurePacket::decode)
                .encoder(RemoveStructurePacket::toBytes)
                .consumerMainThread(RemoveStructurePacket::handlePacket)
                .add();

        INSTANCE.messageBuilder(AttachStructurePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(AttachStructurePacket::decode)
                .encoder(AttachStructurePacket::toBytes)
                .consumerMainThread(AttachStructurePacket::handlePacket)
                .add();

        // Сервер -> Клиент пакеты
        INSTANCE.messageBuilder(SyncStructuresPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncStructuresPacket::decode)
                .encoder(SyncStructuresPacket::toBytes)
                .consumerMainThread(SyncStructuresPacket::handlePacket)
                .add();

        INSTANCE.messageBuilder(UpdateStructureHealthPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateStructureHealthPacket::decode)
                .encoder(UpdateStructureHealthPacket::toBytes)
                .consumerMainThread(UpdateStructureHealthPacket::handlePacket)
                .add();
    }

    /**
     * Отправить пакет всем игрокам на сервере
     */
    public static <MSG> void sendToAll(MSG message) {
        // TODO: Реализовать отправку пакета всем игрокам
    }

    /**
     * Отправить пакет конкретному игроку
     */
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        // TODO: Реализовать отправку пакета конкретному игроку
    }
}
