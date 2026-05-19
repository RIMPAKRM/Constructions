package com.constructions.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RampStructureTests {
    private RampStructure ramp;
    private UUID testPlayerId;
    private BlockPos basePosition;

    @BeforeEach
    public void setUp() {
        testPlayerId = UUID.randomUUID();
        basePosition = new BlockPos(5, 64, 5);
        ramp = new RampStructure(basePosition, testPlayerId, Direction.SOUTH);
    }

    @Test
    public void testRampCreation() {
        assertNotNull(ramp);
        assertEquals("ramp", ramp.getStructureType());
        assertEquals(testPlayerId, ramp.getOwner());
        assertEquals(basePosition, ramp.getBasePosition());
    }

    @Test
    public void testRampBlocks() {
        List<BlockPos> blocks = ramp.getStructureBlocks();

        assertNotNull(blocks);
        assertEquals(5, blocks.size());
        assertTrue(blocks.contains(basePosition));
    }
}
