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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.manager;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.plugin.test.service.TestService;
import org.openvpms.plugin.test.service.impl.TestServiceImpl;
import org.openvpms.plugins.test.api.TestPlugin;
import org.openvpms.plugins.test.impl.TestPluginImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link PluginManager}.
 *
 * @author Tim Anderson
 */
public class PluginManagerTestCase extends ArchetypeServiceTest {

    /**
     * Verifies that services can be provided to a plugin, and that the plugin can call them.
     * <p>
     * This provides an {@link TestService} and {@link IArchetypeService} to the {@link TestPluginImpl} plugin,
     * which calls the {@link TestService} with the value of the practice name, as determined from
     * the {@link IArchetypeService}.
     *
     * @throws Exception for any error
     */
    @Test
    public void testPluginManager() throws Exception {
        final TestServiceImpl service = new TestServiceImpl();
        assertNull(service.getValue());

        String name = TestHelper.getPractice().getName();
        assertNotNull(name);

        // provide the TestService and ArchetypeService to the plugin
        PluginServiceProvider provider = new PluginServiceProvider() {
            public List<ServiceRegistration<?>> provide(BundleContext context) {
                ServiceRegistration<?> testService = context.registerService(TestService.class.getName(), service,
                        new Hashtable<String, Object>());
                ServiceRegistration<?> archetypeService = context.registerService(IArchetypeService.class.getName(),
                        getArchetypeService(),
                        new Hashtable<String, Object>());
                return Arrays.asList(testService, archetypeService);
            }
        };

        // start the plugin manager
        PluginManager manager = new PluginManager(FelixHelper.getFelixDir(), provider);
        manager.start();
        for (int i = 0; i < 20; ++i) {
            if (StringUtils.equals(name, service.getValue())) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }

        // verify the service was called by the plugin, with the practice name
        assertEquals(name, service.getValue());

        for (int i = 0; i < 20; ++i) {
            if (manager.getService(TestPlugin.class) != null) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }

        // now verify the plugin was exported
        assertNotNull(manager.getService(TestPlugin.class));

        manager.destroy();
    }

}
