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

import lombok.Getter;

/**
 * Event that is published when a non-AdministrativeMetadata is partially updated (patched) in the system.
 * This event carries the patched entity and can be used by listeners
 * to react to entity patch operations.
 */
@Getter
public class GenericEntityPatchedEvent extends AbstractDomainEvent {
    private final Object entity;
    private final String entityType;

    /**
     * Creates a new GenericEntityPatchedEvent for the given entity.
     *
     * @param entity     the patched entity
     * @param entityType the type identifier for the entity
     */
    public GenericEntityPatchedEvent(Object entity, String entityType) {
        this.entity = entity;
        this.entityType = entityType;
    }
}