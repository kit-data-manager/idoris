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

import edu.kit.datamanager.idoris.attributes.dto.AttributeDto;
import edu.kit.datamanager.idoris.attributes.web.v1.AttributeController;
import io.micrometer.observation.annotation.Observed;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler that adds HATEOAS links to AttributeDto responses.
 * Adds:
 * - self: /v1/attributes/{id}
 * - relations: dataType set/detach, override set/detach
 */
@Component
@Observed(contextualName = "attributeDtoModelAssembler")
public class AttributeModelAssembler implements RepresentationModelAssembler<AttributeDto, EntityModel<AttributeDto>> {

    @Override
    public EntityModel<AttributeDto> toModel(AttributeDto entity) {
        EntityModel<AttributeDto> model = EntityModel.of(entity);

        // Add collection link
        model.add(linkTo(methodOn(AttributeController.class).list()).withRel("collection"));

        // Relation operation links (templated with {id}) for discoverability
        // Clients can replace {id} with their known identifier (PID or internalId)
        String base = "/v1/attributes/{id}";
        model.add(Link.of(base).withSelfRel().withTitle("self (templated)"));
        model.add(Link.of(base + "/dataType").withRel("dataType:set"));
        model.add(Link.of(base + "/dataType").withRel("dataType:detach").withTitle("DELETE"));
        model.add(Link.of(base + "/override").withRel("override:set"));
        model.add(Link.of(base + "/override").withRel("override:detach").withTitle("DELETE"));

        return model;
    }

    @Override
    public CollectionModel<EntityModel<AttributeDto>> toCollectionModel(Iterable<? extends AttributeDto> entities) {
        CollectionModel<EntityModel<AttributeDto>> collection = RepresentationModelAssembler.super.toCollectionModel(entities);
        collection.add(linkTo(methodOn(AttributeController.class).list()).withSelfRel());
        return collection;
    }
}
