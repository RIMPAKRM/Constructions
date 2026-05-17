package com.constructions.structures;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

/**
 * Тесты для менеджера структур
 */
public class StructureManagerTests {
    private StructureManager manager;
    private UUID testPlayerId;
    private FoundationStructure testStructure;

    @BeforeEach
    public void setUp() {
        manager = new StructureManager();
        testPlayerId = UUID.randomUUID();
        testStructure = new FoundationStructure(new BlockPos(0, 64, 0), testPlayerId);
    }

    @Test
    public void testAddStructure() {
        manager.addStructure(testStructure);
        
        Structure retrieved = manager.getStructure(testStructure.getStructureId());
        assertNotNull(retrieved);
        assertEquals(testStructure.getStructureId(), retrieved.getStructureId());
    }

    @Test
    public void testRemoveStructure() {
        manager.addStructure(testStructure);
        manager.removeStructure(testStructure.getStructureId());
        
        Structure retrieved = manager.getStructure(testStructure.getStructureId());
        assertNull(retrieved);
    }

    @Test
    public void testGetPlayerStructures() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        FoundationStructure struct1 = new FoundationStructure(new BlockPos(0, 64, 0), player1);
        FoundationStructure struct2 = new FoundationStructure(new BlockPos(100, 64, 100), player1);
        FoundationStructure struct3 = new FoundationStructure(new BlockPos(200, 64, 200), player2);
        
        manager.addStructure(struct1);
        manager.addStructure(struct2);
        manager.addStructure(struct3);
        
        List<Structure> player1Structures = manager.getPlayerStructures(player1);
        assertEquals(2, player1Structures.size());
        
        List<Structure> player2Structures = manager.getPlayerStructures(player2);
        assertEquals(1, player2Structures.size());
    }

    @Test
    public void testGetStructuresInRadius() {
        BlockPos center = new BlockPos(0, 64, 0);
        
        FoundationStructure close1 = new FoundationStructure(new BlockPos(5, 64, 5), testPlayerId);
        FoundationStructure close2 = new FoundationStructure(new BlockPos(-5, 64, -5), testPlayerId);
        FoundationStructure far = new FoundationStructure(new BlockPos(200, 64, 200), testPlayerId);
        
        manager.addStructure(close1);
        manager.addStructure(close2);
        manager.addStructure(far);
        
        List<Structure> nearby = manager.getStructuresInRadius(center, 20);
        assertEquals(2, nearby.size());
    }

    @Test
    public void testBlocksOccupied() {
        manager.addStructure(testStructure);
        
        // Получить блоки первой структуры
        var blockPositions = testStructure.getBlockPositions();
        
        assertTrue(manager.areBlocksOccupied(blockPositions));
        
        // Проверить, что свободные блоки не занимаются
        assertFalse(manager.areBlocksOccupied(java.util.Set.of(new BlockPos(1000, 64, 1000))));
    }

    @Test
    public void testDamageStructure() {
        manager.addStructure(testStructure);
        double initialHealth = testStructure.getCurrentHealth();
        
        manager.damageStructure(testStructure.getStructureId(), 30);
        
        Structure updated = manager.getStructure(testStructure.getStructureId());
        assertEquals(initialHealth - 30, updated.getCurrentHealth());
    }

    @Test
    public void testDestroyStructure() {
        manager.addStructure(testStructure);
        double maxHealth = testStructure.getMaxHealth();
        
        manager.damageStructure(testStructure.getStructureId(), maxHealth + 10);
        
        Structure destroyed = manager.getStructure(testStructure.getStructureId());
        assertNull(destroyed); // Структура удалена
    }

    @Test
    public void testMultipleStructures() {
        for (int i = 0; i < 10; i++) {
            FoundationStructure struct = new FoundationStructure(
                    new BlockPos(i * 100, 64, 0), 
                    UUID.randomUUID()
            );
            manager.addStructure(struct);
        }
        
        // Все структуры должны быть сохранены
        assertEquals(10, manager.getStructureCount());
    }
}
