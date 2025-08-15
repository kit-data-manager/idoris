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

package edu.kit.datamanager.idoris.core.domain.dao;

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IGenericRepoTest {

    @Mock
    private IGenericRepo<AdministrativeMetadata> repository;

    @Mock
    private AdministrativeMetadata entity;

    @BeforeEach
    void setUp() {
        // Set up the mock repository to return the entity when findByPid is called with "pid123"
        when(repository.findByPid("pid123")).thenReturn(Optional.of(entity));

        // Set up the mock repository to return the entity when findByInternalId is called with "internal456"
        when(repository.findByInternalId("internal456")).thenReturn(Optional.of(entity));

        // Set up the mock repository to delegate findById to the default implementation
        when(repository.findById(anyString())).thenCallRealMethod();
    }

    @Test
    void findById_WithPid_ShouldFindByPid() {
        // When
        Optional<AdministrativeMetadata> result = repository.findById("pid123");

        // Then
        assertTrue(result.isPresent());
        assertEquals(entity, result.get());
        verify(repository).findByPid("pid123");
        verify(repository, never()).findByInternalId(anyString());
    }

    @Test
    void findById_WithInternalId_ShouldFindByInternalId() {
        // Given
        when(repository.findByPid("internal456")).thenReturn(Optional.empty());

        // When
        Optional<AdministrativeMetadata> result = repository.findById("internal456");

        // Then
        assertTrue(result.isPresent());
        assertEquals(entity, result.get());
        verify(repository).findByPid("internal456");
        verify(repository).findByInternalId("internal456");
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        // Given
        when(repository.findByPid("nonexistent")).thenReturn(Optional.empty());
        when(repository.findByInternalId("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<AdministrativeMetadata> result = repository.findById("nonexistent");

        // Then
        assertTrue(result.isEmpty());
        verify(repository).findByPid("nonexistent");
        verify(repository).findByInternalId("nonexistent");
    }
}