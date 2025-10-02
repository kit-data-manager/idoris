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

import edu.kit.datamanager.idoris.core.domain.valueObjects.EmailAddress;
import edu.kit.datamanager.idoris.core.domain.valueObjects.Name;
import edu.kit.datamanager.idoris.core.domain.valueObjects.ORCiD;
import edu.kit.datamanager.idoris.core.domain.valueObjects.PID;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.value.StringValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class Neo4JConfiguration {
    @Bean
    public Neo4jConversions neo4jConversions() {
        return new Neo4jConversions(Set.of(
                new PIDConverter(),
                new ORCIDConverter(),
                new NameConverter(),
                new EmailConverter()
        ));
    }

    // Converter to handle conversion between PID and String
    public static class PIDConverter implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            Set<ConvertiblePair> convertiblePairs = new HashSet<>();
            convertiblePairs.add(new ConvertiblePair(PID.class, Value.class));
            convertiblePairs.add(new ConvertiblePair(Value.class, PID.class));
            return convertiblePairs;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (PID.class.isAssignableFrom(sourceType.getType())) {
                // convert to Neo4j Driver Value
                return new StringValue(source.toString());
            } else {
                Value value = (Value) source;
                // convert to MyCustomType
                return new PID(value.asString());
            }
        }

    }

    public static class ORCIDConverter implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            Set<ConvertiblePair> convertiblePairs = new HashSet<>();
            convertiblePairs.add(new ConvertiblePair(ORCiD.class, URL.class));
            convertiblePairs.add(new ConvertiblePair(URL.class, ORCiD.class));
            return convertiblePairs;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (ORCiD.class.isAssignableFrom(sourceType.getType())) {
                // convert to Neo4j Driver Value
                return new StringValue(source.toString());
            } else {
                // convert to MyCustomType
                return new ORCiD((String) source);
            }
        }

    }

    public static class NameConverter implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            Set<ConvertiblePair> convertiblePairs = new HashSet<>();
            convertiblePairs.add(new ConvertiblePair(Name.class, Value.class));
            convertiblePairs.add(new ConvertiblePair(Value.class, Name.class));
            return convertiblePairs;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (Name.class.isAssignableFrom(sourceType.getType())) {
                // convert to Neo4j Driver Value
                return new StringValue(source.toString());
            } else {
                // convert to MyCustomType
                Value value = (Value) source;
                return new Name(value.asString());
            }
        }
    }

    public static class EmailConverter implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            Set<ConvertiblePair> convertiblePairs = new HashSet<>();
            convertiblePairs.add(new ConvertiblePair(EmailAddress.class, Value.class));
            convertiblePairs.add(new ConvertiblePair(Value.class, EmailAddress.class));
            return convertiblePairs;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (EmailAddress.class.isAssignableFrom(sourceType.getType())) {
                // convert to Neo4j Driver Value
                return new StringValue(source.toString());
            } else {
                Value value = (Value) source;
                // convert to MyCustomType
                return new EmailAddress(value.asString());
            }
        }
    }
}
