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
package edu.kit.datamanager.idoris.configuration;

import edu.kit.datamanager.idoris.rules.logic.OutputMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.INFO;

/**
 * This class is used to configure the application.
 * It reads the values from the application.properties file.
 *
 * @author maximiliani
 */
@Configuration
@Getter
@Validated
public class ApplicationProperties {

    /**
     * The base URL of the IDORIS service, used in e.g., the PID records.
     */
    @Value("${idoris.base-url}")
    @NotNull(message = "Base URL is required")
    private String baseUrl;

    /**
     * The policy to use for validating the input.
     *
     * @see ValidationPolicy
     */
    @Value("${idoris.validation-policy:LAX}")
    @NotNull
    private ValidationPolicy validationPolicy = ValidationPolicy.LAX;

    /**
     * The lowest severity level that is shown to the user.
     *
     * @see OutputMessage.MessageSeverity
     */
    @Value("${idoris.validation-level:INFO}")
    @NotNull
    private OutputMessage.MessageSeverity validationLevel = INFO;

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
