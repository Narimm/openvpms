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

package org.openvpms.booking.impl;

import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.booking.api.BookingService;
import org.openvpms.booking.domain.Booking;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the {@link BookingServiceImpl}.
 *
 * @author Tim Anderson
 */
public class BookingServiceImplTest extends ArchetypeServiceTest {

    /**
     * The customer rules.
     */
    @Autowired
    private CustomerRules customerRules;

    /**
     * The transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * The user rules.
     */
    @Autowired
    private UserRules userRules;

    /**
     * The test location.
     */
    private Party location;

    /**
     * The appointment type.
     */
    private Entity appointmentType;

    /**
     * The schedule.
     */
    private Entity schedule;

    /**
     * The appointment rules.
     */
    private AppointmentRules appointmentRules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        appointmentRules = new AppointmentRules(getArchetypeService());
        location = TestHelper.createLocation();
        appointmentType = ScheduleTestHelper.createAppointmentType();
        IMObjectBean appointmentTypeBean = new IMObjectBean(appointmentType);
        appointmentTypeBean.setValue("sendReminders", true);
        appointmentTypeBean.save();
        schedule = ScheduleTestHelper.createSchedule(15, DateUnits.MINUTES.toString(), 1, appointmentType, location);
        IMObjectBean scheduleBean = new IMObjectBean(schedule);
        scheduleBean.setValue("onlineBooking", true);
        scheduleBean.setValue("sendReminders", true);
        scheduleBean.save();
        configureSMSJob();
    }

    /**
     * Tests making a booking.
     */
    @Test
    public void testBooking() {
        Party customer = TestHelper.createCustomer();
        customer.addContact(TestHelper.createEmailContact("foo@bar.com"));
        Party patient = TestHelper.createPatient(customer, false);
        patient.setName("Fido");
        save(customer, patient);
        IMObjectBean customerBean = new IMObjectBean(customer);
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        Date startTime = DateRules.getTomorrow();
        Date endTime = DateRules.getDate(startTime, 1, DateUnits.MINUTES);
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName(customerBean.getString("firstName"));
        booking.setLastName(customerBean.getString("lastName"));
        booking.setEmail("foo@bar.com");
        booking.setPatientName(patient.getName());
        booking.setNotes("Some notes");

        Act appointment = createBooking(booking);
        checkAppointment(appointment, startTime, endTime, customer, patient, false, "Notes: " + booking.getNotes());
    }

    /**
     * Test booking for new customer.
     */
    @Test
    public void testBookingForNewCustomer() {
        Party onlineBookingCustomer = getOnlineBookingCustomer(true);
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        Date startTime = DateRules.getTomorrow();
        Date endTime = DateRules.getDate(startTime, 1, DateUnits.MINUTES);
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName("Foo");
        booking.setLastName("Bar" + System.currentTimeMillis());
        booking.setEmail("foo@bar.com");
        booking.setPatientName("Fido");

        Act appointment = createBooking(booking);
        String bookingNotes = "Title: Mr\n" +
                              "First Name: Foo\n" +
                              "Last Name: " + booking.getLastName() + "\n" +
                              "Email: foo@bar.com\n" +
                              "Patient: Fido";
        checkAppointment(appointment, startTime, endTime, onlineBookingCustomer, null, false, bookingNotes);
    }

    /**
     * Verifies that the send reminder flag is set if the appointment if the customer can receive SMS
     */
    @Test
    public void testSendReminder() {
        Party customer = TestHelper.createCustomer();
        String phoneNumber = "04123456789";
        customer.addContact(createSMSContact(phoneNumber));
        Party patient = TestHelper.createPatient(customer, false);
        patient.setName("Fido");
        save(customer, patient);
        IMObjectBean customerBean = new IMObjectBean(customer);
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        Date startTime = DateRules.getNextDate(DateRules.getTomorrow());
        Date endTime = DateRules.getDate(startTime, 1, DateUnits.MINUTES);
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName(customerBean.getString("firstName"));
        booking.setLastName(customerBean.getString("lastName"));
        booking.setMobile(phoneNumber);
        booking.setPatientName(patient.getName());

        Act appointment = createBooking(booking);
        checkAppointment(appointment, startTime, endTime, customer, patient, true, null);
    }

    /**
     * Verifies that a {@code BadRequestException} is thrown if no online customer is configured.
     */
    @Test
    public void testMissingOnlineBookingCustomer() {
        Party onlineBookingCustomer = getOnlineBookingCustomer(false);
        if (onlineBookingCustomer != null) {
            onlineBookingCustomer.setActive(false);
            save(onlineBookingCustomer);
        }
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        Date startTime = DateRules.getTomorrow();
        Date endTime = DateRules.getDate(startTime, 1, DateUnits.MINUTES);
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName("Foo");
        booking.setLastName("Bar");

        try {
            createBooking(booking);
            fail("Expected BadRequestException");
        } catch (BadRequestException exception) {
            assertEquals("There is no Online Booking customer configured to handle new customers",
                         exception.getMessage());
        }
    }

    /**
     * Tests booking cancellation.
     */
    @Test
    public void testCancel() {
        getOnlineBookingCustomer(true);
        Booking booking = new Booking();
        booking.setLocation(location.getId());
        booking.setSchedule(schedule.getId());
        booking.setAppointmentType(appointmentType.getId());
        Date startTime = DateRules.getTomorrow();
        Date endTime = DateRules.getDate(startTime, 1, DateUnits.MINUTES);
        booking.setStart(startTime);
        booking.setEnd(endTime);
        booking.setTitle("Mr");
        booking.setFirstName("Foo");
        booking.setLastName("Bar" + System.currentTimeMillis());
        booking.setEmail("foo@bar.com");
        booking.setPatientName("Fido");

        ArrayList<Act> acts = new ArrayList<>();
        BookingService service = createBookingService(acts);
        Response response1 = service.create(booking, createUriInfo());
        assertEquals(201, response1.getStatus());
        assertEquals(1, acts.size());
        Act appointment = acts.get(0);
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());

        String reference = (String) response1.getEntity();
        Response response2 = service.cancel(reference);
        assertEquals(204, response2.getStatus());

        appointment = get(appointment);
        assertEquals(ActStatus.CANCELLED, appointment.getStatus());

        try {
            service.cancel(reference);
            fail("Expected cancellation to fail");
        } catch (NotFoundException exception) {
            assertEquals("Booking not found", exception.getMessage());
        }
    }

    /**
     * Verifies an appointment matches that expected.
     *
     * @param appointment  the appointment to check
     * @param startTime    the expected start time
     * @param endTime      the expected end time
     * @param customer     the expected customer
     * @param patient      the expected patient
     * @param sendReminder the expected 'send reminder' flag
     * @param bookingNotes the expected booking notes
     */
    private void checkAppointment(Act appointment, Date startTime, Date endTime, Party customer, Party patient,
                                  boolean sendReminder, String bookingNotes) {
        ActBean bean = new ActBean(appointment);
        assertEquals(startTime, bean.getDate("startTime"));
        assertEquals(endTime, bean.getDate("endTime"));
        assertEquals(customer, bean.getNodeParticipant("customer"));
        assertEquals(patient, bean.getNodeParticipant("patient"));
        assertEquals(schedule, bean.getNodeParticipant("schedule"));
        assertEquals(appointmentType, bean.getNodeParticipant("appointmentType"));
        assertEquals(sendReminder, bean.getBoolean("sendReminder"));
        assertEquals(bookingNotes, bean.getString("bookingNotes"));
    }

    private void configureSMSJob() {
        IMObjectBean bean;
        ArchetypeQuery query = new ArchetypeQuery("entity.jobAppointmentReminder", true);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(query);
        if (!iterator.hasNext()) {
            bean = new IMObjectBean(create("entity.jobAppointmentReminder"));
            bean.addNodeTarget("runAs", TestHelper.createUser());
        } else {
            bean = new IMObjectBean(iterator.next());
        }
        bean.setValue("smsFrom", 3);
        bean.setValue("smsFromUnits", DateUnits.DAYS);
        bean.setValue("smsTo", 1);
        bean.setValue("smsToUnits", DateUnits.DAYS);
        bean.setValue("noReminder", 1);
        bean.setValue("noReminderUnits", DateUnits.DAYS);
        bean.save();
    }

    /**
     * Creates an SMS contact.
     *
     * @param phoneNumber the phone number
     * @return a new contact
     */
    private Contact createSMSContact(String phoneNumber) {
        Contact contact = TestHelper.createPhoneContact(null, phoneNumber);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("sms", true);
        return contact;
    }

    /**
     * Creates a booking.
     *
     * @param booking the booking
     * @return the corresponding appointment
     */
    private Act createBooking(Booking booking) {
        final List<Act> acts = new ArrayList<>();
        BookingService service = createBookingService(acts);
        Response response = service.create(booking, createUriInfo());
        assertEquals(201, response.getStatus());
        assertEquals(1, acts.size());
        return acts.get(0);
    }

    /**
     * Creates a booking service that collects appointments.
     *
     * @param acts the list to collect appointment acts
     * @return the booking service
     */
    private BookingService createBookingService(final List<Act> acts) {
        return new BookingServiceImpl(getArchetypeService(), customerRules, appointmentRules,
                                      userRules, transactionManager) {
            @Override
            protected void save(Act act, Entity schedule) {
                super.save(act, schedule);
                acts.add(act);
            }
        };
    }

    /**
     * Helper to create a {@code UriInfo}.
     *
     * @return a new {@code UriInfo}
     */
    private UriInfo createUriInfo() {
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        try {
            URI uri = new URI("http://localhost:8080/openvpms/ws/booking/v1/bookings");
            Mockito.when(uriInfo.getAbsolutePath()).thenReturn(uri);
            JerseyUriBuilder builder = new JerseyUriBuilder();
            builder.uri(uri);
            Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
        } catch (Exception exception) {
            fail(exception.getMessage());
        }
        return uriInfo;
    }

    /**
     * Returns the online booking customer.
     *
     * @param create if {@code true}, create the customer if it doesn't exist
     * @return the customer, or {@code null} if it doesn't exist
     */
    private Party getOnlineBookingCustomer(boolean create) {
        ArchetypeQuery query = new ArchetypeQuery(CustomerArchetypes.PERSON, true);
        query.add(Constraints.eq("name", "Online Booking,"));
        IMObjectQueryIterator<Party> iterator = new IMObjectQueryIterator<>(getArchetypeService(), query);
        Party customer = (iterator.hasNext()) ? iterator.next() : null;
        if (customer == null && create) {
            customer = TestHelper.createCustomer(null, "Online Booking", true);
        }
        return customer;
    }
}
