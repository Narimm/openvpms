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

package org.openvpms.component.business.dao.hibernate.im.common;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateUtil;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Classification;

/**
 * Test the hierarchical Classification data structure
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentClassificationTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentClassificationTestCase.class);
    }

    /**
     * Constructor for ClassficationTestCase.
     * 
     * @param name
     */
    public PersistentClassificationTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test creation of a simple classification with no parent
     */
    public void testSimpleClassificationCreation() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            int acount = HibernateUtil.getTableRowCount(session, "classification");

            Classification classification = createClassification();

            tx = session.beginTransaction();
            session.save(classification);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernateUtil
                    .getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 1);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Create a classification with children and then test that we can nagivate.
     */
    public void testSingleLevelClassificationHierarchy() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            int acount = HibernateUtil.getTableRowCount(
                    session, "classification");

            tx = session.beginTransaction();
            Classification parent = createClassification();
            
            Classification child1 = createClassification();
            Classification child2 = createClassification();
            Classification child3 = createClassification();
            
            parent.addChild(child1);
            parent.addChild(child2);
            parent.addChild(child3);
            session.saveOrUpdate(parent);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernateUtil
                    .getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 4);

            // retrieve the classification and ensure there are three children
            Classification original = (Classification) session.load(
                    Classification.class, new Long(parent.getUid()));
            assertTrue(original.getChildren().size() == 3);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test single level with a deletion
     */
    public void testSingleLevelClassificationHierarchyWithDelete()
            throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            int acount = HibernateUtil
            .getTableRowCount(session, "classification");

            tx = session.beginTransaction();
            Classification parent = createClassification();

            Classification child1 = createClassification();
            Classification child2 = createClassification();
            Classification child3 = createClassification();
            
            parent.addChild(child1);
            parent.addChild(child2);
            parent.addChild(child3);
            session.saveOrUpdate(parent);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernateUtil
                    .getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 4);

            // retrieve the classification and ensure there are three children
            tx = session.beginTransaction();
            Classification original = (Classification) session.load(
                    Classification.class, parent.getUid());
            assertTrue(original.getChildren().size() == 3);

            Classification achild = original.getChildren().iterator().next();
            assertTrue(original.removeChild(achild));
            session.saveOrUpdate(original);
            tx.commit();

            // check that there is one less entry than before
            acount1 = HibernateUtil
            .getTableRowCount(session, "classification");
            assertTrue(acount1 == acount + 3);

            // no retrieve the classification again and check the number of
            // children
            original = (Classification) session.load(Classification.class,
                    parent.getUid());
            assertTrue(original.getChildren().size() == 2);

        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    public void testMultipleLevelClassificationHierarchy() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            int levels = ((Integer) this.getTestData().getTestCaseParameter(
                    "testMultipleLevelClassificationHierarchy", "normal",
                    "levels")).intValue();
            int childrenPerLevel = ((Integer) this.getTestData()
                    .getTestCaseParameter(
                            "testMultipleLevelClassificationHierarchy",
                            "normal", "childrenPerLevel")).intValue();

            tx = session.beginTransaction();
            Classification root = createClassification();
            session.saveOrUpdate(root);
            createClassificationHierarchy(session, root, childrenPerLevel, 0, levels);
            tx.commit();

            // retrieve the root
            root = (Classification)session.load(Classification.class, root.getUid());
            assertTrue(getClassification(root, 0, levels - 1)
                    .getChildren().size() == childrenPerLevel);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Return a default classification.
     * 
     * @return Classification
     * @thorws Exception
     */
    private Classification createClassification() throws Exception {
        return new Classification(createArchetypeId(), null, null);
    }

    /**
     * Create a classification hierarchy
     * 
     * @param session
     *            the session to use
     * @param parent
     *            the parent classification
     * @param numOfChildren
     *            number of children to add per level
     * @param level
     *            the level in the hierarchy
     * @param maxLevels
     *            the maximum number of levels in the hierarchy
     * @throws Exception
     *             propagate exception to caller
     */
    public void createClassificationHierarchy(Session session,
            Classification parent, int numOfChildren, int level, int maxLevels)
            throws Exception {
        for (int cindex = 0; cindex < numOfChildren; cindex++) {
            Classification child = createClassification();
            parent.addChild(child);
            if ((level + 1) < maxLevels) {
                createClassificationHierarchy(session, child, numOfChildren,
                        (level + 1), maxLevels);
            }
            session.saveOrUpdate(parent);
        }
    }
    
    /**
     * Return the classification at the specified level in the hierarchy
     * 
     * @param root
     *            the root classification
     * @param currentLevel
     *            the current level
     * @param targetLevel
     *            the final level           
     */
    public Classification getClassification(Classification root, int currentLevel, 
            int targetLevel) {
        if (currentLevel == targetLevel) {
            return root;
        } else {
            return getClassification(root.getChildren().iterator().next(), 
                    currentLevel + 1, targetLevel);
        }
    }
    
    /**
     * Return the archetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createArchetypeId() {
        return new ArchetypeId("openvpms-party-classification.classification.1.0");
    }
}
