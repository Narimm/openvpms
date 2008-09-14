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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import echopointng.BalloonHelp;
import echopointng.layout.TableLayoutDataEx;
import echopointng.table.TableColumnEx;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.ColourHelper;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentTableModel extends AbstractTableModel {

    private Date day = new Date();

    private TableColumnModel model = new DefaultTableColumnModel();

    private boolean singleScheduleView;

    private static final int DEFAULT_SLOT_SIZE = 15;
    private static final int DEFAULT_START = 9 * 60;
    private static final int DEFAULT_END = 18 * 60;

    private int startTime = DEFAULT_START;

    private int endTime = DEFAULT_END;

    private int slotSize = DEFAULT_SLOT_SIZE;

    private int rows = (DEFAULT_END - DEFAULT_START) / DEFAULT_SLOT_SIZE;

    private final AppointmentRules rules;

    private Map<IMObjectReference, String> appointmentColours;

    /**
     * The start time index.
     */
    private static final int START_TIME_INDEX = 0;

    /**
     * The status index.
     */
    private static final int STATUS_INDEX = 1;

    /**
     * The appointment name index.
     */
    private static final int APPOINTMENT_INDEX = 2;

    /**
     * The customer name index.
     */
    private static final int CUSTOMER_INDEX = 3;

    /**
     * The patient name index.
     */
    private static final int PATIENT_INDEX = 4;

    /**
     * The reason index.
     */
    private static final int REASON_INDEX = 5;

    /**
     * The description index.
     */
    private static final int DESCRIPTION_INDEX = 6;

    /**
     * The nodes to display.
     */
    public static final String[][] NODE_NAMES = new String[][]{
            {"startTime", Appointment.ACT_START_TIME},
            {"status", Appointment.ACT_STATUS},
            {"appointmentType", Appointment.APPOINTMENT_TYPE_REFERENCE},
            {"customer", Appointment.CUSTOMER_REFERENCE},
            {"patient", Appointment.PATIENT_REFERENCE},
            {"reason", Appointment.ACT_REASON},
            {"description", Appointment.ACT_DESCRIPTION}};

    /**
     * The column names, for single schedule view.
     */
    private String[] columnNames;

    /**
     * Cached status lookup names.
     */
    private Map<String, String> statuses;

    /**
     * Cached reason lookup names.
     */
    private Map<String, String> reasons;


    public AppointmentTableModel() {
        rules = new AppointmentRules();
        loadAppointmentColours();

        columnNames = new String[NODE_NAMES.length];
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(
                "act.customerAppointment");
        if (archetype != null) {
            for (int i = 0; i < NODE_NAMES.length; ++i) {
                NodeDescriptor descriptor = archetype.getNodeDescriptor(
                        NODE_NAMES[i][0]);
                if (descriptor != null) {
                    columnNames[i] = descriptor.getDisplayName();
                }
            }
        }
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    public List<Party> getSchedules() {
        List<Party> result = new ArrayList<Party>();
        for (Column column : getColumns()) {
            if (column.getSchedule() != null) {
                result.add(column.getSchedule());
            }
        }
        return result;
    }

    /**
     * Sets the schedules to display.
     *
     * @param schedules the schedules
     */
    public void setSchedules(List<Party> schedules) {
        startTime = -1;
        endTime = -1;
        slotSize = -1;
        model = createColumnModel(schedules);
        singleScheduleView = schedules.size() == 1;
        for (Party schedule : schedules) {
            EntityBean bean = new EntityBean(schedule);
            Date start = bean.getDate("startTime");
            if (start != null) {
                int time = getTime(start);
                if (startTime == -1 || time < startTime) {
                    startTime = time;
                }
            }
            Date end = bean.getDate("endTime");
            if (end != null) {
                int time = getTime(end);
                if (time > endTime) {
                    endTime = time;
                }
            }
            int mins = rules.getSlotSize(schedule);
            if (mins < slotSize) {
                slotSize = mins;
            }
        }
        if (startTime == -1) {
            startTime = DEFAULT_START;
        }
        if (endTime == -1) {
            endTime = DEFAULT_END;
        }
        if (slotSize == -1) {
            slotSize = DEFAULT_SLOT_SIZE;
        }
        int timeRange = endTime - startTime;
        if (timeRange > 0 && slotSize > 0) {
            rows = timeRange / slotSize;
        } else {
            rows = 0;
        }
        fireTableStructureChanged();
    }

    public void setAppointments(Date day,
                                Map<Party, List<ObjectSet>> appointments) {
        this.day = day;
        for (Column column : getColumns()) {
            Party schedule = column.getSchedule();
            if (schedule != null) {
                List<ObjectSet> sets = appointments.get(schedule);
                column.setAppointments(sets);
            }
        }
        fireTableDataChanged();
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return getColumn(column).getHeaderValue().toString();
    }

    public TableColumnModel getColumnModel() {
        return model;
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return model.getColumnCount();
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        return rows;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     * Column and row values are 0-based.
     * <strong>WARNING: Take note that the column is the first parameter
     * passed to this method, and the row is the second parameter.</strong>
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    public Object getValueAt(int column, int row) {
        Object result = null;
        if (column == START_TIME_INDEX) {
            Date date = getStartTime(row);
            Label label = LabelFactory.create();
            if (date != null) {
                label.setText(DateHelper.formatTime(date, false));
            }
            result = label;
        } else {
            Column col = getColumn(column);
            ObjectSet set = getAppointment(col, row);
            if (set != null) {
                if (singleScheduleView) {
                    result = getValue(set, col);
                } else {
                    result = getAppointment(set);
                }
            }
        }
        return result;
    }

    public Date getStartTime(int row) {
        int time = startTime + row * slotSize;
        return DateRules.getDate(day, time, DateUnits.MINUTES);
    }


    /**
     * Returns the appointment at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the appointment, or <tt>null</tt> if none is found
     */
    public ObjectSet getAppointment(int column, int row) {
        return getAppointment(getColumn(column), row);
    }

    private ObjectSet getAppointment(Column col, int row) {
        ObjectSet result = null;
        Date startTime = getStartTime(row);
        List<ObjectSet> sets = col.getAppointments();
        if (sets != null) {
            for (ObjectSet set : sets) {
                Date date = set.getDate(Appointment.ACT_START_TIME);
                if (date != null) {
                    int compare = DateRules.compareTo(startTime, date);
                    if (compare == 0) {
                        result = set;
                        break;
                    } else if (compare < 0) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public Party getSchedule(int column) {
        Column col = getColumn(column);
        return col.getSchedule();
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param set    the object
     * @param column the column
     * @return the value at the given coordinate.
     */
    protected Object getValue(ObjectSet set, TableColumn column) {
        Object result = null;
        int index = column.getModelIndex();
        Object value = set.get(NODE_NAMES[index][1]);
        switch (index) {
            case START_TIME_INDEX:
                Date date = (Date) value;
                Label label = LabelFactory.create();
                if (date != null) {
                    label.setText(DateHelper.formatTime(date, false));
                }
                result = label;
                break;
            case STATUS_INDEX:
                if (value instanceof String) {
                    result = getStatus(set, (String) value);
                }
                break;
            case REASON_INDEX:
                if (value instanceof String) {
                    result = getReason((String) value);
                }
                break;
            case DESCRIPTION_INDEX:
                result = value;
                break;
            case APPOINTMENT_INDEX:
                result = getViewer(set, Appointment.APPOINTMENT_TYPE_REFERENCE,
                                   Appointment.APPOINTMENT_TYPE_NAME, false);
                break;
            case CUSTOMER_INDEX:
                result = getViewer(set, Appointment.CUSTOMER_REFERENCE,
                                   Appointment.CUSTOMER_NAME, true);
                break;
            case PATIENT_INDEX:
                result = getViewer(set, Appointment.PATIENT_REFERENCE,
                                   Appointment.PATIENT_NAME, true);
                break;
        }
        if (!(result instanceof Component)) {
            Label label = LabelFactory.create();
            if (result != null) {
                label.setText(result.toString());
            }
            result = label;
        }
        int span = getRowSpan(set.getDate(Appointment.ACT_START_TIME),
                              set.getDate(Appointment.ACT_END_TIME));

        if (span > 1) {
            Component component = (Component) result;
            TableLayoutDataEx layout = new TableLayoutDataEx();
            layout.setRowSpan(span);
            component.setLayoutData(layout);
        }

        return result;
    }

    private Component getAppointment(ObjectSet set) {
        Component result;
        String customer = set.getString(Appointment.CUSTOMER_NAME);
        String patient = set.getString(Appointment.PATIENT_NAME);
        String notes = set.getString(Appointment.ACT_DESCRIPTION);
        String text;
        if (patient == null) {
            text = Messages.get("workflow.scheduling.grid.customer", customer);
        } else {
            text = Messages.get("workflow.scheduling.grid.customerpatient",
                                customer, patient);
        }
        Label label = LabelFactory.create();
        label.setText(text);
        if (notes != null) {
            BalloonHelp help = new BalloonHelp("<p>" + notes + "</p>");
            result = RowFactory.create("CellSpacing", label, help);
        } else {
            result = label;
        }

        TableLayoutDataEx layout = new TableLayoutDataEx();
        result.setLayoutData(layout);

        Color color = getAppointmentColour(set);
        if (color != null) {
            layout.setBackground(color);
        }

        int span = getRowSpan(set.getDate(Appointment.ACT_START_TIME),
                              set.getDate(Appointment.ACT_END_TIME));

        if (span > 1) {
            layout.setRowSpan(span);
        }
        return result;
    }

    private Color getAppointmentColour(ObjectSet set) {
        String colour = appointmentColours.get(
                set.getReference(Appointment.APPOINTMENT_TYPE_REFERENCE));
        return ColourHelper.getColor(colour);
    }

    private int getRowSpan(Date startTime, Date endTime) {
        long duration = (endTime.getTime() - startTime.getTime()) / 1000 / 60;
        return (int) duration / slotSize;
    }

    /**
     * Returns a column given its model index.
     *
     * @param column the column index
     * @return the column
     */
    private Column getColumn(int column) {
        Column result = null;
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            Column col = (Column) iterator.next();
            if (col.getModelIndex() == column) {
                result = col;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a status name given its code.
     *
     * @param set  the object set
     * @param code the status code
     * @return the status name
     */
    private String getStatus(ObjectSet set, String code) {
        String status = null;

        if (AppointmentStatus.CHECKED_IN.equals(code)) {
            Date arrival = set.getDate(Appointment.ARRIVAL_TIME);
            if (arrival != null) {
                String diff = DateHelper.formatTimeDiff(arrival, new Date());
                status = Messages.get("appointmenttablemodel.waiting", diff);
            }
        }
        if (status == null) {
            if (statuses == null) {
                statuses = LookupNameHelper.getLookupNames(
                        "act.customerAppointment", "status");
            }
            if (statuses != null) {
                status = statuses.get(code);
            }
        }
        return status;
    }

    /**
     * Returns a reason name given its code.
     *
     * @param code the reason code
     * @return the reason name
     */
    private String getReason(String code) {
        if (reasons == null) {
            reasons = LookupNameHelper.getLookupNames("act.customerAppointment",
                                                      "reason");
        }
        return (reasons != null) ? reasons.get(code) : null;
    }

    /**
     * Returns a viewer for an object reference.
     *
     * @param set     the object set
     * @param refKey  the object reference key
     * @param nameKey the entity name key
     * @param link    if <code>true</code> enable an hyperlink to the object
     * @return a new component to view the object reference
     */
    private Component getViewer(ObjectSet set, String refKey, String nameKey,
                                boolean link) {
        IMObjectReference ref = set.getReference(refKey);
        String name = set.getString(nameKey);
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(
                ref, name, link);
        return viewer.getComponent();
    }

    private TableColumnModel createColumnModel(List<Party> schedules) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        int index = START_TIME_INDEX;
        if (schedules.size() == 1) {
            Party schedule = schedules.get(0);
            TableCellRenderer renderer = new AppointmentTableCellRenderer();
            for (int i = 0; i < columnNames.length; ++i) {
                Column column = new Column(i, schedule, columnNames[i]);
                column.setCellRenderer(renderer);
                result.addColumn(column);
            }
        } else {
            result.addColumn(new Column(index, columnNames[index]));
            ++index;
            for (Party schedule : schedules) {
                Column column = new Column(index++, schedule);
                result.addColumn(column);
            }
        }
        return result;
    }

    private int getTime(Date startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int mins = calendar.get(Calendar.MINUTE);
        return (hour * 60) + mins;
    }

    private List<Column> getColumns() {
        List<Column> result = new ArrayList<Column>();
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            result.add((Column) iterator.next());
        }
        return result;
    }

    private void loadAppointmentColours() {
        appointmentColours = new HashMap<IMObjectReference, String>();
        ArchetypeQuery query = new ArchetypeQuery("entity.appointmentType",
                                                  true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        Iterator<Entity> iter = new IMObjectQueryIterator<Entity>(query);
        while (iter.hasNext()) {
            Entity type = iter.next();
            IMObjectBean bean = new IMObjectBean(type);
            appointmentColours.put(type.getObjectReference(),
                                   bean.getString("colour"));
        }
    }


    private static class Column extends TableColumnEx {

        private Party schedule;

        private List<ObjectSet> appointments;

        public Column(int modelIndex, Party schedule) {
            this(modelIndex, schedule, schedule.getName());
        }

        public Column(int modelIndex, Party schedule, String heading) {
            super(modelIndex);
            this.schedule = schedule;
            setHeaderValue(heading);
            setHeaderRenderer(AppointmentTableHeaderRenderer.INSTANCE);
            setCellRenderer(AppointmentGridCellRenderer.INSTANCE);
            setWidth(new Extent(100));
        }

        public Column(int modelIndex, String heading) {
            this(modelIndex, null, heading);
        }

        public Party getSchedule() {
            return schedule;
        }

        public void setAppointments(List<ObjectSet> appointments) {
            this.appointments = appointments;
        }

        public List<ObjectSet> getAppointments() {
            return appointments;
        }

    }

}
