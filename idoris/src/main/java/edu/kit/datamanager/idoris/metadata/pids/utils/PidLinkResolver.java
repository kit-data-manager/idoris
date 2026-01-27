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
package edu.kit.datamanager.idoris.metadata.pids.utils;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.metadata.pids.domain.PIDNode;
import edu.kit.datamanager.idoris.metadata.pids.services.PersistentIdentifierService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility component to resolve a PID link ("/pid/{pid}") for a given entity.
 * Returns null when no PID is available yet.
 */
@Component
public class PidLinkResolver {
    private final PersistentIdentifierService pidService;

    public PidLinkResolver(PersistentIdentifierService pidService) {
        this.pidService = pidService;
    }

    /**
     * Resolves a relative link to the PID endpoint for the given entity.
     *
     * @param entity AdministrativeMetadata entity
     * @return Link string like "/pid/{pid}" or null if no PID exists yet
     */
    public String linkFor(AdministrativeMetadata entity) {
        if (entity == null) return null;
        Optional<PIDNode> opt = pidService.getPersistentIdentifier(entity);
        return opt.map(pid -> "/pid/" + pid.getPid()).orElse(null);
    }
}
