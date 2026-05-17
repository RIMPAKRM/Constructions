package com.constructions.events;

import com.constructions.ConstructionsMod;
import com.constructions.structures.StructureManager;
import com.constructions.structures.Structure;
import com.constructions.structures.AuthCabinetStructure;
import com.constructions.structures.DoorStructure;
import com.constructions.structures.RoofHoleTrapdoorStructure;
import com.constructions.structures.ExplosiveManager;
import com.constructions.ConstructionsConfig;
import com.constructions.items.ExplosiveItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.UUID;

/**
 * Обработчики событий мода Constructions
 */
@Mod.EventBusSubscriber(modid = ConstructionsMod.MODID)
public class ModEvents {

    /**
     * Событие разрушения блока
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && !event.getPlayer().level().isClientSide) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            BlockPos brokenPos = event.getPos();
            StructureManager manager = StructureManager.get(player.level());
            
            Structure structure = manager.getStructureAtPosition(brokenPos);
            if (structure != null) {
                manager.damageStructure(structure.getStructureId(), structure.getMaxHealth());
                event.setCanceled(true);
            }
        }
    }

    /**
     * Событие размещения блока
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            StructureManager manager = StructureManager.get(player.level());
            
            // TODO: Проверить, может ли игрок размещать блоки (режим Adventure)
            // TODO: Проверить расстояние от зон лута
        }
    }

    /**
     * Событие атаки на сущность (для взрывчатки на структурах)
     */
    @SubscribeEvent
    public static void onEntityAttack(AttackEntityEvent event) {
        // TODO: Обработать атаку на взрывчатку, прикреплённую к структуре
    }

    /**
     * Событие правого клика по блоку (взаимодействие со шкафом авторизации)
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            BlockPos clickedPos = event.getPos();
            StructureManager manager = StructureManager.get(player.level());
            
            Structure structure = manager.getStructureAtPosition(clickedPos);
            if (structure instanceof AuthCabinetStructure cabinet) {
                event.setCanceled(true);

                if (player.isCrouching()) {
                    cabinet.clearAllAuthorizedPlayers();
                    player.displayClientMessage(Component.literal("§c[Шкаф авторизации] Все игроки очищены."), false);
                    return;
                }

                // Если шкаф неактивен и это владелец - активировать
                if (!cabinet.isActive() && player.getUUID().equals(cabinet.getOwner())) {
                    cabinet.setActive(true);
                    player.displayClientMessage(Component.literal("§a[Шкаф авторизации] Шкаф активирован. Система доступа включена."), false);
                    return;
                }

                // ПКМ - авторизовать или показать список авторизованных
                if (cabinet.isPlayerInAuthList(player.getUUID())) {
                    // Показать список авторизованных
                    StringBuilder playerList = new StringBuilder("§6[Шкаф авторизации] Авторизованные игроки:\n");
                    for (var authorizedId : cabinet.getAuthorizedPlayers()) {
                        playerList.append("§7- ").append(resolvePlayerName(player, authorizedId)).append("\n");
                    }
                    if (cabinet.getAuthorizedPlayers().isEmpty()) {
                        playerList.append("§7Никто не авторизован\n");
                    }
                    playerList.append("§7+ Владелец: ").append(resolvePlayerName(player, cabinet.getOwner()));
                    // Сообщение только этому игроку
                    player.displayClientMessage(
                        Component.literal(playerList.toString()),
                        false
                    );
                } else {
                    // Авторизовать игрока
                    cabinet.addAuthorizedPlayer(player.getUUID());
                    // Сообщение только этому игроку
                    player.displayClientMessage(Component.literal("§a[Шкаф авторизации] Вы авторизированы! Нажмите ПКМ снова чтобы увидеть список."), false);
                }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            BlockPos clickedPos = event.getPos();
            StructureManager manager = StructureManager.get(player.level());
            Structure structure = manager.getStructureAtPosition(clickedPos);
            if (structure instanceof AuthCabinetStructure cabinet) {
                event.setCanceled(true);
                cabinet.clearAllAuthorizedPlayers();
                player.displayClientMessage(Component.literal("§c[Шкаф авторизации] Все игроки очищены."), false);
            }
        }
    }

    /**
     * Проверка авторизации при взаимодействии с дверями и люками
     */
    @SubscribeEvent
    public static void onDoorInteraction(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            BlockPos pos = event.getPos();
            StructureManager manager = StructureManager.get(player.level());
            
            Structure structure = manager.getStructureAtPosition(pos);
            if (structure != null && !(structure instanceof AuthCabinetStructure)) {
                if (tryDetonateStructureWithExplosive(event, player, structure, manager)) {
                    event.setCanceled(true);
                    return;
                }
            }

            if (structure instanceof DoorStructure door) {
                boolean authorized = isPlayerAuthorizedForAccess(player, door.getBasePosition(), manager);
                if (!authorized) {
                    player.displayClientMessage(Component.literal("§c[Доступ запрещён] Вы не авторизированы в шкафе авторизации!"), false);
                    event.setCanceled(true);
                }
            } else if (structure instanceof RoofHoleTrapdoorStructure hatch) {
                boolean authorized = isPlayerAuthorizedForAccess(player, hatch.getBasePosition(), manager);
                if (!authorized) {
                    player.displayClientMessage(Component.literal("§c[Доступ запрещён] Вы не авторизированы в шкафе авторизации!"), false);
                    event.setCanceled(true);
                }
            }
        }
    }

    private static boolean tryDetonateStructureWithExplosive(PlayerInteractEvent.RightClickBlock event, ServerPlayer player, Structure structure, StructureManager manager) {
        ItemStack stack = player.getItemInHand(event.getHand());
        if (!(stack.getItem() instanceof ExplosiveItem explosiveItem)) {
            return false;
        }

        BlockPos targetPos = structure.getBasePosition();
        Structure targetStructure = manager.getStructureAtPosition(targetPos);
        if (targetStructure == null) {
            targetStructure = manager.getStructureAtPosition(targetPos.below());
        }

        if (targetStructure == null) {
            return false;
        }

        ExplosiveManager explosiveManager = ExplosiveManager.getInstance();
        explosiveManager.attachExplosive(targetStructure.getStructureId(), targetPos, explosiveItem.getExplosiveType());
        explosiveManager.detonateExplosive(player.level(), targetStructure.getStructureId(), targetPos, explosiveItem.getExplosiveType(), manager);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return true;
    }

    private static boolean isPlayerAuthorizedForAccess(ServerPlayer player, BlockPos pos, StructureManager manager) {
        UUID playerId = player.getUUID();
        int radius = ConstructionsConfig.Server.AUTH_CABINET_RADIUS;
        
        List<Structure> nearby = manager.getStructuresInRadius(pos, radius);
        for (Structure s : nearby) {
            if (s instanceof AuthCabinetStructure cabinet) {
                // Если шкаф активен - проверяем авторизацию, если неактивен - доступ запрещён
                return cabinet.isActive() && cabinet.isPlayerAuthorized(playerId);
            }
        }
        
        // Если нет ближайшего шкафа - доступ разрешён
        return true;
    }

    private static String resolvePlayerName(ServerPlayer viewer, UUID playerId) {
        if (viewer.server != null) {
            ServerPlayer onlinePlayer = viewer.server.getPlayerList().getPlayer(playerId);
            if (onlinePlayer != null) {
                return onlinePlayer.getGameProfile().getName();
            }
        }

        return playerId.toString();
    }
}
