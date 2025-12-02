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

package edu.kit.datamanager.idoris.rules.logic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;


@NullMarked
/**
 * Immutable record representing a message produced during rule processing.
 * Each message has a text content, a severity level, and optional related elements.
 *
 * <p>This record is used to communicate validation results, warnings, and informational messages
 * from rule processors to the calling code.</p>
 *
 * @param message  the text content of the message, must not be null
 * @param severity the severity level of the message, must not be null
 * @param element  optional elements related to this message (e.g., the objects that caused the message)
 */
public record OutputMessage(@NotNull String message, @NotNull MessageSeverity severity, @NotNull @JsonIgnore Rule rule,
                            @Nullable Object... element) {

    @JsonProperty("rule")
    public RuleInfo getRuleInfo() {
        return new RuleInfo(
                rule.name(),
                rule.description(),
                rule.tasks(),
                classNames(rule.appliesTo()),
                classNames(rule.dependsOn()),
                classNames(rule.executeBefore()),
                classNames(rule.onError()),
                rule.executeOnEvent()
        );
    }

    private static String[] classNames(Class<?>[] classes) {
        return Arrays.stream(classes).map(Class::getSimpleName).toArray(String[]::new);
    }

    /**
     * Enumeration of possible message severity levels.
     * Severities are ordered from least severe (INFO) to most severe (ERROR).
     */
    @AllArgsConstructor
    @Getter
    public enum MessageSeverity {
        /**
         * Informational message that doesn't indicate a problem.
         * Typically used for providing context or additional information.
         */
        INFO("INFO"),        // ordinal 0

        /**
         * Warning message that indicates a potential issue but not a critical problem.
         * The system can continue to function, but the user should be aware of the warning.
         */
        WARNING("WARNING"),  // ordinal 1

        /**
         * Error message that indicates a critical problem.
         * The system may not function correctly due to this error.
         */
        ERROR("ERROR");      // ordinal 2

        /**
         * The string representation of this severity level
         */
        private final String value;

        /**
         * Checks if this severity is higher than or equal to the other severity.
         * Assumes severities are ordered by their declaration (ordinal value):
         * INFO < WARNING < ERROR
         *
         * @param other The severity to compare against.
         * @return true if this severity is higher than or equal to other; false otherwise.
         */
        @SuppressWarnings("EnumOrdinal")
        public boolean isHigherOrEqualTo(MessageSeverity other) {
            if (other == null) {
                // Or throw new IllegalArgumentException("Cannot compare with a null severity");
                return false; // Or handle as per specific requirements for null inputs
            }
            return this.ordinal() >= other.ordinal();
        }
    }

    /**
     * DTO projection of the Rule annotation for JSON serialization.
     */
    public record RuleInfo(
            String name,
            String description,
            RuleTask[] tasks,
            String[] appliesTo,
            String[] dependsOn,
            String[] executeBefore,
            String[] onError,
            RuleEvent[] executeOnEvent
    ) {
    }
}