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
 * Event that is published when an entity is deleted from the system.
 * This event carries the deleted entity and can be used by listeners
 * to perform additional operations like cleanup, notification, etc.
 *
 * @param <T> the type of entity that was deleted, must extend AdministrativeMetadata
 */
@Getter
@ToString(callSuper = true)
public class EntityDeletedEvent<T extends AdministrativeMetadata> extends AbstractDomainEvent {
    private final T entity;
    private final String entityType;
    private final String entityInternalId;

    /**
     * Creates a new EntityDeletedEvent for the given entity.
     *
     * @param entity the deleted entity
     */
    public EntityDeletedEvent(T entity) {
        this.entity = entity;
        this.entityType = entity.getClass().getSimpleName();
        this.entityInternalId = entity.getInternalId();
    }

    /**
     * Gets the entity that was deleted.
     *
     * @return the deleted entity
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Gets the type of the entity that was deleted.
     *
     * @return the entity type
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Gets the internal ID of the entity that was deleted.
     *
     * @return the entity internal ID
     */
    public String getEntityInternalId() {
        return entityInternalId;
    }
}
