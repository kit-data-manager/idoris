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

import edu.kit.datamanager.idoris.users.entities.ORCiDUser;
import edu.kit.datamanager.idoris.users.entities.TextUser;
import edu.kit.datamanager.idoris.users.entities.User;
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
            description = "Retrieves a user by their PID or internal ID",
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

    /**
     * Gets all TextUser entities.
     *
     * @return a collection of all TextUser entities
     */
    @GetMapping("/text")
    @Operation(
            summary = "Get all text users",
            description = "Retrieves all text users in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Text users retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TextUser.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<TextUser>>> getAllTextUsers();

    /**
     * Gets a TextUser entity by its email.
     *
     * @param email the email of the TextUser to retrieve
     * @return the TextUser entity
     */
    @GetMapping("/text/email/{email}")
    @Operation(
            summary = "Get text user by email",
            description = "Retrieves a text user by their email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Text user retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TextUser.class))),
                    @ApiResponse(responseCode = "404", description = "Text user not found")
            }
    )
    ResponseEntity<EntityModel<TextUser>> getTextUserByEmail(
            @Parameter(description = "Email of the TextUser", required = true)
            @PathVariable String email);

    /**
     * Gets all ORCiDUser entities.
     *
     * @return a collection of all ORCiDUser entities
     */
    @GetMapping("/orcid")
    @Operation(
            summary = "Get all ORCID users",
            description = "Retrieves all ORCID users in the system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ORCID users retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = ORCiDUser.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<ORCiDUser>>> getAllORCiDUsers();

    /**
     * Gets an ORCiDUser entity by its ORCID.
     *
     * @param orcidStr the ORCID identifier string of the ORCiDUser to retrieve
     * @return the ORCiDUser entity
     */
    @GetMapping("/orcid/{orcidStr}")
    @Operation(
            summary = "Get ORCID user by ORCID",
            description = "Retrieves an ORCID user by their ORCID identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ORCID user retrieved successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = ORCiDUser.class))),
                    @ApiResponse(responseCode = "404", description = "ORCID user not found")
            }
    )
    ResponseEntity<EntityModel<ORCiDUser>> getORCiDUserByORCiD(
            @Parameter(description = "ORCID identifier of the ORCiDUser", required = true)
            @PathVariable String orcidStr);

    /**
     * Creates a new TextUser entity.
     *
     * @param user the TextUser entity to create
     * @return the created TextUser entity
     */
    @PostMapping("/text")
    @Operation(
            summary = "Create text user",
            description = "Creates a new text user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Text user created successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TextUser.class)))
            }
    )
    ResponseEntity<EntityModel<TextUser>> createTextUser(
            @Parameter(description = "TextUser to create", required = true)
            @RequestBody TextUser user);

    /**
     * Creates a new ORCiDUser entity.
     *
     * @param user the ORCiDUser entity to create
     * @return the created ORCiDUser entity
     */
    @PostMapping("/orcid")
    @Operation(
            summary = "Create ORCID user",
            description = "Creates a new ORCID user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "ORCID user created successfully",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = ORCiDUser.class)))
            }
    )
    ResponseEntity<EntityModel<ORCiDUser>> createORCiDUser(
            @Parameter(description = "ORCiDUser to create", required = true)
            @RequestBody ORCiDUser user);

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
            description = "Updates an existing user by their PID or internal ID",
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
