package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Управляет активными рейд-блоками после взрыва структур.
 */
public class RaidBlockManager {
    private static final RaidBlockManager INSTANCE = new RaidBlockManager();
    private static final int RAID_RADIUS = 30;
    private static final int RAID_DURATION_TICKS = 20 * 60 * 2;
    private static final double RAID_RADIUS_SQUARED = RAID_RADIUS * RAID_RADIUS;

    private final List<RaidZone> activeRaids = new ArrayList<>();

    public static RaidBlockManager getInstance() {
        return INSTANCE;
    }

    public void startRaidBlock(ServerLevel level, BlockPos center) {
        if (level == null || center == null) {
            return;
        }

        RaidZone existing = findMergeTarget(level, center);
        if (existing != null) {
            existing.extend(RAID_DURATION_TICKS);
            return;
        }

        activeRaids.add(new RaidZone(level.dimension(), center.immutable(), RAID_DURATION_TICKS));
    }

    public void tick(ServerLevel level) {
        if (level == null) {
            return;
        }

        Iterator<RaidZone> iterator = activeRaids.iterator();
        while (iterator.hasNext()) {
            RaidZone raidZone = iterator.next();
            if (!raidZone.dimension.equals(level.dimension())) {
                continue;
            }

            raidZone.tick(level);
            if (raidZone.isExpired()) {
                raidZone.dispose();
                iterator.remove();
            }
        }
    }

    public boolean isBuildBlocked(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null) {
            return false;
        }

        for (RaidZone raidZone : activeRaids) {
            if (!raidZone.dimension.equals(serverLevel.dimension())) {
                continue;
            }
            if (raidZone.contains(pos)) {
                return true;
            }
        }

        return false;
    }

    private RaidZone findMergeTarget(ServerLevel level, BlockPos center) {
        for (RaidZone raidZone : activeRaids) {
            if (!raidZone.dimension.equals(level.dimension())) {
                continue;
            }
            if (raidZone.centerCloserThan(center, RAID_RADIUS)) {
                return raidZone;
            }
        }

        return null;
    }

    private static String formatTime(int ticksRemaining) {
        int totalSeconds = Math.max(0, ticksRemaining) / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static final class RaidZone {
        private final net.minecraft.resources.ResourceKey<Level> dimension;
        private final BlockPos center;
        private final ServerBossEvent bossBar;
        private int ticksRemaining;
        private int lastDisplayedSeconds = -1;

        private RaidZone(net.minecraft.resources.ResourceKey<Level> dimension, BlockPos center, int ticksRemaining) {
            this.dimension = dimension;
            this.center = center;
            this.ticksRemaining = ticksRemaining;
            this.bossBar = new ServerBossEvent(
                    Component.literal("Рейд-блок: " + formatTime(ticksRemaining)),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.PROGRESS
            );
            this.bossBar.setVisible(true);
        }

        private void extend(int additionalTicks) {
            this.ticksRemaining = Math.min(RAID_DURATION_TICKS, this.ticksRemaining + additionalTicks);
        }

        private void tick(ServerLevel level) {
            if (ticksRemaining > 0) {
                ticksRemaining--;
            }

            bossBar.setProgress(Math.max(0.0F, ticksRemaining / (float) RAID_DURATION_TICKS));

            int secondsRemaining = Math.max(0, ticksRemaining) / 20;
            if (secondsRemaining != lastDisplayedSeconds) {
                bossBar.setName(Component.literal("Рейд-блок: " + formatTime(ticksRemaining)));
                lastDisplayedSeconds = secondsRemaining;
            }

            for (ServerPlayer player : level.players()) {
                boolean inRange = contains(player.blockPosition());
                if (inRange) {
                    bossBar.addPlayer(player);
                } else {
                    bossBar.removePlayer(player);
                }
            }
        }

        private boolean contains(BlockPos pos) {
            return distanceSquared(pos, center) <= RAID_RADIUS_SQUARED;
        }

        private boolean centerCloserThan(BlockPos otherCenter, int radius) {
            double radiusSquared = radius * radius;
            return distanceSquared(otherCenter, center) <= radiusSquared;
        }

        private boolean isExpired() {
            return ticksRemaining <= 0;
        }

        private void dispose() {
            bossBar.removeAllPlayers();
        }

        private static double distanceSquared(BlockPos a, BlockPos b) {
            double dx = (a.getX() + 0.5) - (b.getX() + 0.5);
            double dy = (a.getY() + 0.5) - (b.getY() + 0.5);
            double dz = (a.getZ() + 0.5) - (b.getZ() + 0.5);
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
