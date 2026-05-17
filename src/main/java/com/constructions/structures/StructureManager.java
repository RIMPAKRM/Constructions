package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import com.constructions.blocks.ModBlocks;
import com.constructions.utils.StructurePlacementUtils;

import java.util.*;

/**
 * Менеджер для хранения и управления всеми структурами на сервере
 * Использует SavedData для сохранения между перезагрузками
 */
public class StructureManager extends SavedData {
    private static final String FILE_NAME = "constructions_structures";
    
    private Map<UUID, Structure> structures = new HashMap<>();
    private Map<BlockPos, UUID> blockToStructure = new HashMap<>();
    private Map<UUID, Long> structureCreationTime = new HashMap<>();

    public StructureManager() {
    }

    /**
     * Получить менеджер структур для мира
     */
    public static StructureManager get(Level level) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel)) {
            throw new RuntimeException("StructureManager can only be accessed on server-side!");
        }
        
        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(StructureManager::load, StructureManager::new, FILE_NAME);
    }

    /**
     * Добавить новую структуру
     */
    public void addStructure(Structure structure) {
        structures.put(structure.getStructureId(), structure);
        structureCreationTime.put(structure.getStructureId(), System.currentTimeMillis());
        
        // Обновить маппинг блокпозиций
        for (BlockPos pos : structure.getBlockPositions()) {
            blockToStructure.put(pos, structure.getStructureId());
        }
        
        setDirty();
    }

    /**
     * Зарегистрировать дополнительные блоки за существующей структурой.
     * Используется для опор лестницы, которые размещаются рядом с собственными блоками лестницы.
     */
    public void addBlocksToStructure(UUID structureId, Collection<BlockPos> positions) {
        if (!structures.containsKey(structureId) || positions == null || positions.isEmpty()) {
            return;
        }

        for (BlockPos pos : positions) {
            blockToStructure.put(pos, structureId);
        }
        setDirty();
    }

    /**
     * Удалить структуру по ID
     */
    public void removeStructure(UUID structureId) {
        removeStructure(null, structureId, false);
    }

    /**
     * Удалить структуру по ID и при необходимости очистить блоки в мире.
     */
    public void removeStructure(Level level, UUID structureId) {
        removeStructure(level, structureId, false);
    }

    /**
     * Удалить структуру по ID, при необходимости пересобрав соседние фундаменты.
     */
    public void removeStructure(Level level, UUID structureId, boolean refreshNearbyFoundations) {
        Structure structure = structures.remove(structureId);
        if (structure != null) {
            structureCreationTime.remove(structureId);
            
            // Удалить из маппинга блокпозиций - основные блоки
            for (BlockPos pos : structure.getBlockPositions()) {
                blockToStructure.remove(pos);
            }
            
            // Удалить все оставшиеся блоки, связанные с этой структурой (включая опорные)
            blockToStructure.entrySet().removeIf(entry -> entry.getValue().equals(structureId));

            if (level != null) {
                cleanupSharedSupportBlocks(level, structure);
                if (refreshNearbyFoundations && structure instanceof FoundationStructure foundationStructure) {
                    refreshNeighboringFoundations(level, foundationStructure);
                }
            }
            
            setDirty();
        }
    }

    private void refreshNeighboringFoundations(Level level, FoundationStructure removedFoundation) {
        BlockPos removedBase = removedFoundation.getBasePosition();

        for (Structure structure : new ArrayList<>(structures.values())) {
            if (!(structure instanceof FoundationStructure foundationStructure)) {
                continue;
            }

            BlockPos base = foundationStructure.getBasePosition();
            int dx = Math.abs(base.getX() - removedBase.getX());
            int dz = Math.abs(base.getZ() - removedBase.getZ());
            if (dx > 8 || dz > 8) {
                continue;
            }

            refreshFoundationBlocks(level, foundationStructure);
        }
    }

    private void refreshFoundationBlocks(Level level, FoundationStructure foundation) {
        Set<BlockPos> oldPositions = foundation.getBlockPositions();
        int[] newHeights = StructurePlacementUtils.computeFoundationSupportDepths(level, foundation.getBasePosition());
        for (int i = 0; i < newHeights.length; i++) {
            if (newHeights[i] < 0) {
                newHeights[i] = 0;
            }
        }

        foundation.setPillarHeights(newHeights);
        Set<BlockPos> newPositions = foundation.getBlockPositions();

        for (BlockPos oldPos : oldPositions) {
            if (newPositions.contains(oldPos)) {
                continue;
            }

            level.setBlockAndUpdate(oldPos, Blocks.AIR.defaultBlockState());
            blockToStructure.remove(oldPos);
        }

        for (BlockPos newPos : newPositions) {
            BlockState state = newPos.getY() == foundation.getBasePosition().getY()
                    ? ModBlocks.FOUNDATION_BASE.get().defaultBlockState()
                    : Blocks.OAK_LOG.defaultBlockState();
            level.setBlockAndUpdate(newPos, state);
            blockToStructure.put(newPos, foundation.getStructureId());
        }
    }

    private void cleanupSharedSupportBlocks(Level level, Structure removedStructure) {
        for (BlockPos pos : removedStructure.getBlockPositions()) {
            if (!isSharedSupportBlock(level, pos)) {
                continue;
            }

            long remainingOwners = structures.values().stream()
                    .filter(structure -> structure.getBlockPositions().contains(pos))
                    .count();

            // Убираем только те опоры, которые больше не используются никакой оставшейся структурой.
            if (remainingOwners == 0) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                blockToStructure.remove(pos);
            }
        }
    }

    private boolean isSharedSupportBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.OAK_LOG) || level.getBlockState(pos).is(ModBlocks.FOUNDATION_BASE.get());
    }

    /**
     * Получить структуру по ID
     */
    public Structure getStructure(UUID structureId) {
        return structures.get(structureId);
    }

    /**
     * Получить структуру по позиции блока
     */
    public Structure getStructureAtPosition(BlockPos pos) {
        UUID structureId = blockToStructure.get(pos);
        return structureId != null ? structures.get(structureId) : null;
    }

    /**
     * Получить все структуры, принадлежащие игроку
     */
    public List<Structure> getPlayerStructures(UUID playerId) {
        List<Structure> playerStructures = new ArrayList<>();
        for (Structure structure : structures.values()) {
            if (structure.getOwner().equals(playerId)) {
                playerStructures.add(structure);
            }
        }
        return playerStructures;
    }

    /**
     * Получить все структуры в радиусе
     */
    public List<Structure> getStructuresInRadius(BlockPos center, int radius) {
        List<Structure> nearby = new ArrayList<>();
        for (Structure structure : structures.values()) {
            if (center.closerThan(structure.getBasePosition(), radius)) {
                nearby.add(structure);
            }
        }
        return nearby;
    }

    /**
     * Получить общее количество структур
     */
    public int getStructureCount() {
        return structures.size();
    }

    /**
     * Проверить, заняты ли блоки другой структурой
     */
    public boolean areBlocksOccupied(Set<BlockPos> positions) {
        for (BlockPos pos : positions) {
            if (blockToStructure.containsKey(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Обновить текущее здоровье структуры
     */
    public void damageStructure(UUID structureId, double damage) {
        damageStructure(null, structureId, damage);
    }

    public void damageStructure(Level level, UUID structureId, double damage) {
        damageStructure(level, structureId, damage, false);
    }

    public void damageStructure(Level level, UUID structureId, double damage, boolean refreshNearbyFoundations) {
        Structure structure = structures.get(structureId);
        if (structure != null) {
            structure.takeDamage(damage);
            if (structure.isDestroyed()) {
                removeStructure(level, structureId, refreshNearbyFoundations);
            }
            setDirty();
        }
    }

    /**
     * Очистить устаревшие структуры (без шкафа авторизации)
     */
    public void cleanupExpiredStructures() {
        // TODO: Реализовать логику удаления структур без авторизации через 3 дня
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (Structure structure : structures.values()) {
            CompoundTag structureTag = structure.serializeNBT();
            listTag.add(structureTag);
        }
        tag.put("structures", listTag);
        return tag;
    }

    public static StructureManager load(CompoundTag tag) {
        StructureManager manager = new StructureManager();
        ListTag listTag = tag.getList("structures", Tag.TAG_COMPOUND);
        
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag structureTag = listTag.getCompound(i);
            String type = structureTag.getString("type");

            Structure structure = createStructureFromTag(type, structureTag);
            if (structure == null) {
                continue;
            }

            structure.deserializeNBT(structureTag);
            manager.addStructure(structure);
            registerSupportBlocks(manager, structure);
        }

        return manager;
    }

    private static Structure createStructureFromTag(String type, CompoundTag tag) {
        BlockPos basePosition = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        UUID owner = UUID.fromString(tag.getString("owner"));

        return switch (type) {
            case "foundation" -> new FoundationStructure(basePosition, owner);
            case "wall" -> new WallStructure(basePosition, owner, parseOrientation(tag.getString("orientation")));
            case "door_frame" -> new DoorFrameStructure(basePosition, owner, parseOrientation(tag.getString("orientation")));
            case "roof" -> new RoofStructure(basePosition, owner);
            case "roof_hole" -> new RoofHoleStructure(basePosition, owner);
            case "roof_hole_trapdoor" -> new RoofHoleTrapdoorStructure(basePosition, owner);
            case "floor_ladder" -> new FloorLadderStructure(basePosition, owner, parseDirection(tag.getString("facing")), tag.getBoolean("withSupport"));
            case "floor_ladder_no_support" -> new FloorLadderStructure(basePosition, owner, parseDirection(tag.getString("facing")), tag.getBoolean("withSupport"));
            case "wooden_door" -> new DoorStructure(basePosition, owner, DoorStructure.DoorType.WOODEN);
            case "iron_door" -> new DoorStructure(basePosition, owner, DoorStructure.DoorType.IRON);
            case "storage_chest" -> new StorageChestStructure(basePosition, owner);
            case "auth_cabinet" -> new AuthCabinetStructure(basePosition, owner);
            case "campfire" -> new CampfireStructure(basePosition, owner);
            default -> null;
        };
    }

    private static WallStructure.WallOrientation parseOrientation(String value) {
        try {
            return value == null || value.isEmpty() ? WallStructure.WallOrientation.NORTH : WallStructure.WallOrientation.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return WallStructure.WallOrientation.NORTH;
        }
    }

    private static net.minecraft.core.Direction parseDirection(String value) {
        try {
            return value == null || value.isEmpty() ? net.minecraft.core.Direction.NORTH : net.minecraft.core.Direction.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return net.minecraft.core.Direction.NORTH;
        }
    }

    private static void registerSupportBlocks(StructureManager manager, Structure structure) {
        if (structure instanceof FloorLadderStructure floorLadder && floorLadder.isWithSupport()) {
            manager.addBlocksToStructure(structure.getStructureId(), floorLadder.getSupportBlocks());
            return;
        }

        if (structure instanceof WallStructure wall) {
            manager.addBlocksToStructure(structure.getStructureId(), wall.getSupportBlocks());
            return;
        }

        if (structure instanceof DoorFrameStructure doorFrame) {
            manager.addBlocksToStructure(structure.getStructureId(), doorFrame.getSupportBlocks());
            return;
        }

        if (structure instanceof RoofStructure roof) {
            manager.addBlocksToStructure(structure.getStructureId(), roof.getSupportBlocks());
            return;
        }

        if (structure instanceof RoofHoleStructure roofHole) {
            manager.addBlocksToStructure(structure.getStructureId(), roofHole.getSupportBlocks());
        }
    }
}
