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

package org.openvpms.web.component.edit;

import java.math.BigDecimal;

import nextapp.echo2.app.ApplicationInstance;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.app.OpenVPMSApp;
import org.openvpms.web.component.im.util.DescriptorHelper;


/**
 * {@link NumericPropertyHandler} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NumericPropertyHandlerTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Integer node.
     */
    private NodeDescriptor _intNode;

    /**
     * BigDecimal node.
     */
    private NodeDescriptor _decNode;


    /**
     * Tests {@link PropertyHandler#isValid} for an integer node.
     */
    public void testIntegerIsValid() {
        NumericPropertyHandler handler = new NumericPropertyHandler(_intNode);

        // test string validity
        assertFalse(handler.isValid("abc"));
        assertTrue(handler.isValid("1"));
        assertFalse(handler.isValid("1.0"));
        // invalid as Integer throws NumberFormatException

        // test numeric validity
        assertTrue(handler.isValid(new Long(1)));
        assertTrue(handler.isValid(new BigDecimal(1.0)));
        assertTrue(handler.isValid(new Double(1.5)));
        // valid as Double provides toInt()
    }

    /**
     * Tests {@link PropertyHandler#apply} for an integer node.
     */
    public void testIntegerApply() {
        final Integer one = 1;
        NumericPropertyHandler handler = new NumericPropertyHandler(_intNode);

        // test string conversions
        try {
            handler.apply("abc");
        } catch (ValidationException exception) {
            assertFalse(exception.getErrors().isEmpty());
        }

        Integer int1 = (Integer) handler.apply("1");
        assertEquals(one, int1);

        try {
            handler.apply("1.0");
        } catch (ValidationException exception) {
            // see comments in testIntegerIsValid
            assertFalse(exception.getErrors().isEmpty());
        }

        // test numeric conversions
        assertEquals(one, handler.apply(new Long(1)));
        assertEquals(one, handler.apply(new BigDecimal(1.0)));
        assertEquals(one, handler.apply(new Double(1.5)));
    }

    /**
     * Tests {@link PropertyHandler#isValid} for a BigDecimal node.
     */
    public void testDecimalIsValid() {
        NumericPropertyHandler handler = new NumericPropertyHandler(_decNode);

        // test string validity
        assertFalse(handler.isValid("abc"));
        assertTrue(handler.isValid("1"));
        assertTrue(handler.isValid("1.0"));

        // test numeric validity
        assertTrue(handler.isValid(new Long(1)));
        assertTrue(handler.isValid(new BigDecimal(1.0)));
        assertTrue(handler.isValid(new Double(1.5)));
    }

    /**
     * Tests {@link PropertyHandler#apply} for an integer node.
     */
    public void testDecimalApply() {
        final BigDecimal one = new BigDecimal("1.0");
        final BigDecimal half = new BigDecimal("0.5");
        NumericPropertyHandler handler = new NumericPropertyHandler(_decNode);

        // test string conversions
        try {
            handler.apply("abc");
        } catch (ValidationException exception) {
            assertFalse(exception.getErrors().isEmpty());
        }

        // Note: BigDecimal.compareTo() is used instead of equals as equals
        // considers equal values with different scales to be different.
        BigDecimal dec1 = (BigDecimal) handler.apply("0.5");
        assertTrue(half.compareTo(dec1) == 0);

        BigDecimal dec2 = (BigDecimal) handler.apply("1.0");
        assertTrue(one.compareTo(dec2) == 0);

        // test numeric conversions
        BigDecimal dec3 = (BigDecimal) handler.apply(new Long(1));
        assertTrue(one.compareTo(dec3) == 0);

        BigDecimal dec4 = (BigDecimal) handler.apply(new BigDecimal(0.5));
        assertTrue(half.compareTo(dec4) == 0);

        BigDecimal dec5 = (BigDecimal) handler.apply(new Double(0.5));
        assertTrue(half.compareTo(dec5) == 0);
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }


    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        OpenVPMSApp app = (OpenVPMSApp) applicationContext.getBean("app");
        app.setApplicationContext(applicationContext);
        ApplicationInstance.setActive(app);

        // get the node descriptors
        _intNode = getDescriptor("classification.appointmentType", "noSlots");
        assertEquals(Integer.class, _intNode.getClazz());

        _decNode = getDescriptor("productPrice.fixedPrice", "price");
        assertEquals(BigDecimal.class, _decNode.getClazz());
    }

    /**
     * Helper to return a node descriptor.
     *
     * @param archetype the archetype name
     * @param node      the node name
     * @return the node descriptor
     */
    protected NodeDescriptor getDescriptor(String archetype, String node) {
        ArchetypeDescriptor type
                = DescriptorHelper.getArchetypeDescriptor(archetype);
        assertNotNull(type);
        NodeDescriptor result = type.getNodeDescriptor(node);
        assertNotNull(result);
        return result;
    }

}
