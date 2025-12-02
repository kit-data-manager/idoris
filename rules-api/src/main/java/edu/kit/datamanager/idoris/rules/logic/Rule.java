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

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Annotation to define a rule that can be applied to elements in the system.
 * This annotation is used to specify the types of elements the rule applies to,
 * the processors to be executed before, after, and on error, and the events that trigger the rule.
 * It also includes metadata such as the name, description, and tasks associated with the rule.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface Rule {
    /**
     * Specifies the types of elements this rule applies to.
     * This is used to filter which rules are applicable to a given element.
     *
     * @return an array of classes that extend VisitableElement
     */
    Class<? extends VisitableElement>[] appliesTo();

    /**
     * Specifies the processors that should be executed before the rule execution.
     * These processors can be used to prepare the input for the rule or to perform preliminary checks.
     * These processors can modify the input or output of the rule.
     *
     * @return arrays of classes that implement IRule
     */
    Class<? extends IRule>[] dependsOn() default {};

    /**
     * Specifies the processors that should be executed after the main rule processing.
     * This can be used for additional validation, transformation, or other post-processing tasks.
     *
     * @return an array of classes that implement IRule
     */
    Class<? extends IRule>[] executeBefore() default {};

    /**
     * Specifies the processors that should be executed in case of an error during rule processing.
     * This can be used for error handling, logging, or alternative processing paths.
     *
     * @return an array of classes that implement IRule
     */
    Class<? extends IRule>[] onError() default {};

    /**
     * Specifies the events that trigger the execution of this rule.
     * This allows the rule to be executed in response to specific events in the system.
     *
     * @return an array of RuleEvent enums that define the events
     */
    RuleEvent[] executeOnEvent() default {};

    /**
     * Specifies the name of the rule.
     * This is used for identification and logging purposes.
     *
     * @return the name of the rule
     */
    String name();

    /**
     * Provides a description of the rule.
     * This can be used to explain the purpose and functionality of the rule.
     *
     * @return a string description of the rule
     */
    String description() default "";

    /**
     * Specifies the tasks that this rule is responsible for.
     * This allows the rule to be categorized and executed based on the specific task it handles.
     *
     * @return an array of RuleTask enums that define the tasks
     */
    RuleTask[] tasks() default {RuleTask.VALIDATE}; // e.g. "VALIDATE", "CONSUME", "OPTIMIZE", "GENERATE_SCHEMA"
}

