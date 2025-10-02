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

package edu.kit.datamanager.idoris.rules.logic;

import java.util.function.Supplier;

/**
 * Service interface for executing rules on elements based on specific tasks.
 * This logic allows for the execution of all rules associated with a given task
 * on a provided element, as well as the execution of specific rules by name or instance.
 * The results of rule executions are accumulated and returned as a single result object.
 */
public interface IRuleService {
    /**
     * Executes all rules associated with the given task on the provided element.
     *
     * @param task          the {@link RuleTask} to execute (e.g., {@link RuleTask#VALIDATE}). This filters the rules to be executed.
     * @param element       the domain element to execute the rules on, must implement {@link VisitableElement}
     * @param resultFactory factory for creating result objects (typically a constructor reference), used to accumulate results
     * @param <T>           the type of the element to be processed, must extend {@link VisitableElement}
     * @param <R>           the type of the result produced by the rules, must extend {@link RuleOutput}
     * @return the merged result of all executed rules, or an empty result if no rules apply
     * @throws RuntimeException if a critical error occurs during rule execution that prevents
     *                          completion of the operation
     * @see RuleTask
     * @see VisitableElement
     * @see RuleOutput
     */
    <T extends VisitableElement, R extends RuleOutput<R>> R executeRules(
            RuleTask task,
            T element,
            Supplier<R> resultFactory
    ) throws RuntimeException;

    /**
     * Executes a specific rule by its name on the provided element.
     * This method is useful if just a single rule needs to be executed, bypassing the task-based filtering.
     * The rule name must be unique across all registered rules.
     * If no rule with the given name is found, an exception will be thrown.
     *
     * @param ruleName      the unique name of the rule to be executed
     * @param element       the domain element to execute the rules on, must implement {@link VisitableElement}
     * @param resultFactory factory for creating result objects (typically a constructor reference), used to accumulate results
     * @param <T>           the type of the element to be processed, must extend {@link VisitableElement}
     * @param <R>           the type of the result produced by the rules, must extend {@link RuleOutput}
     * @return the merged result of all executed rules, or an empty result if no rules apply
     * @throws IllegalArgumentException if no rule with the given name is found
     * @throws RuntimeException         if a critical error occurs during rule execution that prevents
     *                                  completion of the operation
     * @see VisitableElement
     * @see RuleOutput
     * @see IRule
     */
    <T extends VisitableElement, R extends RuleOutput<R>> R executeSpecificRule(
            String ruleName,
            T element,
            Supplier<R> resultFactory
    ) throws IllegalArgumentException, RuntimeException;

    /**
     * Executes a specific rule on the provided element.
     * This method is useful if just a single rule needs to be executed, bypassing the task-based filtering.
     *
     * @param rule          the rule to be executed
     * @param element       the domain element to execute the rules on, must implement {@link VisitableElement}
     * @param resultFactory factory for creating result objects (typically a constructor reference), used to accumulate results
     * @param <T>           the type of the element to be processed, must extend {@link VisitableElement}
     * @param <R>           the type of the result produced by the rules, must extend {@link RuleOutput}
     * @return the merged result of all executed rules, or an empty result if no rules apply
     * @throws IllegalArgumentException if the element type is not applicable on the rule
     * @throws RuntimeException         if a critical error occurs during rule execution that prevents
     *                                  completion of the operation
     * @see VisitableElement
     * @see RuleOutput
     * @see IRule
     */
    <T extends VisitableElement, R extends RuleOutput<R>> R executeSpecificRule(
            IRule<T, R> rule,
            T element,
            Supplier<R> resultFactory
    ) throws RuntimeException, IllegalArgumentException;
}
