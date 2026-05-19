package com.constructions.structures;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HalfWallStructureTests {
    private HalfWallStructure halfWall;
    private UUID testPlayerId;
    private BlockPos basePosition;

    @BeforeEach
    public void setUp() {
        testPlayerId = UUID.randomUUID();
        basePosition = new BlockPos(10, 64, 10);
        halfWall = new HalfWallStructure(basePosition, testPlayerId, WallStructure.WallOrientation.EAST);
    }

    @Test
    public void testHalfWallCreation() {
        assertNotNull(halfWall);
        assertEquals("half_wall", halfWall.getStructureType());
        assertEquals(testPlayerId, halfWall.getOwner());
        assertEquals(basePosition, halfWall.getBasePosition());
    }

    @Test
    public void testHalfWallBlocks() {
        List<BlockPos> blocks = halfWall.getStructureBlocks();

        assertNotNull(blocks);
        assertEquals(5, blocks.size());
        assertTrue(halfWall.getBlockPositions().contains(basePosition));
    }
}
