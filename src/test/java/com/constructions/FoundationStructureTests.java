package com.constructions.structures;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

/**
 * Тесты для структуры фундамента
 */
public class FoundationStructureTests {
    private FoundationStructure foundation;
    private UUID testPlayerId;
    private BlockPos testPosition;

    @BeforeEach
    public void setUp() {
        testPlayerId = UUID.randomUUID();
        testPosition = new BlockPos(0, 64, 0);
        foundation = new FoundationStructure(testPosition, testPlayerId);
    }

    @Test
    public void testFoundationCreation() {
        assertNotNull(foundation);
        assertEquals("foundation", foundation.getStructureType());
        assertEquals(testPlayerId, foundation.getOwner());
        assertEquals(testPosition, foundation.getBasePosition());
    }

    @Test
    public void testFoundationHealth() {
        double initialHealth = foundation.getCurrentHealth();
        assertTrue(initialHealth > 0);
        assertEquals(foundation.getMaxHealth(), initialHealth);
        
        foundation.takeDamage(10);
        assertEquals(initialHealth - 10, foundation.getCurrentHealth());
        
        foundation.takeDamage(initialHealth);
        assertTrue(foundation.isDestroyed());
    }

    @Test
    public void testPillarHeights() {
        int[] heights = new int[]{0, 1, 1, 2};
        foundation.setPillarHeights(heights);
        assertArrayEquals(heights, foundation.getPillarHeights());
    }

    @Test
    public void testInvalidPillarHeight() {
        assertThrows(IllegalArgumentException.class, () -> {
            foundation.setPillarHeights(new int[]{0, 1, 1, 5}); // 5 > MAX (3)
        });
    }

    @Test
    public void testStructureBlocks() {
        foundation.setPillarHeights(new int[]{0, 0, 0, 0});
        List<BlockPos> blocks = foundation.getStructureBlocks();
        
        assertNotNull(blocks);
        assertTrue(blocks.size() > 0);
        
        // Проверить, что блоки находятся в правильном диапазоне
        for (BlockPos pos : blocks) {
            assertTrue(Math.abs(pos.getX() - testPosition.getX()) <= 4);
            assertTrue(Math.abs(pos.getZ() - testPosition.getZ()) <= 4);
        }
    }

    @Test
    public void testAttachedStructures() {
        UUID attachedId = UUID.randomUUID();
        foundation.attachStructure(attachedId);
        
        assertTrue(foundation.getAttachedStructures().contains(attachedId));
        
        foundation.detachStructure(attachedId);
        assertFalse(foundation.getAttachedStructures().contains(attachedId));
    }

    @Test
    public void testSerialize() {
        foundation.setPillarHeights(new int[]{0, 1, 1, 2});
        foundation.takeDamage(25);
        
        var tag = foundation.serializeNBT();
        
        assertNotNull(tag);
        assertEquals("foundation", tag.getString("type"));
        assertEquals(foundation.getStructureId().toString(), tag.getString("id"));
        assertEquals(testPlayerId.toString(), tag.getString("owner"));
        assertEquals(foundation.getCurrentHealth(), tag.getDouble("health"));
    }
}
