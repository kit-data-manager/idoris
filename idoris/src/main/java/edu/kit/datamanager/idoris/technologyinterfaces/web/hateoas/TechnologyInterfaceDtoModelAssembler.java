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
package edu.kit.datamanager.idoris.technologyinterfaces.web.hateoas;

import edu.kit.datamanager.idoris.pids.api.IInternalPIDService;
import edu.kit.datamanager.idoris.technologyinterfaces.dto.TechnologyInterfaceDto;
import edu.kit.datamanager.idoris.technologyinterfaces.web.v1.TechnologyInterfaceController;
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
 * Assembler that adds HATEOAS links to TechnologyInterfaceDto responses.
 */
@Component
@Observed(contextualName = "technologyInterfaceDtoModelAssembler")
public class TechnologyInterfaceDtoModelAssembler implements RepresentationModelAssembler<TechnologyInterfaceDto, EntityModel<TechnologyInterfaceDto>> {

    @Autowired(required = false)
    private IInternalPIDService pidService;

    @Override
    public EntityModel<TechnologyInterfaceDto> toModel(TechnologyInterfaceDto dto) {
        EntityModel<TechnologyInterfaceDto> model = EntityModel.of(dto);

        // Collection link
        model.add(linkTo(methodOn(TechnologyInterfaceController.class).getAllTechnologyInterfaces()).withRel("collection"));

        // Add PID link if resolvable
        if (dto.getInternalId() != null && pidService != null) {
            pidService.getPIDLinkForInternalID(dto.getInternalId()).forEach(model::add);
        }

        // Self and relation links
        String base = "/v1/technologyInterfaces/{id}";
        if (dto.getInternalId() != null) {
            model.add(linkTo(methodOn(TechnologyInterfaceController.class).getTechnologyInterface(dto.getInternalId())).withSelfRel());
        } else {
            model.add(Link.of(base).withSelfRel().withTitle("self (templated)"));
        }
        model.add(Link.of(base + "/attributes").withRel("attributes:list"));
        model.add(Link.of(base + "/outputs").withRel("outputs:list"));
        model.add(Link.of(base + "/attributes:link").withRel("attributes:link"));
        model.add(Link.of(base + "/attributes:unlink").withRel("attributes:unlink"));
        model.add(Link.of(base + "/outputs:link").withRel("outputs:link"));
        model.add(Link.of(base + "/outputs:unlink").withRel("outputs:unlink"));

        return model;
    }

    @Override
    public CollectionModel<EntityModel<TechnologyInterfaceDto>> toCollectionModel(Iterable<? extends TechnologyInterfaceDto> entities) {
        CollectionModel<EntityModel<TechnologyInterfaceDto>> collection = RepresentationModelAssembler.super.toCollectionModel(entities);
        collection.add(linkTo(methodOn(TechnologyInterfaceController.class).getAllTechnologyInterfaces()).withSelfRel());
        return collection;
    }
}
