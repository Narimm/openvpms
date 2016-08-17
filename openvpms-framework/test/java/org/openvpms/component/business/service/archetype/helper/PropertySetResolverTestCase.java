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

package org.openvpms.component.business.service.archetype.helper;

import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.openvpms.component.system.common.query.ObjectSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


/**
 * {@link PropertySetResolver} test case.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class PropertySetResolverTestCase extends AbstractArchetypeServiceTest {

    /**
     * Thge lookup service.
     */
    @Autowired
    private ILookupService lookups;

    /**
     * Tests single-level property resolution.
     */
    @Test
    public void testSingleLevelResolution() {
        ObjectSet set = new ObjectSet();
        set.set("firstName", "J");
        set.set("lastName", "Zoo");
        PropertySetResolver resolver = new PropertySetResolver(set, getArchetypeService(), lookups);
        assertEquals("J", resolver.getObject("firstName"));
        assertEquals("Zoo", resolver.getObject("lastName"));
    }

    /**
     * Tests multiple-level property resolution.
     */
    @Test
    public void testMultiLevelResolution() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());

        PropertySetResolver resolver = new PropertySetResolver(set, getArchetypeService(), lookups);
        assertEquals("J", resolver.getObject("act.customer.entity.firstName"));
        assertEquals("Zoo", resolver.getObject("act.customer.entity.lastName"));

        assertEquals("Estimation", resolver.getObject("act.displayName"));
        assertEquals("Act Customer",
                     resolver.getObject("act.customer.displayName"));
        assertEquals("Customer(Person)",
                     resolver.getObject("act.customer.entity.displayName"));
        assertEquals("party.customerperson",
                     resolver.getObject("act.customer.entity.shortName"));
    }

    /**
     * Tests resolution where the property is an object reference.
     */
    @Test
    public void testResolutionByReference() {
        Party party = createCustomer();
        ObjectSet set = new ObjectSet();
        set.set("ref", party.getObjectReference());
        PropertySetResolver resolver = new PropertySetResolver(set, getArchetypeService(), lookups);
        assertEquals("J", resolver.getObject("ref.firstName"));
        assertEquals("Zoo", resolver.getObject("ref.lastName"));
        assertEquals("Customer(Person)",
                     resolver.getObject("ref.displayName"));
        assertEquals("party.customerperson",
                     resolver.getObject("ref.shortName"));
    }

    /**
     * Tests behaviour where an intermediate node doesn't exist.
     */
    @Test
    public void testMissingReference() {
        ActBean act = createAct("act.customerEstimation");
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());
        PropertySetResolver resolver = new PropertySetResolver(set, getArchetypeService(), lookups);
        assertNull(resolver.getObject("act.customer.entity.firstName"));
    }

    /**
     * Tests behaviour where an invalid property name is supplied.
     */
    @Test
    public void testInvalidProperty() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());
        PropertySetResolver resolver = new PropertySetResolver(set, getArchetypeService(), lookups);

        // root node followed by invalid node
        try {
            resolver.getObject("act.customer.invalidNode");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidProperty,
                         exception.getErrorCode());
        }

        // intermediate node followed by invalid node
        try {
            resolver.getObject("act.customer.entity.invalidNode");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidProperty,
                         exception.getErrorCode());
        }

        // leaf node followed by invalid node
        try {
            resolver.getObject("act.startTime.displayName");
            fail("expected PropertyResolverException to be thrown");
        } catch (PropertyResolverException exception) {
            assertEquals(PropertyResolverException.ErrorCode.InvalidObject,
                         exception.getErrorCode());
        }
    }

    /**
     * Tests lookups.
     */
    @Test
    public void testLookups() {
        Party patient = (Party) create("party.patientpet");
        Lookup species = LookupUtil.createLookup(getArchetypeService(), "lookup.species", "CANINE", "Canine");
        save(species);

        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("species", species.getCode());

        ObjectSet set = new ObjectSet();
        set.set("patient", patient);
        PropertySetResolver resolver = new PropertySetResolver(set, getArchetypeService(), lookups);

        assertEquals(species.getCode(), resolver.getObject("patient.species"));
        assertEquals(species.getCode(), resolver.getObject("patient.species.code"));
        assertEquals("Canine", resolver.getObject("patient.species.name"));
        assertEquals(species.getId(), resolver.getObject("patient.species.id"));
        assertEquals("Species", resolver.getObject("patient.species.displayName"));
    }

    /**
     * Tests local lookups.
     */
    @Test
    public void testLocalLookups() {
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);
        ObjectSet set = new ObjectSet();
        set.set("act", act.getAct());
        PropertySetResolver resolver = new PropertySetResolver(set, getArchetypeService(), lookups);

        assertEquals("MR", resolver.getObject("act.customer.entity.title"));
        assertEquals("MR", resolver.getObject("act.customer.entity.title.code"));
        assertEquals("Mr", resolver.getObject("act.customer.entity.title.name"));
    }


    /**
     * Helper to create a new act wrapped in a bean.
     *
     * @param shortName the act short name
     * @return a new act
     */
    private ActBean createAct(String shortName) {
        return new ActBean((Act) create(shortName));
    }

    /**
     * Helper to create and save a customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        IMObjectBean bean = createBean("party.customerperson");
        bean.setValue("title", "MR");
        bean.setValue("firstName", "J");
        bean.setValue("lastName", "Zoo");
        Contact contact = (Contact) create("contact.phoneNumber");
        assertNotNull(contact);
        bean.addValue("contacts", contact);
        bean.save();
        return (Party) bean.getObject();
    }
}
