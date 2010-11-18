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

package org.openvpms.web.app.subsystem;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Basic CRUD workspace.
 * <p/>
 * Provides an {@link IMObjectSelector selector} to select objects and
 * {@link CRUDWindow CRUD window}. The selector is optional.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class BasicCRUDWorkspace<T extends IMObject>
        extends AbstractCRUDWorkspace<T, T> {

    /**
     * Constructs a new <tt>BasicCRUDWorkspace</tt>, with a selector for
     * the object.
     * <p/>
     * The {@link #setArchetypes} method must be invoked to set the archetypes
     * that the workspace supports, before performing any operations.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public BasicCRUDWorkspace(String subsystemId, String workspaceId) {
        this(subsystemId, workspaceId, null);
    }

    /**
     * Constructs a new <tt>BasicCRUDWorkspace</tt>, with a selector for
     * the object.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param archetypes  the archetypes that this operates on.
     *                    If <tt>null</tt>, the {@link #setArchetypes}
     *                    method must be invoked to set a non-null value
     *                    before performing any operation
     */
    public BasicCRUDWorkspace(String subsystemId, String workspaceId,
                              Archetypes<T> archetypes) {
        super(subsystemId, workspaceId, archetypes, archetypes);
    }

    /**
     * Constructs a new <tt>BasicCRUDWorkspace</tt>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param archetypes   the archetypes that this operates on.
     *                     If <tt>null</tt>, the {@link #setArchetypes}
     *                     method must be invoked to set a non-null value
     *                     before performing any operation
     * @param showSelector if <tt>true</tt>, show a selector to select the
     *                     object
     */
    public BasicCRUDWorkspace(String subsystemId, String workspaceId,
                              Archetypes<T> archetypes, boolean showSelector) {
        super(subsystemId, workspaceId, archetypes, archetypes, showSelector);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(T object) {
        super.setObject(object);
        getCRUDWindow().setObject(object);
    }

    /**
     * Creates a new dialog to select an object.
     * <p/>
     * This implementation adds a 'New' button.
     *
     * @param browser the browser
     * @return a new dialog
     */
    @Override
    protected BrowserDialog<T> createBrowserDialog(Browser<T> browser) {
        String title = Messages.get("imobject.select.title", getArchetypes().getDisplayName());
        return new BrowserDialog<T>(title, browser, true);
    }

    /**
     * Invoked when the selection browser is closed.
     *
     * @param dialog the browser dialog
     */
    @Override
    protected void onSelectClosed(BrowserDialog<T> dialog) {
        if (dialog.createNew()) {
            getCRUDWindow().create();
        } else {
            T object = dialog.getSelected();
            if (object != null) {
                onSelected(object);
            }
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(T object, boolean isNew) {
        setObject(object);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(T object) {
        setObject(null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(T object) {
        object = IMObjectHelper.reload(object);
        setObject(object);
    }

    /**
     * Sets the archetypes that this operates on.
     *
     * @param archetypes the archetypes
     */
    @Override
    protected void setArchetypes(Archetypes<T> archetypes) {
        super.setArchetypes(archetypes);
        setChildArchetypes(archetypes);
    }

}
