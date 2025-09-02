/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.users.web.hateoas;

import edu.kit.datamanager.idoris.core.domain.User;
import edu.kit.datamanager.idoris.users.web.v1.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler for converting User entities to EntityModel objects with HATEOAS links.
 */
@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    /**
     * Converts a User entity to an EntityModel with HATEOAS links.
     *
     * @param user the User entity to convert
     * @return an EntityModel containing the User and links
     */
    @Override
    public EntityModel<User> toModel(User user) {
        EntityModel<User> entityModel = EntityModel.of(user);

        // Add self link with affordances for update, partial update, and delete
        entityModel.add(
                linkTo(methodOn(UserController.class).getUserById(user.getInternalId()))
                        .withSelfRel()
                        .andAffordance(afford(methodOn(UserController.class).updateUser(user.getInternalId(), user)))
                        .andAffordance(afford(methodOn(UserController.class).partiallyUpdateUser(user.getInternalId(), user)))
                        .andAffordance(afford(methodOn(UserController.class).deleteUser(user.getInternalId())))
        );

        // Add link to contributions
        entityModel.add(linkTo(methodOn(UserController.class).getUserContributions(user.getInternalId())).withRel("contributions"));

        // Add link to all users
        entityModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return entityModel;
    }
}