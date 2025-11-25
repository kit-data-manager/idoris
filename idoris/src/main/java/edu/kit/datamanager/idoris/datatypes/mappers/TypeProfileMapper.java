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

import edu.kit.datamanager.idoris.core.domain.Attribute;
import edu.kit.datamanager.idoris.core.domain.TypeProfile;
import edu.kit.datamanager.idoris.core.domain.valueObjects.Name;
import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileDto;
import io.micrometer.observation.annotation.Observed;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for TypeProfile entity and DTO.
 * - Convert entity -> DTO exposing user fields and relationship IDs
 * - Convert DTO -> entity (scalar fields only)
 * - Apply partial updates (patch semantics) for scalar fields
 */
@Observed(contextualName = "typeProfileMapper")
@org.springframework.stereotype.Component
public class TypeProfileMapper {

    public TypeProfileDto toDto(TypeProfile entity) {
        if (entity == null) return null;
        Set<String> inheritsFromIds = toIds(entity.getInheritsFrom());
        Set<String> attributeIds = toIdsAttr(entity.getAttributes());
        return TypeProfileDto.builder()
                .internalId(entity.getId())
                .name(entity.getName().toString())
                .description(entity.getDescription().toString())
                .defaultValue(entity.getDefaultValue())
                .inheritsFromIds(inheritsFromIds)
                .attributeIds(attributeIds)
                .permitEmbedding(entity.isPermitEmbedding())
                .isAbstract(entity.isAbstract())
                .allowAdditionalAttributes(entity.isAllowAdditionalAttributes())
                .validationPolicy(entity.getValidationPolicy())
                .build();
    }

    private Set<String> toIds(Set<TypeProfile> profiles) {
        if (profiles == null) return Collections.emptySet();
        return profiles.stream()
                .filter(Objects::nonNull)
                .map(TypeProfile::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<String> toIdsAttr(Set<Attribute> attributes) {
        if (attributes == null) return Collections.emptySet();
        return attributes.stream()
                .filter(Objects::nonNull)
                .map(Attribute::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public TypeProfile toEntity(TypeProfileDto dto) {
        if (dto == null) return null;
        TypeProfile entity = new TypeProfile();
        entity.setName(new Name(dto.getName()));
        entity.setDescription(new Name(dto.getDescription()));
        entity.setDefaultValue(dto.getDefaultValue());
        if (dto.getPermitEmbedding() != null) entity.setPermitEmbedding(dto.getPermitEmbedding());
        if (dto.getIsAbstract() != null) entity.setAbstract(dto.getIsAbstract());
        if (dto.getAllowAdditionalAttributes() != null)
            entity.setAllowAdditionalAttributes(dto.getAllowAdditionalAttributes());
        if (dto.getValidationPolicy() != null) entity.setValidationPolicy(dto.getValidationPolicy());
        // Relationships (inheritsFrom, attributes) are linked via logic/DAO using IDs
        return entity;
    }

    public TypeProfile applyPatch(TypeProfileDto dto, TypeProfile entity) {
        if (dto == null || entity == null) return entity;
        if (dto.getName() != null) entity.setName(new Name(dto.getName()));
        if (dto.getDescription() != null) entity.setDescription(new Name(dto.getDescription()));
        if (dto.getDefaultValue() != null) entity.setDefaultValue(dto.getDefaultValue());
        if (dto.getPermitEmbedding() != null) entity.setPermitEmbedding(dto.getPermitEmbedding());
        if (dto.getIsAbstract() != null) entity.setAbstract(dto.getIsAbstract());
        if (dto.getAllowAdditionalAttributes() != null)
            entity.setAllowAdditionalAttributes(dto.getAllowAdditionalAttributes());
        if (dto.getValidationPolicy() != null) entity.setValidationPolicy(dto.getValidationPolicy());
        // Relationship IDs handled elsewhere
        return entity;
    }
}
