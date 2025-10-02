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
package edu.kit.datamanager.idoris.attributes.mappers;

import edu.kit.datamanager.idoris.attributes.dto.AttributeDto;
import edu.kit.datamanager.idoris.core.domain.Attribute;
import edu.kit.datamanager.idoris.core.domain.valueObjects.Name;
import io.micrometer.observation.annotation.Observed;

/**
 * Mapper for Attribute entity and DTO.
 * - Convert entity -> DTO exposing user fields and relationship IDs
 * - Convert DTO -> entity (scalar fields only)
 * - Apply partial updates (patch semantics) for scalar fields
 */
@Observed(contextualName = "attributeMapper")
@org.springframework.stereotype.Component
public class AttributeMapper {

    public AttributeDto toDto(Attribute entity) {
        if (entity == null) return null;
        String dataTypeId = entity.getDataTypeId();
        String overrideId = entity.getOverride() != null ? entity.getOverride().getId() : null;
        return AttributeDto.builder()
                .internalId(entity.getId())
                .name(entity.getName().toString())
                .description(entity.getDescription().toString())
                .defaultValue(entity.getDefaultValue())
                .constantValue(entity.getConstantValue())
                .lowerBoundCardinality(entity.getLowerBoundCardinality())
                .upperBoundCardinality(entity.getUpperBoundCardinality())
                .dataTypeId(dataTypeId)
                .overrideId(overrideId)
                .build();
    }

    public Attribute toEntity(AttributeDto dto) {
        if (dto == null) return null;
        Attribute entity = new Attribute();
        entity.setName(new Name(dto.getName()));
        entity.setDescription(new Name(dto.getDescription()));
        entity.setDefaultValue(dto.getDefaultValue());
        entity.setConstantValue(dto.getConstantValue());
        entity.setLowerBoundCardinality(dto.getLowerBoundCardinality());
        entity.setUpperBoundCardinality(dto.getUpperBoundCardinality());
        // Relationships (dataType/override) are linked via logic/DAO using IDs.
        return entity;
    }

    public Attribute applyPatch(AttributeDto dto, Attribute entity) {
        if (dto == null || entity == null) return entity;
        if (dto.getName() != null) entity.setName(new Name(dto.getName()));
        if (dto.getDescription() != null) entity.setDescription(new Name(dto.getDescription()));
        if (dto.getDefaultValue() != null) entity.setDefaultValue(dto.getDefaultValue());
        if (dto.getConstantValue() != null) entity.setConstantValue(dto.getConstantValue());
        if (dto.getLowerBoundCardinality() != null) entity.setLowerBoundCardinality(dto.getLowerBoundCardinality());
        if (dto.getUpperBoundCardinality() != null) entity.setUpperBoundCardinality(dto.getUpperBoundCardinality());
        // Relationship IDs handled elsewhere
        return entity;
    }
}
