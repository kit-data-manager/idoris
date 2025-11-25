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
package edu.kit.datamanager.idoris.datatypes.api;

import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * External API for TypeProfile operations (DTO-first).
 */
public interface ITypeProfileExternalService {
    TypeProfileDto create(TypeProfileDto dto);

    TypeProfileDto update(String id, TypeProfileDto dto);

    TypeProfileDto patch(String id, TypeProfileDto dto);

    void delete(String id);

    Optional<TypeProfileDto> get(String id);

    List<TypeProfileDto> list();

    // Relationship operations
    TypeProfileDto addInheritsFrom(String profileId, Set<String> parentIds);

    TypeProfileDto removeInheritsFrom(String profileId, Set<String> parentIds);

    TypeProfileDto addAttributes(String profileId, Set<String> attributeIds);

    TypeProfileDto removeAttributes(String profileId, Set<String> attributeIds);

    // Additional query operations
    Set<String> getInheritedAttributes(String id);

    Object getInheritanceTree(String id);

    List<Object> getOperationsForTypeProfile(String id);
}
