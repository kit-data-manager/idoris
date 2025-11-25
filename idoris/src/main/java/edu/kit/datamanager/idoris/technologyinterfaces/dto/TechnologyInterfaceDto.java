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
package edu.kit.datamanager.idoris.technologyinterfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

/**
 * Data Transfer Object for TechnologyInterface as a Java record.
 * Contains only user-defined information and identifiers for relationships.
 * Note: pidLink removed; use HATEOAS link to /pid/{pid}.
 */
@Builder
@Schema(name = "TechnologyInterface", description = "DTO representing a Technology Interface")
public record TechnologyInterfaceDto(
        @Schema(description = "Internal ID (internal use only)") String internalId,
        @Schema(description = "Name of the technology interface") String name,
        @Schema(description = "Description of the technology interface") String description,
        @Schema(description = "Adapter identifiers supported by this interface") Set<String> adapters,
        @Schema(description = "IDs of input Attribute nodes") Set<String> attributeIds,
        @Schema(description = "IDs of output Attribute nodes") Set<String> outputIds
) {
    // JavaBean-style getters for compatibility
    public String getInternalId() {
        return internalId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public java.util.Set<String> getAdapters() {
        return adapters;
    }

    public java.util.Set<String> getAttributeIds() {
        return attributeIds;
    }

    public java.util.Set<String> getOutputIds() {
        return outputIds;
    }
}
