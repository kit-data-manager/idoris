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
package edu.kit.datamanager.idoris.technologyinterfaces.api;

import edu.kit.datamanager.idoris.technologyinterfaces.dto.TechnologyInterfaceDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * External API for Technology Interface operations exposed to web/controllers and other modules.
 * DTO-first contract.
 */
public interface ITechnologyInterfaceService {
    TechnologyInterfaceDto create(TechnologyInterfaceDto dto);

    TechnologyInterfaceDto update(String id, TechnologyInterfaceDto dto);

    TechnologyInterfaceDto patch(String id, TechnologyInterfaceDto dto);

    void delete(String id);

    Optional<TechnologyInterfaceDto> get(String id);

    List<TechnologyInterfaceDto> list();

    // Relationship operations (by IDs). Implemented idempotently by the logic.
    TechnologyInterfaceDto linkInputs(String technologyInterfaceId, Set<String> attributeIds);

    TechnologyInterfaceDto unlinkInputs(String technologyInterfaceId, Set<String> attributeIds);

    TechnologyInterfaceDto linkOutputs(String technologyInterfaceId, Set<String> attributeIds);

    TechnologyInterfaceDto unlinkOutputs(String technologyInterfaceId, Set<String> attributeIds);
}
