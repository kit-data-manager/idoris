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

package edu.kit.datamanager.idoris.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.kit.datamanager.idoris.rules.logic.RuleOutput;
import edu.kit.datamanager.idoris.rules.logic.Visitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.CompositeProperty;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
public abstract class VisitableElement implements edu.kit.datamanager.idoris.rules.logic.VisitableElement {
    private static final Logger LOG = LoggerFactory.getLogger(VisitableElement.class);

    @CompositeProperty
    @JsonIgnore
    private Map<String, Instant> visitedBy = new HashMap<>();

    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String internalId;

    public <T extends RuleOutput<T>> T execute(Visitor<T> visitor, Object... args) {
//        if (isVisitedBy(visitor)) {
//            LOG.info("Class {} already visited by visitor {}. ABORTING!", this.getClass().getName(), visitor.getClass().getName());
//            return null;
//        } else {
        LOG.info("Visiting class {} with visitor {}", this.getClass().getName(), visitor.getClass().getName());
        visitedBy.put(visitor.getClass().getName(), Instant.now());
        return accept(visitor, args);
//        }
    }

    protected abstract <T extends RuleOutput<T>> T accept(Visitor<T> visitor, Object... args);

    public Instant wasVisistedBy(Visitor<?> visitor) {
        return visitedBy.get(visitor.getClass().getName());
    }

    public void clearVisitedByAll() {
        visitedBy.clear();
    }

    public void clearVisitedBy(Visitor<?> visitor) {
        visitedBy.remove(visitor.getClass().getName());
    }

    public String getId() {
        return internalId;
    }
}