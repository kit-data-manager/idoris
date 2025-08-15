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
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base class for all domain events.
 * Provides common functionality and properties for all events.
 */
@Getter
@ToString
public abstract class AbstractDomainEvent implements DomainEvent {
    private final Instant timestamp = Instant.now();
    private final String eventId = UUID.randomUUID().toString();

    /**
     * Gets the timestamp when this event occurred.
     *
     * @return the instant when the event was created
     */
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the unique identifier for this event.
     *
     * @return the event's unique identifier
     */
    @Override
    public String getEventId() {
        return eventId;
    }
}