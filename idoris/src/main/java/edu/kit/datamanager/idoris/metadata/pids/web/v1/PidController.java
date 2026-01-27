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
package edu.kit.datamanager.idoris.metadata.pids.web.v1;

import edu.kit.datamanager.idoris.metadata.pids.domain.PIDNode;
import edu.kit.datamanager.idoris.metadata.pids.services.PersistentIdentifierService;
import edu.kit.datamanager.idoris.metadata.pids.web.api.IPidApi;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller for PID-related operations.
 * This controller handles:
 * - /pid - Lists all PIDs exposed by IDORIS
 * - /pid/{pidValue} - Redirects to the entity the PID refers to
 * - /pid/tombstone/{pidValue} - Handles tombstone pages for deleted entities
 */
@RestController
@Slf4j
@Observed(contextualName = "pidController")
//@Tag(name = "Persistent Identifier", description = "API for accessing Persistent Identifiers (PIDs)")
public class PidController implements IPidApi {

    private final PersistentIdentifierService pidService;

    /**
     * Creates a new PidController with the given dependencies.
     *
     * @param pidService The PersistentIdentifierService
     */
    @Autowired
    public PidController(PersistentIdentifierService pidService) {
        this.pidService = pidService;
    }

    /**
     * Lists all PIDs exposed by IDORIS.
     *
     * @return A collection of all PersistentIdentifiers
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "pidController.getAllPersistentIdentifiers", description = "Time taken to get all persistent identifiers", histogram = true)
    @Counted(value = "pidController.getAllPersistentIdentifiers.count", description = "Number of get all persistent identifiers requests")
    public ResponseEntity<CollectionModel<EntityModel<PIDNode>>> getAllPersistentIdentifiers() {
        log.debug("Getting all PersistentIdentifiers");
        CollectionModel<EntityModel<PIDNode>> pids = CollectionModel.of(pidService.getAllPersistentIdentifiers().stream().map(EntityModel::of).toList());
        log.info("Retrieved {} persistent identifiers", pids.getContent().size());
        return ResponseEntity.ok(pids);
    }

    /**
     * Redirects to the appropriate entity based on the PID value.
     * If the PID is a tombstone, redirects to the tombstone page.
     * Otherwise, redirects to the entity's page.
     *
     * @param pid The PID value to redirect to
     * @return A ResponseEntity with a redirect status or not found if no entity is found
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "pidController.redirectToEntity", description = "Time taken to redirect to entity", histogram = true)
    @Counted(value = "pidController.redirectToEntity.count", description = "Number of redirect to entity requests")
    public ResponseEntity<Void> redirectToEntity(@SpanAttribute("pid.value") String pid, WebRequest request) {
        String pidValue = getContentPathFromRequest("pid", request, pid);

        log.debug("Redirecting to pid {}", pidValue);

        // Get the PIDNode for the given PID
        Optional<PIDNode> optionalPid = pidService.getPersistentIdentifier(pidValue);
        if (optionalPid.isEmpty()) {
            log.warn("No PIDNode found for PID: {}", pidValue);
            return ResponseEntity.notFound().build();
        }

        PIDNode pidNode = optionalPid.get();

        // If the PID is a tombstone, redirect to the tombstone page
        if (pidNode.isTombstone()) {
            log.debug("PID is a tombstone, redirecting to tombstone page: {}", pidValue);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/tombstone/" + pidValue))
                    .build();
        }

        // If the PID has an entity, redirect to the entity's page
        if (pidNode.getEntity() != null) {
            String entityType = pidNode.getEntityType().toLowerCase(Locale.ROOT) + "s";
            String entityId = pidNode.getEntityInternalId();
            log.debug("Redirecting to entity: {}/{}", entityType, entityId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/api/v1/" + entityType + "/" + entityId))
                    .build();
        }

        // If the PID has no entity and is not a tombstone, return not found
        log.warn("PID has no entity and is not a tombstone: {}", pidValue);
        return ResponseEntity.notFound().build();
    }

    /**
     * Extracts and returns the content path from the incoming web request, based on the specified last path element.
     *
     * @param lastPathElement the last path element used to determine the content path
     * @param request         the incoming web request containing the requested URI and attributes
     * @return the extracted content path from the request
     * @throws IllegalArgumentException if the requested URI cannot be obtained from the web request
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    private String getContentPathFromRequest(String lastPathElement, WebRequest request, String alternativeInput) {
        String requestedUri = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST);
        String result = "";
        if (requestedUri != null) {
            log.debug("Requested URI: {}", requestedUri);
            result = requestedUri
                    .substring(requestedUri.indexOf(lastPathElement + "/") + (lastPathElement + "/").length())
                    .replace("**", "");
        }

        if (result.isBlank()) {
            log.debug("Requested URI is blank. Using alternative input: {}", alternativeInput);
            result = alternativeInput;
        }

        result = result.replace("%2F", "/");
        result = result.replace("**", "");

        return result;
    }

    /**
     * Handles requests to the tombstone page.
     * Returns a 410 Gone status with information about the deleted entity.
     *
     * @param pid The PID value of the tombstone
     * @return A ResponseEntity with a 410 Gone status and information about the deleted entity
     */
    @Override
    @WithSpan(kind = SpanKind.SERVER)
    @Timed(value = "pidController.handleTombstone", description = "Time taken to handle tombstone request", histogram = true)
    @Counted(value = "pidController.handleTombstone.count", description = "Number of tombstone requests")
    public ResponseEntity<String> handleTombstone(@SpanAttribute("pid.value") String pid, WebRequest request) {
        String pidValue = getContentPathFromRequest("pid", request, pid);

        log.debug("Handling tombstone request for PID: {}", pidValue);

        // Get the PIDNode for the given PID
        PIDNode pidNode = pidService.getPersistentIdentifier(pidValue).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PID not found: " + pidValue));

        // If the PID is not a tombstone, redirect to the entity's page
        if (!pidNode.isTombstone()) {
            log.debug("PID is not a tombstone, redirecting to entity page: {}", pidValue);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/pid/" + pidValue))
                    .build();
        }

        // Return a 410 Gone status with information about the deleted entity
        String message = String.format("The entity with PID %s has been deleted at %s. Entity type: %s",
                pidNode.getPid(), pidNode.getDeletedAt(), pidNode.getEntityType());
        log.debug("Returning tombstone message: {}", message);
        log.info("Served tombstone for PID: {}", pidValue);
        return ResponseEntity.status(HttpStatus.GONE)
                .body(message);
    }
}