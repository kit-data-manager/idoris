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

import edu.kit.datamanager.idoris.datatypes.api.IAtomicDataTypeExternalService;
import edu.kit.datamanager.idoris.datatypes.dto.AtomicDataTypeDto;
import edu.kit.datamanager.idoris.datatypes.mappers.AtomicDataTypeMapper;
import edu.kit.datamanager.idoris.datatypes.web.api.IAtomicDataTypeApi;
import edu.kit.datamanager.idoris.datatypes.web.hateoas.AtomicDataTypeModelAssembler;
import edu.kit.datamanager.idoris.operations.api.IOperationExternalService;
import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for AtomicDataType entities.
 * This controller provides endpoints for managing AtomicDataType entities using DTOs exclusively.
 */
@RestController
@RequestMapping("/v1/atomicDataTypes")
@Tag(name = "AtomicDataType", description = "API for managing AtomicDataTypes")
@Slf4j
@Observed(contextualName = "atomicDataTypeController")
public class AtomicDataTypeController implements IAtomicDataTypeApi {

    private final IAtomicDataTypeExternalService atomicDataTypeService;
    private final IOperationExternalService operationService;
    private final AtomicDataTypeModelAssembler atomicDataTypeModelAssembler;
    private final AtomicDataTypeMapper mapper;

    public AtomicDataTypeController(IAtomicDataTypeExternalService atomicDataTypeService, IOperationExternalService operationService, AtomicDataTypeModelAssembler atomicDataTypeModelAssembler, AtomicDataTypeMapper mapper) {
        this.atomicDataTypeService = atomicDataTypeService;
        this.operationService = operationService;
        this.atomicDataTypeModelAssembler = atomicDataTypeModelAssembler;
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.getAllAtomicDataTypes", description = "Time taken to get all atomic data types", histogram = true)
    @Counted(value = "atomicDataTypeController.getAllAtomicDataTypes.count", description = "Number of get all atomic data types requests")
    public ResponseEntity<CollectionModel<EntityModel<AtomicDataTypeDto>>> getAllAtomicDataTypes() {
        List<AtomicDataTypeDto> dtos = atomicDataTypeService.list();
        List<EntityModel<AtomicDataTypeDto>> entityModels = dtos.stream()
                .map(atomicDataTypeModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<AtomicDataTypeDto>> collectionModel = CollectionModel.of(
                entityModels,
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
                                    schema = @Schema(implementation = AtomicDataTypeDto.class))),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.getAtomicDataType", description = "Time taken to get an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.getAtomicDataType.count", description = "Number of get atomic data type requests")
    public ResponseEntity<EntityModel<AtomicDataTypeDto>> getAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id) {
        return atomicDataTypeService.get(id)
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
                                    schema = @Schema(implementation = AtomicDataTypeDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.createAtomicDataType", description = "Time taken to create an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.createAtomicDataType.count", description = "Number of create atomic data type requests")
    public ResponseEntity<EntityModel<AtomicDataTypeDto>> createAtomicDataType(
            @Parameter(description = "AtomicDataType to create", required = true)
            @SpanAttribute @Valid @RequestBody AtomicDataTypeDto atomicDataType) {

        AtomicDataTypeDto saved = atomicDataTypeService.create(atomicDataType);
        return ResponseEntity.status(HttpStatus.CREATED).body(atomicDataTypeModelAssembler.toModel(saved));
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
                                    schema = @Schema(implementation = AtomicDataTypeDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.updateAtomicDataType", description = "Time taken to update an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.updateAtomicDataType.count", description = "Number of update atomic data type requests")
    public ResponseEntity<EntityModel<AtomicDataTypeDto>> updateAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id,
            @Parameter(description = "Updated AtomicDataType", required = true)
            @SpanAttribute @Valid @RequestBody AtomicDataTypeDto atomicDataType) {

        if (atomicDataTypeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AtomicDataTypeDto updated = atomicDataTypeService.update(id, atomicDataType);
        EntityModel<AtomicDataTypeDto> entityModel = atomicDataTypeModelAssembler.toModel(updated);
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
        if (atomicDataTypeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        atomicDataTypeService.delete(id);
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
                                    schema = @Schema(implementation = OperationResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.getOperationsForAtomicDataType", description = "Time taken to get operations for an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.getOperationsForAtomicDataType.count", description = "Number of get operations for atomic data type requests")
    public ResponseEntity<CollectionModel<EntityModel<OperationResponseDto>>> getOperationsForAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id) {
        if (atomicDataTypeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<EntityModel<OperationResponseDto>> operations = operationService.getOperationsForDataType(id).stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(id)).withSelfRel(),
                        linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(id)).withRel("atomicDataType")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<OperationResponseDto>> collectionModel = CollectionModel.of(
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
                                    schema = @Schema(implementation = AtomicDataTypeDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "atomicDataTypeController.patchAtomicDataType", description = "Time taken to patch an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeController.patchAtomicDataType.count", description = "Number of patch atomic data type requests")
    public ResponseEntity<EntityModel<AtomicDataTypeDto>> patchAtomicDataType(
            @Parameter(description = "PID or internal ID of the AtomicDataType", required = true)
            @SpanAttribute @PathVariable String id,
            @Parameter(description = "Partial AtomicDataType with fields to update", required = true)
            @SpanAttribute @RequestBody AtomicDataTypeDto atomicDataTypePatch) {

        if (atomicDataTypeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AtomicDataTypeDto patched = atomicDataTypeService.patch(id, atomicDataTypePatch);
        EntityModel<AtomicDataTypeDto> entityModel = atomicDataTypeModelAssembler.toModel(patched);
        return ResponseEntity.ok(entityModel);
    }
}
