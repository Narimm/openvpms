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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.LabelFactory;


/**
 * An {@link IMObjectComponentFactory} that returns read-only components for
 * display in a table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TableComponentFactory extends AbstractReadOnlyComponentFactory {

    /**
     * Construct a new <code>TableComponentFactory</code>.
     *
     * @param context the layout context.
     */
    public TableComponentFactory(LayoutContext context) {
        super(context);
    }

    /**
     * Returns a component to display a lookup.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the lookup
     */
    protected Component getLookup(IMObject context, NodeDescriptor descriptor) {
        return getLabel(context, descriptor);
    }

    /**
     * Returns a component to display a number.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected Component getNumber(IMObject context, NodeDescriptor descriptor) {
        String value = getNumericValue(context, descriptor);
        Label label = LabelFactory.create();
        label.setText(value);
        TableLayoutData layout = new TableLayoutData();
        Alignment right = new Alignment(Alignment.RIGHT,
                                        Alignment.DEFAULT);
        layout.setAlignment(right);
        label.setLayoutData(layout);
        return label;
    }

    /**
     * Returns a component to display a date.
     *
     * @param context    the context object
     * @param descriptor the node descriptor
     * @return a component to display the datge
     */
    protected Component getDate(IMObject context, NodeDescriptor descriptor) {
        String value = getDateValue(context, descriptor);
        Label label = LabelFactory.create();
        label.setText(value);
        return label;
    }

}
