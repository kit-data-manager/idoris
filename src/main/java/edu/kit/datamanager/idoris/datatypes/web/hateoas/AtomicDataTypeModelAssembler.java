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
import edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType;
import edu.kit.datamanager.idoris.datatypes.web.v1.AtomicDataTypeController;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler for converting AtomicDataType entities to EntityModel objects with HATEOAS links.
 */
@Component
public class AtomicDataTypeModelAssembler implements EntityModelAssembler<AtomicDataType> {

    /**
     * Converts an AtomicDataType entity to an EntityModel with HATEOAS links.
     *
     * @param atomicDataType the AtomicDataType entity to convert
     * @return an EntityModel containing the AtomicDataType and links
     */
    @Override
    public EntityModel<AtomicDataType> toModel(AtomicDataType atomicDataType) {
        EntityModel<AtomicDataType> entityModel = toModelWithoutLinks(atomicDataType);

        // Add self link
        entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(atomicDataType.getId())).withSelfRel());

        // Add link to all atomic data types
        entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getAllAtomicDataTypes()).withRel("atomicDataTypes"));

        // Add link to operations
        entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(atomicDataType.getId())).withRel("operations"));

        // Add link to inherits from if present
        if (atomicDataType.getInheritsFrom() != null) {
            entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(atomicDataType.getInheritsFrom().getId())).withRel("inheritsFrom"));
        }

        return entityModel;
    }
}