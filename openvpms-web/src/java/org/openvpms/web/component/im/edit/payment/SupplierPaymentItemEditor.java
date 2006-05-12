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

package org.openvpms.web.component.im.edit.payment;

import java.math.BigDecimal;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * An editor for {@link Act}s which have an archetype in
 * <em>act.supplierAccountPayment*</em>, and <em>act.supplierAccountRefund*</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class SupplierPaymentItemEditor extends AbstractIMObjectEditor {

    /**
     * Construct a new <code>SupplierPaymentItemEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public SupplierPaymentItemEditor(Act act, Act parent,
                                     LayoutContext context) {
        super(act, parent, context);
        if (!IMObjectHelper.isA(act,
                                "act.supplierAccountPayment*",
                                "act.supplierAccountRefund*")
            || IMObjectHelper.isA(act,
                                  "act.supplierAccountPayment",
                                  "act.supplierAccountRefund")) {
            throw new IllegalArgumentException(
                    "Invalid act type: " + act.getArchetypeId().getShortName());
        }

        if (act.isNew() &&
            IMObjectHelper.isA(act, "act.supplierAccountPayment*")) {
            // Default the amount to the outstanding balance
            Party supplier = Context.getInstance().getSupplier();
            if (supplier != null) {
                BigDecimal diff = ActHelper.sum(parent, "amount");
                BigDecimal current = ActHelper.getSupplierAccountBalance(supplier);
                BigDecimal balance = current.subtract(diff);
                Property amount = getProperty("amount");
                amount.setValue(balance);
            }
        }
    }

}
