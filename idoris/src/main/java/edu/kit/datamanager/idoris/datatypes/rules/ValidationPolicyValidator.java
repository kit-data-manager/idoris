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

package edu.kit.datamanager.idoris.datatypes.rules;

import edu.kit.datamanager.idoris.core.domain.Attribute;
import edu.kit.datamanager.idoris.core.domain.TypeProfile;
import edu.kit.datamanager.idoris.core.domain.ValidationResult;
import edu.kit.datamanager.idoris.core.domain.ValidationVisitor;
import edu.kit.datamanager.idoris.core.domain.enums.CombinationOptions;
import edu.kit.datamanager.idoris.rules.logic.Rule;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.kit.datamanager.idoris.rules.logic.OutputMessage.MessageSeverity.ERROR;

/**
 * Rule-based validator that checks validation policy constraints for entities.
 * This validator ensures that validation policies between parent and child entities
 * are followed correctly.
 */
@Slf4j
@Observed(contextualName = "validationPolicyValidator")
@Rule(
        appliesTo = TypeProfile.class,
        name = "ValidationPolicyRule",
        description = "Validates that entities follow the validation policies defined by their parent entities",
        dependsOn = InheritanceValidator.class,
        tasks = RuleTask.VALIDATE
)
public class ValidationPolicyValidator extends ValidationVisitor {

    /**
     * Validates validation policy constraints for TypeProfile entities
     *
     * @param typeProfile The type profile to validate
     * @param args        Additional arguments (not used in this implementation)
     * @return ValidationResult containing any validation errors
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.validationPolicyValidator.visitTypeProfile", description = "Time to validate policy for TypeProfile", histogram = true)
    @Counted(value = "rules.validationPolicyValidator.visitTypeProfile.count", description = "Number of TypeProfile validation policy validations")
    public ValidationResult visit(TypeProfile typeProfile, Object... args) {
        ValidationResult result = new ValidationResult();

        if (typeProfile.getInheritsFrom() == null || typeProfile.getInheritsFrom().isEmpty()) {
            log.debug("TypeProfile {} has no parent TypeProfiles. Skipping validation.", typeProfile.getId());
            return result;
        }

        for (TypeProfile parent : typeProfile.getInheritsFrom()) {
            if (parent == null || parent.getValidationPolicy() == null) {
                log.debug("Parent TypeProfile is null or has no SubSchemaRelation defined. Skipping validation.");
                continue;
            }
            log.debug("Validating TypeProfile {} against parent TypeProfile {}", typeProfile.getId(), parent.getId());

            if (!parent.isAllowAdditionalAttributes() && !typeProfile.getAttributes().isEmpty())
                result.addMessage("TypeProfile " + typeProfile.getId() + " defines additional properties, but inherits from the TypeProfile " +
                                parent.getId() + " that denies additional properties.",
                        getTypeProfileAndParentElementaryInformation(typeProfile, parent,
                                Map.of("countOfAttributes", typeProfile.getAttributes().size())), rule, ERROR);

            switch (parent.getValidationPolicy()) {
                case ALL -> {
                    for (Attribute attribute : parent.getAttributes()) {
                        List<Attribute> undefinedAttributes = typeProfile.getAttributes().stream()
                                .filter(a -> java.util.Objects.equals(a.getDataTypeId(), attribute.getDataTypeId()))
                                .toList();

                        if (!undefinedAttributes.isEmpty()) {
                            result.addMessage("TypeProfile " + typeProfile.getId() + " does not define all properties defined in the TypeProfile " +
                                            parent.getId() + " that requires all properties.",
                                    getTypeProfileAndParentElementaryInformation(typeProfile, parent,
                                            Map.of("numberOfUndefinedAttributes", undefinedAttributes.size(),
                                                    "undefinedAttributes", undefinedAttributes)), rule, ERROR);
                        }
                    }
                }
                case ANY -> {
                    if (typeProfile.getAttributes().stream().noneMatch(a -> parent.getAttributes().stream()
                            .anyMatch(pa -> java.util.Objects.equals(pa.getDataTypeId(), a.getDataTypeId())))) {
                        result.addMessage("TypeProfile " + typeProfile.getId() + " does not define any property defined in the TypeProfile " +
                                        parent.getId() + " that requires at least one property.",
                                getTypeProfileAndParentElementaryInformation(typeProfile, parent, null), rule, ERROR);
                    }
                }
                case ONE -> {
                    if (typeProfile.getAttributes().stream().filter(a -> parent.getAttributes().stream()
                            .anyMatch(pa -> java.util.Objects.equals(pa.getDataTypeId(), a.getDataTypeId()))).count() != 1) {
                        result.addMessage("TypeProfile " + typeProfile.getId() + " does not define exactly one property defined in the TypeProfile " +
                                        parent.getId() + " that requires exactly one property.",
                                getTypeProfileAndParentElementaryInformation(typeProfile, parent, null), rule, ERROR);
                    }
                }
                case NONE -> {
                    List<Attribute> illegallyDefinedAttributes = typeProfile.getAttributes().stream()
                            .filter(a -> parent.getAttributes().stream().anyMatch(pa -> java.util.Objects.equals(pa.getDataTypeId(), a.getDataTypeId())))
                            .toList();

                    if (!illegallyDefinedAttributes.isEmpty()) {
                        result.addMessage("TypeProfile " + typeProfile.getId() + " defines a property defined in the TypeProfile " +
                                        parent.getId() + " that requires no property.",
                                getTypeProfileAndParentElementaryInformation(typeProfile, parent,
                                        Map.of("numberOfIllegallyDefinedAttributes", illegallyDefinedAttributes.size(),
                                                "illegallyDefinedAttributes", illegallyDefinedAttributes)), rule, ERROR);
                    }
                }
                default -> throw new IllegalStateException("Unknown ValidationPolicy " + parent.getValidationPolicy());
            }
        }
        log.debug("Validation of TypeProfile {} against parent TypeProfiles completed. result={}", typeProfile.getId(), result);
        return result;
    }

    /**
     * Helper method to create information about type profiles for error messages
     *
     * @param typeProfile      The type profile being validated
     * @param parent           The parent type profile
     * @param otherInformation Additional information to include
     * @return Map containing elementary information about the type profiles
     */
    @WithSpan(kind = SpanKind.INTERNAL)
    @Timed(value = "rules.validationPolicyValidator.getElementaryInformation", description = "Time to build elementary information for validation message", histogram = true)
    @Counted(value = "rules.validationPolicyValidator.getElementaryInformation.count", description = "Number of elementary information builds")
    private Map<String, Object> getTypeProfileAndParentElementaryInformation(TypeProfile typeProfile, TypeProfile parent, Map<String, Object> otherInformation) {
        Map<String, Object> result = new HashMap<>();
        result.put("this", new ElementaryInformation(typeProfile.getId(), typeProfile.getName().toString(), typeProfile.getValidationPolicy()));
        result.put("parent", new ElementaryInformation(parent.getId(), parent.getName().toString(), parent.getValidationPolicy()));
        result.put("otherInformation", otherInformation);
        return result;
    }

    /**
     * Record for storing elementary information about a type profile
     */
    private record ElementaryInformation(String pid, String name, CombinationOptions validationPolicy) {
    }
}
