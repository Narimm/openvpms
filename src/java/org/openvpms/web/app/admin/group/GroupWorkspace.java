/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.admin.group;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.app.subsystem.ResultSetCRUDWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.Archetypes;


/**
 * Group workspace.
 *
 * @author Tim Anderson
 */
public class GroupWorkspace extends ResultSetCRUDWorkspace<Entity> {

    /**
     * Constructs {@code GroupWorkspace}
     *
     * @param context the context
     */
    public GroupWorkspace(Context context) {
        super("admin", "group", context);
        setArchetypes(Archetypes.create("entity.userGroup", Entity.class));
    }

}
