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

package edu.kit.datamanager.idoris.pids.repositories;

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PersistentIdentifier entities.
 * This interface provides methods for CRUD operations on PersistentIdentifier entities.
 */
@Repository
public interface PersistentIdentifierRepository extends Neo4jRepository<PersistentIdentifier, String> {

    /**
     * Finds a PersistentIdentifier by the entity it identifies.
     *
     * @param entity The entity to find the PID for
     * @return An Optional containing the PersistentIdentifier if found, or empty if not found
     */
    Optional<PersistentIdentifier> findByEntity(AdministrativeMetadata entity);

    /**
     * Finds a PersistentIdentifier by the internal ID of the entity it identifies.
     *
     * @param entityInternalId The internal ID of the entity to find the PID for
     * @return An Optional containing the PersistentIdentifier if found, or empty if not found
     */
    Optional<PersistentIdentifier> findByEntityInternalId(String entityInternalId);

    /**
     * Finds all PersistentIdentifiers for entities of the given type.
     *
     * @param entityType The type of entity to find PIDs for
     * @return A list of PersistentIdentifiers for entities of the given type
     */
    List<PersistentIdentifier> findByEntityType(String entityType);

    /**
     * Finds all PersistentIdentifiers that are tombstones (entity has been deleted).
     *
     * @return A list of PersistentIdentifiers that are tombstones
     */
    List<PersistentIdentifier> findByTombstoneTrue();

    /**
     * Finds all PersistentIdentifiers that are not tombstones (entity has not been deleted).
     *
     * @return A list of PersistentIdentifiers that are not tombstones
     */
    List<PersistentIdentifier> findByTombstoneFalse();
}