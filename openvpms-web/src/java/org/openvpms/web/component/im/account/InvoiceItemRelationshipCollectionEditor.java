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

package org.openvpms.web.component.im.account;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.act.DefaultActTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Editor for <em>actRelationship.customerAccountInvoiceItem</em> and
 * <em>actRelationship.customerAccountCreditItem</em> act relationships.
 * Sets an {@link MedicationManager} on {@link CustomerInvoiceItemEditor}
 * instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class InvoiceItemRelationshipCollectionEditor
        extends ActRelationshipCollectionEditor {

    /**
     * The medication manager.
     */
    private final MedicationManager medicationMgr = new MedicationManager();


    /**
     * Constructs a new <code>InvoiceItemRelationshipCollectionEditor</code>.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public InvoiceItemRelationshipCollectionEditor(CollectionProperty property,
                                                   Act act,
                                                   LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <code>object</code>
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        IMObjectEditor editor = super.createEditor(object, context);
        if (editor instanceof CustomerInvoiceItemEditor) {
            ((CustomerInvoiceItemEditor) editor).setMedicationManager(
                    medicationMgr);
        }
        return editor;
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        ResultSet<IMObject> set = super.createResultSet();
        set.sort(new SortConstraint[]{new NodeSortConstraint("startTime",
                                                             false)});
        return set;
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        IMTableModel model = new DefaultActTableModel(
                editor.getArchetypeRange(), context);
        return (IMTableModel<IMObject>) model;
    }
}
