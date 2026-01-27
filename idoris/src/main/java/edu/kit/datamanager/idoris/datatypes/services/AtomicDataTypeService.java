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

package edu.kit.datamanager.idoris.datatypes.services;

// ... existing code ...

import edu.kit.datamanager.idoris.core.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.core.domain.AtomicDataType;
import edu.kit.datamanager.idoris.core.domain.ValidationResult;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.core.exceptions.ValidationException;
import edu.kit.datamanager.idoris.core.services.RuleService;
import edu.kit.datamanager.idoris.datatypes.api.IAtomicDataTypeService;
import edu.kit.datamanager.idoris.datatypes.dao.IAtomicDataTypeDao;
import edu.kit.datamanager.idoris.datatypes.dto.AtomicDataTypeDto;
import edu.kit.datamanager.idoris.datatypes.mappers.AtomicDataTypeMapper;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing AtomicDataType entities.
 * This service provides methods for creating, updating, and retrieving AtomicDataType entities using DTOs exclusively.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
@Observed(contextualName = "atomicDataTypeService")
public class AtomicDataTypeService implements IAtomicDataTypeService {
    private final IAtomicDataTypeDao atomicDataTypeDao;
    private final EventPublisherService eventPublisher;
    private final AtomicDataTypeMapper mapper;
    private final RuleService ruleService;
    private final ApplicationProperties applicationProperties;

    /**
     * Creates a new AtomicDataTypeService with the given dependencies.
     *
     * @param atomicDataTypeDao     the AtomicDataType repository
     * @param eventPublisher        the event publisher service
     * @param mapper                the AtomicDataType mapper
     * @param ruleService           the rule service for validation
     * @param applicationProperties the application properties
     */
    public AtomicDataTypeService(IAtomicDataTypeDao atomicDataTypeDao, EventPublisherService eventPublisher, AtomicDataTypeMapper mapper, RuleService ruleService, ApplicationProperties applicationProperties) {
        this.atomicDataTypeDao = atomicDataTypeDao;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
        this.ruleService = ruleService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.create", description = "Time taken to create an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.create.count", description = "Number of atomic data type creations")
    public AtomicDataTypeDto create(@Valid AtomicDataTypeDto dto) {
        log.debug("Creating AtomicDataType DTO: {}", dto.getName());

        AtomicDataType entity = mapper.toEntity(dto);
        entity.setInternalId(null);
        entity.setVersion(null);

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                entity,
                ValidationResult::new
        );
        log.debug("Validation result for AtomicDataType {}: {}", entity, validationResult);

        // Check if validation failed based on validation policy
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        AtomicDataType saved = atomicDataTypeDao.save(entity);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created AtomicDataType with PID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.update", description = "Time taken to update an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.update.count", description = "Number of atomic data type updates")
    public AtomicDataTypeDto update(String id, AtomicDataTypeDto dto) {
        log.debug("Updating AtomicDataType with ID: {}", id);

        AtomicDataType existing = atomicDataTypeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));

        Long previousVersion = existing.getVersion();

        // Apply the DTO to the existing entity
        mapper.applyPatch(dto, existing);

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                existing,
                ValidationResult::new
        );
        log.debug("Validation result for AtomicDataType {}: {}", existing, validationResult);

        // Check if validation failed
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        AtomicDataType saved = atomicDataTypeDao.save(existing);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated AtomicDataType with PID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.patch", description = "Time taken to patch an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.patch.count", description = "Number of atomic data type patches")
    public AtomicDataTypeDto patch(String id, AtomicDataTypeDto dto) {
        log.debug("Patching AtomicDataType with ID: {}", id);

        AtomicDataType existing = atomicDataTypeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));

        Long previousVersion = existing.getVersion();

        // Apply the patch
        mapper.applyPatch(dto, existing);

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                existing,
                ValidationResult::new
        );

        // Check if validation failed
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        AtomicDataType saved = atomicDataTypeDao.save(existing);
        eventPublisher.publishEntityPatched(saved, previousVersion);
        log.info("Patched AtomicDataType with PID: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.delete", description = "Time taken to delete an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.delete.count", description = "Number of atomic data type deletions")
    public void delete(@SpanAttribute("atomicDataType.id") String id) {
        log.debug("Deleting AtomicDataType with ID: {}", id);

        AtomicDataType atomicDataType = atomicDataTypeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));

        atomicDataTypeDao.delete(atomicDataType);
        eventPublisher.publishEntityDeleted(atomicDataType);
        log.info("Deleted AtomicDataType with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.get", description = "Time taken to get an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.get.count", description = "Number of atomic data type retrievals")
    public Optional<AtomicDataTypeDto> get(@SpanAttribute("atomicDataType.id") String id) {
        log.debug("Retrieving AtomicDataType with ID: {}", id);
        return atomicDataTypeDao.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.list", description = "Time taken to get all atomic data types", histogram = true)
    @Counted(value = "atomicDataTypeService.list.count", description = "Number of get all atomic data types requests")
    public List<AtomicDataTypeDto> list() {
        log.debug("Retrieving all AtomicDataTypes");
        return atomicDataTypeDao.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.setInheritsFrom", description = "Time taken to set inheritance for an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.setInheritsFrom.count", description = "Number of set inheritance operations")
    public AtomicDataTypeDto setInheritsFrom(String id, String parentId) {
        log.debug("Setting inheritance for AtomicDataType with ID: {} to parent: {}", id, parentId);

        AtomicDataType existing = atomicDataTypeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));

        // Find the parent AtomicDataType
        AtomicDataType parent = atomicDataTypeDao.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent AtomicDataType not found with ID: " + parentId));

        Long previousVersion = existing.getVersion();

        // Set the inheritance relationship
        existing.setInheritsFrom(parent);

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                existing,
                ValidationResult::new
        );

        // Check if validation failed
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        AtomicDataType saved = atomicDataTypeDao.save(existing);
        eventPublisher.publishEntityUpdated(saved, previousVersion);

        log.info("Set inheritance for AtomicDataType {} to parent {}", saved.getId(), parentId);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "atomicDataTypeService.detachInheritsFrom", description = "Time taken to detach inheritance for an atomic data type", histogram = true)
    @Counted(value = "atomicDataTypeService.detachInheritsFrom.count", description = "Number of detach inheritance operations")
    public AtomicDataTypeDto detachInheritsFrom(String id) {
        log.debug("Detaching inheritance for AtomicDataType with ID: {}", id);

        AtomicDataType existing = atomicDataTypeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));

        Long previousVersion = existing.getVersion();

        // Remove the inheritance relationship
        existing.setInheritsFrom(null);

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                existing,
                ValidationResult::new
        );

        // Check if validation failed
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        AtomicDataType saved = atomicDataTypeDao.save(existing);
        eventPublisher.publishEntityUpdated(saved, previousVersion);

        log.info("Detached inheritance for AtomicDataType {}", saved.getId());
        return mapper.toDto(saved);
    }

    private boolean hasValidationErrors(ValidationResult validationResult) {
        return validationResult.getOutputMessages()
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel())
                        && !entry.getValue().isEmpty());
    }

//    // Legacy methods for backward compatibility - delegate to DTO methods
//
//    /**
//     * @deprecated Use create(AtomicDataTypeDto) instead
//     */
//    @Deprecated
//    @Transactional
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.createAtomicDataType", description = "Time taken to create an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.createAtomicDataType.count", description = "Number of atomic data type creations")
//    public AtomicDataType createAtomicDataType(AtomicDataType atomicDataType) {
//        AtomicDataTypeDto dto = mapper.toDto(atomicDataType);
//        AtomicDataTypeDto created = create(dto);
//        return mapper.toEntity(created);
//    }
//
//    /**
//     * @deprecated Use update(String, AtomicDataTypeDto) instead
//     */
//    @Deprecated
//    @Transactional
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.updateAtomicDataType", description = "Time taken to update an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.updateAtomicDataType.count", description = "Number of atomic data type updates")
//    public AtomicDataType updateAtomicDataType(AtomicDataType atomicDataType) {
//        AtomicDataTypeDto dto = mapper.toDto(atomicDataType);
//        AtomicDataTypeDto updated = update(atomicDataType.getId(), dto);
//        return mapper.toEntity(updated);
//    }
//
//    /**
//     * @deprecated Use delete(String) instead
//     */
//    @Deprecated
//    public void deleteAtomicDataType(String id) {
//        delete(id);
//    }
//
//    /**
//     * @deprecated Use get(String) instead
//     */
//    @Deprecated
//    @Transactional(readOnly = true)
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.getAtomicDataType", description = "Time taken to get an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.getAtomicDataType.count", description = "Number of atomic data type retrievals")
//    public Optional<AtomicDataType> getAtomicDataType(@SpanAttribute("atomicDataType.id") String id) {
//        return get(id).map(mapper::toEntity);
//    }
//
//    /**
//     * @deprecated Use list() instead
//     */
//    @Deprecated
//    @Transactional(readOnly = true)
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.getAllAtomicDataTypes", description = "Time taken to get all atomic data types", histogram = true)
//    @Counted(value = "atomicDataTypeService.getAllAtomicDataTypes.count", description = "Number of get all atomic data types requests")
//    public List<AtomicDataType> getAllAtomicDataTypes() {
//        return list().stream()
//                .map(mapper::toEntity)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * @deprecated Use patch(String, AtomicDataTypeDto) instead
//     */
//    @Deprecated
//    @Transactional
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.patchAtomicDataType", description = "Time taken to patch an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.patchAtomicDataType.count", description = "Number of atomic data type patches")
//    public AtomicDataType patchAtomicDataType(@SpanAttribute("atomicDataType.id") String id, AtomicDataType atomicDataTypePatch) {
//        AtomicDataTypeDto dto = mapper.toDto(atomicDataTypePatch);
//        AtomicDataTypeDto patched = patch(id, dto);
//        return mapper.toEntity(patched);
//    }
}
//
/// **
// * Service for managing AtomicDataType entities.
// * This logic provides methods for creating, updating, and retrieving AtomicDataType entities.
// * It publishes domain events when entities are created, updated, or deleted.
// */
//@Service
//@Slf4j
//@Observed(contextualName = "atomicDataTypeService")
//public class AtomicDataTypeService {
//    private final IAtomicDataTypeDao atomicDataTypeDao;
//    private final EventPublisherService eventPublisher;
//
//    /**
//     * Creates a new AtomicDataTypeService with the given dependencies.
//     *
//     * @param atomicDataTypeDao the AtomicDataType repository
//     * @param eventPublisher    the event publisher logic
//     */
//    public AtomicDataTypeService(IAtomicDataTypeDao atomicDataTypeDao, EventPublisherService eventPublisher) {
//        this.atomicDataTypeDao = atomicDataTypeDao;
//        this.eventPublisher = eventPublisher;
//    }
//
//    /**
//     * Creates a new AtomicDataType entity.
//     *
//     * @param atomicDataType the AtomicDataType entity to create
//     * @return the created AtomicDataType entity
//     */
//    @Transactional
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.createAtomicDataType", description = "Time taken to create an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.createAtomicDataType.count", description = "Number of atomic data type creations")
//    public AtomicDataType createAtomicDataType(AtomicDataType atomicDataType) {
//        log.debug("Creating AtomicDataType: {}", atomicDataType);
//        atomicDataType.setInternalId(null);
//        atomicDataType.setVersion(null);
//        AtomicDataType saved = atomicDataTypeDao.save(atomicDataType);
//        eventPublisher.publishEntityCreated(saved);
//        log.info("Created AtomicDataType with PID: {}", saved.getId());
//        return saved;
//    }
//
//    /**
//     * Updates an existing AtomicDataType entity.
//     *
//     * @param atomicDataType the AtomicDataType entity to update
//     * @return the updated AtomicDataType entity
//     * @throws IllegalArgumentException if the AtomicDataType does not exist
//     */
//    @Transactional
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.updateAtomicDataType", description = "Time taken to update an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.updateAtomicDataType.count", description = "Number of atomic data type updates")
//    public AtomicDataType updateAtomicDataType(AtomicDataType atomicDataType) {
//        log.debug("Updating AtomicDataType: {}", atomicDataType);
//
//        if (atomicDataType.getId() == null || atomicDataType.getId().isEmpty()) {
//            throw new IllegalArgumentException("AtomicDataType must have a PID to be updated");
//        }
//
//        // Get the current version before updating
//        AtomicDataType existing = atomicDataTypeDao.findById(atomicDataType.getId())
//                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with PID: " + atomicDataType.getId()));
//
//        Long previousVersion = existing.getVersion();
//
//        AtomicDataType saved = atomicDataTypeDao.save(atomicDataType);
//        eventPublisher.publishEntityUpdated(saved, previousVersion);
//        log.info("Updated AtomicDataType with PID: {}", saved.getId());
//        return saved;
//    }
//
//    /**
//     * Deletes an AtomicDataType entity.
//     *
//     * @param id the PID or internal ID of the AtomicDataType to delete
//     * @throws IllegalArgumentException if the AtomicDataType does not exist
//     */
//    @Transactional
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.deleteAtomicDataType", description = "Time taken to delete an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.deleteAtomicDataType.count", description = "Number of atomic data type deletions")
//    public void deleteAtomicDataType(@SpanAttribute("atomicDataType.id") String id) {
//        log.debug("Deleting AtomicDataType with ID: {}", id);
//
//        AtomicDataType atomicDataType = atomicDataTypeDao.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));
//
//        atomicDataTypeDao.delete(atomicDataType);
//        eventPublisher.publishEntityDeleted(atomicDataType);
//        log.info("Deleted AtomicDataType with ID: {}", id);
//    }
//
//    /**
//     * Retrieves an AtomicDataType entity by its PID or internal ID.
//     *
//     * @param id the PID or internal ID of the AtomicDataType to retrieve
//     * @return an Optional containing the AtomicDataType, or empty if not found
//     */
//    @Transactional(readOnly = true)
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.getAtomicDataType", description = "Time taken to get an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.getAtomicDataType.count", description = "Number of atomic data type retrievals")
//    public Optional<AtomicDataType> getAtomicDataType(@SpanAttribute("atomicDataType.id") String id) {
//        log.debug("Retrieving AtomicDataType with ID: {}", id);
//        return atomicDataTypeDao.findById(id);
//    }
//
//    /**
//     * Retrieves all AtomicDataType entities.
//     *
//     * @return a list of all AtomicDataType entities
//     */
//    @Transactional(readOnly = true)
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.getAllAtomicDataTypes", description = "Time taken to get all atomic data types", histogram = true)
//    @Counted(value = "atomicDataTypeService.getAllAtomicDataTypes.count", description = "Number of get all atomic data types requests")
//    public List<AtomicDataType> getAllAtomicDataTypes() {
//        log.debug("Retrieving all AtomicDataTypes");
//        return atomicDataTypeDao.findAll();
//    }
//
//    /**
//     * Partially updates an existing AtomicDataType entity.
//     *
//     * @param id                  the PID or internal ID of the AtomicDataType to patch
//     * @param atomicDataTypePatch the partial AtomicDataType entity with fields to update
//     * @return the patched AtomicDataType entity
//     * @throws IllegalArgumentException if the AtomicDataType does not exist
//     */
//    @Transactional
//    @WithSpan(kind = SpanKind.INTERNAL)
//    @Timed(value = "atomicDataTypeService.patchAtomicDataType", description = "Time taken to patch an atomic data type", histogram = true)
//    @Counted(value = "atomicDataTypeService.patchAtomicDataType.count", description = "Number of atomic data type patches")
//    public AtomicDataType patchAtomicDataType(@SpanAttribute("atomicDataType.id") String id, AtomicDataType atomicDataTypePatch) {
//        log.debug("Patching AtomicDataType with ID: {}, patch: {}", id, atomicDataTypePatch);
//        if (id == null || id.isEmpty()) {
//            throw new IllegalArgumentException("AtomicDataType ID cannot be null or empty");
//        }
//
//        // Get the current entity
//        AtomicDataType existing = atomicDataTypeDao.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with ID: " + id));
//        Long previousVersion = existing.getVersion();
//
//        // Apply non-null fields from the patch to the existing entity
//        if (atomicDataTypePatch.getName() != null) {
//            existing.setName(atomicDataTypePatch.getName());
//        }
//        if (atomicDataTypePatch.getDescription() != null) {
//            existing.setDescription(atomicDataTypePatch.getDescription());
//        }
//        if (atomicDataTypePatch.getDefaultValue() != null) {
//            existing.setDefaultValue(atomicDataTypePatch.getDefaultValue());
//        }
//        if (atomicDataTypePatch.getPrimitiveDataType() != null) {
//            existing.setPrimitiveDataType(atomicDataTypePatch.getPrimitiveDataType());
//        }
//        if (atomicDataTypePatch.getRegularExpression() != null) {
//            existing.setRegularExpression(atomicDataTypePatch.getRegularExpression());
//        }
//        if (atomicDataTypePatch.getPermittedValues() != null) {
//            existing.setPermittedValues(atomicDataTypePatch.getPermittedValues());
//        }
//        if (atomicDataTypePatch.getForbiddenValues() != null) {
//            existing.setForbiddenValues(atomicDataTypePatch.getForbiddenValues());
//        }
//        if (atomicDataTypePatch.getMinimum() != null) {
//            existing.setMinimum(atomicDataTypePatch.getMinimum());
//        }
//        if (atomicDataTypePatch.getMaximum() != null) {
//            existing.setMaximum(atomicDataTypePatch.getMaximum());
//        }
//        if (atomicDataTypePatch.getInheritsFrom() != null) {
//            existing.setInheritsFrom(atomicDataTypePatch.getInheritsFrom());
//        }
//
//        // Save the updated entity
//        AtomicDataType saved = atomicDataTypeDao.save(existing);
//
//        // Publish the patched event
//        eventPublisher.publishEntityPatched(saved, previousVersion);
//
//        log.info("Patched AtomicDataType with PID: {}", saved.getId());
//        return saved;
//    }
//}
