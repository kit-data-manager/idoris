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
import edu.kit.datamanager.idoris.datatypes.entities.DataType;
import edu.kit.datamanager.idoris.datatypes.entities.TypeProfile;
import edu.kit.datamanager.idoris.datatypes.web.v1.AtomicDataTypeController;
import edu.kit.datamanager.idoris.datatypes.web.v1.TypeProfileController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler for converting DataType entities to EntityModel objects with HATEOAS links.
 * This assembler delegates to specific assemblers based on the type of DataType.
 * <p>
 * This class combines the functionality of both an EntityModelAssembler and a
 * RepresentationModelProcessor, handling all HATEOAS concerns for DataType entities
 * in one place, according to Domain-Driven Design principles.
 */
@Component
public class DataTypeModelAssembler implements
        EntityModelAssembler<DataType>,
        RepresentationModelProcessor<EntityModel<DataType>> {

    @Autowired
    private AtomicDataTypeModelAssembler atomicDataTypeModelAssembler;

    @Autowired
    private TypeProfileModelAssembler typeProfileModelAssembler;

    /**
     * Converts a DataType entity to an EntityModel with HATEOAS links.
     * Delegates to specific assemblers based on the type of DataType.
     *
     * @param dataType the DataType entity to convert
     * @return an EntityModel containing the DataType and links
     */
    @Override
    public EntityModel<DataType> toModel(DataType dataType) {
        if (dataType instanceof AtomicDataType) {
            return EntityModel.of(dataType, atomicDataTypeModelAssembler.toModel((AtomicDataType) dataType).getLinks());
        } else if (dataType instanceof TypeProfile) {
            return EntityModel.of(dataType, typeProfileModelAssembler.toModel((TypeProfile) dataType).getLinks());
        } else {
            // Generic DataType handling
            EntityModel<DataType> entityModel = toModelWithoutLinks(dataType);

            // Add self link based on the type
            if (dataType instanceof AtomicDataType) {
                entityModel.add(linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(dataType.getId())).withSelfRel());
            } else if (dataType instanceof TypeProfile) {
                entityModel.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(dataType.getId())).withSelfRel());
            }

            return entityModel;
        }
    }

    /**
     * Processes an EntityModel of DataType to add additional HATEOAS links.
     * This method adds type-specific operation links based on the DataType instance.
     *
     * @param model the EntityModel to process
     * @return the processed EntityModel with additional links
     */
    @Override
    public EntityModel<DataType> process(EntityModel<DataType> model) {
        DataType dataType = model.getContent();
        if (dataType == null) {
            return model;
        }

        String pid = dataType.getId();

        // Add link to operations for this data type based on its type
        if (dataType instanceof AtomicDataType) {
            model.add(linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(pid)).withRel("operations"));
        } else if (dataType instanceof TypeProfile) {
            model.add(linkTo(methodOn(TypeProfileController.class).getOperationsForTypeProfile(pid)).withRel("operations"));
        }

        return model;
    }
}
