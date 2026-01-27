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

import edu.kit.datamanager.idoris.core.domain.valueObjects.AttributeMapping;
import edu.kit.datamanager.idoris.core.domain.valueObjects.OperationStep;
import edu.kit.datamanager.idoris.operations.api.IOperationStepsService;
import edu.kit.datamanager.idoris.operations.dao.IOperationDao;
import edu.kit.datamanager.idoris.operations.dao.IOperationRelationshipDao;
import edu.kit.datamanager.idoris.operations.dao.IOperationStepDao;
import edu.kit.datamanager.idoris.operations.dto.OperationStepDto;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Observed(contextualName = "operationManagementService")
@RequiredArgsConstructor
class OperationManagementService implements IOperationStepsService {

    private final IOperationDao operationDao;
    private final IOperationStepDao stepDao;
    private final IOperationRelationshipDao relDao;

    @Override
    @Transactional(readOnly = true)
    public List<OperationStepDto> listSteps(String operationId) {
        ensureOperationExists(operationId);
        Iterable<OperationStep> steps = relDao.listSteps(operationId);
        return toDtos(steps);
    }

    // Helpers
    private void ensureOperationExists(String id) {
        if (operationDao.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Operation not found: " + id);
        }
    }

    private List<OperationStepDto> toDtos(Iterable<OperationStep> steps) {
        return java.util.stream.StreamSupport.stream(steps.spliterator(), false)
                .map(this::toDto)
                .toList();
    }

    private OperationStepDto toDto(OperationStep s) {
        return OperationStepDto.builder()
                .internalId(s.getId())
                .index(s.getIndex())
                .name(s.getName())
                .mode(s.getMode())
                .build();
    }

    @Override
    @Transactional
    public OperationStepDto createStep(String operationId, OperationStepDto stepDto) {
        ensureOperationExists(operationId);
        OperationStep step = new OperationStep();
        step.setIndex(stepDto.getIndex());
        step.setName(stepDto.getName());
        if (stepDto.getMode() != null) step.setMode(stepDto.getMode());
        OperationStep saved = stepDao.save(step);
        relDao.addStep(operationId, saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public void removeSteps(String operationId, Set<String> stepIds) {
        ensureOperationExists(operationId);
        if (stepIds == null || stepIds.isEmpty()) return;
        relDao.removeSteps(operationId, stepIds);
    }

    @Override
    @Transactional
    public void linkExistingSteps(String operationId, Collection<String> stepIds) {
        ensureOperationExists(operationId);
        if (stepIds == null) return;
        for (String sid : stepIds) {
            // verify step exists
            if (sid != null && stepDao.existsById(sid)) {
                relDao.addStep(operationId, sid);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listInputMappings(String stepId) {
        Iterable<AttributeMapping> mappings = relDao.listInputMappings(stepId);
        return toIds(mappings);
    }

    private List<String> toIds(Iterable<AttributeMapping> mappings) {
        return java.util.stream.StreamSupport.stream(mappings.spliterator(), false)
                .map(AttributeMapping::getId)
                .toList();
    }

    @Override
    @Transactional
    public void addInputMappings(String stepId, Set<String> mappingIds) {
        if (mappingIds == null || mappingIds.isEmpty()) return;
        relDao.addInputMappings(stepId, mappingIds);
    }

    @Override
    @Transactional
    public void removeInputMappings(String stepId, Set<String> mappingIds) {
        if (mappingIds == null || mappingIds.isEmpty()) return;
        relDao.removeInputMappings(stepId, mappingIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listOutputMappings(String stepId) {
        Iterable<AttributeMapping> mappings = relDao.listOutputMappings(stepId);
        return toIds(mappings);
    }

    @Override
    @Transactional
    public void addOutputMappings(String stepId, Set<String> mappingIds) {
        if (mappingIds == null || mappingIds.isEmpty()) return;
        relDao.addOutputMappings(stepId, mappingIds);
    }

    @Override
    @Transactional
    public void removeOutputMappings(String stepId, Set<String> mappingIds) {
        if (mappingIds == null || mappingIds.isEmpty()) return;
        relDao.removeOutputMappings(stepId, mappingIds);
    }
}
