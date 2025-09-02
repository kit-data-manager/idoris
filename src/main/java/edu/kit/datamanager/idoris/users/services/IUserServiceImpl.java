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

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.domain.User;
import edu.kit.datamanager.idoris.users.api.IUserService;
import edu.kit.datamanager.idoris.users.dao.IUserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the IUserService interface.
 * Provides operations for managing both TextUsers and ORCiDUsers.
 */
@Slf4j
@Service
@Transactional
public class IUserServiceImpl implements IUserService {

    private final IUserDao userDao;

    @Autowired
    public IUserServiceImpl(IUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> createUser(User user) {
        User toSave = new User();

        if (user.getName() != null && !user.getName().isBlank()) {
            String filteredName = user.getName().trim();
            if (filteredName.length() > 255) { // Limit name length to 255 characters
                filteredName = filteredName.substring(0, 255);
            }
            // Remove any unwanted characters from the name (e.g., control characters, HTML tags, etc.)
            filteredName = filteredName.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", ""); // Remove control characters except for newlines and tabs
            filteredName = filteredName.replaceAll("<[^>]*>", ""); // Remove HTML tags
            filteredName = filteredName.replaceAll("[\"'\\\\]", ""); // Remove quotes and backslashes
            filteredName = filteredName.replaceAll("\\s+", " "); // Replace multiple spaces with a single space
            filteredName = filteredName.replaceAll("[<>]", ""); // Remove angle brackets
            filteredName = filteredName.replaceAll("[&]", "and"); // Replace ampersands with 'and'
            filteredName = filteredName.trim(); // Trim again after all replacements
            toSave.setName(filteredName);
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            String filteredEmail = user.getEmail().trim();
            if (filteredEmail.length() > 320) { // Limit email length to 320 characters
                throw new IllegalArgumentException("Email address cannot be longer than 320 characters.");
            }
            // Basic email format validation
            String emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,}$";
            if (!filteredEmail.toUpperCase().matches(emailPattern)) {
                throw new IllegalArgumentException("Invalid email format: " + filteredEmail);
            }
            // Check for existing user with the same email
            if (userDao.findByEmail(filteredEmail).isPresent()) {
                throw new IllegalArgumentException("User with email " + filteredEmail + " already exists");
            }
            toSave.setEmail(filteredEmail);
        }
        if (user.getOrcid() != null) {
            // Check if the ORCiD is in a valid format
            String orcidPattern = "^https?://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{4}$";
            if (!user.getOrcid().toString().matches(orcidPattern)) {
                throw new IllegalArgumentException("Invalid ORCiD URL format: " + user.getOrcid());
            }

            // Check for existing user with the same ORCiD
            if (userDao.findByOrcid(user.getOrcid()).isPresent()) {
                throw new IllegalArgumentException("User with ORCiD " + user.getOrcid() + " already exists");
            }

            toSave.setOrcid(user.getOrcid());
        }
        User savedUser = userDao.save(toSave);
        return Optional.of(savedUser);
    }

    @Override
    public List<User> findAllUsers() {
        return userDao.findAll();
    }

    @Override
    public Optional<User> findUserById(String id) {
        return userDao.findById(id);
    }

    @Override
    public Optional<User> findUserByORCiD(URI orcid) {
        return userDao.findByOrcid(orcid);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public List<AdministrativeMetadata> getContributions(String id) {
        return userDao.getContributions(id);
    }

    @Override
    public User updateUser(String id, User user) throws IllegalArgumentException {
        if (!userDao.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }

        user.setInternalId(id);

        return userDao.save(user);
    }

    @Override
    public User partiallyUpdateUser(String id, User user) throws IllegalArgumentException {
        User existingUser = userDao.findById(id).orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found"));
        user.setInternalId(id);
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getOrcid() != null) {
            existingUser.setOrcid(user.getOrcid());
        }
        userDao.save(existingUser);
        return existingUser;
    }

    @Override
    public void deleteUser(String id) throws IllegalArgumentException {
        if (!userDao.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        userDao.deleteById(id);
    }
}