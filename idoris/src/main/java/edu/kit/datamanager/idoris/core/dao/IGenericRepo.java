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

package edu.kit.datamanager.idoris.core.dao;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IGenericRepo<T extends AdministrativeMetadata> extends Repository<T, String>, Neo4jRepository<T, String>, ListCrudRepository<T, String>, PagingAndSortingRepository<T, String> {
    /**
     * Finds an entity by its ID, which can be either a PID or an internal ID.
     * This method first tries to find the entity by PID, and if not found, tries to find it by internal ID.
     *
     * @param id The ID of the entity to find (either PID or internal ID)
     * @return An Optional containing the entity if found, or empty if not found
     */
    default Optional<T> findByPIDorInternalId(String id) {
        Optional<T> byPid = findByPid(id);
        return byPid.isPresent() ? byPid : findByInternalId(id);
    }

    /**
     * Finds an entity by its PID.
     * This method queries the PIDNode table to find the entity associated with the given PID.
     *
     * @param pid The PID of the entity to find
     * @return An Optional containing the entity if found, or empty if not found
     */
    @Query("MATCH (p:PIDNode {pid: $pid})-[:IDENTIFIES]->(e:IDORIS) RETURN e")
    Optional<T> findByPid(@Param("pid") String pid);

    /**
     * Finds an entity by its internal ID.
     * This method queries the PIDNode table to find the entity with the given internal ID.
     *
     * @param internalId The internal ID of the entity to find
     * @return An Optional containing the entity if found, or empty if not found
     */
    Optional<T> findByInternalId(@Param("internalId") String internalId);

    /**
     * Finds all PIDs associated with a given internal ID.
     * This method queries the PIDNode table to find all PIDs that identify the entity with the given internal ID.
     *
     * @param internalId The internal ID of the entity
     * @return A list of PIDNode objects associated with the internal ID
     */
    @Query("MATCH (p:PIDNode)-[:IDENTIFIES]->(e:IDORIS {internalId: $internalId}) RETURN p")
    List<String> findPidsByInternalId(@Param("internalId") String internalId);
}
