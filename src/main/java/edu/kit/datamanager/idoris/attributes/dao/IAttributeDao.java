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

package edu.kit.datamanager.idoris.attributes.dao;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.core.domain.dao.IGenericRepo;
import org.springframework.data.neo4j.repository.query.Query;

public interface IAttributeDao extends IGenericRepo<Attribute> {
    @Query("MATCH (n:Attribute)" +
            " WHERE size([(n)-[:dataType]->() | 1]) = 1 AND NOT (n)<-[]-()" +
            " WITH n" +
            " MATCH (x)-[r]->()" +
            " WHERE type(r) <> \"dataType\"" +
            " WITH collect(DISTINCT x) as otherNodes, n" +
            " WHERE NOT n IN otherNodes" +
            " DETACH DELETE n")
    void deleteOrphanedAttributes();
}
