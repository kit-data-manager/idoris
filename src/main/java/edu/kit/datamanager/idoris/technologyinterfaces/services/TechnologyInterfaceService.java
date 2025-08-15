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

package edu.kit.datamanager.idoris.technologyinterfaces.services;

import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.technologyinterfaces.dao.ITechnologyInterfaceDao;
import edu.kit.datamanager.idoris.technologyinterfaces.entities.TechnologyInterface;
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
 * Service for managing TechnologyInterface entities.
 * This service provides methods for creating, updating, and retrieving TechnologyInterface entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
@Observed(contextualName = "technologyInterfaceService")
public class TechnologyInterfaceService {
    private final ITechnologyInterfaceDao technologyInterfaceDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new TechnologyInterfaceService with the given dependencies.
     *
     * @param technologyInterfaceDao the TechnologyInterface repository
     * @param eventPublisher         the event publisher service
     */
    public TechnologyInterfaceService(ITechnologyInterfaceDao technologyInterfaceDao, EventPublisherService eventPublisher) {
        this.technologyInterfaceDao = technologyInterfaceDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new TechnologyInterface entity.
     *
     * @param technologyInterface the TechnologyInterface entity to create
     * @return the created TechnologyInterface entity
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "technologyInterfaceService.createTechnologyInterface", description = "Time taken to create a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceService.createTechnologyInterface.count", description = "Number of technology interface creations")
    public TechnologyInterface createTechnologyInterface(@SpanAttribute TechnologyInterface technologyInterface) {
        log.debug("Creating TechnologyInterface: {}", technologyInterface);
        TechnologyInterface saved = technologyInterfaceDao.save(technologyInterface);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created TechnologyInterface with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Updates an existing TechnologyInterface entity.
     *
     * @param technologyInterface the TechnologyInterface entity to update
     * @return the updated TechnologyInterface entity
     * @throws IllegalArgumentException if the TechnologyInterface does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "technologyInterfaceService.updateTechnologyInterface", description = "Time taken to update a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceService.updateTechnologyInterface.count", description = "Number of technology interface updates")
    public TechnologyInterface updateTechnologyInterface(@SpanAttribute TechnologyInterface technologyInterface) {
        log.debug("Updating TechnologyInterface: {}", technologyInterface);

        if (technologyInterface.getId() == null || technologyInterface.getId().isEmpty()) {
            throw new IllegalArgumentException("TechnologyInterface must have a PID to be updated");
        }

        // Get the current version before updating
        TechnologyInterface existing = technologyInterfaceDao.findById(technologyInterface.getId())
                .orElseThrow(() -> new IllegalArgumentException("TechnologyInterface not found with PID: " + technologyInterface.getId()));

        Long previousVersion = existing.getVersion();

        TechnologyInterface saved = technologyInterfaceDao.save(technologyInterface);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated TechnologyInterface with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Deletes a TechnologyInterface entity.
     *
     * @param id the PID or internal ID of the TechnologyInterface to delete
     * @throws IllegalArgumentException if the TechnologyInterface does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "technologyInterfaceService.deleteTechnologyInterface", description = "Time taken to delete a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceService.deleteTechnologyInterface.count", description = "Number of technology interface deletions")
    public void deleteTechnologyInterface(@SpanAttribute("technologyInterface.id") String id) {
        log.debug("Deleting TechnologyInterface with ID: {}", id);

        TechnologyInterface technologyInterface = technologyInterfaceDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TechnologyInterface not found with ID: " + id));

        technologyInterfaceDao.delete(technologyInterface);
        eventPublisher.publishEntityDeleted(technologyInterface);
        log.info("Deleted TechnologyInterface with ID: {}", id);
    }

    /**
     * Retrieves a TechnologyInterface entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the TechnologyInterface to retrieve
     * @return an Optional containing the TechnologyInterface, or empty if not found
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "technologyInterfaceService.getTechnologyInterface", description = "Time taken to get a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceService.getTechnologyInterface.count", description = "Number of technology interface retrievals")
    public Optional<TechnologyInterface> getTechnologyInterface(@SpanAttribute("technologyInterface.id") String id) {
        log.debug("Retrieving TechnologyInterface with ID: {}", id);
        return technologyInterfaceDao.findById(id);
    }

    /**
     * Retrieves all TechnologyInterface entities.
     *
     * @return a list of all TechnologyInterface entities
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "technologyInterfaceService.getAllTechnologyInterfaces", description = "Time taken to get all technology interfaces", histogram = true)
    @Counted(value = "technologyInterfaceService.getAllTechnologyInterfaces.count", description = "Number of get all technology interfaces requests")
    public List<TechnologyInterface> getAllTechnologyInterfaces() {
        log.debug("Retrieving all TechnologyInterfaces");
        List<TechnologyInterface> interfaces = technologyInterfaceDao.findAll();
        log.info("Retrieved {} technology interfaces", interfaces.size());
        return interfaces;
    }

    /**
     * Partially updates an existing TechnologyInterface entity.
     *
     * @param id                       the PID or internal ID of the TechnologyInterface to patch
     * @param technologyInterfacePatch the partial TechnologyInterface entity with fields to update
     * @return the patched TechnologyInterface entity
     * @throws IllegalArgumentException if the TechnologyInterface does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "technologyInterfaceService.patchTechnologyInterface", description = "Time taken to patch a technology interface", histogram = true)
    @Counted(value = "technologyInterfaceService.patchTechnologyInterface.count", description = "Number of technology interface patches")
    public TechnologyInterface patchTechnologyInterface(@SpanAttribute("technologyInterface.id") String id, @SpanAttribute TechnologyInterface technologyInterfacePatch) {
        log.debug("Patching TechnologyInterface with ID: {}, patch: {}", id, technologyInterfacePatch);
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("TechnologyInterface ID cannot be null or empty");
        }

        // Get the current entity
        TechnologyInterface existing = technologyInterfaceDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TechnologyInterface not found with ID: " + id));
        Long previousVersion = existing.getVersion();

        // Apply non-null fields from the patch to the existing entity
        if (technologyInterfacePatch.getName() != null) {
            existing.setName(technologyInterfacePatch.getName());
        }
        if (technologyInterfacePatch.getDescription() != null) {
            existing.setDescription(technologyInterfacePatch.getDescription());
        }
        if (technologyInterfacePatch.getAttributes() != null) {
            existing.setAttributes(technologyInterfacePatch.getAttributes());
        }
        if (technologyInterfacePatch.getOutputs() != null) {
            existing.setOutputs(technologyInterfacePatch.getOutputs());
        }
        if (technologyInterfacePatch.getAdapters() != null) {
            existing.setAdapters(technologyInterfacePatch.getAdapters());
        }

        // Save the updated entity
        TechnologyInterface saved = technologyInterfaceDao.save(existing);

        // Publish the patched event
        eventPublisher.publishEntityPatched(saved, previousVersion);

        log.info("Patched TechnologyInterface with PID: {}", saved.getId());
        return saved;
    }
}
