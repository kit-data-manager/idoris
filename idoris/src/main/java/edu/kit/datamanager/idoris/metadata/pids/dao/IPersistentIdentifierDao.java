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
package edu.kit.datamanager.idoris.metadata.pids.dao;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.domain.valueObjects.PID;
import edu.kit.datamanager.idoris.metadata.pids.domain.PIDNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PIDNode nodes.
 */
public interface IPersistentIdentifierDao extends Neo4jRepository<PIDNode, PID>, PagingAndSortingRepository<PIDNode, PID> {


    /**
     * Finds a PIDNode by the entity it identifies.
     *
     * @param entity The entity to find the PID for
     * @return An Optional containing the PIDNode if found, or empty if not found
     */
    Optional<PIDNode> findByEntity(AdministrativeMetadata entity);

    /**
     * Finds a PIDNode by the internal ID of the entity it identifies.
     *
     * @param entityInternalId The internal ID of the entity to find the PID for
     * @return An Optional containing the PIDNode if found, or empty if not found
     */
    Optional<PIDNode> findByEntityInternalId(String entityInternalId);

    /**
     * Finds all PersistentIdentifiers for entities of the given type.
     *
     * @param entityType The type of entity to find PIDs for
     * @return A list of PersistentIdentifiers for entities of the given type
     */
    List<PIDNode> findByEntityType(String entityType);

    /**
     * Finds all PersistentIdentifiers that are tombstones (entity has been deleted).
     *
     * @return A list of PersistentIdentifiers that are tombstones
     */
    List<PIDNode> findByTombstoneTrue();

    /**
     * Finds all PersistentIdentifiers that are not tombstones (entity has not been deleted).
     *
     * @return A list of PersistentIdentifiers that are not tombstones
     */
    List<PIDNode> findByTombstoneFalse();

    /**
     * Resolves the PID for an entity by its internalId via the IDENTIFIES relation.
     *
     * @param internalId entity internal id
     * @return Optional PID string
     */
    @Query("MATCH (p:PIDNode)-[:IDENTIFIES]->(e) WHERE e.internalId = $entityInternalId RETURN p")
    List<PIDNode> findPidsByEntityInternalId(@Param("entityInternalId") String internalId);
}
