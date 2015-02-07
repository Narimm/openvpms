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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.cache.SoftRefIMObjectCache;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ProductTemplateExpander}.
 *
 * @author Tim Anderson
 */
public class ProductTemplateExpanderTestCase extends AbstractAppTest {

    /**
     * Tests template expansion.
     */
    @Test
    public void testExpand() {
        Product productX = TestHelper.createProduct();
        Product productY = TestHelper.createProduct();
        Product productZ = TestHelper.createProduct();

        Product templateA = createTemplate("templateA");
        Product templateB = createTemplate("templateB");
        Product templateC = createTemplate("templateC");

        addInclude(templateA, templateB, 1);
        addInclude(templateA, templateC, 2);
        addInclude(templateB, productX, 5);
        addInclude(templateB, productY, 2);
        addInclude(templateC, productX, 1);
        addInclude(templateC, productZ, 10);

        Map<Product, BigDecimal> includes = expand(templateA);
        assertEquals(3, includes.size());

        checkInclude(includes, productX, 7);
        checkInclude(includes, productY, 2);
        checkInclude(includes, productZ, 20);
    }

    /**
     * Verifies that no includes are returned if a template is included recursively.
     */
    @Test
    public void testRecursion() {
        Product productX = TestHelper.createProduct();

        Product templateA = createTemplate("templateA");
        Product templateB = createTemplate("templateB");
        Product templateC = createTemplate("templateC");

        addInclude(templateA, templateB, 1);
        addInclude(templateA, productX, 1);
        addInclude(templateB, templateC, 1);
        addInclude(templateB, productX, 1);
        addInclude(templateC, templateA, 1);
        addInclude(templateC, productX, 1);

        Map<Product, BigDecimal> includes = expand(templateA);
        assertEquals(0, includes.size());
    }

    /**
     * Expands a template.
     *
     * @param template the template to expand
     * @return the expanded template
     */
    private Map<Product, BigDecimal> expand(Product template) {
        ProductTemplateExpander expander = new ProductTemplateExpander();
        return expander.expand(template, new SoftRefIMObjectCache(getArchetypeService()));
    }


    /**
     * Verifies a product and quantity is included.
     *
     * @param includes the includes
     * @param product  the expected product
     * @param quantity the expected quantity
     */
    private void checkInclude(Map<Product, BigDecimal> includes, Product product, int quantity) {
        BigDecimal value = includes.get(product);
        checkEquals(BigDecimal.valueOf(quantity), value);
    }

    /**
     * Creates a template.
     *
     * @param name the template name
     * @return a new template
     */
    private Product createTemplate(String name) {
        Product template = (Product) create(ProductArchetypes.TEMPLATE);
        template.setName(name);
        save(template);
        return template;
    }

    /**
     * Adds an include to the template.
     *
     * @param template the template
     * @param include  the product to include
     * @param quantity the include quantity
     */
    private void addInclude(Product template, Product include, int quantity) {
        EntityBean bean = new EntityBean(template);
        IMObjectRelationship relationship = bean.addNodeTarget("includes", include);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("includeQty", quantity);
        bean.save();
    }

}
