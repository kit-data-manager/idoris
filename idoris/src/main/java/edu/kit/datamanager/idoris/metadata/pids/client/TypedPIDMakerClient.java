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

package edu.kit.datamanager.idoris.metadata.pids.client;

import edu.kit.datamanager.idoris.metadata.pids.client.model.PIDRecord;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

/**
 * Client for the Typed PID Maker logic.
 * This interface defines the operations for interacting with the logic.
 */
@HttpExchange("/api/v1/pit/pid")
@Observed(contextualName = "pidMakerClient")
public interface TypedPIDMakerClient {

    /**
     * Creates a new PID record using the SimplePidRecord format.
     *
     * @param record The PID record to create
     * @return The created PID record with response headers (including ETag)
     */
    @PostExchange(
            value = "/",
            accept = "application/vnd.datamanager.pid.simple+json",
            contentType = "application/vnd.datamanager.pid.simple+json")
    @WithSpan(kind = SpanKind.CLIENT)
    @Timed(value = "pidMakerClient.createPIDRecord", description = "Time taken to create a PID record", histogram = true)
    @Counted(value = "pidMakerClient.createPIDRecord.count", description = "Number of PID record creations")
    ResponseEntity<PIDRecord> createPIDRecord(@RequestBody PIDRecord record);

    /**
     * Gets a PID record by its PID using the SimplePidRecord format.
     *
     * @param pid The PID of the record to get
     * @return The PID record with response headers (including ETag)
     */
    @GetExchange(
            value = "/{pid}",
            accept = "application/vnd.datamanager.pid.simple+json")
    @WithSpan(kind = SpanKind.CLIENT)
    @Timed(value = "pidMakerClient.getPIDRecord", description = "Time taken to retrieve a PID record", histogram = true)
    @Counted(value = "pidMakerClient.getPIDRecord.count", description = "Number of PID record retrievals")
    ResponseEntity<PIDRecord> getPIDRecord(@SpanAttribute("pid.value") @PathVariable String pid);

    /**
     * Updates an existing PID record using the SimplePidRecord format.
     *
     * @param pid    The PID of the record to update
     * @param record The updated PID record
     * @param etag   The ETag value for the If-Match header
     * @return The updated PID record with response headers (including ETag)
     */
    @PutExchange(
            value = "/{pid}",
            accept = "application/vnd.datamanager.pid.simple+json",
            contentType = "application/vnd.datamanager.pid.simple+json")
    @WithSpan(kind = SpanKind.CLIENT)
    @Timed(value = "pidMakerClient.updatePIDRecord", description = "Time taken to update a PID record", histogram = true)
    @Counted(value = "pidMakerClient.updatePIDRecord.count", description = "Number of PID record updates")
    ResponseEntity<PIDRecord> updatePIDRecord(@SpanAttribute("pid.value") @PathVariable String pid,
                                              @RequestBody PIDRecord record,
                                              @RequestHeader("If-Match") String etag);
}