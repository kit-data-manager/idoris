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

import edu.kit.datamanager.idoris.core.domain.AtomicDataType;
import edu.kit.datamanager.idoris.core.domain.TypeProfile;
import edu.kit.datamanager.idoris.datatypes.api.IDataTypeExternalService;
import edu.kit.datamanager.idoris.datatypes.dao.IAtomicDataTypeDao;
import edu.kit.datamanager.idoris.datatypes.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.datatypes.dto.DataTypeDto;
import edu.kit.datamanager.idoris.datatypes.mappers.AtomicDataTypeMapper;
import edu.kit.datamanager.idoris.datatypes.mappers.TypeProfileMapper;
import edu.kit.datamanager.idoris.operations.api.IOperationExternalService;
import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for generic DataType operations.
 * Handles both AtomicDataType and TypeProfile entities.
 */
@Service
@Slf4j
@Observed(contextualName = "dataTypeDtoService")
class DataTypeDtoService implements IDataTypeExternalService {

    private final IAtomicDataTypeDao atomicDataTypeDao;
    private final ITypeProfileDao typeProfileDao;
    private final AtomicDataTypeMapper atomicDataTypeMapper;
    private final TypeProfileMapper typeProfileMapper;
    private final IOperationExternalService operationService;

    public DataTypeDtoService(IAtomicDataTypeDao atomicDataTypeDao,
                              ITypeProfileDao typeProfileDao,
                              AtomicDataTypeMapper atomicDataTypeMapper,
                              TypeProfileMapper typeProfileMapper,
                              IOperationExternalService operationService) {
        this.atomicDataTypeDao = atomicDataTypeDao;
        this.typeProfileDao = typeProfileDao;
        this.atomicDataTypeMapper = atomicDataTypeMapper;
        this.typeProfileMapper = typeProfileMapper;
        this.operationService = operationService;
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "dataTypeDtoService.list", histogram = true)
    @Counted(value = "dataTypeDtoService.list.count")
    public List<DataTypeDto> list() {
        log.debug("Listing all DataTypes");

        List<DataTypeDto> allDataTypes = new ArrayList<>();

        // Add all TypeProfiles
        List<TypeProfile> typeProfiles = typeProfileDao.findAll();
        typeProfiles.forEach(tp -> allDataTypes.add(typeProfileMapper.toDto(tp)));

        // Add all AtomicDataTypes
        List<AtomicDataType> atomicDataTypes = atomicDataTypeDao.findAll();
        atomicDataTypes.forEach(adt -> allDataTypes.add(atomicDataTypeMapper.toDto(adt)));

        return allDataTypes;
    }

    @Override
    @Transactional
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "dataTypeDtoService.delete", histogram = true)
    @Counted(value = "dataTypeDtoService.delete.count")
    public void delete(@SpanAttribute String id) {
        log.debug("Deleting DataType with ID: {}", id);

        // Try to find and delete as TypeProfile first
        Optional<TypeProfile> typeProfile = typeProfileDao.findById(id);
        if (typeProfile.isPresent()) {
            typeProfileDao.delete(typeProfile.get());
            return;
        }

        // Try to find and delete as AtomicDataType
        Optional<AtomicDataType> atomicDataType = atomicDataTypeDao.findById(id);
        if (atomicDataType.isPresent()) {
            atomicDataTypeDao.delete(atomicDataType.get());
            return;
        }

        throw new IllegalArgumentException("DataType not found: " + id);
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "dataTypeDtoService.getInheritanceHierarchy", histogram = true)
    @Counted(value = "dataTypeDtoService.getInheritanceHierarchy.count")
    public Object getInheritanceHierarchy(@SpanAttribute String id) {
        log.debug("Getting inheritance hierarchy for DataType: {}", id);

        // Check if it's a TypeProfile (which has inheritance)
        Optional<TypeProfile> typeProfile = typeProfileDao.findById(id);
        if (typeProfile.isPresent()) {
            // Use the inheritance chain logic from TypeProfile DAO
            Iterable<TypeProfile> inheritanceChain = typeProfileDao.findAllTypeProfilesInInheritanceChain(id);

            Map<String, Object> hierarchy = new HashMap<>();
            hierarchy.put("rootId", id);
            hierarchy.put("rootType", "PROFILE");

            List<Map<String, Object>> parents = new ArrayList<>();
            for (TypeProfile parent : inheritanceChain) {
                Map<String, Object> parentNode = new HashMap<>();
                parentNode.put("id", parent.getId());
                parentNode.put("name", parent.getName());
                parentNode.put("type", "PROFILE");
                parents.add(parentNode);
            }

            hierarchy.put("inheritsFrom", parents);
            return hierarchy;
        }

        // Check if it's an AtomicDataType
        Optional<AtomicDataType> atomicDataType = atomicDataTypeDao.findById(id);
        if (atomicDataType.isPresent()) {
            Map<String, Object> hierarchy = new HashMap<>();
            hierarchy.put("rootId", id);
            hierarchy.put("rootType", "ATOMIC");
            hierarchy.put("inheritsFrom", List.of()); // AtomicDataTypes don't have inheritance
            return hierarchy;
        }

        throw new IllegalArgumentException("DataType not found: " + id);
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "dataTypeDtoService.getOperationsForDataType", histogram = true)
    @Counted(value = "dataTypeDtoService.getOperationsForDataType.count")
    public List<Object> getOperationsForDataType(@SpanAttribute String id) {
        log.debug("Getting operations for DataType: {}", id);

        // Verify the DataType exists
        if (get(id).isEmpty()) {
            throw new IllegalArgumentException("DataType not found: " + id);
        }

        // Use the Operations external logic (DTO-first) to get operations for this DataType
        List<OperationResponseDto> operations = operationService.getOperationsForDataType(id);
        return operations.stream()
                .map(dto -> {
                    Map<String, Object> operationMap = new HashMap<>();
                    operationMap.put("id", dto.getInternalId());
                    operationMap.put("name", dto.getName());
                    operationMap.put("description", dto.getDescription());
                    operationMap.put("executableOnAttributeId", dto.getExecutableOnAttributeId());
                    operationMap.put("returnAttributeIds", dto.getReturnAttributeIds());
                    operationMap.put("environmentAttributeIds", dto.getEnvironmentAttributeIds());
                    operationMap.put("executionStepIds", dto.getExecutionStepIds());
                    return operationMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "dataTypeDtoService.get", histogram = true)
    @Counted(value = "dataTypeDtoService.get.count")
    public Optional<DataTypeDto> get(@SpanAttribute String id) {
        log.debug("Getting DataType with ID: {}", id);

        // Try to find as TypeProfile first
        Optional<TypeProfile> typeProfile = typeProfileDao.findById(id);
        if (typeProfile.isPresent()) {
            return Optional.of(typeProfileMapper.toDto(typeProfile.get()));
        }

        // Try to find as AtomicDataType
        Optional<AtomicDataType> atomicDataType = atomicDataTypeDao.findById(id);
        if (atomicDataType.isPresent()) {
            return Optional.of(atomicDataTypeMapper.toDto(atomicDataType.get()));
        }

        return Optional.empty();
    }
}
