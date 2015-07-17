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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workflow.Workflow;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.workflow.checkin.CheckInWorkflow;
import org.openvpms.web.workspace.workflow.checkout.CheckOutWorkflow;
import org.openvpms.web.workspace.workflow.consult.ConsultWorkflow;


/**
 * Abstract implementation of {@link WorkflowFactory}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractWorkflowFactory implements WorkflowFactory {

    /**
     * Creates a check-in workflow.
     *
     * @param customer  the customer
     * @param patient   the patient
     * @param context   the external context to access and update
     * @param help      the help context
     */
    public Workflow createCheckInWorkflow(Party customer, Party patient, Context context, HelpContext help) {
        return new CheckInWorkflow(customer, patient, context, help);
    }

    /**
     * Creates a check-in workflow from an appointment.
     *
     * @param appointment the appointment
     * @param context     the external context to access and update
     * @param help        the help context
     */
    public Workflow createCheckInWorkflow(Act appointment, Context context, HelpContext help) {
        return new CheckInWorkflow(appointment, context, help);
    }

    /**
     * Creates a consult workflow from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act     the act
     * @param context the external context to access and update
     * @param help    the help context
     */
    public Workflow createConsultWorkflow(Act act, Context context, HelpContext help) {
        return new ConsultWorkflow(act, context, help);
    }

    /**
     * Creates a check-out workflow from an <em>act.customerAppointment</em> or <em>act.customerTask</em>.
     *
     * @param act     the act
     * @param context the external context to access and update
     * @param help    the help context
     */
    public Workflow createCheckOutWorkflow(Act act, Context context, HelpContext help) {
        return new CheckOutWorkflow(act, context, help);
    }

}
