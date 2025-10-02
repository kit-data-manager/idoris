/*
 * Copyright (c) 2025 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.idoris.datatypes.mappers;

import edu.kit.datamanager.idoris.core.domain.AtomicDataType;
import edu.kit.datamanager.idoris.core.domain.valueObjects.Name;
import edu.kit.datamanager.idoris.datatypes.dto.AtomicDataTypeDto;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Component;

/**
 * Mapper for AtomicDataType entity and DTO.
 * - Convert entity -> DTO exposing user fields and relationship IDs
 * - Convert DTO -> entity (scalar fields only)
 * - Apply partial updates (patch semantics) for scalar fields
 */
@Observed(contextualName = "atomicDataTypeMapper")
@Component
public class AtomicDataTypeMapper {

    public AtomicDataTypeDto toDto(AtomicDataType entity) {
        if (entity == null) return null;
        String inheritsFromId = entity.getInheritsFrom() != null ? entity.getInheritsFrom().getId() : null;
        return AtomicDataTypeDto.builder()
                .internalId(entity.getId())
                .name(entity.getName().toString())
                .description(entity.getDescription().toString())
                .defaultValue(entity.getDefaultValue())
                .inheritsFromId(inheritsFromId)
                .primitiveDataType(entity.getPrimitiveDataType())
                .regularExpression(entity.getRegularExpression())
                .permittedValues(entity.getPermittedValues())
                .forbiddenValues(entity.getForbiddenValues())
                .minimum(entity.getMinimum())
                .maximum(entity.getMaximum())
                .build();
    }

    public AtomicDataType toEntity(AtomicDataTypeDto dto) {
        if (dto == null) return null;
        AtomicDataType entity = new AtomicDataType();
        entity.setName(new Name(dto.getName()));
        entity.setDescription(new Name(dto.getDescription()));
        entity.setDefaultValue(dto.getDefaultValue());
        entity.setPrimitiveDataType(dto.getPrimitiveDataType());
        entity.setRegularExpression(dto.getRegularExpression());
        entity.setPermittedValues(dto.getPermittedValues());
        entity.setForbiddenValues(dto.getForbiddenValues());
        entity.setMinimum(dto.getMinimum());
        entity.setMaximum(dto.getMaximum());
        // Relationship inheritsFrom is set via logic/DAO using ID
        return entity;
    }

    public AtomicDataType applyPatch(AtomicDataTypeDto dto, AtomicDataType entity) {
        if (dto == null || entity == null) return entity;
        if (dto.getName() != null) entity.setName(new Name(dto.getName()));
        if (dto.getDescription() != null) entity.setDescription(new Name(dto.getDescription()));
        if (dto.getDefaultValue() != null) entity.setDefaultValue(dto.getDefaultValue());
        if (dto.getPrimitiveDataType() != null) entity.setPrimitiveDataType(dto.getPrimitiveDataType());
        if (dto.getRegularExpression() != null) entity.setRegularExpression(dto.getRegularExpression());
        if (dto.getPermittedValues() != null) entity.setPermittedValues(dto.getPermittedValues());
        if (dto.getForbiddenValues() != null) entity.setForbiddenValues(dto.getForbiddenValues());
        if (dto.getMinimum() != null) entity.setMinimum(dto.getMinimum());
        if (dto.getMaximum() != null) entity.setMaximum(dto.getMaximum());
        // Relationship ID handled elsewhere
        return entity;
    }
}
