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

import edu.kit.datamanager.idoris.datatypes.dto.AtomicDataTypeDto;
import edu.kit.datamanager.idoris.datatypes.dto.DataTypeDto;
import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileDto;
import edu.kit.datamanager.idoris.datatypes.web.v1.DataTypeController;
import edu.kit.datamanager.idoris.datatypes.web.v1.TypeProfileController;
import edu.kit.datamanager.idoris.pids.api.IInternalPIDService;
import io.micrometer.observation.annotation.Observed;
import org.jspecify.annotations.NonNull;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler that adds HATEOAS links to DataType DTO responses.
 * Handles both AtomicDataType and TypeProfile DTOs.
 */
@Component
@Observed(contextualName = "dataTypeDtoModelAssembler")
public class DataTypeModelAssembler implements RepresentationModelAssembler<DataTypeDto, EntityModel<DataTypeDto>> {

    private final IInternalPIDService internalPIDService;

    public DataTypeModelAssembler(IInternalPIDService internalPIDService) {
        this.internalPIDService = internalPIDService;
    }

    @Override
    public EntityModel<DataTypeDto> toModel(@NonNull DataTypeDto dto) {
        EntityModel<DataTypeDto> model = EntityModel.of(dto);

        // Add self link to the unified DataType controller
        if (dto.getInternalId() != null) {
            model.add(linkTo(methodOn(DataTypeController.class).get(dto.getInternalId())).withSelfRel());
        }

        // Add collection link
        model.add(linkTo(methodOn(DataTypeController.class).list()).withRel("collection"));

        // Add type-specific links
        if (dto instanceof AtomicDataTypeDto && dto.getInternalId() != null) {
            // For now, just add a templated link since AtomicDataTypeController may not exist yet
            model.add(Link.of("/v1/atomicDataTypes/{id}").withRel("atomicDataType"));
        } else if (dto instanceof TypeProfileDto && dto.getInternalId() != null) {
            // Link to specific TypeProfile endpoint
            model.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(dto.getInternalId())).withRel("typeProfile"));

            // TypeProfile-specific RESTful relationship links
            model.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(dto.getInternalId())).withSelfRel());
            model.add(linkTo(methodOn(TypeProfileController.class).getInheritedAttributes(dto.getInternalId())).withRel("inheritedAttributes"));
            model.add(linkTo(methodOn(TypeProfileController.class).getInheritanceTree(dto.getInternalId())).withRel("inheritanceTree"));
            // Relationship collections
            model.add(linkTo(methodOn(TypeProfileController.class).listInheritsFrom(dto.getInternalId())).withRel("inheritsFrom"));
            model.add(linkTo(methodOn(TypeProfileController.class).listAttributes(dto.getInternalId())).withRel("attributes"));
        }

        // Add common links
        if (dto.getInternalId() != null) {
            model.add(internalPIDService.getPIDLinkForInternalID(dto.getInternalId()));
            model.add(linkTo(methodOn(DataTypeController.class).getInheritanceHierarchy(dto.getInternalId())).withRel("inheritanceHierarchy"));
            model.add(linkTo(methodOn(DataTypeController.class).getOperations(dto.getInternalId())).withRel("operations"));
        }

        return model;
    }

    @Override
    public CollectionModel<EntityModel<DataTypeDto>> toCollectionModel(Iterable<? extends DataTypeDto> entities) {
        CollectionModel<EntityModel<DataTypeDto>> collection = RepresentationModelAssembler.super.toCollectionModel(entities);
        collection.add(linkTo(methodOn(DataTypeController.class).list()).withSelfRel());
        return collection;
    }
}
