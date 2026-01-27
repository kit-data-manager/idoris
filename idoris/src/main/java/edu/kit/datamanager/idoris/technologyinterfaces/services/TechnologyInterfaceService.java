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

package edu.kit.datamanager.idoris.technologyinterfaces.services;

import edu.kit.datamanager.idoris.attributes.api.IAttributeService;
import edu.kit.datamanager.idoris.core.domain.TechnologyInterface;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.technologyinterfaces.api.ITechnologyInterfaceService;
import edu.kit.datamanager.idoris.technologyinterfaces.dao.ITechnologyInterfaceDao;
import edu.kit.datamanager.idoris.technologyinterfaces.dto.TechnologyInterfaceDto;
import edu.kit.datamanager.idoris.technologyinterfaces.mappers.TechnologyInterfaceMapper;
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
import java.util.Set;

/**
 * Service for managing TechnologyInterface entities.
 * This logic provides methods for creating, updating, and retrieving TechnologyInterface entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
@Observed(contextualName = "technologyInterfaceService")
class TechnologyInterfaceService implements ITechnologyInterfaceService {
    private final ITechnologyInterfaceDao technologyInterfaceDao;
    private final EventPublisherService eventPublisher;
    private final TechnologyInterfaceMapper mapper;
    private final IAttributeService attributeService;

    /**
     * Creates a new TechnologyInterfaceService with the given dependencies.
     *
     * @param technologyInterfaceDao the TechnologyInterface repository
     * @param eventPublisher         the event publisher logic
     * @param mapper                 the mapper for DTO/entity conversion
     */
    public TechnologyInterfaceService(ITechnologyInterfaceDao technologyInterfaceDao, EventPublisherService eventPublisher, TechnologyInterfaceMapper mapper, IAttributeService attributeService) {
        this.technologyInterfaceDao = technologyInterfaceDao;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
        this.attributeService = attributeService;
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

    @Override
    @Transactional
    public TechnologyInterfaceDto create(TechnologyInterfaceDto dto) {
        TechnologyInterface entity = mapper.toEntity(dto);
        TechnologyInterface saved = technologyInterfaceDao.save(entity);
        // Validate and handle relationships if provided
        ensureAttributesExist(dto.getAttributeIds());
        ensureAttributesExist(dto.getOutputIds());
        if (dto.getAttributeIds() != null && !dto.getAttributeIds().isEmpty()) {
            technologyInterfaceDao.linkInputs(saved.getId(), dto.getAttributeIds());
        }
        if (dto.getOutputIds() != null && !dto.getOutputIds().isEmpty()) {
            technologyInterfaceDao.linkOutputs(saved.getId(), dto.getOutputIds());
        }
        // Reload to include relationships
        TechnologyInterface reloaded = technologyInterfaceDao.findById(saved.getId()).orElse(saved);
        // Publish module-scoped created event after successful composite linking
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.technologyinterfaces.events.TechnologyInterfaceCreatedEvent(reloaded.getId(), mapper.toDto(reloaded)));
        return mapper.toDto(reloaded);
    }

    // ===================== DTO-first External API =====================

    private void ensureAttributesExist(Set<String> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) return;
        for (String attrId : attributeIds) {
            assert attributeService.get(attrId).isPresent();
        }
    }

    @Override
    @Transactional
    public TechnologyInterfaceDto update(String id, TechnologyInterfaceDto dto) {
        TechnologyInterface existing = technologyInterfaceDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TechnologyInterface not found with ID: " + id));
        Long previousVersion = existing.getVersion();
        // For minimal impact, update scalar fields only; relations managed by dedicated ops
        mapper.applyPatch(dto, existing);
        TechnologyInterface saved = technologyInterfaceDao.save(existing);
        // Publish module-scoped updated event
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.technologyinterfaces.events.TechnologyInterfaceUpdatedEvent(saved.getId(), previousVersion, mapper.toDto(saved)));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public TechnologyInterfaceDto patch(String id, TechnologyInterfaceDto dto) {
        TechnologyInterface existing = technologyInterfaceDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TechnologyInterface not found with ID: " + id));
        Long previousVersion = existing.getVersion();
        mapper.applyPatch(dto, existing);
        TechnologyInterface saved = technologyInterfaceDao.save(existing);
        // Publish module-scoped patched event
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.technologyinterfaces.events.TechnologyInterfacePatchedEvent(saved.getId(), previousVersion, mapper.toDto(saved)));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(String id) {
        // Publish module-scoped deleted event before deletion to include full payload
        TechnologyInterface existing = technologyInterfaceDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TechnologyInterface not found with ID: " + id));
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.technologyinterfaces.events.TechnologyInterfaceDeletedEvent(existing.getId(), mapper.toDto(existing)));
        technologyInterfaceDao.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TechnologyInterfaceDto> get(String id) {
        return technologyInterfaceDao.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnologyInterfaceDto> list() {
        return technologyInterfaceDao.findAll().stream().map(mapper::toDto).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public TechnologyInterfaceDto linkInputs(String technologyInterfaceId, Set<String> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) return get(technologyInterfaceId).orElseThrow();
        ensureAttributesExist(attributeIds);
        technologyInterfaceDao.linkInputs(technologyInterfaceId, attributeIds);
        return get(technologyInterfaceId).orElseThrow();
    }

    @Override
    @Transactional
    public TechnologyInterfaceDto unlinkInputs(String technologyInterfaceId, Set<String> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) return get(technologyInterfaceId).orElseThrow();
        technologyInterfaceDao.unlinkInputs(technologyInterfaceId, attributeIds);
        return get(technologyInterfaceId).orElseThrow();
    }

    @Override
    @Transactional
    public TechnologyInterfaceDto linkOutputs(String technologyInterfaceId, Set<String> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) return get(technologyInterfaceId).orElseThrow();
        ensureAttributesExist(attributeIds);
        technologyInterfaceDao.linkOutputs(technologyInterfaceId, attributeIds);
        return get(technologyInterfaceId).orElseThrow();
    }

    @Override
    @Transactional
    public TechnologyInterfaceDto unlinkOutputs(String technologyInterfaceId, Set<String> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) return get(technologyInterfaceId).orElseThrow();
        technologyInterfaceDao.unlinkOutputs(technologyInterfaceId, attributeIds);
        return get(technologyInterfaceId).orElseThrow();
    }
}
