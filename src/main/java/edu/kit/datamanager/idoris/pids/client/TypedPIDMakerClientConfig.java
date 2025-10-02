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

package edu.kit.datamanager.idoris.pids.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.idoris.core.configuration.TypedPIDMakerConfig;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * Configuration class for the TypedPIDMakerClient.
 * This class registers the TypedPIDMakerClient as a bean in the Spring application context.
 */
@Configuration
@ConditionalOnBean(TypedPIDMakerConfig.class)
@Observed
@Slf4j
public class TypedPIDMakerClientConfig {

    /**
     * Creates a TypedPIDMakerClient bean using Spring HTTP Interface.
     *
     * @param config The TypedPIDMakerConfig
     * @return The TypedPIDMakerClient
     */
    @Bean
    @WithSpan(kind = SpanKind.CLIENT)
    @Timed(value = "typedPIDMakerClientConfig.createClient", description = "Time taken to create TypedPIDMakerClient", histogram = true)
    @Counted(value = "typedPIDMakerClientConfig.createClient.count", description = "Number of TypedPIDMakerClient creations")
    public TypedPIDMakerClient typedPIDMakerClient(TypedPIDMakerConfig config, ObjectMapper objectMapper) {
        log.info("Creating TypedPIDMakerClient with base URL: {}", config.getBaseUrl());
        // Create a client HTTP request factory with the configured timeout
        ClientHttpRequestFactory requestFactory = new DecodingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());

        RestClient restClient = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .requestInterceptor((request, body, execution) -> {
                    // Manual trace header injection
                    Span currentSpan = Span.current();
                    if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
                        SpanContext spanContext = currentSpan.getSpanContext();
                        String traceParent = String.format("00-%s-%s-%s",
                                spanContext.getTraceId(),
                                spanContext.getSpanId(),
                                spanContext.getTraceFlags().asHex());
                        request.getHeaders().add("traceparent", traceParent);

                        // Add tracestate if available
                        if (!spanContext.getTraceState().isEmpty()) {
                            request.getHeaders().add("tracestate", spanContext.getTraceState().toString());
                        }

                        currentSpan.setAttribute("request.method", request.getMethod().toString());
                        currentSpan.setAttribute("request.url", request.getURI().toString());
                        currentSpan.setAttribute("request.headers", request.getHeaders().toString());
                        currentSpan.setAttribute("request.body", new String(body));
                    }

                    log.debug("Outgoing request headers: {}", request.getHeaders());

                    // Execute the request and capture the response
                    ClientHttpResponse response = execution.execute(request, body);

                    // Add response attributes to the span
                    if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
                        try {
                            currentSpan.setAttribute("response.status_code", response.getStatusCode().value());
                            currentSpan.setAttribute("response.status_text", response.getStatusText());
                            currentSpan.setAttribute("response.headers", response.getHeaders().toString());

                            // Read the response body for span attributes
                            // Note: This creates a buffered response to avoid consuming the stream
                            byte[] bodyBytes = response.getBody().readAllBytes();
                            String responseBody = new String(bodyBytes, StandardCharsets.UTF_8);
                            currentSpan.setAttribute("response.body", responseBody);

                            log.debug("Response status: {}, headers: {}, body: {}",
                                    response.getStatusCode(), response.getHeaders(), responseBody);

                            // Return a new response with the buffered body
                            return new BufferedClientHttpResponse(response, bodyBytes);
                        } catch (IOException e) {
                            log.warn("Failed to read response body for span attributes", e);
                            currentSpan.setAttribute("response.status_code", response.getStatusCode().value());
                            currentSpan.setAttribute("response.status_text", response.getStatusText());
                            currentSpan.setAttribute("response.headers", response.getHeaders().toString());
                            currentSpan.setAttribute("response.body.error", "Failed to read response body: " + e.getMessage());
                        }
                    }

                    return response;

                })
                .requestFactory(requestFactory)
                .defaultHeaders(headers -> headers.setAccept(java.util.List.of(MediaType.parseMediaType("application/vnd.datamanager.pid.simple+json"))))
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new RestClientException(String.format("Error response from Typed PID Maker: %s %s %s", response.getStatusCode(), response.getStatusText(), response));
                })
                .messageConverters(converters -> {
                    // Add a custom converter for application/octet-stream to handle binary data
                    converters.add(new MappingJackson2HttpMessageConverter(objectMapper) {
                        @Override
                        protected boolean canRead(MediaType mediaType) {
                            return super.canRead(mediaType) ||
                                    MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(mediaType);
                        }
                    });
                })
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        TypedPIDMakerClient client = factory.createClient(TypedPIDMakerClient.class);
        log.info("Successfully created TypedPIDMakerClient for base URL: {}", config.getBaseUrl());
        return client;
    }

    /**
     * This record wraps a ClientHttpRequestFactory to decode slashes in URIs.
     * This is necessary because some PID services may encode slashes in URIs as %2F.
     *
     * @param delegate the original ClientHttpRequestFactory to delegate to.
     */
    private record DecodingClientHttpRequestFactory(
            ClientHttpRequestFactory delegate) implements ClientHttpRequestFactory {

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
            URI decodedUri = decodeSlashes(uri);
            return delegate.createRequest(decodedUri, httpMethod);
        }

        private URI decodeSlashes(URI uri) {
            String uriString = uri.toString();
            if (uriString.contains("%2F")) {
                String decodedUriString = uriString.replace("%2F", "/");
                try {
                    return new URI(decodedUriString);
                } catch (URISyntaxException e) {
                    // If decoding fails, return the original URI
                    return uri;
                }
            }
            return uri;
        }
    }

    /**
     * A wrapper for ClientHttpResponse that allows the body to be read multiple times.
     */
    private record BufferedClientHttpResponse(ClientHttpResponse delegate,
                                              byte[] bufferedBody) implements ClientHttpResponse {

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public void close() {
            delegate.close();
        }

        @Override
        public java.io.InputStream getBody() throws IOException {
            return new java.io.ByteArrayInputStream(bufferedBody);
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }
    }
}
