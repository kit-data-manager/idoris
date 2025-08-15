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
package edu.kit.datamanager.idoris.pids.web.hateoas;

import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import edu.kit.datamanager.idoris.pids.web.v1.PidController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * A model assembler for PersistentIdentifier entities.
 * This class converts PersistentIdentifier entities to EntityModel<PersistentIdentifier>
 * with HATEOAS links.
 * <p>
 * This class combines the functionality of both a RepresentationModelAssembler and a
 * RepresentationModelProcessor, handling all HATEOAS concerns for PersistentIdentifier entities
 * in one place, according to Domain-Driven Design principles.
 */
@Component
public class PersistentIdentifierModelAssembler implements
        RepresentationModelAssembler<PersistentIdentifier, EntityModel<PersistentIdentifier>>,
        RepresentationModelProcessor<EntityModel<PersistentIdentifier>> {

    @Override
    public EntityModel<PersistentIdentifier> toModel(PersistentIdentifier pid) {
        EntityModel<PersistentIdentifier> pidModel = EntityModel.of(pid);

        // Add self link
        pidModel.add(linkTo(methodOn(PidController.class).getAllPersistentIdentifiers()).withRel("persistentIdentifiers"));

        return pidModel;
    }

    @Override
    public EntityModel<PersistentIdentifier> process(EntityModel<PersistentIdentifier> model) {
        PersistentIdentifier pid = model.getContent();
        if (pid == null) {
            return model;
        }

        String pidValue = pid.getPid();

        // Add link to resolve the entity
        model.add(linkTo(methodOn(PidController.class).redirectToEntity(pidValue)).withRel("resolve"));

        // Add link to tombstone if it is a tombstone
        if (pid.isTombstone()) {
            model.add(linkTo(methodOn(PidController.class).handleTombstone(pidValue)).withRel("tombstone"));
        }

        return model;
    }
}
