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

public final class Name extends AbstractValueObject<String> {
    private final String name;

    public Name(String name) {
        super(name);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        // Allow harmless characters, remove others
        name = name.replaceAll("[^\\p{L}\\p{N} .,\\-_#@'&:;€$£¥()]", "");

        name = name.trim();

        if (name.length() < 3) {
            throw new IllegalArgumentException("Name must be at least 3 characters long");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Name cannot be longer than 255 characters");
        }

        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Name name1)) return false;

        return name.equals(name1.name);
    }

    @Override
    public String toString() {
        return name;
    }
}