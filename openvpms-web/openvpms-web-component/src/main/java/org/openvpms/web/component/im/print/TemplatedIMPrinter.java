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

package org.openvpms.web.component.im.print;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.PrintProperties;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.TemplatedReporter;


/**
 * Base class for printers that print reports using a template.
 *
 * @author Tim Anderson
 */
public abstract class TemplatedIMPrinter<T> extends AbstractIMPrinter<T> {

    /**
     * The context.
     */
    private final Context context;


    /**
     * Constructs a {@link TemplatedIMPrinter}.
     *
     * @param reporter the reporter
     * @param context  the context
     * @param service  the archetype service
     * @throws ArchetypeServiceException for any archetype service error
     */
    public TemplatedIMPrinter(TemplatedReporter<T> reporter, Context context, IArchetypeService service) {
        super(reporter, service);
        this.context = context;
        DocumentTemplate template = getTemplate();
        if (template != null) {
            setInteractive(getInteractive(template, getDefaultPrinter(), context));
            setCopies(template.getCopies());
        }
        setFields(ReportContextFactory.create(context));
    }

    /**
     * Returns a display name for the objects being printed.
     *
     * @return a display name for the objects being printed
     */
    public String getDisplayName() {
        return getReporter().getDisplayName();
    }

    /**
     * Returns the default printer for an object.
     *
     * @return the default printer, or {@code null} if there is none defined
     * @throws OpenVPMSException for any error
     */
    public String getDefaultPrinter() {
        String printer;
        DocumentTemplate template = getTemplate();
        if (template != null) {
            printer = getDefaultPrinter(template, context);
        } else {
            printer = getDefaultLocationPrinter(context.getLocation());
        }
        return printer;
    }

    /**
     * Returns the reporter.
     *
     * @return the reporter
     */
    @Override
    public TemplatedReporter<T> getReporter() {
        return (TemplatedReporter<T>) super.getReporter();
    }

    /**
     * Returns the print properties for an object.
     *
     * @param printer the printer
     * @return the print properties
     * @throws OpenVPMSException for any error
     */
    @Override
    protected PrintProperties getProperties(String printer) {
        return getProperties(printer, getTemplate(), context);
    }


    /**
     * Returns the document template.
     *
     * @return the document template, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected DocumentTemplate getTemplate() {
        return getReporter().getTemplate();
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }
}
