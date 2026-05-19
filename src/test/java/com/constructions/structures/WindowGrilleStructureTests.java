package com.constructions.structures;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WindowGrilleStructureTests {
    private WindowGrilleStructure grille;
    private UUID testPlayerId;
    private BlockPos basePosition;

    @BeforeEach
    public void setUp() {
        testPlayerId = UUID.randomUUID();
        basePosition = new BlockPos(3, 70, 3);
        grille = new WindowGrilleStructure(basePosition, testPlayerId);
    }

    @Test
    public void testGrilleCreation() {
        assertNotNull(grille);
        assertEquals("window_grille", grille.getStructureType());
        assertEquals(testPlayerId, grille.getOwner());
        assertEquals(basePosition, grille.getBasePosition());
    }

    @Test
    public void testGrilleBlocks() {
        List<BlockPos> blocks = grille.getStructureBlocks();

        assertNotNull(blocks);
        assertEquals(1, blocks.size());
        assertEquals(basePosition, blocks.get(0));
    }
}
