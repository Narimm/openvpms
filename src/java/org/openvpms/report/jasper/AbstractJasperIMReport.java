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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDefaultCompiler;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.fill.JREvaluator;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.ReportException;
import static org.openvpms.report.ReportException.ErrorCode.*;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrinterName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Abstract implementation of the {@link JasperIMReport} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractJasperIMReport<T> implements JasperIMReport<T> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Cached expression evaluator.
     */
    private JREvaluator evaluator;


    /**
     * Constructs a new <tt>AbstractJasperIMReport</tt>.
     *
     * @param service  the archetype service
     * @param handlers the document handlers
     */
    public AbstractJasperIMReport(IArchetypeService service,
                                  DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Returns the set of parameter types that may be supplied to the report.
     *
     * @return the parameter types
     * @throws ReportException if a parameter expression can't be evaluated
     */
    public Set<ParameterType> getParameterTypes() {
        Set<ParameterType> types = new LinkedHashSet<ParameterType>();
        JasperReport report = getReport();
        for (JRParameter p : report.getParameters()) {
            if (!p.isSystemDefined() && p.isForPrompting()) {
                JRExpression expression = p.getDefaultValueExpression();
                Object defaultValue = null;
                if (expression != null) {
                    try {
                        defaultValue = getEvaluator().evaluate(expression);
                    } catch (JRException exception) {
                        throw new ReportException(FailedToGetParameters,
                                                  exception);
                    }
                }
                ParameterType type = new ParameterType(p.getName(),
                                                       p.getValueClass(),
                                                       p.getDescription(),
                                                       defaultValue);
                types.add(type);
            }
        }
        return types;
    }

    /**
     * Generates a report.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @param mimeTypes  a list of mime-types, used to select the preferred
     *                   output format of the report
     * @return a document containing the report
     * @throws UnsupportedOperationException if this operation is not supported
     */
    public Document generate(Map<String, Object> parameters,
                             String[] mimeTypes) {
        Document document;
        String mimeType = getMimeType(mimeTypes);
        Map<String, Object> properties = getDefaultParameters();
        properties.putAll(parameters);
        try {
            JasperPrint print = JasperFillManager.fillReport(getReport(),
                                                             properties);
            document = convert(print, mimeType);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());

        }
        return document;
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects   the objects to report on
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document generate(Iterator<T> objects, String[] mimeTypes) {
        Document document;
        String mimeType = getMimeType(mimeTypes);
        try {
            JasperPrint print = report(objects);
            document = convert(print, mimeType);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        }
        return document;
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @param properties the print properties
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void print(Map<String, Object> parameters,
                      PrintProperties properties) {
        Map<String, Object> params = getDefaultParameters();
        params.putAll(parameters);
        try {
            JasperPrint print = JasperFillManager.fillReport(getReport(),
                                                             params);
            print(print, properties);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());

        }
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param properties the print properties
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void print(Iterator<T> objects, PrintProperties properties) {
        try {
            JasperPrint print = report(objects);
            print(print, properties);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        }
    }

    /**
     * Generates a report for an object.
     *
     * @param objects
     * @return the report
     * @throws JRException for any error
     */
    public JasperPrint report(Iterator<T> objects) throws JRException {
        JRDataSource source = createDataSource(objects);
        HashMap<String, Object> properties
                = new HashMap<String, Object>(getDefaultParameters());
        properties.put("dataSource", source);
        return JasperFillManager.fillReport(getReport(), properties, source);
    }

    /**
     * Creates a data source for a collection of objects.
     *
     * @param objects an iterator over the collection of objects
     * @return a new datas ource
     */
    protected abstract JRDataSource createDataSource(Iterator<T> objects);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the default report parameters to use when filling the report.
     *
     * @return the report parameters
     */
    protected Map<String, Object> getDefaultParameters() {
        return new HashMap<String, Object>();
    }

    /**
     * Converts a report to a document.
     *
     * @param report   the report to convert
     * @param mimeType the mime-type of the document
     * @return a document containing the report
     * @throws ReportException           for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document convert(JasperPrint report, String mimeType) {
        Document document;
        try {
            byte[] data;
            String ext;
            if (DocFormats.PDF_TYPE.equals(mimeType)) {
                data = JasperExportManager.exportReportToPdf(report);
                ext = DocFormats.PDF_EXT;
            } else {
                data = exportToRTF(report);
                ext = DocFormats.RTF_EXT;
            }
            String name = report.getName() + "." + ext;
            DocumentHandler handler = handlers.get(name, "document.other",
                                                   mimeType);
            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            document = handler.create(name, stream, mimeType, data.length);
        } catch (DocumentException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToGenerateReport,
                                      exception.getMessage());
        }
        return document;
    }

    /**
     * Exports a generated jasper report to an RTF stream.
     *
     * @param report the report
     * @return a new serialized RTF
     * @throws JRException if the export fails
     */
    protected byte[] exportToRTF(JasperPrint report) throws JRException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        JRRtfExporter exporter = new JRRtfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, report);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
        exporter.exportReport();
        return stream.toByteArray();
    }

    /**
     * Returns the preferred mime-type from a list of mime types.
     *
     * @param mimeTypes the mime types
     * @return the preferred mime type
     * @throws ReportException if none of the mime types are supported
     */
    private String getMimeType(String[] mimeTypes) {
        String mimeType = null;
        for (String type : mimeTypes) {
            if (DocFormats.PDF_TYPE.equals(type)
                    || DocFormats.RTF_TYPE.equals(type)) {
                mimeType = type;
                break;
            }
        }
        if (mimeType == null) {
            throw new ReportException(UnsupportedMimeTypes);
        }
        return mimeType;
    }

    /**
     * Prints a <tt>JasperPrint</tt> to a printer.
     *
     * @param print      the object to print
     * @param properties the print properties
     * @throws JRException for any error
     */
    private void print(JasperPrint print, PrintProperties properties)
            throws JRException {
        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setParameter(JRPrintServiceExporterParameter.JASPER_PRINT,
                              print);

        // print 1 copy
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(new Copies(1));
        MediaSizeName mediaSize = properties.getMediaSize();
        OrientationRequested orientation = properties.getOrientation();
        MediaTray tray = properties.getMediaTray();
        if (mediaSize != null) {
            aset.add(mediaSize);
        }
        if (orientation != null) {
            aset.add(orientation);
        }
        if (tray != null) {
            aset.add(tray);
        }
        exporter.setParameter(
                JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET,
                aset);

        // set the printer name
        PrintServiceAttributeSet serviceAttributeSet
                = new HashPrintServiceAttributeSet();
        serviceAttributeSet.add(
                new PrinterName(properties.getPrinterName(), null));
        exporter.setParameter(
                JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET,
                serviceAttributeSet);
        // print it
        exporter.exportReport();
    }

    /**
     * Returns the expression evaluator.
     *
     * @return the expression evaluator
     * @throws JRException if the evaluator can't be loaded
     */
    private JREvaluator getEvaluator() throws JRException {
        if (evaluator == null) {
            JRDefaultCompiler compiler = JRDefaultCompiler.getInstance();
            evaluator = compiler.loadEvaluator(getReport());
        }
        return evaluator;
    }
}
