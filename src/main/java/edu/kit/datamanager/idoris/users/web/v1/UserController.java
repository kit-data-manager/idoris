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

package edu.kit.datamanager.idoris.users.web.v1;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.domain.User;
import edu.kit.datamanager.idoris.core.domain.valueObjects.ORCiD;
import edu.kit.datamanager.idoris.users.api.IUserService;
import edu.kit.datamanager.idoris.users.web.api.IUserApi;
import edu.kit.datamanager.idoris.users.web.hateoas.UserModelAssembler;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for User entities.
 * This controller provides endpoints for managing User entities.
 */
@RestController
@RequestMapping("/v1/users")
@Slf4j
@Observed(contextualName = "userController")
public class UserController implements IUserApi {

    private final IUserService IUserService;
    private final UserModelAssembler userModelAssembler;

    @Autowired
    public UserController(IUserService IUserService, UserModelAssembler userModelAssembler) {
        this.IUserService = IUserService;
        this.userModelAssembler = userModelAssembler;
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.createUser", description = "Time taken to create a user", histogram = true)
    @Counted(value = "userController.createUser.count", description = "Number of create user requests")
    public ResponseEntity<EntityModel<User>> createUser(User user) {
        log.debug("Creating user: {}", user);
        return IUserService.createUser(user)
                .map(createdUser -> {
                    log.info("Created user with ID: {}", createdUser.getInternalId());
                    return userModelAssembler.toModel(createdUser);
                })
                .map(entityModel -> ResponseEntity
                        .created(linkTo(methodOn(UserController.class).getUserById(entityModel.getContent().getInternalId())).toUri())
                        .body(entityModel))
                .orElseThrow(() -> {
                    log.error("Failed to create user");
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create user");
                });
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getAllUsers", description = "Time taken to get all users", histogram = true)
    @Counted(value = "userController.getAllUsers.count", description = "Number of get all users requests")
    public ResponseEntity<CollectionModel<EntityModel<User>>> getAllUsers() {
        log.debug("Getting all users");
        List<EntityModel<User>> users = IUserService.findAllUsers().stream()
                .map(userModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<User>> collectionModel = CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel()
        );

        log.info("Retrieved {} users", users.size());
        return ResponseEntity.ok(collectionModel);
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getUserById", description = "Time taken to get a user by ID", histogram = true)
    @Counted(value = "userController.getUserById.count", description = "Number of get user by ID requests")
    public ResponseEntity<EntityModel<User>> getUserById(@SpanAttribute("user.id") String id) {
        log.debug("Getting user by ID: {}", id);
        return IUserService.findUserById(id)
                .map(user -> {
                    log.info("Found user with ID: {}", id);
                    return userModelAssembler.toModel(user);
                })
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getUserContributions", description = "Time taken to get user contributions", histogram = true)
    @Counted(value = "userController.getUserContributions.count", description = "Number of get user contributions requests")
    public ResponseEntity<CollectionModel<AdministrativeMetadata>> getUserContributions(String id) {
        log.debug("Getting user contributions by ID: {}", id);
        return IUserService.getContributions(id)
                .stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), contributions -> {
                    CollectionModel<AdministrativeMetadata> collectionModel = CollectionModel.of(
                            contributions,
                            linkTo(methodOn(UserController.class).getUserContributions(id)).withSelfRel(),
                            linkTo(methodOn(UserController.class).getUserById(id)).withRel("user")
                    );
                    log.info("Found {} contributions for user with ID: {}", contributions.size(), id);
                    return ResponseEntity.ok(collectionModel);
                }));
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getUserByORCiD", description = "Time taken to get an user by ORCID", histogram = true)
    @Counted(value = "userController.getUserByORCiD.count", description = "Number of get user by ORCID requests")
    public ResponseEntity<EntityModel<User>> getUserByORCiD(@SpanAttribute("user.orcid") String orcidStr) {
        log.debug("Getting ORCID user by ORCID: {}", orcidStr);
        ORCiD orCiD = new ORCiD(orcidStr);

        return IUserService.findUserByORCiD(orCiD.get())
                .map(user -> {
                    log.info("Found ORCID user with ORCID: {}", orcidStr);
                    return userModelAssembler.toModel(user);
                })
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("ORCID user not found with ORCID: {}", orcidStr);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "ORCID user not found");
                });
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getUserByEmail", description = "Time taken to get a user by email", histogram = true)
    @Counted(value = "userController.getUserByEmail.count", description = "Number of get  user by email requests")
    public ResponseEntity<EntityModel<User>> getUserByEmail(@SpanAttribute("user.email") String email) {
        log.debug("Getting user by email: {}", email);
        return IUserService.findUserByEmail(email)
                .map(user -> {
                    log.info("Found user with email: {}", email);
                    return EntityModel.of(user,
                            linkTo(methodOn(UserController.class).getUserByEmail(email)).withSelfRel(),
                            linkTo(methodOn(UserController.class).getAllUsers()).withRel("textUsers"),
                            linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
                })
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.updateUser", description = "Time taken to update a user", histogram = true)
    @Counted(value = "userController.updateUser.count", description = "Number of update user requests")
    public ResponseEntity<EntityModel<User>> updateUser(@SpanAttribute("user.id") String id, @SpanAttribute User user) {
        log.debug("Updating user with ID: {}", id);
        try {
            User updatedUser = IUserService.updateUser(id, user);
            EntityModel<User> entityModel = userModelAssembler.toModel(updatedUser);
            log.info("Updated user with ID: {}", id);
            return ResponseEntity.ok(entityModel);
        } catch (IllegalArgumentException e) {
            log.error("Failed to update user with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<EntityModel<User>> partiallyUpdateUser(String id, User user) {
        log.debug("Partially updating user with ID: {}", id);
        try {
            User updatedUser = IUserService.partiallyUpdateUser(id, user);
            EntityModel<User> entityModel = userModelAssembler.toModel(updatedUser);
            log.info("Partially updated user with ID: {}", id);
            return ResponseEntity.ok(entityModel);
        } catch (IllegalArgumentException e) {
            log.error("Failed to partially update user with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.deleteUser", description = "Time taken to delete a user", histogram = true)
    @Counted(value = "userController.deleteUser.count", description = "Number of delete user requests")
    public ResponseEntity<Void> deleteUser(@SpanAttribute("user.id") String id) {
        log.debug("Deleting user with ID: {}", id);
        try {
            IUserService.deleteUser(id);
            log.info("Deleted user with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to delete user with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
