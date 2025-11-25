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

package edu.kit.datamanager.idoris.core.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for web-related settings.
 * This class configures CORS, HATEOAS, and other web-related settings.
 */
@Configuration
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL, EnableHypermediaSupport.HypermediaType.HAL_FORMS, EnableHypermediaSupport.HypermediaType.COLLECTION_JSON})
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${idoris.security.enable-auth:false}")
    private boolean enableAuth;
    @Value("${idoris.security.enable-csrf:true}")
    private boolean enableCsrf;
    @Value("${idoris.security.allowedOriginPattern:http*://localhost:[*]}")
    private String allowedOriginPattern;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();

//        http.authorizeHttpRequests(authorize -> authorize
//                        // everyone, even unauthenticated users may do HTTP OPTIONS on urls or access swagger
//                        .requestMatchers(HttpMethod.OPTIONS, "/**", "/swagger-ui.html", "/swagger-ui/*", "/v3/**").permitAll()
//                        // permit access to actuator endpoints
//                        .requestMatchers("/actuator/**").permitAll()
//                        // TODO protect the actual API
//                        .requestMatchers("/api/v1/**").permitAll())
//                // do not store sessions (use stateless "sessions")
//                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
////                .addFilterAfter(keycloaktokenFilterBean(), BasicAuthenticationFilter.class)
//                .headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable))
//                .csrf(csrf -> {
//                    if (!enableCsrf) {
//                        log.info("Disable CSRF");
//                        // https://developer.mozilla.org/en-US/docs/Glossary/CSRF
//                        csrf.disable();
//                    }
//                });
//
//
//        if (!enableAuth) {
//            log.info("Authentication is DISABLED. Adding 'NoAuthenticationFilter' to authentication chain.");
//            AuthenticationManager defaultAuthenticationManager = http.getSharedObject(AuthenticationManager.class);
////            http.addFilterAfter(new NoAuthenticationFilter(jwtSecret, defaultAuthenticationManager), KeycloakTokenFilter.class); TODO
//        } else {
//            log.info("Authentication is ENABLED.");
//        }
//
//        return http.build();
    }

    /**
     * Configures CORS settings.
     *
     * @return the WebMvcConfigurer with CORS configuration
     */
    @Bean
    public WebSecurityCustomizer webSecurity() {
        return web -> web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        // might be necessary for certain identifier types.
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(false);
        corsConfig.addAllowedOriginPattern(allowedOriginPattern);
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addExposedHeader("Content-Range");
        corsConfig.addExposedHeader("ETag");
        corsConfig.addExposedHeader("Last-Modified");
        corsConfig.addExposedHeader("Expires");
        corsConfig.addExposedHeader("Cache-Control");
        corsConfig.addExposedHeader("Trace-ID");

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/swagger-ui.html");
    }
}
