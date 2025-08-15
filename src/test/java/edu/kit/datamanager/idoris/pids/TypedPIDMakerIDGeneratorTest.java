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

package edu.kit.datamanager.idoris.pids;

import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.configuration.TypedPIDMakerConfig;
import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import edu.kit.datamanager.idoris.pids.client.TypedPIDMakerClient;
import edu.kit.datamanager.idoris.pids.client.model.PIDRecord;
import edu.kit.datamanager.idoris.rules.logic.RuleOutput;
import edu.kit.datamanager.idoris.rules.logic.Visitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypedPIDMakerIDGeneratorTest {

    private final String baseUrl = "http://example.com";
    @Mock
    private TypedPIDMakerClient client;
    @Mock
    private TypedPIDMakerConfig config;
    @Mock
    private ApplicationProperties applicationProperties;
    private TypedPIDMakerIDGenerator generator;

    @BeforeEach
    void setUp() {
        when(applicationProperties.getBaseUrl()).thenReturn(baseUrl);
        generator = new TypedPIDMakerIDGenerator(applicationProperties, client, config);
    }

    @Test
    void generateId_alwaysIncludesPointerToEntity() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setInternalId("test-internal-id");

        when(config.isMeaningfulPIDRecords()).thenReturn(false);

        PIDRecord createdRecord = new PIDRecord("test-pid", List.of());
        when(client.createPIDRecord(any())).thenReturn(createdRecord);

        // Act
        String pid = generator.generateId("TestEntity", entity);

        // Assert
        assertEquals("test-pid", pid);

        ArgumentCaptor<PIDRecord> recordCaptor = ArgumentCaptor.forClass(PIDRecord.class);
        verify(client).createPIDRecord(recordCaptor.capture());

        PIDRecord capturedRecord = recordCaptor.getValue();

        // Verify that the record contains a pointer to the entity
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("digitalObjectLocation") &&
                        entry.value().contains(entity.getInternalId())));

        // Verify that the record contains the entity type
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("entityType") &&
                        entry.value().equals("TestEntity")));
    }

    @Test
    void generateId_withMeaningfulRecords_includesAdministrativeMetadata() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setInternalId("test-internal-id");
        entity.setName("Test Entity");
        entity.setDescription("Test Description");
        entity.setCreatedAt(Instant.now());
        entity.setLastModifiedAt(Instant.now());
        entity.setVersion(1L);

        when(config.isMeaningfulPIDRecords()).thenReturn(true);

        PIDRecord createdRecord = new PIDRecord("test-pid", List.of());
        when(client.createPIDRecord(any())).thenReturn(createdRecord);

        // Act
        String pid = generator.generateId("TestEntity", entity);

        // Assert
        assertEquals("test-pid", pid);

        ArgumentCaptor<PIDRecord> recordCaptor = ArgumentCaptor.forClass(PIDRecord.class);
        verify(client).createPIDRecord(recordCaptor.capture());

        PIDRecord capturedRecord = recordCaptor.getValue();

        // Verify that the record contains a pointer to the entity
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("digitalObjectLocation") &&
                        entry.value().contains(entity.getInternalId())));

        // Verify that the record contains the entity type
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("entityType") &&
                        entry.value().equals("TestEntity")));

        // Verify that the record contains the Helmholtz Kernel Information Profile
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("21.T11148/076759916209e5d62bd5") &&
                        entry.value().equals("21.T11148/b9b76f887845e32d29f7")));

        // Verify that the record contains basic metadata
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("name") &&
                        entry.value().equals("Test Entity")));

        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("description") &&
                        entry.value().equals("Test Description")));

        // Verify that the record contains timestamps
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("21.T11148/aafd5fb4c7222e2d950a")));

        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("21.T11148/397d831aa3a9d18eb52c")));

        // Verify that the record contains version information
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("21.T11148/c692273deb2772da307f") &&
                        entry.value().equals("1")));
    }

    @Test
    void generateId_withExistingPid_updatesRecord() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setInternalId("test-internal-id");
        entity.setPid("existing-pid");
        entity.setName("Test Entity");

        when(config.isMeaningfulPIDRecords()).thenReturn(true);
        when(config.isUpdatePIDRecords()).thenReturn(true);

        PIDRecord existingRecord = new PIDRecord("existing-pid", List.of());
        when(client.getPIDRecord("existing-pid")).thenReturn(existingRecord);

        // Act
        String pid = generator.generateId("TestEntity", entity);

        // Assert
        assertEquals("existing-pid", pid);

        ArgumentCaptor<PIDRecord> recordCaptor = ArgumentCaptor.forClass(PIDRecord.class);
        verify(client).updatePIDRecord(eq("existing-pid"), recordCaptor.capture());

        PIDRecord capturedRecord = recordCaptor.getValue();

        // Verify that the record contains a pointer to the entity
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("digitalObjectLocation") &&
                        entry.value().contains("existing-pid")));

        // Verify that the record contains the entity type
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("entityType") &&
                        entry.value().equals("TestEntity")));

        // Verify that the record contains basic metadata
        assertTrue(capturedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("name") &&
                        entry.value().equals("Test Entity")));
    }

    // Test entity class
    private static class TestEntity extends AdministrativeMetadata {
        @Override
        protected <T extends RuleOutput<T>> T accept(Visitor<T> visitor, Object... args) {
            // Simple implementation for testing purposes
            return null;
        }
    }
}