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

import edu.kit.datamanager.idoris.core.domain.AtomicDataType;
import edu.kit.datamanager.idoris.datatypes.dto.AtomicDataTypeDto;
import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
 * API interface for AtomicDataType endpoints.
 * This interface defines the REST API for managing AtomicDataType entities.
 */
@RestController
@RequestMapping(value = "/api/v1/atomicdatatypes")
@Tag(name = "AtomicDataType", description = "API for managing AtomicDataTypes")
@Observed
public interface IAtomicDataTypeApi {

    /**
     * Gets all AtomicDataType entities.
     *
     * @return a collection of all AtomicDataType entities
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all AtomicDataTypes",
            description = "Returns a collection of all AtomicDataType entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataTypes found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataTypeDto.class)))
            }
    )
    @WithSpan
    ResponseEntity<CollectionModel<EntityModel<AtomicDataTypeDto>>> getAllAtomicDataTypes();

    /**
     * Gets an AtomicDataType entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the AtomicDataType to retrieve
     * @return the AtomicDataType entity
     */
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get an AtomicDataType by PID or internal ID",
            description = "Returns an AtomicDataType entity by its PID or internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataType found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan
    ResponseEntity<EntityModel<AtomicDataTypeDto>> getAtomicDataType(
            @SpanAttribute("atomicDataType.id")
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @PathVariable("id") String id);

    /**
     * Creates a new AtomicDataType entity.
     * The entity is validated before saving.
     *
     * @param atomicDataType the AtomicDataType entity to create
     * @return the created AtomicDataType entity
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new AtomicDataType",
            description = "Creates a new AtomicDataType entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "201", description = "AtomicDataType created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataTypeDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
            }
    )
    @WithSpan
    ResponseEntity<EntityModel<AtomicDataTypeDto>> createAtomicDataType(

            @Parameter(description = "AtomicDataType to create", required = true)
            @Valid @RequestBody AtomicDataTypeDto atomicDataType);

    /**
     * Updates an existing AtomicDataType entity.
     * The entity is validated before saving.
     *
     * @param id             the PID or internal ID of the AtomicDataType to update
     * @param atomicDataType the updated AtomicDataType entity
     * @return the updated AtomicDataType entity
     */
    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update an AtomicDataType",
            description = "Updates an existing AtomicDataType entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataType updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataTypeDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan
    ResponseEntity<EntityModel<AtomicDataTypeDto>> updateAtomicDataType(
            @SpanAttribute("atomicDataType.id")
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @PathVariable String id,

            @Parameter(description = "Updated AtomicDataType", required = true)
            @Valid @RequestBody AtomicDataTypeDto atomicDataType);

    /**
     * Deletes an AtomicDataType entity.
     *
     * @param id the PID or internal ID of the AtomicDataType to delete
     * @return no content
     */
    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an AtomicDataType",
            description = "Deletes an AtomicDataType entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "AtomicDataType deleted"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan
    ResponseEntity<Void> deleteAtomicDataType(
            @SpanAttribute("atomicDataType.id")
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @PathVariable String id);

    /**
     * Gets operations for an AtomicDataType.
     *
     * @param id the PID or internal ID of the AtomicDataType
     * @return a collection of operations for the AtomicDataType
     */
    @GetMapping("/{id}/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for an AtomicDataType",
            description = "Returns a collection of operations that can be executed on an AtomicDataType",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = OperationResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan
    ResponseEntity<CollectionModel<EntityModel<OperationResponseDto>>> getOperationsForAtomicDataType(
            @SpanAttribute("atomicDataType.id")
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @PathVariable String id);

    /**
     * Partially updates an AtomicDataType entity.
     *
     * @param id                  the PID or internal ID of the AtomicDataType to patch
     * @param atomicDataTypePatch the partial AtomicDataType entity with fields to update
     * @return the patched AtomicDataType entity
     */
    @PatchMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Partially update an AtomicDataType",
            description = "Updates specific fields of an existing AtomicDataType entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataType patched",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan
    ResponseEntity<EntityModel<AtomicDataTypeDto>> patchAtomicDataType(
            @SpanAttribute
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @PathVariable String id,
            @Parameter(description = "Partial AtomicDataType with fields to update", required = true)
            @RequestBody AtomicDataTypeDto atomicDataTypePatch);
}
