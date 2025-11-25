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

/**
 * Users module for IDORIS.
 * This module contains entity definitions, domain services, and business logic related to users.
 * It is responsible for managing users and their relationships.
 *
 * <p>The Users module depends on the core module for base abstractions and interfaces.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "IDORIS Users",
        allowedDependencies = {"core"}
)
package edu.kit.datamanager.idoris.users;