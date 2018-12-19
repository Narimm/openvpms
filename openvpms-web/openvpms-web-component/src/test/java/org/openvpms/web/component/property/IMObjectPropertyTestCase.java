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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests the {@link IMObjectProperty} class.
 *
 * @author Tim Anderson
 */
public class IMObjectPropertyTestCase extends AbstractPropertyTest {

    /**
     * Tests the {@link IMObjectProperty#getObject()} method.
     */
    @Test
    public void testGetObject() {
        IMObject object = createIMObject();
        IMObjectProperty property = new IMObjectProperty(object, createDescriptor());
        assertSame(property.getObject(), object);
    }

    /**
     * Tests the {@link IMObjectProperty#getDescriptor()} method.
     */
    @Test
    public void testGetDescriptor() {
        NodeDescriptor descriptor = createDescriptor();
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);
        assertSame(property.getDescriptor(), descriptor);
    }

    /**
     * Tests the {@link IMObjectProperty#getDisplayName()} method.
     */
    @Test
    public void testDisplayName() {
        NodeDescriptor descriptor = createDescriptor();
        descriptor.setDisplayName("foo");
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);
        assertEquals("foo", property.getDisplayName());
    }

    /**
     * Tests the {@link IMObjectProperty#getDescription()} method.
     */
    @Test
    public void testDescription() {
        NodeDescriptor descriptor = createDescriptor();
        descriptor.setDescription("foo");
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);
        assertEquals("foo", property.getDescription());
    }

    /**
     * Tests the {@link IMObjectProperty#isLookup()} method.
     */
    @Test
    public void testIsLookup() {
        NodeDescriptor descriptor = createDescriptor();
        descriptor.setType(String.class.getName());
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);
        assertFalse(property.isLookup());
        AssertionDescriptor lookup = new AssertionDescriptor();
        lookup.setName("lookup");
        descriptor.addAssertionDescriptor(lookup);
        assertTrue(property.isLookup());
    }

    /**
     * Tests the {@link IMObjectProperty#isLookup()} method.
     */
    @Test
    public void testIsCollection() {
        NodeDescriptor descriptor = createDescriptor();
        descriptor.setType(String.class.getName());
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);
        assertFalse(property.isCollection());
        descriptor.setType(HashMap.class.getName());
        assertTrue(property.isCollection());
        descriptor.setType(ArrayList.class.getName());
        assertTrue(property.isCollection());
        descriptor.setType(HashSet.class.getName());
        assertTrue(property.isCollection());
    }

    /**
     * Tests the {@link IMObjectProperty#getArchetypeRange()} method.
     */
    @Test
    public void testGetArchetypeRange() {
        NodeDescriptor descriptor = createDescriptor();
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);

        assertEquals(0, property.getArchetypeRange().length);

        // add an archetype range with a single short name
        addArchetypeRange(descriptor, PatientArchetypes.PATIENT);
        assertEquals(1, property.getArchetypeRange().length);
        assertEquals(PatientArchetypes.PATIENT, property.getArchetypeRange()[0]);

        // add an archetype range with a wildcard and verify wildcards are expanded
        addArchetypeRange(descriptor, "act.customerAccountCharges*");
        List<String> values = Arrays.asList(property.getArchetypeRange());
        assertEquals(3, values.size());
        assertTrue(values.contains(CustomerAccountArchetypes.INVOICE));
        assertTrue(values.contains(CustomerAccountArchetypes.CREDIT));
        assertTrue(values.contains(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Tests the {@link IMObjectProperty#isDerived()} method.
     */
    @Test
    public void testIsDerived() {
        NodeDescriptor descriptor = createDescriptor();
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);

        assertFalse(property.isDerived());
        descriptor.setDerived(true);
        assertTrue(property.isDerived());
    }

    /**
     * Tests the {@link IMObjectProperty#isReadOnly()} method.
     */
    @Test
    public void testIsReadOnly() {
        NodeDescriptor descriptor = createDescriptor();
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);

        assertFalse(property.isReadOnly());
        descriptor.setReadOnly(true);
        assertTrue(property.isReadOnly());
    }

    /**
     * Tests the {@link IMObjectProperty#isHidden()} method.
     */
    @Test
    public void testIsHidden() {
        NodeDescriptor descriptor = createDescriptor();
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);

        assertFalse(property.isHidden());
        descriptor.setHidden(true);
        assertTrue(property.isHidden());
    }

    /**
     * Tests the {@link IMObjectProperty#isRequired()} method.
     */
    @Test
    public void testIsRequired() {
        NodeDescriptor descriptor = createDescriptor();
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);

        assertFalse(property.isRequired());
        descriptor.setMinCardinality(1);
        assertTrue(property.isRequired());
    }

    /**
     * Tests the {@link IMObjectProperty#getMinCardinality()} method.
     */
    @Test
    public void testGetMinCardinality() {
        NodeDescriptor descriptor = createDescriptor();
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);

        assertEquals(0, property.getMinCardinality());
        descriptor.setMinCardinality(1);
        assertEquals(1, property.getMinCardinality());
    }

    /**
     * Tests the {@link IMObjectProperty#getMaxCardinality()} method.
     */
    @Test
    public void testGetMaxCardinality() {
        NodeDescriptor descriptor = createDescriptor();
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);

        // check default
        assertEquals(1, property.getMaxCardinality());

        // change cardinality
        descriptor.setMaxCardinality(10);
        assertEquals(10, property.getMaxCardinality());

        // check unbounded
        descriptor.setMaxCardinality(NodeDescriptor.UNBOUNDED);
        assertEquals(-1, property.getMaxCardinality());
    }

    /**
     * Verifies that {@code UnsupportedOperationException} is raised for attempts to change derived properties.
     */
    @Test
    public void testUnsupportedOperationExceptionForDerivedProperty() {
        NodeDescriptor descriptor = createDescriptor();
        descriptor.setName("derived");
        descriptor.setDerived(true);
        IMObjectProperty property = new IMObjectProperty(createIMObject(), descriptor);

        try {
            property.setValue("Foo");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected behaviour
        }

        try {
            property.add("Foo");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected behaviour
        }

        try {
            property.remove("Foo");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected behaviour
        }
    }

    /**
     * Checks validation where a property is mandatory.
     */
    @Test
    public void testRequiredValidation() {
        Party person = (Party) create(CustomerArchetypes.PERSON);
        NodeDescriptor descriptor = PropertyTestHelper.getDescriptor(person, "lastName");
        IMObjectProperty property = new IMObjectProperty(person, descriptor);

        property.setValue(null);
        String error = Messages.format("property.error.required", property.getDisplayName());
        checkValidationError(property, error);
        property.setValue("X");
        assertTrue(property.isValid());

        property.setValue("");
        checkValidationError(property, error);
    }

    /**
     * Checks validation for minimum cardinality.
     */
    @Test
    public void testMinCardinalityValidation() {
        Party customer = TestHelper.createCustomer(false);
        Act invoice = (Act) create(CustomerAccountArchetypes.INVOICE);
        Participation participation = createCustomerParticipation(invoice, customer);

        NodeDescriptor descriptor = PropertyTestHelper.getDescriptor(invoice, "customer");
        assertEquals(1, descriptor.getMinCardinality());

        IMObjectProperty property = new IMObjectProperty(invoice, descriptor);
        assertFalse(property.isValid());
        String error = Messages.format("property.error.minSize", property.getDisplayName(), property.getMinCardinality());
        checkValidationError(property, error);
        property.add(participation);
        assertTrue(property.isValid());

        property.remove(participation);
        checkValidationError(property, error);
    }

    /**
     * Checks validation for maximum cardinality.
     */
    @Test
    public void testMaxCardinalityValidation() {
        Party customer = TestHelper.createCustomer(false);
        Act invoice = (Act) create(CustomerAccountArchetypes.INVOICE);
        Participation participation1 = createCustomerParticipation(invoice, customer);
        Participation participation2 = createCustomerParticipation(invoice, customer);

        NodeDescriptor descriptor = PropertyTestHelper.getDescriptor(invoice, "customer");
        assertEquals(1, descriptor.getMaxCardinality());

        IMObjectProperty property = new IMObjectProperty(invoice, descriptor);
        assertFalse(property.isValid());
        String error = Messages.format("property.error.maxSize", property.getDisplayName(), property.getMinCardinality());
        property.add(participation1);
        property.add(participation2);
        checkValidationError(property, error);

        property.remove(participation1);
        assertTrue(property.isValid());
    }

    /**
     * Tests that collection items are validated.
     */
    @Test
    public void testValidationOfInvalidCollectionItems() {
        Party customer = TestHelper.createCustomer(false);
        Act invoice = (Act) create(CustomerAccountArchetypes.INVOICE);
        Participation participation = createCustomerParticipation(invoice, customer);

        NodeDescriptor descriptor = PropertyTestHelper.getDescriptor(invoice, "customer");
        IMObjectProperty property = new IMObjectProperty(invoice, descriptor);

        participation.setAct(null); // act is mandatory
        property.add(participation);
        String error = "Failed to validate Act of Act Customer: value is required";  // archetype service error
        checkPreformattedValidationError(property, error);

        participation.setAct(invoice.getObjectReference());
        assertTrue(property.isValid());
    }

    /**
     * Verifies that assertions are evaluated by {@link IMObjectProperty#validate(Validator)}.
     */
    @Test
    public void testValidateAssertions() {
        IMObjectProperty property = createProperty("name", String.class);
        NodeDescriptor descriptor = property.getDescriptor();
        descriptor.setPath("/name");
        AssertionDescriptor assertion = new AssertionDescriptor();
        assertion.setDescriptor(getArchetypeService().getAssertionTypeDescriptor("regularExpression"));
        assertNotNull(assertion.getDescriptor());
        assertion.setName("regularExpression");
        AssertionProperty ap = new AssertionProperty();
        ap.setName("expression");
        ap.setValue("\\d+");
        assertion.addProperty(ap);
        descriptor.addAssertionDescriptor(assertion);
        property.setValue("abc");
        assertFalse(property.validate(new DefaultValidator()));
        property.setValue("123");
        assertTrue(property.validate(new DefaultValidator()));
    }

    /**
     * Tests the {@link Property#isEmpty()} method for collection properties.
     */
    public void testIsEmptyCollection() {
        Party customer = (Party) create(CustomerArchetypes.PERSON);
        NodeDescriptor descriptor = PropertyTestHelper.getDescriptor(customer, "contacts");
        IMObjectProperty property = new IMObjectProperty(customer, descriptor);
        assertTrue(property.isEmpty());
        property.add(TestHelper.createPhoneContact("03", "1234567"));
        assertFalse(property.isEmpty());
    }

    /**
     * Creates a boolean property.
     *
     * @param name the property name
     * @return a new boolean property
     */
    protected Property createBooleanProperty(String name) {
        return createProperty(name, "active", TestHelper.createCustomer(false));
    }

    /**
     * Creates a string property.
     *
     * @param name      the property name
     * @param minLength the minimum length
     * @param maxLength the maximum length
     * @return a new string property
     */
    protected Property createStringProperty(String name, int minLength, int maxLength) {
        IMObjectProperty result = createProperty(name, "lastName", TestHelper.createCustomer(false));
        result.getDescriptor().setMinLength(minLength);
        result.getDescriptor().setMaxLength(maxLength);
        return result;
    }

    /**
     * Creates a property of the specified type.
     *
     * @param name the property name
     * @param type the property type
     * @return a new property
     */
    protected IMObjectProperty createProperty(String name, Class type) {
        IMObject object = createIMObject();
        object.setArchetypeId(new ArchetypeId(CustomerArchetypes.PERSON));
        NodeDescriptor descriptor = createDescriptor();
        descriptor.setName(name);
        descriptor.setType(type.getName());
        return new IMObjectProperty(object, descriptor);
    }

    /**
     * Creates a string property.
     *
     * @param minLength the minimum length
     * @param maxLength the maximum length
     * @return a new property
     */
    protected TestProperty createTestProperty(int minLength, int maxLength) {
        Party customer = TestHelper.createCustomer(false);
        NodeDescriptor descriptor = cloneDescriptor("foo", "lastName", customer);
        descriptor.setMinLength(minLength);
        descriptor.setMaxLength(maxLength);
        return new IMObjectTestProperty(customer, descriptor);
    }

    /**
     * Verifies a validation error matches that expected.
     * <p/>
     * This implementation reformats it according to the property archetype and node name before delegating to the
     * parent implementation.
     *
     * @param property the property to check
     * @param message  the expected validation error message
     */
    @Override
    protected void checkValidationError(Property property, String message) {
        IMObjectProperty p = (IMObjectProperty) property;
        IMObject parent = (IMObject) p.getObject();
        String expected = ValidatorError.format(parent.getArchetypeId().getShortName(), property.getName(), message);
        super.checkValidationError(property, expected);
    }

    private IMObject createIMObject() {
        return new IMObject();
    }

    /**
     * Creates a new node descriptor.
     *
     * @return a new node descriptor
     */
    private NodeDescriptor createDescriptor() {
        return new org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor();
    }

    /**
     * Creates a new property.
     *
     * @param name     the property name
     * @param nodeName the node to use as a template
     * @param object   the object
     * @return a new property
     */
    private IMObjectProperty createProperty(String name, String nodeName, IMObject object) {
        NodeDescriptor descriptor = cloneDescriptor(name, nodeName, object);
        return new IMObjectProperty(object, descriptor);
    }

    /**
     * Clones a node descriptor, assigning it a new name.
     * <p/>
     * This is to avoid affected descriptors cached by the archetype service.
     *
     * @param name     the new name
     * @param nodeName the existing node name
     * @param object   the object
     * @return the new descriptor
     */
    private NodeDescriptor cloneDescriptor(String name, String nodeName, IMObject object) {
        NodeDescriptor descriptor = PropertyTestHelper.getDescriptor(object, nodeName);
        try {
            org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor d
                    = (org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor) descriptor;
            descriptor = (NodeDescriptor) d.clone(); // clone it to avoid affecting cached descriptor
        } catch (CloneNotSupportedException exception) {
            fail("Failed to clone descriptor");
        }
        descriptor.setName(name);
        return descriptor;
    }

    /**
     * Verifies a preformatted validation error matches that expected.
     *
     * @param property the property to check
     * @param message  the expected validation error message
     */
    private void checkPreformattedValidationError(Property property, String message) {
        super.checkValidationError(property, message);
    }

    /**
     * Helper to create a new customer participation.
     *
     * @param act      the act
     * @param customer the customer
     * @return a new customer participation
     */
    private Participation createCustomerParticipation(Act act, Party customer) {
        Participation participation = (Participation) create(CustomerArchetypes.CUSTOMER_PARTICIPATION);
        participation.setAct(act.getObjectReference());
        participation.setEntity(customer.getObjectReference());
        return participation;
    }

    /**
     * Adds an archetype range assertion to a node descriptor.
     *
     * @param descriptor the node descriptor
     * @param value      the short name to add
     */
    private void addArchetypeRange(NodeDescriptor descriptor, String value) {
        AssertionDescriptor range = new AssertionDescriptor();
        range.setName("archetypeRange");
        PropertyList archetypes = new PropertyList();
        archetypes.setName("archetypes");
        PropertyMap archetype = new PropertyMap();
        archetype.setName("archetype");
        AssertionProperty shortName = new AssertionProperty();
        shortName.setName("shortName");
        shortName.setValue(value);
        archetype.addProperty(shortName);
        archetypes.addProperty(archetype);
        range.addProperty(archetypes);
        descriptor.addAssertionDescriptor(range);
    }

    private class IMObjectTestProperty extends IMObjectProperty implements TestProperty {

        /**
         * The count of validations.
         */
        private int count = 0;

        /**
         * Constructs an {@code IMObjectTestProperty}.
         *
         * @param object     the object that the property belongs to
         * @param descriptor the property descriptor
         */
        IMObjectTestProperty(IMObject object, NodeDescriptor descriptor) {
            super(object, descriptor);
        }

        /**
         * Returns the no. of times validation has been invoked.
         *
         * @return the no. of times validation has been invoked
         */
        public int getValidations() {
            return count;
        }

        /**
         * Validates the object.
         *
         * @param validator the validator
         * @return {@code true} if the object and its descendants are valid otherwise {@code false}
         */
        @Override
        protected boolean doValidation(Validator validator) {
            ++count;
            return super.doValidation(validator);
        }
    }

}
