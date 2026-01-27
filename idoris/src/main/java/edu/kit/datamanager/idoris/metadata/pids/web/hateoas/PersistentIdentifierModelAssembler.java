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
package edu.kit.datamanager.idoris.metadata.pids.web.hateoas;

import edu.kit.datamanager.idoris.metadata.pids.domain.PIDNode;
import edu.kit.datamanager.idoris.metadata.pids.web.v1.PidController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * A model assembler for PIDNode entities.
 * This class converts PIDNode entities to EntityModel<PIDNode>
 * with HATEOAS links.
 * <p>
 * This class combines the functionality of both a RepresentationModelAssembler and a
 * RepresentationModelProcessor, handling all HATEOAS concerns for PIDNode entities
 * in one place, according to Domain-Driven Design principles.
 */
@Component
public class PersistentIdentifierModelAssembler implements
        RepresentationModelAssembler<PIDNode, EntityModel<PIDNode>>,
        RepresentationModelProcessor<EntityModel<PIDNode>> {

    @Override
    public EntityModel<PIDNode> toModel(PIDNode pid) {
        EntityModel<PIDNode> pidModel = EntityModel.of(pid);

        // Add self link
        pidModel.add(linkTo(methodOn(PidController.class).getAllPersistentIdentifiers()).withRel("persistentIdentifiers"));

        return pidModel;
    }

    @Override
    public EntityModel<PIDNode> process(EntityModel<PIDNode> model) {
        PIDNode pid = model.getContent();
        if (pid == null) {
            return model;
        }

        String pidValue = pid.getPid().toString();

        // Add link to resolve the entity
        model.add(linkTo(methodOn(PidController.class).redirectToEntity(pidValue, null)).withRel("resolve"));

        // Add link to tombstone if it is a tombstone
        if (pid.isTombstone()) {
            model.add(linkTo(methodOn(PidController.class).handleTombstone(pidValue, null)).withRel("tombstone"));
        }

        return model;
    }
}
