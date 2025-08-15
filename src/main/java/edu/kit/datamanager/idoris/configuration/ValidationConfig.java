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

package edu.kit.datamanager.idoris.configuration;

import edu.kit.datamanager.idoris.core.domain.VisitableElement;
import edu.kit.datamanager.idoris.rules.logic.OutputMessage;
import edu.kit.datamanager.idoris.rules.logic.RuleService;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;

import java.util.List;

/**
 * Configuration class for validation-related settings.
 * This class configures validators and validation-related beans.
 */
@Configuration
@Slf4j
public class ValidationConfig {

    /**
     * Creates a validator that uses the rule-based validation system.
     *
     * @param ruleService           the rule service
     * @param applicationProperties the application properties
     * @return a validator that uses the rule-based validation system
     */
    @Bean
    public Validator ruleBasedValidator(RuleService ruleService, ApplicationProperties applicationProperties) {
        return new RuleBasedVisitableElementValidator(ruleService, applicationProperties);
    }

    /**
     * Spring validator that uses the rule-based validation system.
     * This validator integrates with Spring's validation framework and executes
     * all applicable validation rules through the RuleService.
     */
    private static class RuleBasedVisitableElementValidator implements org.springframework.validation.Validator {
        private final RuleService ruleService;
        private final ApplicationProperties applicationProperties;

        public RuleBasedVisitableElementValidator(RuleService ruleService, ApplicationProperties applicationProperties) {
            this.ruleService = ruleService;
            this.applicationProperties = applicationProperties;
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return VisitableElement.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, org.springframework.validation.Errors errors) {
            if (!(target instanceof VisitableElement element)) {
                return;
            }

            try {
                // Execute validation rules using RuleService
                ValidationResult result = ruleService.executeRules(
                        RuleTask.VALIDATE,
                        element,
                        ValidationResult::new
                );

                // Convert ValidationResult to Spring validation errors
                convertToSpringErrors(result, errors);

            } catch (Exception e) {
                log.error("Error during rule-based validation: " + e.getMessage());
                errors.reject("validation.error", "Validation failed due to internal error");
            }
        }

        /**
         * Converts ValidationResult messages to Spring validation errors.
         * Only includes messages that meet the configured validation level threshold.
         */
        private void convertToSpringErrors(ValidationResult result, org.springframework.validation.Errors errors) {
            if (result == null || result.isEmpty()) {
                return;
            }

            result.getOutputMessages().entrySet().stream()
                    .filter(entry -> entry.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel()))
                    .forEach(entry -> {
                        OutputMessage.MessageSeverity severity = entry.getKey();
                        List<OutputMessage> messages = entry.getValue();

                        for (OutputMessage message : messages) {
                            String errorCode = "validation." + severity.name().toLowerCase();
                            String defaultMessage = message.message();

                            if (severity == OutputMessage.MessageSeverity.ERROR) {
                                errors.reject(errorCode, defaultMessage);
                            } else {
                                // For warnings and info, we can still add them but they won't fail validation
                                errors.reject("validation.warning", defaultMessage);
                            }
                        }
                    });
        }
    }
}
