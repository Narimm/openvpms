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

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.openoffice.OpenOfficeHelper;
import org.openvpms.web.component.im.print.TemplatedIMPrinter;
import org.openvpms.web.component.im.report.DocumentActReporter;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * A printer for {@link DocumentAct}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActPrinter extends TemplatedIMPrinter<IMObject> {

    /**
     * Constructs a new <tt>DocumentActPrinter</tt>.
     *
     * @param object the object to print
     * @throws DocumentException if the object doesn't have any
     *                           <em>participation.documentTemplate</em>
     *                           participation
     */
    public DocumentActPrinter(DocumentAct object) {
        super(new DocumentActReporter(object));
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be <code>null</code>
     * @throws OpenVPMSException for any error
     */
    @Override
    public void print(String printer) {
        DocumentAct act = (DocumentAct) getObject();
        Document doc = (Document) IMObjectHelper.getObject(
                act.getDocReference());
        if (doc == null) {
            super.print(printer);
        } else if (DocFormats.ODT_TYPE.equals(doc.getMimeType())) {
            OpenOfficeHelper.getPrintService().print(doc, printer);
        } else {
            download(doc);
        }
    }

}
