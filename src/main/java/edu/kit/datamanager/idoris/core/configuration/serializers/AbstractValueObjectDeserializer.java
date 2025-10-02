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
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import edu.kit.datamanager.idoris.core.domain.valueObjects.AbstractValueObject;
//
//import java.io.IOException;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//
//public class AbstractValueObjectDeserializer<T extends AbstractValueObject<?>> extends JsonDeserializer<T> {
//
//    @Override
//    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//        String json = p.getText().trim();
//
//        // Get the class of this deserializer (ValuedObjectDeserializer)
//        Class<?> clazz = getClass().getGenericInterfaces()[0].getClass();
//
//        // Find a constructor in the concrete subclass that accepts a single String argument
//        Constructor<T> constructor;
//        try {
//            constructor = (Constructor<T>) clazz.getConstructor(String.class);
//        } catch (NoSuchMethodException e) {
//            throw new IllegalStateException("No suitable constructor found", e);
//        }
//
//        // Instantiate and return the concrete subclass
//        try {
//            return constructor.newInstance(json);
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//            throw new IllegalStateException("Failed to create instance of " + clazz, e);
//        }
//    }
//}
