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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.Collection;


/**
 * Editor for a collection of {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public interface IMObjectCollectionEditor extends PropertyEditor, Saveable {

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    CollectionProperty getCollection();

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    IMObject getObject();

    /**
     * Determines if items can be added and removed.
     *
     * @param readOnly if <tt>true</tt> items can't be added and removed
     */
    void setCardinalityReadOnly(boolean readOnly);

    /**
     * Determines if items can be added or removed.
     *
     * @return <tt>true</tt> if items can't be added or removed.
     */
    boolean isCardinalityReadOnly();

    /**
     * Sets a listener to be notified when an object is created.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    void setCreationListener(IMObjectCreationListener listener);

    /**
     * Creates a new object.
     * <p/>
     * The object is not automatically added to the collection.
     * <p/>
     * If an {@link IMObjectCreationListener} is registered, it will be
     * notified on successful creation of an object.
     *
     * @return a new object, or {@code null} if the object can't be created
     */
    IMObject create();

    /**
     * Adds an object to the collection.
     *
     * @param object the object to add
     */
    void add(IMObject object);

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    void remove(IMObject object);

    /**
     * Refreshes the collection display.
     */
    void refresh();

    /**
     * Returns editors for items in the collection.
     * <p/>
     * These include any editors that have been created for objects in the
     * collection, and the current editor, which may be for an uncommitted object.
     * <p/>
     * If an object hasn't been edited, it may not have a corresponding editor.
     *
     * @return editors for items in the collection and editors for items not yet committed to the collection
     */
    Collection<IMObjectEditor> getEditors();

}
