package com.constructions.structures;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoofHoleTrapdoorStructureTests {
    private RoofHoleTrapdoorStructure trapdoor;
    private UUID testPlayerId;
    private BlockPos testPosition;

    @BeforeEach
    public void setUp() {
        testPlayerId = UUID.randomUUID();
        testPosition = new BlockPos(8, 70, 8);
        trapdoor = new RoofHoleTrapdoorStructure(testPosition, testPlayerId);
    }

    @Test
    public void testTrapdoorCreation() {
        assertNotNull(trapdoor);
        assertEquals("roof_hole_trapdoor", trapdoor.getStructureType());
        assertEquals(testPlayerId, trapdoor.getOwner());
        assertEquals(testPosition, trapdoor.getBasePosition());
    }

    @Test
    public void testTrapdoorBlocks() {
        List<BlockPos> blocks = trapdoor.getStructureBlocks();

        assertNotNull(blocks);
        assertEquals(1, blocks.size());
        assertEquals(testPosition, blocks.get(0));
        assertTrue(trapdoor.getBlockPositions().contains(testPosition));
    }
}