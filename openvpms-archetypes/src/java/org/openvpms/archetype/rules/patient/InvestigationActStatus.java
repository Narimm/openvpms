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
package org.openvpms.archetype.rules.patient;

/**
 * Result status types for investigation acts.
 *
 * @author Tim Anderson
 */
public class InvestigationActStatus {

    /**
     * Pending result act status.
     */
    public static final String PENDING = "PENDING";

    /**
     * Sent result act status.
     */
    public static final String SENT = "SENT";

    /**
     * Preliminary result act status.
     */
    public static final String PRELIMINARY = "PRELIMINARY";

    /**
     * Final result act status.
     */
    public static final String FINAL = "FINAL";

}
