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

package edu.kit.datamanager.idoris;

import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import lombok.extern.java.Log;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.modulith.Modulith;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableNeo4jRepositories
@EnableNeo4jAuditing
@EnableTransactionManagement
@EntityScan("edu.kit.datamanager")
@org.springframework.context.annotation.Configuration
@Log
@Modulith
public class IdorisApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdorisApplication.class, args);
        System.out.println();
        System.out.println("---------------------------------");
        System.out.println("IDORIS started successfully.");
        System.out.println("---------------------------------");
    }

    @Bean
    @ConfigurationProperties("repo")
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

    @Bean
    Configuration cypherDslConfiguration() {
        return Configuration
                .newConfig()
                .withDialect(Dialect.NEO4J_5)
                .build();
    }

    // Unregister the OpenTelemetryMeterRegistry from Metrics.globalRegistry and make it available
    // as a Spring bean instead.
//    @Bean
//    @ConditionalOnClass(name = "io.opentelemetry.javaagent.OpenTelemetryAgent")
//    public MeterRegistry otelRegistry() {
//        Optional<MeterRegistry> otelRegistry = Metrics.globalRegistry.getRegistries().stream()
//                .filter(r -> r.getClass().getName().contains("OpenTelemetryMeterRegistry"))
//                .findAny();
//        otelRegistry.ifPresent(Metrics.globalRegistry::remove);
//        return otelRegistry.orElse(null);
//    }
//    @Bean
//    public MeterRegistry getMeterRegistry() {
//        return new CompositeMeterRegistry();
//    }
}
