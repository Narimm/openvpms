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

package org.openvpms.web.workspace.admin.archetype;

import org.openvpms.component.system.common.exception.OpenVPMSException;

/**
 * Exception raised by {@link ArchetypeEditor} exception.
 *
 * @author Tim Anderson
 */
public class ArchetypeEditorException extends OpenVPMSException {

    /**
     * Constructs an {@link ArchetypeEditorException}.
     *
     * @param msg the error message
     */
    public ArchetypeEditorException(String msg) {
        super(msg);
    }
}
