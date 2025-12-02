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

import lombok.extern.java.Log;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EntityScan
@EnableNeo4jRepositories
@EnableNeo4jAuditing
@EnableTransactionManagement
@EnableAspectJAutoProxy
// Scans for aspects in the current package and sub-packages (e.g. for PIISpanAttribute)
@ConfigurationPropertiesScan
@Log
@Modulithic(systemName = "IDORIS")
@EnableAsync
public class Application {
    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("\n---------------------------------");
        System.out.println("IDORIS started successfully.");
        System.out.println("---------------------------------\n");
    }

    @Bean
    Configuration cypherDslConfiguration() {
        return Configuration
                .newConfig()
                .withDialect(Dialect.NEO4J_5)
                .build();
    }
}
