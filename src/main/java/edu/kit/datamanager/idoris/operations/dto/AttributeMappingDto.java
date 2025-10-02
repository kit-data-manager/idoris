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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO for AttributeMapping used inside OperationStep nested creation.
 */
@Builder
@Schema(name = "AttributeMapping", description = "Attribute mapping definition for a step")
public record AttributeMappingDto(
        @Schema(description = "Internal ID (server-managed)") String internalId,
        @Schema(description = "Name of the mapping") String name,
        @Schema(description = "PID or ID of input Attribute") String inputAttributeId,
        @Schema(description = "Replacement template / expression") String replaceCharactersInValueWithInput,
        @Schema(description = "Static value (optional)") String value,
        @Schema(description = "Index for ordering") Integer index,
        @Schema(description = "PID or ID of output Attribute") String outputAttributeId
) {
    public String getInternalId() {
        return internalId;
    }

    public String getName() {
        return name;
    }

    public String getInputAttributeId() {
        return inputAttributeId;
    }

    public String getReplaceCharactersInValueWithInput() {
        return replaceCharactersInValueWithInput;
    }

    public String getValue() {
        return value;
    }

    public Integer getIndex() {
        return index;
    }

    public String getOutputAttributeId() {
        return outputAttributeId;
    }
}
