/// *
// * Copyright (c) 2025 Karlsruhe Institute of Technology
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package edu.kit.datamanager.idoris.core.configuration.serializers;
//
//import com.fasterxml.jackson.databind.module.SimpleModule;
//import edu.kit.datamanager.idoris.core.domain.valueObjects.EmailAddress;
//import edu.kit.datamanager.idoris.core.domain.valueObjects.Name;
//import edu.kit.datamanager.idoris.core.domain.valueObjects.ORCiD;
//import edu.kit.datamanager.idoris.core.domain.valueObjects.PID;
//
//public class ValueObjectJacksonModule extends SimpleModule {
//    public ValueObjectJacksonModule() {
//        super("ValueObjectJacksonModule");
//
//        // Register your deserializer
//        addDeserializer(Name.class, new AbstractValueObjectDeserializer<>());
//
//        // Register your serializer
//        addSerializer(Name.class, new AbstractValueObjectSerializer());
//
//        addDeserializer(ORCiD.class, new AbstractValueObjectDeserializer<>());
//        addSerializer(ORCiD.class, new AbstractValueObjectSerializer());
//
//        addDeserializer(PID.class, new AbstractValueObjectDeserializer<>());
//        addSerializer(PID.class, new AbstractValueObjectSerializer());
//
//        addDeserializer(EmailAddress.class, new AbstractValueObjectDeserializer<>());
//        addSerializer(EmailAddress.class, new AbstractValueObjectSerializer());
//
//        // Repeat the process for other value object classes (e.g., Email)
//    }
//}
//
