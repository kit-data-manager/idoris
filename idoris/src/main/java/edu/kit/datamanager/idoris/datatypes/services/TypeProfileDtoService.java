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

import edu.kit.datamanager.idoris.core.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.core.domain.TypeProfile;
import edu.kit.datamanager.idoris.core.domain.ValidationResult;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.core.exceptions.ValidationException;
import edu.kit.datamanager.idoris.datatypes.api.ITypeProfileService;
import edu.kit.datamanager.idoris.datatypes.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileDto;
import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileInheritance;
import edu.kit.datamanager.idoris.datatypes.mappers.TypeProfileMapper;
import edu.kit.datamanager.idoris.datatypes.rules.TypeProfileValidationPolicyValidator;
import edu.kit.datamanager.idoris.operations.api.IOperationService;
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
 * DTO-first logic for TypeProfiles, implementing exported external and internal APIs.
 * Service implementation is package-private to enforce module boundaries.
 */
@Service
@Slf4j
@Observed(contextualName = "typeProfileDtoService")
class TypeProfileDtoService implements ITypeProfileService {

    private final ITypeProfileDao typeProfileDao;
    private final EventPublisherService eventPublisher;
    private final TypeProfileMapper mapper;
    private final ApplicationProperties appProps;
    private final IOperationService operationService;

    public TypeProfileDtoService(ITypeProfileDao typeProfileDao, EventPublisherService eventPublisher, TypeProfileMapper mapper, ApplicationProperties appProps, IOperationService operationService) {
        this.typeProfileDao = typeProfileDao;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
        this.appProps = appProps;
        this.operationService = operationService;
    }

    // ===== External API =====

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileDtoService.create", histogram = true)
    @Counted(value = "typeProfileDtoService.create.count")
    public TypeProfileDto create(@SpanAttribute TypeProfileDto dto) {
        log.debug("Creating TypeProfile DTO: {}", dto.getName());
        TypeProfile entity = mapper.toEntity(dto);
        TypeProfile saved = typeProfileDao.save(entity);
        // Handle relationships if provided
        if (dto.getInheritsFromIds() != null && !dto.getInheritsFromIds().isEmpty()) {
            typeProfileDao.addInheritsFrom(saved.getId(), dto.getInheritsFromIds());
        }
        if (dto.getAttributeIds() != null && !dto.getAttributeIds().isEmpty()) {
            typeProfileDao.addAttributes(saved.getId(), dto.getAttributeIds());
        }
        // Reload to include relationships and run validation before event publishing
        TypeProfile reloaded = typeProfileDao.findById(saved.getId()).orElse(saved);
        validateOrThrow(reloaded);
        // Publish module-scoped created event after successful validation
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.datatypes.events.TypeProfileCreatedEvent(reloaded.getId(), mapper.toDto(reloaded)));
        return mapper.toDto(reloaded);
    }

    /**
     * Validate the given TypeProfile based on application validation policy and throw if invalid.
     * STRICT: errors or warnings cause failure. LAX: only errors cause failure.
     */
    private void validateOrThrow(TypeProfile typeProfile) {
        TypeProfileValidationPolicyValidator validator = new TypeProfileValidationPolicyValidator();
        ValidationResult result = typeProfile.execute(validator);
        boolean strict = appProps.getValidationPolicy() == ApplicationProperties.ValidationPolicy.STRICT;
        boolean hasErrors = result.getErrorCount() > 0;
        boolean hasWarnings = result.getWarningCount() > 0;
        if (hasErrors || (strict && hasWarnings)) {
            String message = "TypeProfile validation failed: " + result;
            throw new ValidationException(message, result);
        }
    }

    @Override
    @Transactional
    public TypeProfileDto update(String id, TypeProfileDto dto) {
        TypeProfile existing = typeProfileDao.findById(id).orElseThrow(() -> new IllegalArgumentException("TypeProfile not found: " + id));
        Long previousVersion = existing.getVersion();
        mapper.applyPatch(dto, existing);
        TypeProfile saved = typeProfileDao.save(existing);
        // Reload and validate before publishing event
        TypeProfile reloaded = typeProfileDao.findById(saved.getId()).orElse(saved);
        validateOrThrow(reloaded);
        // Publish module-scoped updated event
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.datatypes.events.TypeProfileUpdatedEvent(reloaded.getId(), previousVersion, mapper.toDto(reloaded)));
        return mapper.toDto(reloaded);
    }

    @Override
    @Transactional
    public TypeProfileDto patch(String id, TypeProfileDto dto) {
        TypeProfile existing = typeProfileDao.findById(id).orElseThrow(() -> new IllegalArgumentException("TypeProfile not found: " + id));
        Long previousVersion = existing.getVersion();
        mapper.applyPatch(dto, existing);
        TypeProfile saved = typeProfileDao.save(existing);
        // Reload and validate before publishing event
        TypeProfile reloaded = typeProfileDao.findById(saved.getId()).orElse(saved);
        validateOrThrow(reloaded);
        // Publish module-scoped patched event
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.datatypes.events.TypeProfilePatchedEvent(reloaded.getId(), previousVersion, mapper.toDto(reloaded)));
        return mapper.toDto(reloaded);
    }

    @Override
    @Transactional
    public void delete(String id) {
        TypeProfile existing = typeProfileDao.findById(id).orElseThrow(() -> new IllegalArgumentException("TypeProfile not found: " + id));
        // Publish module-scoped deleted event before deletion
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.datatypes.events.TypeProfileDeletedEvent(existing.getId(), mapper.toDto(existing)));
        typeProfileDao.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TypeProfileDto> get(String id) {
        return typeProfileDao.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeProfileDto> list() {
        return typeProfileDao.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public TypeProfileDto addInheritsFrom(String profileId, Set<String> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) return get(profileId).orElseThrow();
        typeProfileDao.addInheritsFrom(profileId, parentIds);
        return get(profileId).orElseThrow();
    }

    @Override
    @Transactional
    public TypeProfileDto removeInheritsFrom(String profileId, Set<String> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) return get(profileId).orElseThrow();
        typeProfileDao.removeInheritsFrom(profileId, parentIds);
        return get(profileId).orElseThrow();
    }

    @Override
    @Transactional
    public TypeProfileDto addAttributes(String profileId, Set<String> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) return get(profileId).orElseThrow();
        typeProfileDao.addAttributes(profileId, attributeIds);
        return get(profileId).orElseThrow();
    }

    // ===== Internal API =====

    @Override
    @Transactional
    public TypeProfileDto removeAttributes(String profileId, Set<String> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty()) return get(profileId).orElseThrow();
        typeProfileDao.removeAttributes(profileId, attributeIds);
        return get(profileId).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileDtoService.getInheritedAttributes", histogram = true)
    @Counted(value = "typeProfileDtoService.getInheritedAttributes.count")
    public Set<String> getInheritedAttributes(@SpanAttribute String id) {
        log.debug("Getting inherited attributes for TypeProfile: {}", id);
        // Find all TypeProfiles in the inheritance chain with their attributes
        Iterable<TypeProfile> inheritanceChain = typeProfileDao.findAllTypeProfilesWithTheirAttributesInInheritanceChain(id);

        Set<String> inheritedAttributeIds = new java.util.HashSet<>();
        for (TypeProfile profile : inheritanceChain) {
            if (profile.getAttributes() != null) {
                profile.getAttributes().forEach(attribute -> {
                    if (attribute.getId() != null) {
                        inheritedAttributeIds.add(attribute.getId());
                    }
                });
            }
        }

        return inheritedAttributeIds;
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileDtoService.getInheritanceTree", histogram = true)
    @Counted(value = "typeProfileDtoService.getInheritanceTree.count")
    public Object getInheritanceTree(@SpanAttribute String id) {
        log.debug("Getting inheritance tree for TypeProfile: {}", id);

        // Get the root TypeProfile to get its name
        TypeProfile rootProfile = typeProfileDao.findById(id).orElse(null);
        if (rootProfile == null) {
            throw new IllegalArgumentException("TypeProfile not found: " + id);
        }

        // Find all TypeProfiles in the inheritance chain
        Iterable<TypeProfile> inheritanceChain = typeProfileDao.findAllTypeProfilesInInheritanceChain(id);

        // Convert to TypeProfileInheritance structure
        java.util.List<TypeProfileInheritance.TypeProfileNode> parents = new java.util.ArrayList<>();
        int level = 0;
        for (TypeProfile parent : inheritanceChain) {
            java.util.List<String> directAttributes = new java.util.ArrayList<>();
            if (parent.getAttributes() != null) {
                parent.getAttributes().forEach(attr -> directAttributes.add(attr.getId()));
            }

            TypeProfileInheritance.TypeProfileNode parentNode =
                    TypeProfileInheritance.TypeProfileNode.builder()
                            .id(parent.getId())
                            .name(parent.getName().toString())
                            .description(parent.getDescription().toString())
                            .level(level++)
                            .directAttributes(directAttributes)
                            .build();
            parents.add(parentNode);
        }

        return TypeProfileInheritance.builder()
                .rootId(id)
                .rootName(rootProfile.getName().toString())
                .inheritsFrom(parents)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "typeProfileDtoService.getOperationsForTypeProfile", histogram = true)
    @Counted(value = "typeProfileDtoService.getOperationsForTypeProfile.count")
    public List<Object> getOperationsForTypeProfile(@SpanAttribute String id) {
        log.debug("Getting operations for TypeProfile: {}", id);

        // Verify the TypeProfile exists
        if (typeProfileDao.findById(id).isEmpty()) {
            throw new IllegalArgumentException("TypeProfile not found: " + id);
        }

        // Get operations that can be executed on this TypeProfile via external logic (DTO-first)
        java.util.List<edu.kit.datamanager.idoris.operations.dto.OperationResponseDto> operations = operationService.getOperationsForDataType(id);

        // Convert OperationDto to response maps for the API response
        java.util.List<Object> operationList = new java.util.ArrayList<>();
        for (edu.kit.datamanager.idoris.operations.dto.OperationResponseDto dto : operations) {
            java.util.Map<String, Object> operationMap = new java.util.HashMap<>();
            operationMap.put("id", dto.getInternalId());
            operationMap.put("name", dto.getName());
            operationMap.put("description", dto.getDescription());
            operationMap.put("executableOnAttributeId", dto.getExecutableOnAttributeId());
            operationMap.put("returnAttributeIds", dto.getReturnAttributeIds());
            operationMap.put("environmentAttributeIds", dto.getEnvironmentAttributeIds());
            operationMap.put("executionStepIds", dto.getExecutionStepIds());
            operationList.add(operationMap);
        }

        return operationList;
    }
}
