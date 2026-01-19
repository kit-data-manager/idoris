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
package edu.kit.datamanager.idoris.datatypes.api;

import edu.kit.datamanager.idoris.datatypes.dto.DataTypeDto;

import java.util.List;
import java.util.Optional;

/**
 * External API for generic DataType operations (DTO-first).
 * This logic provides operations that work with any DataType subtype.
 */
public interface IDataTypeExternalService {

    /**
     * Retrieves a DataType by its ID.
     *
     * @param id the ID of the DataType (PID or internal ID)
     * @return Optional containing the DataType DTO, or empty if not found
     */
    Optional<DataTypeDto> get(String id);

    /**
     * Lists all DataType entities.
     *
     * @return list of all DataType DTOs
     */
    List<DataTypeDto> list();

    /**
     * Deletes a DataType by its ID.
     *
     * @param id the ID of the DataType to delete
     */
    void delete(String id);

    /**
     * Gets the inheritance hierarchy for a DataType.
     * Works with both AtomicDataType and TypeProfile entities.
     *
     * @param id the ID of the DataType
     * @return the inheritance hierarchy
     */
    Object getInheritanceHierarchy(String id);

    /**
     * Gets operations available for a DataType.
     *
     * @param id the ID of the DataType
     * @return list of operations that can be executed on this DataType
     */
    List<Object> getOperationsForDataType(String id);

    /**
     * Checks if one DataType inherits from another.
     *
     * @param childId  the ID of the child DataType
     * @param parentId the ID of the parent DataType
     * @return true if childId inherits from parentId, false otherwise
     */
    Boolean inheritsFrom(String childId, String parentId);
}
