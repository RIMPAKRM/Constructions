#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re

# Читаем файл
with open('src/main/java/com/constructions/utils/StructurePlacementUtils.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Находим и заменяем hasRoofHoleBelow
in_ramp_support = False
new_lines = []
i = 0

while i < len(lines):
    line = lines[i]
    
    # Ищем функцию hasRoofHoleBelow
    if 'private static boolean hasRoofHoleBelow' in line:
        # Копируем до строки с проверкой лестницы
        while i < len(lines) and '// Проверка 2: прямо под люком должна быть лестница' not in lines[i]:
            new_lines.append(lines[i])
            i += 1
        
        # Пропускаем старый код проверки лестницы
        if i < len(lines):
            new_lines.append(lines[i])  # Коммент
            i += 1
            # Пропускаем BlockState belowState = ...
            while i < len(lines) and 'BlockState belowState' not in lines[i]:
                i += 1
            if i < len(lines):
                i += 1
            # Пропускаем if (!belowState.is(Blocks.LADDER))
            while i < len(lines) and 'if (!belowState.is(Blocks.LADDER))' not in lines[i]:
                i += 1
            if i < len(lines):
                i += 1
                # Пропускаем return false;
                if i < len(lines) and 'return false;' in lines[i]:
                    i += 1
                # Пропускаем }
                if i < len(lines) and '}' in lines[i]:
                    i += 1
            
            # Вставляем новый код
            new_lines.append('        BlockState belowState = level.getBlockState(belowTrapdoor);\n')
            new_lines.append('        // Можно ставить над лестницей ИЛИ над крышей\n')
            new_lines.append('        if (!belowState.is(Blocks.LADDER)) {\n')
            new_lines.append('            // Если не лестница - проверяем есть ли крыша с проёмом\n')
            new_lines.append('            if (findRoofHoleBase(level, belowTrapdoor) == null) {\n')
            new_lines.append('                return false;\n')
            new_lines.append('            }\n')
            new_lines.append('            return true;\n')
            new_lines.append('        }\n')
            new_lines.append('\n')
    else:
        new_lines.append(line)
    
    i += 1

# Записываем обратно
with open('src/main/java/com/constructions/utils/StructurePlacementUtils.java', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print('Fixed hasRoofHoleBelow')
