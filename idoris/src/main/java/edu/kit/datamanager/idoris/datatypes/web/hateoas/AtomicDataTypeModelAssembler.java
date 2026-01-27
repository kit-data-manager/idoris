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

import edu.kit.datamanager.idoris.core.web.hateoas.EntityModelAssembler;
import edu.kit.datamanager.idoris.datatypes.dto.AtomicDataTypeDto;
import edu.kit.datamanager.idoris.datatypes.web.v1.AtomicDataTypeController;
import edu.kit.datamanager.idoris.metadata.pids.api.IInternalPIDService;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler for converting AtomicDataTypeDto objects to EntityModel objects with HATEOAS links.
 */
@Component
public class AtomicDataTypeModelAssembler implements EntityModelAssembler<AtomicDataTypeDto> {

    private final IInternalPIDService internalPIDService;

    AtomicDataTypeModelAssembler(IInternalPIDService internalPIDService) {
        this.internalPIDService = internalPIDService;
    }

    /**
     * Converts an AtomicDataTypeDto to an EntityModel with HATEOAS links.
     *
     * @param atomicDataTypeDto the AtomicDataTypeDto to convert
     * @return an EntityModel containing the AtomicDataTypeDto and links
     */
    @Override
    public EntityModel<AtomicDataTypeDto> toModel(AtomicDataTypeDto atomicDataTypeDto) {
        EntityModel<AtomicDataTypeDto> entityModel = EntityModel.of(atomicDataTypeDto);

        // Add self link
        if (atomicDataTypeDto.getInternalId() != null) {
            entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(atomicDataTypeDto.getInternalId())).withSelfRel());
            entityModel.add(internalPIDService.getPIDLinkForInternalID(atomicDataTypeDto.getInternalId()));
            entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(atomicDataTypeDto.getInternalId())).withRel("operations"));
        }

        // Add link to all atomic data types
        entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getAllAtomicDataTypes()).withRel("atomicDataTypes"));

        // Add link to inherits from if present
        if (atomicDataTypeDto.getInheritsFromId() != null) {
            entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(atomicDataTypeDto.getInheritsFromId())).withRel("inheritsFrom"));
        }

        return entityModel;
    }
}