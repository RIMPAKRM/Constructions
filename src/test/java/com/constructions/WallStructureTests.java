package com.constructions.structures;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

/**
 * Тесты для структуры стены
 */
public class WallStructureTests {
    private WallStructure wall;
    private UUID testPlayerId;
    private BlockPos testPosition;

    @BeforeEach
    public void setUp() {
        testPlayerId = UUID.randomUUID();
        testPosition = new BlockPos(10, 65, 10);
        wall = new WallStructure(testPosition, testPlayerId, WallStructure.WallOrientation.NORTH);
    }

    @Test
    public void testWallCreation() {
        assertNotNull(wall);
        assertEquals("wall", wall.getStructureType());
        assertEquals(WallStructure.WallOrientation.NORTH, wall.getOrientation());
        assertEquals(testPlayerId, wall.getOwner());
    }

    @Test
    public void testWallHealth() {
        double initialHealth = wall.getCurrentHealth();
        assertTrue(initialHealth > 0);
        
        wall.takeDamage(20);
        assertEquals(initialHealth - 20, wall.getCurrentHealth());
    }

    @Test
    public void testWallBlocks() {
        List<BlockPos> blocks = wall.getStructureBlocks();
        
        assertNotNull(blocks);
        assertTrue(blocks.size() > 0);
        
        // Проверить размеры: 5 в ширину, 4 в высоту
        int expectedSize = 5 * 4;
        assertEquals(expectedSize, blocks.size());
    }

    @Test
    public void testDifferentOrientations() {
        for (WallStructure.WallOrientation orientation : WallStructure.WallOrientation.values()) {
            WallStructure testWall = new WallStructure(testPosition, testPlayerId, orientation);
            assertEquals(orientation, testWall.getOrientation());
            
            List<BlockPos> blocks = testWall.getStructureBlocks();
            assertEquals(20, blocks.size()); // 5x4
        }
    }

    @Test
    public void testWallDestruction() {
        double maxHealth = wall.getMaxHealth();
        wall.takeDamage(maxHealth + 10);
        
        assertTrue(wall.isDestroyed());
    }
}
