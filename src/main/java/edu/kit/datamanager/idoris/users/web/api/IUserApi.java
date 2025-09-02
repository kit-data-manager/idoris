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

package edu.kit.datamanager.idoris.users.web.api;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API interface for User endpoints.
 * This interface defines the REST API for managing User entities.
 */
@Tag(name = "User Management", description = "API for managing users in the system")
public interface IUserApi {
    /**
     * Creates a new User entity.
     *
     * @param user the User entity to create
     * @return the created User entity
     */
    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user in the system",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    ResponseEntity<EntityModel<User>> createUser(
            @Parameter(description = "User to create", required = true, schema = @Schema(implementation = User.class))
            @RequestBody User user);

    /**
     * Gets all User entities.
     *
     * @return a collection of all User entities
     */
    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieves all users in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = User.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<User>>> getAllUsers();

    /**
     * Gets a User entity by its ID.
     *
     * @param id the PID or internal ID of the User to retrieve
     * @return the User entity
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a user by its internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    ResponseEntity<EntityModel<User>> getUserById(
            @Parameter(description = "PID or internal ID of the User", required = true)
            @PathVariable String id);

    @GetMapping("/{id}/contributions")
    @Operation(
            summary = "Get user contributions",
            description = "Retrieves a list of contributions associated with the user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contributions retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    ResponseEntity<CollectionModel<AdministrativeMetadata>> getUserContributions(
            @Parameter(description = "Internal ID of the User", required = true)
            @PathVariable String id);

    /**
     * Gets an ORCiDUser entity by its ORCID.
     *
     * @param orcidStr the ORCID identifier string of the ORCiDUser to retrieve
     * @return the ORCiDUser entity
     */
    @GetMapping
    @Operation(
            summary = "Get user by ORCID",
            description = "Retrieves a user by their ORCID identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    ResponseEntity<EntityModel<User>> getUserByORCiD(
            @RequestParam(required = true, name = "orcid") String orcidStr);

    /**
     * Gets an ORCiDUser entity by its ORCID.
     *
     * @param orcidStr the ORCID identifier string of the ORCiDUser to retrieve
     * @return the ORCiDUser entity
     */
    @GetMapping
    @Operation(
            summary = "Get user by e-mail address",
            description = "Retrieves a user by their e-mail address",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    ResponseEntity<EntityModel<User>> getUserByEmail(
            @RequestParam(required = true, name = "e-mail") String orcidStr);

    /**
     * Updates an existing User entity.
     *
     * @param id   the PID or internal ID of the User to update
     * @param user the updated User entity
     * @return the updated User entity
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update user",
            description = "Updates an existing user by its internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    ResponseEntity<EntityModel<User>> updateUser(
            @Parameter(description = "PID or internal ID of the User", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated User", required = true)
            @RequestBody User user);

    @PatchMapping("/{id}")
    @Operation(
            summary = "Partially update user",
            description = "Partially updates an existing user by its internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    ResponseEntity<EntityModel<User>> partiallyUpdateUser(
            @Parameter(description = "PID or internal ID of the User", required = true)
            @PathVariable String id,
            @Parameter(description = "User fields to update (only non-null fields will be updated)",
                    required = true, schema = @Schema(implementation = User.class))
            @RequestBody User user);

    /**
     * Deletes a User entity.
     *
     * @param id the PID or internal ID of the User to delete
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user",
            description = "Deletes a user by their PID or internal ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "PID or internal ID of the User", required = true)
            @PathVariable String id);
}
