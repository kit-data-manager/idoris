/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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

import edu.kit.datamanager.idoris.core.domain.dao.IGenericRepo;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import org.springframework.data.neo4j.repository.query.Query;

/**
 * Repository interface for Operation entities.
 */
public interface IOperationDao extends IGenericRepo<Operation> {
    /**
     * Gets operations that can be executed on a data type.
     * This method finds operations that are executable on the given data type or its attributes.
     *
     * @param pid the PID of the data type
     * @return an Iterable of Operation entities
     */
    @Query("""
            MATCH (d:DataType {pid: $pid})<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {pid: $pid})-[:attributes]->(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {pid: $pid})-[:attributes]->(:Attribute)-[:dataType]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {pid: $pid})-[:inheritsFrom*]->(:DataType)-[:attributes]->(:Attribute)-[:dataType]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {pid: $pid})-[:inheritsFrom*]->(:DataType)-[:attributes]->(:Attribute)-[:dataType]->(:DataType)-[:inheritsFrom*]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o""")
    Iterable<Operation> getOperationsForDataTypeByPid(String pid);

    /**
     * Gets operations that can be executed on a data type.
     * This method finds operations that are executable on the given data type or its attributes.
     *
     * @param internalId the internal ID of the data type
     * @return an Iterable of Operation entities
     */
    @Query("""
            MATCH (d:DataType {internalId: $internalId})<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {internalId: $internalId})-[:attributes]->(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {internalId: $internalId})-[:attributes]->(:Attribute)-[:dataType]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {internalId: $internalId})-[:inheritsFrom*]->(:DataType)-[:attributes]->(:Attribute)-[:dataType]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {internalId: $internalId})-[:inheritsFrom*]->(:DataType)-[:attributes]->(:Attribute)-[:dataType]->(:DataType)-[:inheritsFrom*]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o""")
    Iterable<Operation> getOperationsForDataTypeByInternalId(String internalId);

    /**
     * Gets operations that can be executed on a data type.
     * This method finds operations that are executable on the given data type or its attributes.
     * This method tries to find operations using both PID and internal ID.
     *
     * @param id the ID of the data type (either PID or internal ID)
     * @return an Iterable of Operation entities
     */
    default Iterable<Operation> getOperationsForDataType(String id) {
        // Try both PID and internal ID
        Iterable<Operation> byPid = getOperationsForDataTypeByPid(id);
        Iterable<Operation> byInternalId = getOperationsForDataTypeByInternalId(id);

        // Combine the results
        return () -> {
            java.util.Iterator<Operation> pidIterator = byPid.iterator();
            java.util.Iterator<Operation> internalIdIterator = byInternalId.iterator();

            return new java.util.Iterator<Operation>() {
                @Override
                public boolean hasNext() {
                    return pidIterator.hasNext() || internalIdIterator.hasNext();
                }

                @Override
                public Operation next() {
                    if (pidIterator.hasNext()) {
                        return pidIterator.next();
                    }
                    return internalIdIterator.next();
                }
            };
        };
    }
}
