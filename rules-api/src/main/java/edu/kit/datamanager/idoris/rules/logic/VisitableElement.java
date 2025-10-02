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

public interface VisitableElement {
    /**
     * Get the unique identifier of this element.
     *
     * @return the unique identifier of this element
     */
    String getId();

    /**
     * This is called to execute the visitor on this element.
     * By overriding this method, you can execute pre- or post-processing steps after the double-dispatch.
     * This could be useful for caching results or logging.
     *
     * @param visitor the visitor to execute
     * @param args    additional arguments to pass to the visitor
     * @param <T>     the type of the rule output
     * @return the result of the visitor execution
     */
    default <T extends RuleOutput<T>> T execute(Visitor<T> visitor, Object... args) {
        return accept(visitor, args);
    }

    /**
     * Accept a visitor and allow it to process this element.
     * This method is part of the Visitor design pattern, enabling double-dispatch.
     * This method must be implemented by all classes implementing VisitableElement.
     *
     * @param visitor the visitor to accept
     * @param args    additional arguments to pass to the visitor
     * @param <T>     the type of the rule output
     * @return the result of the visitor processing
     */
    <T extends RuleOutput<T>> T accept(Visitor<T> visitor, Object... args);
}
