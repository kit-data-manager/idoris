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

package edu.kit.datamanager.idoris.core.domain.entities;

import edu.kit.datamanager.idoris.core.domain.VisitableElement;
import edu.kit.datamanager.idoris.users.entities.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Node("IDORIS")
public abstract class AdministrativeMetadata extends VisitableElement implements Serializable {
    String name;

    String description;

    @Version
    Long version;

    @CreatedDate
    Instant createdAt;

    @LastModifiedDate
    Instant lastModifiedAt;

    Set<String> expectedUseCases;

    @Relationship(value = "contributors", direction = Relationship.Direction.OUTGOING)
    Set<User> contributors;

    Set<Reference> references;
}
