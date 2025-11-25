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
package edu.kit.datamanager.idoris.datatypes.services;

import edu.kit.datamanager.idoris.core.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.core.domain.TypeProfile;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.datatypes.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileDto;
import edu.kit.datamanager.idoris.datatypes.events.TypeProfileCreatedEvent;
import edu.kit.datamanager.idoris.datatypes.events.TypeProfilePatchedEvent;
import edu.kit.datamanager.idoris.datatypes.events.TypeProfileUpdatedEvent;
import edu.kit.datamanager.idoris.datatypes.mappers.TypeProfileMapper;
import edu.kit.datamanager.idoris.operations.services.api.IOperationExternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeProfileDtoServiceTest {

    @Mock
    private ITypeProfileDao dao;

    @Mock
    private EventPublisherService publisher;

    @Mock
    private ApplicationProperties appProps;

    @Mock
    private IOperationExternalService operationService;

    private TypeProfileMapper mapper;
    private TypeProfileDtoService service;

    @BeforeEach
    void setUp() {
        mapper = new TypeProfileMapper();
        lenient().when(appProps.getValidationPolicy())
                .thenReturn(ApplicationProperties.ValidationPolicy.LAX);
        service = new TypeProfileDtoService(dao, publisher, mapper, appProps, operationService);
    }

    @Test
    void create_withoutRelations_publishesCreatedEventAfterReload() {
        TypeProfileDto dto = TypeProfileDto.builder().name("tp").build();
        // save assigns ID via mock
        TypeProfile savedMock = mock(TypeProfile.class);
        lenient().when(savedMock.getId()).thenReturn("tp-1");
        when(dao.save(any(TypeProfile.class))).thenReturn(savedMock);
        // reload returns same entity (no relations set)
        when(dao.findById("tp-1")).thenReturn(Optional.of(new TypeProfile()));

        TypeProfileDto created = service.create(dto);
        assertNotNull(created);

        // verify event published
        verify(publisher, times(1)).publishEvent(isA(TypeProfileCreatedEvent.class));
        // verify no relation ops executed when sets are null
        verify(dao, never()).addInheritsFrom(anyString(), anyCollection());
        verify(dao, never()).addAttributes(anyString(), anyCollection());
    }

    @Test
    void update_publishesUpdatedEventAfterValidation() {
        TypeProfile existing = new TypeProfile();
        when(dao.findById("tp-2")).thenReturn(Optional.of(existing));
        when(dao.save(any(TypeProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        TypeProfileDto dto = TypeProfileDto.builder().description("new desc").build();
        TypeProfileDto result = service.update("tp-2", dto);
        assertNotNull(result);
        verify(publisher).publishEvent(isA(TypeProfileUpdatedEvent.class));
    }

    @Test
    void patch_publishesPatchedEventAfterValidation() {
        TypeProfile existing = new TypeProfile();
        when(dao.findById("tp-3")).thenReturn(Optional.of(existing));
        when(dao.save(any(TypeProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        TypeProfileDto dto = TypeProfileDto.builder().name("patched").build();
        TypeProfileDto result = service.patch("tp-3", dto);
        assertNotNull(result);
        verify(publisher).publishEvent(isA(TypeProfilePatchedEvent.class));
    }
}
