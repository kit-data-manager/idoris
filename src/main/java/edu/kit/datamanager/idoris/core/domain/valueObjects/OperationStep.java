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

package edu.kit.datamanager.idoris.core.domain.valueObjects;

import edu.kit.datamanager.idoris.core.domain.Operation;
import edu.kit.datamanager.idoris.core.domain.TechnologyInterface;
import edu.kit.datamanager.idoris.core.domain.enums.ExecutionMode;
import edu.kit.datamanager.idoris.rules.logic.RuleOutput;
import edu.kit.datamanager.idoris.rules.logic.VisitableElement;
import edu.kit.datamanager.idoris.rules.logic.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.List;

@Node("OperationStep")
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class OperationStep implements VisitableElement, Serializable {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    private String internalId;

    private Integer index;

    private String name;

    private ExecutionMode mode = ExecutionMode.sync;

    @Relationship(value = "subSteps", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> subSteps;

    @Relationship(value = "executeOperation", direction = Relationship.Direction.OUTGOING)
    private Operation executeOperation;

    @Relationship(value = "useTechnology", direction = Relationship.Direction.OUTGOING)
    private TechnologyInterface useTechnology;

    @Relationship(value = "inputMappings", direction = Relationship.Direction.INCOMING)
    private List<AttributeMapping> inputMappings;

    @Relationship(value = "outputMappings", direction = Relationship.Direction.OUTGOING)
    private List<AttributeMapping> outputMappings;

    @Override
    public String getId() {
        return internalId;
    }

    @Override
    public <T extends RuleOutput<T>> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }
}