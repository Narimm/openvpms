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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.Table;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableCellRenderer;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.util.Date;

/**
 * Cell renderer for appointments.
 *
 * @author Tim Anderson
 */
public abstract class AbstractAppointmentTableCellRender extends ScheduleTableCellRenderer {

    /**
     * Constructs a {@link AbstractAppointmentTableCellRender}.
     *
     * @param model the table model
     */
    public AbstractAppointmentTableCellRender(ScheduleTableModel model) {
        super(model);
    }


    /**
     * Helper to create a label indicating the reminder status of an appointment.
     *
     * @param reminderSent  the date a reminder was sent. May be {@code null}
     * @param reminderError the reminder error, if the reminder couldn't be sent. May be {@code null}
     * @return a new label
     */
    public static Label createReminderIcon(Date reminderSent, String reminderError) {
        String style;
        if (!StringUtils.isEmpty(reminderError)) {
            style = "AppointmentReminder.error";
        } else if (reminderSent != null) {
            style = "AppointmentReminder.sent";
        } else {
            style = "AppointmentReminder.unsent";
        }
        return LabelFactory.create(null, style);
    }

    /**
     * Returns a component representing an event.
     *
     * @param table  the table
     * @param event  the event
     * @param column the column
     * @param row    the row
     * @return the component
     */
    @Override
    protected Component getEvent(Table table, PropertySet event, int column, int row) {
        String text = evaluate(event);
        if (text == null) {
            String customer = event.getString(ScheduleEvent.CUSTOMER_NAME);
            String patient = event.getString(ScheduleEvent.PATIENT_NAME);
            String status = getModel().getStatus(event);
            String reason = event.getString(ScheduleEvent.ACT_REASON_NAME);
            if (reason == null) {
                // fall back to the code
                reason = event.getString(ScheduleEvent.ACT_REASON);
            }

            if (patient == null) {
                text = Messages.format("workflow.scheduling.appointment.table.customer",
                                       customer, reason, status);
            } else {
                text = Messages.format("workflow.scheduling.appointment.table.customerpatient",
                                       customer, patient, reason, status);
            }
        }

        String notes = event.getString(ScheduleEvent.ACT_DESCRIPTION);
        return createLabelWithNotes(text, notes, event.getBoolean(ScheduleEvent.SEND_REMINDER),
                                    event.getDate(ScheduleEvent.REMINDER_SENT),
                                    event.getString(ScheduleEvent.REMINDER_ERROR));
    }

    /**
     * Helper to create a multiline label with optional notes popup,
     * if the supplied notes are non-null and {@code displayNotes} is
     * {@code true}.
     *
     * @param text          the label text
     * @param notes         the notes. May be {@code null}
     * @param sendReminder  if {@code true}, a reminder should be sent for this appointment
     * @param reminderSent  the date a reminder was sent. May be {@code null}
     * @param reminderError the reminder error, if the reminder couldn't be sent. May be {@code null}
     * @return a component representing the label with optional popup
     */
    protected Component createLabelWithNotes(String text, String notes, boolean sendReminder, Date reminderSent,
                                             String reminderError) {
        Component result = createLabelWithNotes(text, notes);
        if (sendReminder || reminderSent != null || reminderError != null) {
            if (!(result instanceof Row)) {
                result = RowFactory.create(Styles.CELL_SPACING, result);
            }
            Label reminder = createReminderIcon(reminderSent, reminderError);
            reminder.setLayoutData(RowFactory.layout(new Alignment(Alignment.RIGHT, Alignment.TOP), Styles.FULL_WIDTH));
            result.add(reminder);
        }
        return result;
    }

}
