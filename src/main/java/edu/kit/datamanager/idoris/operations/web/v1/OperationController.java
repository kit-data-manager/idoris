/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.operations.web.v1;

import edu.kit.datamanager.idoris.core.domain.exceptions.ValidationException;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import edu.kit.datamanager.idoris.operations.services.OperationService;
import edu.kit.datamanager.idoris.operations.web.api.IOperationApi;
import edu.kit.datamanager.idoris.operations.web.hateoas.OperationModelAssembler;
import edu.kit.datamanager.idoris.rules.validation.ValidationPolicyValidator;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for Operation entities.
 * This controller provides endpoints for managing Operation entities.
 */
@RestController
@RequestMapping("/v1/operations")
@Tag(name = "Operation", description = "API for managing Operations")
@Observed(contextualName = "operationController")
public class OperationController implements IOperationApi {

    @Autowired
    private OperationService operationService;

    @Autowired
    private OperationModelAssembler operationModelAssembler;

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.getAllOperations", description = "Time taken to get all operations", histogram = true)
    @Counted(value = "operationController.getAllOperations.count", description = "Number of get all operations requests")
    public ResponseEntity<CollectionModel<EntityModel<Operation>>> getAllOperations() {
        List<EntityModel<Operation>> operations = StreamSupport.stream(operationService.getAllOperations().spliterator(), false)
                .map(operationModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Operation>> collectionModel = CollectionModel.of(
                operations,
                linkTo(methodOn(OperationController.class).getAllOperations()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.getOperation", description = "Time taken to get an operation", histogram = true)
    @Counted(value = "operationController.getOperation.count", description = "Number of get operation requests")
    public ResponseEntity<EntityModel<Operation>> getOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @SpanAttribute("operation.id") @PathVariable String id) {
        return operationService.getOperation(id)
                .map(operationModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.createOperation", description = "Time taken to create an operation", histogram = true)
    @Counted(value = "operationController.createOperation.count", description = "Number of create operation requests")
    public ResponseEntity<EntityModel<Operation>> createOperation(
            @Parameter(description = "Operation to create", required = true)
            @Valid @RequestBody Operation operation) {
        // Validate the operation using the ValidationPolicyValidator
        ValidationPolicyValidator validator = new ValidationPolicyValidator();
        ValidationResult validationResult = operation.execute(validator);

        // Check if validation failed
        if (!validationResult.isValid()) {
            throw new ValidationException("Operation validation failed", validationResult);
        }

        // Only save if validation passes
        Operation createdOperation = operationService.createOperation(operation);
        EntityModel<Operation> entityModel = operationModelAssembler.toModel(createdOperation);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.updateOperation", description = "Time taken to update an operation", histogram = true)
    @Counted(value = "operationController.updateOperation.count", description = "Number of update operation requests")
    public ResponseEntity<EntityModel<Operation>> updateOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @SpanAttribute @PathVariable String id,
            @Parameter(description = "Updated Operation", required = true)
            @SpanAttribute @Valid @RequestBody Operation operation) {
        // Check if the entity exists
        if (!operationService.getOperation(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Get the existing entity to get its PID and internalId
        Operation existing = operationService.getOperation(id).get();

        // Set the PID from the existing entity
        operation.setInternalId(existing.getId());

        // Ensure internal ID is preserved
        operation.setInternalId(existing.getInternalId());

        // Validate the operation using the ValidationPolicyValidator
        ValidationPolicyValidator validator = new ValidationPolicyValidator();
        ValidationResult validationResult = operation.execute(validator);

        // Check if validation failed
        if (!validationResult.isValid()) {
            throw new ValidationException("Operation validation failed", validationResult);
        }

        // Only save if validation passes
        Operation updatedOperation = operationService.updateOperation(operation);
        EntityModel<Operation> entityModel = operationModelAssembler.toModel(updatedOperation);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an Operation",
            description = "Deletes an Operation entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Operation deleted"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.deleteOperation", description = "Time taken to delete an operation", histogram = true)
    @Counted(value = "operationController.deleteOperation.count", description = "Number of delete operation requests")
    public ResponseEntity<Void> deleteOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @SpanAttribute("operation.id") @PathVariable String id) {
        if (!operationService.getOperation(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        operationService.deleteOperation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.validate", description = "Time taken to validate an operation", histogram = true)
    @Counted(value = "operationController.validate.count", description = "Number of validate operation requests")
    public ResponseEntity<?> validate(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @SpanAttribute("operation.id") @PathVariable String id) {
        if (!operationService.getOperation(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Operation operation = operationService.getOperation(id).get();
        ValidationPolicyValidator validator = new ValidationPolicyValidator();
        ValidationResult result = operation.execute(validator);

        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(218).body(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.getOperationsForDataType", description = "Time taken to get operations for a data type", histogram = true)
    @Counted(value = "operationController.getOperationsForDataType.count", description = "Number of get operations for data type requests")
    public ResponseEntity<CollectionModel<EntityModel<Operation>>> getOperationsForDataType(
            @Parameter(description = "PID or internal ID of the data type", required = true)
            @SpanAttribute("dataType.id") @RequestParam String id) {
        List<EntityModel<Operation>> operations = StreamSupport.stream(operationService.getOperationsForDataType(id).spliterator(), false)
                .map(operationModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Operation>> collectionModel = CollectionModel.of(
                operations,
                linkTo(methodOn(OperationController.class).getOperationsForDataType(id)).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.patchOperation", description = "Time taken to patch an operation", histogram = true)
    @Counted(value = "operationController.patchOperation.count", description = "Number of patch operation requests")
    public ResponseEntity<EntityModel<Operation>> patchOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @SpanAttribute @PathVariable String id,
            @Parameter(description = "Partial Operation with fields to update", required = true)
            @SpanAttribute @RequestBody Operation operationPatch) {
        if (!operationService.getOperation(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Validate the patch if it contains fields that need validation
        if (operationPatch.getExecutableOn() != null ||
                operationPatch.getReturns() != null ||
                operationPatch.getEnvironment() != null ||
                operationPatch.getExecution() != null) {

            // Get the current entity
            Operation existing = operationService.getOperation(id).get();

            // Create a merged entity for validation
            Operation merged = new Operation();
            merged.setInternalId(existing.getId());
            merged.setName(operationPatch.getName() != null ? operationPatch.getName() : existing.getName());
            merged.setDescription(operationPatch.getDescription() != null ? operationPatch.getDescription() : existing.getDescription());
            merged.setExecutableOn(operationPatch.getExecutableOn() != null ? operationPatch.getExecutableOn() : existing.getExecutableOn());
            merged.setReturns(operationPatch.getReturns() != null ? operationPatch.getReturns() : existing.getReturns());
            merged.setEnvironment(operationPatch.getEnvironment() != null ? operationPatch.getEnvironment() : existing.getEnvironment());
            merged.setExecution(operationPatch.getExecution() != null ? operationPatch.getExecution() : existing.getExecution());

            // Validate the merged entity
            ValidationPolicyValidator validator = new ValidationPolicyValidator();
            ValidationResult validationResult = merged.execute(validator);

            // Check if validation failed
            if (!validationResult.isValid()) {
                throw new ValidationException("Operation validation failed", validationResult);
            }
        }

        Operation patchedOperation = operationService.patchOperation(id, operationPatch);
        EntityModel<Operation> entityModel = operationModelAssembler.toModel(patchedOperation);
        return ResponseEntity.ok(entityModel);
    }
}
