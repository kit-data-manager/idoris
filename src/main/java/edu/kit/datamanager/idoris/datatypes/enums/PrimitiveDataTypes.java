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

package edu.kit.datamanager.idoris.datatypes.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PrimitiveDataTypes {
    string("string", String.class),
    integer("integer", Integer.class),
    number("number", Number.class),
    bool("boolean", Boolean.class);

    private final String jsonName;
    private final Class<?> javaClass;

    public static PrimitiveDataTypes fromJsonName(String jsonName) {
        for (PrimitiveDataTypes type : values()) {
            if (type.getJsonName().equalsIgnoreCase(jsonName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown JSON name: " + jsonName);
    }

    public boolean isValueValid(Object value) {
        if (value == null) {
            return false;
        }
        return switch (this) {
            case string -> value instanceof String || !String.valueOf(value).isBlank();
            case integer -> value instanceof Integer || (value instanceof String string && string.matches("-?\\d+"));
            case number ->
                    value instanceof Number || (value instanceof String string && string.matches("-?\\d+(\\.\\d+)?"));
            case bool ->
                    value instanceof Boolean || (value instanceof String string && ("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)));
        };
    }
}