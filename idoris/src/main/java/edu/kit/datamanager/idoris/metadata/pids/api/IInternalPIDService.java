/*
 * Copyright (c) 2025-2026 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.metadata.pids.api;

import edu.kit.datamanager.idoris.core.domain.valueObjects.PID;
import org.springframework.hateoas.Link;
import org.springframework.modulith.NamedInterface;

import java.util.List;

/**
 * This API enables other endpoints to retrieve PIDs associated with entities in IDORIS and uniformly resolve them.
 *
 * @author maximiliani
 */
@NamedInterface
public interface IInternalPIDService {
    /**
     * This method makes a lookup for an internal ID and returns all PIDs pointing to this ID.
     *
     * @param internalId The internal ID of the entity that might have PIDs
     * @return All PIDs associated with this internal ID. If none are found, this list is empty.
     */
    List<PID> getPIDAssociatedWithInternalID(String internalId);

    /**
     * This method retrieves all PIDs for the internal ID and returns a list of HATEOAS links that resolve this PID.
     *
     * @param internalId The internal ID of the entity that might have PIDs
     * @return A list of HATEOAS links to the /pid endpoint of IDORIS, which will resolve and redirect to the domain entity.
     */
    List<Link> getPIDLinkForInternalID(String internalId);

    /**
     * This method converts a PID into a link to the /pid/{pid} endpoint of IDORIS
     *
     * @param pid A valid PID
     * @return A HATEOAS link to the /pid/{pid} endpoint, which resolves the PID and redirects the user to the domain entity.
     */
    Link getLinkForPID(PID pid);
}
