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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.policy;

import org.openvpms.component.business.domain.im.party.Party;

import java.util.Date;

/**
 * Animal insurance policy.
 *
 * @author Tim Anderson
 */
public interface Policy {

    /**
     * Returns the policy number, issued by the insurer.
     *
     * @return the policy number
     */
    String getPolicyNumber();

    /**
     * Returns the date when the policy expires.
     *
     * @return the policy expiry date
     */
    Date getExpiryDate();

    /**
     * Returns the policy holder.
     *
     * @return the policy holder
     */
    PolicyHolder getPolicyHolder();

    /**
     * Returns the animal that the policy applies to.
     *
     * @return the animal
     */
    Animal getAnimal();

    /**
     * Returns the insurer that issued the policy.
     *
     * @return insurer that issued the policy
     */
    Party getInsurer();

}
