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

import edu.kit.datamanager.idoris.core.domain.web.hateoas.EntityModelAssembler;
import edu.kit.datamanager.idoris.technologyinterfaces.entities.TechnologyInterface;
import edu.kit.datamanager.idoris.technologyinterfaces.web.v1.TechnologyInterfaceController;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler for converting TechnologyInterface entities to EntityModel objects with HATEOAS links.
 */
@Component
public class TechnologyInterfaceModelAssembler implements EntityModelAssembler<TechnologyInterface> {

    /**
     * Converts a TechnologyInterface entity to an EntityModel with HATEOAS links.
     *
     * @param technologyInterface the TechnologyInterface entity to convert
     * @return an EntityModel containing the TechnologyInterface and links
     */
    @Override
    public EntityModel<TechnologyInterface> toModel(TechnologyInterface technologyInterface) {
        EntityModel<TechnologyInterface> entityModel = toModelWithoutLinks(technologyInterface);

        // Add self link
        entityModel.add(linkTo(methodOn(TechnologyInterfaceController.class).getTechnologyInterface(technologyInterface.getId())).withSelfRel());

        // Add link to attributes
        entityModel.add(linkTo(methodOn(TechnologyInterfaceController.class).getAttributes(technologyInterface.getId())).withRel("attributes"));

        // Add link to outputs
        entityModel.add(linkTo(methodOn(TechnologyInterfaceController.class).getOutputs(technologyInterface.getId())).withRel("outputs"));

        // Add link to all technology interfaces
        entityModel.add(linkTo(methodOn(TechnologyInterfaceController.class).getAllTechnologyInterfaces()).withRel("technologyInterfaces"));

        return entityModel;
    }
}