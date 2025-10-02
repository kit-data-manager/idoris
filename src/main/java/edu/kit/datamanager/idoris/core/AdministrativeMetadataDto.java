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
package edu.kit.datamanager.idoris.core;

import java.time.Instant;

/**
 * Marker interface for Response DTOs that expose administrative metadata maintained by the server.
 * Records representing response payloads should implement this interface to provide a common
 * contract across modules without leaking entity classes.
 */
public interface AdministrativeMetadataDto {
    String internalId();

    Long version();

    Instant createdAt();

    Instant lastModifiedAt();

    default String createdBy() {
        return null;
    }

    default String lastModifiedBy() {
        return null;
    }
}