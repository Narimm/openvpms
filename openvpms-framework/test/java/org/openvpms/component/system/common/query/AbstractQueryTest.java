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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.system.common.query;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.junit.Before;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.Iterator;


/**
 * Abstract base class for query tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("/org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml")
public class AbstractQueryTest extends AbstractArchetypeServiceTest {

    /**
     * The no. of acts.
     */
    private final int actCount = 10;

    /**
     * The name assigned to each act in a test run.
     */
    private String name;


    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Before
    public void setUp() throws Exception {
        if (name == null) {
            name = "QueryTest" + System.currentTimeMillis();
            for (int i = 0; i < actCount; ++i) {
                Act act = (Act) create("act.simple");
                ActBean bean = new ActBean(act);
                bean.setValue("startTime", new Date());
                bean.setValue("name", name);
                bean.save();
            }
        }
    }

    /**
     * Creates a new query for all 'act.simple' acts with a particular name.
     *
     * @return a new query
     */
    protected ArchetypeQuery createQuery() {
        ShortNameConstraint constraint = new ShortNameConstraint("act", "act.simple");
        ArchetypeQuery query = new ArchetypeQuery(constraint);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        query.add(new NodeConstraint("name", name));
        return query;
    }

    /**
     * Verfies that an iterator has the expected no. of elements and that
     * each element passes a user supplied check.
     *
     * @param iterator the iterator
     * @param check    the check
     */
    protected <T> void checkIterator(Iterator<T> iterator, Check<T> check) {
        int count = 0;
        while (iterator.hasNext()) {
            T object = iterator.next();
            assertNotNull(object);
            check.check(object);
            ++count;
        }
        assertEquals(actCount, count);
    }

    interface Check<T> {

        void check(T object);
    }
}
