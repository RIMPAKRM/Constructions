package com.constructions.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Пакет обновления здоровья структуры
 * Отправляется с сервера на клиент при изменении здоровья
 */
public class UpdateStructureHealthPacket {
    private UUID structureId;
    private double health;
    private double maxHealth;

    public UpdateStructureHealthPacket() {
    }

    public UpdateStructureHealthPacket(UUID structureId, double health, double maxHealth) {
        this.structureId = structureId;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public static void toBytes(UpdateStructureHealthPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.structureId);
        buf.writeDouble(msg.health);
        buf.writeDouble(msg.maxHealth);
    }

    public static UpdateStructureHealthPacket decode(FriendlyByteBuf buf) {
        return new UpdateStructureHealthPacket(buf.readUUID(), buf.readDouble(), buf.readDouble());
    }

    public static boolean handlePacket(UpdateStructureHealthPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                // TODO: Обновить здоровье структуры на клиенте
            });
        });
        return true;
    }

    public UUID getStructureId() { return structureId; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
}
