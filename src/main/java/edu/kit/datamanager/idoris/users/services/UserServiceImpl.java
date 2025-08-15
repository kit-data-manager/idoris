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

import edu.kit.datamanager.idoris.users.dao.IUserDao;
import edu.kit.datamanager.idoris.users.entities.ORCiDUser;
import edu.kit.datamanager.idoris.users.entities.TextUser;
import edu.kit.datamanager.idoris.users.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of the UserService interface.
 * Provides operations for managing both TextUsers and ORCiDUsers.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final IUserDao userDao;

    @Autowired
    public UserServiceImpl(IUserDao userDao) {
        this.userDao = userDao;
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
    public List<TextUser> findAllTextUsers() {
        return StreamSupport.stream(userDao.findAllTextUsers().spliterator(), false)
                .map(user -> (TextUser) user)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TextUser> findTextUserByEmail(String email) {
        User user = userDao.findTextUserByEmail(email);
        return Optional.ofNullable(user).map(u -> (TextUser) u);
    }

    @Override
    public List<ORCiDUser> findAllORCiDUsers() {
        return StreamSupport.stream(userDao.findAllORCiDUsers().spliterator(), false)
                .map(user -> (ORCiDUser) user)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ORCiDUser> findORCiDUserByORCiD(URL orcid) {
        User user = userDao.findORCiDUserByORCiD(orcid.toString());
        return Optional.ofNullable(user).map(u -> (ORCiDUser) u);
    }

    @Override
    public TextUser createTextUser(TextUser user) {
        // Set the type field for proper serialization/deserialization
        user.setType("text");
        return userDao.save(user);
    }

    @Override
    public ORCiDUser createORCiDUser(ORCiDUser user) {
        // Set the type field for proper serialization/deserialization
        user.setType("orcid");
        return userDao.save(user);
    }

    @Override
    public User updateUser(String id, User user) {
        if (!userDao.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }

        // Ensure the ID is set correctly
        if (user instanceof TextUser) {
            user.setInternalId(id);
        } else if (user instanceof ORCiDUser) {
            user.setInternalId(id);
        }

        return userDao.save(user);
    }

    @Override
    public void deleteUser(String id) {
        if (!userDao.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " not found");
        }
        userDao.deleteById(id);
    }
}