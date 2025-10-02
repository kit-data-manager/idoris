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
package edu.kit.datamanager.idoris.operations.api;

import edu.kit.datamanager.idoris.operations.dto.OperationRequestDto;
import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;

import java.util.List;
import java.util.Optional;

/**
 * External API for Operation (DTO-first) used by controllers and other modules.
 */
public interface IOperationExternalService {
    OperationResponseDto create(OperationRequestDto dto);

    OperationResponseDto update(String id, OperationRequestDto dto);

    OperationResponseDto patch(String id, OperationRequestDto dto);

    void delete(String id);

    Optional<OperationResponseDto> get(String id);

    List<OperationResponseDto> list();

    List<OperationResponseDto> getOperationsForDataType(String dataTypeId);

    ValidationResult validate(String id);
}
