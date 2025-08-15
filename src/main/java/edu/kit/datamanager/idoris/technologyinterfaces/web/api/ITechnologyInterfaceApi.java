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

package edu.kit.datamanager.idoris.technologyinterfaces.web.api;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.technologyinterfaces.entities.TechnologyInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API interface for TechnologyInterface endpoints.
 * This interface defines the REST API for managing TechnologyInterface entities.
 */
@Tag(name = "TechnologyInterface", description = "API for managing TechnologyInterfaces")
public interface ITechnologyInterfaceApi {

    /**
     * Gets all TechnologyInterface entities.
     *
     * @return a collection of all TechnologyInterface entities
     */
    @GetMapping
    @Operation(
            summary = "Get all TechnologyInterfaces",
            description = "Returns a collection of all TechnologyInterface entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TechnologyInterfaces found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TechnologyInterface.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<TechnologyInterface>>> getAllTechnologyInterfaces();

    /**
     * Gets a TechnologyInterface entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the TechnologyInterface to retrieve
     * @return the TechnologyInterface entity
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get a TechnologyInterface by PID or internal ID",
            description = "Returns a TechnologyInterface entity by its PID or internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TechnologyInterface found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TechnologyInterface.class))),
                    @ApiResponse(responseCode = "404", description = "TechnologyInterface not found")
            }
    )
    ResponseEntity<EntityModel<TechnologyInterface>> getTechnologyInterface(
            @Parameter(description = "PID or internal ID of the TechnologyInterface", required = true)
            @PathVariable String id);

    /**
     * Gets the attributes of a TechnologyInterface.
     *
     * @param id the PID or internal ID of the TechnologyInterface
     * @return a collection of attributes
     */
    @GetMapping("/{id}/attributes")
    @Operation(
            summary = "Get attributes of a TechnologyInterface",
            description = "Returns a collection of attributes of a TechnologyInterface",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attributes found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class))),
                    @ApiResponse(responseCode = "404", description = "TechnologyInterface not found")
            }
    )
    ResponseEntity<CollectionModel<EntityModel<Attribute>>> getAttributes(
            @Parameter(description = "PID or internal ID of the TechnologyInterface", required = true)
            @PathVariable String id);

    /**
     * Gets the outputs of a TechnologyInterface.
     *
     * @param id the PID or internal ID of the TechnologyInterface
     * @return a collection of outputs
     */
    @GetMapping("/{id}/outputs")
    @Operation(
            summary = "Get outputs of a TechnologyInterface",
            description = "Returns a collection of outputs of a TechnologyInterface",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Outputs found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class))),
                    @ApiResponse(responseCode = "404", description = "TechnologyInterface not found")
            }
    )
    ResponseEntity<CollectionModel<EntityModel<Attribute>>> getOutputs(
            @Parameter(description = "PID or internal ID of the TechnologyInterface", required = true)
            @PathVariable String id);

    /**
     * Creates a new TechnologyInterface entity.
     *
     * @param technologyInterface the TechnologyInterface entity to create
     * @return the created TechnologyInterface entity
     */
    @PostMapping
    @Operation(
            summary = "Create a new TechnologyInterface",
            description = "Creates a new TechnologyInterface entity",
            responses = {
                    @ApiResponse(responseCode = "201", description = "TechnologyInterface created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TechnologyInterface.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    ResponseEntity<EntityModel<TechnologyInterface>> createTechnologyInterface(
            @Parameter(description = "TechnologyInterface to create", required = true)
            @Valid @RequestBody TechnologyInterface technologyInterface);

    /**
     * Updates an existing TechnologyInterface entity.
     *
     * @param id                  the PID or internal ID of the TechnologyInterface to update
     * @param technologyInterface the updated TechnologyInterface entity
     * @return the updated TechnologyInterface entity
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update a TechnologyInterface",
            description = "Updates an existing TechnologyInterface entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TechnologyInterface updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TechnologyInterface.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "TechnologyInterface not found")
            }
    )
    ResponseEntity<EntityModel<TechnologyInterface>> updateTechnologyInterface(
            @Parameter(description = "PID or internal ID of the TechnologyInterface", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated TechnologyInterface", required = true)
            @Valid @RequestBody TechnologyInterface technologyInterface);

    /**
     * Deletes a TechnologyInterface entity.
     *
     * @param id the PID or internal ID of the TechnologyInterface to delete
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a TechnologyInterface",
            description = "Deletes a TechnologyInterface entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "TechnologyInterface deleted"),
                    @ApiResponse(responseCode = "404", description = "TechnologyInterface not found")
            }
    )
    ResponseEntity<Void> deleteTechnologyInterface(
            @Parameter(description = "PID or internal ID of the TechnologyInterface", required = true)
            @PathVariable String id);

    /**
     * Partially updates a TechnologyInterface entity.
     *
     * @param id                       the PID or internal ID of the TechnologyInterface to patch
     * @param technologyInterfacePatch the partial TechnologyInterface entity with fields to update
     * @return the patched TechnologyInterface entity
     */
    @PatchMapping("/{id}")
    @Operation(
            summary = "Partially update a TechnologyInterface",
            description = "Updates specific fields of an existing TechnologyInterface entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TechnologyInterface patched",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TechnologyInterface.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "TechnologyInterface not found")
            }
    )
    ResponseEntity<EntityModel<TechnologyInterface>> patchTechnologyInterface(
            @Parameter(description = "PID or internal ID of the TechnologyInterface", required = true)
            @PathVariable String id,
            @Parameter(description = "Partial TechnologyInterface with fields to update", required = true)
            @RequestBody TechnologyInterface technologyInterfacePatch);
}
