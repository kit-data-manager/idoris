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
package edu.kit.datamanager.idoris.pids.web.v1;

import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import edu.kit.datamanager.idoris.pids.services.PersistentIdentifierService;
import edu.kit.datamanager.idoris.pids.web.api.IPidApi;
import edu.kit.datamanager.idoris.pids.web.hateoas.PersistentIdentifierModelAssembler;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller for PID-related operations.
 * This controller handles:
 * - /pid - Lists all PIDs exposed by IDORIS
 * - /pid/{pidValue} - Redirects to the entity the PID refers to
 * - /pid/tombstone/{pidValue} - Handles tombstone pages for deleted entities
 */
@RestController
@RequestMapping("/v1/pid")
@Slf4j
@Observed(contextualName = "pidController")
@Tag(name = "Persistent Identifier", description = "API for accessing Persistent Identifiers (PIDs)")
public class PidController implements IPidApi {

    private final PersistentIdentifierService pidService;
    private final PersistentIdentifierModelAssembler pidModelAssembler;

    /**
     * Creates a new PidController with the given dependencies.
     *
     * @param pidService        The PersistentIdentifierService
     * @param pidModelAssembler The PersistentIdentifierModelAssembler
     */
    @Autowired
    public PidController(PersistentIdentifierService pidService, PersistentIdentifierModelAssembler pidModelAssembler) {
        this.pidService = pidService;
        this.pidModelAssembler = pidModelAssembler;
    }

    /**
     * Lists all PIDs exposed by IDORIS.
     *
     * @return A collection of all PersistentIdentifiers
     */
    @Override
    @GetMapping
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "pidController.getAllPersistentIdentifiers", description = "Time taken to get all persistent identifiers", histogram = true)
    @Counted(value = "pidController.getAllPersistentIdentifiers.count", description = "Number of get all persistent identifiers requests")
    public ResponseEntity<CollectionModel<EntityModel<PersistentIdentifier>>> getAllPersistentIdentifiers() {
        log.debug("Getting all PersistentIdentifiers");
        List<EntityModel<PersistentIdentifier>> pids = pidService.getAllPersistentIdentifiers().stream()
                .map(pidModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PersistentIdentifier>> collectionModel = CollectionModel.of(
                pids,
                linkTo(methodOn(PidController.class).getAllPersistentIdentifiers()).withSelfRel()
        );

        log.info("Retrieved {} persistent identifiers", pids.size());
        return ResponseEntity.ok(collectionModel);
    }

    /**
     * Redirects to the appropriate entity based on the PID value.
     * If the PID is a tombstone, redirects to the tombstone page.
     * Otherwise, redirects to the entity's page.
     *
     * @param pidValue The PID value to redirect to
     * @return A ResponseEntity with a redirect status or not found if no entity is found
     */
    @Override
    @GetMapping("/{pidValue}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "pidController.redirectToEntity", description = "Time taken to redirect to entity", histogram = true)
    @Counted(value = "pidController.redirectToEntity.count", description = "Number of redirect to entity requests")
    public ResponseEntity<Void> redirectToEntity(@SpanAttribute("pid.value") @PathVariable("pidValue") String pidValue) {
        log.debug("Redirecting PID: {}", pidValue);

        // Get the PersistentIdentifier for the given PID
        Optional<PersistentIdentifier> optionalPid = pidService.getPersistentIdentifier(pidValue);
        if (optionalPid.isEmpty()) {
            log.warn("No PersistentIdentifier found for PID: {}", pidValue);
            return ResponseEntity.notFound().build();
        }

        PersistentIdentifier pid = optionalPid.get();

        // If the PID is a tombstone, redirect to the tombstone page
        if (pid.isTombstone()) {
            log.debug("PID is a tombstone, redirecting to tombstone page: {}", pidValue);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/tombstone/" + pidValue))
                    .build();
        }

        // If the PID has an entity, redirect to the entity's page
        if (pid.getEntity() != null) {
            String entityType = pid.getEntityType().toLowerCase() + "s";
            String entityId = pid.getEntityInternalId();
            log.debug("Redirecting to entity: {}/{}", entityType, entityId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/" + entityType + "/" + entityId))
                    .build();
        }

        // If the PID has no entity and is not a tombstone, return not found
        log.warn("PID has no entity and is not a tombstone: {}", pidValue);
        return ResponseEntity.notFound().build();
    }

    /**
     * Handles requests to the tombstone page.
     * Returns a 410 Gone status with information about the deleted entity.
     *
     * @param pidValue The PID value of the tombstone
     * @return A ResponseEntity with a 410 Gone status and information about the deleted entity
     */
    @Override
    @GetMapping("/tombstone/{pidValue}")
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "pidController.handleTombstone", description = "Time taken to handle tombstone request", histogram = true)
    @Counted(value = "pidController.handleTombstone.count", description = "Number of tombstone requests")
    public ResponseEntity<String> handleTombstone(@SpanAttribute("pid.value") @PathVariable("pidValue") String pidValue) {
        log.debug("Handling tombstone request for PID: {}", pidValue);

        // Get the PersistentIdentifier for the given PID
        Optional<PersistentIdentifier> optionalPid = pidService.getPersistentIdentifier(pidValue);
        if (optionalPid.isEmpty()) {
            log.warn("No PersistentIdentifier found for PID: {}", pidValue);
            return ResponseEntity.notFound().build();
        }

        PersistentIdentifier pid = optionalPid.get();

        // If the PID is not a tombstone, redirect to the entity's page
        if (!pid.isTombstone()) {
            log.debug("PID is not a tombstone, redirecting to entity page: {}", pidValue);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/pid/" + pidValue))
                    .build();
        }

        // Return a 410 Gone status with information about the deleted entity
        String message = String.format("The entity with PID %s has been deleted at %s. Entity type: %s",
                pidValue, pid.getDeletedAt(), pid.getEntityType());
        log.debug("Returning tombstone message: {}", message);
        log.info("Served tombstone for PID: {}", pidValue);
        return ResponseEntity.status(HttpStatus.GONE)
                .body(message);
    }
}