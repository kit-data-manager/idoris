/*
 * Copyright (c) 2025 Karlsruhe Institute of Technology
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

/**
 * A specialized visitor for validating domain entities against defined rules.
 *
 * <p>This visitor traverses the domain model and delegates validation to the rule logic.
 * For each visited element, it executes all applicable validation rules and accumulates
 * the results into a hierarchical validation report.</p>
 *
 * <p>The visitor uses caching to avoid redundant validation of the same elements and
 * can detect and handle cycles in the domain model graph.</p>
 *
 * <p>This implementation is a Spring component that can be injected into services that
 * need validation capabilities.</p>
 */

import edu.kit.datamanager.idoris.rules.logic.IRule;
import edu.kit.datamanager.idoris.rules.logic.VisitableElement;
import edu.kit.datamanager.idoris.rules.logic.Visitor;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@Observed
public abstract class ValidationVisitor extends Visitor<ValidationResult> implements IRule<VisitableElement, ValidationResult> {

    /**
     * Constructor for ValidationVisitor.
     * Initializes the visitor with a factory that creates new ValidationResult instances.
     */
    public ValidationVisitor() {
        super(ValidationResult::new);
    }

    /**
     * Process method required by IRule interface.
     * Delegates to the appropriate validate method based on element type.
     *
     * @param input  the element to process
     * @param output the output to update with processing results
     */
    @Override
    @WithSpan(kind = SpanKind.INTERNAL)
    public void process(@SpanAttribute VisitableElement input, @SpanAttribute ValidationResult output) {
        ValidationResult result = input.execute(this);
        output.merge(result);
    }
}
