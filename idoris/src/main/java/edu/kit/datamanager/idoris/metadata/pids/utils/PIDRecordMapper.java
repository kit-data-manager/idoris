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

import edu.kit.datamanager.idoris.core.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.domain.valueObjects.PID;
import edu.kit.datamanager.idoris.metadata.pids.client.model.PIDRecord;
import edu.kit.datamanager.idoris.metadata.pids.client.model.PIDRecordEntry;
import edu.kit.datamanager.idoris.metadata.pids.domain.PIDNode;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for mapping between PIDNode entities and PIDRecord objects.
 * This class extracts the mapping logic to reduce redundancies.
 */
@Component
@Slf4j
@Observed(contextualName = "pidRecordMapper")
public class PIDRecordMapper {

    private final ApplicationProperties applicationProperties;

    /**
     * Creates a new PIDRecordMapper with the given dependencies.
     *
     * @param applicationProperties The application properties
     */
    @Autowired
    public PIDRecordMapper(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Converts a PIDNode to a PIDRecord.
     * This method creates a PIDRecord with metadata from the PIDNode and its associated entity.
     *
     * @param pid The PIDNode to convert
     * @return The converted PIDRecord
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    public PIDRecord toPIDRecord(@SpanAttribute PIDNode pid) {
        log.debug("Converting PIDNode to PIDRecord: {}", pid.getPid());
        List<PIDRecordEntry> recordEntries = new ArrayList<>();
        AdministrativeMetadata entity = pid.getEntity();

        // Helmholtz Kernel Information Profile
        recordEntries.add(new PIDRecordEntry(new PID("21.T11148/076759916209e5d62bd5"), "21.T11148/b9b76f887845e32d29f7"));

        // Always add a pointer to the entity
        String baseUrl = getBaseUrl();
        String doLocation;

        if (pid.isTombstone()) {
            // For tombstones, use a special URL that indicates the entity has been deleted
            doLocation = String.format("%s/tombstone/%s", baseUrl, pid.getPid());
            recordEntries.add(new PIDRecordEntry(new PID("21.T11148/d1ec8ccbfa6de41da894"), "TOMBSTONE")); //TODO: Add more tombstone information
//            recordEntries.add(new PIDRecordEntry("deletedAt", pid.getDeletedAt().toString()));
        } else {
            // For active entities, use a URL that points to the entity
            doLocation = String.format("%s/pid/%s", baseUrl, pid.getPid());
        }

        log.debug("Using DO location: {}", doLocation);
        recordEntries.add(new PIDRecordEntry(new PID("21.T11148/b8457812905b83046284"), doLocation));

        // Add entity type information as digitalObjectType (currently hardcoded to "application/json")
        recordEntries.add(new PIDRecordEntry(new PID("21.T11148/1c699a5d1b4ad3ba4956"), "21.T11148/ca9fd0b2414177b79ac2"));

        // Add CC0 license information
        recordEntries.add(new PIDRecordEntry(new PID("21.T11148/2f314c8fe5fb6a0063a8"), "https://spdx.org/license/CC0-1.0/"));

        // Add nested SHA-256 hash for the doLocation
        String sha256Hash = "";
        try {
            // Calculate the SHA-256 hash of the doLocation as hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(doLocation.getBytes(StandardCharsets.UTF_8))) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            sha256Hash = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        recordEntries.add(new PIDRecordEntry(new PID("21.T11148/82e2503c49209e987740"), String.format("{\"sha256sum\": \"sha256 %s\"}", sha256Hash)));

        // Add basic metadata
        if (entity.getName() != null) {
            recordEntries.add(new PIDRecordEntry(new PID("21.T11148/6ae999552a0d2dca14d6"), entity.getName().toString()));
        }

        // Add timestamps
        Instant createdAt = entity.getCreatedAt();
        if (createdAt != null) {
            recordEntries.add(new PIDRecordEntry(new PID("21.T11148/aafd5fb4c7222e2d950a"), createdAt.toString()));
        }

        Instant lastModifiedAt = entity.getLastModifiedAt();
        if (lastModifiedAt != null) {
            recordEntries.add(new PIDRecordEntry(new PID("21.T11148/397d831aa3a9d18eb52c"), lastModifiedAt.toString()));
        }

        // Add version information
        Long version = entity.getVersion();
        if (version != null) {
            recordEntries.add(new PIDRecordEntry(new PID("21.T11148/c692273deb2772da307f"), "v" + version));
        }

        // Add contributors
        if (entity.getContributors() != null && !entity.getContributors().isEmpty()) {
            entity.getContributors().forEach(contributor -> {
                URL orcidURL = contributor.getOrcid().get();
                if (orcidURL != null && orcidURL.getHost() != null && (orcidURL.getHost().equals("orcid.org") || orcidURL.getHost().endsWith(".orcid.org"))) {
                    recordEntries.add(new PIDRecordEntry(new PID("21.T11148/1a73af9e7ae00182733b"), orcidURL.toExternalForm()));
                    log.debug("Added ORCiD URL: {}", orcidURL);
                } else {
                    log.info("This contributor does not have a URL, skipping: {}", contributor);
                }
            });
        }

        // Add references
        if (entity.getReferences() != null && !entity.getReferences().isEmpty()) {
            entity.getReferences().forEach(reference -> {
                PID relationPID = reference.relationType();
                PID targetPID = reference.targetPID();
                if (relationPID != null && targetPID != null) {
                    recordEntries.add(new PIDRecordEntry(relationPID, targetPID.toString()));
                    log.debug("Added reference: {} -> {}", relationPID, targetPID);
                } else {
                    log.warn("Invalid reference found, skipping: {}", reference);
                }
            });
        }

        // Create the PID record
        PIDRecord pidRecord = new PIDRecord(pid.getPid(), recordEntries);
        log.debug("Created PIDRecord: {}", pidRecord);
        return pidRecord;
    }

    /**
     * Gets the base URL from the application properties.
     * This method ensures that the base URL does not end with a slash.
     *
     * @return The base URL
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    private String getBaseUrl() {
        log.debug("Getting base URL from application properties");
        String baseUrl = applicationProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            log.error("Base URL is not configured or is empty.");
            throw new IllegalArgumentException("Base URL must be configured.");
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        log.debug("Using base URL: {}", baseUrl);
        return baseUrl;
    }
}