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

import edu.kit.datamanager.idoris.operations.dto.OperationRequestDto;
import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
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
 * API interface for Operation endpoints (DTO-first).
 */
@Tag(name = "Operation", description = "API for managing Operations")
public interface IOperationApi {

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all Operations",
            description = "Returns a collection of all Operation DTOs",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = OperationResponseDto.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<OperationResponseDto>>> getAllOperations();

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get an Operation by PID or internal ID",
            description = "Returns an Operation DTO by its PID or internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = OperationResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<EntityModel<OperationResponseDto>> getOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id);

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new Operation",
            description = "Creates a new Operation DTO after validating it",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Operation created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = OperationResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
            }
    )
    ResponseEntity<EntityModel<OperationResponseDto>> createOperation(
            @Parameter(description = "Operation to create", required = true)
            @Valid @RequestBody OperationRequestDto operation);

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update an Operation",
            description = "Updates an existing Operation DTO after validating it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = OperationResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<EntityModel<OperationResponseDto>> updateOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated Operation", required = true)
            @Valid @RequestBody OperationRequestDto operation);

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an Operation",
            description = "Deletes an Operation DTO",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Operation deleted"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<Void> deleteOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id);

    @GetMapping("/{id}/validate")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Validate an Operation",
            description = "Validates an Operation and returns the validation result",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation is valid"),
                    @ApiResponse(responseCode = "218", description = "Operation is invalid"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<?> validate(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id);

    @GetMapping("/search/getOperationsForDataType")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for a data type",
            description = "Returns a collection of operations that can be executed on a data type",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = OperationResponseDto.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<OperationResponseDto>>> getOperationsForDataType(
            @Parameter(description = "PID or internal ID of the data type", required = true)
            @RequestParam String id);

    @PatchMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Partially update an Operation",
            description = "Updates specific fields of an existing Operation DTO",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation patched",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = OperationResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    ResponseEntity<EntityModel<OperationResponseDto>> patchOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @PathVariable String id,
            @Parameter(description = "Partial Operation with fields to update", required = true)
            @RequestBody OperationRequestDto operationPatch);
}
