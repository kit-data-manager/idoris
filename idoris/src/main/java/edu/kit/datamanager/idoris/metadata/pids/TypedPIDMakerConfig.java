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

package edu.kit.datamanager.idoris.metadata.pids;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("idoris.typed-pid-maker")
@Getter
@Setter
@Validated
public class TypedPIDMakerConfig {
    /**
     * When true, include AdministrativeMetadata fields in created PID records.
     * <br>
     * Default: true
     */
    private Boolean meaningfulPIDRecords = true;
    /**
     * When true, update existing PID records when a PID is already present on the entity.
     * <br>
     * Default: true
     */
    private Boolean updatePIDRecords = true;
    /**
     * The timeout in milliseconds for requests to the Typed PID Maker logic.
     * <br>
     * Default: 10 s
     */
    private Integer timeoutSeconds = 10;

    /**
     * The maximum number of total tries to perform an action with the Typed PID Maker.
     * CAUTION: This is not limited to idempotent operations.
     * <br>
     * Default: 1 (no retry)
     */
    private Integer maxTotalTries = 1;

    /**
     * The base URL for the Typed PID Maker logic.
     * This is required when the logic is enabled.
     */
    private String baseUrl;
}
