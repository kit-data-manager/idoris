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

package edu.kit.datamanager.idoris.operations.entities;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import edu.kit.datamanager.idoris.rules.logic.RuleOutput;
import edu.kit.datamanager.idoris.rules.logic.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;
import java.util.Set;

@Node("Operation")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Operation extends AdministrativeMetadata {
    @Relationship(value = "executableOn", direction = Relationship.Direction.OUTGOING)
    private Attribute executableOn;
    @Relationship(value = "returns", direction = Relationship.Direction.INCOMING)
    private Set<Attribute> returns;
    @Relationship(value = "environment", direction = Relationship.Direction.INCOMING)
    private Set<Attribute> environment;

    @Relationship(value = "execution", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> execution;

    @Override
    protected <T extends RuleOutput<T>> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }
}
