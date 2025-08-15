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

package edu.kit.datamanager.idoris.operations.services;

import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.operations.dao.IAttributeMappingDao;
import edu.kit.datamanager.idoris.operations.entities.AttributeMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing AttributeMapping entities.
 * This service provides methods for creating, updating, and retrieving AttributeMapping entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
public class AttributeMappingService {
    private final IAttributeMappingDao attributeMappingDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new AttributeMappingService with the given dependencies.
     *
     * @param attributeMappingDao the AttributeMapping repository
     * @param eventPublisher      the event publisher service
     */
    public AttributeMappingService(IAttributeMappingDao attributeMappingDao, EventPublisherService eventPublisher) {
        this.attributeMappingDao = attributeMappingDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new AttributeMapping entity.
     *
     * @param attributeMapping the AttributeMapping entity to create
     * @return the created AttributeMapping entity
     */
    @Transactional
    public AttributeMapping createAttributeMapping(AttributeMapping attributeMapping) {
        log.debug("Creating AttributeMapping: {}", attributeMapping);
        AttributeMapping saved = attributeMappingDao.save(attributeMapping);
        eventPublisher.publishEntityCreated(saved, "AttributeMapping");
        log.info("Created AttributeMapping with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Updates an existing AttributeMapping entity.
     *
     * @param attributeMapping the AttributeMapping entity to update
     * @return the updated AttributeMapping entity
     * @throws IllegalArgumentException if the AttributeMapping does not exist
     */
    @Transactional
    public AttributeMapping updateAttributeMapping(AttributeMapping attributeMapping) {
        log.debug("Updating AttributeMapping: {}", attributeMapping);

        if (attributeMapping.getId() == null || attributeMapping.getId().isEmpty()) {
            throw new IllegalArgumentException("AttributeMapping must have an ID to be updated");
        }

        // Check if the entity exists
        if (!attributeMappingDao.existsById(attributeMapping.getId())) {
            throw new IllegalArgumentException("AttributeMapping not found with ID: " + attributeMapping.getId());
        }

        AttributeMapping saved = attributeMappingDao.save(attributeMapping);
        eventPublisher.publishEntityUpdated(saved, "AttributeMapping");
        log.info("Updated AttributeMapping with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Deletes an AttributeMapping entity.
     *
     * @param id the ID of the AttributeMapping to delete
     * @throws IllegalArgumentException if the AttributeMapping does not exist
     */
    @Transactional
    public void deleteAttributeMapping(String id) {
        log.debug("Deleting AttributeMapping with ID: {}", id);

        AttributeMapping attributeMapping = attributeMappingDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AttributeMapping not found with ID: " + id));

        attributeMappingDao.delete(attributeMapping);
        eventPublisher.publishEntityDeleted(attributeMapping, "AttributeMapping");
        log.info("Deleted AttributeMapping with ID: {}", id);
    }

    /**
     * Retrieves an AttributeMapping entity by its ID.
     *
     * @param id the ID of the AttributeMapping to retrieve
     * @return an Optional containing the AttributeMapping, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<AttributeMapping> getAttributeMapping(String id) {
        log.debug("Retrieving AttributeMapping with ID: {}", id);
        return attributeMappingDao.findById(id);
    }

    /**
     * Retrieves all AttributeMapping entities.
     *
     * @return a list of all AttributeMapping entities
     */
    @Transactional(readOnly = true)
    public List<AttributeMapping> getAllAttributeMappings() {
        log.debug("Retrieving all AttributeMappings");
        return attributeMappingDao.findAll();
    }

    /**
     * Finds AttributeMapping entities by input attribute PID.
     *
     * @param pid the PID of the input attribute
     * @return a list of AttributeMapping entities
     * @deprecated Use {@link #findByInputAttributeId(String)} instead
     */
    @Transactional(readOnly = true)
    @Deprecated
    public List<AttributeMapping> findByInputAttributePid(String pid) {
        log.debug("Finding AttributeMappings by input attribute PID: {}", pid);
        return (List<AttributeMapping>) attributeMappingDao.findByInputAttributePid(pid);
    }

    /**
     * Finds AttributeMapping entities by input attribute ID (either PID or internal ID).
     *
     * @param id the ID of the input attribute (either PID or internal ID)
     * @return a list of AttributeMapping entities
     */
    @Transactional(readOnly = true)
    public List<AttributeMapping> findByInputAttributeId(String id) {
        log.debug("Finding AttributeMappings by input attribute ID: {}", id);
        return (List<AttributeMapping>) attributeMappingDao.findByInputAttributeId(id);
    }

    /**
     * Finds AttributeMapping entities by output attribute PID.
     *
     * @param pid the PID of the output attribute
     * @return a list of AttributeMapping entities
     * @deprecated Use {@link #findByOutputAttributeId(String)} instead
     */
    @Transactional(readOnly = true)
    @Deprecated
    public List<AttributeMapping> findByOutputAttributePid(String pid) {
        log.debug("Finding AttributeMappings by output attribute PID: {}", pid);
        return (List<AttributeMapping>) attributeMappingDao.findByOutputAttributePid(pid);
    }

    /**
     * Finds AttributeMapping entities by output attribute ID (either PID or internal ID).
     *
     * @param id the ID of the output attribute (either PID or internal ID)
     * @return a list of AttributeMapping entities
     */
    @Transactional(readOnly = true)
    public List<AttributeMapping> findByOutputAttributeId(String id) {
        log.debug("Finding AttributeMappings by output attribute ID: {}", id);
        return (List<AttributeMapping>) attributeMappingDao.findByOutputAttributeId(id);
    }
}
