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
package edu.kit.datamanager.idoris.technologyinterfaces.mappers;

import edu.kit.datamanager.idoris.core.domain.Attribute;
import edu.kit.datamanager.idoris.core.domain.TechnologyInterface;
import edu.kit.datamanager.idoris.core.domain.valueObjects.Name;
import edu.kit.datamanager.idoris.technologyinterfaces.dto.TechnologyInterfaceDto;
import io.micrometer.observation.annotation.Observed;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for TechnologyInterface entity and DTO.
 * <p>
 * Responsibilities:
 * - Convert entity -> DTO, exposing only user-defined fields and relationship IDs.
 * - Convert DTO -> entity (scalar fields only). Relationship linking (attributes/outputs)
 * is orchestrated by services via repositories.
 * - Apply partial updates from DTO to existing entity (patch semantics) for scalar fields.
 */
@Observed(contextualName = "technologyInterfaceMapper")
@org.springframework.stereotype.Component
public class TechnologyInterfaceMapper {

    /**
     * Converts a TechnologyInterface entity to a DTO.
     *
     * @param entity TechnologyInterface entity (may be null)
     * @return DTO or null if input is null
     */
    public TechnologyInterfaceDto toDto(TechnologyInterface entity) {
        if (entity == null) return null;
        Set<String> attributeIds = toIds(entity.getAttributes());
        Set<String> outputIds = toIds(entity.getOutputs());
        return TechnologyInterfaceDto.builder()
                .internalId(entity.getId())
                .name(entity.getName().toString())
                .description(entity.getDescription().toString())
                .adapters(entity.getAdapters())
                .attributeIds(attributeIds)
                .outputIds(outputIds)
                .build();
    }

    private Set<String> toIds(Set<Attribute> attributes) {
        if (attributes == null) return Collections.emptySet();
        return attributes.stream()
                .filter(Objects::nonNull)
                .map(Attribute::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Creates a new TechnologyInterface entity from DTO. Only scalar fields are set.
     * Relationship IDs from the DTO must be linked in the logic layer.
     *
     * @param dto DTO (may be null)
     * @return new entity or null
     */
    public TechnologyInterface toEntity(TechnologyInterfaceDto dto) {
        if (dto == null) return null;
        TechnologyInterface entity = new TechnologyInterface();
        // id is managed by persistence; do not set internalId directly from dto.id
        entity.setName(new Name(dto.getName()));
        entity.setDescription(new Name(dto.getDescription()));
        if (dto.getAdapters() != null) {
            entity.setAdapters(dto.getAdapters());
        }
        return entity;
    }

    /**
     * Applies non-null scalar fields from DTO to an existing entity (patch semantics).
     * Relationship changes are not handled here.
     *
     * @param dto    DTO with fields to apply
     * @param entity existing entity to mutate
     * @return the same entity instance for chaining
     */
    public TechnologyInterface applyPatch(TechnologyInterfaceDto dto, TechnologyInterface entity) {
        if (dto == null || entity == null) return entity;
        if (dto.getName() != null) {
            entity.setName(new Name(dto.getName()));
        }
        if (dto.getDescription() != null) {
            entity.setDescription(new Name(dto.getDescription()));
        }
        if (dto.getAdapters() != null) {
            entity.setAdapters(dto.getAdapters());
        }
        return entity;
    }
}
