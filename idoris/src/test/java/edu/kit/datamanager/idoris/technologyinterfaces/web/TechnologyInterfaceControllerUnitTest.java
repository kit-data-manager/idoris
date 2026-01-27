/*
 * Copyright (c) 2025-2026 Karlsruhe Institute of Technology
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
package edu.kit.datamanager.idoris.technologyinterfaces.web;

import edu.kit.datamanager.idoris.technologyinterfaces.api.ITechnologyInterfaceService;
import edu.kit.datamanager.idoris.technologyinterfaces.dto.TechnologyInterfaceDto;
import edu.kit.datamanager.idoris.technologyinterfaces.web.v1.TechnologyInterfaceController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class TechnologyInterfaceControllerUnitTest {

    @Mock
    private ITechnologyInterfaceService service;

    @InjectMocks
    private TechnologyInterfaceController controller;

//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//        controller = new TechnologyInterfaceController);
//        // inject mock via reflection because fields are @Autowired
//        try {
//            var f = TechnologyInterfaceController.class.getDeclaredField("technologyInterfaceService");
//            f.setAccessible(true);
//            f.set(controller, service);
//            var a = TechnologyInterfaceController.class.getDeclaredField("assembler");
//            a.setAccessible(true);
//            a.set(controller, new TechnologyInterfaceDtoModelAssembler());
//        } catch (Exception e) {
//            fail("Failed to inject dependencies: " + e.getMessage());
//        }
//    }

    @Test
    void get_returnsOkOrNotFound() {
        TechnologyInterfaceDto dto = TechnologyInterfaceDto.builder().name("X").build();
        when(service.get("id1")).thenReturn(Optional.of(dto));
        when(service.get("missing")).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<TechnologyInterfaceDto>> ok = controller.getTechnologyInterface("id1");
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        assertNotNull(ok.getBody());
        assertEquals("X", ok.getBody().getContent().getName());

        ResponseEntity<EntityModel<TechnologyInterfaceDto>> nf = controller.getTechnologyInterface("missing");
        assertEquals(HttpStatus.NOT_FOUND, nf.getStatusCode());
    }

    @Test
    void list_returnsOk() {
        when(service.list()).thenReturn(List.of(TechnologyInterfaceDto.builder().name("A").build()));
        ResponseEntity<CollectionModel<EntityModel<TechnologyInterfaceDto>>> res = controller.getAllTechnologyInterfaces();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(1, res.getBody().getContent().size());
    }

    @Test
    void create_returnsCreated() {
        TechnologyInterfaceDto input = TechnologyInterfaceDto.builder().name("N").build();
        when(service.create(any(TechnologyInterfaceDto.class))).thenReturn(input);
        ResponseEntity<EntityModel<TechnologyInterfaceDto>> res = controller.createTechnologyInterface(input);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("N", res.getBody().getContent().getName());
    }

    @Test
    void update_checksExistence() {
        when(service.get("id")).thenReturn(Optional.empty());
        ResponseEntity<EntityModel<TechnologyInterfaceDto>> nf = controller.updateTechnologyInterface("id", TechnologyInterfaceDto.builder().build());
        assertEquals(HttpStatus.NOT_FOUND, nf.getStatusCode());

        when(service.get("id")).thenReturn(Optional.of(TechnologyInterfaceDto.builder().build()));
        when(service.update(eq("id"), any(TechnologyInterfaceDto.class)))
                .thenReturn(TechnologyInterfaceDto.builder().name("U").build());
        ResponseEntity<EntityModel<TechnologyInterfaceDto>> ok = controller.updateTechnologyInterface("id", TechnologyInterfaceDto.builder().build());
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        assertNotNull(ok.getBody());
        assertEquals("U", ok.getBody().getContent().getName());
    }

    @Test
    void patch_checksExistence() {
        when(service.get("id")).thenReturn(Optional.empty());
        ResponseEntity<EntityModel<TechnologyInterfaceDto>> nf = controller.patchTechnologyInterface("id", TechnologyInterfaceDto.builder().build());
        assertEquals(HttpStatus.NOT_FOUND, nf.getStatusCode());

        when(service.get("id")).thenReturn(Optional.of(TechnologyInterfaceDto.builder().build()));
        when(service.patch(eq("id"), any(TechnologyInterfaceDto.class)))
                .thenReturn(TechnologyInterfaceDto.builder().name("P").build());
        ResponseEntity<EntityModel<TechnologyInterfaceDto>> ok = controller.patchTechnologyInterface("id", TechnologyInterfaceDto.builder().build());
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        assertNotNull(ok.getBody());
        assertEquals("P", ok.getBody().getContent().getName());
    }

    @Test
    void delete_checksExistence() {
        when(service.get("id")).thenReturn(Optional.empty());
        ResponseEntity<Void> nf = controller.deleteTechnologyInterface("id");
        assertEquals(HttpStatus.NOT_FOUND, nf.getStatusCode());

        when(service.get("id")).thenReturn(Optional.of(TechnologyInterfaceDto.builder().build()));
        doNothing().when(service).delete("id");
        ResponseEntity<Void> noContent = controller.deleteTechnologyInterface("id");
        assertEquals(HttpStatus.NO_CONTENT, noContent.getStatusCode());
    }

    @Test
    void link_and_unlink_endpoints_delegate() {
        when(service.get("id")).thenReturn(Optional.of(TechnologyInterfaceDto.builder().build()));
        when(service.linkInputs(eq("id"), any(Set.class))).thenReturn(TechnologyInterfaceDto.builder().build());
        when(service.unlinkInputs(eq("id"), any(Set.class))).thenReturn(TechnologyInterfaceDto.builder().build());
        when(service.linkOutputs(eq("id"), any(Set.class))).thenReturn(TechnologyInterfaceDto.builder().build());
        when(service.unlinkOutputs(eq("id"), any(Set.class))).thenReturn(TechnologyInterfaceDto.builder().build());

        assertEquals(HttpStatus.OK, controller.linkAttributes("id", Set.of("a")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.unlinkAttributes("id", Set.of("a")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.linkOutputs("id", Set.of("a")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.unlinkOutputs("id", Set.of("a")).getStatusCode());
    }
}
