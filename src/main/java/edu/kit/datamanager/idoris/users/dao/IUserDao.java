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

package edu.kit.datamanager.idoris.users.dao;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.domain.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public interface IUserDao extends Neo4jRepository<User, String>, ListCrudRepository<User, String>, PagingAndSortingRepository<User, String> {
    List<User> findAll();

    Optional<User> findByOrcid(URL orcid);

    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    @Query("MATCH (u:User {internalId: $userId})<-[:contributors]-(adm:AdministrativeMetadata) RETURN adm")
    List<AdministrativeMetadata> getContributions(String userId);
}