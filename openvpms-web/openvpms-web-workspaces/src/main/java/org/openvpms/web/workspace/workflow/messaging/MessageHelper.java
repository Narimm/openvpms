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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.messaging;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Message helper methods.
 *
 * @author Tim Anderson
 */
public class MessageHelper {

    /**
     * Returns all users in a list of users and groups.
     *
     * @param usersOrGroups the list of users and groups
     * @param service       the archetype service
     * @return the users in the list
     */
    public static Set<User> getUsers(List<Entity> usersOrGroups, IArchetypeService service) {
        Set<User> result = new HashSet<>();
        Set<Entity> groups = new HashSet<>();
        for (Entity entity : usersOrGroups) {
            if (entity instanceof User) {
                result.add((User) entity);
            } else if (entity != null) {
                if (!groups.contains(entity)) {
                    groups.add(entity);
                    List<User> users = getUsers(entity, service);
                    result.addAll(users);
                }
            }
        }
        return result;
    }

    /**
     * Returns all users in a group.
     *
     * @param group   the <em>entity.userGroup</em>.
     * @param service the archetype service
     * @return the users
     */
    public static List<User> getUsers(Entity group, IArchetypeService service) {
        EntityBean bean = new EntityBean(group, service);
        return bean.getNodeTargetObjects("users", User.class);
    }

}
