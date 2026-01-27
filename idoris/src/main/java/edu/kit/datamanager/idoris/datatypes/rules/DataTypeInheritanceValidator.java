/*
 * Copyright (c) 2024-2026 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.datatypes.rules;

import edu.kit.datamanager.idoris.core.domain.AtomicDataType;
import edu.kit.datamanager.idoris.core.domain.TypeProfile;
import edu.kit.datamanager.idoris.core.domain.ValidationResult;
import edu.kit.datamanager.idoris.core.domain.ValidationVisitor;
import edu.kit.datamanager.idoris.rules.logic.Rule;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.ERROR;


/**
 * Rule-based validator that checks inheritance constraints for entities.
 * This validator ensures that inheritance relationships are valid and that
 * inherited properties maintain consistency with parent entities.
 */
@Slf4j
@Observed
@Rule(
        appliesTo = {
                AtomicDataType.class,
                TypeProfile.class
        },
        name = "DataTypeInheritanceValidator",
        description = "Validates that data types properly follow inheritance rules and constraints",
        tasks = RuleTask.VALIDATE
)
public class DataTypeInheritanceValidator extends ValidationVisitor {

    /**
     * Validates inheritance relationships for AtomicDataType entities
     *
     * @param atomicDataType The atomic data type to validate
     * @param args           Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.inheritanceValidator.visitAtomicDataType", description = "Time to validate inheritance for AtomicDataType", histogram = true)
    @Counted(value = "rules.inheritanceValidator.visitAtomicDataType.count", description = "Number of AtomicDataType inheritance validations")
    public ValidationResult visit(AtomicDataType atomicDataType, Object... args) {
        ValidationResult result = new ValidationResult();

        AtomicDataType parent = atomicDataType.getInheritsFrom();
        if (parent != null) {
            if (!atomicDataType.getPrimitiveDataType().equals(parent.getPrimitiveDataType()))
                result.addMessage("Primitive data type does not match parent", atomicDataType, rule, ERROR);

            // Compare permitted values with parent
            if (parent.getPermittedValues() != null && !parent.getPermittedValues().isEmpty()) {
                if (atomicDataType.getPermittedValues() == null || atomicDataType.getPermittedValues().isEmpty())
                    result.addMessage("Permitted values are not defined for atomic data type, but should contain at least those defined by the parent",
                            atomicDataType, rule, ERROR);
                else if (!atomicDataType.getPermittedValues().containsAll(parent.getPermittedValues()))
                    result.addMessage("Permitted values do not match parent", atomicDataType, rule, ERROR);
            }

            // Compare forbidden values with parent
            if (parent.getForbiddenValues() != null && !parent.getForbiddenValues().isEmpty()) {
                if (atomicDataType.getForbiddenValues() == null || atomicDataType.getForbiddenValues().isEmpty())
                    result.addMessage("Forbidden values are not defined for atomic data type, but should contain at least those defined by the parent",
                            atomicDataType, rule, ERROR);
                else if (!atomicDataType.getForbiddenValues().containsAll(parent.getForbiddenValues()))
                    result.addMessage("Forbidden values do not match parent", atomicDataType, rule, ERROR);
            }

            // Detect conflicts between permitted and forbidden values in atomic data type and parent
            if (atomicDataType.getPermittedValues() != null && atomicDataType.getForbiddenValues() != null &&
                    !atomicDataType.getPermittedValues().isEmpty() && !atomicDataType.getForbiddenValues().isEmpty() &&
                    atomicDataType.getPermittedValues().stream().anyMatch(atomicDataType.getForbiddenValues()::contains)) {
                result.addMessage("Atomic data type has conflicting permitted and forbidden values", atomicDataType, rule, ERROR);
            }
        }

        return result;
    }

    /**
     * Validates inheritance relationships for TypeProfile entities
     *
     * @param typeProfile The type profile to validate
     * @param args        Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.inheritanceValidator.visitTypeProfile", description = "Time to validate inheritance for TypeProfile", histogram = true)
    @Counted(value = "rules.inheritanceValidator.visitTypeProfile.count", description = "Number of TypeProfile inheritance validations")
    public ValidationResult visit(TypeProfile typeProfile, Object... args) {
        ValidationResult result = new ValidationResult();

        if (typeProfile.getInheritsFrom() != null && !typeProfile.getInheritsFrom().isEmpty()) {
            for (TypeProfile parent : typeProfile.getInheritsFrom()) {
                if (parent.isAbstract() && !typeProfile.isAbstract()) {
                    result.addMessage("TypeProfile " + typeProfile.getId() + " is not abstract, but inherits from the TypeProfile " +
                            parent.getId() + " that is abstract.", typeProfile, rule, ERROR);
                }
            }
        }

        return result;
    }
}
