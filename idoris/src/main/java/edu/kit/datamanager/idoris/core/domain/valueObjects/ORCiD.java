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

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Value object representing a validated ORCiD.
 * Ensures the ORCiD is in the correct format and has a valid check digit.
 * Note: This class does not verify the existence of the ORCiD in the ORCiD registry.
 */
@Slf4j
public final class ORCiD extends AbstractValueObject<String> {
    private static final String ORCID_REGEX = "^(\\d{4}-\\d{4}-\\d{4}-[(\\d{3}\\dX)(\\d{4})])$";
    private static final String ORCID_URL_REGEX = "^(https?://)?((.+\\.)?orcid\\.org)/(\\d{4}-\\d{4}-\\d{4}-[(\\d{3}\\dX)(\\d{4})])$";

    private final URL orcid;

    /**
     * Constructor accepting an unvalidated URL.
     *
     * @param unvalidated The unvalidated ORCiD URL.
     * @throws IllegalArgumentException if the URL is null or invalid.
     */
    public ORCiD(URL unvalidated) {
        super(unvalidated.toString());
        if (unvalidated == null) {
            throw new IllegalArgumentException("ORCiD URL cannot be null");
        }
        this.orcid = this.getValidORCiDURL(unvalidated.getHost(), unvalidated.getPath());
    }

    /**
     * Validates the ORCiD components and constructs a URL.
     *
     * @param host The host part of the ORCiD URL.
     * @param path The path part of the ORCiD URL.
     * @return A validated ORCiD URL.
     * @throws IllegalArgumentException if validation fails.
     */
    private URL getValidORCiDURL(String host, String path) {
        String orcidId = validate(host, path);
        URI uri = URI.create(String.format("https://%s/%s", host, orcidId));
        try {
            return uri.toURL();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create ORCiD URL", e);
        }
    }

    /**
     * Validates the ORCiD format and check digit.
     *
     * @param host The ORCiD registry used (e.g., orcid.org or sandbox.orcid.org). Must end with orcid.org.
     * @param path The ORCiD identifier path (e.g., 0000-0002-1825-0097).
     * @return The validated ORCiD identifier.
     * @throws IllegalArgumentException if validation fails.
     */
    private String validate(String host, String path) {
        if (host == null || host.isBlank() || !host.endsWith("orcid.org")) {
            throw new IllegalArgumentException("ORCiD URL must have a valid host ending with orcid.org");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("ORCiD URL must have a valid path");
        }
        String orcidId = path.startsWith("/") ? path.substring(1) : path;
        if (!orcidId.matches(ORCID_REGEX)) {
            throw new IllegalArgumentException("Invalid ORCiD format");
        }

        String[] parts = orcidId.split("-");
        String baseDigits = String.join("", parts).substring(0, 15);
        char expectedCheckDigit = generateCheckDigit(baseDigits.toCharArray());
        char actualCheckDigit = parts[3].substring(3).charAt(0);
        if (expectedCheckDigit != actualCheckDigit) {
            throw new IllegalArgumentException("Invalid ORCiD check digit");
        }

        return orcidId;
    }

    /**
     * Generates the check digit for the given base digits using the ISO 7064 Mod 11-2 algorithm.
     * Provided in the ORCiD specification: <a href="https://support.orcid.org/hc/en-us/articles/360006897674-Structure-of-the-ORCID-Identifier">Structure of the ORCID Identifier</a>
     *
     * @param baseDigits The first 15 digits of the ORCiD identifier. Without dashes and check digit.
     * @return The calculated check digit as a char ("0"-"9" or "X").
     */
    private Character generateCheckDigit(char[] baseDigits) {
        if (baseDigits == null || baseDigits.length != 15) {
            throw new IllegalArgumentException("Base digits must be exactly 15 characters long");
        }

        int total = 0;
        for (int i = 0; i < 15; i++) {
            if (baseDigits[i] < '0' || baseDigits[i] > '9') {
                throw new IllegalArgumentException("Base digits must be numeric");
            }
            int digit = Character.getNumericValue(baseDigits[i]);
            total = (total + digit) * 2;
        }
        int remainder = total % 11;
        int result = (12 - remainder) % 11;
        return result == 10 ? 'X' : (char) result;
    }

    /**
     * Constructor accepting an unvalidated string.
     * The string can be either a full ORCiD URL or just the ORCiD ID.
     *
     * @param unvalidatedInput The unvalidated ORCiD input string.
     * @throws IllegalArgumentException if the input is invalid.
     */
//    @JsonCreator
    public ORCiD(String unvalidatedInput) {
        super(unvalidatedInput);
        if (unvalidatedInput == null || unvalidatedInput.isBlank()) {
            throw new IllegalArgumentException("ORCiD input cannot be null or blank");
        }

        // Use the grouping in the regex to extract host and path
        Pattern pattern = Pattern.compile(ORCID_URL_REGEX);
        var matcher = pattern.matcher(unvalidatedInput);
        if (matcher.matches()) {
            // If a full URL is provided, extract host and path
            String host = matcher.group(2);
            String path = matcher.group(4);
            this.orcid = this.getValidORCiDURL(host, path);
        } else if (unvalidatedInput.matches(ORCID_REGEX)) {
            // If only the ORCiD ID is provided, construct the full URL using the default host
            String host = "orcid.org";
            this.orcid = this.getValidORCiDURL(host, unvalidatedInput);
        } else {
            throw new IllegalArgumentException("Invalid ORCiD input format: Either full URL or ORCiD ID string must be provided");
        }
    }

    /**
     * Returns the validated ORCiD URL.
     * e.g., https://orcid.org/0000-0002-1825-0097
     *
     * @return The ORCiD URL.
     */
    public URL get() {
        return orcid;
    }

    /**
     * Returns the ORCiD identifier without the URL part.
     *
     * @return The ORCiD ID string (e.g., 0000-0002-1825-0097).
     */
    public String getORCiDIDSubstring() {
        String path = orcid.getPath();
        return path.startsWith("/") ? path.substring(1) : path;
    }

    @Override
    public int hashCode() {
        return orcid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ORCiD orCiD)) return false;

        return orcid.equals(orCiD.orcid);
    }

    @Override
//    @JsonValue
    public String toString() {
        return orcid.toExternalForm();
    }

}
