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

package edu.kit.datamanager.idoris.technologyinterfaces.rules;

import edu.kit.datamanager.idoris.core.domain.TechnologyInterface;
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
import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.WARNING;

@Slf4j
@Observed
@Rule(
        appliesTo = TechnologyInterface.class,
        name = "TechnologyInterfaceSyntaxRule",
        description = "Validates that Technology Interfaces follow required syntax rules and constraints",
        tasks = RuleTask.VALIDATE
)
public class TechnologyInterfaceSyntaxValidator extends ValidationVisitor {
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
                    technologyInterface, rule, ERROR);
        }

        if (technologyInterface.getDescription() == null) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the operation type profile.",
                    technologyInterface, rule, WARNING);
        }

        if (technologyInterface.getAdapters() == null || technologyInterface.getAdapters().isEmpty()) {
            result.addMessage("You SHOULD specify at least one adapter for the technology interface.",
                    technologyInterface, rule, WARNING);
        }

        return result;
    }
}
