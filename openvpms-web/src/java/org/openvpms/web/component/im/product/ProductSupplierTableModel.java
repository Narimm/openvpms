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

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipDescriptorTableModel;


/**
 * Table model for <em>entityRelationship.productSupplier</em> objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ProductSupplierTableModel
        extends RelationshipDescriptorTableModel<EntityRelationship> {

    /**
     * The nodes to include in the table.
     */
    private static final String[] NODES = {"description", "preferred",
                                           "packageSize", "packageUnits",
                                           "listPrice", "nettPrice"};

    /**
     * Creates a new <tt>ProductSupplierTableModel</tt>.
     * <p/>
     * Enables selection if the context is in edit mode, or <tt>null</tt>
     *
     * @param shortNames    the archetype short names
     * @param context       the layout context. May be <tt>null</tt>
     * @param displayTarget if <tt>true</tt> display the target node,
     *                      otherwise display the source node
     */
    public ProductSupplierTableModel(String[] shortNames,
                                     LayoutContext context,
                                     boolean displayTarget) {
        super(shortNames, context, displayTarget);
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     *
     * @return the list of node descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return NODES;
    }
}
