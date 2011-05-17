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

package org.openvpms.web.component.im.table.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Table model that displays all related acts in a {@link DefaultActTableModel}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BasicActRelationshipTableModel
        extends AbstractActRelationshipTableModel<Act> {

    /**
     * Constructs a new <tt>CustomerAccountActRelationshipTableModel</tt>
     *
     * @param relationshipTypes the act relationship short names
     * @param context           the layout context. May be <tt>null</tt>
     */
    public BasicActRelationshipTableModel(String[] relationshipTypes,
                                          LayoutContext context) {
        String[] shortNames = getTargetShortNames(relationshipTypes);
        setModel(new DefaultActTableModel(shortNames, context));
    }
}
