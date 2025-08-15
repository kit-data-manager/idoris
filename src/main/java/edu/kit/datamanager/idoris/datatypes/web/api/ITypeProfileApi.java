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

package edu.kit.datamanager.idoris.datatypes.web.api;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.datatypes.entities.TypeProfile;
import edu.kit.datamanager.idoris.datatypes.web.v1.TypeProfileController.TypeProfileInheritance;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API interface for TypeProfile endpoints.
 * This interface defines the REST API for managing TypeProfile entities.
 */
@Tag(name = "TypeProfile", description = "API for managing TypeProfiles")
@Observed
public interface ITypeProfileApi {

    /**
     * Gets all TypeProfile entities.
     *
     * @return a collection of all TypeProfile entities
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all TypeProfiles",
            description = "Returns a collection of all TypeProfile entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfiles found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<TypeProfile>>> getAllTypeProfiles();

    /**
     * Gets a TypeProfile entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the TypeProfile to retrieve
     * @return the TypeProfile entity
     */
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get a TypeProfile by PID or internal ID",
            description = "Returns a TypeProfile entity by its PID or internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfile found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class))),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    ResponseEntity<EntityModel<TypeProfile>> getTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id);

    /**
     * Gets operations for a TypeProfile.
     *
     * @param id the PID or internal ID of the TypeProfile
     * @return a collection of operations for the TypeProfile
     */
    @GetMapping("/{id}/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for a TypeProfile",
            description = "Returns a collection of operations that can be executed on a TypeProfile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    ResponseEntity<CollectionModel<EntityModel<Operation>>> getOperationsForTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id);

    /**
     * Validates a TypeProfile entity.
     *
     * @param id the PID or internal ID of the TypeProfile to validate
     * @return the validation result
     */
    @GetMapping("/{id}/validate")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Validate a TypeProfile",
            description = "Validates a TypeProfile entity and returns the validation result",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfile is valid"),
                    @ApiResponse(responseCode = "218", description = "TypeProfile is invalid"),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    ResponseEntity<?> validate(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id);

    /**
     * Gets inherited attributes for a TypeProfile.
     *
     * @param id the PID or internal ID of the TypeProfile
     * @return a collection of inherited attributes
     */
    @GetMapping("/{id}/inheritedAttributes")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get inherited attributes of a TypeProfile",
            description = "Returns a collection of attributes inherited by a TypeProfile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inherited attributes found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class))),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    ResponseEntity<CollectionModel<EntityModel<Attribute>>> getInheritedAttributes(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id);

    /**
     * Creates a new TypeProfile entity.
     * The entity is validated before saving.
     *
     * @param typeProfile the TypeProfile entity to create
     * @return the created TypeProfile entity
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new TypeProfile",
            description = "Creates a new TypeProfile entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "201", description = "TypeProfile created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
            }
    )
    ResponseEntity<EntityModel<TypeProfile>> createTypeProfile(
            @Parameter(description = "TypeProfile to create", required = true)
            @Valid @RequestBody TypeProfile typeProfile);

    /**
     * Updates an existing TypeProfile entity.
     * The entity is validated before saving.
     *
     * @param id          the PID or internal ID of the TypeProfile to update
     * @param typeProfile the updated TypeProfile entity
     * @return the updated TypeProfile entity
     */
    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update a TypeProfile",
            description = "Updates an existing TypeProfile entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfile updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    ResponseEntity<EntityModel<TypeProfile>> updateTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated TypeProfile", required = true)
            @Valid @RequestBody TypeProfile typeProfile);

    /**
     * Deletes a TypeProfile entity.
     *
     * @param id the PID or internal ID of the TypeProfile to delete
     * @return no content
     */
    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete a TypeProfile",
            description = "Deletes a TypeProfile entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "TypeProfile deleted"),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    ResponseEntity<Void> deleteTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id);

    /**
     * Gets the inheritance tree of a TypeProfile.
     *
     * @param id the PID or internal ID of the TypeProfile
     * @return the inheritance tree
     */
    @GetMapping("/{id}/inheritanceTree")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get inheritance tree of a TypeProfile",
            description = "Returns the inheritance tree of a TypeProfile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inheritance tree found",
                            content = @Content(mediaType = "application/hal+json")),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    ResponseEntity<EntityModel<TypeProfileInheritance>> getInheritanceTree(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @NotNull @PathVariable String id);

    /**
     * Partially updates a TypeProfile entity.
     *
     * @param id               the PID or internal ID of the TypeProfile to patch
     * @param typeProfilePatch the partial TypeProfile entity with fields to update
     * @return the patched TypeProfile entity
     */
    @PatchMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Partially update a TypeProfile",
            description = "Updates specific fields of an existing TypeProfile entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfile patched",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    ResponseEntity<EntityModel<TypeProfile>> patchTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id,
            @Parameter(description = "Partial TypeProfile with fields to update", required = true)
            @RequestBody TypeProfile typeProfilePatch);
}
