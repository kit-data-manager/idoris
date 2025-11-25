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

import edu.kit.datamanager.idoris.rules.logic.OutputMessage;
import edu.kit.datamanager.idoris.rules.logic.RuleOutput;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.*;

/**
 * A composite result container for validation operations.
 *
 * <p>This class implements the {@link RuleOutput} interface to be compatible with the rule processing
 * framework. It stores validation messages of different severity levels (errors, warnings, info)
 * and can be structured hierarchically with child results to reflect complex validation processes.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Hierarchical structure with parent-child relationships</li>
 *   <li>Message categorization by severity (ERROR, WARNING, INFO)</li>
 *   <li>Aggregation of message counts across the hierarchy</li>
 *   <li>Fluent interface for building validation results</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * ValidationResult result = new ValidationResult()
 *     .addMessage("Invalid format", element, OutputMessage.MessageSeverity.ERROR)
 *     .addChild(nestedValidation);
 *
 * if (!result.isValid()) {
 *     // Handle validation failure
 * }
 * </pre>
 */
@Slf4j
@Getter
@Setter
public class ValidationResult implements RuleOutput<ValidationResult> {
    /**
     * List of validation messages directly associated with this result
     */
    private List<OutputMessage> messages = new ArrayList<>();

    /**
     * List of child validation results that form a hierarchical structure
     */
    private List<ValidationResult> children = new ArrayList<>();

    public static ValidationResult combine(ValidationResult... results) {
        ValidationResult combined = new ValidationResult();
        if (results != null) {
            for (ValidationResult result : results) {
                if (result != null && !result.isEmpty()) {
                    combined.merge(result);
                }
            }
        }
        return combined;
    }

    public static ValidationResult error(String message, Object element) {
        return new ValidationResult().addMessage(message, element, OutputMessage.MessageSeverity.ERROR);
    }

    /**
     * Adds a validation message to this result.
     *
     * @param message the text content of the message
     * @param element the element that the message relates to
     * @param type    the severity type of the message
     * @return this result instance for method chaining
     */
    public ValidationResult addMessage(String message, Object element, OutputMessage.MessageSeverity type) {
        messages.add(new OutputMessage(message, type, element));
        return this;
    }

    public static ValidationResult warning(String message, Object element) {
        return new ValidationResult().addMessage(message, element, OutputMessage.MessageSeverity.WARNING);
    }

    public static ValidationResult info(String message, Object element) {
        return new ValidationResult().addMessage(message, element, OutputMessage.MessageSeverity.INFO);
    }

    public static ValidationResult ok() {
        return new ValidationResult();
    }

    /**
     * Adds a child validation result to this result.
     * Empty child results are not added to avoid cluttering the hierarchy.
     *
     * @param child the child validation result to add
     * @return this result instance for method chaining
     */
    public ValidationResult addChild(ValidationResult child) {
        if (child != null && !child.isEmpty()) children.add(child);
        return this;
    }

    /**
     * Checks if this validation result represents a valid state.
     * A result is considered valid if it has no error messages in itself or any of its children.
     *
     * @return true if no errors exist, false otherwise
     */
    public boolean isValid() {
        return getErrorCount() == 0;
    }

    /**
     * Counts the total number of error messages in this result and all its children.
     *
     * @return the total number of error messages
     */
    public int getErrorCount() {
        int ownErrors = (int) messages.stream().filter(m -> m.severity() == ERROR).count();
        int childErrors = children.stream().mapToInt(ValidationResult::getErrorCount).sum();
        return ownErrors + childErrors;
    }

    /**
     * Counts the total number of warning messages in this result and all its children.
     *
     * @return the total number of warning messages
     */
    public int getWarningCount() {
        int ownWarnings = (int) messages.stream().filter(m -> m.severity() == WARNING).count();
        int childWarnings = children.stream().mapToInt(ValidationResult::getWarningCount).sum();
        return ownWarnings + childWarnings;
    }

    /**
     * Counts the total number of informational messages in this result and all its children.
     *
     * @return the total number of informational messages
     */
    public int getInfoCount() {
        int ownInfo = (int) messages.stream().filter(m -> m.severity() == INFO).count();
        int childInfo = children.stream().mapToInt(ValidationResult::getInfoCount).sum();
        return ownInfo + childInfo;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "messages=" + messages +
                ", children=" + children +
                ", errorCount=" + getErrorCount() +
                ", warningCount=" + getWarningCount() +
                '}';
    }

    public boolean isEmpty() {
        boolean noErrors = getErrorCount() == 0;
        boolean noWarnings = getWarningCount() == 0;
        boolean noInfo = getInfoCount() == 0;
        return noErrors && noWarnings && noInfo;
    }

    public List<OutputMessage> getOutputMessages(OutputMessage.MessageSeverity severity) {
        List<OutputMessage> result = new ArrayList<>();
        messages.stream().filter(m -> m.severity() == severity).forEach(result::add);
        children.stream().map(child -> child.getOutputMessages(severity)).forEach(result::addAll);
        return result;
    }

    public Map<OutputMessage.MessageSeverity, List<OutputMessage>> getOutputMessages() {
        return Map.of(
                OutputMessage.MessageSeverity.ERROR, getOutputMessages(OutputMessage.MessageSeverity.ERROR),
                OutputMessage.MessageSeverity.WARNING, getOutputMessages(OutputMessage.MessageSeverity.WARNING),
                OutputMessage.MessageSeverity.INFO, getOutputMessages(OutputMessage.MessageSeverity.INFO)
        );
    }

    @Override
    public ValidationResult merge(ValidationResult... others) {
        if (others == null) return this;

        try {
            // Process each ValidationResult individually
            for (ValidationResult other : others) {
                if (other == null) continue;

                if (!other.isEmpty()) {
                    this.addChild(other);
                }
            }
        } catch (Exception e) {
            // Catch any exception to prevent the application from crashing
            log.error("Error in ValidationResult.merge: {}", e.getMessage(), e);
        }

        return this;
    }

    @Override
    public ValidationResult empty() {
        return new ValidationResult();
    }

    @Override
    public ValidationResult addMessage(OutputMessage message) {
        // Convert OutputMessage to OutputMessage
        OutputMessage.MessageSeverity severity = convertSeverity(message.severity());
        Object element = message.element().length > 0 ? message.element()[0] : null;
        messages.add(new OutputMessage(message.message(), severity, element));
        return this;
    }

    private OutputMessage.MessageSeverity convertSeverity(OutputMessage.MessageSeverity severity) {
        return switch (severity) {
            case INFO -> OutputMessage.MessageSeverity.INFO;
            case WARNING -> OutputMessage.MessageSeverity.WARNING;
            case ERROR -> OutputMessage.MessageSeverity.ERROR;
        };
    }
}