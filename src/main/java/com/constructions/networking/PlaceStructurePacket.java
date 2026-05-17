package com.constructions.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import com.constructions.utils.StructurePlacementUtils;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Пакет для размещения структуры на сервере
 * Отправляется с клиента на сервер
 */
public class PlaceStructurePacket {
    private UUID playerId;
    private BlockPos position;
    private String structureType;
    private float yaw; // Ориентация для разных типов структур
    private String face;
    private String hand;

    public PlaceStructurePacket() {
    }

    public PlaceStructurePacket(UUID playerId, BlockPos position, String structureType, float yaw, String face, String hand) {
        this.playerId = playerId;
        this.position = position;
        this.structureType = structureType;
        this.yaw = yaw;
        this.face = face;
        this.hand = hand;
    }

    public static void toBytes(PlaceStructurePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
        buf.writeBlockPos(msg.position);
        buf.writeUtf(msg.structureType);
        buf.writeFloat(msg.yaw);
        buf.writeUtf(msg.face == null ? Direction.UP.name() : msg.face);
        buf.writeUtf(msg.hand == null ? InteractionHand.MAIN_HAND.name() : msg.hand);
    }

    public static PlaceStructurePacket decode(FriendlyByteBuf buf) {
        UUID playerId = buf.readUUID();
        BlockPos position = buf.readBlockPos();
        String structureType = buf.readUtf(128);
        float yaw = buf.readFloat();
        String face = buf.readUtf(32);
        String hand = buf.readUtf(32);
        return new PlaceStructurePacket(playerId, position, structureType, yaw, face, hand);
    }

    public static boolean handlePacket(PlaceStructurePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer == null) {
                return;
            }

            if (!serverPlayer.getUUID().equals(msg.playerId)) {
                return;
            }

            ServerLevel serverLevel = serverPlayer.serverLevel();
            Direction faceDirection;
            try {
                faceDirection = Direction.valueOf(msg.face == null ? Direction.UP.name() : msg.face);
            } catch (IllegalArgumentException ex) {
                faceDirection = Direction.UP;
            }

            InteractionHand interactionHand;
            try {
                interactionHand = InteractionHand.valueOf(msg.hand == null ? InteractionHand.MAIN_HAND.name() : msg.hand);
            } catch (IllegalArgumentException ex) {
                interactionHand = InteractionHand.MAIN_HAND;
            }

                boolean placed = StructurePlacementUtils.placeStructure(
                    serverLevel,
                    serverPlayer,
                    msg.position,
                    msg.structureType,
                    msg.yaw,
                        faceDirection,
                    interactionHand
            );

            if (placed) {
                serverPlayer.inventoryMenu.broadcastChanges();
            }
        });
        context.setPacketHandled(true);
        return true;
    }

    public UUID getPlayerId() { return playerId; }
    public BlockPos getPosition() { return position; }
    public String getStructureType() { return structureType; }
    public float getYaw() { return yaw; }
    public String getFace() { return face; }
    public String getHand() { return hand; }
}
