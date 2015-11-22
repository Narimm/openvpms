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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.ActCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.mr.Prescriptions;

import java.util.Date;


/**
 * Editor for <em>actRelationship.customerAccountInvoiceItem</em> and
 * <em>actRelationship.customerAccountCreditItem</em> act relationships.
 * Sets a {@link EditorQueue} on {@link CustomerChargeActItemEditor} instances.
 *
 * @author Tim Anderson
 */
public class ChargeItemRelationshipCollectionEditor extends AbstractChargeItemRelationshipCollectionEditor {

    /**
     * Last Selected Item Date.
     */
    private Date lastItemDate = null;

    /**
     * The popup editor manager.
     */
    private EditorQueue editorQueue;

    /**
     * The prescriptions.
     */
    private final Prescriptions prescriptions;

    /**
     * The charge context.
     */
    private final ChargeContext chargeContext;

    /**
     * Listener invoked when {@link #onAdd()} is invoked.
     */
    private Runnable listener;

    /**
     * The start time node name.
     */
    private static final String START_TIME = "startTime";

    /**
     * Constructs a {@link ChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        this(property, act, context, ActCollectionResultSetFactory.INSTANCE);
    }

    /**
     * Constructs a {@link ChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context,
                                                  CollectionResultSetFactory factory) {
        super(property, act, context, factory);
        editorQueue = new DefaultEditorQueue(context.getContext());
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE)) {
            prescriptions = new Prescriptions(getCurrentActs(), ServiceHelper.getBean(PrescriptionRules.class));
        } else {
            prescriptions = null;
        }
        chargeContext = new ChargeContext();
    }

    /**
     * Returns the charge context.
     *
     * @return the charge context
     */
    public ChargeContext getChargeContext() {
        return chargeContext;
    }

    /**
     * Sets the popup editor manager.
     *
     * @param queue the popup editor manager. May be {@code null}
     */
    public void setEditorQueue(EditorQueue queue) {
        editorQueue = queue;
    }

    /**
     * Returns the popup editor manager.
     *
     * @return the popup editor manager. May be {@code null}
     */
    public EditorQueue getEditorQueue() {
        return editorQueue;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        final IMObjectEditor editor = super.createEditor(object, context);
        initialiseEditor(editor);
        return editor;
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        super.remove(object);
        if (prescriptions != null) {
            prescriptions.removeItem((Act) object);
        }
    }

    /**
     * Registers a listener that is invoked when the user adds an item.
     * <p>
     * Note that this is not invoked for template expansion.
     *
     * @param listener the listener to invoke. May be {@code null}
     */
    public void setAddItemListener(Runnable listener) {
        this.listener = listener;
    }

    /**
     * Invoked when the "Add" button is pressed. Creates a new instance of the selected archetype, and displays it in
     * an editor.
     *
     * @return the new editor, or {@code null} if one could not be created
     */
    @Override
    protected IMObjectEditor onAdd() {
        IMObjectEditor editor = add();
        if (editor != null && listener != null) {
            EditorQueue queue = getEditorQueue();
            if (!queue.isComplete()) {
                queue.queue(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.run();
                        }
                    }
                });
            } else {
                listener.run();
            }
        }
        return editor;
    }

    /**
     * Initialises an editor.
     *
     * @param editor the editor
     */
    protected void initialiseEditor(final IMObjectEditor editor) {
        if (editor instanceof CustomerChargeActItemEditor) {
            CustomerChargeActItemEditor itemEditor = (CustomerChargeActItemEditor) editor;
            itemEditor.setEditorQueue(editorQueue);
            itemEditor.setPrescriptions(prescriptions);
            itemEditor.setChargeContext(chargeContext);
            itemEditor.setDoseManager(getDoseManager());
        }

        // Set startTime to to last used value
        if (lastItemDate != null) {
            editor.getProperty(START_TIME).setValue(lastItemDate);
        }

        // add a listener to store the last used item starttime.
        ModifiableListener startTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                lastItemDate = (Date) editor.getProperty(START_TIME).getValue();
            }
        };
        editor.getProperty(START_TIME).addModifiableListener(startTimeListener);
    }

    /**
     * Saves any current edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        if (prescriptions != null) {
            prescriptions.save();
        }
        // Need to save prescriptions first, as invoice item deletion can cause StaleObjectStateExceptions otherwise
        super.doSave();
    }
}
