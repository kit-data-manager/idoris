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
import edu.kit.datamanager.idoris.core.domain.enums.CombinationOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * DTO for TypeProfile exposing user-defined fields and relationship IDs.
 */
@SuppressWarnings("StringConcatToTextBlock")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TypeProfile", description = "DTO representing a Type Profile")
public class TypeProfileDto extends DataTypeDto {

    @JsonProperty("defaultValue")
    @Schema(description = "Default value")
    private String defaultValue;
    @JsonProperty("inheritsFromIds")
    @Schema(description = "IDs of parent TypeProfiles this one inherits from")
    private Set<String> inheritsFromIds;
    @JsonProperty("attributeIds")
    @Schema(description = "IDs of Attribute nodes included in this profile")
    private Set<String> attributeIds;
    @JsonProperty("permitEmbedding")
    @Schema(description = "Whether embedding is permitted")
    private Boolean permitEmbedding;
    @JsonProperty("isAbstract")
    @Schema(description = "Whether this profile is abstract")
    private Boolean isAbstract;
    @JsonProperty("allowAdditionalAttributes")
    @Schema(description = "Whether additional attributes are allowed")
    private Boolean allowAdditionalAttributes;
    @JsonProperty("validationPolicy")
    @Schema(description = "Validation policy for combining attributes")
    private CombinationOptions validationPolicy;

    @Override
    public String getType() {
        return "PROFILE";
    }
}
