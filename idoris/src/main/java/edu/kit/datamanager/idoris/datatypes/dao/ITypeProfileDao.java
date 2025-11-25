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

package edu.kit.datamanager.idoris.datatypes.dao;

import edu.kit.datamanager.idoris.core.dao.IGenericRepo;
import edu.kit.datamanager.idoris.core.domain.TypeProfile;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

@OpenAPIDefinition
public interface ITypeProfileDao extends IGenericRepo<TypeProfile> {
    /**
     * Finds all TypeProfile entities with their attributes in the inheritance chain of the given TypeProfile.
     * This method first tries to find the entity by PID, and if not found, tries to find it by internal ID.
     *
     * @param id the ID of the TypeProfile (either PID or internal ID)
     * @return an Iterable of TypeProfile entities with their attributes in the inheritance chain
     */
    default Iterable<TypeProfile> findAllTypeProfilesWithTheirAttributesInInheritanceChain(String id) {
        // First try to find by PID
        Optional<TypeProfile> byPid = findByPid(id);
        if (byPid.isPresent()) {
            return findAllTypeProfilesWithTheirAttributesInInheritanceChainByPid(id);
        }
        // If not found by PID, try to find by internal ID
        return findAllTypeProfilesWithTheirAttributesInInheritanceChainByInternalId(id);
    }

    @Query("MATCH (pid:PIDNode {pid: $pid})-[:IDENTIFIES]->(d:TypeProfile) MATCH (d)-[i:inheritsFrom*]->(d2:TypeProfile)-[profileAttribute:attributes]->(dataType:DataType) RETURN i, d2, collect(profileAttribute), collect(dataType)")
    Iterable<TypeProfile> findAllTypeProfilesWithTheirAttributesInInheritanceChainByPid(String pid);

    @Query("MATCH (d:TypeProfile {internalId: $internalId})-[i:inheritsFrom*]->(d2:TypeProfile)-[profileAttribute:attributes]->(dataType:DataType) RETURN i, d2, collect(profileAttribute), collect(dataType)")
    Iterable<TypeProfile> findAllTypeProfilesWithTheirAttributesInInheritanceChainByInternalId(String internalId);

    /**
     * Finds all TypeProfile entities in the inheritance chain of the given TypeProfile.
     * This method first tries to find the entity by PID, and if not found, tries to find it by internal ID.
     *
     * @param id the ID of the TypeProfile (either PID or internal ID)
     * @return an Iterable of TypeProfile entities in the inheritance chain
     */
    default Iterable<TypeProfile> findAllTypeProfilesInInheritanceChain(String id) {
        // First try to find by PID
        Optional<TypeProfile> byPid = findByPid(id);
        if (byPid.isPresent()) {
            return findAllTypeProfilesInInheritanceChainByPid(id);
        }
        // If not found by PID, try to find by internal ID
        return findAllTypeProfilesInInheritanceChainByInternalId(id);
    }

    @Query("MATCH (pid:PIDNode {pid: $pid})-[:IDENTIFIES]->(d:TypeProfile) MATCH (d)-[:inheritsFrom*]->(typeProfile:TypeProfile) return typeProfile")
    Iterable<TypeProfile> findAllTypeProfilesInInheritanceChainByPid(String pid);

    @Query("MATCH (d:TypeProfile {internalId: $internalId})-[:inheritsFrom*]->(typeProfile:TypeProfile) return typeProfile")
    Iterable<TypeProfile> findAllTypeProfilesInInheritanceChainByInternalId(String internalId);

    // ===== Relationship operations: inheritsFrom =====
    @Query("""
            MATCH (p:TypeProfile)
            WHERE p.internalId = $profileId OR EXISTS { MATCH (pid:PIDNode {pid: $profileId})-[:IDENTIFIES]->(p) }
            WITH p
            UNWIND $parentIds AS parentId
            MATCH (parent:TypeProfile)
            WHERE parent.internalId = parentId OR EXISTS { MATCH (pp:PIDNode {pid: parentId})-[:IDENTIFIES]->(parent) }
            MERGE (p)-[:inheritsFrom]->(parent)
            """)
    void addInheritsFrom(@Param("profileId") String profileId, @Param("parentIds") Collection<String> parentIds);

    @Query("""
            MATCH (p:TypeProfile)
            WHERE p.internalId = $profileId OR EXISTS { MATCH (pid:PIDNode {pid: $profileId})-[:IDENTIFIES]->(p) }
            WITH p
            UNWIND $parentIds AS parentId
            MATCH (parent:TypeProfile)
            WHERE parent.internalId = parentId OR EXISTS { MATCH (pp:PIDNode {pid: parentId})-[:IDENTIFIES]->(parent) }
            MATCH (p)-[r:inheritsFrom]->(parent)
            DELETE r
            """)
    void removeInheritsFrom(@Param("profileId") String profileId, @Param("parentIds") Collection<String> parentIds);

    // ===== Relationship operations: attributes =====
    @Query("""
            MATCH (p:TypeProfile)
            WHERE p.internalId = $profileId OR EXISTS { MATCH (pid:PIDNode {pid: $profileId})-[:IDENTIFIES]->(p) }
            WITH p
            UNWIND $attributeIds AS attributeId
            MATCH (a:Attribute)
            WHERE a.internalId = attributeId OR EXISTS { MATCH (pp:PIDNode {pid: attributeId})-[:IDENTIFIES]->(a) }
            MERGE (p)-[:attributes]->(a)
            """)
    void addAttributes(@Param("profileId") String profileId, @Param("attributeIds") Collection<String> attributeIds);

    @Query("""
            MATCH (p:TypeProfile)
            WHERE p.internalId = $profileId OR EXISTS { MATCH (pid:PIDNode {pid: $profileId})-[:IDENTIFIES]->(p) }
            WITH p
            UNWIND $attributeIds AS attributeId
            MATCH (a:Attribute)
            WHERE a.internalId = attributeId OR EXISTS { MATCH (pp:PIDNode {pid: attributeId})-[:IDENTIFIES]->(a) }
            MATCH (p)-[r:attributes]->(a)
            DELETE r
            """)
    void removeAttributes(@Param("profileId") String profileId, @Param("attributeIds") Collection<String> attributeIds);
}
