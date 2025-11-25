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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the inheritance tree structure of a TypeProfile.
 * This class provides information about the inheritance hierarchy
 * of a TypeProfile, including all parent profiles it inherits from.
 */
@SuppressWarnings("StringConcatToTextBlock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "TypeProfile inheritance tree structure")
public class TypeProfileInheritance {

    /**
     * The root TypeProfile ID (the one whose inheritance tree is being queried).
     */
    @JsonProperty("rootId")
    @Schema(description = "The ID of the root TypeProfile", example = "tp-12345")
    private String rootId;

    /**
     * The name of the root TypeProfile.
     */
    @JsonProperty("rootName")
    @Schema(description = "The name of the root TypeProfile", example = "PersonProfile")
    private String rootName;

    /**
     * List of parent TypeProfiles that this TypeProfile inherits from.
     * This includes all levels of the inheritance hierarchy.
     */
    @JsonProperty("inheritsFrom")
    @Schema(description = "List of parent TypeProfiles in the inheritance hierarchy")
    private List<TypeProfileNode> inheritsFrom;

    /**
     * Represents a node in the inheritance tree.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "A node in the TypeProfile inheritance tree")
    public static class TypeProfileNode {

        /**
         * The unique identifier of the TypeProfile.
         */
        @JsonProperty("id")
        @Schema(description = "The unique identifier of the TypeProfile", example = "tp-67890")
        private String id;

        /**
         * The name of the TypeProfile.
         */
        @JsonProperty("name")
        @Schema(description = "The name of the TypeProfile", example = "BaseProfile")
        private String name;

        /**
         * The description of the TypeProfile.
         */
        @JsonProperty("description")
        @Schema(description = "The description of the TypeProfile", example = "Base profile for all entities")
        private String description;

        /**
         * The level in the inheritance hierarchy (0 = direct parent, 1 = grandparent, etc.).
         */
        @JsonProperty("level")
        @Schema(description = "The inheritance level (0 = direct parent)", example = "0")
        private int level;

        /**
         * The attributes defined directly on this TypeProfile (not inherited).
         */
        @JsonProperty("directAttributes")
        @Schema(description = "Attributes defined directly on this TypeProfile")
        private List<String> directAttributes;
    }
}
