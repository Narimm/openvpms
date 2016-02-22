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

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderExporter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.List;


/**
 * Processor that exports reminders to CSV.
 *
 * @author Tim Anderson
 */
public class ReminderExportProcessor extends AbstractReminderBatchProcessor {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The logger.
     */
    private final ReminderCommunicationLogger logger;


    /**
     * Constructs a {@link ReminderExportProcessor}.
     *
     * @param reminders  the reminders
     * @param statistics the reminder statistics
     * @param context    the context
     * @param logger     if specified, log exported reminders
     */
    public ReminderExportProcessor(List<List<ReminderEvent>> reminders, Statistics statistics, Context context,
                                   ReminderCommunicationLogger logger) {
        super(reminders, statistics);
        this.context = context;
        this.logger = logger;
    }

    /**
     * The processor title.
     *
     * @return the processor title
     */
    public String getTitle() {
        return Messages.get("reporting.reminder.run.export");
    }

    /**
     * Processes the batch.
     */
    public void process() {
        List<ReminderEvent> reminders = getReminders();
        if (!reminders.isEmpty()) {
            try {
                ReminderExporter exporter = ServiceHelper.getBean(ReminderExporter.class);
                Document document = exporter.export(reminders);
                DownloadServlet.startDownload(document);
                updateReminders();
                notifyCompleted();
            } catch (OpenVPMSException exception) {
                notifyError(exception);
            }
        } else {
            notifyCompleted();
        }
    }

    /**
     * Restarts processing.
     */
    public void restart() {
        // no-op
    }

    /**
     * Updates a reminder.
     *
     * @param reminder the reminder to update
     * @param date     the last-sent date
     * @return {@code true} if the reminder was updated
     */
    @Override
    protected boolean updateReminder(ReminderEvent reminder, Date date) {
        boolean updated = super.updateReminder(reminder, date);
        if (updated && logger != null) {
            logger.logExport(reminder, context.getLocation());
        }
        return updated;
    }

    /**
     * Notifies the listener (if any) of processing completion.
     */
    @Override
    protected void notifyCompleted() {
        setStatus(Messages.get("reporting.reminder.export.status.end"));
        super.notifyCompleted();
    }

    /**
     * Invoked if an error occurs processing the batch.
     * <p/>
     * Sets the error message on each reminder, and notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    protected void notifyError(Throwable exception) {
        setStatus(Messages.get("reporting.reminder.export.status.failed"));
        super.notifyError(exception);
    }

}
