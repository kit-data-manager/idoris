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

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.attributes.web.hateoas.AttributeModelAssembler;
import edu.kit.datamanager.idoris.technologyinterfaces.entities.TechnologyInterface;
import edu.kit.datamanager.idoris.technologyinterfaces.services.TechnologyInterfaceService;
import edu.kit.datamanager.idoris.technologyinterfaces.web.api.ITechnologyInterfaceApi;
import edu.kit.datamanager.idoris.technologyinterfaces.web.hateoas.TechnologyInterfaceModelAssembler;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for TechnologyInterface entities.
 * This controller provides endpoints for managing TechnologyInterface entities.
 */
@RestController
@RequestMapping("/v1/technologyInterfaces")
@Slf4j
@Observed(contextualName = "technologyInterfaceController")
public class TechnologyInterfaceController implements ITechnologyInterfaceApi {

    @Autowired
    private TechnologyInterfaceService technologyInterfaceService;

    @Autowired
    private TechnologyInterfaceModelAssembler technologyInterfaceModelAssembler;

    @Autowired
    private AttributeModelAssembler attributeModelAssembler;

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.getAllTechnologyInterfaces", description = "Time taken to get all technology interfaces", histogram = true)
    @Counted(value = "technologyInterfaceController.getAllTechnologyInterfaces.count", description = "Number of get all technology interfaces requests")
    public ResponseEntity<CollectionModel<EntityModel<TechnologyInterface>>> getAllTechnologyInterfaces() {
        log.debug("Getting all TechnologyInterfaces");
        List<EntityModel<TechnologyInterface>> technologyInterfaces = StreamSupport.stream(technologyInterfaceService.getAllTechnologyInterfaces().spliterator(), false)
                .map(technologyInterfaceModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TechnologyInterface>> collectionModel = CollectionModel.of(
                technologyInterfaces,
                linkTo(methodOn(TechnologyInterfaceController.class).getAllTechnologyInterfaces()).withSelfRel()
        );

        log.info("Retrieved {} technology interfaces", technologyInterfaces.size());
        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.getTechnologyInterface", description = "Time taken to get a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.getTechnologyInterface.count", description = "Number of get technology interface requests")
    public ResponseEntity<EntityModel<TechnologyInterface>> getTechnologyInterface(@SpanAttribute("technologyInterface.id") String id) {
        log.debug("Getting TechnologyInterface with ID: {}", id);
        return technologyInterfaceService.getTechnologyInterface(id)
                .map(technologyInterface -> {
                    log.info("Found TechnologyInterface with ID: {}", id);
                    return technologyInterfaceModelAssembler.toModel(technologyInterface);
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("TechnologyInterface not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.getAttributes", description = "Time taken to get technology interface attributes", histogram = true)
    @Counted(value = "technologyInterfaceController.getAttributes.count", description = "Number of get technology interface attributes requests")
    public ResponseEntity<CollectionModel<EntityModel<Attribute>>> getAttributes(@SpanAttribute("technologyInterface.id") String id) {
        log.debug("Getting attributes for TechnologyInterface with ID: {}", id);
        return technologyInterfaceService.getTechnologyInterface(id)
                .map(technologyInterface -> {
                    List<EntityModel<Attribute>> attributes = StreamSupport.stream(technologyInterface.getAttributes().spliterator(), false)
                            .map(attributeModelAssembler::toModel)
                            .collect(Collectors.toList());

                    CollectionModel<EntityModel<Attribute>> collectionModel = CollectionModel.of(
                            attributes,
                            linkTo(methodOn(TechnologyInterfaceController.class).getAttributes(id)).withSelfRel(),
                            linkTo(methodOn(TechnologyInterfaceController.class).getTechnologyInterface(id)).withRel("technologyInterface")
                    );

                    log.info("Retrieved {} attributes for TechnologyInterface with ID: {}", attributes.size(), id);
                    return ResponseEntity.ok(collectionModel);
                })
                .orElseGet(() -> {
                    log.warn("TechnologyInterface not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.getOutputs", description = "Time taken to get technology interface outputs", histogram = true)
    @Counted(value = "technologyInterfaceController.getOutputs.count", description = "Number of get technology interface outputs requests")
    public ResponseEntity<CollectionModel<EntityModel<Attribute>>> getOutputs(@SpanAttribute("technologyInterface.id") String id) {
        log.debug("Getting outputs for TechnologyInterface with ID: {}", id);
        return technologyInterfaceService.getTechnologyInterface(id)
                .map(technologyInterface -> {
                    List<EntityModel<Attribute>> outputs = StreamSupport.stream(technologyInterface.getOutputs().spliterator(), false)
                            .map(attributeModelAssembler::toModel)
                            .collect(Collectors.toList());

                    CollectionModel<EntityModel<Attribute>> collectionModel = CollectionModel.of(
                            outputs,
                            linkTo(methodOn(TechnologyInterfaceController.class).getOutputs(id)).withSelfRel(),
                            linkTo(methodOn(TechnologyInterfaceController.class).getTechnologyInterface(id)).withRel("technologyInterface")
                    );

                    log.info("Retrieved {} outputs for TechnologyInterface with ID: {}", outputs.size(), id);
                    return ResponseEntity.ok(collectionModel);
                })
                .orElseGet(() -> {
                    log.warn("TechnologyInterface not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.createTechnologyInterface", description = "Time taken to create a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.createTechnologyInterface.count", description = "Number of create technology interface requests")
    public ResponseEntity<EntityModel<TechnologyInterface>> createTechnologyInterface(@SpanAttribute TechnologyInterface technologyInterface) {
        log.debug("Creating TechnologyInterface: {}", technologyInterface.getName());
        TechnologyInterface createdTechnologyInterface = technologyInterfaceService.createTechnologyInterface(technologyInterface);
        EntityModel<TechnologyInterface> entityModel = technologyInterfaceModelAssembler.toModel(createdTechnologyInterface);
        log.info("Created TechnologyInterface with ID: {}", createdTechnologyInterface.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.updateTechnologyInterface", description = "Time taken to update a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.updateTechnologyInterface.count", description = "Number of update technology interface requests")
    public ResponseEntity<EntityModel<TechnologyInterface>> updateTechnologyInterface(@SpanAttribute("technologyInterface.id") String id, @SpanAttribute TechnologyInterface technologyInterface) {
        log.debug("Updating TechnologyInterface with ID: {}", id);
        // Check if the entity exists
        if (!technologyInterfaceService.getTechnologyInterface(id).isPresent()) {
            log.warn("TechnologyInterface not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        // Get the existing entity to get its PID and internalId
        TechnologyInterface existing = technologyInterfaceService.getTechnologyInterface(id).get();

        // Set the PID from the existing entity
        technologyInterface.setInternalId(existing.getId());

        // Ensure internal ID is preserved
        technologyInterface.setInternalId(existing.getInternalId());

        TechnologyInterface updatedTechnologyInterface = technologyInterfaceService.updateTechnologyInterface(technologyInterface);
        EntityModel<TechnologyInterface> entityModel = technologyInterfaceModelAssembler.toModel(updatedTechnologyInterface);
        log.info("Updated TechnologyInterface with ID: {}", id);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.deleteTechnologyInterface", description = "Time taken to delete a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.deleteTechnologyInterface.count", description = "Number of delete technology interface requests")
    public ResponseEntity<Void> deleteTechnologyInterface(@SpanAttribute("technologyInterface.id") String id) {
        log.debug("Deleting TechnologyInterface with ID: {}", id);
        if (!technologyInterfaceService.getTechnologyInterface(id).isPresent()) {
            log.warn("TechnologyInterface not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        technologyInterfaceService.deleteTechnologyInterface(id);
        log.info("Deleted TechnologyInterface with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "technologyInterfaceController.patchTechnologyInterface", description = "Time taken to patch a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceController.patchTechnologyInterface.count", description = "Number of patch technology interface requests")
    public ResponseEntity<EntityModel<TechnologyInterface>> patchTechnologyInterface(@SpanAttribute("technologyInterface.id") String id, @SpanAttribute TechnologyInterface technologyInterfacePatch) {
        log.debug("Patching TechnologyInterface with ID: {}", id);
        if (!technologyInterfaceService.getTechnologyInterface(id).isPresent()) {
            log.warn("TechnologyInterface not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        TechnologyInterface patchedTechnologyInterface = technologyInterfaceService.patchTechnologyInterface(id, technologyInterfacePatch);
        EntityModel<TechnologyInterface> entityModel = technologyInterfaceModelAssembler.toModel(patchedTechnologyInterface);
        log.info("Patched TechnologyInterface with ID: {}", id);
        return ResponseEntity.ok(entityModel);
    }
}
