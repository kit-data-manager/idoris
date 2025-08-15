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

package edu.kit.datamanager.idoris.datatypes.entities;

import edu.kit.datamanager.idoris.datatypes.enums.PrimitiveDataTypes;
import edu.kit.datamanager.idoris.rules.logic.RuleOutput;
import edu.kit.datamanager.idoris.rules.logic.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("AtomicDataType")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AtomicDataType extends DataType {
    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private AtomicDataType inheritsFrom;

    private PrimitiveDataTypes primitiveDataType;
    private String regularExpression;

    private Set<String> permittedValues;
    private Set<String> forbiddenValues;
    private Integer minimum;
    private Integer maximum;

    @Override
    protected <T extends RuleOutput<T>> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }

    @Override
    public boolean inheritsFrom(DataType dataType) {
        if (dataType instanceof AtomicDataType atomicDataType) {
            if (atomicDataType.equals(this)) {
                return true;
            }
            return inheritsFrom != null && inheritsFrom.inheritsFrom(atomicDataType);
        }
        return false;
    }
}
