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
package edu.kit.datamanager.idoris.datatypes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base DTO for all DataType entities.
 * Uses polymorphic serialization to handle different DataType subtypes.
 */
@SuppressWarnings("StringConcatToTextBlock")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AtomicDataTypeDto.class, name = "ATOMIC"),
        @JsonSubTypes.Type(value = TypeProfileDto.class, name = "PROFILE")
})
@Schema(description = "Base DTO for DataType entities",
        discriminatorProperty = "type",
        subTypes = {AtomicDataTypeDto.class, TypeProfileDto.class})
public abstract class DataTypeDto {

    /**
     * The internal identifier of the DataType.
     */
    @JsonProperty("internalId")
    @Schema(description = "Internal identifier of the DataType", example = "dt-12345")
    private String internalId;

    /**
     * The name of the DataType.
     */
    @JsonProperty("name")
    @Schema(description = "Name of the DataType", example = "PersonType")
    private String name;

    /**
     * The description of the DataType.
     */
    @JsonProperty("description")
    @Schema(description = "Description of the DataType", example = "Data type for person entities")
    private String description;

    /**
     * The version of this DataType (for optimistic locking).
     */
    @JsonProperty("version")
    @Schema(description = "Version number for optimistic locking", example = "1")
    private Long version;

    /**
     * Link to the PID endpoint for this DataType.
     */
    @JsonProperty("pidLink")
    @Schema(description = "Link to the PID endpoint", example = "/pid/dt-12345")
    private String pidLink;

    /**
     * The type discriminator for polymorphic serialization.
     */
    @JsonProperty("type")
    @Schema(description = "Type discriminator (ATOMIC or PROFILE)", example = "PROFILE")
    public abstract String getType();
}
