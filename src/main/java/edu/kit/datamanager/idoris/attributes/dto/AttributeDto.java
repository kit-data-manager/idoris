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
package edu.kit.datamanager.idoris.attributes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Data Transfer Object for Attribute as a Java record.
 * Contains only user-defined information and identifiers for relationships.
 * <p>
 * Note: pidLink has been removed from DTOs. Use HATEOAS links to /pid/{pid} instead.
 */
@Builder
@Schema(name = "Attribute", description = "DTO representing an Attribute")
public record AttributeDto(
        @Schema(description = "Internal ID (internal use only)") String internalId,
        @Schema(description = "Name of the attribute") String name,
        @Schema(description = "Description of the attribute") String description,
        @Schema(description = "Default value") String defaultValue,
        @Schema(description = "Constant value (if fixed)") String constantValue,
        @Schema(description = "Lower bound cardinality") Integer lowerBoundCardinality,
        @Schema(description = "Upper bound cardinality") Integer upperBoundCardinality,
        @Schema(description = "ID of the DataType node") String dataTypeId,
        @Schema(description = "ID of an Attribute this one overrides") String overrideId
) {
    // JavaBean-style getters for compatibility with existing code/tests
    public String getInternalId() {
        return internalId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public Integer getLowerBoundCardinality() {
        return lowerBoundCardinality;
    }

    public Integer getUpperBoundCardinality() {
        return upperBoundCardinality;
    }

    public String getDataTypeId() {
        return dataTypeId;
    }

    public String getOverrideId() {
        return overrideId;
    }
}
