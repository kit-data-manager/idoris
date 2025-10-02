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

package edu.kit.datamanager.idoris.core.domain;

import edu.kit.datamanager.idoris.core.domain.valueObjects.EmailAddress;
import edu.kit.datamanager.idoris.core.domain.valueObjects.Name;
import edu.kit.datamanager.idoris.core.domain.valueObjects.ORCiD;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.io.Serializable;
import java.time.Instant;

@Node("User")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class User implements Serializable {
    @CreatedDate
    Instant createdAt;

    @LastModifiedDate
    Instant updatedAt;

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String internalId;
    private Name name;
    private EmailAddress email;
    private ORCiD orcid;
}
