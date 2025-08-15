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

import edu.kit.datamanager.idoris.core.domain.web.hateoas.EntityModelAssembler;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import edu.kit.datamanager.idoris.operations.web.v1.OperationController;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler for converting Operation entities to EntityModel objects with HATEOAS links.
 */
@Component
public class OperationModelAssembler implements EntityModelAssembler<Operation> {

    /**
     * Converts an Operation entity to an EntityModel with HATEOAS links.
     *
     * @param operation the Operation entity to convert
     * @return an EntityModel containing the Operation and links
     */
    @Override
    public EntityModel<Operation> toModel(Operation operation) {
        EntityModel<Operation> entityModel = toModelWithoutLinks(operation);

        // Add self link
        entityModel.add(linkTo(methodOn(OperationController.class).getOperation(operation.getId())).withSelfRel());

        // Add link to all operations
        entityModel.add(linkTo(methodOn(OperationController.class).getAllOperations()).withRel("operations"));

        // Add link to executable on data type
        if (operation.getExecutableOn() != null && operation.getExecutableOn().getDataType() != null) {
            entityModel.add(linkTo(methodOn(OperationController.class).getOperation(operation.getExecutableOn().getDataType().getId())).withRel("executableOn"));
        }

        return entityModel;
    }
}