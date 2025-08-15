/*
 * Copyright (c) 2025 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.pids.web.v1;

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import edu.kit.datamanager.idoris.pids.services.PersistentIdentifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PidControllerTest {

    @Mock
    private PersistentIdentifierService pidService;

    @InjectMocks
    private PidController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllPersistentIdentifiers_shouldReturnAllPersistentIdentifiers() throws Exception {
        // Arrange
        PersistentIdentifier pid1 = createPersistentIdentifier("pid1", "TestEntity", "entity1", false);
        PersistentIdentifier pid2 = createPersistentIdentifier("pid2", "TestEntity", "entity2", false);
        List<PersistentIdentifier> pids = List.of(pid1, pid2);

        when(pidService.getAllPersistentIdentifiers()).thenReturn(pids);

        // Act & Assert
        mockMvc.perform(get("/pid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pid", is("pid1")))
                .andExpect(jsonPath("$[1].pid", is("pid2")));
    }

    @Test
    void redirectToEntity_withExistingPid_shouldRedirectToEntity() throws Exception {
        // Arrange
        String pidValue = "test-pid";
        String entityType = "testentity";
        String entityId = "entity-id";

        PersistentIdentifier pid = createPersistentIdentifier(pidValue, "TestEntity", entityId, false);
        pid.setEntity(mock(AdministrativeMetadata.class));

        when(pidService.getPersistentIdentifier(pidValue)).thenReturn(Optional.of(pid));

        // Act & Assert
        mockMvc.perform(get("/pid/{pidValue}", pidValue))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/" + entityType + "s/" + entityId));
    }

    @Test
    void redirectToEntity_withTombstonePid_shouldRedirectToTombstone() throws Exception {
        // Arrange
        String pidValue = "test-pid";

        PersistentIdentifier pid = createPersistentIdentifier(pidValue, "TestEntity", "entity-id", true);

        when(pidService.getPersistentIdentifier(pidValue)).thenReturn(Optional.of(pid));

        // Act & Assert
        mockMvc.perform(get("/pid/{pidValue}", pidValue))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/tombstone/" + pidValue));
    }

    @Test
    void redirectToEntity_withNonExistingPid_shouldReturnNotFound() throws Exception {
        // Arrange
        String pidValue = "non-existing-pid";

        when(pidService.getPersistentIdentifier(pidValue)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/pid/{pidValue}", pidValue))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirectToEntity_withPidWithoutEntityAndNotTombstone_shouldReturnNotFound() throws Exception {
        // Arrange
        String pidValue = "test-pid";

        PersistentIdentifier pid = createPersistentIdentifier(pidValue, "TestEntity", "entity-id", false);
        pid.setEntity(null);

        when(pidService.getPersistentIdentifier(pidValue)).thenReturn(Optional.of(pid));

        // Act & Assert
        mockMvc.perform(get("/pid/{pidValue}", pidValue))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleTombstone_withTombstonePid_shouldReturnGoneWithMessage() throws Exception {
        // Arrange
        String pidValue = "test-pid";
        Instant deletedAt = Instant.parse("2023-01-01T00:00:00Z");

        PersistentIdentifier pid = createPersistentIdentifier(pidValue, "TestEntity", "entity-id", true);
        pid.setDeletedAt(deletedAt);

        when(pidService.getPersistentIdentifier(pidValue)).thenReturn(Optional.of(pid));

        // Act & Assert
        mockMvc.perform(get("/pid/tombstone/{pidValue}", pidValue))
                .andExpect(status().isGone())
                .andExpect(content().string("The entity with PID test-pid has been deleted at 2023-01-01T00:00:00Z. Entity type: TestEntity"));
    }

    @Test
    void handleTombstone_withNonTombstonePid_shouldRedirectToEntity() throws Exception {
        // Arrange
        String pidValue = "test-pid";

        PersistentIdentifier pid = createPersistentIdentifier(pidValue, "TestEntity", "entity-id", false);

        when(pidService.getPersistentIdentifier(pidValue)).thenReturn(Optional.of(pid));

        // Act & Assert
        mockMvc.perform(get("/pid/tombstone/{pidValue}", pidValue))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/pid/" + pidValue));
    }

    @Test
    void handleTombstone_withNonExistingPid_shouldReturnNotFound() throws Exception {
        // Arrange
        String pidValue = "non-existing-pid";

        when(pidService.getPersistentIdentifier(pidValue)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/pid/tombstone/{pidValue}", pidValue))
                .andExpect(status().isNotFound());
    }

    private PersistentIdentifier createPersistentIdentifier(String pid, String entityType, String entityInternalId, boolean tombstone) {
        PersistentIdentifier persistentIdentifier = new PersistentIdentifier();
        persistentIdentifier.setPid(pid);
        persistentIdentifier.setEntityType(entityType);
        persistentIdentifier.setEntityInternalId(entityInternalId);
        persistentIdentifier.setTombstone(tombstone);
        if (tombstone) {
            persistentIdentifier.setDeletedAt(Instant.now());
        }
        persistentIdentifier.setCreatedAt(Instant.now());
        persistentIdentifier.setLastModifiedAt(Instant.now());
        persistentIdentifier.setVersion(1L);
        persistentIdentifier.setMetadata(new HashMap<>());
        return persistentIdentifier;
    }
}