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

package org.openvpms.web.component.im.print;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.IMObjectReportFactory;


/**
 * Prints reports for {@link IMObject}s generated by {@link IMObjectReport}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportPrinter extends AbstractIMObjectPrinter {

    /**
     * Constructs a new <code>IMObjectReportPrinter</code>.
     *
     * @param type display name for the types of objects that this may
     *             print
     */
    public IMObjectReportPrinter(String type) {
        super(type);
    }

    /**
     * Returns a document for an object.
     *
     * @param object the object
     * @return a document
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document getDocument(IMObject object) {
        String shortName = object.getArchetypeId().getShortName();
        String[] mimeTypes = {DocFormats.PDF_TYPE};
        // @todo - need to generate reports to ODT/RTF format in order to print
        IMObjectReport report = IMObjectReportFactory.create(
                shortName, mimeTypes,
                ArchetypeServiceHelper.getArchetypeService());
        return report.generate(object);
    }
}
