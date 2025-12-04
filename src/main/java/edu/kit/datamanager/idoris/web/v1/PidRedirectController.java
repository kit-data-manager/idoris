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
package edu.kit.datamanager.idoris.web.v1;

import com.google.common.base.Ascii;
import edu.kit.datamanager.idoris.dao.*;
import lombok.extern.java.Log;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.*;

@Controller
@RequestMapping("/pid/")
@Log
public class PidRedirectController {

    private static final String FIND_ENTITY_BY_PID_QUERY = "MATCH (n {pid: $pid}) RETURN n.pid AS pid, labels(n) AS nodeLabels LIMIT 1";

    private final Neo4jClient neo4jClient;
    private final Map<String, IGenericRepo<?>> labelToDaoMap;

    public PidRedirectController(Neo4jClient neo4jClient,
                                 IAtomicDataTypeDao atomicDataTypeDao,
                                 IAttributeDao attributeDao,
                                 IOperationDao operationDao,
                                 ITechnologyInterfaceDao technologyInterfaceDao,
                                 ITypeProfileDao typeProfileDao) {
        this.neo4jClient = neo4jClient;

        Map<String, IGenericRepo<?>> mapBuilder = new HashMap<>();
        mapBuilder.put("AtomicDataType", atomicDataTypeDao);
        mapBuilder.put("Attribute", attributeDao);
        mapBuilder.put("Operation", operationDao);
        mapBuilder.put("TechnologyInterface", technologyInterfaceDao);
        mapBuilder.put("TypeProfile", typeProfileDao);
        this.labelToDaoMap = Collections.unmodifiableMap(mapBuilder);
    }

    /**
     * Redirects to the appropriate entity based on the PID value.
     * The method fetches the entity data from Neo4j, extracts the PID and labels,
     * and determines the redirect path based on the labels.
     *
     * @param pidValue The PID value to redirect to.
     * @return A ResponseEntity with a redirect status or not found if no entity is found.
     */
    @GetMapping("/{pidValue}")
    public ResponseEntity<Void> redirectToEntity(@PathVariable("pidValue") String pidValue) {
        Optional<Map<String, Object>> entityDataOptional = fetchNeo4jData(pidValue);

        if (entityDataOptional.isEmpty()) {
            log.warning("No entity data found in Neo4j for PID: " + pidValue);
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> entityData = entityDataOptional.get();
        String entityPid = entityData.containsKey("pid") ? (String) entityData.get("pid") : null;

        if (entityPid == null || entityPid.isEmpty()) {
            log.warning("Extracted PID is null or empty for input: " + pidValue);
            return ResponseEntity.notFound().build();
        }

        List<String> nodeLabels = extractAndFilterLabels(entityData);

        if (nodeLabels.isEmpty()) {
            log.warning("No suitable labels found for PID: " + pidValue + " after filtering.");
            return ResponseEntity.notFound().build();
        }

        Optional<String> redirectPathBaseOptional = determineRedirectPathBase(entityPid, nodeLabels);

        if (redirectPathBaseOptional.isPresent()) {
            String redirectUrl = String.format("/%s/%s", redirectPathBaseOptional.get(), entityPid);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        } else {
            log.warning("No endpoint could be determined for PID: " + pidValue + " with labels: " + nodeLabels);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Fetches entity data from Neo4j based on the provided PID value.
     *
     * @param pidValue The PID value to search for.
     * @return An Optional containing the entity data if found, otherwise empty.
     */
    private Optional<Map<String, Object>> fetchNeo4jData(String pidValue) {
        return neo4jClient.query(FIND_ENTITY_BY_PID_QUERY)
                .bind(pidValue).to("pid")
                .fetch().one();
    }

    /**
     * Extracts and filters labels from the entity data.
     * Filters out null, empty, "GenericIDORISEntity", and labels starting with "_".
     *
     * @param entityData The entity data map containing labels.
     * @return A list of filtered labels.
     */
    private List<String> extractAndFilterLabels(Map<String, Object> entityData) {
        Object rawLabels = entityData.get("nodeLabels");
        if (!(rawLabels instanceof Value nodeLabelsValue)) {
            log.fine("nodeLabels field is missing or not of type Value for entity data: " + entityData);
            return Collections.emptyList();
        }

        return nodeLabelsValue.asList(Value::asString)
                .stream()
                .filter(label -> label != null && !label.isEmpty())
                .map(String::trim)
                .filter(label -> !label.equals("GenericIDORISEntity") && !label.startsWith("_"))
                .toList();
    }

    /**
     * Determines the redirect path base based on the entity PID and its labels.
     * It looks up the DAO for each label, fetches the entity by PID, and constructs the path base.
     *
     * @param entityPid  The PID of the entity.
     * @param nodeLabels The list of labels associated with the entity.
     * @return An Optional containing the redirect path base if found, otherwise empty.
     */
    private Optional<String> determineRedirectPathBase(String entityPid, List<String> nodeLabels) {
        return nodeLabels.stream()
                .map(labelToDaoMap::get) // Get DAO for label
                .filter(Objects::nonNull) // Filter out labels not in map (or DAOs that are null)
                .map(dao -> {
                    try {
                        // Assuming IGenericRepo has findByPID method.
                        // The exact return type of findByPID (e.g., entity or Optional<entity>)
                        // is handled by the filter(Objects::nonNull) that follows.
                        return dao.findByPid(entityPid);
                    } catch (Exception e) {
                        log.warning("Error calling findByPID on DAO for PID " + entityPid + ": " + e.getMessage());
                        return null; // Treat as not found if there's an error
                    }
                })
                .filter(Objects::nonNull) // Filter out if entity not found by this DAO or if an error occurred
                .map(entity -> Ascii.toLowerCase(entity.getClass().getSimpleName()) + "s") // Determine path base
                .findFirst(); // Take the first one that matches
    }
}
