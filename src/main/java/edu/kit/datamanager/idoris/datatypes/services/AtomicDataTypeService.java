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

package edu.kit.datamanager.idoris.datatypes.services;

import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.datatypes.dao.IAtomicDataTypeDao;
import edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType;
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
 * Service for managing AtomicDataType entities.
 * This service provides methods for creating, updating, and retrieving AtomicDataType entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
@Observed(contextualName = "atomicDataTypeService")
public class AtomicDataTypeService {
    private final IAtomicDataTypeDao atomicDataTypeDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new AtomicDataTypeService with the given dependencies.
     *
     * @param atomicDataTypeDao the AtomicDataType repository
     * @param eventPublisher    the event publisher service
     */
    public AtomicDataTypeService(IAtomicDataTypeDao atomicDataTypeDao, EventPublisherService eventPublisher) {
        this.atomicDataTypeDao = atomicDataTypeDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new AtomicDataType entity.
     *
     * @param atomicDataType the AtomicDataType entity to create
     * @return the created AtomicDataType entity
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.createAtomicDataType", description = "Time taken to create an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.createAtomicDataType.count", description = "Number of atomic data type creations")
    public AtomicDataType createAtomicDataType(AtomicDataType atomicDataType) {
        log.debug("Creating AtomicDataType: {}", atomicDataType);
        atomicDataType.setInternalId(null);
        atomicDataType.setVersion(null);
        AtomicDataType saved = atomicDataTypeDao.save(atomicDataType);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created AtomicDataType with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Updates an existing AtomicDataType entity.
     *
     * @param atomicDataType the AtomicDataType entity to update
     * @return the updated AtomicDataType entity
     * @throws IllegalArgumentException if the AtomicDataType does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.updateAtomicDataType", description = "Time taken to update an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.updateAtomicDataType.count", description = "Number of atomic data type updates")
    public AtomicDataType updateAtomicDataType(AtomicDataType atomicDataType) {
        log.debug("Updating AtomicDataType: {}", atomicDataType);

        if (atomicDataType.getId() == null || atomicDataType.getId().isEmpty()) {
            throw new IllegalArgumentException("AtomicDataType must have a PID to be updated");
        }

        // Get the current version before updating
        AtomicDataType existing = atomicDataTypeDao.findById(atomicDataType.getId())
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with PID: " + atomicDataType.getId()));

        Long previousVersion = existing.getVersion();

        AtomicDataType saved = atomicDataTypeDao.save(atomicDataType);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated AtomicDataType with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Deletes an AtomicDataType entity.
     *
     * @param id the PID or internal ID of the AtomicDataType to delete
     * @throws IllegalArgumentException if the AtomicDataType does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.deleteAtomicDataType", description = "Time taken to delete an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.deleteAtomicDataType.count", description = "Number of atomic data type deletions")
    public void deleteAtomicDataType(@SpanAttribute("atomicDataType.id") String id) {
        log.debug("Deleting AtomicDataType with ID: {}", id);

        AtomicDataType atomicDataType = atomicDataTypeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));

        atomicDataTypeDao.delete(atomicDataType);
        eventPublisher.publishEntityDeleted(atomicDataType);
        log.info("Deleted AtomicDataType with ID: {}", id);
    }

    /**
     * Retrieves an AtomicDataType entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the AtomicDataType to retrieve
     * @return an Optional containing the AtomicDataType, or empty if not found
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.getAtomicDataType", description = "Time taken to get an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.getAtomicDataType.count", description = "Number of atomic data type retrievals")
    public Optional<AtomicDataType> getAtomicDataType(@SpanAttribute("atomicDataType.id") String id) {
        log.debug("Retrieving AtomicDataType with ID: {}", id);
        return atomicDataTypeDao.findById(id);
    }

    /**
     * Retrieves all AtomicDataType entities.
     *
     * @return a list of all AtomicDataType entities
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.getAllAtomicDataTypes", description = "Time taken to get all atomic data types", histogram = true)
    @Counted(value = "atomicDataTypeService.getAllAtomicDataTypes.count", description = "Number of get all atomic data types requests")
    public List<AtomicDataType> getAllAtomicDataTypes() {
        log.debug("Retrieving all AtomicDataTypes");
        return atomicDataTypeDao.findAll();
    }

    /**
     * Partially updates an existing AtomicDataType entity.
     *
     * @param id                  the PID or internal ID of the AtomicDataType to patch
     * @param atomicDataTypePatch the partial AtomicDataType entity with fields to update
     * @return the patched AtomicDataType entity
     * @throws IllegalArgumentException if the AtomicDataType does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.patchAtomicDataType", description = "Time taken to patch an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.patchAtomicDataType.count", description = "Number of atomic data type patches")
    public AtomicDataType patchAtomicDataType(@SpanAttribute("atomicDataType.id") String id, AtomicDataType atomicDataTypePatch) {
        log.debug("Patching AtomicDataType with ID: {}, patch: {}", id, atomicDataTypePatch);
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("AtomicDataType ID cannot be null or empty");
        }

        // Get the current entity
        AtomicDataType existing = atomicDataTypeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));
        Long previousVersion = existing.getVersion();

        // Apply non-null fields from the patch to the existing entity
        if (atomicDataTypePatch.getName() != null) {
            existing.setName(atomicDataTypePatch.getName());
        }
        if (atomicDataTypePatch.getDescription() != null) {
            existing.setDescription(atomicDataTypePatch.getDescription());
        }
        if (atomicDataTypePatch.getDefaultValue() != null) {
            existing.setDefaultValue(atomicDataTypePatch.getDefaultValue());
        }
        if (atomicDataTypePatch.getPrimitiveDataType() != null) {
            existing.setPrimitiveDataType(atomicDataTypePatch.getPrimitiveDataType());
        }
        if (atomicDataTypePatch.getRegularExpression() != null) {
            existing.setRegularExpression(atomicDataTypePatch.getRegularExpression());
        }
        if (atomicDataTypePatch.getPermittedValues() != null) {
            existing.setPermittedValues(atomicDataTypePatch.getPermittedValues());
        }
        if (atomicDataTypePatch.getForbiddenValues() != null) {
            existing.setForbiddenValues(atomicDataTypePatch.getForbiddenValues());
        }
        if (atomicDataTypePatch.getMinimum() != null) {
            existing.setMinimum(atomicDataTypePatch.getMinimum());
        }
        if (atomicDataTypePatch.getMaximum() != null) {
            existing.setMaximum(atomicDataTypePatch.getMaximum());
        }
        if (atomicDataTypePatch.getInheritsFrom() != null) {
            existing.setInheritsFrom(atomicDataTypePatch.getInheritsFrom());
        }

        // Save the updated entity
        AtomicDataType saved = atomicDataTypeDao.save(existing);

        // Publish the patched event
        eventPublisher.publishEntityPatched(saved, previousVersion);

        log.info("Patched AtomicDataType with PID: {}", saved.getId());
        return saved;
    }
}
