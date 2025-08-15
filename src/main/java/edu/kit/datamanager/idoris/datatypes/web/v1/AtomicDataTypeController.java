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

package edu.kit.datamanager.idoris.datatypes.web.v1;

import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.core.domain.exceptions.ValidationException;
import edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType;
import edu.kit.datamanager.idoris.datatypes.services.AtomicDataTypeService;
import edu.kit.datamanager.idoris.datatypes.web.api.IAtomicDataTypeApi;
import edu.kit.datamanager.idoris.datatypes.web.hateoas.AtomicDataTypeModelAssembler;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import edu.kit.datamanager.idoris.operations.services.OperationService;
import edu.kit.datamanager.idoris.rules.logic.RuleService;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
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
import lombok.extern.slf4j.Slf4j;
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
 * REST controller for AtomicDataType entities.
 * This controller provides endpoints for managing AtomicDataType entities.
 */
@RestController
@RequestMapping("/v1/atomicDataTypes")
@Tag(name = "AtomicDataType", description = "API for managing AtomicDataTypes")
@Slf4j
@Observed(contextualName = "atomicDataTypeController")
public class AtomicDataTypeController implements IAtomicDataTypeApi {

    private final AtomicDataTypeService atomicDataTypeService;
    private final OperationService operationService;
    private final AtomicDataTypeModelAssembler atomicDataTypeModelAssembler;
    private final RuleService ruleService;
    private final ApplicationProperties applicationProperties;

    public AtomicDataTypeController(AtomicDataTypeService atomicDataTypeService, OperationService operationService, AtomicDataTypeModelAssembler atomicDataTypeModelAssembler, RuleService ruleService, ApplicationProperties applicationProperties) {
        this.atomicDataTypeService = atomicDataTypeService;
        this.operationService = operationService;
        this.atomicDataTypeModelAssembler = atomicDataTypeModelAssembler;
        this.ruleService = ruleService;
        this.applicationProperties = applicationProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all AtomicDataTypes",
            description = "Returns a collection of all AtomicDataType entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataTypes found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class)))
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.getAllAtomicDataTypes", description = "Time taken to get all atomic data types", histogram = true)
    @Counted(value = "atomicDataTypeController.getAllAtomicDataTypes.count", description = "Number of get all atomic data types requests")
    public ResponseEntity<CollectionModel<EntityModel<AtomicDataType>>> getAllAtomicDataTypes() {
        List<EntityModel<AtomicDataType>> atomicDataTypes = atomicDataTypeService.getAllAtomicDataTypes().stream()
                .map(atomicDataTypeModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<AtomicDataType>> collectionModel = CollectionModel.of(
                atomicDataTypes,
                linkTo(methodOn(AtomicDataTypeController.class).getAllAtomicDataTypes()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.getAtomicDataType", description = "Time taken to get an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.getAtomicDataType.count", description = "Number of get atomic data type requests")
    public ResponseEntity<EntityModel<AtomicDataType>> getAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id) {
        return atomicDataTypeService.getAtomicDataType(id)
                .map(atomicDataTypeModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new AtomicDataType",
            description = "Creates a new AtomicDataType entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "201", description = "AtomicDataType created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.createAtomicDataType", description = "Time taken to create an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.createAtomicDataType.count", description = "Number of create atomic data type requests")
    public ResponseEntity<EntityModel<AtomicDataType>> createAtomicDataType(
            @Parameter(description = "AtomicDataType to create", required = true)
            @SpanAttribute @Valid @RequestBody AtomicDataType atomicDataType) {

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                atomicDataType,
                ValidationResult::new
        );
        log.debug("Validation result for AtomicDataType {}: {}", atomicDataType, validationResult);

        // Check if validation failed based on your validation policy
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        // Only save if validation passes
        AtomicDataType saved = atomicDataTypeService.createAtomicDataType(atomicDataType);
        return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(saved));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update an AtomicDataType",
            description = "Updates an existing AtomicDataType entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataType updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.updateAtomicDataType", description = "Time taken to update an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.updateAtomicDataType.count", description = "Number of update atomic data type requests")
    public ResponseEntity<EntityModel<AtomicDataType>> updateAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id,
            @Parameter(description = "Updated AtomicDataType", required = true)
            @SpanAttribute @Valid @RequestBody AtomicDataType atomicDataType) {
        // Check if the entity exists
        if (!atomicDataTypeService.getAtomicDataType(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Get the existing entity to get its PID and internalId
        AtomicDataType existing = atomicDataTypeService.getAtomicDataType(id).get();

        // Set the PID from the existing entity
        atomicDataType.setInternalId(existing.getId());

        // Ensure internal ID is preserved
        atomicDataType.setInternalId(existing.getInternalId());

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                atomicDataType,
                ValidationResult::new
        );
        log.debug("Validation result for AtomicDataType {}: {}", atomicDataType, validationResult);

        // Check if validation failed based on your validation policy
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        // Only save if validation passes
        AtomicDataType updatedAtomicDataType = atomicDataTypeService.updateAtomicDataType(atomicDataType);
        EntityModel<AtomicDataType> entityModel = atomicDataTypeModelAssembler.toModel(updatedAtomicDataType);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an AtomicDataType",
            description = "Deletes an AtomicDataType entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "AtomicDataType deleted"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.deleteAtomicDataType", description = "Time taken to delete an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.deleteAtomicDataType.count", description = "Number of delete atomic data type requests")
    public ResponseEntity<Void> deleteAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id) {
        if (!atomicDataTypeService.getAtomicDataType(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        atomicDataTypeService.deleteAtomicDataType(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/{id}/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for an AtomicDataType",
            description = "Returns a collection of operations that can be executed on an AtomicDataType",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.getOperationsForAtomicDataType", description = "Time taken to get operations for an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.getOperationsForAtomicDataType.count", description = "Number of get operations for atomic data type requests")
    public ResponseEntity<CollectionModel<EntityModel<Operation>>> getOperationsForAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id) {
        if (!atomicDataTypeService.getAtomicDataType(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<EntityModel<Operation>> operations = StreamSupport.stream(operationService.getOperationsForDataType(id).spliterator(), false)
                .map(operation -> EntityModel.of(operation,
                        linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(id)).withSelfRel(),
                        linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(id)).withRel("atomicDataType")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Operation>> collectionModel = CollectionModel.of(
                operations,
                linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(id)).withSelfRel(),
                linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(id)).withRel("atomicDataType")
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.patchAtomicDataType", description = "Time taken to patch an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.patchAtomicDataType.count", description = "Number of patch atomic data type requests")
    public ResponseEntity<EntityModel<AtomicDataType>> patchAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id,
            @Parameter(description = "Partial AtomicDataType with fields to update", required = true)
            @SpanAttribute @RequestBody AtomicDataType atomicDataTypePatch) {
        if (!atomicDataTypeService.getAtomicDataType(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Validate the patch if it contains fields that need validation
        if (atomicDataTypePatch.getPrimitiveDataType() != null ||
                atomicDataTypePatch.getRegularExpression() != null ||
                atomicDataTypePatch.getPermittedValues() != null ||
                atomicDataTypePatch.getForbiddenValues() != null ||
                atomicDataTypePatch.getMinimum() != null ||
                atomicDataTypePatch.getMaximum() != null ||
                atomicDataTypePatch.getInheritsFrom() != null) {

            // Get the current entity
            AtomicDataType existing = atomicDataTypeService.getAtomicDataType(id).get();

            // Create a merged entity for validation
            AtomicDataType merged = new AtomicDataType();
            merged.setInternalId(existing.getId());
            merged.setName(atomicDataTypePatch.getName() != null ? atomicDataTypePatch.getName() : existing.getName());
            merged.setDescription(atomicDataTypePatch.getDescription() != null ? atomicDataTypePatch.getDescription() : existing.getDescription());
            merged.setDefaultValue(atomicDataTypePatch.getDefaultValue() != null ? atomicDataTypePatch.getDefaultValue() : existing.getDefaultValue());
            merged.setPrimitiveDataType(atomicDataTypePatch.getPrimitiveDataType() != null ? atomicDataTypePatch.getPrimitiveDataType() : existing.getPrimitiveDataType());
            merged.setRegularExpression(atomicDataTypePatch.getRegularExpression() != null ? atomicDataTypePatch.getRegularExpression() : existing.getRegularExpression());
            merged.setPermittedValues(atomicDataTypePatch.getPermittedValues() != null ? atomicDataTypePatch.getPermittedValues() : existing.getPermittedValues());
            merged.setForbiddenValues(atomicDataTypePatch.getForbiddenValues() != null ? atomicDataTypePatch.getForbiddenValues() : existing.getForbiddenValues());
            merged.setMinimum(atomicDataTypePatch.getMinimum() != null ? atomicDataTypePatch.getMinimum() : existing.getMinimum());
            merged.setMaximum(atomicDataTypePatch.getMaximum() != null ? atomicDataTypePatch.getMaximum() : existing.getMaximum());
            merged.setInheritsFrom(atomicDataTypePatch.getInheritsFrom() != null ? atomicDataTypePatch.getInheritsFrom() : existing.getInheritsFrom());

            // Validate the merged entity
            ValidationResult validationResult = ruleService.executeRules(
                    RuleTask.VALIDATE,
                    merged,
                    ValidationResult::new
            );

            // Check if validation failed
            if (hasValidationErrors(validationResult)) {
                throw new ValidationException("Entity validation failed", validationResult);
            }
        }

        AtomicDataType patchedAtomicDataType = atomicDataTypeService.patchAtomicDataType(id, atomicDataTypePatch);
        EntityModel<AtomicDataType> entityModel = atomicDataTypeModelAssembler.toModel(patchedAtomicDataType);
        return ResponseEntity.ok(entityModel);
    }

    private boolean hasValidationErrors(ValidationResult validationResult) {
        return validationResult.getOutputMessages()
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel())
                        && !entry.getValue().isEmpty());
    }

}
