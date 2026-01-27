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

package edu.kit.datamanager.idoris.operations.rules;

import edu.kit.datamanager.idoris.core.domain.Operation;
import edu.kit.datamanager.idoris.core.domain.ValidationResult;
import edu.kit.datamanager.idoris.core.domain.ValidationVisitor;
import edu.kit.datamanager.idoris.core.domain.enums.ExecutionMode;
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
@Observed
@Rule(
        appliesTo = {
                AttributeMapping.class,
                Operation.class,
                OperationStep.class,
        },
        name = "OperationSyntaxRule",
        description = "Validates that Operations and their entities follow required syntax rules and constraints",
        tasks = RuleTask.VALIDATE
)
public class OperationSyntaxValidator extends ValidationVisitor {

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
                    attributeMapping, rule, WARNING);
        }

        if (attributeMapping.getIndex() != null && attributeMapping.getIndex() < 0) {
            result.addMessage("The index is out of range. It has to be a positive number or zero.",
                    attributeMapping, rule, ERROR);
        }

        if (attributeMapping.getOutput() == null) {
            result.addMessage("Output MUST be specified.", attributeMapping, rule, ERROR);
        }

        if (attributeMapping.getInput() == null && attributeMapping.getValue() == null) {
            result.addMessage("Input and value MUST NOT be unspecified at the same time.",
                    attributeMapping, rule, ERROR);
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
                        attributeMapping, rule, ERROR);
            }

            // If the input has an upper cardinality smaller than the lower cardinality of the output, return an error.
            if (attributeMapping.getInput().getUpperBoundCardinality() != null &&
                    attributeMapping.getOutput().getLowerBoundCardinality() > attributeMapping.getInput().getUpperBoundCardinality()) {
                result.addMessage("The output cardinality is not compatible with the input cardinality. The lower bound cardinality of the output must be less than or equal to the upper bound cardinality of the input.",
                        attributeMapping, rule, ERROR);
            }
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
                    operation, rule, ERROR);
        }

        if (operation.getDescription() == null) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the operation.",
                    operation, rule, WARNING);
        }

        if (operation.getExecutableOn() == null) {
            result.addMessage("You MUST specify an attribute on which the operation can be executed.",
                    operation, rule, ERROR);
        }

        if ((operation.getReturns() == null || operation.getReturns().isEmpty())) {
            result.addMessage("There are no return values provided for this Operation.", operation, rule, INFO);
        }

        if (operation.getExecution() == null || operation.getExecution().isEmpty()) {
            result.addMessage("You MUST specify at least one execution step for a valid operation.",
                    operation, rule, ERROR);
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
                    operationStep, rule, WARNING);
        }

        if (operationStep.getIndex() == null) {
            result.addMessage("Execution order index MUST be specified. If multiple Operation Steps for an Operation have the same index, the execution may happen in random order or be parallelized.",
                    operationStep, rule, ERROR);
        }

        if (operationStep.getMode() == null) {
            result.addMessage("An execution mode must be specified. Default is synchronous execution. Select from: " +
                    Arrays.toString(ExecutionMode.values()), operationStep, rule, ERROR);
        }

        if ((operationStep.getExecuteOperation() == null && operationStep.getUseTechnology() == null) ||
                (operationStep.getExecuteOperation() != null && operationStep.getUseTechnology() != null)) {
            result.addMessage("You MUST specify either an operation or an operation type profile for the operation step. You can only specify exactly one!",
                    operationStep, rule, ERROR);
        }

        return result;
    }
}
