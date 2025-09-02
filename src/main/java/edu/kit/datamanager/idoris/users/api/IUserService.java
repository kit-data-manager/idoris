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

package edu.kit.datamanager.idoris.users.api;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.domain.User;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing users in the system.
 * Provides operations for both TextUsers and ORCiDUsers.
 */
public interface IUserService {
    Optional<User> createUser(User user);

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
    Optional<User> findUserById(String id);

    /**
     * Find a user by their ORCiD.
     *
     * @param orcid The ORCiD of the user
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findUserByORCiD(URI orcid);

    /**
     * Find a user by their email address.
     *
     * @param email The email address of the user
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Get a list of contributions (URIs) associated with a user by their ID.
     *
     * @param id The PID or internal ID of the user
     * @return List of AdministrativeMetadata objects representing the contributions
     */
    List<AdministrativeMetadata> getContributions(String id);

    /**
     * Update an existing user.
     *
     * @param id   The PID or internal ID of the user to update
     * @param user The updated user information
     * @return The updated user
     * @throws IllegalArgumentException if the user is not found
     */
    User updateUser(String id, User user) throws IllegalArgumentException;

    /**
     * Partially update an existing user.
     *
     * @param id   The PID or internal ID of the user to update
     * @param user The user information to update (only non-null fields will be updated)
     * @return The updated user
     * @throws IllegalArgumentException if the user is not found
     */
    User partiallyUpdateUser(String id, User user) throws IllegalArgumentException;

    /**
     * Delete a user by their PID or internal ID.
     *
     * @param id The PID or internal ID of the user to delete
     * @throws IllegalArgumentException if the user is not found
     */
    void deleteUser(String id) throws IllegalArgumentException;
}
