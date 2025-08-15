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

package edu.kit.datamanager.idoris.core.domain.web.hateoas;

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

/**
 * Base interface for entity model assemblers.
 * This interface defines the contract for assemblers that convert entities to EntityModel objects with HATEOAS links.
 *
 * @param <T> the entity type, must extend AdministrativeMetadata
 */
public interface EntityModelAssembler<T extends AdministrativeMetadata> extends RepresentationModelAssembler<T, EntityModel<T>> {

    /**
     * Converts an entity to an EntityModel with HATEOAS links.
     * This method is implemented by the RepresentationModelAssembler interface.
     *
     * @param entity the entity to convert
     * @return an EntityModel containing the entity and links
     */
    @Override
    EntityModel<T> toModel(T entity);

    /**
     * Creates an EntityModel for the given entity without adding links.
     * This method can be used as a base for the toModel method.
     *
     * @param entity the entity to convert
     * @return an EntityModel containing the entity without links
     */
    default EntityModel<T> toModelWithoutLinks(T entity) {
        return EntityModel.of(entity);
    }
}