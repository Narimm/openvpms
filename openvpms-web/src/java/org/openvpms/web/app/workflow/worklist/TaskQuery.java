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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.worklist;

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.app.workflow.scheduling.ScheduleQuery;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Queries <em>act.customerTask</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskQuery extends ScheduleQuery {

    /**
     * Creates a new <tt>TaskQuery</tt>.
     */
    public TaskQuery() {
        super(ServiceHelper.getTaskService());
    }

    /**
     * Returns the schedule views.
     * <p/>
     * This returns the <em>entity.organisationWorkListView</em> entities for
     * the current location.
     *
     * @return the schedule views
     */
    protected List<Entity> getScheduleViews() {
        Party location = GlobalContext.getInstance().getLocation();
        List<Entity> views;
        if (location != null) {
            LocationRules locationRules = new LocationRules();
            views = locationRules.getWorkListViews(location);
        } else {
            views = Collections.emptyList();
        }
        return views;
    }

    /**
     * Returns the default schedule view.
     *
     * @return the default schedule view. May be <tt>null</tt>
     */
    protected Entity getDefaultScheduleView() {
        Party location = GlobalContext.getInstance().getLocation();
        if (location != null) {
            LocationRules locationRules = new LocationRules();
            return locationRules.getDefaultWorkListView(location);
        }
        return null;
    }

    /**
     * Returns the schedules for the specified schedule view.
     *
     * @param view the schedule view
     * @return the corresponding schedules
     */
    protected List<Entity> getSchedules(Entity view) {
        List<Entity> result = new ArrayList<Entity>();
        EntityBean bean = new EntityBean(view);
        List<EntityRelationship> relationships
                = bean.getNodeRelationships("workLists");
        Collections.sort(relationships, new Comparator<EntityRelationship>() {
            public int compare(EntityRelationship o1, EntityRelationship o2) {
                return o1.getSequence() - o2.getSequence();
            }
        });
        for (EntityRelationship relationship : relationships) {
            Entity schedule = (Entity) IMObjectHelper.getObject(
                    relationship.getTarget());
            if (schedule != null) {
                result.add(schedule);
            }
        }
        return result;
    }

    /**
     * Returns a display name for the schedule selector.
     *
     * @return a display name for the schedule selector
     */
    protected String getScheduleDisplayName() {
        return Messages.get("workflow.scheduling.query.worklist");
    }
}
