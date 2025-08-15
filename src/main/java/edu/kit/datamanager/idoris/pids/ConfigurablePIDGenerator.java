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

package edu.kit.datamanager.idoris.pids;

import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.neo4j.core.schema.IdGenerator;
import org.springframework.stereotype.Component;

/**
 * A configurable PID generator that delegates to either TypedPIDMakerIDGenerator
 * or LocalUUIDPIDGenerator based on the 'idoris.pid-generation' application property.
 */
@Component
public class ConfigurablePIDGenerator implements IdGenerator<String> {

    private static final Logger log = LoggerFactory.getLogger(ConfigurablePIDGenerator.class);

    private final ApplicationProperties applicationProperties;
    private final ObjectProvider<TypedPIDMakerIDGenerator> typedPidMakerProvider;

    public ConfigurablePIDGenerator(ApplicationProperties applicationProperties,
                                    ObjectProvider<TypedPIDMakerIDGenerator> typedPidMakerProvider) {
        this.applicationProperties = applicationProperties;
        this.typedPidMakerProvider = typedPidMakerProvider;
    }

    /**
     * Generates a PID based on the configured strategy.
     *
     * @param primaryLabel The primary label of the entity.
     * @param entity       The entity for which the PID is generated.
     * @return A generated PID as a String.
     * @throws IllegalArgumentException if the primary label is null or empty, or if the entity is not an instance of AdministrativeMetadata.
     * @throws IllegalStateException    if the configured PID generation strategy is not available.
     */
    @Override
    public String generateId(String primaryLabel, Object entity) {

        // Validate inputs
        if (primaryLabel.isEmpty()) {
            log.error("Primary label is null or empty");
            throw new IllegalArgumentException("Primary label must not be null or empty.");
        }
        if (!(entity instanceof AdministrativeMetadata)) {
            log.error("Entity is null or not an instance of AdministrativeMetadata");
            throw new IllegalArgumentException("Entity must be a non-null instance of AdministrativeMetadata.");
        }

        TypedPIDMakerIDGenerator typedGenerator = typedPidMakerProvider.getIfAvailable();

        if (typedGenerator != null) {
            log.debug("Using TypedPIDMakerIDGenerator for entity labeled '{}'", primaryLabel);
            return typedGenerator.generateId(primaryLabel, entity);
        } else {
            log.error("PID generation strategy is TYPED_PID_MAKER, but TypedPIDMakerIDGenerator bean is not available.");
            throw new IllegalStateException("TypedPIDMakerIDGenerator bean is not available. Check your configuration.");
        }
    }
}
