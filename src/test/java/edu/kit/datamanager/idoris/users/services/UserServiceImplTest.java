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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private IUserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private TextUser textUser;
    private ORCiDUser orcidUser;
    private final String userId = "test-id";

    @BeforeEach
    void setUp() throws MalformedURLException {
        textUser = new TextUser("John Doe", "john.doe@example.com", "Test user");
        orcidUser = new ORCiDUser(new URL("https://orcid.org/0000-0000-0000-0000"));
    }

    @Test
    void findAllUsers() {
        when(userDao.findAll()).thenReturn(Arrays.asList(textUser, orcidUser));

        List<User> users = userService.findAllUsers();

        assertEquals(2, users.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void findUserById() {
        when(userDao.findById(userId)).thenReturn(Optional.of(textUser));

        Optional<User> result = userService.findUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(textUser, result.get());
        verify(userDao, times(1)).findById(userId);
    }

    @Test
    void findAllTextUsers() {
        when(userDao.findAllTextUsers()).thenReturn(Collections.singletonList(textUser));

        List<TextUser> users = userService.findAllTextUsers();

        assertEquals(1, users.size());
        assertEquals(textUser, users.get(0));
        verify(userDao, times(1)).findAllTextUsers();
    }

    @Test
    void findTextUserByEmail() {
        String email = "john.doe@example.com";
        when(userDao.findTextUserByEmail(email)).thenReturn(textUser);

        Optional<TextUser> result = userService.findTextUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(textUser, result.get());
        verify(userDao, times(1)).findTextUserByEmail(email);
    }

    @Test
    void findAllORCiDUsers() {
        when(userDao.findAllORCiDUsers()).thenReturn(Collections.singletonList(orcidUser));

        List<ORCiDUser> users = userService.findAllORCiDUsers();

        assertEquals(1, users.size());
        assertEquals(orcidUser, users.get(0));
        verify(userDao, times(1)).findAllORCiDUsers();
    }

    @Test
    void findORCiDUserByORCiD() throws MalformedURLException {
        URL orcid = new URL("https://orcid.org/0000-0000-0000-0000");
        when(userDao.findORCiDUserByORCiD(orcid.toString())).thenReturn(orcidUser);

        Optional<ORCiDUser> result = userService.findORCiDUserByORCiD(orcid);

        assertTrue(result.isPresent());
        assertEquals(orcidUser, result.get());
        verify(userDao, times(1)).findORCiDUserByORCiD(orcid.toString());
    }

    @Test
    void createTextUser() {
        when(userDao.save(any(TextUser.class))).thenReturn(textUser);

        TextUser result = userService.createTextUser(textUser);

        assertEquals(textUser, result);
        assertEquals("text", textUser.getType());
        verify(userDao, times(1)).save(textUser);
    }

    @Test
    void createORCiDUser() {
        when(userDao.save(any(ORCiDUser.class))).thenReturn(orcidUser);

        ORCiDUser result = userService.createORCiDUser(orcidUser);

        assertEquals(orcidUser, result);
        assertEquals("orcid", orcidUser.getType());
        verify(userDao, times(1)).save(orcidUser);
    }

    @Test
    void updateUser() {
        when(userDao.existsById(userId)).thenReturn(true);
        when(userDao.save(any(User.class))).thenReturn(textUser);

        User result = userService.updateUser(userId, textUser);

        assertEquals(textUser, result);
        verify(userDao, times(1)).existsById(userId);
        verify(userDao, times(1)).save(textUser);
    }

    @Test
    void updateUser_NotFound() {
        when(userDao.existsById(userId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(userId, textUser));
        verify(userDao, times(1)).existsById(userId);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void deleteUser() {
        when(userDao.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userDao, times(1)).existsById(userId);
        verify(userDao, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_NotFound() {
        when(userDao.existsById(userId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(userId));
        verify(userDao, times(1)).existsById(userId);
        verify(userDao, never()).deleteById(anyString());
    }
}