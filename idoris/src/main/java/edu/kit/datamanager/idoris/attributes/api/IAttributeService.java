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
package edu.kit.datamanager.idoris.attributes.api;

import edu.kit.datamanager.idoris.attributes.dto.AttributeDto;

import java.util.List;
import java.util.Optional;

/**
 * External API for Attribute operations exposed to web/controllers and other modules.
 * DTO-first contract.
 */
public interface IAttributeService {
    AttributeDto create(AttributeDto dto);

    AttributeDto update(String id, AttributeDto dto);

    AttributeDto patch(String id, AttributeDto dto);

    void delete(String id);

    Optional<AttributeDto> get(String id);

    List<AttributeDto> list();

    // Relationship operations by IDs
    AttributeDto setDataType(String attributeId, String dataTypeId);

    AttributeDto setOverride(String attributeId, String overrideAttributeId);

    AttributeDto removeOverride(String attributeId);
}
