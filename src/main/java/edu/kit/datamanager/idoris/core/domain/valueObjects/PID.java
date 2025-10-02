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

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.regex.Pattern;

/**
 * Value object representing a persistent identifier (PID) in the form of a handle.
 * A valid PID must match the pattern: <prefix>/<suffix>, where:
 * - <prefix> consists of alphanumeric characters and dots (e.g., "12345.6789")
 * - <suffix> consists of printable ASCII characters (from '!' to '~')
 * Note: This class does not verify the existence of the PID in any registry.
 */
public final class PID extends AbstractValueObject<String> {
    private static final String HANDLE_REGEX = "^([0-9A-Za-z]+(\\.[0-9A-Za-z]+)*)/([!-~]+)$";
    private final String prefix;
    private final String suffix;

    /**
     * Constructor accepting an unvalidated PID string.
     *
     * @param unvalidated The unvalidated PID string.
     * @throws IllegalArgumentException if the PID is null, blank, or invalid.
     */
    @JsonCreator
    public PID(String unvalidated) {
        super(unvalidated);
        if (unvalidated.isBlank()) {
            throw new IllegalArgumentException("PID cannot be null or blank.");
        }

        Pattern pattern = Pattern.compile(HANDLE_REGEX);
        var matcher = pattern.matcher(unvalidated);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("PID must be of the form <prefix>/<suffix> with valid characters.");
        }

        if (matcher.groupCount() < 2) {
            throw new IllegalArgumentException("PID must contain both a prefix and a suffix.");
        }

        // Split into prefix and suffix using the regex groups
        this.prefix = matcher.group(1); // The prefix part is the second capturing group
        this.suffix = matcher.group(3); // The suffix part is the last capturing group
    }

    /**
     * This method is only necessary for the neo4j OGM to work properly.
     * It returns a valid PID string.
     *
     * @return The validated PID string.
     */
    public String getUnvalidated() {
        return get();
    }

    @Override
    public int hashCode() {
        int result = getPrefix().hashCode();
        result = 31 * result + getSuffix().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PID pid)) return false;

        return getPrefix().equals(pid.getPrefix()) && getSuffix().equals(pid.getSuffix());
    }

    @Override
    public String toString() {
        return get();
    }

    /**
     * Returns the prefix part of the PID.
     *
     * @return The prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the suffix part of the PID.
     *
     * @return The suffix.
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Returns the full PID string in the format <prefix>/<suffix>.
     *
     * @return The full PID.
     */
    public String get() {
        return String.format("%s/%s", prefix, suffix);
    }
}
