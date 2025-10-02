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
package edu.kit.datamanager.idoris.operations.mappers;

import edu.kit.datamanager.idoris.core.domain.Operation;
import edu.kit.datamanager.idoris.core.domain.valueObjects.Name;
import edu.kit.datamanager.idoris.operations.dto.OperationRequestDto;
import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
import io.micrometer.observation.annotation.Observed;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Observed(contextualName = "operationMapper")
@org.springframework.stereotype.Component
public class OperationMapper {

    public OperationResponseDto toResponseDto(Operation entity) {
        if (entity == null) return null;
        String executableOnId = entity.getExecutableOn() != null ? entity.getExecutableOn().getId() : null;
        Set<String> returnIds = entity.getReturns() == null ? Collections.emptySet() : entity.getReturns().stream()
                .filter(Objects::nonNull)
                .map(a -> a.getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> envIds = entity.getEnvironment() == null ? Collections.emptySet() : entity.getEnvironment().stream()
                .filter(Objects::nonNull)
                .map(a -> a.getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        // Execution steps are complex; expose only IDs if present
        java.util.List<String> stepIds = entity.getExecution() == null ? java.util.List.of() : entity.getExecution().stream()
                .filter(Objects::nonNull)
                .map(s -> s.getId())
                .filter(Objects::nonNull)
                .toList();
        return OperationResponseDto.builder()
                .internalId(entity.getId())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .lastModifiedAt(entity.getLastModifiedAt())
                .name(entity.getName().toString())
                .description(entity.getDescription().toString())
                .executableOnAttributeId(executableOnId)
                .returnAttributeIds(returnIds)
                .environmentAttributeIds(envIds)
                .executionStepIds(stepIds)
                .build();
    }

    public Operation toEntity(OperationRequestDto dto) {
        if (dto == null) return null;
        Operation entity = new Operation();
        entity.setName(new Name(dto.getName()));
        entity.setDescription(new Name(dto.getDescription()));
        // Relationship linking by IDs is handled in DAO/logic layer if needed
        return entity;
    }

    public Operation applyPatch(OperationRequestDto dto, Operation entity) {
        if (dto == null || entity == null) return entity;
        if (dto.getName() != null) entity.setName(new Name(dto.getName()));
        if (dto.getDescription() != null) entity.setDescription(new Name(dto.getDescription()));
        // Relationship IDs handled elsewhere
        return entity;
    }
}
