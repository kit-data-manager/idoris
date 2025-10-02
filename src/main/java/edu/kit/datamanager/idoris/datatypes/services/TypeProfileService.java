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

import edu.kit.datamanager.idoris.core.domain.TypeProfile;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.datatypes.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.rules.validation.ValidationPolicyValidator;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
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
 * Service for managing TypeProfile entities.
 * This logic provides methods for creating, updating, and retrieving TypeProfile entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
@Observed(contextualName = "typeProfileService")
public class TypeProfileService {
    private final ITypeProfileDao typeProfileDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new TypeProfileService with the given dependencies.
     *
     * @param typeProfileDao the TypeProfile repository
     * @param eventPublisher the event publisher logic
     */
    public TypeProfileService(ITypeProfileDao typeProfileDao, EventPublisherService eventPublisher) {
        this.typeProfileDao = typeProfileDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new TypeProfile entity.
     *
     * @param typeProfile the TypeProfile entity to create
     * @return the created TypeProfile entity
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileService.createTypeProfile", description = "Time taken to create a type profile", histogram = true)
    @Counted(value = "typeProfileService.createTypeProfile.count", description = "Number of type profile creations")
    public TypeProfile createTypeProfile(@SpanAttribute TypeProfile typeProfile) {
        log.debug("Creating TypeProfile: {}", typeProfile);
        TypeProfile saved = typeProfileDao.save(typeProfile);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created TypeProfile with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Updates an existing TypeProfile entity.
     *
     * @param typeProfile the TypeProfile entity to update
     * @return the updated TypeProfile entity
     * @throws IllegalArgumentException if the TypeProfile does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileService.updateTypeProfile", description = "Time taken to update a type profile", histogram = true)
    @Counted(value = "typeProfileService.updateTypeProfile.count", description = "Number of type profile updates")
    public TypeProfile updateTypeProfile(@SpanAttribute TypeProfile typeProfile) {
        log.debug("Updating TypeProfile: {}", typeProfile);
        if (typeProfile.getId() == null || typeProfile.getId().isEmpty()) {
            throw new IllegalArgumentException("TypeProfile must have a PID to be updated");
        }
        // Get the current version before updating
        TypeProfile existing = typeProfileDao.findById(typeProfile.getId())
                .orElseThrow(() -> new IllegalArgumentException("TypeProfile not found with PID: " + typeProfile.getId()));
        Long previousVersion = existing.getVersion();
        TypeProfile saved = typeProfileDao.save(typeProfile);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated TypeProfile with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Deletes a TypeProfile entity.
     *
     * @param id the PID or internal ID of the TypeProfile to delete
     * @throws IllegalArgumentException if the TypeProfile does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileService.deleteTypeProfile", description = "Time taken to delete a type profile", histogram = true)
    @Counted(value = "typeProfileService.deleteTypeProfile.count", description = "Number of type profile deletions")
    public void deleteTypeProfile(@SpanAttribute String id) {
        log.debug("Deleting TypeProfile with ID: {}", id);

        TypeProfile typeProfile = typeProfileDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TypeProfile not found with ID: " + id));

        typeProfileDao.delete(typeProfile);
        eventPublisher.publishEntityDeleted(typeProfile);
        log.info("Deleted TypeProfile with ID: {}", id);
    }

    /**
     * Retrieves a TypeProfile entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the TypeProfile to retrieve
     * @return an Optional containing the TypeProfile, or empty if not found
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileService.getTypeProfile", description = "Time taken to get a type profile", histogram = true)
    @Counted(value = "typeProfileService.getTypeProfile.count", description = "Number of type profile retrievals")
    public Optional<TypeProfile> getTypeProfile(@SpanAttribute String id) {
        log.debug("Retrieving TypeProfile with ID: {}", id);
        return typeProfileDao.findById(id);
    }

    /**
     * Retrieves all TypeProfile entities.
     *
     * @return a list of all TypeProfile entities
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileService.getAllTypeProfiles", description = "Time taken to get all type profiles", histogram = true)
    @Counted(value = "typeProfileService.getAllTypeProfiles.count", description = "Number of get all type profiles requests")
    public List<TypeProfile> getAllTypeProfiles() {
        log.debug("Retrieving all TypeProfiles");
        return typeProfileDao.findAll();
    }

    /**
     * Validates a TypeProfile entity.
     *
     * @param id the PID or internal ID of the TypeProfile to validate
     * @return the validation result
     * @throws IllegalArgumentException if the TypeProfile does not exist
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileService.validateTypeProfile", description = "Time taken to validate a type profile", histogram = true)
    @Counted(value = "typeProfileService.validateTypeProfile.count", description = "Number of type profile validations")
    public ValidationResult validateTypeProfile(@SpanAttribute String id) {
        log.debug("Validating TypeProfile with ID: {}", id);

        TypeProfile typeProfile = typeProfileDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TypeProfile not found with ID: " + id));

        ValidationPolicyValidator validator = new ValidationPolicyValidator();
        return typeProfile.execute(validator);
    }

    /**
     * Retrieves all TypeProfiles in the inheritance chain of a TypeProfile.
     *
     * @param id the PID or internal ID of the TypeProfile
     * @return an Iterable of TypeProfiles in the inheritance chain
     * @throws IllegalArgumentException if the TypeProfile does not exist
     */
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileService.getInheritanceChain", description = "Time taken to get inheritance chain", histogram = true)
    @Counted(value = "typeProfileService.getInheritanceChain.count", description = "Number of inheritance chain retrievals")
    public Iterable<TypeProfile> getInheritanceChain(@SpanAttribute String id) {
        log.debug("Retrieving inheritance chain for TypeProfile with ID: {}", id);

        TypeProfile typeProfile = typeProfileDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TypeProfile not found with ID: " + id));

        return typeProfileDao.findAllTypeProfilesInInheritanceChain(typeProfile.getId());
    }

    /**
     * Partially updates an existing TypeProfile entity.
     *
     * @param id               the PID or internal ID of the TypeProfile to patch
     * @param typeProfilePatch the partial TypeProfile entity with fields to update
     * @return the patched TypeProfile entity
     * @throws IllegalArgumentException if the TypeProfile does not exist
     */
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileService.patchTypeProfile", description = "Time taken to patch a type profile", histogram = true)
    @Counted(value = "typeProfileService.patchTypeProfile.count", description = "Number of type profile patches")
    public TypeProfile patchTypeProfile(@SpanAttribute String id, @SpanAttribute TypeProfile typeProfilePatch) {
        log.debug("Patching TypeProfile with ID: {}, patch: {}", id, typeProfilePatch);
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("TypeProfile ID cannot be null or empty");
        }

        // Get the current entity
        TypeProfile existing = typeProfileDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TypeProfile not found with ID: " + id));
        Long previousVersion = existing.getVersion();

        // Apply non-null fields from the patch to the existing entity
        if (typeProfilePatch.getName() != null) {
            existing.setName(typeProfilePatch.getName());
        }
        if (typeProfilePatch.getDescription() != null) {
            existing.setDescription(typeProfilePatch.getDescription());
        }
        if (typeProfilePatch.getAttributes() != null && !typeProfilePatch.getAttributes().isEmpty()) {
            existing.setAttributes(typeProfilePatch.getAttributes());
        }
        if (typeProfilePatch.getInheritsFrom() != null && !typeProfilePatch.getInheritsFrom().isEmpty()) {
            existing.setInheritsFrom(typeProfilePatch.getInheritsFrom());
        }

        // Save the updated entity
        TypeProfile saved = typeProfileDao.save(existing);

        // Publish the patched event
        eventPublisher.publishEntityPatched(saved, previousVersion);

        log.info("Patched TypeProfile with PID: {}", saved.getId());
        return saved;
    }
}
