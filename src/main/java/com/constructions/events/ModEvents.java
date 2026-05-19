package com.constructions.events;

import com.constructions.ConstructionsMod;
import com.constructions.structures.StructureManager;
import com.constructions.structures.Structure;
import com.constructions.structures.AuthCabinetStructure;
import com.constructions.structures.FoundationStructure;
import com.constructions.structures.DoorStructure;
import com.constructions.structures.RoofHoleTrapdoorStructure;
import com.constructions.structures.ExplosiveManager;
import com.constructions.ConstructionsConfig;
import com.constructions.items.ExplosiveItem;
import com.constructions.items.BuilderHammerItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
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
import java.util.HashMap;
import java.util.Map;

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
            // Если игрок держит молот и натягивает его - блокируем все действия
            ItemStack itemStack = player.getItemInHand(event.getHand());
            boolean holdingHammer = itemStack.getItem() instanceof BuilderHammerItem;
            if (itemStack.getItem() instanceof BuilderHammerItem && player.isUsingItem()) {
                event.setCanceled(true);
                return;
            }

            BlockPos clickedPos = event.getPos();
            StructureManager manager = StructureManager.get(player.level());
            
            Structure structure = manager.getStructureAtPosition(clickedPos);
            if (structure instanceof AuthCabinetStructure cabinet) {
                // Игнорируем взаимодействие со шкафом, если в руках молот,
                // чтобы запускался сценарий разборки молотом.
                if (holdingHammer) {
                    return;
                }

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
        Structure targetStructure = manager.getStructureAtPositionPrecise(targetPos);

        if (targetStructure == null) {
            targetStructure = manager.getStructureAtPosition(targetPos);
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
        int radius = 20;
        
        List<Structure> nearby = manager.getStructuresInRadius(pos, radius);
        boolean hasCabinetInRadius = false;
        for (Structure s : nearby) {
            if (s instanceof AuthCabinetStructure cabinet) {
                hasCabinetInRadius = true;
                // Если шкаф активен - проверяем авторизацию, если неактивен - доступ запрещён
                return cabinet.isActive() && cabinet.isPlayerAuthorized(playerId);
            }
        }
        
        // Если в радиусе 20 блоков вообще нет шкафа — доступ разрешён.
        return !hasCabinetInRadius;
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

    // Отслеживание молотов: UUID игрока -> остаток тиков натяжения в предыдущем тике
    private static final Map<UUID, Integer> HAMMER_CHARGE_TRACKING = new HashMap<>();

    /**
     * Обработчик завершения натяжения молота - срабатывает когда остаток тиков упал до 0
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide && event.player instanceof ServerPlayer player) {
            ItemStack mainHand = player.getMainHandItem();
            UUID playerId = player.getUUID();
            
            if (mainHand.getItem() instanceof BuilderHammerItem) {
                int currentTicks = player.getUseItemRemainingTicks();
                Integer previousTicks = HAMMER_CHARGE_TRACKING.getOrDefault(playerId, -1);
                
                // Если натяжение только что завершилось (переход из положительного в 0 или ниже)
                if (previousTicks > 0 && currentTicks <= 0) {
                    handleHammerStrike(player, mainHand);
                }
                
                // Сохраняем текущий остаток для следующего тика
                HAMMER_CHARGE_TRACKING.put(playerId, currentTicks);
            } else {
                // Очищаем отслеживание если молот больше не в руке
                HAMMER_CHARGE_TRACKING.remove(playerId);
            }
        }
    }

    private static void handleHammerStrike(ServerPlayer player, ItemStack hammerStack) {
        if (player.level().isClientSide) {
            return;
        }

        // Ищем блок, на который смотрит игрок
        if (player.pick(5.0, 0.0f, false) instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            StructureManager manager = StructureManager.get(player.level());
            Structure structure = manager.getStructureAtPosition(pos);

            if (structure == null) {
                net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(pos);
                if (state.is(net.minecraft.world.level.block.Blocks.TORCH)
                        || state.is(net.minecraft.world.level.block.Blocks.WALL_TORCH)) {
                    player.level().setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                    ItemStack torchStack = new ItemStack(net.minecraft.world.item.Items.TORCH, 1);
                    if (!player.addItem(torchStack)) {
                        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                                player.level(),
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                torchStack
                        );
                        player.level().addFreshEntity(itemEntity);
                    }
                    player.displayClientMessage(Component.literal("§a[Молот] Факел снят."), false);
                }
                return;
            }

            // Молот работает только для игроков, авторизованных в шкафе.
            if (!isPlayerAuthorizedForAccess(player, structure.getBasePosition(), manager)) {
                player.displayClientMessage(Component.literal("§c[Молот] Вы не авторизированы в шкафе авторизации!"), false);
                return;
            }

            // Создаём ItemStack из типа структуры
            ItemStack structureItem = createStructureItemStack(structure);

            // Наносим 1000 урона структуре (гарантирует разрушение)
            manager.damageStructure(player.level(), structure.getStructureId(), 1000.0, false);

            // Кладём в инвентарь или дропим
            if (!player.addItem(structureItem)) {
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                        player.level(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        structureItem
                );
                player.level().addFreshEntity(itemEntity);
            }

            player.displayClientMessage(Component.literal("§a[Молот] Структура разобрана."), false);
        }
    }

    /**
     * Создаёт ItemStack на основе типа разрушенной структуры
     */
    private static ItemStack createStructureItemStack(Structure structure) {
        return switch(structure.getStructureType()) {
            case "foundation" -> new ItemStack(com.constructions.items.ModItems.FOUNDATION_ITEM.get(), 1);
            case "wall" -> new ItemStack(com.constructions.items.ModItems.WALL_ITEM.get(), 1);
            case "door_frame" -> new ItemStack(com.constructions.items.ModItems.DOOR_FRAME_ITEM.get(), 1);
            case "roof" -> new ItemStack(com.constructions.items.ModItems.ROOF_ITEM.get(), 1);
            case "roof_hole" -> new ItemStack(com.constructions.items.ModItems.ROOF_HOLE_ITEM.get(), 1);
            case "roof_hole_trapdoor" -> new ItemStack(com.constructions.items.ModItems.ROOF_HOLE_TRAPDOOR_ITEM.get(), 1);
            case "floor_ladder" -> new ItemStack(com.constructions.items.ModItems.FLOOR_LADDER_ITEM.get(), 1);
            case "floor_ladder_no_support" -> new ItemStack(com.constructions.items.ModItems.FLOOR_LADDER_NO_SUPPORT_ITEM.get(), 1);
            case "wooden_door" -> new ItemStack(com.constructions.items.ModItems.WOODEN_DOOR_ITEM.get(), 1);
            case "iron_door" -> new ItemStack(com.constructions.items.ModItems.IRON_DOOR_ITEM.get(), 1);
            case "storage_chest" -> new ItemStack(com.constructions.items.ModItems.STORAGE_CHEST_ITEM.get(), 1);
            case "window_frame" -> new ItemStack(com.constructions.items.ModItems.WINDOW_FRAME_ITEM.get(), 1);
            case "window_grille" -> new ItemStack(com.constructions.items.ModItems.WINDOW_GRILLE_ITEM.get(), 1);
            case "half_wall" -> new ItemStack(com.constructions.items.ModItems.HALF_WALL_ITEM.get(), 1);
            case "ramp" -> new ItemStack(com.constructions.items.ModItems.RAMP_ITEM.get(), 1);
            case "auth_cabinet" -> new ItemStack(com.constructions.items.ModItems.AUTH_CABINET_ITEM.get(), 1);
            case "campfire" -> new ItemStack(com.constructions.items.ModItems.CAMPFIRE_ITEM.get(), 1);
            default -> new ItemStack(net.minecraft.world.item.Items.DIAMOND, 1);
        };
    }
}
