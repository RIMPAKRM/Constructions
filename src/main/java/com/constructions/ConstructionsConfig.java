package com.constructions;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Конфигурация мода Constructions
 * Хранит все настройки системы строительства и механики базы
 */
@Mod.EventBusSubscriber(modid = ConstructionsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConstructionsConfig {

    public static class Server {
        // Расстояние от зон лута, на котором нельзя ставить базы
        public static int MIN_DISTANCE_FROM_LOOT = 100;
        
        // Времена жизни строений без шкафа авторизации (в часах)
        public static int UNCLAIMED_STRUCTURE_LIFETIME_HOURS = 72;
        public static int CHEST_LIFETIME_HOURS = 120;
        public static int CAMPFIRE_LIFETIME_HOURS = 2;
        
        // Радиус действия шкафа авторизации
        public static int AUTH_CABINET_RADIUS = 20;
        
        // Максимальная высота фундамента (в блоках)
        public static int MAX_FOUNDATION_HEIGHT = 3;
        
        // Урон взрывчатки
        public static double WEAK_EXPLOSIVE_DAMAGE = 2.0;
        public static double MEDIUM_EXPLOSIVE_DAMAGE = 5.0;
        public static double STRONG_EXPLOSIVE_DAMAGE = 10.0;
        
        // Расстояние до взрыва для получения урона
        public static int EXPLOSION_DAMAGE_RADIUS = 5;
        
        // Конфиг спецификации для доступа к значениям
        public static ForgeConfigSpec.IntValue MIN_DISTANCE_FROM_LOOT_VALUE;
        public static ForgeConfigSpec.IntValue MAX_FOUNDATION_HEIGHT_VALUE;
        public static ForgeConfigSpec.IntValue UNCLAIMED_STRUCTURE_LIFETIME_VALUE;
        public static ForgeConfigSpec.IntValue CHEST_LIFETIME_VALUE;
        public static ForgeConfigSpec.IntValue CAMPFIRE_LIFETIME_VALUE;
        public static ForgeConfigSpec.IntValue AUTH_CABINET_RADIUS_VALUE;
        public static ForgeConfigSpec.DoubleValue WEAK_EXPLOSIVE_DAMAGE_VALUE;
        public static ForgeConfigSpec.DoubleValue MEDIUM_EXPLOSIVE_DAMAGE_VALUE;
        public static ForgeConfigSpec.DoubleValue STRONG_EXPLOSIVE_DAMAGE_VALUE;
        public static ForgeConfigSpec.IntValue EXPLOSION_DAMAGE_RADIUS_VALUE;
    }

    public static class Common {
        // Общие настройки видимости и превью
        public static boolean ENABLE_HOLOGRAM_PREVIEW = true;
        public static int PREVIEW_BLOCK_HEIGHT = 1;
        
        public static ForgeConfigSpec.BooleanValue ENABLE_HOLOGRAM_PREVIEW_VALUE;
        public static ForgeConfigSpec.IntValue PREVIEW_BLOCK_HEIGHT_VALUE;
    }

    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        // Конфигурация сервера
        SERVER_BUILDER.comment("Server-side settings for Constructions mod");
        
        SERVER_BUILDER.push("building");
        Server.MIN_DISTANCE_FROM_LOOT_VALUE = SERVER_BUILDER
                .comment("Minimum distance from loot zones where structures cannot be placed (blocks)")
                .defineInRange("minDistanceFromLoot", 100, 0, Integer.MAX_VALUE);
        Server.MAX_FOUNDATION_HEIGHT_VALUE = SERVER_BUILDER
                .comment("Maximum height of foundation pillars (blocks)")
                .defineInRange("maxFoundationHeight", 3, 1, 10);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("structures");
        Server.UNCLAIMED_STRUCTURE_LIFETIME_VALUE = SERVER_BUILDER
                .comment("Lifetime of structures without auth cabinet (hours)")
                .defineInRange("unclaimedStructureLifetime", 72, 1, Integer.MAX_VALUE);
        Server.CHEST_LIFETIME_VALUE = SERVER_BUILDER
                .comment("Lifetime of chests without auth cabinet (hours)")
                .defineInRange("chestLifetime", 120, 1, Integer.MAX_VALUE);
        Server.CAMPFIRE_LIFETIME_VALUE = SERVER_BUILDER
                .comment("Lifetime of campfires without auth cabinet (hours)")
                .defineInRange("campfireLifetime", 2, 1, 24);
        Server.AUTH_CABINET_RADIUS_VALUE = SERVER_BUILDER
                .comment("Radius of auth cabinet influence (blocks)")
                .defineInRange("authCabinetRadius", 20, 10, 100);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("explosives");
        Server.WEAK_EXPLOSIVE_DAMAGE_VALUE = SERVER_BUILDER
                .comment("Damage of weak homemade explosive")
                .defineInRange("weakExplosiveDamage", 2.0, 0.1, 20.0);
        Server.MEDIUM_EXPLOSIVE_DAMAGE_VALUE = SERVER_BUILDER
                .comment("Damage of medium homemade explosive")
                .defineInRange("mediumExplosiveDamage", 5.0, 0.1, 30.0);
        Server.STRONG_EXPLOSIVE_DAMAGE_VALUE = SERVER_BUILDER
                .comment("Damage of military explosive")
                .defineInRange("strongExplosiveDamage", 10.0, 0.1, 50.0);
        Server.EXPLOSION_DAMAGE_RADIUS_VALUE = SERVER_BUILDER
                .comment("Radius for explosion damage to player (blocks)")
                .defineInRange("explosionDamageRadius", 5, 1, 20);
        SERVER_BUILDER.pop();

        // Конфигурация для клиента
        COMMON_BUILDER.comment("Common settings for Constructions mod");
        
        COMMON_BUILDER.push("client");
        Common.ENABLE_HOLOGRAM_PREVIEW_VALUE = COMMON_BUILDER
                .comment("Enable hologram preview when placing structures")
                .define("enableHologramPreview", true);
        Common.PREVIEW_BLOCK_HEIGHT_VALUE = COMMON_BUILDER
                .comment("Height of preview blocks above ground")
                .defineInRange("previewBlockHeight", 1, 0, 5);
        COMMON_BUILDER.pop();

        SERVER_SPEC = SERVER_BUILDER.build();
        COMMON_SPEC = COMMON_BUILDER.build();
    }

    @Mod.EventBusSubscriber(modid = ConstructionsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ServerConfig {
                // Конфигурационные значения используются через ForgeConfigSpec напрямую.
    }
}
