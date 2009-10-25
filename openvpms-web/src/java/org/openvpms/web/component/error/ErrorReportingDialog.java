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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.error;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.InformationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.context.ApplicationContext;


/**
 * An {@link ErrorDialog} that provides the option to report errors back to OpenVPMS.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ErrorReportingDialog extends ErrorDialog {

    /**
     * The error reporter.
     */
    private ErrorReporter reporter;

    /**
     * The error report.
     */
    private ErrorReport report;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(ErrorReportingDialog.class);


    /**
     * Construct a new <tt>ErrorReportingDialog</tt>.
     *
     * @param message   the error message
     * @param exception the exception to display
     */
    public ErrorReportingDialog(String message, Throwable exception) {
        super(Messages.get("errordialog.title"), message, OK);
        ApplicationContext context = ServiceHelper.getContext();
        if (context.containsBean("errorReporter")) {
            reporter = (ErrorReporter) context.getBean("errorReporter");
            report = new ErrorReport(message, exception);
            if (reporter.isReportable(exception)) {
                addButton("errorreportdialog.report", new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        reportError();
                    }
                });
            }
        }
    }

    /**
     * Helper to show a new error reporting dialog.
     *
     * @param message   dialog message
     * @param exception the cause
     */
    public static void show(String message, Throwable exception) {
        ErrorDialog dialog = new ErrorReportingDialog(message, exception);
        dialog.show();
    }

    /**
     * Pops up a dialog to report the error.
     */
    private void reportError() {
        SendReportDialog dialog = new SendReportDialog();
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doReport();
            }

            @Override
            public void onCancel() {
                doCancel();
            }
        });
        dialog.show();
    }

    /**
     * Sends the report and closes the dialog.
     */
    private void doReport() {
        reporter.report(report);
        onOK();
    }

    /**
     * Closes the dialog without sending the report.
     */
    private void doCancel() {
        onCancel();
    }

    /**
     * Shows the report in a new dialog.
     */
    private void showReport() {
        try {
            String xml = report.toXML();
            InformationDialog.show(Messages.get("errorreportdialog.showtitle"), xml);
        } catch (Throwable exception) {
            log.error(exception, exception);
            ErrorDialog.show(exception);
        }
    }

    /**
     * Confirmation dialog that prompts to send the report.
     */
    private class SendReportDialog extends ConfirmationDialog {

        /**
         * Constructs a new <tt>SendReportDialog</tt>.
         */
        public SendReportDialog() {
            super(Messages.get("errorreportdialog.title"),
                  Messages.get("errorreportdialog.message"), new String[0]);
            addButton("errorreportdialog.send", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onOK();
                }
            });
            addButton("errorreportdialog.nosend", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            });
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label message = LabelFactory.create();
            message.setText(getMessage());
            Button show = ButtonFactory.create("errorreportdialog.showlink", "hyperlink", new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showReport();
                }
            });
            Label content = LabelFactory.create("errorreportdialog.show");
            Column column = ColumnFactory.create(
                    "Inset", ColumnFactory.create("CellSpacing", message,
                                                  RowFactory.create("CellSpacing", content, show)));
            getLayout().add(column);
        }

    }

}
