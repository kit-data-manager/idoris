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
package edu.kit.datamanager.idoris.attributes.web.v1;

import edu.kit.datamanager.idoris.attributes.api.IAttributeExternalService;
import edu.kit.datamanager.idoris.attributes.dto.AttributeDto;
import edu.kit.datamanager.idoris.attributes.web.hateoas.AttributeModelAssembler;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * DTO-first REST controller for Attributes.
 * Provides endpoints for managing Attributes using DTOs exclusively.
 */
@RestController
@RequestMapping("/v1/attributes")
@Slf4j
@Observed(contextualName = "attributeController")
public class AttributeController {

    @Autowired
    private IAttributeExternalService attributeService;

    @Autowired
    private AttributeModelAssembler assembler;

    @GetMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.list", description = "Time taken to list attributes", histogram = true)
    @Counted(value = "attributeController.list.count", description = "Number of attribute list requests")
    public ResponseEntity<CollectionModel<EntityModel<AttributeDto>>> list() {
        List<AttributeDto> list = attributeService.list();
        return ResponseEntity.ok(assembler.toCollectionModel(list));
    }

    @GetMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.get", description = "Time taken to get attribute", histogram = true)
    @Counted(value = "attributeController.get.count", description = "Number of attribute get requests")
    public ResponseEntity<EntityModel<AttributeDto>> get(@SpanAttribute("attribute.id") @PathVariable String id) {
        Optional<AttributeDto> dto = attributeService.get(id);
        return dto.map(d -> ResponseEntity.ok(assembler.toModel(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.create", description = "Time taken to create attribute", histogram = true)
    @Counted(value = "attributeController.create.count", description = "Number of attribute create requests")
    public ResponseEntity<EntityModel<AttributeDto>> create(@RequestBody AttributeDto dto) {
        AttributeDto created = attributeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(created));
    }

    @PutMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.update", description = "Time taken to update attribute", histogram = true)
    @Counted(value = "attributeController.update.count", description = "Number of attribute update requests")
    public ResponseEntity<EntityModel<AttributeDto>> update(@SpanAttribute("attribute.id") @PathVariable String id, @RequestBody AttributeDto dto) {
        if (attributeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AttributeDto updated = attributeService.update(id, dto);
        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @PatchMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.patch", description = "Time taken to patch attribute", histogram = true)
    @Counted(value = "attributeController.patch.count", description = "Number of attribute patch requests")
    public ResponseEntity<EntityModel<AttributeDto>> patch(@SpanAttribute("attribute.id") @PathVariable String id, @RequestBody AttributeDto dto) {
        if (attributeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AttributeDto patched = attributeService.patch(id, dto);
        return ResponseEntity.ok(assembler.toModel(patched));
    }

    @DeleteMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.delete", description = "Time taken to delete attribute", histogram = true)
    @Counted(value = "attributeController.delete.count", description = "Number of attribute delete requests")
    public ResponseEntity<Void> delete(@SpanAttribute("attribute.id") @PathVariable String id) {
        if (attributeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        attributeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Relationship endpoints

    @PostMapping("/{id}/dataType")
    @WithSpan(kind = SpanKind.SERVER)
    public ResponseEntity<EntityModel<AttributeDto>> setDataType(@PathVariable String id, @RequestBody String dataTypeId) {
        if (attributeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AttributeDto dto = attributeService.setDataType(id, dataTypeId);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @DeleteMapping("/{id}/dataType")
    @WithSpan(kind = SpanKind.SERVER)
    public ResponseEntity<EntityModel<AttributeDto>> detachDataType(@PathVariable String id) {
        if (attributeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AttributeDto dto = attributeService.detachDataType(id);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PostMapping("/{id}/override")
    @WithSpan(kind = SpanKind.SERVER)
    public ResponseEntity<EntityModel<AttributeDto>> setOverride(@PathVariable String id, @RequestBody String overrideAttributeId) {
        if (attributeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AttributeDto dto = attributeService.setOverride(id, overrideAttributeId);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @DeleteMapping("/{id}/override")
    @WithSpan(kind = SpanKind.SERVER)
    public ResponseEntity<EntityModel<AttributeDto>> detachOverride(@PathVariable String id) {
        if (attributeService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AttributeDto dto = attributeService.detachOverride(id);
        return ResponseEntity.ok(assembler.toModel(dto));
    }
}
