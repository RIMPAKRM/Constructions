package com.constructions.structures;

import com.constructions.ConstructionsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/**
 * Менеджер взрывчатки, прикреплённой к структурам
 * Управляет взрывом и уроном по структурам
 */
public class ExplosiveManager {
    private static final ExplosiveManager INSTANCE = new ExplosiveManager();
    
    public enum ExplosiveType {
        WEAK(16.0, "weak_explosive"),
        MEDIUM(35.0, "medium_explosive"),
        STRONG(80.0, "strong_explosive");

        private final double structureDamage;
        private final String itemName;

        ExplosiveType(double structureDamage, String itemName) {
            this.structureDamage = structureDamage;
            this.itemName = itemName;
        }

        public double getStructureDamage() {
            return structureDamage;
        }

        public double getPlayerDamage() {
            return switch (this) {
                case WEAK -> ConstructionsConfig.Server.WEAK_EXPLOSIVE_DAMAGE_VALUE != null
                        ? ConstructionsConfig.Server.WEAK_EXPLOSIVE_DAMAGE_VALUE.get()
                        : ConstructionsConfig.Server.WEAK_EXPLOSIVE_DAMAGE;
                case MEDIUM -> ConstructionsConfig.Server.MEDIUM_EXPLOSIVE_DAMAGE_VALUE != null
                        ? ConstructionsConfig.Server.MEDIUM_EXPLOSIVE_DAMAGE_VALUE.get()
                        : ConstructionsConfig.Server.MEDIUM_EXPLOSIVE_DAMAGE;
                case STRONG -> ConstructionsConfig.Server.STRONG_EXPLOSIVE_DAMAGE_VALUE != null
                        ? ConstructionsConfig.Server.STRONG_EXPLOSIVE_DAMAGE_VALUE.get()
                        : ConstructionsConfig.Server.STRONG_EXPLOSIVE_DAMAGE;
            };
        }

        public String getItemName() {
            return itemName;
        }
    }

    private static final int EXPLOSION_DAMAGE_RADIUS = 5;
    private static final int EXPLOSION_POWER = 2;
    private Map<UUID, List<BlockPos>> explosivesOnStructures = new HashMap<>();

    public static ExplosiveManager getInstance() {
        return INSTANCE;
    }

    /**
     * Добавить взрывчатку на структуру
     */
    public void attachExplosive(UUID structureId, BlockPos pos, ExplosiveType type) {
        explosivesOnStructures.computeIfAbsent(structureId, k -> new ArrayList<>()).add(pos);
    }

    /**
     * Получить все взрывчатки на структуре
     */
    public List<BlockPos> getExplosivesOnStructure(UUID structureId) {
        return explosivesOnStructures.getOrDefault(structureId, new ArrayList<>());
    }

    /**
     * Взорвать взрывчатку на структуре
     */
    public void detonateExplosive(Level level, UUID structureId, BlockPos explosivePos, 
                                   ExplosiveType explosiveType, StructureManager structureManager) {
        Structure structure = structureManager.getStructure(structureId);
        if (structure == null) {
            return;
        }

        Set<BlockPos> structureBlocks = collectDestroyedBlockPositions(structure);

        // Взрывчатка не должна ваншотить цель с полного HP, но должна уверенно сносить её за несколько подрывов.
        double baseDamage = explosiveType.getStructureDamage();
        double currentHealth = structure.getCurrentHealth();
        double damage = baseDamage;

        if (currentHealth >= structure.getMaxHealth() && baseDamage >= currentHealth) {
            damage = Math.max(1.0, currentHealth - 1.0);
        }

        structureManager.damageStructure(level, structureId, damage, false);

        boolean destroyed = structureManager.getStructure(structureId) == null;
        if (destroyed && level instanceof ServerLevel serverLevel) {
            for (BlockPos structureBlockPos : structureBlocks) {
                serverLevel.setBlockAndUpdate(structureBlockPos, Blocks.AIR.defaultBlockState());
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            // Взрыв без разрушения блоков мира: сначала урон по HP, блоки удаляются только при 0 HP.
            serverLevel.explode(null, explosivePos.getX() + 0.5, explosivePos.getY() + 0.5, explosivePos.getZ() + 0.5,
                    EXPLOSION_POWER, Level.ExplosionInteraction.NONE);
        }

        // Удалить взрывчатку со структуры
        List<BlockPos> explosives = explosivesOnStructures.get(structureId);
        if (explosives != null) {
            explosives.remove(explosivePos);
        }
    }

    private Set<BlockPos> collectDestroyedBlockPositions(Structure structure) {
        Set<BlockPos> positions = new HashSet<>(structure.getBlockPositions());

        if (structure instanceof WallStructure wall) {
            positions.addAll(wall.getSupportBlocks());
        } else if (structure instanceof DoorFrameStructure doorFrame) {
            positions.addAll(doorFrame.getSupportBlocks());
        } else if (structure instanceof RoofStructure roof) {
            positions.addAll(roof.getSupportBlocks());
        } else if (structure instanceof RoofHoleStructure roofHole) {
            positions.addAll(roofHole.getSupportBlocks());
        } else if (structure instanceof FloorLadderStructure floorLadder && floorLadder.isWithSupport()) {
            positions.addAll(floorLadder.getSupportBlocks());
        }

        return positions;
    }

    /**
     * Проверить, получает ли игрок урон от взрыва
     */
    public boolean shouldPlayerTakeDamage(Player player, BlockPos explosivePos) {
        // Если игрок дальше чем EXPLOSION_DAMAGE_RADIUS, он не получает урон
        if (explosivePos.closerThan(player.blockPosition(), EXPLOSION_DAMAGE_RADIUS)) {
            // Проверить, защищён ли игрок стеной (простая проверка с луча)
            // TODO: Реализовать более сложную проверку линии видимости
            return true;
        }
        return false;
    }

    /**
     * Применить урон игроку от взрыва
     */
    public void damagePlayer(Player player, ExplosiveType explosiveType) {
        float damage = (float) explosiveType.getPlayerDamage();
        player.hurt(player.level().damageSources().explosion(null), damage);
    }
}
