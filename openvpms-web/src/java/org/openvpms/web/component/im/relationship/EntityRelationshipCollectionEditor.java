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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Editor for collections of {@link EntityRelationship}s.
 * * <p/>
 * If the relationships have a <em>sequence</em> node, the collection will
 * be ordered on it, and controls displayed to move relationships up or down
 * within the collection.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipCollectionEditor
        extends RelationshipCollectionEditor {

    /**
     * Determines if the collection has a sequence node.
     * If so, the collection is automatically ordered on the sequence.
     */
    private boolean sequenced;

    /**
     * The relationships being displayed, used when the collection is sequenced.
     */
    private List<RelationshipState> relationships;


    /**
     * Creates a new <tt>EntityRelationshipCollectionEditor</tt>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public EntityRelationshipCollectionEditor(
            CollectionProperty property, Entity object,
            LayoutContext context) {
        super(new EntityRelationshipCollectionPropertyEditor(property, object),
              object, context);
        sequenced = EntityRelationshipCollectionHelper.hasSequenceNode(
                property.getArchetypeRange());
    }

    /**
     * Creates a new object, subject to a short name being selected, and
     * current collection cardinality. This must be registered with the
     * collection.
     * <p/>
     * If an {@link IMObjectCreationListener} is registered, it will be
     * notified on successful creation of an object.
     *
     * @return a new object, or <tt>null</tt> if the object can't be created
     */
    @Override
    public IMObject create() {
        EntityRelationship relationship = (EntityRelationship) super.create();
        if (sequenced && relationships != null) {
            if (!relationships.isEmpty()) {
                RelationshipState state
                        = relationships.get(relationships.size() - 1);
                EntityRelationship last
                        = (EntityRelationship) state.getRelationship();
                int sequence = last.getSequence();
                relationship.setSequence(++sequence);
            }
        }
        return relationship;
    }

    /**
     * Creates a new result set for display.
     * <p/>
     * If the relationships have a <em>sequence</em> node, they will be
     * ordered on this.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<RelationshipState> createResultSet() {
        ResultSet<RelationshipState> result;
        RelationshipCollectionPropertyEditor editor
                = getCollectionPropertyEditor();
        List<RelationshipState> relationships
                = new ArrayList<RelationshipState>(editor.getRelationships());
        if (sequenced) {
            EntityRelationshipCollectionHelper.sort(relationships);
            result = new ListResultSet<RelationshipState>(relationships, ROWS);
            this.relationships = relationships;
        } else {
            result = super.createResultSet();
        }
        return result;
    }

    /**
     * Lays out the component in the specified container.
     *
     * @param container the container
     * @param context   the layout context
     */
    @Override
    protected void doLayout(Component container, LayoutContext context) {
        if (sequenced) {
            doSequenceLayout(container);
        } else {
            super.doLayout(container, context);
        }
    }

    /**
     * Moves the selected relationship up in the table.
     */
    private void onMoveUp() {
        RelationshipState selected = getTable().getTable().getSelected();
        if (selected != null) {
            int index = relationships.indexOf(selected);
            if (index > 0) {
                swap(index, index - 1);
            }
        }
    }

    /**
     * Moves the selected relationship up in the table.
     */
    private void onMoveDown() {
        RelationshipState selected = getTable().getTable().getSelected();
        if (selected != null) {
            int index = relationships.indexOf(selected);
            if (index < relationships.size() - 1) {
                swap(index, index + 1);
            }
        }
    }

    /**
     * Swaps two relationships in the table.
     *
     * @param index1 the index of the first relationship
     * @param index2 the index of the second relationship
     */
    private void swap(int index1, int index2) {
        RelationshipState r1 = relationships.get(index1);
        RelationshipState r2 = relationships.get(index2);

        IMObjectEditor editor1 = getEditor(r1.getRelationship());
        IMObjectEditor editor2 = getEditor(r2.getRelationship());
        Property property1 = editor1.getProperty("sequence");
        Property property2 = editor2.getProperty("sequence");
        int value1 = (Integer) property1.getValue();
        int value2 = (Integer) property2.getValue();
        property1.setValue(value2);
        property2.setValue(value1);

        populateTable();

        getTable().getTable().setSelected(r1);
    }

    /**
     * Lays out the component with controls to change the sequence of
     * relationships.
     *
     * @param container the container
     */
    private void doSequenceLayout(Component container) {
        FocusGroup focusGroup = getFocusGroup();
        PagedIMTable<RelationshipState> table = getTable();
        if (!isCardinalityReadOnly()) {
            Row row = createControls(focusGroup);
            container.add(row);
        }
        Button moveUp = ButtonFactory.create("moveup", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onMoveUp();
            }
        });
        Button moveDown = ButtonFactory.create(
                "movedown", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onMoveDown();
            }
        });
        Column column = ColumnFactory.create("CellSpacing", moveUp, moveDown);
        Row row = RowFactory.create("CellSpacing", table, column);

        populateTable();

        focusGroup.add(table);
        focusGroup.add(moveUp);
        focusGroup.add(moveDown);
        container.add(row);
    }

}