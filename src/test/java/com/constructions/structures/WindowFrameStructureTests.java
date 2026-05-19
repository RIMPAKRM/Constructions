package com.constructions.structures;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WindowFrameStructureTests {
    private WindowFrameStructure windowFrame;
    private UUID testPlayerId;
    private BlockPos basePosition;

    @BeforeEach
    public void setUp() {
        testPlayerId = UUID.randomUUID();
        basePosition = new BlockPos(0, 64, 0);
        windowFrame = new WindowFrameStructure(basePosition, testPlayerId, WallStructure.WallOrientation.NORTH);
    }

    @Test
    public void testWindowFrameCreation() {
        assertNotNull(windowFrame);
        assertEquals("window_frame", windowFrame.getStructureType());
        assertEquals(testPlayerId, windowFrame.getOwner());
        assertEquals(basePosition, windowFrame.getBasePosition());
    }

    @Test
    public void testWindowFrameBlocks() {
        List<BlockPos> blocks = windowFrame.getStructureBlocks();

        assertNotNull(blocks);
        assertEquals(14, blocks.size());
        BlockPos opening = windowFrame.getOpeningPosition();
        assertFalse(blocks.contains(opening));
        assertTrue(windowFrame.getBlockPositions().contains(basePosition));
    }
}
