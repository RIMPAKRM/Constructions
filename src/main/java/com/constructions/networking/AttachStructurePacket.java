package com.constructions.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Пакет для скрепления одной структуры к другой
 * Отправляется с клиента на сервер
 */
public class AttachStructurePacket {
    private UUID mainStructureId;
    private UUID attachedStructureId;

    public AttachStructurePacket() {
    }

    public AttachStructurePacket(UUID mainStructureId, UUID attachedStructureId) {
        this.mainStructureId = mainStructureId;
        this.attachedStructureId = attachedStructureId;
    }

    public static void toBytes(AttachStructurePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.mainStructureId);
        buf.writeUUID(msg.attachedStructureId);
    }

    public static AttachStructurePacket decode(FriendlyByteBuf buf) {
        return new AttachStructurePacket(buf.readUUID(), buf.readUUID());
    }

    public static boolean handlePacket(AttachStructurePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // TODO: Обработать скрепление структур на сервере
        });
        return true;
    }

    public UUID getMainStructureId() { return mainStructureId; }
    public UUID getAttachedStructureId() { return attachedStructureId; }
}
