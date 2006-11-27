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

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.LongNameConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.component.util.TextComponentFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link Query} interface that queries {@link
 * IMObject} instances on short name, instance name, and active/inactive
 * status.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractQuery<T extends IMObject> implements Query<T> {

    /**
     * The archetypes to query.
     */
    private final BaseArchetypeConstraint _archetypes;

    /**
     * Archetype short names to matches on.
     */
    private final String[] _shortNames;

    /**
     * Archetype reference model name. May be <code>null</code>
     */
    private final String _refModelName;

    /**
     * Archetype entity name. May be <code>null</code>
     */
    private final String _entityName;

    /**
     * Archetype concept name. May be <code>null</code>
     */
    private final String _conceptName;

    /**
     * Additional constraints to associate with the query. May be
     * <code>null</code>
     */
    private IConstraint _constraints;

    /**
     * Determines if the query should be run automatically.
     */
    private boolean _auto;

    /**
     * Determines if duplicate rows should be filtered.
     */
    private boolean _distinct;

    /**
     * The instance name field. If the text is <code>null</code> or empty, indicates
     * to query all instances.
     */
    private TextField _instanceName;

    /**
     * The inactive check box. If selected, deactived instances will be returned
     * along with the active ones.
     */
    private CheckBox _inactive;

    /**
     * The selected archetype short name. If <code>null</code>, or {@link
     * ArchetypeShortNameListModel#ALL}, indicates to query using all matching
     * short names.
     */
    private String _shortName;

    /**
     * The component representing the query.
     */
    private Component _component;

    /**
     * The event listener list.
     */
    private List<QueryListener> _listeners = new ArrayList<QueryListener>();

    /**
     * The maxmimum no. of rows to return per page.
     */
    private int _maxRows = 20;

    /**
     * Type label id.
     */
    private static final String TYPE_ID = "type";

    /**
     * Name label id.
     */
    private static final String NAME_ID = "name";

    /**
     * Deactivated label id.
     */
    private static final String DEACTIVATED_ID = "deactivated";

    /**
     * Button row style name.
     */
    private static final String ROW_STYLE = "ControlRow";


    /**
     * Construct a new <code>AbstractQuery</code> that queries IMObjects with
     * the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractQuery(String[] shortNames) {
        _shortNames = DescriptorHelper.getShortNames(shortNames);
        _archetypes = new ShortNameConstraint(shortNames, true, true);
        _refModelName = null;
        _entityName = null;
        _conceptName = null;
    }

    /**
     * Construct a new <code>AbstractQuery</code> that queries IMObjects with
     * the specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AbstractQuery(String refModelName, String entityName,
                         String conceptName) {
        _shortNames = DescriptorHelper.getShortNames(refModelName, entityName,
                                                     conceptName);
        _archetypes = new LongNameConstraint(
                refModelName, entityName, conceptName, true, true);
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_component == null) {
            _component = RowFactory.create(ROW_STYLE);
            doLayout(_component);
        }
        return _component;
    }

    /**
     * Sets the maximum no. of rows to return per page.
     *
     * @param rows the maxiomum no. of rows per page
     */
    public void setMaxRows(int rows) {
        _maxRows = rows;
    }

    /**
     * Returns the maximum no. of rows to return per page.
     *
     * @return the maximum no. of rows to return per page
     */
    public int getMaxRows() {
        return _maxRows;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     */
    public ResultSet<T> query(SortConstraint[] sort) {
        String type = getShortName();
        String name = getName();
        boolean activeOnly = !includeInactive();

        BaseArchetypeConstraint archetypes;
        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            archetypes = _archetypes;
            archetypes.setActiveOnly(activeOnly);
        } else {
            archetypes = new ShortNameConstraint(type, true, activeOnly);
        }
        return new DefaultResultSet<T>(archetypes, name, _constraints, sort,
                                       _maxRows, _distinct);
    }

    /**
     * The archetype short names being queried.
     *
     * @return the short names being queried
     */
    public String[] getShortNames() {
        return _shortNames;
    }

    /**
     * Sets the name to query on.
     *
     * @param name the name. May contain wildcards, or be <code>null</code>
     */
    public void setName(String name) {
        getInstanceName().setText(name);
    }

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be <code>null</code>
     */
    public String getName() {
        final String wildcard = "*";
        String name = getInstanceName().getText();
        if (!StringUtils.isEmpty(name)) {
            // if entered name contains a wildcard then leave alone else
            // add one to end
            if (!name.contains(wildcard)) {
                name = name + wildcard;
            }
        }
        return name;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @param auto if <code>true</code> the query should be run automatically
     */
    public void setAuto(boolean auto) {
        _auto = auto;
    }


    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwise <code>false</code>
     */
    public boolean isAuto() {
        return _auto;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    public void setDistinct(boolean distinct) {
        _distinct = distinct;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @return <code>true</code> if duplicate rows should be removed;
     *         otherwise <code>false</code>
     */
    public boolean isDistinct() {
        return _distinct;
    }

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryListener listener) {
        _listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeQueryListener(QueryListener listener) {
        _listeners.remove(listener);
    }

    /**
     * Set query constraints.
     *
     * @param constraints the constraints
     */
    public void setConstraints(IConstraint constraints) {
        _constraints = constraints;
    }

    /**
     * Returns query contraints.
     *
     * @return the constraints
     */
    public IConstraint getConstraints() {
        return _constraints;
    }

    /**
     * Returns the archetype constraint.
     *
     * @return the archetype constraint
     */
    public BaseArchetypeConstraint getArchetypeConstraint() {
        return _archetypes;
    }

    /**
     * Returns the archetype reference model name.
     *
     * @return the archetype reference model name. May be <code>null</code>
     */
    public String getRefModelName() {
        return _refModelName;
    }

    /**
     * Returns the archetype entity name.
     *
     * @return the archetype entity name. May be <code>null</code>
     */
    public String getEntityName() {
        return _entityName;
    }

    /**
     * Returns the archetype concept name.
     *
     * @return the archetype concept name. May be <code>null</code>
     */
    public String getConceptName() {
        return _conceptName;
    }

    /**
     * Determines if inactive instances should be returned.
     *
     * @return <code>true</code> if inactive instances should be retured;
     *         <code>false</code>
     */
    protected boolean includeInactive() {
        return (_inactive != null && _inactive.isSelected());
    }

    /**
     * Returns the selected archetype short name.
     *
     * @return the archetype short name. May be <code>null</code>
     */
    protected String getShortName() {
        return _shortName;
    }

    /**
     * Set the archetype short name.
     *
     * @param name the archetype short name. If <code>null</code>, indicates to
     *             query using all matching short names.
     */
    protected void setShortName(String name) {
        _shortName = name;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addInstanceName(container);
        addInactive(container);
        ApplicationInstance.getActive().setFocusedComponent(getInstanceName());
    }

    /**
     * Adds the short name selector to a container, if there is more than one
     * matching short name
     *
     * @param container the container
     */
    protected void addShortNameSelector(Component container) {
        if (_shortNames.length > 1) {
            final ArchetypeShortNameListModel model
                    = new ArchetypeShortNameListModel(_shortNames, true);
            final SelectField shortNameSelector = SelectFieldFactory.create(
                    model);
            shortNameSelector.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = shortNameSelector.getSelectedIndex();
                    String shortName = model.getShortName(index);
                    setShortName(shortName);
                }
            });

            Label typeLabel = LabelFactory.create(TYPE_ID);
            container.add(typeLabel);
            container.add(shortNameSelector);
        }
    }

    /**
     * Returns the instance name field.
     *
     * @return the instance name field
     */
    protected TextField getInstanceName() {
        if (_instanceName == null) {
            _instanceName = TextComponentFactory.create();
            _instanceName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onQuery();
                }
            });
        }
        return _instanceName;
    }

    /**
     * Adds the instance name field to a container.
     *
     * @param container the container
     */
    protected void addInstanceName(Component container) {
        Label nameLabel = LabelFactory.create(NAME_ID);
        container.add(nameLabel);
        container.add(getInstanceName());
    }

    /**
     * Returns the inactive field.
     *
     * @return the inactive field
     */
    protected CheckBox getInactive() {
        if (_inactive == null) {
            _inactive = new CheckBox();
            _inactive.setSelected(false);
        }
        return _inactive;
    }

    /**
     * Adds the inactive checkbox to a container.
     *
     * @param container the container
     */
    protected void addInactive(Component container) {
        Label deactivedLabel = LabelFactory.create(DEACTIVATED_ID);
        container.add(deactivedLabel);
        container.add(getInactive());
    }

    /**
     * Notify listnerss to perform a query.
     */
    protected void onQuery() {
        QueryListener[] listeners = _listeners.toArray(new QueryListener[0]);
        for (QueryListener listener : listeners) {
            listener.query();
        }
    }

}
