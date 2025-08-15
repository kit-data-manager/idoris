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
import edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.Optional;

/**
 * Repository interface for AtomicDataType entities.
 */
public interface IAtomicDataTypeDao extends IGenericRepo<AtomicDataType> {
    /**
     * Finds all AtomicDataType entities in the inheritance chain of the given AtomicDataType.
     *
     * @param pid the PID of the AtomicDataType
     * @return an Iterable of AtomicDataType entities in the inheritance chain
     */
    @Query("MATCH (d:AtomicDataType {pid: $pid})-[:inheritsFrom*]->(d2:AtomicDataType) RETURN d2")
    Iterable<AtomicDataType> findAllInInheritanceChainByPid(String pid);

    /**
     * Finds all AtomicDataType entities in the inheritance chain of the given AtomicDataType.
     *
     * @param internalId the internal ID of the AtomicDataType
     * @return an Iterable of AtomicDataType entities in the inheritance chain
     */
    @Query("MATCH (d:AtomicDataType {internalId: $internalId})-[:inheritsFrom*]->(d2:AtomicDataType) RETURN d2")
    Iterable<AtomicDataType> findAllInInheritanceChainByInternalId(String internalId);

    /**
     * Finds all AtomicDataType entities in the inheritance chain of the given AtomicDataType.
     * This method first tries to find the entity by PID, and if not found, tries to find it by internal ID.
     *
     * @param id the ID of the AtomicDataType (either PID or internal ID)
     * @return an Iterable of AtomicDataType entities in the inheritance chain
     */
    default Iterable<AtomicDataType> findAllInInheritanceChain(String id) {
        // First try to find by PID
        Optional<AtomicDataType> byPid = findByPid(id);
        if (byPid.isPresent()) {
            return findAllInInheritanceChainByPid(id);
        }
        // If not found by PID, try to find by internal ID
        return findAllInInheritanceChainByInternalId(id);
    }
}
