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

package edu.kit.datamanager.idoris.attributes.rules;

import edu.kit.datamanager.idoris.core.domain.Attribute;
import edu.kit.datamanager.idoris.core.domain.ValidationResult;
import edu.kit.datamanager.idoris.core.domain.ValidationVisitor;
import edu.kit.datamanager.idoris.rules.logic.Rule;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.ERROR;
import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.WARNING;

@Slf4j
@Observed
@Rule(
        appliesTo = Attribute.class,
        name = "AttributeSyntaxRule",
        description = "Validates that attributes follow required syntax rules and constraints",
        tasks = RuleTask.VALIDATE,
        executeBefore = InheritanceValidator.class
)
public class SyntaxValidator extends ValidationVisitor {
    /**
     * Validates syntax constraints for Attribute entities
     *
     * @param attribute The attribute to validate
     * @param args      Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    public ValidationResult visit(Attribute attribute, Object... args) {
        ValidationResult result = new ValidationResult();

        if (attribute.getName() == null) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the attribute.",
                    attribute, rule, ERROR);
        }

        if (attribute.getDescription() == null) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the attribute.",
                    attribute, rule, WARNING);
        }

        if (attribute.getDataTypeId() == null || attribute.getDataTypeId().isBlank()) {
            result.addMessage("You MUST provide a data type for the attribute.", attribute, rule, ERROR);
        }

        if (attribute.getLowerBoundCardinality() == null) {
            result.addMessage("You MUST provide a lower bound cardinality for the attribute.", attribute, rule, ERROR);
        } else if (attribute.getLowerBoundCardinality() < 0) {
            result.addMessage("The lower bound cardinality of an attribute MUST be a positive number or zero.",
                    attribute, rule, ERROR);
        }

        if (attribute.getUpperBoundCardinality() != null && attribute.getUpperBoundCardinality() < 0) {
            result.addMessage("The upper bound cardinality of an attribute MUST be a positive number or zero.",
                    attribute, rule, ERROR);
        } else if (attribute.getUpperBoundCardinality() != null && attribute.getLowerBoundCardinality() != null &&
                attribute.getUpperBoundCardinality() < attribute.getLowerBoundCardinality()) {
            result.addMessage("The upper bound cardinality of an attribute MUST be greater than or equal to the lower bound cardinality.",
                    attribute, rule, ERROR);
        } else if (attribute.getUpperBoundCardinality() == null) {
            result.addMessage("This attribute represents an unlimited number of values. This is not recommended, as it may lead to unexpected results in the future. Please consider setting an upper bound cardinality.",
                    attribute, rule, WARNING);
        }

        return result;
    }
}
