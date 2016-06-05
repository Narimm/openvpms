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

package org.openvpms.archetype.rules.workflow;

import org.joda.time.DateMidnight;
import org.joda.time.Days;
import org.joda.time.Period;
import org.openvpms.archetype.rules.act.DefaultActCopyHandler;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.IterableIMObjectQuery;
import org.openvpms.component.system.common.query.NodeSortConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Appointment rules.
 *
 * @author Tim Anderson
 */
public class AppointmentRules {

    /**
     * Appointment reminder job configuration.
     */
    protected static final String APPOINTMENT_REMINDER_JOB = "entity.jobAppointmentReminder";

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Constructs an {@link AppointmentRules}.
     *
     * @param service the archetype service
     */
    public AppointmentRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the <em>party.organisationSchedule</em>s associated with a
     * <em>entity.organisationScheduleView</em>, in order of their relationship
     * sequence.
     *
     * @param scheduleView the schedule view
     * @return the schedules associated with the view
     */
    public List<Party> getSchedules(Entity scheduleView) {
        List<Party> result = new ArrayList<>();
        EntityBean bean = new EntityBean(scheduleView, service);
        List<EntityRelationship> relationships = bean.getValues("schedules", EntityRelationship.class);
        Collections.sort(relationships, SequenceComparator.INSTANCE);
        for (EntityRelationship relationship : relationships) {
            if (relationship.getTarget() != null) {
                Party schedule = (Party) service.get(relationship.getTarget());
                if (schedule != null) {
                    result.add(schedule);
                }
            }
        }
        return result;
    }

    /**
     * Returns the first view that contain the specified schedule for a given practice location.
     *
     * @param location the practice location
     * @param schedule the schedule
     * @return the view, or {@code null} if none is found
     */
    public Entity getScheduleView(Party location, Entity schedule) {
        EntityBean bean = new EntityBean(location, service);
        for (Entity view : bean.getNodeTargetEntities("scheduleViews", SequenceComparator.INSTANCE)) {
            IMObjectBean viewBean = new IMObjectBean(view, service);
            if (viewBean.hasNodeTarget("schedules", schedule)) {
                return view;
            }
        }
        return null;
    }

    /**
     * Returns the practice location associated with a schedule.
     *
     * @param schedule the schedule
     * @return the location, or {@code null} if none is found
     */
    public Party getLocation(Entity schedule) {
        IMObjectBean bean = new IMObjectBean(schedule, service);
        return (Party) bean.getNodeTargetObject("location");
    }

    /**
     * Returns the schedule slot size in minutes.
     *
     * @param schedule the schedule
     * @return the schedule slot size in minutes
     * @throws OpenVPMSException for any error
     */
    public int getSlotSize(Entity schedule) {
        IMObjectBean bean = new IMObjectBean(schedule, service);
        return getSlotSize(bean);
    }

    /**
     * Returns the default appointment type associated with a schedule.
     *
     * @param schedule the schedule
     * @return the default appointment type, or the the first appointment type
     * if there is no default, or {@code null} if none is found
     * @throws OpenVPMSException for any error
     */
    public Entity getDefaultAppointmentType(Entity schedule) {
        return EntityRelationshipHelper.getDefaultTarget(schedule, "appointmentTypes", service);
    }

    /**
     * Calculates an appointment end time, given the start time, schedule and
     * appointment type.
     *
     * @param startTime       the start time
     * @param schedule        an instance of <em>party.organisationSchedule</em>
     * @param appointmentType an instance of <em>entity.appointmentType</em>
     * @return the appointment end time
     * @throws OpenVPMSException for any error
     */
    public Date calculateEndTime(Date startTime, Entity schedule, Entity appointmentType) {
        EntityBean schedBean = new EntityBean(schedule, service);
        int noSlots = getSlots(schedBean, appointmentType);
        int minutes = getSlotSize(schedBean) * noSlots;
        return DateRules.getDate(startTime, minutes, DateUnits.MINUTES);
    }

    /**
     * Updates any <em>act.customerTask</em> associated with an
     * <em>act.customerAppointment</em> to ensure that it has the same status
     * (where applicable).
     *
     * @param act the appointment
     * @throws OpenVPMSException for any error
     */
    public void updateTask(Act act) {
        ActBean bean = new ActBean(act, service);
        List<Act> tasks = bean.getNodeActs("tasks");
        if (!tasks.isEmpty()) {
            Act task = tasks.get(0);
            updateStatus(act, task);
        }
    }

    /**
     * Updates any <em>act.customerAppointment</em> associated with an
     * <em>act.customerTask</em> to ensure that it has the same status
     * (where applicable).
     *
     * @param act the task
     * @throws OpenVPMSException for any error
     */
    public void updateAppointment(Act act) {
        ActBean bean = new ActBean(act, service);
        List<Act> appointments = bean.getNodeActs("appointments");
        if (!appointments.isEmpty()) {
            Act appointment = appointments.get(0);
            updateStatus(act, appointment);
        }
    }

    /**
     * Determines if a patient has an active appointment.
     *
     * @param patient the patient
     * @return an active appointment, or {@code null} if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Act getActiveAppointment(Party patient) {
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT, true, true);
        query.add(Constraints.join("patient").add(Constraints.eq("entity", patient.getObjectReference())));
        query.add(Constraints.not(Constraints.in("status", AppointmentStatus.PENDING, AppointmentStatus.CANCELLED,
                                                 AppointmentStatus.POSTED)));
        query.add(new NodeSortConstraint("startTime", false));
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iter = new IMObjectQueryIterator<>(service, query);
        return (iter.hasNext()) ? iter.next() : null;
    }

    /**
     * Copies an appointment, excluding any act relationships it may have.
     *
     * @param appointment the appointment to copy
     * @return a copy of the appointment
     */
    public Act copy(Act appointment) {
        IMObjectCopyHandler handler = new DefaultActCopyHandler() {
            {
                setCopy(Act.class, Participation.class);
                setExclude(ActRelationship.class);
            }

            @Override
            protected boolean checkCopyable(ArchetypeDescriptor archetype, NodeDescriptor node) {
                return true;
            }
        };
        IMObjectCopier copier = new IMObjectCopier(handler, service);
        return (Act) copier.apply(appointment).get(0);
    }

    /**
     * Determines if automatic reminders are enabled for a schedule or appointment type.
     *
     * @param entity the schedule or appointment type. May be {@code null}
     * @return {@code true} if reminders are enabled
     */
    public boolean isRemindersEnabled(Entity entity) {
        if (entity != null) {
            IMObjectBean bean = new IMObjectBean(entity, service);
            return bean.getBoolean("sendReminders");
        }
        return false;
    }

    /**
     * Returns the period from the current time when no appointment reminder should be sent.
     *
     * @return the period, or {@code null} if appointment reminders haven't been configured
     */
    public Period getNoReminderPeriod() {
        Period result = null;
        IMObject object = getAppointmentReminderJob();
        if (object != null) {
            IMObjectBean bean = new IMObjectBean(object, service);
            int period = bean.getInt("noReminder");
            DateUnits units = DateUnits.fromString(bean.getString("noReminderUnits"));
            if (period > 0 && units != null) {
                result = units.toPeriod(period);
            }
        }
        return result;
    }

    /**
     * Determines if an appointment is a boarding appointment.
     *
     * @param appointment the appointment
     * @return {@code true} if the appointment is a boarding appointment
     */
    public boolean isBoardingAppointment(Act appointment) {
        boolean result = false;
        ActBean bean = new ActBean(appointment, service);
        Entity schedule = bean.getNodeParticipant("schedule");
        if (schedule != null) {
            IMObjectBean scheduleBean = new IMObjectBean(schedule, service);
            if (scheduleBean.getNodeTargetObjectRef("cageType") != null) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the number of days to charge boarding for an appointment.
     *
     * @param appointment the appointment
     * @return the number of days
     */
    public int getBoardingDays(Act appointment) {
        return getBoardingDays(appointment.getActivityStartTime(), appointment.getActivityEndTime());
    }

    /**
     * Returns the number of days to charge boarding for.
     *
     * @param startTime the boarding start time
     * @param endTime   the boarding end time
     * @return the number of days
     */
    public int getBoardingDays(Date startTime, Date endTime) {
        DateMidnight end = new DateMidnight(endTime);
        int days = Days.daysBetween(new DateMidnight(startTime), end).getDays();
        if (days < 0) {
            days = 0;
        } else if (DateRules.compareTo(endTime, end.toDate()) != 0) {
            // any time after midnight is another day
            days++;
        }
        return days;
    }

    /**
     * Returns the number of nights to charge boarding for.
     *
     * @param startTime the boarding start time
     * @param endTime   the boarding end time
     * @return the number of nights
     */
    public int getBoardingNights(Date startTime, Date endTime) {
        int days = getBoardingDays(startTime, endTime);
        return days > 1 ? days - 1 : days;
    }

    /**
     * Returns the patient clinical event associated with an appointment.
     *
     * @param appointment the appointment
     * @return the event, or {@code null} if none exists
     */
    public Act getEvent(Act appointment) {
        ActBean bean = new ActBean(appointment, service);
        return (Act) bean.getNodeTargetObject("event");
    }

    /**
     * Returns pending appointments for a customer.
     *
     * @param customer the customer
     * @param interval the interval, relative to the current date/time
     * @param units    the interval units
     * @return the pending appointments for the customer
     */
    public Iterable<Act> getCustomerAppointments(Party customer, int interval, DateUnits units) {
        ArchetypeQuery query = createAppointmentQuery(customer, "customer", interval, units);
        return new IterableIMObjectQuery<>(service, query);
    }

    /**
     * Returns pending appointments for a patient.
     *
     * @param patient  the patient
     * @param interval the interval, relative to the current date/time
     * @param units    the interval units
     * @return the pending appointments for the customer
     */
    public Iterable<Act> getPatientAppointments(Party patient, int interval, DateUnits units) {
        ArchetypeQuery query = createAppointmentQuery(patient, "patient", interval, units);
        return new IterableIMObjectQuery<>(service, query);
    }

    /**
     * Creates a pending appointment query for a party.
     *
     * @param party    a customer or patient
     * @param node     the customer/patient node
     * @param interval the interval, relative to the current date/time
     * @param units    the interval units
     * @return a new query
     */
    protected ArchetypeQuery createAppointmentQuery(Party party, String node, int interval, DateUnits units) {
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT);
        query.add(Constraints.join(node).add(Constraints.eq("entity", party)));
        Date from = new Date();
        Date to = DateRules.getDate(from, interval, units);
        query.add(Constraints.gte("startTime", from));
        query.add(Constraints.lt("startTime", to));
        query.add(Constraints.eq("status", AppointmentStatus.PENDING));
        query.add(Constraints.sort("startTime"));
        query.add(Constraints.sort("id"));
        return query;
    }

    /**
     * Returns the appointment reminder job configuration, if one is present.
     *
     * @return the configuration, or {@code null} if none exists
     */
    protected IMObject getAppointmentReminderJob() {
        ArchetypeQuery query = new ArchetypeQuery(APPOINTMENT_REMINDER_JOB, true, true);
        query.setMaxResults(1);
        Iterator<IMObject> iterator = new IMObjectQueryIterator<>(service, query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
     * Returns the schedule slot size in minutes.
     *
     * @param schedule the schedule
     * @return the schedule slot size in minutes
     * @throws OpenVPMSException for any error
     */
    private int getSlotSize(IMObjectBean schedule) {
        int slotSize = schedule.getInt("slotSize");
        String slotUnits = schedule.getString("slotUnits");
        int result;
        if ("HOURS".equals(slotUnits)) {
            result = slotSize * 60;
        } else {
            result = slotSize;
        }
        return result;
    }

    /**
     * Helper to return the no. of slots for an appointment type.
     *
     * @param schedule        the schedule
     * @param appointmentType the appointment type
     * @return the no. of slots, or {@code 0} if unknown
     * @throws OpenVPMSException for any error
     */
    private int getSlots(EntityBean schedule, Entity appointmentType) {
        int noSlots = 0;
        EntityRelationship relationship
                = schedule.getRelationship(appointmentType);
        if (relationship != null) {
            IMObjectBean bean = new IMObjectBean(relationship, service);
            noSlots = bean.getInt("noSlots");
        }
        return noSlots;
    }

    /**
     * Updates the status of a linked act to that of the supplied act,
     * where the statuses are common.
     *
     * @param act    the act
     * @param linked the act to update
     */
    private void updateStatus(Act act, Act linked) {
        String status = act.getStatus();
        // Only update the linked act status if workflow status not pending.
        if (WorkflowStatus.IN_PROGRESS.equals(status)
            || WorkflowStatus.BILLED.equals(status)
            || WorkflowStatus.COMPLETED.equals(status)
            || WorkflowStatus.CANCELLED.equals(status)) {
            if (!status.equals(linked.getStatus())) {
                linked.setStatus(status);
                if (TypeHelper.isA(linked, ScheduleArchetypes.TASK)
                    && WorkflowStatus.COMPLETED.equals(status)) {
                    // update the task's end time to now
                    linked.setActivityEndTime(new Date());
                }
                service.save(linked);
            }
        }
    }

}
