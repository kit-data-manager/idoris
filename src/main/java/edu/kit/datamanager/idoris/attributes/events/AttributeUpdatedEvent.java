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
package edu.kit.datamanager.idoris.attributes.events;

import edu.kit.datamanager.idoris.attributes.dto.AttributeDto;
import edu.kit.datamanager.idoris.core.events.AbstractDomainEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AttributeUpdatedEvent extends AbstractDomainEvent {
    @Schema(description = "ID (PID or internalId) of the Attribute")
    private final String id;
    private final Long previousVersion;
    private final AttributeDto payload;

    public AttributeUpdatedEvent(String id, Long previousVersion, AttributeDto payload) {
        this.id = id;
        this.previousVersion = previousVersion;
        this.payload = payload;
    }
}