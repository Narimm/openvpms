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

import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.IRequestCycle;
import org.openvpms.component.presentation.tapestry.Global;
import org.openvpms.component.presentation.tapestry.page.SearchPage;


/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class SearchLink extends Link
{
    public abstract void setArchetypeName(String archetypeName);

    public void click(IRequestCycle cycle)
    {
        SearchPage page = (SearchPage) findPage(cycle, "Search");
        Global global = (Global)page.getGlobal();
        try {
            page.setInstances(global.getEntityService().getByShortName(getArchetypeName()));
        } catch (Exception ex) {
            throw new ApplicationRuntimeException(ex);
        }
        page.activateExternalPage(new Object[] {getArchetypeName()}, cycle);
    }

    /**
     * @return
     */
    public String getLinkText()
    {
        return "Search " + getArchetypeName();
    }
}
