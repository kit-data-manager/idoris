/*
 * Copyright (c) 2026 Karlsruhe Institute of Technology
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
import edu.kit.datamanager.idoris.datatypes.api.IDataTypeExternalService;
import edu.kit.datamanager.idoris.rules.logic.Rule;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.ERROR;

@Slf4j
@Observed
@Rule(
        appliesTo = Attribute.class,
        name = "AttributeInheritanceValidator",
        description = "Validates that attributes correctly inherit properties from their parent definitions",
        tasks = RuleTask.VALIDATE,
        dependsOn = SyntaxValidator.class
)
public class InheritanceValidator extends ValidationVisitor {

    private final IDataTypeExternalService dataTypeService;

    public InheritanceValidator(IDataTypeExternalService dataTypeService) {
        super();
        this.dataTypeService = dataTypeService;
    }

    /**
     * Validates inheritance relationships for Attribute entities
     *
     * @param attribute The attribute to validate
     * @param args      Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    public ValidationResult visit(Attribute attribute, Object... args) {
        ValidationResult result = new ValidationResult();

        if (attribute.getOverride() != null && attribute.getOverride().getDataTypeId() != null) {
            Attribute override = attribute.getOverride();

            if (dataTypeService.inheritsFrom(attribute.getDataTypeId(), override.getDataTypeId()) == false) {
                final Map<String, ?> element = Map.of(
                        "attribute", attribute,
                        "attributeDataTypeId", attribute.getDataTypeId(),
                        "overriddenAttribute", override,
                        "overriddenDataTypeId", override.getDataTypeId()
                );
                result.addMessage(
                        "The data type of an overridden attribute MUST inherit from the data type of the attribute that is being overwritten.",
                        element,
                        rule,
                        ERROR);
            }

            if (attribute.getLowerBoundCardinality() < override.getLowerBoundCardinality())
                result.addMessage("The lower bound cardinality of an attribute MUST be more or equally restrictive than the lower bound cardinality of the attribute that was overwritten. Overriding a more restrictive attribute as a less restrictive attribute is NOT possible.",
                        attribute, rule, ERROR);

            if (attribute.getUpperBoundCardinality() == null && override.getUpperBoundCardinality() != null) {
                result.addMessage("The upper bound cardinality of an attribute MUST be defined if the attribute that was overwritten has an upper bound cardinality defined.",
                        attribute, rule, ERROR);
            } else if (override.getUpperBoundCardinality() != null && attribute.getUpperBoundCardinality() > override.getUpperBoundCardinality())
                result.addMessage("The upper bound cardinality of an attribute MUST be more or equally restrictive than the upper bound cardinality of the attribute that was overwritten. Overriding a less restrictive attribute as a more restrictive attribute is NOT possible.",
                        attribute, rule, ERROR);
        }

        return result;
    }
}
