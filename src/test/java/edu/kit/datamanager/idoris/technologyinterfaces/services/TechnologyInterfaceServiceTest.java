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
package edu.kit.datamanager.idoris.technologyinterfaces.services;

import edu.kit.datamanager.idoris.attributes.services.api.IAttributeInternalService;
import edu.kit.datamanager.idoris.core.domain.TechnologyInterface;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.technologyinterfaces.dao.ITechnologyInterfaceDao;
import edu.kit.datamanager.idoris.technologyinterfaces.dto.TechnologyInterfaceDto;
import edu.kit.datamanager.idoris.technologyinterfaces.events.TechnologyInterfaceCreatedEvent;
import edu.kit.datamanager.idoris.technologyinterfaces.mappers.TechnologyInterfaceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechnologyInterfaceServiceTest {

    @Mock
    private ITechnologyInterfaceDao dao;

    @Mock
    private EventPublisherService publisher;

    @Mock
    private IAttributeInternalService attributeInternalService;

    private TechnologyInterfaceMapper mapper;
    private TechnologyInterfaceService service;

    @BeforeEach
    void setUp() {
        mapper = new TechnologyInterfaceMapper();
        service = new TechnologyInterfaceService(dao, publisher, mapper, attributeInternalService);
    }

    @Test
    void create_withInputsAndOutputs_linksRelations_andPublishesEvent() {
        TechnologyInterfaceDto dto = TechnologyInterfaceDto.builder()
                .name("TI")
                .attributeIds(Set.of("a1", "a2"))
                .outputIds(Set.of("o1"))
                .build();
        // save returns entity with id
        TechnologyInterface saved = mock(TechnologyInterface.class);
        lenient().when(saved.getId()).thenReturn("ti-1");
        when(dao.save(any(TechnologyInterface.class))).thenReturn(saved);
        when(dao.findById("ti-1")).thenReturn(Optional.of(saved));

        TechnologyInterfaceDto created = service.create(dto);
        assertNotNull(created);

        // Ensure attributes existence is checked
        verify(attributeInternalService, times(1)).ensureExists("a1");
        verify(attributeInternalService, times(1)).ensureExists("a2");
        verify(attributeInternalService, times(1)).ensureExists("o1");
        // DAO link operations executed
        verify(dao).linkInputs("ti-1", Set.of("a1", "a2"));
        verify(dao).linkOutputs("ti-1", Set.of("o1"));
        // Event published
        verify(publisher).publishEvent(isA(TechnologyInterfaceCreatedEvent.class));
    }

    @Test
    void linkInputs_unlinkOutputs_callDao() {
        when(dao.findById("ti-2")).thenReturn(Optional.of(new TechnologyInterface()));
        // link inputs
        service.linkInputs("ti-2", Set.of("a1"));
        verify(attributeInternalService).ensureExists("a1");
        verify(dao).linkInputs("ti-2", Set.of("a1"));
        // unlink outputs
        service.unlinkOutputs("ti-2", Set.of("o1"));
        verify(dao).unlinkOutputs("ti-2", Set.of("o1"));
    }
}
