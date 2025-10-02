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
import edu.kit.datamanager.idoris.core.domain.enums.PrimitiveDataTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * DTO for AtomicDataType exposing user-defined fields and relationship IDs.
 */
@SuppressWarnings("StringConcatToTextBlock")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AtomicDataType", description = "DTO representing an Atomic Data Type")
public class AtomicDataTypeDto extends DataTypeDto {

    @JsonProperty("defaultValue")
    @Schema(description = "Default value")
    private String defaultValue;
    @JsonProperty("inheritsFromId")
    @Schema(description = "ID of parent AtomicDataType this one inherits from")
    private String inheritsFromId;
    @JsonProperty("primitiveDataType")
    @Schema(description = "Primitive data type")
    private PrimitiveDataTypes primitiveDataType;
    @JsonProperty("regularExpression")
    @Schema(description = "Regular expression constraint")
    private String regularExpression;
    @JsonProperty("permittedValues")
    @Schema(description = "Permitted values")
    private Set<String> permittedValues;
    @JsonProperty("forbiddenValues")
    @Schema(description = "Forbidden values")
    private Set<String> forbiddenValues;
    @JsonProperty("minimum")
    @Schema(description = "Minimum")
    private Integer minimum;
    @JsonProperty("maximum")
    @Schema(description = "Maximum")
    private Integer maximum;

    @Override
    public String getType() {
        return "ATOMIC";
    }
}
