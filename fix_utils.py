# Простой скрипт для добавления функций люка
with open('src/main/java/com/constructions/utils/StructurePlacementUtils.java', 'r', encoding='utf-8-sig') as f:
    content = f.read()

# Проверяем что файл начинается с package
if not content.strip().startswith('package'):
    print("Файл испорчен, нужна ручная исправка")
else:
    # Добавляем импорт  
    if 'RoofHoleTrapdoorStructure' not in content:
        content = content.replace(
            'import com.constructions.structures.RoofHoleStructure;',
            'import com.constructions.structures.RoofHoleStructure;\nimport com.constructions.structures.RoofHoleTrapdoorStructure;'
        )
    
    # Добавляем case в createStructure
    if 'case "roof_hole_trapdoor"' not in content:
        content = content.replace(
            'case "roof_hole" -> new RoofHoleStructure(resolvedBasePosition, owner);',
            'case "roof_hole" -> new RoofHoleStructure(resolvedBasePosition, owner);\n            case "roof_hole_trapdoor" -> new RoofHoleTrapdoorStructure(resolvedBasePosition, owner);'
        )
    
    # Добавляем case в getPlacementBlock
    if 'case "roof_hole_trapdoor"' not in content:
        content = content.replace(
            'case "floor_ladder", "floor_ladder_no_support" -> Blocks.LADDER;',
            'case "floor_ladder", "floor_ladder_no_support" -> Blocks.LADDER;\n            case "roof_hole_trapdoor" -> Blocks.OAK_TRAPDOOR;'
        )
    
    with open('src/main/java/com/constructions/utils/StructurePlacementUtils.java', 'w', encoding='utf-8') as f:
        f.write(content)
    print("Файл восстановлен")
