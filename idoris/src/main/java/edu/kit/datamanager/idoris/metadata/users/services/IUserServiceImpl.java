/*
 * Copyright (c) 2024-2026 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.metadata.users.services;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.domain.User;
import edu.kit.datamanager.idoris.core.domain.valueObjects.ORCiD;
import edu.kit.datamanager.idoris.metadata.users.api.IUserService;
import edu.kit.datamanager.idoris.metadata.users.dao.IUserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
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

        if (user.getName() != null) {
            toSave.setName(user.getName());
        }
        if (user.getOrcid() != null) {
            ORCiD orcid = user.getOrcid();

            // Check for existing user with the same ORCiD
            if (userDao.findByOrcid(orcid.get()).isPresent()) {
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
    public Optional<User> findUserByORCiD(URL orcid) {
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