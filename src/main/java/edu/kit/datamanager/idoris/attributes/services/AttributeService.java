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

package edu.kit.datamanager.idoris.attributes.services;

import edu.kit.datamanager.idoris.attributes.dao.IAttributeDao;
import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Attribute entities.
 * This service provides methods for creating, updating, and retrieving Attribute entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
@Observed(contextualName = "attributeService")
public class AttributeService {
    private final IAttributeDao attributeDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new AttributeService with the given dependencies.
     *
     * @param attributeDao   the Attribute repository
     * @param eventPublisher the event publisher service
     */
    public AttributeService(IAttributeDao attributeDao, EventPublisherService eventPublisher) {
        this.attributeDao = attributeDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new Attribute entity.
     *
     * @param attribute the Attribute entity to create
     * @return the created Attribute entity
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "attributeService.createAttribute", description = "Time taken to create an attribute", histogram = true)
    @Counted(value = "attributeService.createAttribute.count", description = "Number of attribute creations")
    public Attribute createAttribute(Attribute attribute) {
        log.debug("Creating Attribute: {}", attribute);
        Attribute saved = attributeDao.save(attribute);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created Attribute with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Updates an existing Attribute entity.
     *
     * @param attribute the Attribute entity to update
     * @return the updated Attribute entity
     * @throws IllegalArgumentException if the Attribute does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "attributeService.updateAttribute", description = "Time taken to update an attribute", histogram = true)
    @Counted(value = "attributeService.updateAttribute.count", description = "Number of attribute updates")
    public Attribute updateAttribute(Attribute attribute) {
        log.debug("Updating Attribute: {}", attribute);

        if (attribute.getId() == null || attribute.getId().isEmpty()) {
            throw new IllegalArgumentException("Attribute must have a PID to be updated");
        }

        // Get the current version before updating
        Attribute existing = attributeDao.findById(attribute.getId())
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with PID: " + attribute.getId()));

        Long previousVersion = existing.getVersion();

        Attribute saved = attributeDao.save(attribute);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated Attribute with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Deletes an Attribute entity.
     *
     * @param id the PID or internal ID of the Attribute to delete
     * @throws IllegalArgumentException if the Attribute does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "attributeService.deleteAttribute", description = "Time taken to delete an attribute", histogram = true)
    @Counted(value = "attributeService.deleteAttribute.count", description = "Number of attribute deletions")
    public void deleteAttribute(@SpanAttribute("attribute.id") String id) {
        log.debug("Deleting Attribute with ID: {}", id);

        Attribute attribute = attributeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with ID: " + id));

        attributeDao.delete(attribute);
        eventPublisher.publishEntityDeleted(attribute);
        log.info("Deleted Attribute with ID: {}", id);
    }

    /**
     * Retrieves an Attribute entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the Attribute to retrieve
     * @return an Optional containing the Attribute, or empty if not found
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "attributeService.getAttribute", description = "Time taken to get an attribute", histogram = true)
    @Counted(value = "attributeService.getAttribute.count", description = "Number of attribute retrievals")
    public Optional<Attribute> getAttribute(@SpanAttribute("attribute.id") String id) {
        log.debug("Retrieving Attribute with ID: {}", id);
        return attributeDao.findById(id);
    }

    /**
     * Retrieves all Attribute entities.
     *
     * @return a list of all Attribute entities
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "attributeService.getAllAttributes", description = "Time taken to get all attributes", histogram = true)
    @Counted(value = "attributeService.getAllAttributes.count", description = "Number of get all attributes requests")
    public List<Attribute> getAllAttributes() {
        log.debug("Retrieving all Attributes");
        return attributeDao.findAll();
    }

    /**
     * Deletes orphaned Attribute entities.
     * An orphaned Attribute is one that has a dataType relationship but is not referenced by any other node.
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "attributeService.deleteOrphanedAttributes", description = "Time taken to delete orphaned attributes", histogram = true)
    @Counted(value = "attributeService.deleteOrphanedAttributes.count", description = "Number of delete orphaned attributes requests")
    public void deleteOrphanedAttributes() {
        log.debug("Deleting orphaned Attributes");
        attributeDao.deleteOrphanedAttributes();
        log.info("Deleted orphaned Attributes");
    }

    /**
     * Partially updates an existing Attribute entity.
     *
     * @param id             the PID or internal ID of the Attribute to patch
     * @param attributePatch the partial Attribute entity with fields to update
     * @return the patched Attribute entity
     * @throws IllegalArgumentException if the Attribute does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "attributeService.patchAttribute", description = "Time taken to patch an attribute", histogram = true)
    @Counted(value = "attributeService.patchAttribute.count", description = "Number of attribute patches")
    public Attribute patchAttribute(@SpanAttribute("attribute.id") String id, Attribute attributePatch) {
        log.debug("Patching Attribute with ID: {}, patch: {}", id, attributePatch);
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Attribute ID cannot be null or empty");
        }

        // Get the current entity
        Attribute existing = attributeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with ID: " + id));
        Long previousVersion = existing.getVersion();

        // Apply non-null fields from the patch to the existing entity
        if (attributePatch.getName() != null) {
            existing.setName(attributePatch.getName());
        }
        if (attributePatch.getDescription() != null) {
            existing.setDescription(attributePatch.getDescription());
        }
        if (attributePatch.getDefaultValue() != null) {
            existing.setDefaultValue(attributePatch.getDefaultValue());
        }
        if (attributePatch.getConstantValue() != null) {
            existing.setConstantValue(attributePatch.getConstantValue());
        }
        if (attributePatch.getLowerBoundCardinality() != null) {
            existing.setLowerBoundCardinality(attributePatch.getLowerBoundCardinality());
        }
        if (attributePatch.getUpperBoundCardinality() != null) {
            existing.setUpperBoundCardinality(attributePatch.getUpperBoundCardinality());
        }
        if (attributePatch.getDataType() != null) {
            existing.setDataType(attributePatch.getDataType());
        }
        if (attributePatch.getOverride() != null) {
            existing.setOverride(attributePatch.getOverride());
        }

        // Save the updated entity
        Attribute saved = attributeDao.save(existing);

        // Publish the patched event
        eventPublisher.publishEntityPatched(saved, previousVersion);

        log.info("Patched Attribute with PID: {}", saved.getId());
        return saved;
    }
}
