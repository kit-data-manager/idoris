/*
 * Copyright (c) 2025-2026 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.core.domain;

import edu.kit.datamanager.idoris.rules.logic.IRule;
import edu.kit.datamanager.idoris.rules.logic.VisitableElement;
import edu.kit.datamanager.idoris.rules.logic.Visitor;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;

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
        // Fix for Spring AOP / Visitor Pattern conflict:
        // We must pass the ACTUAL object (Target) to the visitor logic, not the CGLIB Proxy.
        // If we pass the Proxy, the reflective dispatch in the base Visitor class may fail
        // to find the specific visit() methods on the generated proxy class.
        Visitor<ValidationResult> visitorInstance = this;

        if (AopUtils.isAopProxy(this)) {
            Object target = AopProxyUtils.getSingletonTarget(this);
            if (target instanceof Visitor) {
                //noinspection unchecked
                visitorInstance = (Visitor<ValidationResult>) target;
            }
        }

        ValidationResult result = input.execute(visitorInstance);
        output.merge(result);
    }
}
