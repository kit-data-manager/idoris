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

package edu.kit.datamanager.idoris.datatypes.rules;

import edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType;
import edu.kit.datamanager.idoris.datatypes.entities.DataType;
import edu.kit.datamanager.idoris.datatypes.entities.TypeProfile;
import edu.kit.datamanager.idoris.rules.logic.Rule;
import edu.kit.datamanager.idoris.rules.validation.SyntaxValidator;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import edu.kit.datamanager.idoris.rules.validation.ValidationVisitor;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Observed(contextualName = "acyclicityValidator")
@Rule(
        appliesTo = {
                edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType.class,
                edu.kit.datamanager.idoris.datatypes.entities.TypeProfile.class
        },
        name = "AcyclicityValidationRule",
        description = "Validates that entities do not form cycles in their inheritance structure",
        tasks = {edu.kit.datamanager.idoris.rules.logic.RuleTask.VALIDATE},
        dependsOn = {SyntaxValidator.class}
)
public class AcyclicityValidator extends ValidationVisitor {
    @Autowired
    private Neo4jClient neo4jClient;

    @Override
    @WithSpan(kind = SpanKind.INTERNAL)
    public ValidationResult visit(AtomicDataType atomicDataType, Object... args) {
        return doesNotInheritItself(atomicDataType);
    }

    @Override
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.acyclicityValidator.visitTypeProfile", description = "Time to check acyclicity for TypeProfile", histogram = true)
    @Counted(value = "rules.acyclicityValidator.visitTypeProfile.count", description = "Number of TypeProfile acyclicity validations")
    public ValidationResult visit(TypeProfile profile, Object... args) {
        return ValidationResult.combine(
                doesNotInheritItself(profile),
                doesNotUseItselfAsAttribute(profile)
        );
    }

    /**
     * Validates that a TypeProfile does not use itself as an attribute type, either directly or through overrides.
     *
     * @param profile The TypeProfile to validate
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.acyclicityValidator.doesNotUseItselfAsAttribute", description = "Time to check for self-reference as attribute", histogram = true)
    @Counted(value = "rules.acyclicityValidator.doesNotUseItselfAsAttribute.count", description = "Number of self-reference attribute checks")
    private ValidationResult doesNotUseItselfAsAttribute(TypeProfile profile) {
        // Optimized single unified query to check for all types of self-reference cycles
        // Using MATCH...WHERE pattern for better readability and performance
        String query = "// Direct cycle check"
                + "MATCH path = (n:TypeProfile)-[:attributes]->(a:Attribute)-[:dataType]->(dt:DataType)"
                + "WHERE n.id = $nodeID AND dt.id = $nodeID "
                + "RETURN path, a.id AS attributeId, 'direct' AS cycleType, NULL AS baseAttributeId"
                + "UNION"
                + "// Attribute override cycle check"
                + "MATCH path = (n:TypeProfile)-[:attributes]->(a:Attribute)-[:override*1..]->(b:Attribute)-[:dataType]->(dt:DataType)"
                + "WHERE n.id = $nodeID AND dt.id = $nodeID "
                + "RETURN path, a.id AS attributeId, 'override' AS cycleType, b.id AS baseAttributeId"
                + "UNION"
                + "// Data type inheritance cycle check"
                + "MATCH path = (n:TypeProfile)-[:attributes]->(a:Attribute)-[:dataType]->(dt1:DataType)-[:inheritsFrom*1..]->(dt2:DataType)"
                + "WHERE n.id = $nodeID AND dt2.id = $nodeID "
                + "RETURN path, a.id AS attributeId, 'inheritance' AS cycleType, NULL AS baseAttributeId"
                + "LIMIT 1";

        // Execute the query - use fetchAs(Map.class) to get proper type-safe access to results
        var result = neo4jClient.query(query)
                .bind(profile.getId()).to("nodeID")
                .fetchAs(java.util.Map.class)
                .one();

        if (result.isPresent()) {
            var map = result.get();
            String cycleType = String.valueOf(map.get("cycleType"));

            String errorMessage = switch (cycleType) {
                case "direct" -> "TypeProfile directly uses itself as an attribute type.";
                case "override" -> {
                    String baseAttributeId = map.get("baseAttributeId") != null ?
                            String.valueOf(map.get("baseAttributeId")) : "Unknown";
                    yield "TypeProfile indirectly uses itself through attribute override. Base attribute ID: " + baseAttributeId;
                }
                case "inheritance" -> "TypeProfile indirectly uses itself through data type inheritance.";
                default -> "TypeProfile has a cyclic reference in its attributes.";
            };

            return ValidationResult.error("Illegal path: " + errorMessage, Map.of(
                    "element", profile,
                    "path", map.get("path"),
                    "cycleType", cycleType
            ));
        }

        return ValidationResult.ok();
    }

    /**
     * Validates that a DataType (TypeProfile or AtomicDataType) does not inherit from itself, preventing circular inheritance.
     *
     * @param dataType The DataType to validate
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.acyclicityValidator.doesNotInheritItself", description = "Time to check self-inheritance acyclicity", histogram = true)
    @Counted(value = "rules.acyclicityValidator.doesNotInheritItself.count", description = "Number of self-inheritance acyclicity checks")
    private ValidationResult doesNotInheritItself(DataType dataType) {
        String query = "MATCH path = (n:DataType {id: $nodeID})-[:inheritsFrom*1..]->(n) RETURN path";

        // Query the path from the Neo4j database
        var path = neo4jClient.query(query)
                .bind(dataType.getId()).to("nodeID")
                .fetch()
                .all();

        if (!path.isEmpty()) {
            return ValidationResult.error("Circular inheritance detected", Map.of("element", dataType, "path", path));
        } else {
            return ValidationResult.ok();
        }
    }
}
