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
package edu.kit.datamanager.idoris.operations.web.hateoas;

import edu.kit.datamanager.idoris.operations.dto.OperationResponseDto;
import edu.kit.datamanager.idoris.operations.web.v1.OperationController;
import edu.kit.datamanager.idoris.pids.api.IInternalPIDService;
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
 * Assembler that adds HATEOAS links to OperationDto responses.
 */
@Component
@Observed(contextualName = "operationDtoModelAssembler")
public class OperationDtoModelAssembler implements RepresentationModelAssembler<OperationResponseDto, EntityModel<OperationResponseDto>> {

    @Autowired(required = false)
    IInternalPIDService pidService;

    @Override
    public EntityModel<OperationResponseDto> toModel(OperationResponseDto dto) {
        EntityModel<OperationResponseDto> model = EntityModel.of(dto);

        // Collection link
        model.add(linkTo(methodOn(OperationController.class).getAllOperations()).withRel("collection"));

        // Add PID link if resolvable
        if (dto.getInternalId() != null && pidService != null) {
            pidService.getPIDLinkForInternalID(dto.getInternalId()).forEach(model::add);
        }

        String base = "/v1/operations/{id}";
        if (dto.getInternalId() != null) {
            model.add(linkTo(methodOn(OperationController.class).getOperation(dto.getInternalId())).withSelfRel());
            model.add(linkTo(methodOn(OperationController.class).validate(dto.getInternalId())).withRel("validate"));
        } else {
            model.add(Link.of(base).withSelfRel().withTitle("self (templated)"));
        }

        return model;
    }

    @Override
    public CollectionModel<EntityModel<OperationResponseDto>> toCollectionModel(Iterable<? extends OperationResponseDto> entities) {
        CollectionModel<EntityModel<OperationResponseDto>> collection = RepresentationModelAssembler.super.toCollectionModel(entities);
        collection.add(linkTo(methodOn(OperationController.class).getAllOperations()).withSelfRel());
        return collection;
    }
}
