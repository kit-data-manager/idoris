/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.datatypes.dao;

import edu.kit.datamanager.idoris.core.domain.dao.IGenericRepo;
import edu.kit.datamanager.idoris.datatypes.entities.DataType;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.Optional;

/**
 * Repository interface for DataType entities.
 */
public interface IDataTypeDao extends IGenericRepo<DataType> {
    /**
     * Finds all DataType entities in the inheritance chain of the given DataType.
     *
     * @param pid the PID of the DataType
     * @return an Iterable of DataType entities in the inheritance chain
     */
    @Query("MATCH (d:DataType {pid: $pid})-[:inheritsFrom*]->(d2:DataType) RETURN d2")
    Iterable<DataType> findAllInInheritanceChainByPid(String pid);

    /**
     * Finds all DataType entities in the inheritance chain of the given DataType.
     *
     * @param internalId the internal ID of the DataType
     * @return an Iterable of DataType entities in the inheritance chain
     */
    @Query("MATCH (d:DataType {internalId: $internalId})-[:inheritsFrom*]->(d2:DataType) RETURN d2")
    Iterable<DataType> findAllInInheritanceChainByInternalId(String internalId);

    /**
     * Finds all DataType entities in the inheritance chain of the given DataType.
     * This method first tries to find the entity by PID, and if not found, tries to find it by internal ID.
     *
     * @param id the ID of the DataType (either PID or internal ID)
     * @return an Iterable of DataType entities in the inheritance chain
     */
    default Iterable<DataType> findAllInInheritanceChain(String id) {
        // First try to find by PID
        Optional<DataType> byPid = findByPid(id);
        if (byPid.isPresent()) {
            return findAllInInheritanceChainByPid(id);
        }
        // If not found by PID, try to find by internal ID
        return findAllInInheritanceChainByInternalId(id);
    }

    /**
     * Gets operations that can be executed on a data type.
     * This method finds operations that are executable on the given data type, its attributes,
     * or any data type in its inheritance chain.
     *
     * @param pid the PID of the data type
     * @return an Iterable of Operation entities
     */
    @Query("Match (:DataType {pid: $pid})-[:attributes|inheritsFrom*]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) return o")
    Iterable<Operation> getOperationsByPid(String pid);

    /**
     * Gets operations that can be executed on a data type.
     * This method finds operations that are executable on the given data type, its attributes,
     * or any data type in its inheritance chain.
     *
     * @param internalId the internal ID of the data type
     * @return an Iterable of Operation entities
     */
    @Query("Match (:DataType {internalId: $internalId})-[:attributes|inheritsFrom*]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) return o")
    Iterable<Operation> getOperationsByInternalId(String internalId);

    /**
     * Gets operations that can be executed on a data type.
     * This method finds operations that are executable on the given data type, its attributes,
     * or any data type in its inheritance chain.
     * This method first tries to find the entity by PID, and if not found, tries to find it by internal ID.
     *
     * @param id the ID of the data type (either PID or internal ID)
     * @return an Iterable of Operation entities
     */
    default Iterable<Operation> getOperations(String id) {
        // First try to find by PID
        Optional<DataType> byPid = findByPid(id);
        if (byPid.isPresent()) {
            return getOperationsByPid(id);
        }
        // If not found by PID, try to find by internal ID
        return getOperationsByInternalId(id);
    }
}
