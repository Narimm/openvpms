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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter;

import org.oasis.ubl.InvoiceType;
import org.openvpms.component.business.domain.im.act.FinancialAct;

import java.util.List;

/**
 * Maps UBL invoices to <em>act.supplierDelivery</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface InvoiceMapper {

    /**
     * Maps an UBL invoice to an <em>act.supplierDelivery</em>.
     *
     * @param invoice the invoice to map
     * @return the acts produced in the mapping. The first element is always the <em>act.supplierDelivery</em>
     */
    List<FinancialAct> map(InvoiceType invoice);
}
