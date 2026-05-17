package com.constructions.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Пакет для удаления структуры
 * Отправляется с клиента на сервер
 */
public class RemoveStructurePacket {
    private UUID structureId;
    private UUID playerId;

    public RemoveStructurePacket() {
    }

    public RemoveStructurePacket(UUID structureId, UUID playerId) {
        this.structureId = structureId;
        this.playerId = playerId;
    }

    public static void toBytes(RemoveStructurePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.structureId);
        buf.writeUUID(msg.playerId);
    }

    public static RemoveStructurePacket decode(FriendlyByteBuf buf) {
        return new RemoveStructurePacket(buf.readUUID(), buf.readUUID());
    }

    public static boolean handlePacket(RemoveStructurePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // TODO: Обработать удаление структуры на сервере
        });
        return true;
    }

    public UUID getStructureId() { return structureId; }
    public UUID getPlayerId() { return playerId; }
}
