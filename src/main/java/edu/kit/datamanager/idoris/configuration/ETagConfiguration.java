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

package edu.kit.datamanager.idoris.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Set;

/**
 * Configuration for ETag support.
 * This configuration adds an ETag filter to the application that will generate ETags for responses
 * and validate If-Match headers for non-idempotent operations (PUT, PATCH, DELETE).
 */
@Configuration
public class ETagConfiguration {

    private static final Set<String> NON_IDEMPOTENT_METHODS = Set.of(
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name()
    );

    /**
     * Creates an ETag filter bean.
     *
     * @param handlerExceptionResolver the handler exception resolver
     * @return the ETag filter
     */
    @Bean
    public OncePerRequestFilter etagFilter(HandlerExceptionResolver handlerExceptionResolver) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {

                // Check if the request is for a non-idempotent operation
                if (NON_IDEMPOTENT_METHODS.contains(request.getMethod())) {
                    // Check if the If-Match header is present
                    String ifMatch = request.getHeader(HttpHeaders.IF_MATCH);
                    if (ifMatch == null || ifMatch.isEmpty()) {
                        response.setStatus(HttpStatus.PRECONDITION_REQUIRED.value());
                        response.getWriter().write("If-Match header is required for non-idempotent operations");
                        return;
                    }
                }

                // Continue with the filter chain
                filterChain.doFilter(request, response);
            }
        };
    }
}
