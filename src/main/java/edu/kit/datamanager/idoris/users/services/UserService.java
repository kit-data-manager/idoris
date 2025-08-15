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

package edu.kit.datamanager.idoris.users.services;

import edu.kit.datamanager.idoris.users.entities.ORCiDUser;
import edu.kit.datamanager.idoris.users.entities.TextUser;
import edu.kit.datamanager.idoris.users.entities.User;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;

import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing users in the system.
 * Provides operations for both TextUsers and ORCiDUsers.
 */
public interface UserService {

    /**
     * Find all users in the system.
     *
     * @return List of all users
     */
    List<User> findAllUsers();

    /**
     * Find a user by their PID or internal ID.
     *
     * @param id The PID or internal ID of the user
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findUserById(@SpanAttribute("user.id") String id);

    /**
     * Find all TextUsers in the system.
     *
     * @return List of all TextUsers
     */
    List<TextUser> findAllTextUsers();

    /**
     * Find a TextUser by their email.
     *
     * @param email The email of the TextUser
     * @return Optional containing the TextUser if found, empty otherwise
     */
    Optional<TextUser> findTextUserByEmail(@SpanAttribute("user.email") String email);

    /**
     * Find all ORCiDUsers in the system.
     *
     * @return List of all ORCiDUsers
     */
    List<ORCiDUser> findAllORCiDUsers();

    /**
     * Find an ORCiDUser by their ORCID.
     *
     * @param orcid The ORCID of the user
     * @return Optional containing the ORCiDUser if found, empty otherwise
     */
    Optional<ORCiDUser> findORCiDUserByORCiD(@SpanAttribute("user.orcid") URL orcid);

    /**
     * Create a new TextUser.
     *
     * @param user The TextUser to create
     * @return The created TextUser
     */
    TextUser createTextUser(@SpanAttribute TextUser user);

    /**
     * Create a new ORCiDUser.
     *
     * @param user The ORCiDUser to create
     * @return The created ORCiDUser
     */
    ORCiDUser createORCiDUser(@SpanAttribute ORCiDUser user);

    /**
     * Update an existing user.
     *
     * @param id   The PID or internal ID of the user to update
     * @param user The updated user information
     * @return The updated user
     * @throws IllegalArgumentException if the user is not found
     */
    User updateUser(@SpanAttribute("user.id") String id, @SpanAttribute User user);

    /**
     * Delete a user by their PID or internal ID.
     *
     * @param id The PID or internal ID of the user to delete
     * @throws IllegalArgumentException if the user is not found
     */
    void deleteUser(@SpanAttribute("user.id") String id);
}
