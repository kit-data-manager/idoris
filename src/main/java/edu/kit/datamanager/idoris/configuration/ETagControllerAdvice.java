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

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Controller advice for ETag support.
 * This advice adds ETag headers to responses that contain AdministrativeMetadata entities.
 */
@ControllerAdvice
public class ETagControllerAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Support all response types
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // Extract the entity from the response body
        Object entity = body;
        if (body instanceof EntityModel) {
            entity = ((EntityModel<?>) body).getContent();
        }

        // If the entity is an AdministrativeMetadata, add an ETag header
        if (entity instanceof AdministrativeMetadata metadata) {
            String etag = "\"" + metadata.getVersion() + "\"";
            response.getHeaders().set(HttpHeaders.ETAG, etag);

            // Store the entity in the request attributes for the ETag filter
            if (request instanceof ServletServerHttpRequest && response instanceof ServletServerHttpResponse) {
                HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
                servletRequest.setAttribute("entity", metadata);
            }

            // Check if this is a conditional request (If-Match header is present)
            String ifMatch = request.getHeaders().getFirst(HttpHeaders.IF_MATCH);
            if (ifMatch != null && !ifMatch.isEmpty()) {
                // If the ETag doesn't match, return 412 Precondition Failed
                if (!ifMatch.equals(etag) && !ifMatch.equals("*")) {
                    response.setStatusCode(HttpStatus.PRECONDITION_FAILED);
                    return null;
                }
            }
        }

        return body;
    }
}