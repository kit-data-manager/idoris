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

package edu.kit.datamanager.idoris.operations.web.api;

import edu.kit.datamanager.idoris.operations.entities.Operation;
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
 * API interface for Operation endpoints.
 * This interface defines the REST API for managing Operation entities.
 */
@Tag(name = "Operation", description = "API for managing Operations")
public interface IOperationApi {

    /**
     * Gets all Operation entities.
     *
     * @return a collection of all Operation entities
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all Operations",
            description = "Returns a collection of all Operation entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<Operation>>> getAllOperations();

    /**
     * Gets an Operation entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the Operation to retrieve
     * @return the Operation entity
     */
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get an Operation by PID or internal ID",
            description = "Returns an Operation entity by its PID or internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<EntityModel<Operation>> getOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id);

    /**
     * Creates a new Operation entity.
     * The entity is validated before saving.
     *
     * @param operation the Operation entity to create
     * @return the created Operation entity
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new Operation",
            description = "Creates a new Operation entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Operation created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
            }
    )
    ResponseEntity<EntityModel<Operation>> createOperation(
            @Parameter(description = "Operation to create", required = true)
            @Valid @RequestBody Operation operation);

    /**
     * Updates an existing Operation entity.
     * The entity is validated before saving.
     *
     * @param id        the PID or internal ID of the Operation to update
     * @param operation the updated Operation entity
     * @return the updated Operation entity
     */
    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update an Operation",
            description = "Updates an existing Operation entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<EntityModel<Operation>> updateOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated Operation", required = true)
            @Valid @RequestBody Operation operation);

    /**
     * Deletes an Operation entity.
     *
     * @param id the PID or internal ID of the Operation to delete
     * @return no content
     */
    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an Operation",
            description = "Deletes an Operation entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Operation deleted"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<Void> deleteOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id);

    /**
     * Validates an Operation entity.
     *
     * @param id the PID or internal ID of the Operation to validate
     * @return the validation result
     */
    @GetMapping("/{id}/validate")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Validate an Operation",
            description = "Validates an Operation entity and returns the validation result",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation is valid"),
                    @ApiResponse(responseCode = "218", description = "Operation is invalid"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<?> validate(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id);

    /**
     * Gets operations for a data type.
     *
     * @param id the PID or internal ID of the data type
     * @return a collection of operations for the data type
     */
    @GetMapping("/search/getOperationsForDataType")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for a data type",
            description = "Returns a collection of operations that can be executed on a data type",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<Operation>>> getOperationsForDataType(
            @Parameter(description = "PID or internal ID of the data type", required = true)
            @RequestParam String id);

    /**
     * Partially updates an Operation entity.
     *
     * @param id             the PID or internal ID of the Operation to patch
     * @param operationPatch the partial Operation entity with fields to update
     * @return the patched Operation entity
     */
    @PatchMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Partially update an Operation",
            description = "Updates specific fields of an existing Operation entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation patched",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<EntityModel<Operation>> patchOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id,
            @Parameter(description = "Partial Operation with fields to update", required = true)
            @RequestBody Operation operationPatch);
}
