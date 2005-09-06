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


package org.openvpms.component.business.domain.im.party;

// java core
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.DvInterval;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

/**
 * A {@link Person} working for a veterinary practice as in the {@link Role}
 * of an employee
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Employee extends Role {

    /**
     * Generate SUID
     */
    private static final long serialVersionUID = 1L;

    
    /** 
     * Default constuctor
     */
    public Employee() {
        // do nothing
    }
    
    /**
     * Constructs an employee role.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name 
     * @param description
     *            the description of this entity            
     * @param contacts
     *            a collection of contacts for this actor            
     * @param activePeriod
     *            the period that this role is valid                        
     * @param details
     *            dynamic properties for this employee
     */
    public Employee(String uid, ArchetypeId archetypeId, String name, 
            String description, Set<Contact> contacts,
            DvInterval<DvDateTime> activePeriod, DynamicAttributeMap details) {
        super(uid, archetypeId, name, description, contacts, activePeriod, details);
    }
}
