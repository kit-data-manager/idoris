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

package edu.kit.datamanager.idoris.core.domain.valueObjects;

/**
 * Value object representing an email address with validation.
 * Validates the email format and length upon instantiation.
 * Uses a regex pattern to ensure the email adheres to standard formats.
 * Note: This regex is a simplified version and may not cover all edge cases.
 */
public final class EmailAddress extends AbstractValueObject<String> {
    private static final String EMAIL_REGEX = "^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$";
    private final String email;

    /**
     * Constructor accepting an email string and validating it.
     *
     * @param email The email address to validate.
     * @throws IllegalArgumentException if the email is null, blank, too short, too long, or invalid.
     */
    public EmailAddress(String email) {
        super(email);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        email = email.trim();

        if (email.length() < 5) {
            throw new IllegalArgumentException("Email must be at least 5 characters long");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email cannot be longer than 255 characters");
        }

        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        this.email = email;
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EmailAddress that)) return false;

        return email.equals(that.email);
    }

    @Override
    public String toString() {
        return email;
    }
}
