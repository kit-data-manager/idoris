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

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.events.EntityCreatedEvent;
import edu.kit.datamanager.idoris.core.events.EntityDeletedEvent;
import edu.kit.datamanager.idoris.core.events.EntityUpdatedEvent;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.pids.domain.PIDNode;
import edu.kit.datamanager.idoris.pids.services.PersistentIdentifierService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Event listener that generates IDs for newly created entities.
 * This listener subscribes to EntityCreatedEvent and uses the PersistentIdentifierService
 * to create PIDNode entities for newly created AdministrativeMetadata entities.
 */
@Component
@Slf4j
@Observed(contextualName = "metadataEventListener")
public class MetadataEventListener {
    private final PersistentIdentifierService pidService;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new MetadataEventListener with the given dependencies.
     *
     * @param pidService     the PersistentIdentifierService
     * @param eventPublisher the event publisher logic
     */
    public MetadataEventListener(PersistentIdentifierService pidService, EventPublisherService eventPublisher) {
        this.pidService = pidService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles EntityCreatedEvent by creating a PIDNode for the entity.
     * This method is executed in a new transaction automatically by Spring Modulith to ensure that the ID creation is isolated from the transaction that created the entity.
     *
     * @param event the entity created event
     */
    @ApplicationModuleListener
    @WithSpan(kind = SpanKind.CONSUMER)
    @Timed(value = "metadataEventListener.handleEntityCreatedEvent", description = "Time taken to handle entity created event", histogram = true)
    @Counted(value = "metadataEventListener.handleEntityCreatedEvent.count", description = "Number of entity created events handled")
    public void handleEntityCreatedEvent(EntityCreatedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        log.debug("Handling EntityCreatedEvent for entity: {}", entity);

        // Check if a PIDNode already exists for this entity
        Optional<PIDNode> existingPid = pidService.getPersistentIdentifier(entity);

        if (existingPid.isPresent()) {
            log.debug("Entity already has a PIDNode: {}", existingPid.get().getPid());
            return;
        }

        // Create a new PIDNode for the entity
        log.info("Creating PIDNode for entity: {}", entity);
        PIDNode pid = pidService.createPersistentIdentifier(entity);
        log.info("Created PIDNode with ID: {} for entity: {}", pid.getPid(), entity);

        // Publish an ID generated event
        eventPublisher.publishIDGenerated(entity, pid.getPid().toString());
    }

    /**
     * Handles EntityUpdatedEvent by updating the PIDNode for the entity.
     * This method is executed in a new transaction automatically by Spring Modulith to ensure that the ID update is isolated from the transaction that updated the entity.
     *
     * @param event the entity updated event
     */
    @ApplicationModuleListener
    @WithSpan(kind = SpanKind.CONSUMER)
    @Timed(value = "metadataEventListener.handleEntityUpdatedEvent", description = "Time taken to handle entity updated event", histogram = true)
    @Counted(value = "metadataEventListener.handleEntityUpdatedEvent.count", description = "Number of entity updated events handled")
    public void handleEntityUpdatedEvent(EntityUpdatedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        log.debug("Handling EntityUpdatedEvent for entity: {}", entity);

        // Check if a PIDNode exists for this entity
        Optional<PIDNode> existingPid = pidService.getPersistentIdentifier(entity);

        if (existingPid.isPresent()) {
            PIDNode pid = existingPid.get();
            log.info("Updating PIDNode for entity: {}", entity);
            pidService.updatePIDRecord(pid);
            log.info("Updated PIDNode with ID: {} for entity: {}", pid.getPid(), entity);
        } else {
            log.warn("No PIDNode found for entity, cannot update: {}", entity);
        }
    }


    /**
     * Handles EntityDeletedEvent by marking the PIDNode as a tombstone.
     * This method is executed in a new transaction automatically by Spring Modulith to ensure that the tombstone creation is isolated from the transaction that deleted the entity.
     *
     * @param event the entity deleted event
     */
    @ApplicationModuleListener
    @WithSpan(kind = SpanKind.CONSUMER)
    @Timed(value = "metadataEventListener.handleEntityDeletedEvent", description = "Time taken to handle entity deleted event", histogram = true)
    @Counted(value = "metadataEventListener.handleEntityDeletedEvent.count", description = "Number of entity deleted events handled")
    public void handleEntityDeletedEvent(EntityDeletedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        log.debug("Handling EntityDeletedEvent for entity: {}", entity);

        // Mark the PIDNode as a tombstone
        Optional<PIDNode> optionalPid = pidService.markAsTombstone(entity);

        if (optionalPid.isPresent()) {
            PIDNode pid = optionalPid.get();
            log.info("Created tombstone for entity with ID: {}", pid.getPid());
        } else {
            log.warn("No PIDNode found for entity, cannot create tombstone: {}", entity);
        }
    }
}
