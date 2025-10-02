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
package edu.kit.datamanager.idoris.technologyinterfaces.web;

import edu.kit.datamanager.idoris.technologyinterfaces.dto.TechnologyInterfaceDto;
import edu.kit.datamanager.idoris.technologyinterfaces.services.api.ITechnologyInterfaceExternalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = edu.kit.datamanager.idoris.technologyinterfaces.web.v1.TechnologyInterfaceController.class)
@org.springframework.boot.autoconfigure.ImportAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class
})
@ExtendWith(SpringExtension.class)
@org.springframework.test.context.TestPropertySource(properties = {
        "idoris.base-url=http://localhost:8095",
        "spring.main.allow-bean-definition-overriding=true"
})
class TechnologyInterfaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ITechnologyInterfaceExternalService service;

    @Test
    void get_returns200_whenFound_and404_whenMissing() throws Exception {
        TechnologyInterfaceDto dto = TechnologyInterfaceDto.builder().name("TI-1").build();
        when(service.get("ti-1")).thenReturn(Optional.of(dto));
        when(service.get("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/technologyInterfaces/ti-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("TI-1")));

        mockMvc.perform(get("/v1/technologyInterfaces/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_returns200_andArray() throws Exception {
        TechnologyInterfaceDto dto1 = TechnologyInterfaceDto.builder().name("A").build();
        TechnologyInterfaceDto dto2 = TechnologyInterfaceDto.builder().name("B").build();
        when(service.list()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/v1/technologyInterfaces"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("A")))
                .andExpect(jsonPath("$[1].name", is("B")));
    }

    @Test
    void create_returns201() throws Exception {
        TechnologyInterfaceDto input = TechnologyInterfaceDto.builder().name("New").build();
        when(service.create(any(TechnologyInterfaceDto.class))).thenReturn(input);

        String body = "{\"name\":\"New\"}";
        mockMvc.perform(post("/v1/technologyInterfaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New")));
    }

    @Test
    void update_returns404_whenMissing_and200_whenExists() throws Exception {
        when(service.get("ti-2")).thenReturn(Optional.of(TechnologyInterfaceDto.builder().name("Old").build()));
        when(service.update(eq("ti-2"), any(TechnologyInterfaceDto.class)))
                .thenReturn(TechnologyInterfaceDto.builder().name("Updated").build());
        when(service.get("missing")).thenReturn(Optional.empty());

        String body = "{\"name\":\"Updated\"}";

        mockMvc.perform(put("/v1/technologyInterfaces/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/v1/technologyInterfaces/ti-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")));
    }

    @Test
    void patch_returns404_whenMissing_and200_whenExists() throws Exception {
        when(service.get("ti-3")).thenReturn(Optional.of(TechnologyInterfaceDto.builder().name("Old").build()));
        when(service.patch(eq("ti-3"), any(TechnologyInterfaceDto.class)))
                .thenReturn(TechnologyInterfaceDto.builder().name("Patched").build());

        String body = "{\"name\":\"Patched\"}";

        mockMvc.perform(patch("/v1/technologyInterfaces/ti-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Patched")));
    }

    @Test
    void delete_returns404_whenMissing_and204_whenExists() throws Exception {
        when(service.get("ti-9")).thenReturn(Optional.of(TechnologyInterfaceDto.builder().name("X").build()));
        Mockito.doNothing().when(service).delete("ti-9");

        mockMvc.perform(delete("/v1/technologyInterfaces/missing"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/v1/technologyInterfaces/ti-9"))
                .andExpect(status().isNoContent());
    }
}
