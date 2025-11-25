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

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/**
 * Event that is published when a new version of an entity is created.
 * This event carries the current entity, the previous version, and change information.
 * It can be used by listeners to perform additional operations like version tracking, notification, or audit logging.
 *
 * @param <T> the type of entity that was versioned, must extend AdministrativeMetadata
 */
@Getter
@ToString(callSuper = true)
public class VersionCreatedEvent<T extends AdministrativeMetadata> extends AbstractDomainEvent {
    private final T currentEntity;
    private final T previousEntity;
    private final Long previousVersion;
    private final Long currentVersion;
    private final Map<String, Object> changes;

    /**
     * Creates a new VersionCreatedEvent for the given entity versions and changes.
     *
     * @param currentEntity  the current version of the entity
     * @param previousEntity the previous version of the entity
     * @param changes        a map of field names to their changed values
     */
    public VersionCreatedEvent(T currentEntity, T previousEntity, Map<String, Object> changes) {
        this.currentEntity = currentEntity;
        this.previousEntity = previousEntity;
        this.previousVersion = previousEntity.getVersion();
        this.currentVersion = currentEntity.getVersion();
        this.changes = changes;
    }

    /**
     * Gets the current version of the entity.
     *
     * @return the current entity
     */
    public T getCurrentEntity() {
        return currentEntity;
    }

    /**
     * Gets the previous version of the entity.
     *
     * @return the previous entity
     */
    public T getPreviousEntity() {
        return previousEntity;
    }

    /**
     * Gets the version number of the previous entity.
     *
     * @return the previous version number
     */
    public Long getPreviousVersion() {
        return previousVersion;
    }

    /**
     * Gets the version number of the current entity.
     *
     * @return the current version number
     */
    public Long getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Gets the changes between the previous and current versions.
     * The map contains field names as keys and their changed values as values.
     *
     * @return the changes map
     */
    public Map<String, Object> getChanges() {
        return changes;
    }
}
