/*
 * Copyright (c) 2024-2026 Karlsruhe Institute of Technology
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

import edu.kit.datamanager.idoris.core.domain.ValidationResult;
import edu.kit.datamanager.idoris.operations.api.IOperationService;
import edu.kit.datamanager.idoris.operations.api.IOperationStepsService;
import edu.kit.datamanager.idoris.operations.dto.OperationRequestDto;
import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
import edu.kit.datamanager.idoris.operations.web.hateoas.OperationDtoModelAssembler;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * DTO-first REST controller for Operations.
 */
@RestController
@RequestMapping("/v1/operations")
@Tag(name = "Operation", description = "API for managing Operations")
@Observed(contextualName = "operationController")
public class OperationController {

    private final IOperationService operationService;

    private final IOperationStepsService operationManagementService;

    private final OperationDtoModelAssembler assembler;

    public OperationController(IOperationService operationService, IOperationStepsService operationManagementService, OperationDtoModelAssembler assembler) {
        this.operationService = operationService;
        this.operationManagementService = operationManagementService;
        this.assembler = assembler;
    }

    @GetMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.list", histogram = true)
    @Counted(value = "operationController.list.count")
    public ResponseEntity<CollectionModel<EntityModel<OperationResponseDto>>> getAllOperations() {
        List<OperationResponseDto> dtos = operationService.list();
        List<EntityModel<OperationResponseDto>> models = dtos.stream().map(assembler::toModel).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models, linkTo(methodOn(OperationController.class).getAllOperations()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.get", histogram = true)
    @Counted(value = "operationController.get.count")
    public ResponseEntity<EntityModel<OperationResponseDto>> getOperation(
            @Parameter(description = "PID or internal ID of the Operation", required = true)
            @SpanAttribute("operation.id") @PathVariable String id) {
        return operationService.get(id)
                .map(assembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.create", histogram = true)
    @Counted(value = "operationController.create.count")
    public ResponseEntity<EntityModel<OperationResponseDto>> createOperation(@Valid @RequestBody OperationRequestDto dto) {
        OperationResponseDto created = operationService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(created));
    }

    @PutMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.update", histogram = true)
    @Counted(value = "operationController.update.count")
    public ResponseEntity<EntityModel<OperationResponseDto>> updateOperation(
            @SpanAttribute @PathVariable String id,
            @Valid @RequestBody OperationRequestDto dto) {
        if (operationService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        OperationResponseDto updated = operationService.update(id, dto);
        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @DeleteMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.delete", histogram = true)
    @Counted(value = "operationController.delete.count")
    public ResponseEntity<Void> deleteOperation(@SpanAttribute("operation.id") @PathVariable String id) {
        if (operationService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        operationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/validate")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.validate", histogram = true)
    @Counted(value = "operationController.validate.count")
    public ResponseEntity<?> validate(@SpanAttribute("operation.id") @PathVariable String id) {
        if (operationService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ValidationResult result = operationService.validate(id);
        if (result.isValid()) return ResponseEntity.ok(result);
        return ResponseEntity.status(218).body(result);
    }

    @GetMapping("/search/getOperationsForDataType")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.getOperationsForDataType", histogram = true)
    @Counted(value = "operationController.getOperationsForDataType.count")
    public ResponseEntity<CollectionModel<EntityModel<OperationResponseDto>>> getOperationsForDataType(
            @Parameter(description = "PID or internal ID of the data type", required = true)
            @SpanAttribute("dataType.id") @RequestParam String id) {
        List<OperationResponseDto> dtos = operationService.getOperationsForDataType(id);
        List<EntityModel<OperationResponseDto>> models = dtos.stream().map(assembler::toModel).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models, linkTo(methodOn(OperationController.class).getOperationsForDataType(id)).withSelfRel()));
    }

    @PatchMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "operationController.patch", histogram = true)
    @Counted(value = "operationController.patch.count")
    public ResponseEntity<EntityModel<OperationResponseDto>> patchOperation(
            @SpanAttribute @PathVariable String id,
            @RequestBody OperationRequestDto dto) {
        if (operationService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        OperationResponseDto patched = operationService.patch(id, dto);
        return ResponseEntity.ok(assembler.toModel(patched));
    }

    // ===== Steps management =====
    @GetMapping("/{id}/steps")
    public ResponseEntity<java.util.List<edu.kit.datamanager.idoris.operations.dto.OperationStepDto>> listSteps(@PathVariable String id) {
        if (operationService.get(id).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(operationManagementService.listSteps(id));
    }

    @PostMapping("/{id}/steps")
    public ResponseEntity<edu.kit.datamanager.idoris.operations.dto.OperationStepDto> createStep(@PathVariable String id,
                                                                                                 @RequestBody edu.kit.datamanager.idoris.operations.dto.OperationStepDto step) {
        if (operationService.get(id).isEmpty()) return ResponseEntity.notFound().build();
        edu.kit.datamanager.idoris.operations.dto.OperationStepDto created = operationManagementService.createStep(id, step);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}/steps")
    public ResponseEntity<Void> deleteSteps(@PathVariable String id, @RequestBody java.util.Set<String> stepIds) {
        if (operationService.get(id).isEmpty()) return ResponseEntity.notFound().build();
        operationManagementService.removeSteps(id, stepIds == null ? java.util.Set.of() : stepIds);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/steps")
    public ResponseEntity<Void> setSteps(@PathVariable String id, @RequestBody java.util.List<String> stepIds) {
        if (operationService.get(id).isEmpty()) return ResponseEntity.notFound().build();
        java.util.List<edu.kit.datamanager.idoris.operations.dto.OperationStepDto> current = operationManagementService.listSteps(id);
        java.util.Set<String> currentIds = current.stream().map(edu.kit.datamanager.idoris.operations.dto.OperationStepDto::getInternalId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> desired = stepIds == null ? java.util.Set.of() : new java.util.HashSet<>(stepIds);
        // remove missing
        java.util.Set<String> toRemove = new java.util.HashSet<>(currentIds);
        toRemove.removeAll(desired);
        if (!toRemove.isEmpty()) operationManagementService.removeSteps(id, toRemove);
        // add missing (link existing)
        java.util.Set<String> toAdd = new java.util.HashSet<>(desired);
        toAdd.removeAll(currentIds);
        if (!toAdd.isEmpty()) operationManagementService.linkExistingSteps(id, toAdd);
        return ResponseEntity.noContent().build();
    }

    // ===== Attribute mappings management for steps =====
    @GetMapping("/steps/{stepId}/inputMappings")
    public ResponseEntity<java.util.List<String>> listInputMappings(@PathVariable String stepId) {
        return ResponseEntity.ok(operationManagementService.listInputMappings(stepId));
    }

    @PostMapping("/steps/{stepId}/inputMappings")
    public ResponseEntity<Void> addInputMappings(@PathVariable String stepId, @RequestBody java.util.Set<String> mappingIds) {
        operationManagementService.addInputMappings(stepId, mappingIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/steps/{stepId}/inputMappings")
    public ResponseEntity<Void> removeInputMappings(@PathVariable String stepId, @RequestBody java.util.Set<String> mappingIds) {
        operationManagementService.removeInputMappings(stepId, mappingIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/steps/{stepId}/outputMappings")
    public ResponseEntity<java.util.List<String>> listOutputMappings(@PathVariable String stepId) {
        return ResponseEntity.ok(operationManagementService.listOutputMappings(stepId));
    }

    @PostMapping("/steps/{stepId}/outputMappings")
    public ResponseEntity<Void> addOutputMappings(@PathVariable String stepId, @RequestBody java.util.Set<String> mappingIds) {
        operationManagementService.addOutputMappings(stepId, mappingIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/steps/{stepId}/outputMappings")
    public ResponseEntity<Void> removeOutputMappings(@PathVariable String stepId, @RequestBody java.util.Set<String> mappingIds) {
        operationManagementService.removeOutputMappings(stepId, mappingIds);
        return ResponseEntity.noContent().build();
    }
}
