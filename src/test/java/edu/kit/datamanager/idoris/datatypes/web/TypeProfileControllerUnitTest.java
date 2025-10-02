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
package edu.kit.datamanager.idoris.datatypes.web;

import edu.kit.datamanager.idoris.datatypes.dto.TypeProfileDto;
import edu.kit.datamanager.idoris.datatypes.services.api.ITypeProfileExternalService;
import edu.kit.datamanager.idoris.datatypes.web.hateoas.TypeProfileModelAssembler;
import edu.kit.datamanager.idoris.datatypes.web.v1.TypeProfileController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import static org.mockito.Mockito.when;

class TypeProfileControllerUnitTest {

    @Mock
    private ITypeProfileExternalService service;

    private TypeProfileController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new TypeProfileController(service, new TypeProfileModelAssembler());
    }

    @Test
    void get_and_list() {
        TypeProfileDto dto = TypeProfileDto.builder().name("TP").build();
        when(service.get("id1")).thenReturn(Optional.of(dto));
        when(service.list()).thenReturn(List.of(dto));

        ResponseEntity<EntityModel<TypeProfileDto>> get = controller.get("id1");
        assertEquals(HttpStatus.OK, get.getStatusCode());
        assertNotNull(get.getBody());
        assertEquals("TP", get.getBody().getContent().getName());

        ResponseEntity<CollectionModel<EntityModel<TypeProfileDto>>> list = controller.list();
        assertEquals(HttpStatus.OK, list.getStatusCode());
        assertNotNull(list.getBody());
        assertEquals(1, list.getBody().getContent().size());
    }

    @Test
    void create_put_patch_delete() {
        TypeProfileDto createdDto = TypeProfileDto.builder().name("C").build();
        when(service.create(any(TypeProfileDto.class))).thenReturn(createdDto);
        ResponseEntity<EntityModel<TypeProfileDto>> created = controller.create(createdDto);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());

        when(service.get("id")).thenReturn(Optional.of(TypeProfileDto.builder().build()));
        when(service.update(eq("id"), any(TypeProfileDto.class)))
                .thenReturn(TypeProfileDto.builder().name("U").build());
        ResponseEntity<EntityModel<TypeProfileDto>> updated = controller.update("id", TypeProfileDto.builder().build());
        assertEquals(HttpStatus.OK, updated.getStatusCode());

        when(service.patch(eq("id"), any(TypeProfileDto.class)))
                .thenReturn(TypeProfileDto.builder().name("P").build());
        ResponseEntity<EntityModel<TypeProfileDto>> patched = controller.patch("id", TypeProfileDto.builder().build());
        assertEquals(HttpStatus.OK, patched.getStatusCode());

        ResponseEntity<Void> deleted = controller.delete("id");
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());
    }

    @Test
    void link_unlink_endpoints() {
        when(service.get("id")).thenReturn(Optional.of(TypeProfileDto.builder().build()));
        when(service.addInheritsFrom(eq("id"), any(Set.class))).thenReturn(TypeProfileDto.builder().build());
        when(service.removeInheritsFrom(eq("id"), any(Set.class))).thenReturn(TypeProfileDto.builder().build());
        when(service.addAttributes(eq("id"), any(Set.class))).thenReturn(TypeProfileDto.builder().build());
        when(service.removeAttributes(eq("id"), any(Set.class))).thenReturn(TypeProfileDto.builder().build());

        assertEquals(HttpStatus.OK, controller.linkInheritsFrom("id", Set.of("p")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.unlinkInheritsFrom("id", Set.of("p")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.linkAttributes("id", Set.of("a")).getStatusCode());
        assertEquals(HttpStatus.OK, controller.unlinkAttributes("id", Set.of("a")).getStatusCode());
    }
}
