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

/**
 * Event that is published when a schema is generated for an entity.
 * This event carries the entity for which the schema was generated, the schema format, and the schema content.
 * It can be used by listeners to perform additional operations like schema validation, storage, or publication.
 */
@Getter
@ToString(callSuper = true)
public class SchemaGeneratedEvent extends AbstractDomainEvent {
    private final AdministrativeMetadata entity;
    private final String schemaFormat;
    private final String schemaContent;
    private final boolean isValid;

    /**
     * Creates a new SchemaGeneratedEvent for the given entity and schema.
     *
     * @param entity        the entity for which the schema was generated
     * @param schemaFormat  the format of the schema (e.g., "json-schema", "xml-schema")
     * @param schemaContent the content of the schema
     * @param isValid       indicates whether the schema is valid
     */
    public SchemaGeneratedEvent(AdministrativeMetadata entity, String schemaFormat, String schemaContent, boolean isValid) {
        this.entity = entity;
        this.schemaFormat = schemaFormat;
        this.schemaContent = schemaContent;
        this.isValid = isValid;
    }

    /**
     * Gets the entity for which the schema was generated.
     *
     * @return the entity
     */
    public AdministrativeMetadata getEntity() {
        return entity;
    }

    /**
     * Gets the format of the schema.
     *
     * @return the schema format
     */
    public String getSchemaFormat() {
        return schemaFormat;
    }

    /**
     * Gets the content of the schema.
     *
     * @return the schema content
     */
    public String getSchemaContent() {
        return schemaContent;
    }

    /**
     * Indicates whether the schema is valid.
     *
     * @return true if the schema is valid, false otherwise
     */
    public boolean isValid() {
        return isValid;
    }
}
