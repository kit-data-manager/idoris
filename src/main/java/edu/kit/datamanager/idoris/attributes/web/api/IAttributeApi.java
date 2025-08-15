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

package edu.kit.datamanager.idoris.attributes.web.api;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.datatypes.entities.DataType;
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
 * API interface for Attribute endpoints.
 * This interface defines the REST API for managing Attribute entities.
 */
@Tag(name = "Attribute", description = "API for managing Attributes")
public interface IAttributeApi {

    /**
     * Gets all Attribute entities.
     *
     * @return a collection of all Attribute entities
     */
    @GetMapping
    @Operation(
            summary = "Get all Attributes",
            description = "Returns a collection of all Attribute entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attributes found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<Attribute>>> getAllAttributes();

    /**
     * Gets an Attribute entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the Attribute to retrieve
     * @return the Attribute entity
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get an Attribute by PID or internal ID",
            description = "Returns an Attribute entity by its PID or internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attribute found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class))),
                    @ApiResponse(responseCode = "404", description = "Attribute not found")
            }
    )
    ResponseEntity<EntityModel<Attribute>> getAttribute(
            @Parameter(description = "PID or internal ID of the Attribute", required = true)
            @PathVariable String id);

    /**
     * Gets the DataType of an Attribute.
     *
     * @param id the PID or internal ID of the Attribute
     * @return the DataType of the Attribute
     */
    @GetMapping("/{id}/dataType")
    @Operation(
            summary = "Get the DataType of an Attribute",
            description = "Returns the DataType of an Attribute",
            responses = {
                    @ApiResponse(responseCode = "200", description = "DataType found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = DataType.class))),
                    @ApiResponse(responseCode = "404", description = "Attribute not found")
            }
    )
    ResponseEntity<EntityModel<DataType>> getDataType(
            @Parameter(description = "PID or internal ID of the Attribute", required = true)
            @PathVariable String id);

    /**
     * Creates a new Attribute entity.
     *
     * @param attribute the Attribute entity to create
     * @return the created Attribute entity
     */
    @PostMapping
    @Operation(
            summary = "Create a new Attribute",
            description = "Creates a new Attribute entity",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Attribute created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    ResponseEntity<EntityModel<Attribute>> createAttribute(
            @Parameter(description = "Attribute to create", required = true)
            @Valid @RequestBody Attribute attribute);

    /**
     * Updates an existing Attribute entity.
     *
     * @param id        the PID or internal ID of the Attribute to update
     * @param attribute the updated Attribute entity
     * @return the updated Attribute entity
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update an Attribute",
            description = "Updates an existing Attribute entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attribute updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Attribute not found")
            }
    )
    ResponseEntity<EntityModel<Attribute>> updateAttribute(
            @Parameter(description = "PID or internal ID of the Attribute", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated Attribute", required = true)
            @Valid @RequestBody Attribute attribute);

    /**
     * Deletes an Attribute entity.
     *
     * @param id the PID or internal ID of the Attribute to delete
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an Attribute",
            description = "Deletes an Attribute entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Attribute deleted"),
                    @ApiResponse(responseCode = "404", description = "Attribute not found")
            }
    )
    ResponseEntity<Void> deleteAttribute(
            @Parameter(description = "PID or internal ID of the Attribute", required = true)
            @PathVariable String id);

    /**
     * Deletes orphaned Attribute entities.
     * An orphaned Attribute is one that has a dataType relationship but is not referenced by any other node.
     *
     * @return no content
     */
    @DeleteMapping("/orphaned")
    @Operation(
            summary = "Delete orphaned Attributes",
            description = "Deletes Attribute entities that are not referenced by any other node",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Orphaned Attributes deleted")
            }
    )
    ResponseEntity<Void> deleteOrphanedAttributes();

    /**
     * Partially updates an Attribute entity.
     *
     * @param id             the PID or internal ID of the Attribute to patch
     * @param attributePatch the partial Attribute entity with fields to update
     * @return the patched Attribute entity
     */
    @PatchMapping("/{id}")
    @Operation(
            summary = "Partially update an Attribute",
            description = "Updates specific fields of an existing Attribute entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attribute patched",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Attribute not found")
            }
    )
    ResponseEntity<EntityModel<Attribute>> patchAttribute(
            @Parameter(description = "PID or internal ID of the Attribute", required = true)
            @PathVariable String id,
            @Parameter(description = "Partial Attribute with fields to update", required = true)
            @RequestBody Attribute attributePatch);
}
