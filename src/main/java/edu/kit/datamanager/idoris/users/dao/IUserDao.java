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

import edu.kit.datamanager.idoris.users.entities.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IUserDao extends Neo4jRepository<User, String>, ListCrudRepository<User, String>, PagingAndSortingRepository<User, String> {
    @Query("MATCH (u:ORCiDUser) RETURN u")
    Iterable<User> findAllORCiDUsers();

    @Query("MATCH (u:ORCiDUser) WHERE u.orcid = $orcid RETURN u")
    User findORCiDUserByORCiD(String orcid);

    @Query("MATCH (u:TextUser) RETURN u")
    Iterable<User> findAllTextUsers();

    @Query("MATCH (u:TextUser) WHERE u.email = $email RETURN u")
    User findTextUserByEmail(String email);
}