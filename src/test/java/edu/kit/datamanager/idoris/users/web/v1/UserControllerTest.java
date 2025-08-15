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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.idoris.users.entities.ORCiDUser;
import edu.kit.datamanager.idoris.users.entities.TextUser;
import edu.kit.datamanager.idoris.users.entities.User;
import edu.kit.datamanager.idoris.users.services.UserService;
import edu.kit.datamanager.idoris.users.web.hateoas.UserModelAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String userId = "test-id";
    private MockMvc mockMvc;
    @Mock
    private UserService userService;
    @Mock
    private UserModelAssembler userModelAssembler;
    private TextUser textUser;
    private ORCiDUser orcidUser;

    @BeforeEach
    void setUp() throws Exception {
        textUser = new TextUser("John Doe", "john.doe@example.com", "Test user");
        orcidUser = new ORCiDUser(new URL("https://orcid.org/0000-0000-0000-0000"));

        // Set up the model assembler to return EntityModel of the entity
        // Use lenient() to avoid "unnecessary stubbing" errors
        lenient().when(userModelAssembler.toModel(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return EntityModel.of(user);
        });

        // Initialize mockMvc with the controller and mocked services
        UserController userController = new UserController(userService, userModelAssembler);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void getAllUsers() throws Exception {
        List<User> users = Arrays.asList(textUser, orcidUser);
        when(userService.findAllUsers()).thenReturn(users);

        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk());

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    void getUserById() throws Exception {
        when(userService.findUserById(userId)).thenReturn(Optional.of(textUser));

        mockMvc.perform(get("/v1/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).findUserById(userId);
        verify(userModelAssembler, times(1)).toModel(any(User.class));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        when(userService.findUserById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findUserById(userId);
    }

    @Test
    void getAllTextUsers() throws Exception {
        List<TextUser> users = Collections.singletonList(textUser);
        when(userService.findAllTextUsers()).thenReturn(users);

        mockMvc.perform(get("/v1/users/text"))
                .andExpect(status().isOk());

        verify(userService, times(1)).findAllTextUsers();
    }

    @Test
    void getTextUserByEmail() throws Exception {
        String email = "john.doe@example.com";
        when(userService.findTextUserByEmail(email)).thenReturn(Optional.of(textUser));

        mockMvc.perform(get("/v1/users/text/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is(email)));

        verify(userService, times(1)).findTextUserByEmail(email);
    }

    @Test
    void getTextUserByEmail_NotFound() throws Exception {
        String email = "nonexistent@example.com";
        when(userService.findTextUserByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/text/email/{email}", email))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findTextUserByEmail(email);
    }

    @Test
    void getAllORCiDUsers() throws Exception {
        List<ORCiDUser> users = Collections.singletonList(orcidUser);
        when(userService.findAllORCiDUsers()).thenReturn(users);

        mockMvc.perform(get("/v1/users/orcid"))
                .andExpect(status().isOk());

        verify(userService, times(1)).findAllORCiDUsers();
    }

    @Test
    void getORCiDUserByORCiD() throws Exception {
        String orcidStr = "0000-0000-0000-0000";
        URL orcid = new URL("https://orcid.org/" + orcidStr);
        when(userService.findORCiDUserByORCiD(any(URL.class))).thenReturn(Optional.of(orcidUser));

        mockMvc.perform(get("/v1/users/orcid/{orcid}", orcidStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcid", is(orcid.toString())));

        verify(userService, times(1)).findORCiDUserByORCiD(any(URL.class));
    }

    @Test
    void getORCiDUserByORCiD_NotFound() throws Exception {
        String orcidStr = "0000-0000-0000-0001";
        URL orcid = new URL("https://orcid.org/" + orcidStr);
        when(userService.findORCiDUserByORCiD(any(URL.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/orcid/{orcid}", orcidStr))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findORCiDUserByORCiD(any(URL.class));
    }

    @Test
    void createTextUser() throws Exception {
        when(userService.createTextUser(any(TextUser.class))).thenReturn(textUser);

        mockMvc.perform(post("/v1/users/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(textUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        verify(userService, times(1)).createTextUser(any(TextUser.class));
    }

    @Test
    void createORCiDUser() throws Exception {
        when(userService.createORCiDUser(any(ORCiDUser.class))).thenReturn(orcidUser);

        mockMvc.perform(post("/v1/users/orcid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orcidUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orcid", is("https://orcid.org/0000-0000-0000-0000")));

        verify(userService, times(1)).createORCiDUser(any(ORCiDUser.class));
    }

    @Test
    void updateUser() throws Exception {
        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(textUser);

        mockMvc.perform(put("/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(textUser)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(eq(userId), any(User.class));
        verify(userModelAssembler, times(1)).toModel(any(User.class));
    }

    @Test
    void updateUser_NotFound() throws Exception {
        when(userService.updateUser(eq(userId), any(User.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(put("/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(textUser)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(userId), any(User.class));
    }

    @Test
    void deleteUser() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void deleteUser_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("User not found")).when(userService).deleteUser(userId);

        mockMvc.perform(delete("/v1/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Configuration
    @Import(UserController.class)
    static class TestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public UserModelAssembler userModelAssembler() {
            return mock(UserModelAssembler.class);
        }
    }
}
