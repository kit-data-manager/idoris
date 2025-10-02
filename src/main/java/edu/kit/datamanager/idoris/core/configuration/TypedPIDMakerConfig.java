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

package edu.kit.datamanager.idoris.core.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties("idoris.typed-pid-maker")
@Validated
@Getter
@Setter
public class TypedPIDMakerConfig {
    /**
     * The base URL for the Typed PID Maker logic.
     * This is required when the logic is enabled.
     */
    @NotNull(message = "Base URL is required when enabled is true")
    private String baseUrl;

    /**
     * When true, include AdministrativeMetadata fields in created PID records.
     */
    private boolean meaningfulPIDRecords = false;

    /**
     * When true, update existing PID records when a PID is already present on the entity.
     */
    private boolean updatePIDRecords = false;

    /**
     * The timeout in milliseconds for requests to the Typed PID Maker logic.
     */
    private int timeout = 5000;

    /**
     * Backward-compatible boolean getter expected by tests.
     */
    public boolean isMeaningfulPIDRecords() {
        return meaningfulPIDRecords;
    }
}
