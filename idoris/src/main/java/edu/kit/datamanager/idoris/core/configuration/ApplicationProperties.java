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
package edu.kit.datamanager.idoris.core.configuration;

import edu.kit.datamanager.idoris.rules.logic.OutputMessage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.INFO;

/**
 * This class is used to configure the application.
 * It reads the values from the application.properties file.
 *
 * @author maximiliani
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "idoris")
@Configuration
@AllArgsConstructor
@RequiredArgsConstructor
@Validated
public class ApplicationProperties {

    /**
     * The policy to use for validating the input.
     *
     * @see ValidationPolicy
     */
    private ValidationPolicy validationPolicy = ValidationPolicy.LAX;

    /**
     * The lowest severity level that is shown to the user.
     *
     * @see OutputMessage.MessageSeverity
     */
    private OutputMessage.MessageSeverity validationLevel = INFO;

    /**
     * The base URL of the application.
     * This is used to generate links in the responses.
     * It must be set to the public URL of the application.
     * Example: https://idoris.example.com
     */
    @NotNull(message = "Base URL is required")
    private String baseUrl;

//    // Reads Keycloak related settings from application.properties
//    @Bean
//    public KeycloakJwtProperties properties() {
//        return new KeycloakJwtProperties();
//    }

    /**
     * The policy to use for validating the input.
     * <p>
     * The policy can be either STRICT or LAX.
     * Strict means that the input must be exactly as expected. Warnings are treated as errors and therefore fail the validation.
     * Lenient means that the input can be slightly different from the expected input. Warnings are provided to the user but DO NOT fail the validation.
     */
    public enum ValidationPolicy {
        STRICT, LAX
    }
}
