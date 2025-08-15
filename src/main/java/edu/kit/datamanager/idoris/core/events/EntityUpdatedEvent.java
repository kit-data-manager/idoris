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
 * Event that is published when an entity is updated in the system.
 * This event carries the updated entity and can be used by listeners
 * to perform additional operations like versioning, validation, etc.
 *
 * @param <T> the type of entity that was updated, must extend AdministrativeMetadata
 */
@Getter
@ToString(callSuper = true)
public class EntityUpdatedEvent<T extends AdministrativeMetadata> extends AbstractDomainEvent {
    private final T entity;
    private final Long previousVersion;

    /**
     * Creates a new EntityUpdatedEvent for the given entity.
     *
     * @param entity          the updated entity
     * @param previousVersion the version of the entity before the update
     */
    public EntityUpdatedEvent(T entity, Long previousVersion) {
        this.entity = entity;
        this.previousVersion = previousVersion;
    }

    /**
     * Gets the entity that was updated.
     *
     * @return the updated entity
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Gets the version of the entity before the update.
     *
     * @return the previous version number
     */
    public Long getPreviousVersion() {
        return previousVersion;
    }
}
