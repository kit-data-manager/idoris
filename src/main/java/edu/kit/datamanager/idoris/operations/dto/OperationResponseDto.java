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
package edu.kit.datamanager.idoris.operations.dto;

import edu.kit.datamanager.idoris.core.AdministrativeMetadataDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Response DTO for Operation. Extends the administrative metadata contract.
 */
@Builder
@Schema(name = "OperationResponse", description = "Operation response resource")
public record OperationResponseDto(
        @Schema(description = "Internal ID (server-managed)") String internalId,
        @Schema(description = "Entity version") Long version,
        @Schema(description = "Creation timestamp") Instant createdAt,
        @Schema(description = "Last modification timestamp") Instant lastModifiedAt,
        @Schema(description = "Name") String name,
        @Schema(description = "Description") String description,
        @Schema(description = "ID of Attribute this operation can execute on") String executableOnAttributeId,
        @Schema(description = "IDs of return Attributes") Set<String> returnAttributeIds,
        @Schema(description = "IDs of environment Attributes") Set<String> environmentAttributeIds,
        @Schema(description = "IDs of execution step nodes (if any)") List<String> executionStepIds,
        @Schema(description = "Full execution step definitions for nested creation (optional)") List<OperationStepDto> executionSteps
) implements AdministrativeMetadataDto {
    // JavaBean getters for compatibility
    public String getInternalId() {
        return internalId;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getExecutableOnAttributeId() {
        return executableOnAttributeId;
    }

    public Set<String> getReturnAttributeIds() {
        return returnAttributeIds;
    }

    public Set<String> getEnvironmentAttributeIds() {
        return environmentAttributeIds;
    }

    public List<String> getExecutionStepIds() {
        return executionStepIds;
    }

    public List<OperationStepDto> getExecutionSteps() {
        return executionSteps;
    }
}