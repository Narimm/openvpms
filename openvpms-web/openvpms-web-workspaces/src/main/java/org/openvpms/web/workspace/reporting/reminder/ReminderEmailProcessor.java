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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessorException;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.IMObjectReporter;
import org.openvpms.web.component.im.report.ObjectSetReporter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.reporting.ReportingException;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.ReminderMissingDocTemplate;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.TemplateMissingEmailText;


/**
 * Sends reminder emails.
 *
 * @author Tim Anderson
 */
public class ReminderEmailProcessor extends AbstractReminderProcessor {

    /**
     * The mail sender.
     */
    private final JavaMailSender sender;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The default email address.
     */
    private final Email defaultAddress;

    /**
     * Email addresses keyed on practice/practice location reference.
     */
    private final Map<IMObjectReference, Email> addresses = new HashMap<IMObjectReference, Email>();


    /**
     * Constructs a {@code ReminderEmailProcessor}.
     *
     * @param sender        the mail sender
     * @param practice      the practice
     * @param groupTemplate the template for grouped reminders
     * @param context       the context
     */
    public ReminderEmailProcessor(JavaMailSender sender, Party practice, DocumentTemplate groupTemplate,
                                  Context context) {
        super(groupTemplate, context);
        this.sender = sender;
        handlers = ServiceHelper.getDocumentHandlers();

        ReminderRules rules = new ReminderRules(ServiceHelper.getArchetypeService(),
                                                ServiceHelper.getBean(PatientRules.class));
        defaultAddress = getEmail(practice, true, rules);

        PracticeRules practiceRules = ServiceHelper.getBean(PracticeRules.class);
        for (Party location : practiceRules.getLocations(practice)) {
            Email address = getEmail(location, false, rules);
            if (address != null) {
                addresses.put(location.getObjectReference(), address);
            }
        }
    }

    /**
     * Processes a list of reminder events.
     *
     * @param events           the events
     * @param shortName        the report archetype short name, used to select the document template if none specified
     * @param documentTemplate the document template to use. May be {@code null}
     */
    protected void process(List<ReminderEvent> events, String shortName, DocumentTemplate documentTemplate) {
        ReminderEvent event = events.get(0);
        Contact contact = event.getContact();
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(documentTemplate, shortName, getContext());
        documentTemplate = locator.getTemplate();
        if (documentTemplate == null) {
            throw new ReportingException(ReminderMissingDocTemplate);
        }

        try {
            Email from = getFromAddress(event.getCustomer());
            IMObjectBean bean = new IMObjectBean(contact);
            String to = bean.getString("emailAddress");

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setValidateAddresses(true);
            helper.setFrom(from.address, from.name);
            helper.setTo(to);

            String subject = documentTemplate.getEmailSubject();
            if (StringUtils.isEmpty(subject)) {
                subject = documentTemplate.getName();
            }
            String body = documentTemplate.getEmailText();
            if (StringUtils.isEmpty(body)) {
                throw new ReportingException(TemplateMissingEmailText, documentTemplate.getName());
            }
            helper.setText(body);

            final Document reminder = createReport(events, documentTemplate);
            final DocumentHandler handler = handlers.get(reminder.getName(), reminder.getArchetypeId().getShortName(),
                                                         reminder.getMimeType());

            helper.setSubject(subject);
            helper.addAttachment(reminder.getName(), new InputStreamSource() {
                public InputStream getInputStream() {
                    return handler.getContent(reminder);
                }
            });
            sender.send(message);
        } catch (ArchetypeServiceException exception) {
            throw exception;
        } catch (ReminderProcessorException exception) {
            throw exception;
        } catch (ReportingException exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new ReportingException(FailedToProcessReminder, exception, exception.getMessage());
        }
    }

    /**
     * Creates a new report.
     *
     * @param events           the reminder events
     * @param documentTemplate the document template
     * @return a new report
     */
    private Document createReport(List<ReminderEvent> events, DocumentTemplate documentTemplate) {
        Document result;
        if (events.size() > 1) {
            List<ObjectSet> sets = createObjectSets(events);
            ObjectSetReporter reporter = new ObjectSetReporter(sets, documentTemplate);
            result = reporter.getDocument(DocFormats.PDF_TYPE, true);
        } else {
            List<Act> acts = new ArrayList<Act>();
            for (ReminderEvent event : events) {
                acts.add(event.getReminder());
            }
            IMObjectReporter<Act> reporter = new IMObjectReporter<Act>(acts, documentTemplate);
            result = reporter.getDocument(DocFormats.PDF_TYPE, true);
        }
        return result;
    }

    /**
     * Returns the from address to use for a customer.
     * <p/>
     * If the customer has a practice location, any reminder
     *
     * @param customer the customer
     * @return the from address
     */
    private Email getFromAddress(Party customer) {
        IMObjectBean bean = new IMObjectBean(customer);
        IMObjectReference locationRef = bean.getNodeTargetObjectRef("location");
        Email result = addresses.get(locationRef);
        if (result == null) {
            result = defaultAddress;
        }
        return result;
    }

    /**
     * Returns the email address for a practice/practice location.
     *
     * @param practice the practice/practice location
     * @param fail     if {@code true} throw an exception if the address is invalid
     * @param rules    the rules
     * @return the address, or {@code null} if a valid address is not found and {@code fail} is {@code false}
     */
    private Email getEmail(Party practice, boolean fail, ReminderRules rules) {
        Contact contact = rules.getEmailContact(practice.getContacts());
        if (contact == null) {
            if (fail) {
                throw new ReportingException(ReportingException.ErrorCode.NoReminderContact, practice.getName());
            }
            return null;
        }
        IMObjectBean bean = new IMObjectBean(contact);
        String address = bean.getString("emailAddress");
        if (StringUtils.isEmpty(address)) {
            if (fail) {
                throw new ReportingException(ReportingException.ErrorCode.InvalidEmailAddress, address,
                                             practice.getName());
            }
            return null;
        }
        return new Email(address, practice.getName());
    }

    /**
     * Email address.
     */
    private static class Email {

        public final String address;

        public final String name;

        public Email(String address, String name) {
            this.address = address;
            this.name = name;
        }
    }

}
