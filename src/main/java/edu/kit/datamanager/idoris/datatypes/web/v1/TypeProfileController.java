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

import edu.kit.datamanager.idoris.datatypes.api.ITypeProfileExternalService;
import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileDto;
import edu.kit.datamanager.idoris.datatypes.web.hateoas.TypeProfileModelAssembler;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * DTO-first REST controller for TypeProfiles.
 * Provides endpoints for managing TypeProfiles using DTOs exclusively, with
 * relationship link/unlink operations for inheritsFrom and attributes.
 */
@RestController
@RequestMapping("/v1/typeProfiles")
@io.swagger.v3.oas.annotations.tags.Tag(name = "TypeProfile", description = "API for managing TypeProfiles")
@Slf4j
@Observed(contextualName = "typeProfileController")
public class TypeProfileController {

    private final ITypeProfileExternalService service;
    private final TypeProfileModelAssembler assembler;

    @org.springframework.beans.factory.annotation.Autowired
    public TypeProfileController(ITypeProfileExternalService service) {
        this.service = service;
        // Fallback assembler to avoid requiring bean in slice tests
        this.assembler = new TypeProfileModelAssembler();
    }

    public TypeProfileController(ITypeProfileExternalService service, TypeProfileModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.list", histogram = true)
    @Counted(value = "typeProfileController.list.count")
    public ResponseEntity<CollectionModel<EntityModel<TypeProfileDto>>> list() {
        List<TypeProfileDto> dtos = service.list();
        return ResponseEntity.ok(assembler.toCollectionModel(dtos));
    }

    @GetMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.get", histogram = true)
    @Counted(value = "typeProfileController.get.count")
    public ResponseEntity<EntityModel<TypeProfileDto>> get(@SpanAttribute("typeProfile.id") @PathVariable String id) {
        Optional<TypeProfileDto> dto = service.get(id);
        return dto.map(d -> ResponseEntity.ok(assembler.toModel(d))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.create", histogram = true)
    @Counted(value = "typeProfileController.create.count")
    public ResponseEntity<EntityModel<TypeProfileDto>> create(@RequestBody TypeProfileDto dto) {
        TypeProfileDto created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(created));
    }

    @PutMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.update", histogram = true)
    @Counted(value = "typeProfileController.update.count")
    public ResponseEntity<EntityModel<TypeProfileDto>> update(@SpanAttribute("typeProfile.id") @PathVariable String id,
                                                              @RequestBody TypeProfileDto dto) {
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TypeProfileDto updated = service.update(id, dto);
        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @PatchMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.patch", histogram = true)
    @Counted(value = "typeProfileController.patch.count")
    public ResponseEntity<EntityModel<TypeProfileDto>> patch(@SpanAttribute("typeProfile.id") @PathVariable String id,
                                                             @RequestBody TypeProfileDto dto) {
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TypeProfileDto patched = service.patch(id, dto);
        return ResponseEntity.ok(assembler.toModel(patched));
    }

    @DeleteMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.delete", histogram = true)
    @Counted(value = "typeProfileController.delete.count")
    public ResponseEntity<Void> delete(@SpanAttribute("typeProfile.id") @PathVariable String id) {
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Relationship endpoints =====

    // Backward-compatible endpoints (deprecated):
    @PostMapping("/{id}/inheritsFrom:link")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.linkInheritsFrom", histogram = true)
    @Counted(value = "typeProfileController.linkInheritsFrom.count")
    public ResponseEntity<EntityModel<TypeProfileDto>> linkInheritsFrom(@PathVariable String id, @RequestBody Set<String> parentIds) {
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TypeProfileDto dto = service.addInheritsFrom(id, parentIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PostMapping("/{id}/inheritsFrom:unlink")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.unlinkInheritsFrom", histogram = true)
    @Counted(value = "typeProfileController.unlinkInheritsFrom.count")
    public ResponseEntity<EntityModel<TypeProfileDto>> unlinkInheritsFrom(@PathVariable String id, @RequestBody Set<String> parentIds) {
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TypeProfileDto dto = service.removeInheritsFrom(id, parentIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PostMapping("/{id}/attributes:link")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.linkAttributes", histogram = true)
    @Counted(value = "typeProfileController.linkAttributes.count")
    public ResponseEntity<EntityModel<TypeProfileDto>> linkAttributes(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TypeProfileDto dto = service.addAttributes(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PostMapping("/{id}/attributes:unlink")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.unlinkAttributes", histogram = true)
    @Counted(value = "typeProfileController.unlinkAttributes.count")
    public ResponseEntity<EntityModel<TypeProfileDto>> unlinkAttributes(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TypeProfileDto dto = service.removeAttributes(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    // New RESTful relationship endpoints
    @GetMapping("/{id}/inheritsFrom")
    public ResponseEntity<Set<String>> listInheritsFrom(@PathVariable String id) {
        return service.get(id)
                .map(tp -> ResponseEntity.ok(tp.getInheritsFromIds()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/inheritsFrom")
    public ResponseEntity<EntityModel<TypeProfileDto>> addInheritsFrom(@PathVariable String id, @RequestBody Set<String> parentIds) {
        if (service.get(id).isEmpty()) return ResponseEntity.notFound().build();
        TypeProfileDto dto = service.addInheritsFrom(id, parentIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @DeleteMapping("/{id}/inheritsFrom")
    public ResponseEntity<EntityModel<TypeProfileDto>> deleteInheritsFrom(@PathVariable String id, @RequestBody Set<String> parentIds) {
        if (service.get(id).isEmpty()) return ResponseEntity.notFound().build();
        TypeProfileDto dto = service.removeInheritsFrom(id, parentIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PutMapping("/{id}/inheritsFrom")
    public ResponseEntity<EntityModel<TypeProfileDto>> setInheritsFrom(@PathVariable String id, @RequestBody Set<String> parentIds) {
        Optional<TypeProfileDto> currentOpt = service.get(id);
        if (currentOpt.isEmpty()) return ResponseEntity.notFound().build();
        Set<String> current = currentOpt.get().getInheritsFromIds();
        // Remove ones not in new set
        if (current != null && !current.isEmpty()) {
            Set<String> toRemove = new java.util.HashSet<>(current);
            toRemove.removeAll(parentIds == null ? java.util.Set.of() : parentIds);
            if (!toRemove.isEmpty()) service.removeInheritsFrom(id, toRemove);
        }
        // Add missing ones
        if (parentIds != null && !parentIds.isEmpty()) service.addInheritsFrom(id, parentIds);
        return ResponseEntity.ok(assembler.toModel(service.get(id).get()));
    }

    @GetMapping("/{id}/attributes")
    public ResponseEntity<Set<String>> listAttributes(@PathVariable String id) {
        return service.get(id)
                .map(tp -> ResponseEntity.ok(tp.getAttributeIds()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/attributes")
    public ResponseEntity<EntityModel<TypeProfileDto>> addAttributesRest(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (service.get(id).isEmpty()) return ResponseEntity.notFound().build();
        TypeProfileDto dto = service.addAttributes(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @DeleteMapping("/{id}/attributes")
    public ResponseEntity<EntityModel<TypeProfileDto>> deleteAttributesRest(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (service.get(id).isEmpty()) return ResponseEntity.notFound().build();
        TypeProfileDto dto = service.removeAttributes(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PutMapping("/{id}/attributes")
    public ResponseEntity<EntityModel<TypeProfileDto>> setAttributes(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        Optional<TypeProfileDto> currentOpt = service.get(id);
        if (currentOpt.isEmpty()) return ResponseEntity.notFound().build();
        Set<String> current = currentOpt.get().getAttributeIds();
        if (current != null && !current.isEmpty()) {
            Set<String> toRemove = new java.util.HashSet<>(current);
            toRemove.removeAll(attributeIds == null ? java.util.Set.of() : attributeIds);
            if (!toRemove.isEmpty()) service.removeAttributes(id, toRemove);
        }
        if (attributeIds != null && !attributeIds.isEmpty()) service.addAttributes(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(service.get(id).get()));
    }

    // Additional endpoints for HATEOAS compatibility

    @GetMapping("/{id}/inheritedAttributes")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.getInheritedAttributes", histogram = true)
    @Counted(value = "typeProfileController.getInheritedAttributes.count")
    public ResponseEntity<Set<String>> getInheritedAttributes(@SpanAttribute("typeProfile.id") @PathVariable String id) {
        log.debug("Getting inherited attributes for TypeProfile with ID: {}", id);
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Get the TypeProfile and collect all inherited attribute IDs
        Optional<TypeProfileDto> dto = service.get(id);
        if (dto.isPresent()) {
            Set<String> inheritedAttributeIds = service.getInheritedAttributes(id);
            return ResponseEntity.ok(inheritedAttributeIds);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/inheritanceTree")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.getInheritanceTree", histogram = true)
    @Counted(value = "typeProfileController.getInheritanceTree.count")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get inheritance tree for a TypeProfile",
            description = "Returns the complete inheritance hierarchy for this TypeProfile"
    )
    public ResponseEntity<EntityModel<Object>> getInheritanceTree(@SpanAttribute("typeProfile.id") @PathVariable String id) {
        log.debug("Getting inheritance tree for TypeProfile with ID: {}", id);
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Get the inheritance chain/tree for the TypeProfile
        Object inheritanceTree = service.getInheritanceTree(id);
        EntityModel<Object> entityModel = EntityModel.of(inheritanceTree);

        return ResponseEntity.ok(entityModel);
    }

    @GetMapping("/{id}/operations")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "typeProfileController.getOperationsForTypeProfile", histogram = true)
    @Counted(value = "typeProfileController.getOperationsForTypeProfile.count")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for a TypeProfile",
            description = "Returns all operations that can be executed on this TypeProfile"
    )
    public ResponseEntity<CollectionModel<EntityModel<Object>>> getOperationsForTypeProfile(@SpanAttribute("typeProfile.id") @PathVariable String id) {
        log.debug("Getting operations for TypeProfile with ID: {}", id);
        if (service.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Get the operations available for this TypeProfile using the Operations module
        List<Object> operations = service.getOperationsForTypeProfile(id);

        // Convert to EntityModel collection
        List<EntityModel<Object>> operationModels = operations.stream()
                .map(EntityModel::of)
                .toList();

        CollectionModel<EntityModel<Object>> collectionModel = CollectionModel.of(operationModels);

        return ResponseEntity.ok(collectionModel);
    }
}
