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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.IMObjectReportException;
import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToGenerateReport;

import java.util.HashMap;
import java.util.Collection;


/**
 * Abstract implementation of the {@link JasperIMObjectReport} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractJasperIMObjectReport extends AbstractJasperReport
        implements JasperIMObjectReport {


    /**
     * Constructs a new <code>AbstractJasperIMObjectReport</code>.
     *
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @param service   the archetype service
     * @throws IMObjectReportException if no mime-type is supported
     */
    public AbstractJasperIMObjectReport(String[] mimeTypes,
                                        IArchetypeService service) {
        super(mimeTypes, service);
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects the objects to report on
     * @return a document containing the report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document generate(Collection<IMObject> objects) {
        Document document;
        try {
            JasperPrint print = report(objects);
            document = convert(print);
        } catch (JRException exception) {
            throw new IMObjectReportException(exception, FailedToGenerateReport,
                                              exception.getMessage());
        }
        return document;
    }

    /**
     * Generates a report for an object.
     *
     * @param objects
     * @return the report
     * @throws JRException for any error
     */
    public JasperPrint report(Collection<IMObject> objects) throws JRException {
          IMObjectCollectionDataSource source
                = new IMObjectCollectionDataSource(objects,
                                                   getArchetypeService());
        HashMap<String, Object> properties
                = new HashMap<String, Object>(getParameters());
        properties.put("dataSource", source);
        return JasperFillManager.fillReport(getReport(), properties, source);
    }

}
