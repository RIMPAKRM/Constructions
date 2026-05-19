package com.constructions.utils;

import com.constructions.blocks.ModBlocks;
import com.constructions.structures.AuthCabinetStructure;
import com.constructions.structures.CampfireStructure;
import com.constructions.structures.DoorFrameStructure;
import com.constructions.structures.DoorStructure;
import com.constructions.structures.FoundationStructure;
import com.constructions.structures.HalfWallStructure;
import com.constructions.structures.FloorLadderStructure;
import com.constructions.structures.RampStructure;
import com.constructions.structures.RoofHoleStructure;
import com.constructions.structures.RoofHoleTrapdoorStructure;
import com.constructions.structures.RoofStructure;
import com.constructions.structures.StorageChestStructure;
import com.constructions.structures.Structure;
import com.constructions.structures.StructureManager;
import com.constructions.structures.WallStructure;
import com.constructions.structures.WindowFrameStructure;
import com.constructions.structures.WindowGrilleStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.constructions.ConstructionsConfig;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Утилиты для создания и визуализации структур при размещении.
 */
public final class StructurePlacementUtils {
    private static final int MAX_ALLOWED_STRUCTURE_OVERLAP = 8;

    private StructurePlacementUtils() {
    }

    public static Structure createStructure(String structureType, BlockPos basePosition, UUID owner, float yaw) {
        return createStructure(structureType, basePosition, owner, yaw, Direction.NORTH, null);
    }

    public static Structure createStructure(String structureType, BlockPos basePosition, UUID owner, float yaw, Direction face, Level level) {
        BlockPos resolvedBasePosition = resolvePlacementBasePosition(structureType, basePosition, face, yaw, level);

        return switch (structureType) {
            case "foundation" -> createFoundationStructure(resolvedBasePosition, owner, level);
            case "wall" -> new WallStructure(resolvedBasePosition, owner, orientationFromFace(resolveWallFace(level, basePosition, face, yaw)));
            case "door_frame" -> new DoorFrameStructure(resolvedBasePosition, owner, orientationFromFace(resolveWallFace(level, basePosition, face, yaw)));
            case "window_frame" -> new WindowFrameStructure(resolvedBasePosition, owner, orientationFromFace(resolveWallFace(level, basePosition, face, yaw)));
            case "half_wall" -> new HalfWallStructure(resolvedBasePosition, owner, orientationFromFace(resolveWallFace(level, basePosition, face, yaw)));
            case "roof" -> new RoofStructure(resolvedBasePosition, owner);
            case "roof_hole" -> new RoofHoleStructure(resolvedBasePosition, owner);
            case "roof_hole_trapdoor" -> new RoofHoleTrapdoorStructure(resolvedBasePosition, owner);
            case "floor_ladder" -> new FloorLadderStructure(resolvedBasePosition, owner, Direction.fromYRot(yaw), true);
            case "floor_ladder_no_support" -> new FloorLadderStructure(resolvedBasePosition, owner, Direction.fromYRot(yaw), false);
            case "wooden_door" -> new DoorStructure(resolvedBasePosition, owner, DoorStructure.DoorType.WOODEN);
            case "iron_door" -> new DoorStructure(resolvedBasePosition, owner, DoorStructure.DoorType.IRON);
            case "storage_chest" -> new StorageChestStructure(resolvedBasePosition, owner);
            case "window_grille" -> new WindowGrilleStructure(resolvedBasePosition, owner);
            case "ramp" -> new RampStructure(resolvedBasePosition, owner, resolveRampFace(level, basePosition, face, yaw).getOpposite());
            case "auth_cabinet" -> new AuthCabinetStructure(resolvedBasePosition, owner);
            case "campfire" -> new CampfireStructure(resolvedBasePosition, owner);
            default -> null;
        };
    }

    public static Block getPlacementBlock(String structureType) {
        return switch (structureType) {
            case "foundation" -> ModBlocks.FOUNDATION_BASE.get();
            case "wooden_door", "iron_door" -> ModBlocks.DOOR_BLOCK.get();
            case "roof_hole_trapdoor" -> Blocks.OAK_TRAPDOOR;
            case "storage_chest" -> Blocks.BARREL;
            case "window_grille" -> Blocks.IRON_BARS;
            case "auth_cabinet" -> Blocks.LECTERN;
            case "floor_ladder", "floor_ladder_no_support" -> Blocks.LADDER;
            default -> ModBlocks.STRUCTURE_BLOCK.get();
        };
    }

    /**
     * Проверить, может ли игрок строить на позиции с учётом ближайшего шкафа авторизации.
     * - Если рядом нет шкафа (в радиусе из конфига) — разрешено.
     * - Если рядом есть АКТИВНЫЙ шкаф — только авторизованные игроки (или владелец) могут строить.
     * - Если рядом есть НЕАКТИВНЫЙ шкаф — никто не может строить.
     */
    public static boolean isPlayerAuthorizedForBuild(Level level, ServerPlayer player, BlockPos pos) {
        if (player == null || !(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        StructureManager manager = StructureManager.get(serverLevel);
        int radius = ConstructionsConfig.Server.AUTH_CABINET_RADIUS;
        List<Structure> nearby = manager.getStructuresInRadius(pos, radius);
        AuthCabinetStructure nearest = null;
        double bestDist = Double.MAX_VALUE;
        for (Structure s : nearby) {
            if (s instanceof AuthCabinetStructure cab) {
                double d = s.getBasePosition().distToCenterSqr(pos.getX(), pos.getY(), pos.getZ());
                if (d < bestDist) {
                    bestDist = d;
                    nearest = cab;
                }
            }
        }

        if (nearest == null) {
            return true;
        }

        // Если шкаф неактивен - доступ запрещён
        if (!nearest.isActive()) {
            return false;
        }

        return nearest.isPlayerAuthorized(player.getUUID());
    }

    public static WallStructure.WallOrientation toOrientation(float yaw) {
        Direction direction = Direction.fromYRot(yaw);
        return switch (direction) {
            case NORTH -> WallStructure.WallOrientation.NORTH;
            case SOUTH -> WallStructure.WallOrientation.SOUTH;
            case EAST -> WallStructure.WallOrientation.EAST;
            case WEST -> WallStructure.WallOrientation.WEST;
            default -> WallStructure.WallOrientation.NORTH;
        };
    }

    public static WallStructure.WallOrientation orientationFromFace(Direction face) {
        return switch (face) {
            case NORTH -> WallStructure.WallOrientation.NORTH;
            case SOUTH -> WallStructure.WallOrientation.SOUTH;
            case EAST -> WallStructure.WallOrientation.EAST;
            case WEST -> WallStructure.WallOrientation.WEST;
            default -> WallStructure.WallOrientation.NORTH;
        };
    }

    private static BlockPos resolvePlacementBasePosition(String structureType, BlockPos clickedPos, Direction face, float yaw, Level level) {
        if (structureType.equals("wall") || structureType.equals("door_frame")
                || structureType.equals("window_frame") || structureType.equals("half_wall")) {
            return resolveWallAnchor(level, clickedPos, face, yaw);
        }

        if (structureType.equals("foundation")) {
            return resolveFoundationAnchor(level, clickedPos, face, yaw);
        }

        if (structureType.equals("roof") || structureType.equals("roof_hole")) {
            return resolveRoofAnchor(level, clickedPos, face, yaw);
        }

        if (structureType.equals("roof_hole_trapdoor")) {
            return resolveRoofHoleTrapdoorAnchor(level, clickedPos, face, yaw);
        }

        if (structureType.equals("window_grille")) {
            return resolveWindowGrilleAnchor(level, clickedPos, face, yaw);
        }

        if (structureType.equals("ramp")) {
            return resolveRampAnchor(level, clickedPos, face, yaw);
        }

        if (structureType.equals("floor_ladder") || structureType.equals("floor_ladder_no_support")) {
            return resolveFloorLadderAnchor(level, clickedPos, face, yaw);
        }

        return clickedPos.relative(face);
    }

    private static FoundationStructure createFoundationStructure(BlockPos basePosition, UUID owner, Level level) {
        FoundationStructure foundation = new FoundationStructure(basePosition, owner);
        if (level == null) {
            return foundation;
        }

        int[] supportDepths = computeFoundationSupportDepths(level, basePosition);
        for (int i = 0; i < supportDepths.length; i++) {
            if (supportDepths[i] < 0) {
                supportDepths[i] = 0;
            }
        }
        foundation.setPillarHeights(supportDepths);
        return foundation;
    }

    public static List<BlockPos> getPreviewBlocks(Level level, String structureType, BlockPos basePosition, UUID owner, float yaw) {
        return getPreviewBlocks(level, structureType, basePosition, Direction.NORTH, owner, yaw);
    }

    public static List<BlockPos> getPreviewBlocks(Level level, String structureType, BlockPos basePosition, Direction face, UUID owner, float yaw) {
        return getPlacementPreview(level, structureType, basePosition, face, owner, yaw).blocks();
    }

    public static PlacementPreview getPlacementPreview(Level level, String structureType, BlockPos basePosition, Direction face, UUID owner, float yaw) {
        Structure structure = createStructure(structureType, basePosition, owner, yaw, face, level);
        if (structure == null) {
            return new PlacementPreview(List.of(), false);
        }

        boolean canPlace = canPlaceStructure(level, structure);
        if (canPlace && structure instanceof FoundationStructure) {
            canPlace = !hasBlockingFoundationOverlapPreview(level, structure);
        }
        
        List<BlockPos> previewBlocks = new ArrayList<>(structure.getBlockPositions());
        
        // Добавляем опорные блоки в превью для структур их имеющих
        if (structure instanceof FloorLadderStructure) {
            FloorLadderStructure floorLadder = (FloorLadderStructure) structure;
            if (floorLadder.isWithSupport()) {
                previewBlocks.addAll(floorLadder.getSupportBlocks());
            }
        } else if (structure instanceof WallStructure) {
            WallStructure wall = (WallStructure) structure;
            previewBlocks.addAll(wall.getSupportBlocks());
        } else if (structure instanceof DoorFrameStructure) {
            DoorFrameStructure doorFrame = (DoorFrameStructure) structure;
            previewBlocks.addAll(doorFrame.getSupportBlocks());
        } else if (structure instanceof WindowFrameStructure) {
            WindowFrameStructure windowFrame = (WindowFrameStructure) structure;
            previewBlocks.addAll(windowFrame.getSupportBlocks());
        } else if (structure instanceof HalfWallStructure) {
            HalfWallStructure halfWall = (HalfWallStructure) structure;
            previewBlocks.addAll(halfWall.getSupportBlocks());
        } else if (structure instanceof RoofStructure) {
            RoofStructure roof = (RoofStructure) structure;
            previewBlocks.addAll(roof.getSupportBlocks());
        } else if (structure instanceof RoofHoleStructure) {
            RoofHoleStructure roofHole = (RoofHoleStructure) structure;
            previewBlocks.addAll(roofHole.getSupportBlocks());
        }

        return new PlacementPreview(List.copyOf(previewBlocks), canPlace);
    }

    public static boolean placeStructure(ServerLevel level, ServerPlayer player, BlockPos basePosition, String structureType, float yaw, Direction face, InteractionHand hand) {
        // Проверка авторизации в шкафе авторизации
        if (!isPlayerAuthorizedForBuild(level, player, basePosition)) {
            if (player != null) {
                player.sendSystemMessage(Component.literal("§cВы не авторизированы в шкафе авторизации в этой зоне!"));
            }
            return false;
        }

        Structure structure = createStructure(structureType, basePosition, player.getUUID(), yaw, face, level);
        if (structure == null) {
            if (player != null) {
                player.sendSystemMessage(Component.literal("§cНе удалось создать структуру: неизвестный тип."));
            }
            return false;
        }

        String placementReason = getPlacementFailureReason(level, structure, structureType);
        if (placementReason != null) {
            if (player != null) {
                player.sendSystemMessage(Component.literal(placementReason));
            }
            return false;
        }

        StructureManager manager = StructureManager.get(level);
        if (hasBlockingOverlap(manager, structure)) {
            if (player != null) {
                player.sendSystemMessage(Component.literal("§cНе удалось установить: место уже занято другой стеной, её опорой или другой структурой."));
            }
            return false;
        }

        manager.addStructure(structure);

        // Регистрируем опорные блоки для структур их имеющих
        Set<BlockPos> supportPositions = new HashSet<>();
        
        if (structure instanceof FloorLadderStructure) {
            FloorLadderStructure floorLadder = (FloorLadderStructure) structure;
            if (floorLadder.isWithSupport()) {
                supportPositions.addAll(floorLadder.getSupportBlocks());
            }
        } else if (structure instanceof WallStructure) {
            WallStructure wall = (WallStructure) structure;
            supportPositions.addAll(wall.getSupportBlocks());
        } else if (structure instanceof DoorFrameStructure) {
            DoorFrameStructure doorFrame = (DoorFrameStructure) structure;
            supportPositions.addAll(doorFrame.getSupportBlocks());
        } else if (structure instanceof WindowFrameStructure) {
            WindowFrameStructure windowFrame = (WindowFrameStructure) structure;
            supportPositions.addAll(windowFrame.getSupportBlocks());
        } else if (structure instanceof HalfWallStructure) {
            HalfWallStructure halfWall = (HalfWallStructure) structure;
            supportPositions.addAll(halfWall.getSupportBlocks());
        } else if (structure instanceof RoofStructure) {
            RoofStructure roof = (RoofStructure) structure;
            supportPositions.addAll(roof.getSupportBlocks());
        } else if (structure instanceof RoofHoleStructure) {
            RoofHoleStructure roofHole = (RoofHoleStructure) structure;
            supportPositions.addAll(roofHole.getSupportBlocks());
        }
        
        // Фильтруем позиции опор: исключаем те, которые занимают другие структуры или содержат существующие блоки
        Set<BlockPos> positionsToRegister = new HashSet<>();
        for (BlockPos supportPos : supportPositions) {
            Structure occupying = manager.getStructureAtPosition(supportPos);
            if (occupying != null && !occupying.getStructureId().equals(structure.getStructureId())) {
                // Позиция занята другой структурой
                continue;
            }

            BlockState supportState = level.getBlockState(supportPos);
            if (isFoundationBlock(supportState) || supportState.is(Blocks.OAK_LOG)) {
                // Позиция уже содержит фундамент или бревно
                continue;
            }

            positionsToRegister.add(supportPos);
        }

        if (!positionsToRegister.isEmpty()) {
            manager.addBlocksToStructure(structure.getStructureId(), positionsToRegister);
        }

        if (!placeStructureBlocks(level, player, structure, structureType)) {
            manager.removeStructure(structure.getStructureId());
            return false;
        }

        if (player != null && !player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }

        return true;
    }

    private static String getPlacementFailureReason(Level level, Structure structure, String structureType) {
        if (!hasAllowedWorldOccupancy(level, structure)) {
            return "§cНе удалось установить " + getStructureName(structureType) + ": на месте есть блок, который нельзя заменить.";
        }

        if (structure instanceof FoundationStructure) {
            if (!hasFoundationSupport(level, structure)) {
                return "§cНе удалось установить фундамент: нет подходящей опоры под ним.";
            }
            return null;
        }

        if (isWallLikeStructure(structure)) {
            if (!hasFoundationEdgeSupport(level, structure)) {
                return "§cНе удалось установить " + getStructureName(structureType) + ": опора должна стоять на краю фундамента или на допустимом шве между фундаментами.";
            }
            return null;
        }

        if (structure instanceof RoofStructure || structure instanceof RoofHoleStructure) {
            if (!hasRoofWallSupport(level, structure)) {
                return "§cНе удалось установить " + getStructureName(structureType) + ": снизу должны быть стены или проём.";
            }
            return null;
        }

        if (structure instanceof FloorLadderStructure floorLadder) {
            if (floorLadder.isWithSupport()) {
                if (!hasRoofHoleAbove(level, structure)) {
                    return "§cНе удалось установить лестницу: сверху нужен проём в крыше.";
                }
            } else {
                if (!hasFoundationOrRoofSupportForLadder(level, structure.getBasePosition())) {
                    return "§cНе удалось установить лестницу без опоры: она должна стоять на фундаменте или крыше.";
                }
            }
            return null;
        }

        if (structure instanceof RoofHoleTrapdoorStructure) {
            if (!hasRoofHoleBelow(level, structure)) {
                return "§cНе удалось установить люк: он должен ставиться над лестницей в проёме крыши или в оконном проёме.";
            }
            return null;
        }

        if (structure instanceof WindowGrilleStructure) {
            if (!isWindowOpening(level, structure.getBasePosition())) {
                return "§cНе удалось установить решётку: она должна ставиться в оконный проём.";
            }
            return null;
        }

        if (structure instanceof RampStructure rampStructure) {
            if (!hasRampSupport(level, rampStructure)) {
                return "§cНе удалось установить рампу: она должна крепиться к внешней стороне фундамента.";
            }
            return null;
        }

        // При попытке установить шкаф авторизации — не должно быть другого шкафа в радиусе
        if (structure instanceof AuthCabinetStructure) {
            if (level instanceof ServerLevel serverLevel) {
                StructureManager manager = StructureManager.get(serverLevel);
                int radius = ConstructionsConfig.Server.AUTH_CABINET_RADIUS;
                List<Structure> nearby = manager.getStructuresInRadius(structure.getBasePosition(), radius);
                for (Structure s : nearby) {
                    if (s instanceof AuthCabinetStructure) {
                        return "§cНе удалось установить шкаф авторизации: рядом уже есть другой шкаф в " + radius + " блоков.";
                    }
                }
            }
            // Шкаф авторизации нельзя ставить в воздухе или без твёрдой опоры под ним
            if (!hasSolidSupport(level, structure.getBasePosition())) {
                return "§cНе удалось установить шкаф авторизации: требуется твёрдая опора под шкафом.";
            }
            return null;
        }

        if (structure instanceof DoorStructure) {
            if (!hasDoorSupport(level, structure)) {
                return "§cНе удалось установить дверь: нужен проём из рамки и опора снизу.";
            }
            return null;
        }

        if (!hasSolidSupport(level, structure.getBasePosition())) {
            return "§cНе удалось установить " + getStructureName(structureType) + ": нет подходящей опоры.";
        }

        return null;
    }

    private static String getStructureName(String structureType) {
        if (structureType == null) {
            return "структуру";
        }

        return switch (structureType) {
            case "wall" -> "стену";
            case "door_frame" -> "дверной проём";
            case "window_frame" -> "оконный проём";
            case "half_wall" -> "полустену";
            case "roof", "roof_hole" -> "крышу";
            case "roof_hole_trapdoor" -> "люк";
            case "floor_ladder", "floor_ladder_no_support" -> "лестницу";
            case "foundation" -> "фундамент";
            case "door" -> "дверь";
            case "window_grille" -> "решётку";
            case "ramp" -> "рампу";
            default -> "структуру";
        };
    }

    public static boolean canPlaceStructure(Level level, Structure structure) {
        if (structure == null) {
            return false;
        }

        if (!hasAllowedWorldOccupancy(level, structure)) {
            return false;
        }

        if (structure instanceof FoundationStructure) {
            return hasFoundationSupport(level, structure);
        }

        if (isWallLikeStructure(structure)) {
            return hasFoundationEdgeSupport(level, structure);
        }

        if (structure instanceof RoofStructure || structure instanceof RoofHoleStructure) {
            return hasRoofWallSupport(level, structure);
        }

        if (structure instanceof FloorLadderStructure) {
            FloorLadderStructure floorLadder = (FloorLadderStructure) structure;
            // Лестница с опорами требует крышу с отверстием
            // Лестница без опор требует фундамент или крышу под собой
            if (floorLadder.isWithSupport()) {
                return hasRoofHoleAbove(level, structure);
            } else {
                return hasFoundationOrRoofSupportForLadder(level, structure.getBasePosition());
            }
        }

        if (structure instanceof RoofHoleTrapdoorStructure) {
            return hasRoofHoleBelow(level, structure);
        }

        if (structure instanceof WindowGrilleStructure) {
            return isWindowOpening(level, structure.getBasePosition());
        }

        if (structure instanceof RampStructure rampStructure) {
            return hasRampSupport(level, rampStructure);
        }

        if (structure instanceof DoorStructure) {
            return hasDoorSupport(level, structure);
        }

        return hasSolidSupport(level, structure.getBasePosition());
    }

    

    private static boolean placeStructureBlocks(ServerLevel level, ServerPlayer player, Structure structure, String structureType) {
        if (structure instanceof DoorStructure doorStructure) {
            Block doorBlock = doorStructure.getDoorType() == DoorStructure.DoorType.IRON ? Blocks.IRON_DOOR : Blocks.OAK_DOOR;
            Direction facing = Direction.fromYRot(player == null ? 0.0F : player.getYRot());
            BlockPos lowerPos = structure.getBasePosition();
            BlockPos upperPos = lowerPos.above();

            BlockState lowerState = doorBlock.defaultBlockState()
                    .setValue(DoorBlock.FACING, facing)
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            BlockState upperState = doorBlock.defaultBlockState()
                    .setValue(DoorBlock.FACING, facing)
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);

            level.setBlockAndUpdate(lowerPos, lowerState);
            level.setBlockAndUpdate(upperPos, upperState);
            return true;
        }

        if (structure instanceof FoundationStructure foundationStructure) {
            BlockState floorState = ModBlocks.FOUNDATION_BASE.get().defaultBlockState();
            BlockState edgeState = Blocks.OAK_LOG.defaultBlockState();

            for (BlockPos pos : foundationStructure.getBlockPositions()) {
                BlockState state = foundationStructure.isEdgePosition(pos) ? edgeState : floorState;
                level.setBlockAndUpdate(pos, state);
            }
            return true;
        }

        if (structure instanceof FloorLadderStructure) {
            FloorLadderStructure floorLadder = (FloorLadderStructure) structure;
            Direction facing = floorLadder.getFacing();
            BlockState ladderState = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing.getOpposite());

            // Ставим опорные блоки только если структура создана с поддержкой
            if (floorLadder.isWithSupport()) {
                BlockState supportState = ModBlocks.STRUCTURE_BLOCK.get().defaultBlockState();
                for (BlockPos supportPos : floorLadder.getSupportBlocks()) {
                    level.setBlockAndUpdate(supportPos, supportState);
                }
            }

            // Устанавливаем лестницу, пропуская люки
            for (BlockPos pos : floorLadder.getLadderBlocks()) {
                BlockState currentState = level.getBlockState(pos);
                // Пропускаем люки - они остаются на месте
                if (currentState.is(Blocks.OAK_TRAPDOOR) || currentState.is(Blocks.IRON_TRAPDOOR)) {
                    continue;
                }
                level.setBlockAndUpdate(pos, ladderState);
            }
            return true;
        }

        // Для стен и проёмов размещаем основные блоки и опоры из дубовых брёвен
        if (isWallLikeStructure(structure)) {
            Block placementBlock = getPlacementBlock(structureType);
            BlockState wallState = placementBlock.defaultBlockState();
            BlockState supportState = Blocks.OAK_LOG.defaultBlockState();

            // Размещаем все блоки структуры, проверяя является ли каждый блок опорой или основной частью
            if (structure instanceof WallStructure) {
                WallStructure wall = (WallStructure) structure;
                for (BlockPos pos : structure.getBlockPositions()) {
                    BlockState state = wall.isSupportBlock(pos) ? supportState : wallState;
                    level.setBlockAndUpdate(pos, state);
                }
            } else if (structure instanceof DoorFrameStructure) {
                DoorFrameStructure doorFrame = (DoorFrameStructure) structure;
                for (BlockPos pos : structure.getBlockPositions()) {
                    BlockState state = doorFrame.isSupportBlock(pos) ? supportState : wallState;
                    level.setBlockAndUpdate(pos, state);
                }
            } else if (structure instanceof WindowFrameStructure) {
                WindowFrameStructure windowFrame = (WindowFrameStructure) structure;
                for (BlockPos pos : structure.getBlockPositions()) {
                    BlockState state = windowFrame.isSupportBlock(pos) ? supportState : wallState;
                    level.setBlockAndUpdate(pos, state);
                }
            } else if (structure instanceof HalfWallStructure) {
                HalfWallStructure halfWall = (HalfWallStructure) structure;
                for (BlockPos pos : structure.getBlockPositions()) {
                    BlockState state = halfWall.isSupportBlock(pos) ? supportState : wallState;
                    level.setBlockAndUpdate(pos, state);
                }
            }
            return true;
        }

        // Для крыш размещаем основные блоки и опоры из дубовых брёвен/плит
        if (structure instanceof RoofStructure || structure instanceof RoofHoleStructure) {
            Block placementBlock = getPlacementBlock(structureType);
            BlockState roofState = placementBlock.defaultBlockState();
            BlockState supportState = Blocks.OAK_LOG.defaultBlockState();

            // Размещаем все блоки структуры, проверяя является ли каждый блок опорой или основной частью
            if (structure instanceof RoofStructure) {
                RoofStructure roof = (RoofStructure) structure;
                for (BlockPos pos : structure.getBlockPositions()) {
                    BlockState state = roof.isSupportBlock(pos) ? supportState : roofState;
                    level.setBlockAndUpdate(pos, state);
                }
            } else {
                RoofHoleStructure roofHole = (RoofHoleStructure) structure;
                for (BlockPos pos : structure.getBlockPositions()) {
                    BlockState state = roofHole.isSupportBlock(pos) ? supportState : roofState;
                    level.setBlockAndUpdate(pos, state);
                }
            }
            return true;
        }

        if (structure instanceof RoofHoleTrapdoorStructure) {
            Direction facing = Direction.fromYRot(player == null ? 0.0F : player.getYRot());
            boolean open = isWindowOpening(level, structure.getBasePosition());
            BlockState trapdoorState = Blocks.OAK_TRAPDOOR.defaultBlockState()
                    .setValue(TrapDoorBlock.FACING, facing)
                    .setValue(TrapDoorBlock.OPEN, open)
                    .setValue(TrapDoorBlock.HALF, Half.BOTTOM);
            level.setBlockAndUpdate(structure.getBasePosition(), trapdoorState);
            return true;
        }

        if (structure instanceof StorageChestStructure) {
            Direction facing = Direction.fromYRot(player == null ? 0.0F : player.getYRot());
            BlockState barrelState = Blocks.BARREL.defaultBlockState()
                    .setValue(BarrelBlock.FACING, facing);
            level.setBlockAndUpdate(structure.getBasePosition(), barrelState);
            return true;
        }

        if (structure instanceof WindowGrilleStructure) {
            level.setBlockAndUpdate(structure.getBasePosition(), Blocks.IRON_BARS.defaultBlockState());
            return true;
        }

        if (structure instanceof RampStructure rampStructure) {
            BlockState stairState = Blocks.OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, rampStructure.getFacing())
                    .setValue(StairBlock.HALF, Half.BOTTOM);
            for (BlockPos pos : rampStructure.getBlockPositions()) {
                level.setBlockAndUpdate(pos, stairState);
            }
            return true;
        }

        Block placementBlock = getPlacementBlock(structureType);
        structure.getBlockPositions().forEach(pos -> level.setBlockAndUpdate(pos, placementBlock.defaultBlockState()));
        return true;
    }

    private static boolean hasAllowedWorldOccupancy(Level level, Structure structure) {
        if (level == null) {
            return true;
        }

        // Лестница без опоры требует полностью свободный путь (кроме люков)
        if (structure instanceof FloorLadderStructure floorLadder && !floorLadder.isWithSupport()) {
            for (BlockPos position : structure.getBlockPositions()) {
                BlockState state = level.getBlockState(position);
                if (state.isAir() || state.canBeReplaced()) {
                    continue;
                }
                // Люки пропускаем (они будут пропущены при установке)
                if (state.is(Blocks.OAK_TRAPDOOR) || state.is(Blocks.IRON_TRAPDOOR)) {
                    continue;
                }
                // Если есть другой блок на пути — лестница не ставится
                return false;
            }
            return true;
        }

        int overlapCount = 0;
        boolean allowStructureOverlap = isWallLikeStructure(structure)
            || structure instanceof RoofStructure
            || structure instanceof RoofHoleStructure
            || structure instanceof FloorLadderStructure;
        boolean allowFoundationOverlap = structure instanceof FoundationStructure;
        int overlapLimit = structure instanceof FoundationStructure ? 25 : (structure instanceof RoofStructure ? 25 : MAX_ALLOWED_STRUCTURE_OVERLAP);

        for (BlockPos position : structure.getBlockPositions()) {
            BlockState state = level.getBlockState(position);
            if (state.isAir() || state.canBeReplaced()) {
                continue;
            }

            // Разрешаем перекрытие со структурными блоками, лестницами и деревянными опорами для стен/крыш
            if (allowStructureOverlap && (state.is(ModBlocks.STRUCTURE_BLOCK.get()) || state.is(Blocks.LADDER) || state.is(Blocks.OAK_LOG))) {
                overlapCount++;
                if (overlapCount > overlapLimit) {
                    return false;
                }
                continue;
            }

            // Лестница с опорой разрешает люки
            if (structure instanceof FloorLadderStructure floorLadder && floorLadder.isWithSupport() 
                    && (state.is(Blocks.OAK_TRAPDOOR) || state.is(Blocks.IRON_TRAPDOOR))) {
                overlapCount++;
                if (overlapCount > overlapLimit) {
                    return false;
                }
                continue;
            }

            if (structure instanceof FloorLadderStructure && state.is(Blocks.LADDER)) {
                overlapCount++;
                if (overlapCount > overlapLimit) {
                    return false;
                }
                continue;
            }

            if (allowFoundationOverlap && isFoundationBlock(state)) {
                overlapCount++;
                if (overlapCount > overlapLimit) {
                    return false;
                }
                continue;
            }

            return false;
        }

        return true;
    }

    private static boolean hasSolidSupport(Level level, BlockPos basePosition) {
        if (level == null) {
            return true;
        }

        BlockPos supportPos = basePosition.below();
        BlockState supportState = level.getBlockState(supportPos);
        return !supportState.isAir() && supportState.isFaceSturdy(level, supportPos, Direction.UP);
    }

    private static boolean hasFoundationOrRoofSupportForLadder(Level level, BlockPos basePosition) {
        if (level == null) {
            return true;
        }

        BlockPos supportPos = basePosition.below();
        BlockState supportState = level.getBlockState(supportPos);
        if (isFoundationBlock(supportState)) {
            return true;
        }

        if (supportState.is(Blocks.OAK_LOG) || supportState.is(ModBlocks.STRUCTURE_BLOCK.get())) {
            return true;
        }

        return false;
    }

    private static boolean hasRampSupport(Level level, RampStructure rampStructure) {
        if (level == null) {
            return true;
        }

        // Рамп можно ставить даже без опоры - просто разрешаем всегда
        // Ранее требовалось чтобы рядом был край фундамента, но это слишком ограничивает
        return true;
    }

    private static boolean canUseFloorLadderSupport(Level level, BlockPos supportPos, Direction facing) {
        if (level == null) {
            return true;
        }

        BlockState supportState = level.getBlockState(supportPos);
        if (supportState.isFaceSturdy(level, supportPos, facing)) {
            return true;
        }

        return supportState.isAir() || supportState.canBeReplaced();
    }

    private static boolean hasRoofHoleAbove(Level level, Structure structure) {
        if (level == null || !(structure instanceof FloorLadderStructure)) {
            return false;
        }

        BlockPos ladderBase = structure.getBasePosition();
        BlockPos roofBase = ladderBase.offset(-2, 3, -2);

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                BlockPos checkPos = roofBase.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);

                if (x == 2 && z == 2) {
                    if (!state.isAir()
                            && !state.canBeReplaced()
                            && !state.is(Blocks.LADDER)
                            && !state.is(Blocks.OAK_TRAPDOOR)
                            && !state.is(Blocks.IRON_TRAPDOOR)) {
                        return false;
                    }
                    continue;
                }

                if (!state.is(ModBlocks.STRUCTURE_BLOCK.get())) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean hasRoofHoleBelow(Level level, Structure structure) {
        if (level == null || !(structure instanceof RoofHoleTrapdoorStructure)) {
            return false;
        }

        BlockPos trapdoorPos = structure.getBasePosition();
        BlockPos belowTrapdoor = trapdoorPos.below();

        // Проверка 1: на месте люка должен быть воздух (не заменяем блоки)
        BlockState trapdoorState = level.getBlockState(trapdoorPos);
        if (!trapdoorState.isAir() && !trapdoorState.canBeReplaced()) {
            return false;
        }

        // Проверка 2: прямо под люком должна быть лестница
        BlockState belowState = level.getBlockState(belowTrapdoor);

        if (!belowState.is(Blocks.LADDER)) {
            return false;
        }

        // Проверка 3: где-то внизу от лестницы должен быть фундамент или крыша из structure_block
        for (int dy = 1; dy <= 5; dy++) {
            BlockPos checkPos = belowTrapdoor.below(dy);
            BlockState checkState = level.getBlockState(checkPos);
            if (isFoundationOrRoofSupportBlock(checkState)) {
                return true;
            }
        }

        return false;
    }

    private static BlockPos findRoofHoleBase(Level level, BlockPos clickedPos) {
        // Ищем проём крыши с дыркой вниз от позиции клика (до 8 блоков вниз)
        for (int dy = 0; dy <= 8; dy++) {
            int y = clickedPos.getY() - dy;

            for (int baseX = clickedPos.getX() - 4; baseX <= clickedPos.getX(); baseX++) {
                for (int baseZ = clickedPos.getZ() - 4; baseZ <= clickedPos.getZ(); baseZ++) {
                    BlockPos roofHoleBase = new BlockPos(baseX, y, baseZ);
                    if (!isRoofHolePattern(level, roofHoleBase)) {
                        continue;
                    }

                    int offsetX = clickedPos.getX() - baseX;
                    int offsetZ = clickedPos.getZ() - baseZ;
                    // Люк можно ставить только над центром проёма 5x5.
                    if (offsetX == 2 && offsetZ == 2) {
                        return roofHoleBase;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isRoofHolePattern(Level level, BlockPos roofHoleBase) {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                BlockPos checkPos = roofHoleBase.offset(x, 0, z);
                BlockState state = level.getBlockState(checkPos);

                if (x == 2 && z == 2) {
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                    continue;
                }

                if (!state.is(ModBlocks.STRUCTURE_BLOCK.get())) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean hasFoundationSupport(Level level, Structure structure) {
        if (level == null) {
            return true;
        }

        BlockPos base = structure.getBasePosition();

        int[] supportDepths = computeFoundationSupportDepths(level, base);
        for (int depth : supportDepths) {
            if (depth < 0) {
                return false;
            }
        }

        return true;
    }

    public static int[] computeFoundationSupportDepths(Level level, BlockPos base) {
        int[] depths = new int[4];
        int[][] cornerOffsets = new int[][] {
                {0, 0},
                {4, 0},
                {0, 4},
                {4, 4}
        };

        for (int i = 0; i < cornerOffsets.length; i++) {
            int cornerX = cornerOffsets[i][0];
            int cornerZ = cornerOffsets[i][1];
            depths[i] = findSupportDepth(level, base.offset(cornerX, 0, cornerZ));
        }

        return depths;
    }

    private static int findSupportDepth(Level level, BlockPos topPos) {
        for (int depth = 0; depth <= 3; depth++) {
            BlockPos supportPos = topPos.below(depth + 1);
            BlockState supportState = level.getBlockState(supportPos);
            if (supportState.isAir()) {
                continue;
            }

            if (isFoundationBlock(supportState)) {
                // Остатки чужих фундаментов после взрыва не должны мешать установке.
                continue;
            }

            if (supportState.isFaceSturdy(level, supportPos, Direction.UP)) {
                return depth;
            }
        }

        return -1;
    }

    private static boolean hasFoundationEdgeSupport(Level level, Structure structure) {
        if (level == null) {
            return true;
        }

        StructureManager manager = level instanceof ServerLevel serverLevel ? StructureManager.get(serverLevel) : null;

        int minY = structure.getBlockPositions().stream().mapToInt(BlockPos::getY).min().orElse(structure.getBasePosition().getY());
        Set<BlockPos> bottomLayer = new HashSet<>();

        for (BlockPos blockPos : structure.getBlockPositions()) {
            if (blockPos.getY() == minY) {
                bottomLayer.add(blockPos);
            }
        }

        if (bottomLayer.isEmpty()) {
            return false;
        }

        WallStructure.WallOrientation wallOrientation = getWallOrientation(structure);
        for (BlockPos blockPos : bottomLayer) {
            if (manager != null) {
                Structure occupying = manager.getStructureAtPosition(blockPos);
                if (isWallLikeStructure(occupying)) {
                    continue;
                }
            }

            BlockPos supportPos = blockPos.below();
            BlockState supportState = level.getBlockState(supportPos);
            if (!isWallSupportBlock(supportState)) {
                return false;
            }

            if (manager != null) {
                Structure supportStructure = manager.getStructureAtPosition(supportPos);
                if (isWallLikeStructure(supportStructure)) {
                    continue;
                }
            }

            if (wallOrientation != null) {
                boolean onFoundation = isFoundationEdge(level, supportPos)
                        || isInternalFoundationSeam(level, supportPos, wallOrientation);
                boolean onRoof = isRoofEdge(level, supportPos)
                        || isInternalRoofSeam(level, supportPos, wallOrientation);
                if (!onFoundation && !onRoof) {
                    return false;
                }
            } else if (!isFoundationEdge(level, supportPos) && !isRoofEdge(level, supportPos)) {
                return false;
            }
        }

        BlockPos centerPos = getWallCenterPosition(structure);
        if (centerPos == null) {
            return false;
        }

        BlockPos centerSupport = centerPos.below();
        if (!isWallSupportBlock(level.getBlockState(centerSupport))) {
            return false;
        }

        if (manager != null) {
            Structure centerSupportStructure = manager.getStructureAtPosition(centerSupport);
            if (isWallLikeStructure(centerSupportStructure)) {
                return true;
            }
        }

        if (wallOrientation != null) {
            boolean onFoundation = isFoundationEdgeCenter(level, centerSupport)
                    || isInternalFoundationSeam(level, centerSupport, wallOrientation);
            boolean onRoof = isRoofEdgeCenter(level, centerSupport)
                    || isInternalRoofSeam(level, centerSupport, wallOrientation);
            if (!onFoundation && !onRoof) {
                return false;
            }
        } else if (!isFoundationEdgeCenter(level, centerSupport) && !isRoofEdgeCenter(level, centerSupport)) {
            return false;
        }

        return true;
    }

    private static boolean hasRoofWallSupport(Level level, Structure structure) {
        if (level == null) {
            return true;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return hasRoofWallSupportPreview(level, structure);
        }

        StructureManager manager = StructureManager.get(serverLevel);
        int minX = structure.getBlockPositions().stream().mapToInt(BlockPos::getX).min().orElse(structure.getBasePosition().getX());
        int maxX = structure.getBlockPositions().stream().mapToInt(BlockPos::getX).max().orElse(structure.getBasePosition().getX());
        int minZ = structure.getBlockPositions().stream().mapToInt(BlockPos::getZ).min().orElse(structure.getBasePosition().getZ());
        int maxZ = structure.getBlockPositions().stream().mapToInt(BlockPos::getZ).max().orElse(structure.getBasePosition().getZ());
        int y = structure.getBasePosition().getY();

        boolean hasAnySupport = false;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean isPerimeter = x == minX || x == maxX || z == minZ || z == maxZ;
                if (!isPerimeter) {
                    continue;
                }

                BlockPos supportPos = new BlockPos(x, y - 1, z);
                Structure supportStructure = manager.getStructureAtPosition(supportPos);
                if (supportStructure instanceof RoofStructure || supportStructure instanceof RoofHoleStructure || supportStructure instanceof FloorLadderStructure) {
                    return false;
                }

                if (supportStructure instanceof WallStructure
                        || supportStructure instanceof DoorFrameStructure
                        || supportStructure instanceof WindowFrameStructure) {
                    hasAnySupport = true;
                }
            }
        }

        return hasAnySupport;
    }

    private static boolean hasRoofWallSupportPreview(Level level, Structure structure) {
        int minX = structure.getBlockPositions().stream().mapToInt(BlockPos::getX).min().orElse(structure.getBasePosition().getX());
        int maxX = structure.getBlockPositions().stream().mapToInt(BlockPos::getX).max().orElse(structure.getBasePosition().getX());
        int minZ = structure.getBlockPositions().stream().mapToInt(BlockPos::getZ).min().orElse(structure.getBasePosition().getZ());
        int maxZ = structure.getBlockPositions().stream().mapToInt(BlockPos::getZ).max().orElse(structure.getBasePosition().getZ());
        int y = structure.getBasePosition().getY();

        boolean hasAnySupport = false;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean isPerimeter = x == minX || x == maxX || z == minZ || z == maxZ;
                if (!isPerimeter) {
                    continue;
                }

                BlockPos supportPos = new BlockPos(x, y - 1, z);
                BlockState supportState = level.getBlockState(supportPos);
                if (!supportState.isAir() && supportState.isFaceSturdy(level, supportPos, Direction.UP)) {
                    hasAnySupport = true;
                }
            }
        }

        return hasAnySupport;
    }

    private static boolean hasDoorSupport(Level level, Structure structure) {
        if (level == null) {
            return true;
        }

        BlockPos lowerPos = structure.getBasePosition();
        BlockPos upperPos = lowerPos.above();
        BlockPos supportPos = lowerPos.below();

        BlockState supportState = level.getBlockState(supportPos);
        if (supportState.isAir() || !supportState.isFaceSturdy(level, supportPos, Direction.UP)) {
            return false;
        }

        if (!level.getBlockState(lowerPos).canBeReplaced() || !level.getBlockState(upperPos).canBeReplaced()) {
            return false;
        }

        return isDoorFrameOpening(level, lowerPos);
    }

    private static boolean isDoorFrameOpening(Level level, BlockPos lowerPos) {
        return isDoorFrameOpeningAlongX(level, lowerPos) || isDoorFrameOpeningAlongZ(level, lowerPos);
    }

    private static boolean isDoorFrameOpeningAlongX(Level level, BlockPos lowerPos) {
        BlockPos upperPos = lowerPos.above();
        BlockPos leftLower = lowerPos.west();
        BlockPos rightLower = lowerPos.east();
        BlockPos leftUpper = upperPos.west();
        BlockPos rightUpper = upperPos.east();
        BlockPos top = upperPos.above();

        return isStructureFrameBlock(level, leftLower)
                && isStructureFrameBlock(level, rightLower)
                && isStructureFrameBlock(level, leftUpper)
                && isStructureFrameBlock(level, rightUpper)
                && isStructureFrameBlock(level, top);
    }

    private static boolean isDoorFrameOpeningAlongZ(Level level, BlockPos lowerPos) {
        BlockPos upperPos = lowerPos.above();
        BlockPos leftLower = lowerPos.north();
        BlockPos rightLower = lowerPos.south();
        BlockPos leftUpper = upperPos.north();
        BlockPos rightUpper = upperPos.south();
        BlockPos top = upperPos.above();

        return isStructureFrameBlock(level, leftLower)
                && isStructureFrameBlock(level, rightLower)
                && isStructureFrameBlock(level, leftUpper)
                && isStructureFrameBlock(level, rightUpper)
                && isStructureFrameBlock(level, top);
    }

    private static boolean isStructureFrameBlock(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.STRUCTURE_BLOCK.get());
    }

    private static boolean isWallLikeStructure(Structure structure) {
        return structure instanceof WallStructure
                || structure instanceof DoorFrameStructure
                || structure instanceof WindowFrameStructure
                || structure instanceof HalfWallStructure;
    }

    private static boolean isFoundationBlock(BlockState state) {
        return state.is(ModBlocks.FOUNDATION_BASE.get()) || state.is(Blocks.OAK_LOG);
    }

    private static boolean isWallSupportBlock(BlockState state) {
        return state.is(Blocks.OAK_LOG);
    }

    private static boolean isFoundationOrRoofSupportBlock(BlockState state) {
        return isFoundationBlock(state) || state.is(ModBlocks.STRUCTURE_BLOCK.get());
    }

    private static boolean isFoundationBlock(Level level, BlockPos pos) {
        return isFoundationBlock(level.getBlockState(pos));
    }

    private static boolean isFoundationEdge(Level level, BlockPos supportPos) {
        if (level == null) {
            return true;
        }

        FoundationBounds bounds = findFoundationBoundsFromPlate(level, supportPos);
        if (bounds == null) {
            return false;
        }

        boolean onEdgeX = supportPos.getX() == bounds.minX || supportPos.getX() == bounds.maxX;
        boolean onEdgeZ = supportPos.getZ() == bounds.minZ || supportPos.getZ() == bounds.maxZ;
        return onEdgeX || onEdgeZ;
    }

    private static boolean isFoundationEdgeCenter(Level level, BlockPos supportPos) {
        if (level == null) {
            return true;
        }

        FoundationBounds bounds = findFoundationBoundsFromPlate(level, supportPos);
        if (bounds == null) {
            return false;
        }

        int centerX = bounds.minX + 2;
        int centerZ = bounds.minZ + 2;
        boolean northOrSouth = (supportPos.getZ() == bounds.minZ || supportPos.getZ() == bounds.maxZ)
                && supportPos.getX() == centerX;
        boolean westOrEast = (supportPos.getX() == bounds.minX || supportPos.getX() == bounds.maxX)
                && supportPos.getZ() == centerZ;
        return northOrSouth || westOrEast;
    }

    private static boolean isRoofEdge(Level level, BlockPos supportPos) {
        if (level == null) {
            return true;
        }

        FoundationBounds bounds = findRoofBoundsFromPlate(level, supportPos);
        if (bounds == null) {
            return false;
        }

        boolean onEdgeX = supportPos.getX() == bounds.minX || supportPos.getX() == bounds.maxX;
        boolean onEdgeZ = supportPos.getZ() == bounds.minZ || supportPos.getZ() == bounds.maxZ;
        return onEdgeX || onEdgeZ;
    }

    private static boolean isRoofEdgeCenter(Level level, BlockPos supportPos) {
        if (level == null) {
            return true;
        }

        FoundationBounds bounds = findRoofBoundsFromPlate(level, supportPos);
        if (bounds == null) {
            return false;
        }

        int centerX = bounds.minX + 2;
        int centerZ = bounds.minZ + 2;
        boolean northOrSouth = (supportPos.getZ() == bounds.minZ || supportPos.getZ() == bounds.maxZ)
                && supportPos.getX() == centerX;
        boolean westOrEast = (supportPos.getX() == bounds.minX || supportPos.getX() == bounds.maxX)
                && supportPos.getZ() == centerZ;
        return northOrSouth || westOrEast;
    }

    private static boolean isInternalFoundationSeam(Level level, BlockPos supportPos, WallStructure.WallOrientation orientation) {
        if (level == null) {
            return true;
        }

        if (orientation == WallStructure.WallOrientation.EAST || orientation == WallStructure.WallOrientation.WEST) {
            FoundationBounds westBounds = findFoundationBoundsFromPlate(level, supportPos.west());
            FoundationBounds eastBounds = findFoundationBoundsFromPlate(level, supportPos.east());
            return westBounds != null && eastBounds != null && !isSameFoundationBounds(westBounds, eastBounds);
        }

        FoundationBounds northBounds = findFoundationBoundsFromPlate(level, supportPos.north());
        FoundationBounds southBounds = findFoundationBoundsFromPlate(level, supportPos.south());
        return northBounds != null && southBounds != null && !isSameFoundationBounds(northBounds, southBounds);
    }

    private static boolean isInternalRoofSeam(Level level, BlockPos supportPos, WallStructure.WallOrientation orientation) {
        if (level == null) {
            return true;
        }

        if (orientation == WallStructure.WallOrientation.EAST || orientation == WallStructure.WallOrientation.WEST) {
            FoundationBounds westBounds = findRoofBoundsFromPlate(level, supportPos.west());
            FoundationBounds eastBounds = findRoofBoundsFromPlate(level, supportPos.east());
            return westBounds != null && eastBounds != null && !isSameFoundationBounds(westBounds, eastBounds);
        }

        FoundationBounds northBounds = findRoofBoundsFromPlate(level, supportPos.north());
        FoundationBounds southBounds = findRoofBoundsFromPlate(level, supportPos.south());
        return northBounds != null && southBounds != null && !isSameFoundationBounds(northBounds, southBounds);
    }

    private static boolean isSameFoundationBounds(FoundationBounds left, FoundationBounds right) {
        if (left == null || right == null) {
            return false;
        }

        return left.minX == right.minX
                && left.maxX == right.maxX
                && left.minZ == right.minZ
                && left.maxZ == right.maxZ
                && left.y == right.y;
    }

    private static BlockPos getWallCenterPosition(Structure structure) {
        if (structure instanceof WallStructure wall) {
            return getWallCenterPosition(wall.getBasePosition(), wall.getOrientation());
        }
        if (structure instanceof DoorFrameStructure doorFrame) {
            return getWallCenterPosition(doorFrame.getBasePosition(), doorFrame.getOrientation());
        }
        if (structure instanceof WindowFrameStructure windowFrame) {
            return getWallCenterPosition(windowFrame.getBasePosition(), windowFrame.getOrientation());
        }
        if (structure instanceof HalfWallStructure halfWall) {
            return getWallCenterPosition(halfWall.getBasePosition(), halfWall.getOrientation());
        }

        return null;
    }

    private static WallStructure.WallOrientation getWallOrientation(Structure structure) {
        if (structure instanceof WallStructure wall) {
            return wall.getOrientation();
        }
        if (structure instanceof DoorFrameStructure doorFrame) {
            return doorFrame.getOrientation();
        }
        if (structure instanceof WindowFrameStructure windowFrame) {
            return windowFrame.getOrientation();
        }
        if (structure instanceof HalfWallStructure halfWall) {
            return halfWall.getOrientation();
        }

        return null;
    }

    private static BlockPos getWallCenterPosition(BlockPos basePosition, WallStructure.WallOrientation orientation) {
        return switch (orientation) {
            case NORTH -> basePosition.offset(2, 0, 0);
            case SOUTH -> basePosition.offset(-2, 0, 0);
            case EAST -> basePosition.offset(0, 0, 2);
            case WEST -> basePosition.offset(0, 0, -2);
        };
    }

    private static BlockPos projectToFoundationTop(FoundationBounds bounds, BlockPos pos) {
        int x = Math.max(bounds.minX, Math.min(bounds.maxX, pos.getX()));
        int z = Math.max(bounds.minZ, Math.min(bounds.maxZ, pos.getZ()));
        return new BlockPos(x, bounds.y, z);
    }

    private static EdgeCenter getNearestWallEdgeCenter(FoundationBounds bounds, BlockPos pos, Direction face, float yaw) {
        int centerX = bounds.minX + 2;
        int centerZ = bounds.minZ + 2;
        EdgeCenter[] candidates = new EdgeCenter[] {
                new EdgeCenter(new BlockPos(centerX, bounds.y, bounds.minZ), Direction.NORTH),
                new EdgeCenter(new BlockPos(centerX, bounds.y, bounds.maxZ), Direction.SOUTH),
                new EdgeCenter(new BlockPos(bounds.minX, bounds.y, centerZ), Direction.WEST),
                new EdgeCenter(new BlockPos(bounds.maxX, bounds.y, centerZ), Direction.EAST)
        };

        EdgeCenter best = null;
        int bestDistance = Integer.MAX_VALUE;
        Direction lookDirection = Direction.fromYRot(yaw);

        for (EdgeCenter candidate : candidates) {
            int dx = pos.getX() - candidate.center.getX();
            int dz = pos.getZ() - candidate.center.getZ();
            int distance = dx * dx + dz * dz;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
                continue;
            }

            if (distance == bestDistance && best != null) {
                if (candidate.face == face) {
                    best = candidate;
                } else if (candidate.face == lookDirection) {
                    best = candidate;
                }
            }
        }

        return best;
    }

    private static final class EdgeCenter {
        private final BlockPos center;
        private final Direction face;

        private EdgeCenter(BlockPos center, Direction face) {
            this.center = center;
            this.face = face;
        }
    }

    private static boolean hasBlockingOverlap(StructureManager manager, Structure structure) {
        if (structure instanceof FoundationStructure) {
            Set<BlockPos> neighboringFoundationBases = new HashSet<>();
            BlockPos newBase = structure.getBasePosition();

            for (BlockPos position : structure.getBlockPositions()) {
                Structure occupying = manager.getStructureAtPosition(position);
                if (occupying == null) {
                    continue;
                }

                if (!(occupying instanceof FoundationStructure)) {
                    return true;
                }

                BlockPos neighborBase = occupying.getBasePosition();
                if (neighborBase.equals(newBase)) {
                    return true;
                }

                neighboringFoundationBases.add(neighborBase);
            }

            if (neighboringFoundationBases.isEmpty()) {
                return false;
            }

            boolean hasDirectEdgeNeighbor = false;
            for (BlockPos neighborBase : neighboringFoundationBases) {
                int dx = Math.abs(neighborBase.getX() - newBase.getX());
                int dz = Math.abs(neighborBase.getZ() - newBase.getZ());

                if ((dx == 4 && dz == 0) || (dx == 0 && dz == 4)) {
                    hasDirectEdgeNeighbor = true;
                    continue;
                }

                if (dx == 4 && dz == 4) {
                    continue;
                }

                return true;
            }

            return !hasDirectEdgeNeighbor;
        }

        if (structure instanceof RoofStructure || structure instanceof RoofHoleStructure) {
            Set<BlockPos> neighboringRoofBases = new HashSet<>();
            BlockPos newBase = structure.getBasePosition();

            for (BlockPos position : structure.getBlockPositions()) {
                Structure occupying = manager.getStructureAtPosition(position);
                if (occupying == null) {
                    continue;
                }

                if (!(occupying instanceof RoofStructure || occupying instanceof RoofHoleStructure)) {
                    return true;
                }

                BlockPos neighborBase = occupying.getBasePosition();
                if (neighborBase.equals(newBase)) {
                    return true;
                }

                neighboringRoofBases.add(neighborBase);
            }

            if (neighboringRoofBases.isEmpty()) {
                return false;
            }

            boolean hasDirectEdgeNeighbor = false;
            for (BlockPos neighborBase : neighboringRoofBases) {
                int dx = Math.abs(neighborBase.getX() - newBase.getX());
                int dz = Math.abs(neighborBase.getZ() - newBase.getZ());

                if ((dx == 4 && dz == 0) || (dx == 0 && dz == 4)) {
                    hasDirectEdgeNeighbor = true;
                    continue;
                }

                if (dx == 4 && dz == 4) {
                    continue;
                }

                return true;
            }

            return !hasDirectEdgeNeighbor;
        }

        if (structure instanceof FloorLadderStructure) {
            for (BlockPos position : structure.getBlockPositions()) {
                Structure occupying = manager.getStructureAtPosition(position);
                if (occupying == null) {
                    continue;
                }

                if (occupying instanceof RoofHoleStructure) {
                    continue;
                }

                if (occupying instanceof FloorLadderStructure) {
                    continue;
                }

                return true;
            }

            return false;
        }

        int overlapCount = 0;
        for (BlockPos position : structure.getBlockPositions()) {
            Structure occupying = manager.getStructureAtPosition(position);
            if (occupying == null) {
                continue;
            }

            if (structure instanceof FoundationStructure && occupying instanceof FoundationStructure) {
                return !hasValidFoundationAdjacency(manager, structure.getBasePosition(), occupying.getBasePosition());
            }

            if (!isWallLikeStructure(structure)) {
                return true;
            }

            if (!isWallLikeStructure(occupying)) {
                return true;
            }

            boolean newIsSupport = isSupportOverlap(structure, position);
            boolean occupyingIsSupport = isSupportOverlap(occupying, position);
            if (newIsSupport || occupyingIsSupport) {
                continue;
            }

            if (isWallLikeStructure(structure)) {
                continue;
            }

            overlapCount++;
            if (overlapCount > MAX_ALLOWED_STRUCTURE_OVERLAP) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSupportOverlap(Structure structure, BlockPos position) {
        if (structure instanceof WallStructure wall) {
            return wall.isSupportBlock(position);
        }

        if (structure instanceof DoorFrameStructure doorFrame) {
            return doorFrame.isSupportBlock(position);
        }

        if (structure instanceof WindowFrameStructure windowFrame) {
            return windowFrame.isSupportBlock(position);
        }

        if (structure instanceof HalfWallStructure halfWall) {
            return halfWall.isSupportBlock(position);
        }

        return false;
    }

    private static BlockPos resolveWallAnchor(Level level, BlockPos clickedPos, Direction face, float yaw) {
        FoundationBounds foundationBounds = findWallTargetedFoundationBounds(level, clickedPos, face, yaw);

        if (foundationBounds == null) {
            return clickedPos.relative(face).above();
        }

        Direction wallFace = resolveWallFace(level, clickedPos, face, yaw);
        EdgeCenter edgeCenter = getNearestWallEdgeCenter(foundationBounds, clickedPos, face, yaw);
        if (edgeCenter == null) {
            return clickedPos.above();
        }

        BlockPos center = edgeCenter.center;
        return switch (edgeCenter.face) {
            case NORTH -> new BlockPos(center.getX() - 2, foundationBounds.y + 1, foundationBounds.minZ);
            case SOUTH -> new BlockPos(center.getX() + 2, foundationBounds.y + 1, foundationBounds.maxZ);
            case EAST -> new BlockPos(foundationBounds.maxX, foundationBounds.y + 1, center.getZ() - 2);
            case WEST -> new BlockPos(foundationBounds.minX, foundationBounds.y + 1, center.getZ() + 2);
            default -> new BlockPos(center.getX() - 2, foundationBounds.y + 1, foundationBounds.minZ);
        };
    }

    private static BlockPos resolveFoundationAnchor(Level level, BlockPos clickedPos, Direction face, float yaw) {
        // Поиск существующего фундамента, на который наводит игрок
        FoundationBounds foundationBounds = findTargetedFoundationBounds(level, clickedPos, face, yaw);

        // Если фундамента не найден, разместить на смещенной позиции
        if (foundationBounds == null) {
            return clickedPos.relative(face);
        }

        // Для стабильной привязки сначала берём сторону клика,
        // а yaw используем только если клик не по боковой стороне.
        Direction placementDirection = face.getAxis().isHorizontal() ? face : Direction.fromYRot(yaw);

        // Смещение на 4 блока в направлении взгляда (с перекрытием на 1 блок)
        return switch (placementDirection) {
            case NORTH -> new BlockPos(foundationBounds.minX, foundationBounds.y, foundationBounds.minZ - 4);
            case SOUTH -> new BlockPos(foundationBounds.minX, foundationBounds.y, foundationBounds.maxZ);
            case EAST -> new BlockPos(foundationBounds.maxX, foundationBounds.y, foundationBounds.minZ);
            case WEST -> new BlockPos(foundationBounds.minX - 4, foundationBounds.y, foundationBounds.minZ);
            default -> new BlockPos(foundationBounds.minX, foundationBounds.y, foundationBounds.minZ);
        };
    }

    private static BlockPos resolveRoofAnchor(Level level, BlockPos clickedPos, Direction face, float yaw) {
        FoundationBounds targetBounds = findRoofTargetedBounds(level, clickedPos, face, yaw);
        if (targetBounds == null) {
            return clickedPos.relative(face);
        }

        return new BlockPos(targetBounds.minX, targetBounds.y + 4, targetBounds.minZ);
    }

    private static BlockPos resolveFloorLadderAnchor(Level level, BlockPos clickedPos, Direction face, float yaw) {
        FoundationBounds targetBounds = findRoofTargetedBounds(level, clickedPos, face, yaw);
        if (targetBounds == null) {
            return clickedPos.above();
        }

        // Лестница должна начинаться на один блок выше верхней плиты фундамента,
        // чтобы опоры не устанавливались под уровень фундамента.
        return new BlockPos(targetBounds.minX + 2, targetBounds.y + 1, targetBounds.minZ + 2);
    }

    private static BlockPos resolveRoofHoleTrapdoorAnchor(Level level, BlockPos clickedPos, Direction face, float yaw) {
        // Люк ставится на 1 блок выше позиции клика (где должна быть лестница)
        return clickedPos.above();
    }

    private static BlockPos resolveWindowGrilleAnchor(Level level, BlockPos clickedPos, Direction face, float yaw) {
        if (level != null) {
            BlockPos windowOpening = findWindowOpening(level, clickedPos);
            if (windowOpening != null) {
                return windowOpening;
            }
        }

        return clickedPos.relative(face);
    }

    private static BlockPos resolveRampAnchor(Level level, BlockPos clickedPos, Direction face, float yaw) {
        FoundationBounds foundationBounds = findWallTargetedFoundationBounds(level, clickedPos, face, yaw);
        if (foundationBounds == null) {
            return clickedPos.relative(face);
        }

        EdgeCenter edgeCenter = getNearestWallEdgeCenter(foundationBounds, clickedPos, face, yaw);
        if (edgeCenter == null) {
            return clickedPos.relative(face);
        }

        BlockPos center = edgeCenter.center;
        int y = foundationBounds.y;
        return switch (edgeCenter.face) {
            case NORTH -> new BlockPos(center.getX() + 2, y, foundationBounds.minZ - 1);
            case SOUTH -> new BlockPos(center.getX() - 2, y, foundationBounds.maxZ + 1);
            case EAST -> new BlockPos(foundationBounds.maxX + 1, y, center.getZ() + 2);
            case WEST -> new BlockPos(foundationBounds.minX - 1, y, center.getZ() - 2);
            default -> new BlockPos(center.getX() + 2, y, foundationBounds.minZ - 1);
        };
    }

    private static Direction resolveRampFace(Level level, BlockPos clickedPos, Direction face, float yaw) {
        FoundationBounds foundationBounds = findWallTargetedFoundationBounds(level, clickedPos, face, yaw);
        if (foundationBounds == null) {
            return face.getAxis().isHorizontal() ? face : Direction.fromYRot(yaw);
        }

        EdgeCenter edgeCenter = getNearestWallEdgeCenter(foundationBounds, clickedPos, face, yaw);
        return edgeCenter == null ? face : edgeCenter.face;
    }

    private static Direction resolveWallFace(Level level, BlockPos clickedPos, Direction face, float yaw) {
        FoundationBounds foundationBounds = findWallTargetedFoundationBounds(level, clickedPos, face, yaw);
        if (foundationBounds == null) {
            return face;
        }

        EdgeCenter edgeCenter = getNearestWallEdgeCenter(foundationBounds, clickedPos, face, yaw);
        return edgeCenter == null ? face : edgeCenter.face;
    }

    private static BlockPos findWindowOpening(Level level, BlockPos clickedPos) {
        if (level == null) {
            return null;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos candidate = clickedPos.offset(dx, dy, dz);
                    if (isWindowOpening(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isWindowOpening(Level level, BlockPos pos) {
        if (level == null) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            StructureManager manager = StructureManager.get(serverLevel);
            BlockPos[] neighbors = new BlockPos[] {
                    pos,
                    pos.above(),
                    pos.below(),
                    pos.north(),
                    pos.south(),
                    pos.east(),
                    pos.west()
            };
            for (BlockPos neighbor : neighbors) {
                Structure structure = manager.getStructureAtPosition(neighbor);
                if (structure instanceof WindowFrameStructure windowFrame && windowFrame.isOpeningPosition(pos)) {
                    return true;
                }
            }
        }

        return isWindowFrameOpeningPattern(level, pos);
    }

    private static boolean isWindowFrameOpeningPattern(Level level, BlockPos pos) {
        // Упрощённая проверка: если вокруг позиции есть блоки (рама) - разрешаем размещение
        // Оконная решётка может ставиться в проёме между вертикальными/горизонтальными блоками
        boolean hasHorizontalFrames = (isFrameBlock(level, pos.north()) || isFrameBlock(level, pos.south()));
        boolean hasVerticalFrames = (isFrameBlock(level, pos.above()) || isFrameBlock(level, pos.below()));
        
        return hasHorizontalFrames && hasVerticalFrames;
    }
    
    private static boolean isFrameBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // Проверяем структурные блоки, бревна, доски - что угодно, что может быть рамой
        return state.is(ModBlocks.STRUCTURE_BLOCK.get()) 
            || state.is(Blocks.OAK_LOG)
            || state.is(Blocks.OAK_PLANKS)
            || state.is(Blocks.OAK_FENCE);
    }

    private static FoundationBounds findFoundationBounds(Level level, BlockPos supportPos) {
        if (level == null || !isFoundationColumn(level, supportPos)) {
            return null;
        }

        int y = findFoundationTopY(level, supportPos);
        if (y == Integer.MIN_VALUE) {
            return null;
        }

        int minX = supportPos.getX();
        int maxX = supportPos.getX();
        int minZ = supportPos.getZ();
        int maxZ = supportPos.getZ();

        while (isFoundationColumn(level, new BlockPos(minX - 1, y, supportPos.getZ()))) {
            minX--;
        }

        while (isFoundationColumn(level, new BlockPos(maxX + 1, y, supportPos.getZ()))) {
            maxX++;
        }

        while (isFoundationColumn(level, new BlockPos(supportPos.getX(), y, minZ - 1))) {
            minZ--;
        }

        while (isFoundationColumn(level, new BlockPos(supportPos.getX(), y, maxZ + 1))) {
            maxZ++;
        }

        return new FoundationBounds(minX, maxX, minZ, maxZ, y);
    }

    /**
     * Приоритетный поиск фундамента, на который, вероятно, смотрит игрок.
     * Проверяем несколько кандидатов в порядке приоритета, чтобы выбрать точный
     * фундамент при наличии соседних.
     */
    private static FoundationBounds findTargetedFoundationBounds(Level level, BlockPos clickedPos, Direction face, float yaw) {
        if (level == null) {
            return null;
        }

        BlockPos[] candidates = buildPlacementCandidates(clickedPos, face, yaw, true);
        return findTargetedBuildBounds(level, candidates, false, true);
    }

    private static FoundationBounds findWallTargetedFoundationBounds(Level level, BlockPos clickedPos, Direction face, float yaw) {
        if (level == null) {
            return null;
        }

        BlockPos[] candidates = buildPlacementCandidates(clickedPos, face, yaw, false);
        return findTargetedBuildBounds(level, candidates, true, true);
    }

    private static FoundationBounds findRoofBoundsBelow(Level level, BlockPos pos, int maxDepth) {
        if (level == null) {
            return null;
        }

        for (int offset = 0; offset <= maxDepth; offset++) {
            BlockPos candidate = pos.below(offset);
            FoundationBounds bounds = findRoofBoundsFromPlate(level, candidate);
            if (bounds != null) {
                return bounds;
            }
        }

        return null;
    }

    private static FoundationBounds findRoofTargetedBounds(Level level, BlockPos clickedPos, Direction face, float yaw) {
        if (level == null) {
            return null;
        }

        BlockPos[] candidates = buildPlacementCandidates(clickedPos, face, yaw, true);
        return findTargetedBuildBounds(level, candidates, true, true);
    }

    private static BlockPos[] buildPlacementCandidates(BlockPos clickedPos, Direction face, float yaw, boolean includeAboveBelow) {
        Direction lookDir = Direction.fromYRot(yaw);
        if (includeAboveBelow) {
            return new BlockPos[] {
                    clickedPos,
                    clickedPos.relative(face),
                    clickedPos.relative(face).below(),
                    clickedPos.relative(lookDir),
                    clickedPos.relative(lookDir).below(),
                    clickedPos.relative(lookDir.getOpposite()),
                    clickedPos.relative(lookDir.getOpposite()).below(),
                    clickedPos.below(),
                    clickedPos.above()
            };
        }

        return new BlockPos[] {
                clickedPos,
                clickedPos.relative(face),
                clickedPos.relative(face).below(),
                clickedPos.relative(lookDir),
                clickedPos.relative(lookDir).below(),
                clickedPos.relative(lookDir.getOpposite()),
                clickedPos.relative(lookDir.getOpposite()).below()
        };
    }

    private static FoundationBounds findTargetedBuildBounds(Level level, BlockPos[] candidates, boolean preferRoof, boolean includeFoundation) {
        if (level == null) {
            return null;
        }

        StructureManager manager = level instanceof ServerLevel serverLevel ? StructureManager.get(serverLevel) : null;

        for (int offset = 0; offset <= 8; offset++) {
            for (BlockPos candidate : candidates) {
                BlockPos checkPos = candidate.below(offset);

                if (preferRoof) {
                    FoundationBounds roofBounds = findRoofBoundsFromPlate(level, checkPos);
                    if (roofBounds != null) {
                        return roofBounds;
                    }

                    if (includeFoundation) {
                        if (manager != null) {
                            Structure structure = manager.getStructureAtPosition(checkPos);
                            if (structure instanceof FoundationStructure foundation) {
                                return createFoundationBoundsFromBase(foundation.getBasePosition());
                            }
                        }

                        FoundationBounds foundationBounds = findFoundationBoundsFromPlate(level, checkPos);
                        if (foundationBounds != null) {
                            return foundationBounds;
                        }
                    }
                    continue;
                }

                if (manager != null) {
                    Structure structure = manager.getStructureAtPosition(checkPos);
                    if (structure instanceof FoundationStructure foundation) {
                        return createFoundationBoundsFromBase(foundation.getBasePosition());
                    }
                }

                FoundationBounds foundationBounds = findFoundationBoundsFromPlate(level, checkPos);
                if (foundationBounds != null) {
                    return foundationBounds;
                }
            }
        }

        return null;
    }

    private static FoundationBounds findFoundationBoundsBelow(Level level, BlockPos pos, int maxDepth) {
        if (level == null) {
            return null;
        }

        for (int offset = 0; offset <= maxDepth; offset++) {
            BlockPos candidate = pos.below(offset);
            FoundationBounds bounds = findFoundationBoundsFromPlate(level, candidate);
            if (bounds != null) {
                return bounds;
            }
        }

        return null;
    }

    private static boolean hasBlockingFoundationOverlapPreview(Level level, Structure structure) {
        if (level == null || !(structure instanceof FoundationStructure)) {
            return false;
        }

        Set<BlockPos> neighboringFoundationBases = new HashSet<>();
        BlockPos newBase = structure.getBasePosition();

        for (BlockPos position : structure.getBlockPositions()) {
            if (!isFoundationBlock(level, position)) {
                continue;
            }

            FoundationBounds bounds = findFoundationBoundsFromPlate(level, position);
            if (bounds == null) {
                continue;
            }

            BlockPos neighborBase = new BlockPos(bounds.minX, bounds.y, bounds.minZ);
            if (neighborBase.equals(newBase)) {
                continue;
            }

            neighboringFoundationBases.add(neighborBase);
        }

        if (neighboringFoundationBases.isEmpty()) {
            return false;
        }

        boolean hasDirectEdgeNeighbor = false;
        for (BlockPos neighborBase : neighboringFoundationBases) {
            int dx = Math.abs(neighborBase.getX() - newBase.getX());
            int dz = Math.abs(neighborBase.getZ() - newBase.getZ());

            if ((dx == 4 && dz == 0) || (dx == 0 && dz == 4)) {
                hasDirectEdgeNeighbor = true;
                continue;
            }

            if (dx == 4 && dz == 4) {
                continue;
            }

            return true;
        }

        return !hasDirectEdgeNeighbor;
    }

    private static FoundationBounds createFoundationBoundsFromBase(BlockPos basePosition) {
        return new FoundationBounds(
                basePosition.getX(),
                basePosition.getX() + 4,
                basePosition.getZ(),
                basePosition.getZ() + 4,
                basePosition.getY()
        );
    }

    private static FoundationBounds findFoundationBoundsFromPlate(Level level, BlockPos pos) {
        if (level == null || !isFoundationColumn(level, pos)) {
            return null;
        }

        int topY = findFoundationTopY(level, pos);
        if (topY == Integer.MIN_VALUE) {
            return null;
        }

        BlockPos topPos = new BlockPos(pos.getX(), topY, pos.getZ());
        if (!isFoundationBlock(level, topPos)) {
            return null;
        }

        int y = topY;
        FoundationBounds best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                int baseX = topPos.getX() - dx;
                int baseZ = topPos.getZ() - dz;
                if (!isFoundationPlate(level, baseX, y, baseZ)) {
                    continue;
                }

                int centerX = baseX + 2;
                int centerZ = baseZ + 2;
                int distance = Math.abs(topPos.getX() - centerX) + Math.abs(topPos.getZ() - centerZ);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new FoundationBounds(baseX, baseX + 4, baseZ, baseZ + 4, y);
                }
            }
        }

        return best;
    }

    private static FoundationBounds findRoofBoundsFromPlate(Level level, BlockPos pos) {
        if (level == null) {
            return null;
        }

        int y = pos.getY();
        FoundationBounds best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                int baseX = pos.getX() - dx;
                int baseZ = pos.getZ() - dz;
                if (!isRoofPlate(level, baseX, y, baseZ)) {
                    continue;
                }

                int centerX = baseX + 2;
                int centerZ = baseZ + 2;
                int distance = Math.abs(pos.getX() - centerX) + Math.abs(pos.getZ() - centerZ);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new FoundationBounds(baseX, baseX + 4, baseZ, baseZ + 4, y);
                }
            }
        }

        return best;
    }

    private static boolean isRoofPlate(Level level, int baseX, int y, int baseZ) {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                BlockPos pos = new BlockPos(baseX + x, y, baseZ + z);
                BlockState state = level.getBlockState(pos);
                boolean isPerimeter = x == 0 || x == 4 || z == 0 || z == 4;

                if (isPerimeter) {
                    if (!state.is(Blocks.OAK_LOG)) {
                        return false;
                    }
                    continue;
                }

                if (x == 2 && z == 2) {
                    if (!state.is(ModBlocks.STRUCTURE_BLOCK.get())
                            && !state.isAir()
                            && !state.canBeReplaced()
                            && !state.is(Blocks.LADDER)
                            && !state.is(Blocks.OAK_TRAPDOOR)
                            && !state.is(Blocks.IRON_TRAPDOOR)) {
                        return false;
                    }
                    continue;
                }

                if (!state.is(ModBlocks.STRUCTURE_BLOCK.get())) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isFoundationPlate(Level level, int baseX, int y, int baseZ) {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                BlockPos pos = new BlockPos(baseX + x, y, baseZ + z);
                if (!isFoundationBlock(level, pos)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static int findFoundationTopY(Level level, BlockPos supportPos) {
        int topY = Integer.MIN_VALUE;

        // Ищём фундамент только вниз, чтобы не захватить крышу выше
        for (int offset = 0; offset <= 8; offset++) {
            BlockPos candidatePos = supportPos.below(offset);
            if (isFoundationBlock(level, candidatePos)) {
                topY = Math.max(topY, candidatePos.getY());
            }
        }

        return topY;
    }

    private static boolean isFoundationColumn(Level level, BlockPos pos) {
        if (level == null) {
            return false;
        }

        // Ищём опору фундамента только вниз, чтобы не захватить крышу выше
        for (int offset = 0; offset <= 8; offset++) {
            if (isFoundationBlock(level, pos.below(offset))) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasValidFoundationAdjacency(StructureManager manager, BlockPos newBase, BlockPos neighborBase) {
        if (manager == null) {
            return true;
        }

        int newX = newBase.getX();
        int newZ = newBase.getZ();
        int neighborX = neighborBase.getX();
        int neighborZ = neighborBase.getZ();

        int dx = Math.abs(newX - neighborX);
        int dz = Math.abs(newZ - neighborZ);

        // Проверяем соседние фундаменты по одной оси
        // Фундамент 5x5, поэтому расстояние между центрами для соседей это 5
        // Если расстояние = 5, они вплотную (без перекрытия) - запретить
        // Если расстояние = 4, они перекрываются на 1 блок - разрешить
        // Если расстояние < 4 или > 5, это не соседи - разрешить

        if (dx == 0) {
            // Один фундамент выше/ниже другого по Z
            if (dz == 5) {
                // Вплотную - не перекрываются - ЗАПРЕТИТЬ
                return false;
            }
            if (dz == 4) {
                // Перекрытие на 1 блок - разрешить
                return true;
            }
        }

        if (dz == 0) {
            // Один фундамент слева/справа от другого по X
            if (dx == 5) {
                // Вплотную - не перекрываются - ЗАПРЕТИТЬ
                return false;
            }
            if (dx == 4) {
                // Перекрытие на 1 блок - разрешить
                return true;
            }
        }

        return true;
    }

    public static final class PlacementPreview {
        private final List<BlockPos> blocks;
        private final boolean valid;

        private PlacementPreview(List<BlockPos> blocks, boolean valid) {
            this.blocks = blocks;
            this.valid = valid;
        }

        public List<BlockPos> blocks() {
            return blocks;
        }

        public boolean valid() {
            return valid;
        }
    }

    private static final class FoundationBounds {
        private final int minX;
        private final int maxX;
        private final int minZ;
        private final int maxZ;
        private final int y;

        private FoundationBounds(int minX, int maxX, int minZ, int maxZ, int y) {
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.y = y;
        }
    }
}
