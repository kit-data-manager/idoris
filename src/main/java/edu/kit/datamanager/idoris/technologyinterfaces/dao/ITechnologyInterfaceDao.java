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

package edu.kit.datamanager.idoris.technologyinterfaces.dao;

import edu.kit.datamanager.idoris.core.dao.IGenericRepo;
import edu.kit.datamanager.idoris.core.domain.TechnologyInterface;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ITechnologyInterfaceDao extends IGenericRepo<TechnologyInterface> {

    @Query("""
            MATCH (ti:TechnologyInterface)
            WHERE ti.internalId = $technologyInterfaceId OR EXISTS { MATCH (p:PersistentIdentifier {pid: $technologyInterfaceId})-[:IDENTIFIES]->(ti) }
            WITH ti
            UNWIND $attributeIds AS attrId
            MATCH (a:Attribute)
            WHERE a.internalId = attrId OR EXISTS { MATCH (pp:PersistentIdentifier {pid: attrId})-[:IDENTIFIES]->(a) }
            MERGE (a)-[:attributes]->(ti)
            """)
    void linkInputs(@Param("technologyInterfaceId") String technologyInterfaceId, @Param("attributeIds") Collection<String> attributeIds);

    @Query("""
            MATCH (ti:TechnologyInterface)
            WHERE ti.internalId = $technologyInterfaceId OR EXISTS { MATCH (p:PersistentIdentifier {pid: $technologyInterfaceId})-[:IDENTIFIES]->(ti) }
            WITH ti
            UNWIND $attributeIds AS attrId
            MATCH (a:Attribute)
            WHERE a.internalId = attrId OR EXISTS { MATCH (pp:PersistentIdentifier {pid: attrId})-[:IDENTIFIES]->(a) }
            OPTIONAL MATCH (a)-[r:attributes]->(ti)
            DELETE r
            """)
    void unlinkInputs(@Param("technologyInterfaceId") String technologyInterfaceId, @Param("attributeIds") Collection<String> attributeIds);

    @Query("""
            MATCH (ti:TechnologyInterface)
            WHERE ti.internalId = $technologyInterfaceId OR EXISTS { MATCH (p:PersistentIdentifier {pid: $technologyInterfaceId})-[:IDENTIFIES]->(ti) }
            WITH ti
            UNWIND $attributeIds AS attrId
            MATCH (a:Attribute)
            WHERE a.internalId = attrId OR EXISTS { MATCH (pp:PersistentIdentifier {pid: attrId})-[:IDENTIFIES]->(a) }
            MERGE (ti)-[:outputs]->(a)
            """)
    void linkOutputs(@Param("technologyInterfaceId") String technologyInterfaceId, @Param("attributeIds") Collection<String> attributeIds);

    @Query("""
            MATCH (ti:TechnologyInterface)
            WHERE ti.internalId = $technologyInterfaceId OR EXISTS { MATCH (p:PersistentIdentifier {pid: $technologyInterfaceId})-[:IDENTIFIES]->(ti) }
            WITH ti
            UNWIND $attributeIds AS attrId
            MATCH (a:Attribute)
            WHERE a.internalId = attrId OR EXISTS { MATCH (pp:PersistentIdentifier {pid: attrId})-[:IDENTIFIES]->(a) }
            OPTIONAL MATCH (ti)-[r:outputs]->(a)
            DELETE r
            """)
    void unlinkOutputs(@Param("technologyInterfaceId") String technologyInterfaceId, @Param("attributeIds") Collection<String> attributeIds);
}
