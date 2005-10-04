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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.presentation.tapestry.component;

import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;


/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class Link extends OpenVpmsComponent
{
    public static String DEFAULT = "Default";

    public abstract String getArchetypeName();

    public String getUnqualifiedArchetypeName()
    {
        return Utils.unqualify(getArchetypeName());
    }

    protected IPage findPage(IRequestCycle cycle, String postfix)
    {
        String pageName = getUnqualifiedArchetypeName() + postfix;

        return Utils.findPage(cycle, pageName, postfix);
    }
}
