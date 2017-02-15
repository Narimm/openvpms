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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addEmailTemplate;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addReminderCount;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.addSMSTemplate;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createDocumentTemplate;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailRule;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createEmailTemplate;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createExportRule;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createListRule;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createPrintRule;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createSMSRule;
import static org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper.createSMSTemplate;

/**
 * Tests the {@link ReminderProcessor}.
 *
 * @author Tim Anderson
 */
public class ReminderProcessorTestCase extends ArchetypeServiceTest {

    /**
     * Patient rules.
     */
    private PatientRules rules;

    /**
     * Reminder rules.
     */
    private ReminderRules reminderRules;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The reminder type.
     */
    private Entity reminderType;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        IArchetypeService service = getArchetypeService();
        rules = new PatientRules(new PracticeRules(service, null), service, getLookupService());
        reminderRules = new ReminderRules(service, rules);
        customer = TestHelper.createCustomer(new Contact[0]); // customer with no contacts
        patient = TestHelper.createPatient(customer);
        reminderType = ReminderTestHelper.createReminderType(1, DateUnits.MONTHS, 1, DateUnits.DAYS);
    }

    /**
     * Verifies that an <em>act.patientReminderItemEmail</em> is created when the rule specifies email, and the
     * customer has an email contact.
     */
    @Test
    public void testEmail() {
        Entity emailTemplate = createEmailTemplate("subject", "text");
        Entity smsTemplate = createSMSTemplate("TEXT", "text");
        Entity template = createDocumentTemplate(emailTemplate, smsTemplate);

        addReminderCount(reminderType, 0, 0, DateUnits.DAYS, template, createEmailRule());
        Act reminder = createReminderDueTomorrow();

        addContacts(createEmail());
        checkProcess(true, false, false, false, false, reminder);

        // with no email contact, email=true is ignored
        clearContacts();
        checkProcess(false, false, false, false, true, reminder);
    }

    /**
     * Verifies that if a rule specifies email and template has no email template, the reminder will be listed.
     */
    @Test
    public void testEmailForNoEmailTemplate() {
        Entity template = createDocumentTemplate();
        addReminderCount(reminderType, 0, 0, DateUnits.DAYS, template, createEmailRule());
        Act reminder = createReminderDueTomorrow();

        addContacts(createEmail());
        checkProcess(false, false, false, false, true, reminder);

        // now add an email template and verify an act.patientReminderItemEmail is created.
        addEmailTemplate(template, createEmailTemplate("subject", "text"));
        checkProcess(true, false, false, false, false, reminder);
    }

    /**
     * Verifies that an <em>act.patientReminderItemSMS</em> is created when the rule specifies SMS, and the
     * customer has an SMS contact.
     */
    @Test
    public void testSMS() {
        Entity emailTemplate = createEmailTemplate("subject", "text");
        Entity smsTemplate = createSMSTemplate("TEXT", "text");
        Entity template = createDocumentTemplate(emailTemplate, smsTemplate);

        addReminderCount(reminderType, 0, 0, DateUnits.DAYS, template, createSMSRule());
        Act reminder = createReminderDueTomorrow();

        addContacts(createPhone(true));
        checkProcess(false, true, false, false, false, reminder);

        // with no SMS contact, sms=true is ignored
        clearContacts();
        checkProcess(false, false, false, false, true, reminder);

        // with a phone contact , sms=true is ignored
        addContacts(createPhone(false));
        checkProcess(false, false, false, false, true, reminder);
    }

    /**
     * Verifies that if a rule specifies SMS and the template has no SMS template, the reminder will be listed.
     */
    @Test
    public void testSMSForNoSMSTemplate() {
        Entity template = createDocumentTemplate();
        addReminderCount(reminderType, 0, 0, DateUnits.DAYS, template, createSMSRule());
        Act reminder = createReminderDueTomorrow();

        addContacts(createPhone(true));
        checkProcess(false, false, false, false, true, reminder);

        // now add an SMS template and verify an act.patientReminderItemSMS is created.
        addSMSTemplate(template, createSMSTemplate("TEXT", "text"));
        checkProcess(false, true, false, false, false, reminder);
    }

    /**
     * Verifies that an <em>act.patientReminderItemSMS</em> is created when the rule specifies print, and the
     * customer has a location contact.
     */
    @Test
    public void testPrint() {
        Entity template = createDocumentTemplate();

        addReminderCount(reminderType, 0, 0, DateUnits.DAYS, template, createPrintRule());
        Act reminder = createReminderDueTomorrow();

        addContacts(createLocation());
        checkProcess(false, false, true, false, false, reminder);

        // with no location contact, print=true is ignored
        clearContacts();
        checkProcess(false, false, false, false, true, reminder);
    }

    /**
     * Verifies that if a rule specifies print and there is no template, the reminder will be listed.
     */
    @Test
    public void testPrintForNoPrintTemplate() {
        Entity count = addReminderCount(reminderType, 0, 0, DateUnits.DAYS, null, createPrintRule());
        Act reminder = createReminderDueTomorrow();

        addContacts(createLocation());
        checkProcess(false, false, false, false, true, reminder);

        // now add a template and verify an act.patientReminderItemPrint is created.
        IMObjectBean bean = new IMObjectBean(count);
        bean.addNodeTarget("template", createDocumentTemplate());
        bean.save();
        checkProcess(false, false, true, false, false, reminder);
    }

    /**
     * Verifies that an <em>act.patientReminderItemExport</em> is created when the rule specifies export, and the
     * customer has a location contact.
     */
    @Test
    public void testProcessExport() {
        Entity template = createDocumentTemplate();

        addReminderCount(reminderType, 0, 0, DateUnits.DAYS, template, createExportRule());
        Act reminder = createReminderDueTomorrow();

        addContacts(createLocation());
        checkProcess(false, false, false, true, false, reminder);

        // with no location contact, export=true is ignored
        clearContacts();
        checkProcess(false, false, false, false, true, reminder);
    }

    /**
     * Verifies that an <em>act.patientReminderItemExport</em> is created when the rule specifies list.
     */
    @Test
    public void testProcessList() {
        Entity template = createDocumentTemplate();

        addReminderCount(reminderType, 0, 0, DateUnits.DAYS, template, createListRule());
        Act reminder = createReminderDueTomorrow();

        // no contacts are required for list
        checkProcess(false, false, false, false, true, reminder);
    }

    /**
     * Verifies that a reminder type without a count is listed.
     */
    @Test
    public void testListForNoReminderCount() {
        Act reminder = createReminderDueTomorrow();
        checkProcess(false, false, false, false, true, reminder);
    }

    /**
     * Verifies that reminders whose due date are passed are cancelled.
     */
    @Test
    public void testCancelForPastDue() {
        Act reminder = createReminder(DateRules.getYesterday());
        assertTrue(reminderRules.shouldCancel(reminder, new Date()));

        List<Act> acts = process(reminder);
        assertEquals(1, acts.size());
        assertTrue(TypeHelper.isA(acts.get(0), ReminderArchetypes.REMINDER));
        assertEquals(ActStatus.CANCELLED, acts.get(0).getStatus());
    }

    /**
     * Verifies that reminders for deceased patients are set to cancel.
     */
    @Test
    public void testCancelForDeceased() {
        Act reminder = createReminderDueTomorrow();
        rules.setDeceased(patient);
        save(patient);

        addContacts(createLocation());
        List<Act> acts = process(reminder);
        assertEquals(1, acts.size());
        assertTrue(TypeHelper.isA(acts.get(0), ReminderArchetypes.REMINDER));
        assertEquals(ActStatus.CANCELLED, acts.get(0).getStatus());
    }

    /**
     * Adds contacts to the customer.
     *
     * @param contacts the contacts to add
     */
    private void addContacts(Contact... contacts) {
        for (Contact contact : contacts) {
            customer.addContact(contact);
        }
        save(customer);
    }

    /**
     * Removes all contacts from the customer.
     */
    private void clearContacts() {
        customer.getContacts().clear();
        save(customer);
    }

    /**
     * Creates a reminder.
     *
     * @param due the the due date
     * @return a new reminder
     */
    private Act createReminder(Date due) {
        return ReminderTestHelper.createReminder(due, patient, reminderType);
    }

    /**
     * Creates a reminder due tomorrow.
     *
     * @return a new reminder
     */
    private Act createReminderDueTomorrow() {
        return createReminder(DateRules.getTomorrow());
    }

    /**
     * Processes a reminder and verifies the result matches that expected.
     *
     * @param email    if {@code true} expect an <em>act.patientReminderItemEmail</em>
     * @param sms      if {@code true} expect an <em>act.patientReminderItemSMS</em>
     * @param print    if {@code true} expect an <em>act.patientReminderItemPrint</em>
     * @param export   if {@code true} expect an <em>act.patientReminderItemExport</em>
     * @param list     if {@code true} expect an <em>act.patientReminderItemList</em>
     * @param reminder the reminder to process
     */
    private void checkProcess(boolean email, boolean sms, boolean print, boolean export, boolean list,
                              Act reminder) {
        List<Act> acts = process(reminder);
        assertFalse(acts.isEmpty());
        save(acts);
        checkActs(acts, ReminderArchetypes.EMAIL_REMINDER, email);
        checkActs(acts, ReminderArchetypes.SMS_REMINDER, sms);
        checkActs(acts, ReminderArchetypes.PRINT_REMINDER, print);
        checkActs(acts, ReminderArchetypes.EXPORT_REMINDER, export);
        checkActs(acts, ReminderArchetypes.LIST_REMINDER, list);
    }

    /**
     * Verifies that the specified act exists/doesn't exist.
     *
     * @param acts the acts
     * @param shortName the act short name
     * @param exists if {@code true}, it must exist at most once in {@code acts}, otherwise it mustn't exist
     */
    private void checkActs(List<Act> acts, String shortName, boolean exists) {
        int found = 0;
        for (Act act : acts) {
            if (TypeHelper.isA(act, shortName)) {
                found++;
            }
        }
        if (exists) {
            if (found == 0) {
                fail(shortName + " not found");
            } else if (found > 1) {
                fail(shortName + " found more than once");
            }
        } else if (found != 0) {
            fail(shortName + " found");
        }
    }

    /**
     * Processes a reminder.
     *
     * @param reminder the reminder
     */
    private List<Act> process(Act reminder) {
        IMObject object = create(ReminderArchetypes.CONFIGURATION);
        ReminderConfiguration config = new ReminderConfiguration(object, getArchetypeService());
        ReminderProcessor processor = new ReminderProcessor(new Date(), config, false, getArchetypeService(), rules);
        return processor.process(reminder);
    }

    /**
     * Creates a location contact.
     *
     * @return a new contact
     */
    private Contact createLocation() {
        return TestHelper.createLocationContact("Foo", "ELTHAM", "VIC", "3095");
    }

    /**
     * Creates an email contact.
     *
     * @return a new contact
     */
    private Contact createEmail() {
        return TestHelper.createEmailContact("foo@bar.com");
    }

    /**
     * Creates a phone contact.
     *
     * @param sms if {@code true}, enables SMS messages
     * @return a new contact
     */
    private Contact createPhone(boolean sms) {
        return TestHelper.createPhoneContact("03", "1234566789", sms);
    }

}
