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

package edu.kit.datamanager.idoris.pids.services;

import edu.kit.datamanager.idoris.configuration.TypedPIDMakerConfig;
import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import edu.kit.datamanager.idoris.pids.client.TypedPIDMakerClient;
import edu.kit.datamanager.idoris.pids.client.model.PIDRecord;
import edu.kit.datamanager.idoris.pids.client.model.PIDRecordEntry;
import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import edu.kit.datamanager.idoris.pids.repositories.PersistentIdentifierRepository;
import edu.kit.datamanager.idoris.pids.utils.PIDRecordMapper;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for PersistentIdentifier entities.
 * This class provides methods for creating, updating, and retrieving PersistentIdentifier entities.
 */
@Service
@Slf4j
@Observed
public class PersistentIdentifierService {

    private final PersistentIdentifierRepository repository;
    private final TypedPIDMakerClient client;
    private final TypedPIDMakerConfig config;
    private final PIDRecordMapper mapper;

    /**
     * Creates a new PersistentIdentifierService with the given dependencies.
     *
     * @param repository The repository for PersistentIdentifier entities
     * @param client     The client for the Typed PID Maker service
     * @param config     The configuration for the Typed PID Maker service
     * @param mapper     The mapper for converting between PersistentIdentifier and PIDRecord
     */
    @Autowired
    public PersistentIdentifierService(PersistentIdentifierRepository repository,
                                       TypedPIDMakerClient client,
                                       TypedPIDMakerConfig config,
                                       PIDRecordMapper mapper) {
        this.repository = repository;
        this.client = client;
        this.config = config;
        this.mapper = mapper;
    }

    /**
     * Creates a new PersistentIdentifier for the given entity.
     * This method creates a new PID record in the Typed PID Maker service and stores a corresponding
     * PersistentIdentifier entity in the local database.
     *
     * @param entity The entity to create a PID for
     * @return The created PersistentIdentifier
     */
    @Transactional
    @WithSpan
    public PersistentIdentifier createPersistentIdentifier(AdministrativeMetadata entity) {
        log.debug("Creating PersistentIdentifier for entity: {}", entity);

        // Check if a PID already exists for this entity
        Optional<PersistentIdentifier> existingPid = repository.findByEntityInternalId(entity.getInternalId());
        if (existingPid.isPresent()) {
            log.debug("PersistentIdentifier already exists for entity: {}", entity);
            return existingPid.get();
        }

        // Create a temporary PID entity to use with the mapper
        PersistentIdentifier tempPid = PersistentIdentifier.builder()
                .pid(null)
                .entityType(entity.getClass().getSimpleName())
                .entityInternalId(entity.getInternalId())
                .entity(entity)
                .tombstone(false)
                .build();

        // Use the mapper to create a PID record with administrative metadata
        PIDRecord record = mapper.toPIDRecord(tempPid);

        // Create the PID record in the Typed PID Maker service
        ResponseEntity<PIDRecord> createdResponse = client.createPIDRecord(record);

        if (!createdResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to create PID record in Typed PID Maker service: {}", createdResponse.getStatusCode());
            throw new RuntimeException("Failed to create PID record in Typed PID Maker service");
        }
        String etag = createdResponse.getHeaders().getETag();
        PIDRecord createdRecord = createdResponse.getBody();

        log.debug("Created first PID record: {}", createdRecord);

        // Set the PID in the temporary PersistentIdentifier entity
        tempPid.setPid(createdRecord.pid());

        // Save the PersistentIdentifier entity
        PersistentIdentifier savedPid = repository.save(tempPid);

        // Update the entity with the saved PersistentIdentifier
        List<PIDRecordEntry> entries = createdRecord.record().stream()
                .map(entry -> {
                    if (Objects.equals(entry.key(), "21.T11148/b8457812905b83046284")) {
                        // Update the DO location to point to the saved PID
                        String doLocation = String.format("%s/pid/%s", config.getBaseUrl(), createdRecord.pid());
                        return new PIDRecordEntry(entry.key(), doLocation);
                    }
                    return entry;
                })
                .toList();
        PIDRecord updatedRecord = new PIDRecord(createdRecord.pid(), entries);
        // Update the PID record in the Typed PID Maker service with the saved PID
        log.debug("Updating PID record with saved PID: {}", updatedRecord);

        ResponseEntity<PIDRecord> updatedResponse = client.updatePIDRecord(savedPid.getPid(), updatedRecord, etag);

        if (!updatedResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to update PID record in Typed PID Maker service: {}", updatedResponse.getStatusCode());
            throw new RuntimeException("Failed to update PID record in Typed PID Maker service");
        }

        log.info("Created PersistentIdentifier: {} with record", savedPid);
        return savedPid;
    }

    /**
     * Marks the PersistentIdentifier for the given entity as a tombstone.
     * This method updates the PID record in the Typed PID Maker service to indicate that the entity has been deleted.
     *
     * @param entity The entity that has been deleted
     * @return The updated PersistentIdentifier, or empty if no PID exists for the entity
     */
    @Transactional
    @WithSpan
    public Optional<PersistentIdentifier> markAsTombstone(AdministrativeMetadata entity) {
        log.debug("Marking PersistentIdentifier as tombstone for entity: {}", entity);

        // Find the PID for the entity
        Optional<PersistentIdentifier> optionalPid = repository.findByEntityInternalId(entity.getInternalId());
        if (optionalPid.isEmpty()) {
            log.warn("No PersistentIdentifier found for entity: {}", entity);
            return Optional.empty();
        }

        PersistentIdentifier pid = optionalPid.get();

        // Mark the PID as a tombstone
        pid.markAsTombstone(Instant.now());

        // Save the updated PID
        PersistentIdentifier savedPid = repository.save(pid);

        // Update the PID record in the Typed PID Maker service
        updatePIDRecord(savedPid);

        log.info("Marked PersistentIdentifier as tombstone: {}", savedPid);
        return Optional.of(savedPid);
    }

    /**
     * Updates the PID record for the given PersistentIdentifier.
     * This method updates the PID record in the Typed PID Maker service with the latest metadata from the entity.
     *
     * @param pid The PersistentIdentifier to update the PID record for
     * @return The updated PersistentIdentifier
     */
    @Transactional
    @WithSpan
    public PersistentIdentifier updatePIDRecord(PersistentIdentifier pid) {
        log.debug("Updating PID record for PersistentIdentifier: {}", pid);

        // Create a PID record with metadata from the entity
        PIDRecord record = mapper.toPIDRecord(pid);

        // If the PID is null, it means the PID has not been created yet
        ResponseEntity<PIDRecord> getResponse = client.getPIDRecord(pid.getPid());

        if (!getResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to retrieve PID record from Typed PID Maker service: {}", getResponse.getStatusCode());
            throw new RuntimeException("Failed to retrieve PID record from Typed PID Maker service");
        }

        // Ensure the record is not null and has the correct PID
        if (record == null || !record.pid().equals(pid.getPid()) || !record.pid().equals(Objects.requireNonNull(getResponse.getBody()).pid())) {
            log.error("PID record is null or PID does not match: expected {}, got {}", pid.getPid(), record != null ? record.pid() : "null");
            throw new RuntimeException("PID record is null or PID does not match");
        }

        // Update the PID record in the Typed PID Maker service
        ResponseEntity<PIDRecord> updatedResponse = client.updatePIDRecord(record.pid(), record, getResponse.getHeaders().getETag());

        if (!updatedResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to update PID record in Typed PID Maker service: {}", updatedResponse.getStatusCode());
            throw new RuntimeException("Failed to update PID record in Typed PID Maker service");
        }

        log.info("Updated PID record for PersistentIdentifier: {}", pid);
        return pid;
    }

    /**
     * Gets the PersistentIdentifier for the given entity.
     *
     * @param entity The entity to get the PID for
     * @return An Optional containing the PersistentIdentifier if found, or empty if not found
     */
    public Optional<PersistentIdentifier> getPersistentIdentifier(AdministrativeMetadata entity) {
        log.debug("Getting PersistentIdentifier for entity: {}", entity);
        return repository.findByEntityInternalId(entity.getInternalId());
    }

    /**
     * Gets the PersistentIdentifier with the given PID.
     *
     * @param pid The PID to get the PersistentIdentifier for
     * @return An Optional containing the PersistentIdentifier if found, or empty if not found
     */
    public Optional<PersistentIdentifier> getPersistentIdentifier(String pid) {
        log.debug("Getting PersistentIdentifier with PID: {}", pid);
        return repository.findById(pid);
    }

    /**
     * Gets all PersistentIdentifiers.
     *
     * @return A list of all PersistentIdentifiers
     */
    public List<PersistentIdentifier> getAllPersistentIdentifiers() {
        log.debug("Getting all PersistentIdentifiers");
        return repository.findAll();
    }

    /**
     * Gets all PersistentIdentifiers for entities of the given type.
     *
     * @param entityType The type of entity to get PIDs for
     * @return A list of PersistentIdentifiers for entities of the given type
     */
    public List<PersistentIdentifier> getPersistentIdentifiersByEntityType(String entityType) {
        log.debug("Getting PersistentIdentifiers for entity type: {}", entityType);
        return repository.findByEntityType(entityType);
    }

    /**
     * Gets all PersistentIdentifiers that are tombstones (entity has been deleted).
     *
     * @return A list of PersistentIdentifiers that are tombstones
     */
    public List<PersistentIdentifier> getTombstones() {
        log.debug("Getting tombstone PersistentIdentifiers");
        return repository.findByTombstoneTrue();
    }
}
