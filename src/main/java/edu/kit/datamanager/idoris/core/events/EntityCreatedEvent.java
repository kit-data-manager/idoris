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
 * Event that is published when a new entity is created in the system.
 * This event carries the newly created entity and can be used by listeners
 * to perform additional operations like PID generation, validation, etc.
 *
 * @param <T> the type of entity that was created, must extend AdministrativeMetadata
 */
@Getter
@ToString(callSuper = true)
public class EntityCreatedEvent<T extends AdministrativeMetadata> extends AbstractDomainEvent {
    private final T entity;

    /**
     * Creates a new EntityCreatedEvent for the given entity.
     *
     * @param entity the newly created entity
     */
    public EntityCreatedEvent(T entity) {
        this.entity = entity;
    }

    /**
     * Gets the entity that was created.
     *
     * @return the newly created entity
     */
    public T getEntity() {
        return entity;
    }
}
