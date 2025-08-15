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

package edu.kit.datamanager.idoris.pids;

import edu.kit.datamanager.idoris.configuration.TypedPIDMakerConfig;
import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import edu.kit.datamanager.idoris.pids.services.PersistentIdentifierService;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.neo4j.core.schema.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ID generator that uses the PersistentIdentifierService to generate PIDs.
 * This implementation creates separate nodes in Neo4j that point to the entities.
 */
@Component
@Slf4j
@ConditionalOnBean(TypedPIDMakerConfig.class)
public class TypedPIDMakerIDGenerator implements IdGenerator<String> {

    private final PersistentIdentifierService pidService;

    /**
     * Constructor.
     *
     * @param pidService The PersistentIdentifierService
     */
    @Autowired
    public TypedPIDMakerIDGenerator(PersistentIdentifierService pidService) {
        this.pidService = pidService;
        log.info("Initialized TypedPIDMakerIDGenerator with PersistentIdentifierService");
    }

    /**
     * Generates a PID for the given entity.
     * This method creates a new PersistentIdentifier entity that points to the given entity.
     *
     * @param primaryLabel The primary label of the entity
     * @param entity       The entity to generate a PID for
     * @return The generated PID
     */
    @Override
    @Nonnull
    public String generateId(String primaryLabel, Object entity) {
        if (!(entity instanceof AdministrativeMetadata idorisEntity)) {
            log.warn("Entity is not an AdministrativeMetadata, falling back to UUID generation");
            return UUID.randomUUID().toString();
        }

        try {
            log.debug("Creating PersistentIdentifier for entity: {}", idorisEntity);

            // Create a new PersistentIdentifier for the entity
            PersistentIdentifier pid = pidService.createPersistentIdentifier(idorisEntity);

            log.info("Created PersistentIdentifier with PID: {}", pid.getPid());
            return pid.getPid();
        } catch (Exception e) {
            log.error("Failed to create PersistentIdentifier: {}", e.getMessage());
            log.warn("Falling back to UUID generation");
            return UUID.randomUUID().toString();
        }
    }
}
