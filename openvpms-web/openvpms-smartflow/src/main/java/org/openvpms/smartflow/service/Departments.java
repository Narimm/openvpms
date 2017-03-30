/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.smartflow.service;

import org.openvpms.smartflow.model.Department;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Smart Flow Sheet departments API.
 *
 * @author benjamincharlton on 21/10/2015.
 */
@Path("/departments")
public interface Departments {

    /**
     * Returns the SFS departments.
     *
     * @return the the departments
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    List<Department> getDepartments();
}
