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

package edu.kit.datamanager.idoris.core.events;

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import lombok.Getter;
import lombok.ToString;

/**
 * Event that is published when an entity is imported from an external system.
 * This event carries the imported entity, the source system, and import metadata.
 * It can be used by listeners to perform additional operations like validation, enrichment, or notification.
 *
 * @param <T> the type of entity that was imported, must extend AdministrativeMetadata
 */
@Getter
@ToString(callSuper = true)
public class EntityImportedEvent<T extends AdministrativeMetadata> extends AbstractDomainEvent {
    private final T entity;
    private final String sourceSystem;
    private final String sourceIdentifier;
    private final ImportResult importResult;

    /**
     * Creates a new EntityImportedEvent for the given entity and source information.
     *
     * @param entity           the imported entity
     * @param sourceSystem     the system from which the entity was imported
     * @param sourceIdentifier the identifier of the entity in the source system
     * @param importResult     the result of the import operation
     */
    public EntityImportedEvent(T entity, String sourceSystem, String sourceIdentifier, ImportResult importResult) {
        this.entity = entity;
        this.sourceSystem = sourceSystem;
        this.sourceIdentifier = sourceIdentifier;
        this.importResult = importResult;
    }

    /**
     * Gets the entity that was imported.
     *
     * @return the imported entity
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Gets the system from which the entity was imported.
     *
     * @return the source system
     */
    public String getSourceSystem() {
        return sourceSystem;
    }

    /**
     * Gets the identifier of the entity in the source system.
     *
     * @return the source identifier
     */
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    /**
     * Gets the result of the import operation.
     *
     * @return the import result
     */
    public ImportResult getImportResult() {
        return importResult;
    }

    /**
     * Enum representing the result of an import operation.
     */
    public enum ImportResult {
        /**
         * The entity was successfully imported.
         */
        SUCCESS,

        /**
         * The entity was partially imported with some data loss or modifications.
         */
        PARTIAL,

        /**
         * The entity was imported but requires manual review.
         */
        NEEDS_REVIEW,

        /**
         * The entity was not imported due to validation errors.
         */
        VALIDATION_ERROR,

        /**
         * The entity was not imported due to a system error.
         */
        SYSTEM_ERROR
    }
}
