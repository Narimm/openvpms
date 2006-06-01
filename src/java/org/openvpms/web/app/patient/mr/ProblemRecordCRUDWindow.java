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

package org.openvpms.web.app.patient.mr;

import static org.openvpms.web.app.patient.mr.PatientRecordTypes.*;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * CRUD Window for patient record acts in 'problem' view.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProblemRecordCRUDWindow extends PatientRecordCRUDWindow {

    /**
     * Clinical problem item short names.
     */
    private final String[] _clinicalProblemItems;


    /**
     * Create a new <code>ProblemRecordCRUDWindow</code>.
     */
    public ProblemRecordCRUDWindow() {
        super(new ProblemRecordShortNames());
        _clinicalProblemItems = ActHelper.getTargetShortNames(
                RELATIONSHIP_CLINICAL_PROBLEM_ITEM);
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        if (isNew) {
            // problem view
            if (IMObjectHelper.isA(object, _clinicalProblemItems)) {
                addActRelationship((Act) object, CLINICAL_PROBLEM,
                                   RELATIONSHIP_CLINICAL_PROBLEM_ITEM);
                addActRelationship((Act) object, CLINICAL_EVENT,
                                   RELATIONSHIP_CLINICAL_EVENT_ITEM);
            } else
            if (IMObjectHelper.isA(object, getClinicalEventItemShortNames())) {
                addActRelationship((Act) object, CLINICAL_EVENT,
                                   RELATIONSHIP_CLINICAL_EVENT_ITEM);
            }
        }
        super.onSaved(object, isNew);
    }

    /**
     * Deletes an object. Invokes {@link #onDeleted} if successful.
     *
     * @param object the object to delete
     */
    @Override
    protected void delete(IMObject object) {
        super.delete(object);
    }

}
