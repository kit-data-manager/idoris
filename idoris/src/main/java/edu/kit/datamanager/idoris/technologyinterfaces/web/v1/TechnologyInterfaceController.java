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

package edu.kit.datamanager.idoris.technologyinterfaces.web.v1;

import edu.kit.datamanager.idoris.technologyinterfaces.api.ITechnologyInterfaceExternalService;
import edu.kit.datamanager.idoris.technologyinterfaces.dto.TechnologyInterfaceDto;
import edu.kit.datamanager.idoris.technologyinterfaces.web.hateoas.TechnologyInterfaceDtoModelAssembler;
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
 * REST controller for TechnologyInterface DTOs.
 * DTO-first endpoints; HATEOAS removed for simplicity per refactoring plan.
 */
@RestController
@RequestMapping("/v1/technologyInterfaces")
@io.swagger.v3.oas.annotations.tags.Tag(name = "TechnologyInterface", description = "API for managing TechnologyInterfaces")
@Slf4j
@Observed(contextualName = "technologyInterfaceController")
public class TechnologyInterfaceController {

    private final ITechnologyInterfaceExternalService technologyInterfaceService;

    private final TechnologyInterfaceDtoModelAssembler assembler;

    public TechnologyInterfaceController(ITechnologyInterfaceExternalService technologyInterfaceService, TechnologyInterfaceDtoModelAssembler assembler) {
        this.technologyInterfaceService = technologyInterfaceService;
        this.assembler = assembler;
    }

    @GetMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.getAllTechnologyInterfaces", description = "Time taken to get all technology interfaces", histogram = true)
    @Counted(value = "technologyInterfaceController.getAllTechnologyInterfaces.count", description = "Number of get all technology interfaces requests")
    public ResponseEntity<CollectionModel<EntityModel<TechnologyInterfaceDto>>> getAllTechnologyInterfaces() {
        log.debug("Getting all TechnologyInterfaces (DTO)");
        List<TechnologyInterfaceDto> list = technologyInterfaceService.list();
        log.info("Retrieved {} technology interfaces", list.size());
        return ResponseEntity.ok(assembler.toCollectionModel(list));
    }

    @GetMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.getTechnologyInterface", description = "Time taken to get a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.getTechnologyInterface.count", description = "Number of get technology interface requests")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> getTechnologyInterface(@SpanAttribute("technologyInterface.id") @PathVariable String id) {
        log.debug("Getting TechnologyInterface with ID: {}", id);
        Optional<TechnologyInterfaceDto> dto = technologyInterfaceService.get(id);
        return dto.map(d -> ResponseEntity.ok(assembler.toModel(d))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/attributes")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.getAttributes", description = "Time taken to get technology interface attributes", histogram = true)
    @Counted(value = "technologyInterfaceController.getAttributes.count", description = "Number of get technology interface attributes requests")
    public ResponseEntity<Set<String>> getAttributes(@SpanAttribute("technologyInterface.id") @PathVariable String id) {
        log.debug("Getting attributes for TechnologyInterface with ID: {}", id);
        return technologyInterfaceService.get(id)
                .map(ti -> ResponseEntity.ok(ti.getAttributeIds()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/outputs")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.getOutputs", description = "Time taken to get technology interface outputs", histogram = true)
    @Counted(value = "technologyInterfaceController.getOutputs.count", description = "Number of get technology interface outputs requests")
    public ResponseEntity<Set<String>> getOutputs(@SpanAttribute("technologyInterface.id") @PathVariable String id) {
        log.debug("Getting outputs for TechnologyInterface with ID: {}", id);
        return technologyInterfaceService.get(id)
                .map(ti -> ResponseEntity.ok(ti.getOutputIds()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.createTechnologyInterface", description = "Time taken to create a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.createTechnologyInterface.count", description = "Number of create technology interface requests")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> createTechnologyInterface(@RequestBody TechnologyInterfaceDto dto) {
        log.debug("Creating TechnologyInterface DTO: {}", dto.getName());
        TechnologyInterfaceDto created = technologyInterfaceService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(created));
    }

    @PutMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.updateTechnologyInterface", description = "Time taken to update a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.updateTechnologyInterface.count", description = "Number of update technology interface requests")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> updateTechnologyInterface(@SpanAttribute("technologyInterface.id") @PathVariable String id,
                                                                                         @RequestBody TechnologyInterfaceDto dto) {
        log.debug("Updating TechnologyInterface with ID: {}", id);
        if (technologyInterfaceService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TechnologyInterfaceDto updated = technologyInterfaceService.update(id, dto);
        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @DeleteMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.deleteTechnologyInterface", description = "Time taken to delete a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.deleteTechnologyInterface.count", description = "Number of delete technology interface requests")
    public ResponseEntity<Void> deleteTechnologyInterface(@SpanAttribute("technologyInterface.id") @PathVariable String id) {
        log.debug("Deleting TechnologyInterface with ID: {}", id);
        if (technologyInterfaceService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        technologyInterfaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.patchTechnologyInterface", description = "Time taken to patch a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.patchTechnologyInterface.count", description = "Number of patch technology interface requests")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> patchTechnologyInterface(@SpanAttribute("technologyInterface.id") @PathVariable String id,
                                                                                        @RequestBody TechnologyInterfaceDto dto) {
        log.debug("Patching TechnologyInterface with ID: {}", id);
        if (technologyInterfaceService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TechnologyInterfaceDto patched = technologyInterfaceService.patch(id, dto);
        return ResponseEntity.ok(assembler.toModel(patched));
    }

    // Relationship endpoints

    // Backward-compatible endpoints
    @PostMapping("/{id}/attributes:link")
    @WithSpan(kind = SpanKind.SERVER)
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> linkAttributes(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (technologyInterfaceService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TechnologyInterfaceDto dto = technologyInterfaceService.linkInputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PostMapping("/{id}/attributes:unlink")
    @WithSpan(kind = SpanKind.SERVER)
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> unlinkAttributes(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (technologyInterfaceService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TechnologyInterfaceDto dto = technologyInterfaceService.unlinkInputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PostMapping("/{id}/outputs:link")
    @WithSpan(kind = SpanKind.SERVER)
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> linkOutputs(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (technologyInterfaceService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TechnologyInterfaceDto dto = technologyInterfaceService.linkOutputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PostMapping("/{id}/outputs:unlink")
    @WithSpan(kind = SpanKind.SERVER)
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> unlinkOutputs(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (technologyInterfaceService.get(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TechnologyInterfaceDto dto = technologyInterfaceService.unlinkOutputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    // New RESTful relationship endpoints
    @PostMapping("/{id}/attributes")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> addAttributes(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (technologyInterfaceService.get(id).isEmpty()) return ResponseEntity.notFound().build();
        TechnologyInterfaceDto dto = technologyInterfaceService.linkInputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @DeleteMapping("/{id}/attributes")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> deleteAttributes(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (technologyInterfaceService.get(id).isEmpty()) return ResponseEntity.notFound().build();
        TechnologyInterfaceDto dto = technologyInterfaceService.unlinkInputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PutMapping("/{id}/attributes")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> setAttributes(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        Optional<TechnologyInterfaceDto> currentOpt = technologyInterfaceService.get(id);
        if (currentOpt.isEmpty()) return ResponseEntity.notFound().build();
        Set<String> current = currentOpt.get().getAttributeIds();
        if (current != null && !current.isEmpty()) {
            Set<String> toRemove = new java.util.HashSet<>(current);
            toRemove.removeAll(attributeIds == null ? java.util.Set.of() : attributeIds);
            if (!toRemove.isEmpty()) technologyInterfaceService.unlinkInputs(id, toRemove);
        }
        if (attributeIds != null && !attributeIds.isEmpty()) technologyInterfaceService.linkInputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(technologyInterfaceService.get(id).get()));
    }

    @PostMapping("/{id}/outputs")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> addOutputs(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (technologyInterfaceService.get(id).isEmpty()) return ResponseEntity.notFound().build();
        TechnologyInterfaceDto dto = technologyInterfaceService.linkOutputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @DeleteMapping("/{id}/outputs")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> deleteOutputs(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        if (technologyInterfaceService.get(id).isEmpty()) return ResponseEntity.notFound().build();
        TechnologyInterfaceDto dto = technologyInterfaceService.unlinkOutputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PutMapping("/{id}/outputs")
    public ResponseEntity<EntityModel<TechnologyInterfaceDto>> setOutputs(@PathVariable String id, @RequestBody Set<String> attributeIds) {
        Optional<TechnologyInterfaceDto> currentOpt = technologyInterfaceService.get(id);
        if (currentOpt.isEmpty()) return ResponseEntity.notFound().build();
        Set<String> current = currentOpt.get().getOutputIds();
        if (current != null && !current.isEmpty()) {
            Set<String> toRemove = new java.util.HashSet<>(current);
            toRemove.removeAll(attributeIds == null ? java.util.Set.of() : attributeIds);
            if (!toRemove.isEmpty()) technologyInterfaceService.unlinkOutputs(id, toRemove);
        }
        if (attributeIds != null && !attributeIds.isEmpty()) technologyInterfaceService.linkOutputs(id, attributeIds);
        return ResponseEntity.ok(assembler.toModel(technologyInterfaceService.get(id).get()));
    }
}
