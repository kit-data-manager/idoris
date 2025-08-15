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

package edu.kit.datamanager.idoris.pids.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Simple PID Record in the Typed PID Maker service.
 * This follows the SimplePidRecord structure from the Typed PID Maker API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@With
public record PIDRecord(String pid, List<PIDRecordEntry> record) {

    /**
     * Constructs a PIDRecord with the given PID and an empty record.
     * If the PID is null, it defaults to an empty string.
     *
     * @param pid The PID of the record
     */
    public PIDRecord(String pid) {
        this(pid, new ArrayList<>());
    }

    /**
     * Constructs a PIDRecord with an empty PID and the given record entries.
     * If the record is null, it initializes an empty list.
     *
     * @param record The list of PIDRecordEntry entries
     */
    public PIDRecord(List<PIDRecordEntry> record) {
        this("", Objects.requireNonNullElseGet(record, ArrayList::new));
    }

    /**
     * Constructs a PIDRecord with the given PID and record entries.
     * If the PID is null, it defaults to an empty string.
     * If the record is null, it initializes an empty list.
     *
     * @param pid    The PID of the record
     * @param record The list of PIDRecordEntry entries
     */
    public PIDRecord(String pid, List<PIDRecordEntry> record) {
        this.pid = Objects.requireNonNullElse(pid, "");
        this.record = Objects.requireNonNullElseGet(record, ArrayList::new);
    }
}
