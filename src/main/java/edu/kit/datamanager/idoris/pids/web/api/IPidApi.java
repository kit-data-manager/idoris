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
package edu.kit.datamanager.idoris.pids.web.api;

import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * API interface for PID-related operations.
 * This interface defines the REST API for accessing Persistent Identifiers (PIDs).
 */
@Tag(name = "Persistent Identifier", description = "API for accessing Persistent Identifiers (PIDs)")
public interface IPidApi {

    /**
     * Lists all PIDs exposed by IDORIS.
     *
     * @return A collection of all PersistentIdentifiers
     */
    @GetMapping
    @Operation(
            summary = "Get all Persistent Identifiers",
            description = "Returns a collection of all Persistent Identifiers exposed by IDORIS",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Persistent Identifiers found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = PersistentIdentifier.class)))
            }
    )
    ResponseEntity<CollectionModel<EntityModel<PersistentIdentifier>>> getAllPersistentIdentifiers();

    /**
     * Redirects to the appropriate entity based on the PID value.
     * If the PID is a tombstone, redirects to the tombstone page.
     * Otherwise, redirects to the entity's page.
     *
     * @param pidValue The PID value to redirect to
     * @return A ResponseEntity with a redirect status or not found if no entity is found
     */
    @GetMapping("/{pidValue}")
    @Operation(
            summary = "Redirect to entity by PID",
            description = "Redirects to the appropriate entity based on the PID value. If the PID is a tombstone, redirects to the tombstone page.",
            responses = {
                    @ApiResponse(responseCode = "302", description = "Found - Redirect to entity or tombstone"),
                    @ApiResponse(responseCode = "404", description = "PID not found")
            }
    )
    ResponseEntity<Void> redirectToEntity(
            @Parameter(description = "PID value to redirect to", required = true)
            @PathVariable("pidValue") String pidValue);

    /**
     * Handles requests to the tombstone page.
     * Returns a 410 Gone status with information about the deleted entity.
     *
     * @param pidValue The PID value of the tombstone
     * @return A ResponseEntity with a 410 Gone status and information about the deleted entity
     */
    @GetMapping("/tombstone/{pidValue}")
    @Operation(
            summary = "Get tombstone information",
            description = "Returns tombstone information for a deleted entity",
            responses = {
                    @ApiResponse(responseCode = "410", description = "Gone - Entity has been deleted",
                            content = @Content(mediaType = "text/plain")),
                    @ApiResponse(responseCode = "302", description = "Found - Redirect to entity (if not a tombstone)"),
                    @ApiResponse(responseCode = "404", description = "PID not found")
            }
    )
    ResponseEntity<String> handleTombstone(
            @Parameter(description = "PID value of the tombstone", required = true)
            @PathVariable("pidValue") String pidValue);
}
