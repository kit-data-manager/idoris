/*
 * Copyright (c) 2025-2026 Karlsruhe Institute of Technology
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

import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileDto;
import edu.kit.datamanager.idoris.datatypes.web.v1.TypeProfileController;
import edu.kit.datamanager.idoris.metadata.pids.api.IInternalPIDService;
import io.micrometer.observation.annotation.Observed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler that adds HATEOAS links to TypeProfileDto responses.
 */
@Component
@Observed(contextualName = "typeProfileDtoModelAssembler")
public class TypeProfileModelAssembler implements RepresentationModelAssembler<TypeProfileDto, EntityModel<TypeProfileDto>> {

    @Autowired
    private IInternalPIDService pidService;

    @Override
    public EntityModel<TypeProfileDto> toModel(TypeProfileDto dto) {
        EntityModel<TypeProfileDto> model = EntityModel.of(dto);

        // Collection link
        model.add(linkTo(methodOn(TypeProfileController.class).getAllTypeProfiles()).withRel("collection"));

        // Add PID link if resolvable
        if (dto.getInternalId() != null && pidService != null) {
            pidService.getPIDLinkForInternalID(dto.getInternalId()).forEach(model::add);
        }

        // Self and relation links
        String base = "/v1/typeProfiles/{id}";
        if (dto.getInternalId() != null) {
            model.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(dto.getInternalId())).withSelfRel());
        } else {
            model.add(Link.of(base).withSelfRel().withTitle("self (templated)"));
        }
        model.add(Link.of(base + "/inheritsFrom:link").withRel("inheritsFrom:link"));
        model.add(Link.of(base + "/inheritsFrom:unlink").withRel("inheritsFrom:unlink"));
        model.add(Link.of(base + "/attributes:link").withRel("attributes:link"));
        model.add(Link.of(base + "/attributes:unlink").withRel("attributes:unlink"));

        // Add link to inherited attributes using the DTO's ID
        if (dto.getInternalId() != null) {
            model.add(linkTo(methodOn(TypeProfileController.class).getInheritedAttributes(dto.getInternalId())).withRel("inheritedAttributes"));

            // Add link to inheritance tree
            model.add(linkTo(methodOn(TypeProfileController.class).getInheritanceTree(dto.getInternalId())).withRel("inheritanceTree"));

            // Add link to operations
            model.add(linkTo(methodOn(TypeProfileController.class).getOperationsForTypeProfile(dto.getInternalId())).withRel("operations"));
        }

        return model;
    }

    @Override
    public CollectionModel<EntityModel<TypeProfileDto>> toCollectionModel(Iterable<? extends TypeProfileDto> entities) {
        CollectionModel<EntityModel<TypeProfileDto>> collection = RepresentationModelAssembler.super.toCollectionModel(entities);
        collection.add(linkTo(methodOn(TypeProfileController.class).getAllTypeProfiles()).withSelfRel());
        return collection;
    }
}
