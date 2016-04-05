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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for <em>act.patientInvestigationVersion</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientInvestigationVersionActEditor extends PatientDocumentActEditor {

    /**
     * Constructs an {@link PatientInvestigationVersionActEditor}.
     *
     * @param act     the act
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PatientInvestigationVersionActEditor(DocumentAct act, Act parent, LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PatientInvestigationVersionActLayoutStrategy(getDocumentEditor(), isLocked());
    }
}
