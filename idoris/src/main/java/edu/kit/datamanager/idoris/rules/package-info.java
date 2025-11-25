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
 * Rules module for IDORIS.
 * This module contains entity definitions, domain services, and business logic related to rules processing.
 * It is responsible for managing rules and rule execution.
 *
 * <p>The Rules module depends on the core module for base abstractions and attributes for entity access.</p>
 */

@ApplicationModule(
        displayName = "IDORIS Rules",
        allowedDependencies = {"core"},
        type = ApplicationModule.Type.OPEN
)
package edu.kit.datamanager.idoris.rules;

import org.springframework.modulith.ApplicationModule;