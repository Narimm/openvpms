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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.Date;


/**
 * Appointment grid.
 *
 * @author Tim Anderson
 */
public interface AppointmentGrid extends ScheduleEventGrid {

    /**
     * Returns the no. of minutes from midnight that the grid starts at.
     *
     * @return the minutes from midnight that the grid starts at
     */
    int getStartMins();

    /**
     * Returns the no. of minutes from midnight that the grid ends at.
     *
     * @return the minutes from midnight that the grid ends at
     */
    int getEndMins();

    /**
     * Returns the size of each slot, in minutes.
     *
     * @return the slot size, in minutes
     */
    int getSlotSize();

    /**
     * Returns the no. of slots that an event occupies, from the specified slot.
     * <p/>
     * If the event begins prior to the slot, the remaining slots will be returned.
     *
     * @param event the event
     * @param slot  the starting slot
     * @return the no. of slots that the event occupies
     */
    int getSlots(PropertySet event, int slot);

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param slot the slot
     * @return the start time of the specified slot
     */
    Date getStartTime(int slot);

    /**
     * Returns the no. of minutes from midnight that the specified slot starts
     * at.
     *
     * @param slot the slot
     * @return the minutes that the slot starts at
     */
    int getStartMins(int slot);

    /**
     * Returns the hour of the specified slot.
     *
     * @param slot the slot
     * @return the hour, in the range 0..23
     */
    int getHour(int slot);

    /**
     * Returns the first slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the first slot that minutes intersects, or {@code -1} if no slots intersect
     */
    int getFirstSlot(int minutes);

    /**
     * Returns the last slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the last slot that minutes intersects, or {@code -1} if no slots intersect
     */
    int getLastSlot(int minutes);

    /**
     * Returns the slot that a time falls in.
     *
     * @param time the time
     * @return the slot, or {@code -1} if the time doesn't intersect any slot
     */
    int getSlot(Date time);
}
