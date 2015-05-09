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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfMonth;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.Month;

/**
 * An editor for expressions that repeat on Sunday-Saturday.
 *
 * @author Tim Anderson
 */
class RepeatOnDaysEditor extends AbstractRepeatExpressionEditor {

    /**
     * The repeat start time.
     */
    private final Date startTime;

    /**
     * The days to repeat on.
     */
    private SimpleProperty[] days = new SimpleProperty[7];


    /**
     * Constructs an {@link RepeatOnDaysEditor}.
     *
     * @param startTime the repeat start time
     */
    public RepeatOnDaysEditor(Date startTime) {
        this(startTime, null);
    }

    /**
     * Constructs an {@link RepeatOnDaysEditor}.
     *
     * @param startTime  the repeat start time
     * @param expression the source expression. May be {@code null}
     */
    public RepeatOnDaysEditor(Date startTime, CronRepeatExpression expression) {
        this.startTime = startTime;
        DayOfWeek dayOfWeek = (expression != null) ? expression.getDayOfWeek() : null;
        for (int i = 0; i < days.length; ++i) {
            int day = Calendar.SUNDAY + i;
            String name = DateFormatSymbols.getInstance().getWeekdays()[day];
            boolean selected = (dayOfWeek != null) && dayOfWeek.isSelected(day);
            days[i] = new SimpleProperty("day" + i, selected, Boolean.class, name);
        }
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        Grid grid = new Grid(4);
        FocusGroup group = getFocusGroup();
        for (SimpleProperty day : days) {
            BoundCheckBox box = new BoundCheckBox(day);
            box.setText(day.getDisplayName());
            grid.add(box);
            group.add(box);
        }
        Label every = LabelFactory.create("workflow.scheduling.appointment.every");
        return RowFactory.create(Styles.CELL_SPACING, every, grid);
    }

    /**
     * Determines if the editor can edit the supplied expression.
     *
     * @param expression the expression
     * @return {@code true} if the editor can edit the expression
     */
    public static boolean supports(CronRepeatExpression expression) {
        DayOfWeek dayOfWeek = expression.getDayOfWeek();
        DayOfMonth dayOfMonth = expression.getDayOfMonth();
        Month month = expression.getMonth();
        return !dayOfWeek.isAll() && !dayOfWeek.isOrdinal() && dayOfMonth.isAll() && month.isAll();
    }

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    @Override
    public RepeatExpression getExpression() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < days.length; ++i) {
            boolean selected = days[i].getBoolean();
            if (selected) {
                String day = DayOfWeek.getDay(Calendar.SUNDAY + i);
                list.add(day);
            }
        }
        if (!list.isEmpty()) {
            DayOfWeek dayOfWeek = new DayOfWeek(list);
            return new CronRepeatExpression(startTime, dayOfWeek);
        }
        return null;
    }

}
