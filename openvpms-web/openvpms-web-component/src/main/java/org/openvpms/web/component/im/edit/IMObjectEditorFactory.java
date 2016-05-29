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

package org.openvpms.web.component.im.edit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.lang.reflect.Constructor;


/**
 * A factory for {@link IMObjectEditor} instances.
 *
 * @author Tim Anderson
 */
public class IMObjectEditorFactory {

    /**
     * Editor implementations.
     */
    private ArchetypeHandlers<IMObjectEditor> editors;

    /**
     * The resource name.
     */
    private final String name;

    /**
     * The fallback resource name.
     */
    private final String fallbackName;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectEditorFactory.class);

    /**
     * The default resource name.
     */
    private static final String NAME = "IMObjectEditorFactory.properties";

    /**
     * The default fallback resource name.
     */
    private static final String FALLBACK_NAME = "DefaultIMObjectEditorFactory.properties";

    /**
     * Constructs an {@link IMObjectEditorFactory}.
     */
    public IMObjectEditorFactory() {
        this(NAME, FALLBACK_NAME);
    }

    /**
     * Constructs an {@link IMObjectEditorFactory}.
     *
     * @param name         the resource name
     * @param fallbackName the fallback resource name. May be {@code null}
     */
    public IMObjectEditorFactory(String name, String fallbackName) {
        this.name = name;
        this.fallbackName = fallbackName;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context. May be {@code null}
     * @return an editor for {@code object}
     */
    public IMObjectEditor create(IMObject object, LayoutContext context) {
        return create(object, null, context);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     * @return an editor for {@code object}
     */
    public IMObjectEditor create(IMObject object, IMObject parent, LayoutContext context) {
        IMObjectEditor result = null;

        String shortName = object.getArchetypeId().getShortName();
        ArchetypeHandler handler = getEditors().getHandler(shortName);

        if (handler != null) {
            Class type = handler.getType();
            Constructor ctor = getConstructor(type, object, parent, context);
            if (ctor != null) {
                try {
                    result = (IMObjectEditor) ctor.newInstance(object, parent, context);
                } catch (Throwable throwable) {
                    log.error(throwable, throwable);
                }
            } else {
                log.error("No valid constructor found for class: " + type.getName());
            }
        }
        if (result == null) {
            result = new DefaultIMObjectEditor(object, parent, context);
        }
        return result;
    }

    /**
     * Returns the editors.
     *
     * @return the editors
     */
    private synchronized ArchetypeHandlers getEditors() {
        if (editors == null) {
            editors = new ArchetypeHandlers<>(name, fallbackName, IMObjectEditor.class);
        }
        return editors;
    }

    /**
     * Returns a constructor to construct a new editor.
     *
     * @param type    the editor type
     * @param object  the object to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}
     * @return a constructor to construct the editor, or {@code null} if none can be found
     */
    private Constructor getConstructor(Class type, IMObject object, IMObject parent, LayoutContext context) {
        Constructor[] ctors = type.getConstructors();

        for (Constructor ctor : ctors) {
            // check parameters
            Class<?>[] ctorTypes = ctor.getParameterTypes();
            if (ctorTypes.length == 3) {
                Class<?> ctorObj = ctorTypes[0];
                Class<?> ctorParent = ctorTypes[1];
                Class<?> ctorLayout = ctorTypes[2];

                if (ctorObj.isAssignableFrom(object.getClass())
                    && ((parent != null && ctorParent.isAssignableFrom(parent.getClass()))
                        || (parent == null && IMObject.class.isAssignableFrom(ctorParent)))
                    && ((context != null && ctorLayout.isAssignableFrom(context.getClass()))
                        || (context == null && LayoutContext.class.isAssignableFrom(ctorLayout)))) {
                    return ctor;
                }
            }
        }
        return null;
    }

}
