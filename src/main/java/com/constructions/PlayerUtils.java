package com.constructions;

import java.util.*;

/**
 * Утилиты для работы с UUID и игроком
 */
public class PlayerUtils {
    
    /**
     * Проверить, является ли игрок владельцем структуры
     */
    public static boolean isStructureOwner(UUID playerId, UUID structureOwnerId) {
        return playerId.equals(structureOwnerId);
    }

    /**
     * Проверить, авторизован ли игрок в шкафу авторизации
     */
    public static boolean isPlayerAuthorized(UUID playerId, Set<UUID> authorizedPlayers) {
        return authorizedPlayers.contains(playerId);
    }

    /**
     * Получить имя игрока (заглушка для будущей реализации)
     */
    public static String getPlayerName(UUID playerId) {
        // TODO: Получить имя игрока из профиля
        return playerId.toString();
    }
}
