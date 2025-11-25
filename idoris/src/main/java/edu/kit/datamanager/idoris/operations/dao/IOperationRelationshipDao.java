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
package edu.kit.datamanager.idoris.operations.dao;

import edu.kit.datamanager.idoris.core.domain.valueObjects.AttributeMapping;
import edu.kit.datamanager.idoris.core.domain.valueObjects.OperationStep;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

/**
 * DAO for managing relationships between Operation, OperationStep, and AttributeMapping.
 */
public interface IOperationRelationshipDao extends Neo4jRepository<OperationStep, String> {

    // Steps for an Operation
    @Query("""
            MATCH (o:Operation)
            WHERE o.internalId = $opId OR EXISTS { MATCH (pid:PersistentIdentifier {pid: $opId})-[:IDENTIFIES]->(o) }
            MATCH (o)-[:execution]->(s:OperationStep)
            RETURN s ORDER BY s.index
            """)
    Iterable<OperationStep> listSteps(@Param("opId") String operationId);

    @Query("""
            MATCH (o:Operation)
            WHERE o.internalId = $opId OR EXISTS { MATCH (pid:PersistentIdentifier {pid: $opId})-[:IDENTIFIES]->(o) }
            MATCH (s:OperationStep {internalId: $stepId})
            MERGE (o)-[:execution]->(s)
            """)
    void addStep(@Param("opId") String operationId, @Param("stepId") String stepId);

    @Query("""
            MATCH (o:Operation)
            WHERE o.internalId = $opId OR EXISTS { MATCH (pid:PersistentIdentifier {pid: $opId})-[:IDENTIFIES]->(o) }
            MATCH (s:OperationStep)
            WHERE s.internalId IN $stepIds
            MATCH (o)-[r:execution]->(s)
            DELETE r
            """)
    void removeSteps(@Param("opId") String operationId, @Param("stepIds") java.util.Collection<String> stepIds);

    // Sub-steps relationship
    @Query("""
            MATCH (p:OperationStep {internalId: $parentId})
            MATCH (c:OperationStep {internalId: $childId})
            MERGE (p)-[:subSteps]->(c)
            """)
    void addSubStep(@Param("parentId") String parentStepId, @Param("childId") String childStepId);

    // ExecuteOperation link
    @Query("""
            MATCH (s:OperationStep {internalId: $stepId})
            MATCH (o:Operation)
            WHERE o.internalId = $opId OR EXISTS { MATCH (pid:PersistentIdentifier {pid: $opId})-[:IDENTIFIES]->(o) }
            MERGE (s)-[:executeOperation]->(o)
            """)
    void setStepExecuteOperation(@Param("stepId") String stepId, @Param("opId") String opId);

    // UseTechnology link
    @Query("""
            MATCH (s:OperationStep {internalId: $stepId})
            MATCH (t:TechnologyInterface)
            WHERE t.internalId = $techId OR EXISTS { MATCH (pid:PersistentIdentifier {pid: $techId})-[:IDENTIFIES]->(t) }
            MERGE (s)-[:useTechnology]->(t)
            """)
    void setStepUseTechnology(@Param("stepId") String stepId, @Param("techId") String technologyId);

    // Input mappings of a step
    @Query("""
            MATCH (m:AttributeMapping)-[:inputMappings]->(s:OperationStep {internalId: $stepId})
            RETURN m ORDER BY m.index
            """)
    Iterable<AttributeMapping> listInputMappings(@Param("stepId") String stepId);

    @Query("""
            UNWIND $mappingIds AS mid
            MATCH (m:AttributeMapping {internalId: mid})
            MATCH (s:OperationStep {internalId: $stepId})
            MERGE (m)-[:inputMappings]->(s)
            """)
    void addInputMappings(@Param("stepId") String stepId, @Param("mappingIds") java.util.Collection<String> mappingIds);

    @Query("""
            MATCH (m:AttributeMapping)-[r:inputMappings]->(s:OperationStep {internalId: $stepId})
            WHERE m.internalId IN $mappingIds
            DELETE r
            """)
    void removeInputMappings(@Param("stepId") String stepId, @Param("mappingIds") java.util.Collection<String> mappingIds);

    // Output mappings of a step
    @Query("""
            MATCH (s:OperationStep {internalId: $stepId})-[:outputMappings]->(m:AttributeMapping)
            RETURN m ORDER BY m.index
            """)
    Iterable<AttributeMapping> listOutputMappings(@Param("stepId") String stepId);

    @Query("""
            UNWIND $mappingIds AS mid
            MATCH (m:AttributeMapping {internalId: mid})
            MATCH (s:OperationStep {internalId: $stepId})
            MERGE (s)-[:outputMappings]->(m)
            """)
    void addOutputMappings(@Param("stepId") String stepId, @Param("mappingIds") java.util.Collection<String> mappingIds);

    @Query("""
            MATCH (s:OperationStep {internalId: $stepId})-[r:outputMappings]->(m:AttributeMapping)
            WHERE m.internalId IN $mappingIds
            DELETE r
            """)
    void removeOutputMappings(@Param("stepId") String stepId, @Param("mappingIds") java.util.Collection<String> mappingIds);
}
