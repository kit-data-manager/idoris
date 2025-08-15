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
 * Event that is published when an ID is generated for an entity.
 * This event carries the entity and the generated ID, and can be used by listeners
 * to perform additional operations like ID record creation, indexing, etc.
 *
 * @param <T> the type of entity for which the ID was generated, must extend AdministrativeMetadata
 */
@Getter
@ToString(callSuper = true)
public class PIDGeneratedEvent<T extends AdministrativeMetadata> extends AbstractDomainEvent {
    private final T entity;
    private final String id;
    private final boolean isNewID;
    private final String entityInternalId;
    private final String entityType;

    /**
     * Creates a new PIDGeneratedEvent for the given entity and ID.
     *
     * @param entity  the entity for which the ID was generated
     * @param id      the generated ID
     * @param isNewID indicates whether this is a newly generated ID or an existing one
     */
    public PIDGeneratedEvent(T entity, String id, boolean isNewID) {
        this.entity = entity;
        this.id = id;
        this.isNewID = isNewID;
        this.entityInternalId = entity.getInternalId();
        this.entityType = entity.getClass().getSimpleName();
    }

    /**
     * Creates a new PIDGeneratedEvent for the given entity and ID.
     * Assumes that the ID is newly generated.
     *
     * @param entity the entity for which the ID was generated
     * @param id     the generated ID
     */
    public PIDGeneratedEvent(T entity, String id) {
        this(entity, id, true);
    }

    /**
     * Gets the entity for which the ID was generated.
     *
     * @return the entity
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Gets the generated ID.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Indicates whether this is a newly generated ID or an existing one.
     *
     * @return true if the ID was newly generated, false if it already existed
     */
    public boolean isNewID() {
        return isNewID;
    }

    /**
     * Gets the internal ID of the entity for which the ID was generated.
     *
     * @return the entity internal ID
     */
    public String getEntityInternalId() {
        return entityInternalId;
    }

    /**
     * Gets the type of the entity for which the ID was generated.
     *
     * @return the entity type
     */
    public String getEntityType() {
        return entityType;
    }
}
