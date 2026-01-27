/*
 * Copyright (c) 2025-2026 Karlsruhe Institute of Technology
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

import edu.kit.datamanager.idoris.datatypes.api.IDataTypeService;
import edu.kit.datamanager.idoris.datatypes.dto.DataTypeDto;
import edu.kit.datamanager.idoris.datatypes.web.hateoas.DataTypeModelAssembler;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * DTO-first REST controller for generic DataType operations.
 * Handles both AtomicDataType and TypeProfile entities through a unified interface.
 */
@RestController
@RequestMapping("/v1/dataTypes")
@io.swagger.v3.oas.annotations.tags.Tag(name = "DataType", description = "Unified API for managing DataTypes (AtomicDataType and TypeProfile)")
@Slf4j
@Observed(contextualName = "dataTypeController")
public class DataTypeController {

    @Autowired
    private IDataTypeService dataTypeService;

    @Autowired
    private DataTypeModelAssembler assembler;

    @GetMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "dataTypeController.list", description = "Time taken to list all DataTypes", histogram = true)
    @Counted(value = "dataTypeController.list.count", description = "Number of DataType list requests")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "List all DataTypes",
            description = "Returns a collection of all DataType entities (both AtomicDataType and TypeProfile)"
    )
    public ResponseEntity<CollectionModel<EntityModel<DataTypeDto>>> list() {
        log.debug("Getting all DataTypes");
        List<DataTypeDto> dataTypes = dataTypeService.list();
        return ResponseEntity.ok(assembler.toCollectionModel(dataTypes));
    }

    @GetMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "dataTypeController.get", description = "Time taken to get a DataType", histogram = true)
    @Counted(value = "dataTypeController.get.count", description = "Number of DataType get requests")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get a DataType by ID",
            description = "Returns a DataType entity by its ID (works for both AtomicDataType and TypeProfile)"
    )
    public ResponseEntity<EntityModel<DataTypeDto>> get(@SpanAttribute("dataType.id") @PathVariable String id) {
        log.debug("Getting DataType with ID: {}", id);
        Optional<DataTypeDto> dto = dataTypeService.get(id);
        return dto.map(d -> ResponseEntity.ok(assembler.toModel(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "dataTypeController.delete", description = "Time taken to delete a DataType", histogram = true)
    @Counted(value = "dataTypeController.delete.count", description = "Number of DataType delete requests")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete a DataType",
            description = "Deletes a DataType entity by its ID (works for both AtomicDataType and TypeProfile)"
    )
    public ResponseEntity<Void> delete(@SpanAttribute("dataType.id") @PathVariable String id) {
        log.debug("Deleting DataType with ID: {}", id);
        try {
            dataTypeService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/inheritanceHierarchy")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "dataTypeController.getInheritanceHierarchy", description = "Time taken to get DataType inheritance hierarchy", histogram = true)
    @Counted(value = "dataTypeController.getInheritanceHierarchy.count", description = "Number of inheritance hierarchy requests")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get inheritance hierarchy for a DataType",
            description = "Returns the inheritance hierarchy for a DataType (meaningful for TypeProfile, empty for AtomicDataType)"
    )
    public ResponseEntity<EntityModel<Object>> getInheritanceHierarchy(@SpanAttribute("dataType.id") @PathVariable String id) {
        log.debug("Getting inheritance hierarchy for DataType: {}", id);
        try {
            Object hierarchy = dataTypeService.getInheritanceHierarchy(id);
            return ResponseEntity.ok(EntityModel.of(hierarchy));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/operations")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "dataTypeController.getOperations", description = "Time taken to get DataType operations", histogram = true)
    @Counted(value = "dataTypeController.getOperations.count", description = "Number of DataType operations requests")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for a DataType",
            description = "Returns all operations that can be executed on this DataType"
    )
    public ResponseEntity<CollectionModel<EntityModel<Object>>> getOperations(@SpanAttribute("dataType.id") @PathVariable String id) {
        log.debug("Getting operations for DataType: {}", id);
        try {
            List<Object> operations = dataTypeService.getOperationsForDataType(id);

            List<EntityModel<Object>> operationModels = operations.stream()
                    .map(EntityModel::of)
                    .toList();

            CollectionModel<EntityModel<Object>> collectionModel = CollectionModel.of(operationModels);
            return ResponseEntity.ok(collectionModel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
