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
package edu.kit.datamanager.idoris.operations.services;

import edu.kit.datamanager.idoris.core.domain.Operation;
import edu.kit.datamanager.idoris.core.domain.ValidationResult;
import edu.kit.datamanager.idoris.core.domain.valueObjects.AttributeMapping;
import edu.kit.datamanager.idoris.core.domain.valueObjects.OperationStep;
import edu.kit.datamanager.idoris.core.services.RuleService;
import edu.kit.datamanager.idoris.operations.api.IOperationService;
import edu.kit.datamanager.idoris.operations.dao.IAttributeMappingDao;
import edu.kit.datamanager.idoris.operations.dao.IOperationDao;
import edu.kit.datamanager.idoris.operations.dao.IOperationRelationshipDao;
import edu.kit.datamanager.idoris.operations.dao.IOperationStepDao;
import edu.kit.datamanager.idoris.operations.dto.AttributeMappingDto;
import edu.kit.datamanager.idoris.operations.dto.OperationRequestDto;
import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
import edu.kit.datamanager.idoris.operations.dto.OperationStepDto;
import edu.kit.datamanager.idoris.operations.events.OperationCreatedEvent;
import edu.kit.datamanager.idoris.operations.events.OperationDeletedEvent;
import edu.kit.datamanager.idoris.operations.events.OperationPatchedEvent;
import edu.kit.datamanager.idoris.operations.events.OperationUpdatedEvent;
import edu.kit.datamanager.idoris.operations.mappers.OperationMapper;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Observed(contextualName = "operationDtoService")
@RequiredArgsConstructor
class OperationDtoService implements IOperationService {

    private final OperationService operationService;
    private final IOperationDao operationDao;
    private final OperationMapper mapper;
    private final IOperationStepDao stepDao;
    private final IOperationRelationshipDao relDao;
    private final IAttributeMappingDao attributeMappingDao;
    private final edu.kit.datamanager.idoris.core.events.EventPublisherService eventPublisher;

    @Autowired
    private RuleService ruleService;

    @Override
    @Transactional
    public OperationResponseDto create(OperationRequestDto dto) {
        log.debug("Creating Operation DTO: {}", dto.getName());
        Operation entity = mapper.toEntity(dto);
        Operation saved = operationService.createOperation(entity);

        // Nested creation: steps and mappings
        if (dto.getExecutionSteps() != null && !dto.getExecutionSteps().isEmpty()) {
            for (OperationStepDto stepDto : dto.getExecutionSteps()) {
                createStepRecursive(saved.getId(), null, stepDto);
            }
        }

        Operation reloaded = operationDao.findById(saved.getId()).orElse(saved);
        OperationResponseDto createdDto = mapper.toResponseDto(reloaded);
        eventPublisher.publishEvent(new OperationCreatedEvent(reloaded.getId(), createdDto));
        return createdDto;
    }

    @Override
    @Transactional
    public OperationResponseDto update(String id, OperationRequestDto dto) {
        Operation existing = operationDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Operation not found: " + id));
        Long previousVersion = existing.getVersion();
        existing = mapper.applyPatch(dto, existing);
        Operation saved = operationService.updateOperation(existing);
        Operation reloaded = operationDao.findById(saved.getId()).orElse(saved);
        OperationResponseDto updatedDto = mapper.toResponseDto(reloaded);
        eventPublisher.publishEvent(new OperationUpdatedEvent(reloaded.getId(), previousVersion, updatedDto));
        return updatedDto;
    }

    @Override
    @Transactional
    public OperationResponseDto patch(String id, OperationRequestDto dto) {
        Operation existing = operationDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Operation not found: " + id));
        Long previousVersion = existing.getVersion();
        existing = mapper.applyPatch(dto, existing);
        Operation saved = operationService.patchOperation(id, existing);
        Operation reloaded = operationDao.findById(saved.getId()).orElse(saved);
        OperationResponseDto patchedDto = mapper.toResponseDto(reloaded);
        eventPublisher.publishEvent(new OperationPatchedEvent(reloaded.getId(), previousVersion, patchedDto));
        return patchedDto;
    }

    @Override
    @Transactional
    public void delete(String id) {
        Optional<Operation> existing = operationDao.findById(id);
        OperationResponseDto payload = existing.map(mapper::toResponseDto).orElse(null);
        operationService.deleteOperation(id);
        if (payload != null) {
            eventPublisher.publishEvent(new OperationDeletedEvent(id, payload));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OperationResponseDto> get(String id) {
        return operationService.getOperation(id).map(mapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationResponseDto> list() {
        return operationService.getAllOperations().stream().map(mapper::toResponseDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationResponseDto> getOperationsForDataType(String dataTypeId) {
        Iterable<Operation> ops = operationService.getOperationsForDataType(dataTypeId);
        return java.util.stream.StreamSupport.stream(ops.spliterator(), false)
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResult validate(String id) {
        Operation op = operationService.getOperation(id).orElseThrow(() -> new IllegalArgumentException("Operation not found: " + id));
        return ruleService.executeRules(RuleTask.VALIDATE, op, ValidationResult::new);
    }

    private String createStepRecursive(String operationId, String parentStepId, OperationStepDto stepDto) {
        OperationStep step = new OperationStep();
        if (stepDto.getIndex() != null) step.setIndex(stepDto.getIndex());
        if (stepDto.getName() != null) step.setName(stepDto.getName());
        if (stepDto.getMode() != null) step.setMode(stepDto.getMode());
        OperationStep savedStep = stepDao.save(step);

        // Link to operation or parent step
        if (parentStepId == null) {
            relDao.addStep(operationId, savedStep.getId());
        } else {
            relDao.addSubStep(parentStepId, savedStep.getId());
        }

        // Optional executeOperation / useTechnology
        if (stepDto.getExecuteOperationId() != null && !stepDto.getExecuteOperationId().isBlank()) {
            relDao.setStepExecuteOperation(savedStep.getId(), stepDto.getExecuteOperationId());
        }
        if (stepDto.getUseTechnologyId() != null && !stepDto.getUseTechnologyId().isBlank()) {
            relDao.setStepUseTechnology(savedStep.getId(), stepDto.getUseTechnologyId());
        }

        // Input mappings
        if (stepDto.getInputMappings() != null) {
            for (AttributeMappingDto m : stepDto.getInputMappings()) {
                String mappingId = createAttributeMapping(m).getId();
                relDao.addInputMappings(savedStep.getId(), java.util.List.of(mappingId));
                // Link to Attributes if provided
                if (m.getInputAttributeId() != null && !m.getInputAttributeId().isBlank()) {
                    attributeMappingDao.linkInputAttribute(mappingId, m.getInputAttributeId());
                }
                if (m.getOutputAttributeId() != null && !m.getOutputAttributeId().isBlank()) {
                    attributeMappingDao.linkOutputAttribute(mappingId, m.getOutputAttributeId());
                }
            }
        }
        // Output mappings
        if (stepDto.getOutputMappings() != null) {
            for (AttributeMappingDto m : stepDto.getOutputMappings()) {
                String mappingId = createAttributeMapping(m).getId();
                relDao.addOutputMappings(savedStep.getId(), java.util.List.of(mappingId));
                if (m.getInputAttributeId() != null && !m.getInputAttributeId().isBlank()) {
                    attributeMappingDao.linkInputAttribute(mappingId, m.getInputAttributeId());
                }
                if (m.getOutputAttributeId() != null && !m.getOutputAttributeId().isBlank()) {
                    attributeMappingDao.linkOutputAttribute(mappingId, m.getOutputAttributeId());
                }
            }
        }

        // Sub-steps
        if (stepDto.getSubSteps() != null && !stepDto.getSubSteps().isEmpty()) {
            for (OperationStepDto child : stepDto.getSubSteps()) {
                createStepRecursive(operationId, savedStep.getId(), child);
            }
        }

        return savedStep.getId();
    }

    private AttributeMapping createAttributeMapping(AttributeMappingDto dto) {
        AttributeMapping m = new AttributeMapping();
        m.setName(dto.getName());
        if (dto.getReplaceCharactersInValueWithInput() != null)
            m.setReplaceCharactersInValueWithInput(dto.getReplaceCharactersInValueWithInput());
        if (dto.getValue() != null) m.setValue(dto.getValue());
        if (dto.getIndex() != null) m.setIndex(dto.getIndex());
        return attributeMappingDao.save(m);
    }
}
