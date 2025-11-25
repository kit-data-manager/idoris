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

import edu.kit.datamanager.idoris.core.domain.enums.ExecutionMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * DTO for OperationStep used for nested creation and listing.
 */
@Builder
@Schema(name = "OperationStep", description = "Operation step definition")
public record OperationStepDto(
        @Schema(description = "Internal ID (server-managed)") String internalId,
        @Schema(description = "Index (order) of the step within an Operation") Integer index,
        @Schema(description = "Name of the step") String name,
        @Schema(description = "Execution mode") ExecutionMode mode,
        @Schema(description = "PID or ID of an Operation executed by this step (optional)") String executeOperationId,
        @Schema(description = "PID or ID of TechnologyInterface used by this step (optional)") String useTechnologyId,
        @Schema(description = "Input AttributeMappings") List<AttributeMappingDto> inputMappings,
        @Schema(description = "Output AttributeMappings") List<AttributeMappingDto> outputMappings,
        @Schema(description = "Nested sub-steps") List<OperationStepDto> subSteps
) {
    // JavaBean getters for compatibility
    public String getInternalId() {
        return internalId;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ExecutionMode getMode() {
        return mode;
    }

    public String getExecuteOperationId() {
        return executeOperationId;
    }

    public String getUseTechnologyId() {
        return useTechnologyId;
    }

    public List<AttributeMappingDto> getInputMappings() {
        return inputMappings;
    }

    public List<AttributeMappingDto> getOutputMappings() {
        return outputMappings;
    }

    public List<OperationStepDto> getSubSteps() {
        return subSteps;
    }
}
