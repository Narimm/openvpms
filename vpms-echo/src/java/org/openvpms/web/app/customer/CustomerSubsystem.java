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

package org.openvpms.web.app.customer;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Customer subsystem.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CustomerSubsystem extends AbstractSubsystem {

    /**
     * Construct a new <code>CustomerSubsystem</code>.
     */
    public CustomerSubsystem() {
        super("customer");
        addWorkspace(new InformationWorkspace());
        addWorkspace(new DummyWorkspace("customer", "document"));
        addWorkspace(new EstimationWorkspace());
        addWorkspace(new InvoiceWorkspace());
        addWorkspace(new DummyWorkspace("customer", "payment"));
        addWorkspace(new AccountWorkspace());
    }
}
