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

import edu.kit.datamanager.idoris.core.domain.*;
import edu.kit.datamanager.idoris.core.domain.enums.CombinationOptions;
import edu.kit.datamanager.idoris.core.domain.enums.PrimitiveDataTypes;
import edu.kit.datamanager.idoris.rules.logic.Rule;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.ERROR;
import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.WARNING;

@Slf4j
@Observed
@Rule(
        appliesTo = {
                AtomicDataType.class,
                TypeProfile.class
        },
        name = "DataTypeSyntaxRule",
        description = "Validates that data types follow required syntax rules and constraints",
        tasks = RuleTask.VALIDATE,
        executeBefore = DataTypeInheritanceValidator.class
)
public class DataTypeSyntaxValidator extends ValidationVisitor {

    /**
     * Validates syntax constraints for AtomicDataType entities
     *
     * @param atomicDataType The atomic data type to validate
     * @param args           Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.syntaxValidator.visitAtomicDataType", description = "Time to validate syntax for AtomicDataType", histogram = true)
    @Counted(value = "rules.syntaxValidator.visitAtomicDataType.count", description = "Number of AtomicDataType syntax validations")
    public ValidationResult visit(AtomicDataType atomicDataType, Object... args) {
        ValidationResult result = new ValidationResult();

        validateDataType(atomicDataType, result);

        if (atomicDataType.getPrimitiveDataType() == null) {
            result.addMessage("You MUST provide a primitive data type for the basic data type. Please select from: " +
                    Arrays.toString(PrimitiveDataTypes.values()), atomicDataType, this.getClass().getAnnotation(Rule.class), ERROR);
        }

        // Ensure that all permitted and forbidden values are of the same primitive data type
        if (atomicDataType.getPermittedValues() != null && atomicDataType.getPrimitiveDataType() != null) {
            for (String value : atomicDataType.getPermittedValues()) {
                if (!atomicDataType.getPrimitiveDataType().isValueValid(value)) {
                    result.addMessage("The permitted value '" + value + "' is not valid for the primitive data type " +
                            atomicDataType.getPrimitiveDataType() + ".", atomicDataType, rule, ERROR);
                }
            }
        }

        // Ensure that no conflicts of permitted and forbidden values exist
        if (atomicDataType.getPermittedValues() != null && atomicDataType.getForbiddenValues() != null) {
            for (String value : atomicDataType.getPermittedValues()) {
                if (atomicDataType.getForbiddenValues().contains(value)) {
                    result.addMessage("The permitted values and forbidden values of the atomic data type must not overlap. The value '" +
                            value + "' is both permitted and forbidden.", atomicDataType, rule, ERROR);
                }
            }
        }

        return result;
    }

    /**
     * Helper method to validate common data type properties
     *
     * @param dataType The data type to validate
     * @param result   The validation result to add messages to
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.syntaxValidator.validateDataType", description = "Time to validate common data type properties", histogram = true)
    @Counted(value = "rules.syntaxValidator.validateDataType.count", description = "Number of data type property validations")
    private void validateDataType(DataType dataType, ValidationResult result) {
        if (dataType.getName() == null) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the data type.",
                    dataType, rule, ERROR);
        }

        if (dataType.getDescription() == null) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the data type.",
                    dataType, rule, WARNING);
        }

        if (dataType.getExpectedUseCases() == null || dataType.getExpectedUseCases().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a list of expected uses for the data type.",
                    dataType, rule, WARNING);
        }
    }

    /**
     * Validates syntax constraints for TypeProfile entities
     *
     * @param typeProfile The type profile to validate
     * @param args        Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.syntaxValidator.visitTypeProfile", description = "Time to validate syntax for TypeProfile", histogram = true)
    @Counted(value = "rules.syntaxValidator.visitTypeProfile.count", description = "Number of TypeProfile syntax validations")
    public ValidationResult visit(TypeProfile typeProfile, Object... args) {
        ValidationResult result = new ValidationResult();

        validateDataType(typeProfile, result);

        if (typeProfile.getValidationPolicy() == null) {
            result.addMessage("You MUST provide a validation policy for the type profile. Please select from: " +
                    Arrays.toString(CombinationOptions.values()), typeProfile, rule, ERROR);
        }

        return result;
    }
}
