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

package org.openvpms.report;

import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Exception class for exceptions raised by {@link IMObjectReport}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportException extends OpenVPMSException {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <code>IMObjectReportException</code>.
     *
     * @param message the error message
     */
    public IMObjectReportException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new <code>IMObjectReportException</code>.
     *
     * @param cause the cause
     */
    public IMObjectReportException(Throwable cause) {
        super(cause);
    }

}
