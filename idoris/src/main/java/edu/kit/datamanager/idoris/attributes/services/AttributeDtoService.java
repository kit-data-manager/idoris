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

import edu.kit.datamanager.idoris.attributes.api.IAttributeExternalService;
import edu.kit.datamanager.idoris.attributes.api.IAttributeInternalService;
import edu.kit.datamanager.idoris.attributes.dao.IAttributeDao;
import edu.kit.datamanager.idoris.attributes.dto.AttributeDto;
import edu.kit.datamanager.idoris.attributes.mappers.AttributeMapper;
import edu.kit.datamanager.idoris.core.domain.Attribute;
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
 * DTO-first logic for Attributes, implementing exported external and internal APIs.
 * Keeps entity-based controller untouched for now; controller migration follows later.
 */
@Service
@Slf4j
@Observed(contextualName = "attributeDtoService")
public class AttributeDtoService implements IAttributeExternalService, IAttributeInternalService {

    private final IAttributeDao attributeDao;
    private final EventPublisherService eventPublisher;
    private final AttributeMapper mapper;

    public AttributeDtoService(IAttributeDao attributeDao, EventPublisherService eventPublisher, AttributeMapper mapper) {
        this.attributeDao = attributeDao;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
    }

    // ===== External API =====

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "attributeDtoService.create", description = "Time taken to create attribute DTO", histogram = true)
    @Counted(value = "attributeDtoService.create.count", description = "Number of attribute DTO creations")
    public AttributeDto create(@SpanAttribute AttributeDto dto) {
        Attribute entity = mapper.toEntity(dto);
        Attribute saved = attributeDao.save(entity);
        // Publish module-scoped event
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.attributes.events.AttributeCreatedEvent(saved.getId(), mapper.toDto(saved)));
        // Link relationships if provided
        if (dto.getDataTypeId() != null && !dto.getDataTypeId().isBlank()) {
            attributeDao.setDataType(saved.getId(), dto.getDataTypeId());
        }
        if (dto.getOverrideId() != null && !dto.getOverrideId().isBlank()) {
            attributeDao.setOverride(saved.getId(), dto.getOverrideId());
        }
        Attribute reloaded = attributeDao.findById(saved.getId()).orElse(saved);
        return mapper.toDto(reloaded);
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    public AttributeDto update(String id, AttributeDto dto) {
        Attribute existing = attributeDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + id));
        Long previousVersion = existing.getVersion();
        mapper.applyPatch(dto, existing); // scalar fields only
        Attribute saved = attributeDao.save(existing);
        // Publish module-scoped updated event
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.attributes.events.AttributeUpdatedEvent(saved.getId(), previousVersion, mapper.toDto(saved)));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    public AttributeDto patch(String id, AttributeDto dto) {
        Attribute existing = attributeDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + id));
        Long previousVersion = existing.getVersion();
        mapper.applyPatch(dto, existing);
        Attribute saved = attributeDao.save(existing);
        // Publish module-scoped patched event
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.attributes.events.AttributePatchedEvent(saved.getId(), previousVersion, mapper.toDto(saved)));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    public void delete(@SpanAttribute("attribute.id") String id) {
        Attribute existing = attributeDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Attribute not found: " + id));
        // Publish module-scoped deleted event before deletion to include full payload
        eventPublisher.publishEvent(new edu.kit.datamanager.idoris.attributes.events.AttributeDeletedEvent(existing.getId(), mapper.toDto(existing)));
        attributeDao.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttributeDto> get(String id) {
        return attributeDao.findById(id).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDto> list() {
        return attributeDao.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public AttributeDto setDataType(String attributeId, String dataTypeId) {
        attributeDao.setDataType(attributeId, dataTypeId);
        return get(attributeId).orElseThrow();
    }

    @Override
    @Transactional
    public AttributeDto detachDataType(String attributeId) {
        attributeDao.detachDataType(attributeId);
        return get(attributeId).orElseThrow();
    }

    @Override
    @Transactional
    public AttributeDto setOverride(String attributeId, String overrideAttributeId) {
        attributeDao.setOverride(attributeId, overrideAttributeId);
        return get(attributeId).orElseThrow();
    }

    @Override
    @Transactional
    public AttributeDto detachOverride(String attributeId) {
        attributeDao.detachOverride(attributeId);
        return get(attributeId).orElseThrow();
    }

    // ===== Internal API =====

    @Override
    @Transactional(readOnly = true)
    public void ensureExists(String id) {
        if (attributeDao.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Attribute not found: " + id);
        }
    }

    @Override
    @Transactional
    public void setDataTypeInternal(String attributeId, String dataTypeId) {
        attributeDao.setDataType(attributeId, dataTypeId);
    }

    @Override
    @Transactional
    public void detachDataTypeInternal(String attributeId) {
        attributeDao.detachDataType(attributeId);
    }

    @Override
    @Transactional
    public void setOverrideInternal(String attributeId, String overrideAttributeId) {
        attributeDao.setOverride(attributeId, overrideAttributeId);
    }

    @Override
    @Transactional
    public void detachOverrideInternal(String attributeId) {
        attributeDao.detachOverride(attributeId);
    }
}
