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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.doc.DocumentEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.PropertySet;

/**
 * Edit layout strategy for <em>act.patientDocument*Version</em> archetypes.
 *
 * @author Tim Anderson
 */
public class PatientDocumentVersionActLayoutStrategy extends PatientDocumentActLayoutStrategy {

    /**
     * Constructs an {@link PatientDocumentVersionActLayoutStrategy}.
     *
     * @param editor the editor. May be {@code null}
     * @param locked determines if the record is locked
     */
    public PatientDocumentVersionActLayoutStrategy(DocumentEditor editor, boolean locked) {
        super(editor, null, locked);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        if (isLocked()) {
            // make the document editor read-only. This replaces the existing component.
            IMObjectComponentFactory factory = context.getComponentFactory();
            addComponent(factory.create(createReadOnly(properties.get("document")), object));
        }
        return super.apply(object, properties, parent, context);
    }
}
