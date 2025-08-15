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

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.attributes.services.AttributeService;
import edu.kit.datamanager.idoris.attributes.web.api.IAttributeApi;
import edu.kit.datamanager.idoris.attributes.web.hateoas.AttributeModelAssembler;
import edu.kit.datamanager.idoris.datatypes.entities.DataType;
import edu.kit.datamanager.idoris.datatypes.web.hateoas.DataTypeModelAssembler;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for Attribute entities.
 * This controller provides endpoints for managing Attribute entities.
 */
@RestController
@RequestMapping("/v1/attributes")
@Slf4j
@Observed(contextualName = "attributeController")
public class AttributeController implements IAttributeApi {

    private final AttributeService attributeService;
    private final AttributeModelAssembler attributeModelAssembler;
    private final DataTypeModelAssembler dataTypeModelAssembler;

    public AttributeController(AttributeService attributeService, AttributeModelAssembler attributeModelAssembler, DataTypeModelAssembler dataTypeModelAssembler) {
        this.attributeService = attributeService;
        this.attributeModelAssembler = attributeModelAssembler;
        this.dataTypeModelAssembler = dataTypeModelAssembler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.getAllAttributes", description = "Time taken to get all attributes", histogram = true)
    @Counted(value = "attributeController.getAllAttributes.count", description = "Number of get all attributes requests")
    public ResponseEntity<CollectionModel<EntityModel<Attribute>>> getAllAttributes() {
        log.debug("Getting all Attributes");
        List<EntityModel<Attribute>> attributes = attributeService.getAllAttributes().stream()
                .map(attributeModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Attribute>> collectionModel = CollectionModel.of(
                attributes,
                linkTo(methodOn(AttributeController.class).getAllAttributes()).withSelfRel()
        );

        log.info("Retrieved {} attributes", attributes.size());
        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.getAttribute", description = "Time taken to get an attribute", histogram = true)
    @Counted(value = "attributeController.getAttribute.count", description = "Number of get attribute requests")
    public ResponseEntity<EntityModel<Attribute>> getAttribute(@SpanAttribute("attribute.pid") String pid) {
        log.debug("Getting Attribute with PID: {}", pid);
        return attributeService.getAttribute(pid)
                .map(attribute -> {
                    log.info("Found Attribute with PID: {}", pid);
                    return attributeModelAssembler.toModel(attribute);
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Attribute not found with PID: {}", pid);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.getDataType", description = "Time taken to get a data type", histogram = true)
    @Counted(value = "attributeController.getDataType.count", description = "Number of get data type requests")
    public ResponseEntity<EntityModel<DataType>> getDataType(@SpanAttribute("attribute.pid") String pid) {
        log.debug("Getting DataType for Attribute with PID: {}", pid);
        return attributeService.getAttribute(pid)
                .map(attribute -> {
                    log.info("Found DataType for Attribute with PID: {}", pid);
                    return attribute.getDataType();
                })
                .map(dataTypeModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Attribute not found with PID: {}", pid);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.createAttribute", description = "Time taken to create an attribute", histogram = true)
    @Counted(value = "attributeController.createAttribute.count", description = "Number of create attribute requests")
    public ResponseEntity<EntityModel<Attribute>> createAttribute(@SpanAttribute Attribute attribute) {
        log.debug("Creating Attribute: {}", attribute.getName());
        Attribute createdAttribute = attributeService.createAttribute(attribute);
        EntityModel<Attribute> entityModel = attributeModelAssembler.toModel(createdAttribute);
        log.info("Created Attribute with PID: {}", createdAttribute.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.updateAttribute", description = "Time taken to update an attribute", histogram = true)
    @Counted(value = "attributeController.updateAttribute.count", description = "Number of update attribute requests")
    public ResponseEntity<EntityModel<Attribute>> updateAttribute(@SpanAttribute("attribute.id") String id, @SpanAttribute Attribute attribute) {
        log.debug("Updating Attribute with ID: {}", id);
        // Check if the entity exists
        if (attributeService.getAttribute(id).isEmpty()) {
            log.warn("Attribute not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        // Get the existing entity to get its PID and internalId
        Attribute existing = attributeService.getAttribute(id).get();

        // Set the PID from the existing entity
        attribute.setInternalId(existing.getId());

        // Ensure internal ID is preserved
        attribute.setInternalId(existing.getInternalId());

        Attribute updatedAttribute = attributeService.updateAttribute(attribute);
        EntityModel<Attribute> entityModel = attributeModelAssembler.toModel(updatedAttribute);
        log.info("Updated Attribute with ID: {}", id);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.deleteAttribute", description = "Time taken to delete an attribute", histogram = true)
    @Counted(value = "attributeController.deleteAttribute.count", description = "Number of delete attribute requests")
    public ResponseEntity<Void> deleteAttribute(@SpanAttribute("attribute.pid") String pid) {
        log.debug("Deleting Attribute with PID: {}", pid);
        if (attributeService.getAttribute(pid).isEmpty()) {
            log.warn("Attribute not found with PID: {}", pid);
            return ResponseEntity.notFound().build();
        }

        attributeService.deleteAttribute(pid);
        log.info("Deleted Attribute with PID: {}", pid);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.deleteOrphanedAttributes", description = "Time taken to delete orphaned attributes", histogram = true)
    @Counted(value = "attributeController.deleteOrphanedAttributes.count", description = "Number of delete orphaned attributes requests")
    public ResponseEntity<Void> deleteOrphanedAttributes() {
        log.debug("Deleting orphaned attributes");
        attributeService.deleteOrphanedAttributes();
        log.info("Deleted orphaned attributes");
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "attributeController.patchAttribute", description = "Time taken to patch an attribute", histogram = true)
    @Counted(value = "attributeController.patchAttribute.count", description = "Number of patch attribute requests")
    public ResponseEntity<EntityModel<Attribute>> patchAttribute(@SpanAttribute("attribute.pid") String pid, @SpanAttribute Attribute attributePatch) {
        log.debug("Patching Attribute with PID: {}", pid);
        if (attributeService.getAttribute(pid).isEmpty()) {
            log.warn("Attribute not found with PID: {}", pid);
            return ResponseEntity.notFound().build();
        }

        Attribute patchedAttribute = attributeService.patchAttribute(pid, attributePatch);
        EntityModel<Attribute> entityModel = attributeModelAssembler.toModel(patchedAttribute);
        log.info("Patched Attribute with PID: {}", pid);
        return ResponseEntity.ok(entityModel);
    }
}
