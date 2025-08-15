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
 * Core module for IDORIS.
 * This module contains base abstractions, common interfaces, and cross-cutting concerns.
 * It also includes the event infrastructure for the event-driven architecture.
 *
 * <p>The core module is a foundational module that other modules depend on.
 * It should not depend on any other module to avoid circular dependencies.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "IDORIS Core",
        allowedDependencies = {}
)
package edu.kit.datamanager.idoris.core;