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

package edu.kit.datamanager.idoris.attributes.web.hateoas;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.attributes.web.v1.AttributeController;
import edu.kit.datamanager.idoris.core.domain.web.hateoas.EntityModelAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler for converting Attribute entities to EntityModel objects with HATEOAS links.
 */
@Component
public class AttributeModelAssembler implements EntityModelAssembler<Attribute> {

    /**
     * Converts an Attribute entity to an EntityModel with HATEOAS links.
     *
     * @param attribute the Attribute entity to convert
     * @return an EntityModel containing the Attribute and links
     */
    @Override
    public EntityModel<Attribute> toModel(Attribute attribute) {
        EntityModel<Attribute> entityModel = toModelWithoutLinks(attribute);

        // Add self link
        entityModel.add(linkTo(methodOn(AttributeController.class).getAttribute(attribute.getId())).withSelfRel());

        // Add link to data type
        if (attribute.getDataType() != null) {
            entityModel.add(linkTo(methodOn(AttributeController.class).getDataType(attribute.getId())).withRel("dataType"));
        }

        // Add link to override attribute if it exists
        if (attribute.getOverride() != null) {
            entityModel.add(linkTo(methodOn(AttributeController.class).getAttribute(attribute.getOverride().getId())).withRel("override"));
        }

        // Add link to all attributes
        entityModel.add(linkTo(methodOn(AttributeController.class).getAllAttributes()).withRel("attributes"));

        return entityModel;
    }
}