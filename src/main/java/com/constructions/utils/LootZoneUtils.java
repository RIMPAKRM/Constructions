package com.constructions.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Утилиты для работы с зонами лута
 * Проверяет расстояния от зон лута из мода spawnchest
 */
public class LootZoneUtils {
    private static final int MIN_DISTANCE_FROM_LOOT = 100;

    /**
     * Проверить, находится ли позиция на допустимом расстоянии от зон лута
     * 
     * @param level уровень (мир)
     * @param pos позиция для проверки
     * @param playerId ID игрока (владельца)
     * @return true если позиция достаточно далеко от лута, false иначе
     */
    public static boolean isValidBuildPosition(Level level, BlockPos pos, UUID playerId) {
        // TODO: Интегрировать с модом spawnchest для проверки зон лута
        // Для теста: просто возвращаем true
        // В реальности нужно:
        // 1. Получить список всех зон лута из spawnchest
        // 2. Проверить расстояние от каждой зоны
        // 3. Вернуть false если хотя бы одна зона ближе, чем MIN_DISTANCE_FROM_LOOT
        
        return true;
    }

    /**
     * Получить расстояние до ближайшей зоны лута
     * 
     * @param level уровень
     * @param pos позиция
     * @return расстояние в блоках, или -1 если зон лута нет
     */
    public static int getDistanceToNearestLootZone(Level level, BlockPos pos) {
        // TODO: Реализовать интеграцию с spawnchest
        return -1;
    }

    /**
     * Сообщить ошибку игроку о том, почему позиция невалидна
     */
    public static String getReasonForInvalidPosition(Level level, BlockPos pos, UUID playerId) {
        int distance = getDistanceToNearestLootZone(level, pos);
        if (distance != -1 && distance < MIN_DISTANCE_FROM_LOOT) {
            return "§cНельзя строить так близко к луту! Минимальное расстояние: " + MIN_DISTANCE_FROM_LOOT + " блоков (текущее: " + distance + ")";
        }
        return null;
    }
}
