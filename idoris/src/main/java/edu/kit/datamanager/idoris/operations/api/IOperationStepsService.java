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
package edu.kit.datamanager.idoris.operations.api;

import edu.kit.datamanager.idoris.operations.dto.OperationStepDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * External API for managing Operation steps and their attribute mappings.
 */
public interface IOperationStepsService {
    // Steps
    List<OperationStepDto> listSteps(String operationId);

    OperationStepDto createStep(String operationId, OperationStepDto step);

    void removeSteps(String operationId, Set<String> stepIds);

    void linkExistingSteps(String operationId, Collection<String> stepIds);

    // Input mappings
    List<String> listInputMappings(String stepId);

    void addInputMappings(String stepId, Set<String> mappingIds);

    void removeInputMappings(String stepId, Set<String> mappingIds);

    // Output mappings
    List<String> listOutputMappings(String stepId);

    void addOutputMappings(String stepId, Set<String> mappingIds);

    void removeOutputMappings(String stepId, Set<String> mappingIds);
}
