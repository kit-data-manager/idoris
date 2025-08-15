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

import edu.kit.datamanager.idoris.users.entities.ORCiDUser;
import edu.kit.datamanager.idoris.users.entities.TextUser;
import edu.kit.datamanager.idoris.users.entities.User;
import edu.kit.datamanager.idoris.users.services.UserService;
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

import java.net.URI;
import java.net.URL;
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

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;

    @Autowired
    public UserController(UserService userService, UserModelAssembler userModelAssembler) {
        this.userService = userService;
        this.userModelAssembler = userModelAssembler;
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getAllUsers", description = "Time taken to get all users", histogram = true)
    @Counted(value = "userController.getAllUsers.count", description = "Number of get all users requests")
    public ResponseEntity<CollectionModel<EntityModel<User>>> getAllUsers() {
        log.debug("Getting all users");
        List<EntityModel<User>> users = userService.findAllUsers().stream()
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
        return userService.findUserById(id)
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
    @Timed(value = "userController.getAllTextUsers", description = "Time taken to get all text users", histogram = true)
    @Counted(value = "userController.getAllTextUsers.count", description = "Number of get all text users requests")
    public ResponseEntity<CollectionModel<EntityModel<TextUser>>> getAllTextUsers() {
        log.debug("Getting all text users");
        List<EntityModel<TextUser>> users = userService.findAllTextUsers().stream()
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UserController.class).getTextUserByEmail(user.getEmail())).withSelfRel(),
                        linkTo(methodOn(UserController.class).getAllTextUsers()).withRel("textUsers")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TextUser>> collectionModel = CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAllTextUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")
        );

        log.info("Retrieved {} text users", users.size());
        return ResponseEntity.ok(collectionModel);
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getTextUserByEmail", description = "Time taken to get a text user by email", histogram = true)
    @Counted(value = "userController.getTextUserByEmail.count", description = "Number of get text user by email requests")
    public ResponseEntity<EntityModel<TextUser>> getTextUserByEmail(@SpanAttribute("user.email") String email) {
        log.debug("Getting text user by email: {}", email);
        return userService.findTextUserByEmail(email)
                .map(user -> {
                    log.info("Found text user with email: {}", email);
                    return EntityModel.of(user,
                            linkTo(methodOn(UserController.class).getTextUserByEmail(email)).withSelfRel(),
                            linkTo(methodOn(UserController.class).getAllTextUsers()).withRel("textUsers"),
                            linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
                })
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    log.warn("Text user not found with email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Text user not found");
                });
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getAllORCiDUsers", description = "Time taken to get all ORCID users", histogram = true)
    @Counted(value = "userController.getAllORCiDUsers.count", description = "Number of get all ORCID users requests")
    public ResponseEntity<CollectionModel<EntityModel<ORCiDUser>>> getAllORCiDUsers() {
        log.debug("Getting all ORCID users");
        List<EntityModel<ORCiDUser>> users = userService.findAllORCiDUsers().stream()
                .map(user -> {
                    // Extract ORCID identifier from the URL
                    String orcidStr = user.getOrcid().toString().replace("https://orcid.org/", "");
                    return EntityModel.of(user,
                            linkTo(methodOn(UserController.class).getORCiDUserByORCiD(orcidStr)).withSelfRel(),
                            linkTo(methodOn(UserController.class).getAllORCiDUsers()).withRel("orcidUsers"));
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ORCiDUser>> collectionModel = CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAllORCiDUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")
        );

        log.info("Retrieved {} ORCID users", users.size());
        return ResponseEntity.ok(collectionModel);
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.getORCiDUserByORCiD", description = "Time taken to get an ORCID user by ORCID", histogram = true)
    @Counted(value = "userController.getORCiDUserByORCiD.count", description = "Number of get ORCID user by ORCID requests")
    public ResponseEntity<EntityModel<ORCiDUser>> getORCiDUserByORCiD(@SpanAttribute("user.orcid") String orcidStr) {
        log.debug("Getting ORCID user by ORCID: {}", orcidStr);
        try {
            // Convert ORCID string to URL
            URL orcid = URI.create("https://orcid.org/" + orcidStr).toURL();
            return userService.findORCiDUserByORCiD(orcid)
                    .map(user -> {
                        log.info("Found ORCID user with ORCID: {}", orcidStr);
                        return EntityModel.of(user,
                                linkTo(methodOn(UserController.class).getORCiDUserByORCiD(orcidStr)).withSelfRel(),
                                linkTo(methodOn(UserController.class).getAllORCiDUsers()).withRel("orcidUsers"),
                                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
                    })
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> {
                        log.warn("ORCID user not found with ORCID: {}", orcidStr);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "ORCID user not found");
                    });
        } catch (java.net.MalformedURLException e) {
            log.error("Invalid ORCID format: {}", orcidStr, e);
            // Only catch MalformedURLException to return BAD_REQUEST
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ORCID format: " + orcidStr, e);
        }
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.createTextUser", description = "Time taken to create a text user", histogram = true)
    @Counted(value = "userController.createTextUser.count", description = "Number of create text user requests")
    public ResponseEntity<EntityModel<TextUser>> createTextUser(@SpanAttribute TextUser user) {
        log.debug("Creating text user: {}", user.getEmail());
        TextUser createdUser = userService.createTextUser(user);
        EntityModel<TextUser> entityModel = EntityModel.of(createdUser,
                linkTo(methodOn(UserController.class).getTextUserByEmail(createdUser.getEmail())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllTextUsers()).withRel("textUsers"),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        log.info("Created text user with email: {}", createdUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.createORCiDUser", description = "Time taken to create an ORCID user", histogram = true)
    @Counted(value = "userController.createORCiDUser.count", description = "Number of create ORCID user requests")
    public ResponseEntity<EntityModel<ORCiDUser>> createORCiDUser(@SpanAttribute ORCiDUser user) {
        log.debug("Creating ORCID user: {}", user.getOrcid());
        ORCiDUser createdUser = userService.createORCiDUser(user);
        // Extract ORCID identifier from the URL
        String orcidStr = createdUser.getOrcid().toString().replace("https://orcid.org/", "");
        EntityModel<ORCiDUser> entityModel = EntityModel.of(createdUser,
                linkTo(methodOn(UserController.class).getORCiDUserByORCiD(orcidStr)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllORCiDUsers()).withRel("orcidUsers"),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        log.info("Created ORCID user with ORCID: {}", createdUser.getOrcid());
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "userController.updateUser", description = "Time taken to update a user", histogram = true)
    @Counted(value = "userController.updateUser.count", description = "Number of update user requests")
    public ResponseEntity<EntityModel<User>> updateUser(@SpanAttribute("user.id") String id, @SpanAttribute User user) {
        log.debug("Updating user with ID: {}", id);
        try {
            User updatedUser = userService.updateUser(id, user);
            EntityModel<User> entityModel = userModelAssembler.toModel(updatedUser);
            log.info("Updated user with ID: {}", id);
            return ResponseEntity.ok(entityModel);
        } catch (IllegalArgumentException e) {
            log.error("Failed to update user with ID: {}", id, e);
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
            userService.deleteUser(id);
            log.info("Deleted user with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to delete user with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
