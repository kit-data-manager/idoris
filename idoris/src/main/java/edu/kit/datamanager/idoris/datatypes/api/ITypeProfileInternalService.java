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
package edu.kit.datamanager.idoris.datatypes.api;

/**
 * Internal API for TypeProfile operations (narrow, inter-module).
 */
public interface ITypeProfileInternalService {
    void ensureExists(String id);

    // Internal link/unlink (no DTO loading)
    void addInheritsFromInternal(String profileId, java.util.Set<String> parentIds);

    void removeInheritsFromInternal(String profileId, java.util.Set<String> parentIds);

    void addAttributesInternal(String profileId, java.util.Set<String> attributeIds);

    void removeAttributesInternal(String profileId, java.util.Set<String> attributeIds);
}