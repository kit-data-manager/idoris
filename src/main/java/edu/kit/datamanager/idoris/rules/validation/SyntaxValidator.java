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

package edu.kit.datamanager.idoris.rules.validation;

import edu.kit.datamanager.idoris.core.domain.*;
import edu.kit.datamanager.idoris.core.domain.enums.CombinationOptions;
import edu.kit.datamanager.idoris.core.domain.enums.ExecutionMode;
import edu.kit.datamanager.idoris.core.domain.enums.PrimitiveDataTypes;
import edu.kit.datamanager.idoris.core.domain.valueObjects.AttributeMapping;
import edu.kit.datamanager.idoris.core.domain.valueObjects.OperationStep;
import edu.kit.datamanager.idoris.rules.logic.Rule;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.*;

@Slf4j
@Observed(contextualName = "syntaxValidator")
@Rule(
        appliesTo = {
                AtomicDataType.class,
                Attribute.class,
                AttributeMapping.class,
                Operation.class,
                OperationStep.class,
                TechnologyInterface.class,
                TypeProfile.class
        },
        name = "SyntaxValidationRule",
        description = "Validates that entities follow required syntax rules and constraints",
        tasks = {RuleTask.VALIDATE},
        executeBefore = {InheritanceValidator.class}
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
                    attribute, ERROR);
        }

        if (attribute.getDescription() == null) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the attribute.",
                    attribute, WARNING);
        }

        if (attribute.getDataTypeId() == null || attribute.getDataTypeId().isBlank()) {
            result.addMessage("You MUST provide a data type for the attribute.", attribute, ERROR);
        }

        if (attribute.getLowerBoundCardinality() == null) {
            result.addMessage("You MUST provide a lower bound cardinality for the attribute.", attribute, ERROR);
        } else if (attribute.getLowerBoundCardinality() < 0) {
            result.addMessage("The lower bound cardinality of an attribute MUST be a positive number or zero.",
                    attribute, ERROR);
        }

        if (attribute.getUpperBoundCardinality() != null && attribute.getUpperBoundCardinality() < 0) {
            result.addMessage("The upper bound cardinality of an attribute MUST be a positive number or zero.",
                    attribute, ERROR);
        } else if (attribute.getUpperBoundCardinality() != null && attribute.getLowerBoundCardinality() != null &&
                attribute.getUpperBoundCardinality() < attribute.getLowerBoundCardinality()) {
            result.addMessage("The upper bound cardinality of an attribute MUST be greater than or equal to the lower bound cardinality.",
                    attribute, ERROR);
        } else if (attribute.getUpperBoundCardinality() == null) {
            result.addMessage("This attribute represents an unlimited number of values. This is not recommended, as it may lead to unexpected results in the future. Please consider setting an upper bound cardinality.",
                    attribute, WARNING);
        }

        return result;
    }

    /**
     * Validates syntax constraints for AttributeMapping entities
     *
     * @param attributeMapping The attribute mapping to validate
     * @param args             Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.syntaxValidator.visitAttributeMapping", description = "Time to validate syntax for AttributeMapping", histogram = true)
    @Counted(value = "rules.syntaxValidator.visitAttributeMapping.count", description = "Number of AttributeMapping syntax validations")
    public ValidationResult visit(AttributeMapping attributeMapping, Object... args) {
        ValidationResult result = new ValidationResult();

        if (attributeMapping.getName() == null || attributeMapping.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a name for the attribute mapping.",
                    attributeMapping, WARNING);
        }

        if (attributeMapping.getIndex() != null && attributeMapping.getIndex() < 0) {
            result.addMessage("The index is out of range. It has to be a positive number or zero.",
                    attributeMapping, ERROR);
        }

        if (attributeMapping.getOutput() == null) {
            result.addMessage("Output MUST be specified.", attributeMapping, ERROR);
        }

        if (attributeMapping.getInput() == null && attributeMapping.getValue() == null) {
            result.addMessage("Input and value MUST NOT be unspecified at the same time.",
                    attributeMapping, ERROR);
        }

        // Check that the output cardinalities are compatible with the input cardinalities.
        if (attributeMapping.getInput() != null && attributeMapping.getOutput() != null) {
            // If the input has an upper cardinality of more than one, the output has at most one or null, and no index is specified, return an error.
            if (attributeMapping.getInput().getLowerBoundCardinality() > 0 &&
                    attributeMapping.getInput().getUpperBoundCardinality() != null &&
                    attributeMapping.getInput().getUpperBoundCardinality() > 1 &&
                    (attributeMapping.getOutput().getLowerBoundCardinality() < 1 ||
                            (attributeMapping.getOutput().getUpperBoundCardinality() != null &&
                                    attributeMapping.getOutput().getUpperBoundCardinality() < 1)) &&
                    attributeMapping.getIndex() == null) {
                result.addMessage("The output cardinality is not compatible with the input cardinality. If the input has an upper cardinality of more than one, the output must have at least a lower cardinality of one and an upper cardinality of one or null.",
                        attributeMapping, ERROR);
            }

            // If the input has an upper cardinality smaller than the lower cardinality of the output, return an error.
            if (attributeMapping.getInput().getUpperBoundCardinality() != null &&
                    attributeMapping.getOutput().getLowerBoundCardinality() > attributeMapping.getInput().getUpperBoundCardinality()) {
                result.addMessage("The output cardinality is not compatible with the input cardinality. The lower bound cardinality of the output must be less than or equal to the upper bound cardinality of the input.",
                        attributeMapping, ERROR);
            }
        }

        return result;
    }

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
                    Arrays.toString(PrimitiveDataTypes.values()), atomicDataType, ERROR);
        }

        // Ensure that all permitted and forbidden values are of the same primitive data type
        if (atomicDataType.getPermittedValues() != null && atomicDataType.getPrimitiveDataType() != null) {
            for (String value : atomicDataType.getPermittedValues()) {
                if (!atomicDataType.getPrimitiveDataType().isValueValid(value)) {
                    result.addMessage("The permitted value '" + value + "' is not valid for the primitive data type " +
                            atomicDataType.getPrimitiveDataType() + ".", atomicDataType, ERROR);
                }
            }
        }

        // Ensure that no conflicts of permitted and forbidden values exist
        if (atomicDataType.getPermittedValues() != null && atomicDataType.getForbiddenValues() != null) {
            for (String value : atomicDataType.getPermittedValues()) {
                if (atomicDataType.getForbiddenValues().contains(value)) {
                    result.addMessage("The permitted values and forbidden values of the atomic data type must not overlap. The value '" +
                            value + "' is both permitted and forbidden.", atomicDataType, ERROR);
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
                    dataType, ERROR);
        }

        if (dataType.getDescription() == null) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the data type.",
                    dataType, WARNING);
        }

        if (dataType.getExpectedUseCases() == null || dataType.getExpectedUseCases().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a list of expected uses for the data type.",
                    dataType, WARNING);
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
                    Arrays.toString(CombinationOptions.values()), typeProfile, ERROR);
        }

        return result;
    }

    /**
     * Validates syntax constraints for Operation entities
     *
     * @param operation The operation to validate
     * @param args      Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.syntaxValidator.visitOperation", description = "Time to validate syntax for Operation", histogram = true)
    @Counted(value = "rules.syntaxValidator.visitOperation.count", description = "Number of Operation syntax validations")
    public ValidationResult visit(Operation operation, Object... args) {
        ValidationResult result = new ValidationResult();

        if (operation.getName() == null) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the operation.",
                    operation, ERROR);
        }

        if (operation.getDescription() == null) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the operation.",
                    operation, WARNING);
        }

        if (operation.getExecutableOn() == null) {
            result.addMessage("You MUST specify an attribute on which the operation can be executed.",
                    operation, ERROR);
        }

        if ((operation.getReturns() == null || operation.getReturns().isEmpty())) {
            result.addMessage("There are no return values provided for this Operation.", operation, INFO);
        }

        if (operation.getExecution() == null || operation.getExecution().isEmpty()) {
            result.addMessage("You MUST specify at least one execution step for a valid operation.",
                    operation, ERROR);
        }

        return result;
    }

    /**
     * Validates syntax constraints for OperationStep entities
     *
     * @param operationStep The operation step to validate
     * @param args          Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.syntaxValidator.visitOperationStep", description = "Time to validate syntax for OperationStep", histogram = true)
    @Counted(value = "rules.syntaxValidator.visitOperationStep.count", description = "Number of OperationStep syntax validations")
    public ValidationResult visit(OperationStep operationStep, Object... args) {
        ValidationResult result = new ValidationResult();

        if (operationStep.getName() == null || operationStep.getName().isEmpty()) {
            result.addMessage("For better human readability, you SHOULD provide a name for the operation step.",
                    operationStep, WARNING);
        }

        if (operationStep.getIndex() == null) {
            result.addMessage("Execution order index MUST be specified. If multiple Operation Steps for an Operation have the same index, the execution may happen in random order or be parallelized.",
                    operationStep, ERROR);
        }

        if (operationStep.getMode() == null) {
            result.addMessage("An execution mode must be specified. Default is synchronous execution. Select from: " +
                    Arrays.toString(ExecutionMode.values()), operationStep, ERROR);
        }

        if ((operationStep.getExecuteOperation() == null && operationStep.getUseTechnology() == null) ||
                (operationStep.getExecuteOperation() != null && operationStep.getUseTechnology() != null)) {
            result.addMessage("You MUST specify either an operation or an operation type profile for the operation step. You can only specify exactly one!",
                    operationStep, ERROR);
        }

        return result;
    }

    /**
     * Validates syntax constraints for TechnologyInterface entities
     *
     * @param technologyInterface The technology interface to validate
     * @param args                Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.syntaxValidator.visitTechnologyInterface", description = "Time to validate syntax for TechnologyInterface", histogram = true)
    @Counted(value = "rules.syntaxValidator.visitTechnologyInterface.count", description = "Number of TechnologyInterface syntax validations")
    public ValidationResult visit(TechnologyInterface technologyInterface, Object... args) {
        ValidationResult result = new ValidationResult();

        if (technologyInterface.getName() == null) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the operation type profile.",
                    technologyInterface, ERROR);
        }

        if (technologyInterface.getDescription() == null) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the operation type profile.",
                    technologyInterface, WARNING);
        }

        if (technologyInterface.getAdapters() == null || technologyInterface.getAdapters().isEmpty()) {
            result.addMessage("You SHOULD specify at least one adapter for the technology interface.",
                    technologyInterface, WARNING);
        }

        return result;
    }
}
