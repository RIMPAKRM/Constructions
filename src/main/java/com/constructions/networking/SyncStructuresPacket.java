package com.constructions.networking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Пакет синхронизации всех структур с клиентом
 * Отправляется с сервера на клиент при подключении или обновлении
 */
public class SyncStructuresPacket {
    private ListTag structuresData;

    public SyncStructuresPacket() {
    }

    public SyncStructuresPacket(ListTag structuresData) {
        this.structuresData = structuresData;
    }

    public static void toBytes(SyncStructuresPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(new CompoundTag() {{
            put("structures", msg.structuresData);
        }});
    }

    public static SyncStructuresPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        return new SyncStructuresPacket(tag.getList("structures", net.minecraft.nbt.Tag.TAG_COMPOUND));
    }

    public static boolean handlePacket(SyncStructuresPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                // TODO: Обработать синхронизацию структур на клиенте
            });
        });
        return true;
    }

    public ListTag getStructuresData() { return structuresData; }
}
