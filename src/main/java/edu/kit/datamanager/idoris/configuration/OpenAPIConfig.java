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

package edu.kit.datamanager.idoris.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "IDORIS API",
                version = "0.2.0",
                description = "API for the Integrated Data Type and Operations Registry with Inheritance System (IDORIS). This API provides endpoints for managing data types, operations, and their relationships within IDORIS.",
                contact = @io.swagger.v3.oas.annotations.info.Contact(
                        name = "KIT Data Manager Team",
                        email = "webmaster@datamanager.kit.edu",
                        url = "https://kit-data-manager.github.io/webpage"
                )
        )
)
public class OpenAPIConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("IDORIS API")
                        .version("0.2.0")
                        .description("API documentation for IDORIS system")
                        .contact(new Contact()
                                .name("KIT Data Manager Team")
                                .email("webmaster@datamanager.kit.edu")
                                .url("https://kit-data-manager.github.io/webpage")))
                .externalDocs(new ExternalDocumentation()
                        .description("IDORIS GitHub Repository")
                        .url("https://github.com/maximiliani/idoris"));
    }
}