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

import edu.kit.datamanager.idoris.users.entities.ORCiDUser;
import edu.kit.datamanager.idoris.users.entities.TextUser;
import edu.kit.datamanager.idoris.users.entities.User;
import edu.kit.datamanager.idoris.users.web.v1.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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

        // Add self link
        entityModel.add(linkTo(methodOn(UserController.class).getUserById(user.getInternalId())).withSelfRel());

        // Add link to all users
        entityModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        // Add type-specific links
        if (user instanceof TextUser) {
            entityModel.add(linkTo(methodOn(UserController.class).getAllTextUsers()).withRel("textUsers"));
            entityModel.add(linkTo(methodOn(UserController.class).getTextUserByEmail(((TextUser) user).getEmail())).withRel("byEmail"));
        } else if (user instanceof ORCiDUser) {
            entityModel.add(linkTo(methodOn(UserController.class).getAllORCiDUsers()).withRel("orcidUsers"));
            entityModel.add(linkTo(methodOn(UserController.class).getORCiDUserByORCiD(String.valueOf(((ORCiDUser) user).getOrcid()))).withRel("byOrcid"));
        }

        return entityModel;
    }
}