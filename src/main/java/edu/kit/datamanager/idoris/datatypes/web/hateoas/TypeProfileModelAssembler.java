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

package edu.kit.datamanager.idoris.datatypes.web.hateoas;

import edu.kit.datamanager.idoris.core.domain.web.hateoas.EntityModelAssembler;
import edu.kit.datamanager.idoris.datatypes.entities.TypeProfile;
import edu.kit.datamanager.idoris.datatypes.web.v1.TypeProfileController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler for converting TypeProfile entities to EntityModel objects with HATEOAS links.
 * <p>
 * This class combines the functionality of both an EntityModelAssembler and a
 * RepresentationModelProcessor, handling all HATEOAS concerns for TypeProfile entities
 * in one place, according to Domain-Driven Design principles.
 */
@Component
public class TypeProfileModelAssembler implements
        EntityModelAssembler<TypeProfile>,
        RepresentationModelProcessor<EntityModel<TypeProfile>> {

    /**
     * Converts a TypeProfile entity to an EntityModel with basic HATEOAS links.
     *
     * @param typeProfile the TypeProfile entity to convert
     * @return an EntityModel containing the TypeProfile and links
     */
    @Override
    public EntityModel<TypeProfile> toModel(TypeProfile typeProfile) {
        EntityModel<TypeProfile> entityModel = toModelWithoutLinks(typeProfile);

        // Add self link
        entityModel.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(typeProfile.getId())).withSelfRel());

        // Add link to all type profiles
        entityModel.add(linkTo(methodOn(TypeProfileController.class).getAllTypeProfiles()).withRel("typeProfiles"));

        return entityModel;
    }

    /**
     * Processes an EntityModel of TypeProfile to add additional HATEOAS links.
     * This method is called after toModel() and enhances the model with more context-specific links.
     *
     * @param model the EntityModel to process
     * @return the processed EntityModel with additional links
     */
    @Override
    public EntityModel<TypeProfile> process(EntityModel<TypeProfile> model) {
        TypeProfile typeProfile = model.getContent();
        if (typeProfile == null) {
            return model;
        }

        String pid = typeProfile.getId();

        // Add link to validate
        model.add(linkTo(methodOn(TypeProfileController.class).validate(pid)).withRel("validate"));

        // Add link to inherited attributes
        model.add(linkTo(methodOn(TypeProfileController.class).getInheritedAttributes(pid)).withRel("inheritedAttributes"));

        // Add link to inheritance tree
        model.add(linkTo(methodOn(TypeProfileController.class).getInheritanceTree(pid)).withRel("inheritanceTree"));

        // Add link to operations
        WebMvcLinkBuilder operationsLinkBuilder = linkTo(methodOn(TypeProfileController.class).getOperationsForTypeProfile(pid));
        model.add(operationsLinkBuilder.withRel("operations"));

        return model;
    }
}