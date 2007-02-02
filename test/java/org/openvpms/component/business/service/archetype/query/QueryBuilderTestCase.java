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

package org.openvpms.component.business.service.archetype.query;

import org.hibernate.HibernateException;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.ArchetypeProperty;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeSortConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.LongNameConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Tests the {@link QueryBuilder} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class QueryBuilderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the entity service
     */
    private QueryBuilder builder;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(QueryBuilderTestCase.class);
    }

    /**
     * Default constructor
     */
    public QueryBuilderTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/query/query-appcontext.xml"
        };
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.builder = (QueryBuilder) applicationContext.getBean(
                "queryBuilder");
    }

    /**
     * Test the query by uid and archetype Id
     */
    public void testQueryByUidAndArchetypeId()
            throws Exception {
        final String expected = "select party0 "
                + "from " + Party.class.getName() + " as party0 "
                + "where (party0.archetypeId.rmName = :rmName0 and "
                + "party0.archetypeId.entityName = :entityName0 and "
                + "party0.archetypeId.concept = :concept0 and "
                + "party0.uid = :uid0)";
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("openvpms-party-person.person.1.0"), false)
                .add(new NodeConstraint("uid", "1"));
        checkQuery(query, expected);
    }

    /**
     * Test the query by uid and archetype short name.
     */
    public void testQueryByUidAndArchetypeShortName()
            throws Exception {
        final String expected = "select party0 "
                + "from " + Party.class.getName() + " as party0 "
                + "where ((party0.archetypeId.entityName = :entityName0 and "
                + "party0.archetypeId.concept = :concept0) and "
                + "party0.uid = :uid0)";
        ArchetypeQuery query = new ArchetypeQuery("person.person", false, false)
                .add(new NodeConstraint("uid", "1"));
        checkQuery(query, expected);
    }

    /**
     * Test the query by archetype id and name.
     */
    public void testQueryByArchetypeIdAndName()
            throws Exception {
        final String expected = "select party0 "
                + "from " + Party.class.getName() + " as party0 "
                + "where (party0.archetypeId.rmName = :rmName0 and "
                + "party0.archetypeId.entityName = :entityName0 and "
                + "party0.archetypeId.concept = :concept0 and "
                + "party0.name like :name0)";
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("openvpms-party-person.person.1.0"), false)
                .add(new NodeConstraint("name", "sa*"));
        checkQuery(query, expected);
    }

    /**
     * Test the query on archetype id, name and a sort criteria.
     */
    public void testQueryByArchetypeIdAndNameAndSort()
            throws Exception {
        final String expected = "select party0 "
                + "from " + Party.class.getName() + " as party0 "
                + "inner join party0.contacts as contacts0 "
                + "where (party0.archetypeId.rmName = :rmName0 and "
                + "party0.archetypeId.entityName = :entityName0 and "
                + "party0.archetypeId.concept = :concept0 and "
                + "party0.name like :name0 and "
                + "(contacts0.archetypeId.entityName = :entityName1 and "
                + "contacts0.archetypeId.concept = :concept1)) "
                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeId("openvpms-party-person.person.1.0"), false)
                .add(new NodeConstraint("name", "sa*"))
                .add(new NodeSortConstraint("name", true))
                .add(new CollectionNodeConstraint("contacts",
                                                  new ShortNameConstraint(
                                                          "contact.location",
                                                          false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test the query on archetype short name, name and a sort criteria
     */
    public void testQueryByArchetypeShortNameAndNameAndSort()
            throws Exception {
        final String expected = "select party0 "
                + "from " + Party.class.getName() + " as party0 "
                + "inner join party0.contacts as contacts0 "
                + "where (((party0.archetypeId.entityName = :entityName0 and "
                + "party0.archetypeId.concept = :concept0) or "
                + "(party0.archetypeId.entityName = :entityName1 and "
                + "party0.archetypeId.concept = :concept1)) and "
                + "party0.uid = :uid0 and party0.name like :name0 and "
                + "(contacts0.archetypeId.entityName = :entityName2 and "
                + "contacts0.archetypeId.concept = :concept2)) "
                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint(
                        new String[]{"person.person", "organization.organization"},
                        false, false))
                .add(new NodeConstraint("uid", "1"))
                .add(new NodeConstraint("name", "sa*"))
                .add(new NodeSortConstraint("name", true))
                .add(new CollectionNodeConstraint("contacts",
                                                  new ShortNameConstraint(
                                                          "contact.location",
                                                          false, false)));
        checkQuery(query, expected);
    }

    /**
     * Test the query on a collection constraint without specifying
     * archetype constraint info.
     */
    public void testCollectionNodeConstraintWithNodeNameOnly()
            throws Exception {
        final String expected = "select party0 "
                + "from " + Party.class.getName() + " as party0 "
                + "inner join party0.contacts as contacts0 "
                + "where (((party0.archetypeId.entityName = :entityName0 and "
                + "party0.archetypeId.concept = :concept0) or "
                + "(party0.archetypeId.entityName = :entityName1 and "
                + "party0.archetypeId.concept = :concept1)) and "
                + "party0.uid = :uid0 and party0.name like :name0 and "
                + "(contacts0.archetypeId.entityName = :entityName2 and "
                + "contacts0.archetypeId.concept like :concept2)) "
                + "order by party0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint(
                        new String[]{"person.person", "organization.organization"},
                        false, false))
                .add(new NodeConstraint("uid", "1"))
                .add(new NodeConstraint("name", "sa*"))
                .add(new NodeSortConstraint("name", true))
                .add(new CollectionNodeConstraint(
                        "contacts", new ShortNameConstraint("contact.*", false,
                                                            false)));
        checkQuery(query, expected);
    }

    /**
     * Test query with an archetype and node based sort constraint.
     */
    public void testWithMultipleSortConstraints()
            throws Exception {
        String expected = "select party0 "
                + "from " + Party.class.getName() + " as party0 "
                + "inner join party0.contacts as contacts0 "
                + "where (((party0.archetypeId.entityName = :entityName0 and "
                + "party0.archetypeId.concept = :concept0) or "
                + "(party0.archetypeId.entityName = :entityName1 and "
                + "party0.archetypeId.concept = :concept1)) and "
                + "party0.uid = :uid0 and party0.name like :name0 and "
                + "(contacts0.archetypeId.entityName = :entityName2 and "
                + "contacts0.archetypeId.concept like :concept2)) "
                + "order by party0.name asc, party0.archetypeId.concept asc, "
                + "contacts0.archetypeId.concept desc";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint(
                        new String[]{"person.person", "organization.organization"},
                        false, false))
                .add(new NodeConstraint("uid", "1"))
                .add(new NodeConstraint("name", "sa*"))
                .add(new NodeSortConstraint("name", true))
                .add(new ArchetypeSortConstraint(ArchetypeProperty.ConceptName,
                                                 true))
                .add(new CollectionNodeConstraint(
                        "contacts",
                        new ShortNameConstraint("contact.*", false,
                                                false))
                        .add(new ArchetypeSortConstraint(
                        ArchetypeProperty.ConceptName, false)));
        checkQuery(query, expected);
    }

    /**
     * Test target lookups.
     */
    public void testTargetLookups()
            throws Exception {
        final String expected = "select lookup0 "
                + "from " + Lookup.class.getName() + " as lookup0 "
                + "inner join lookup0.targetLookupRelationships "
                + "as targetLookupRelationships0 "
                + "where (lookup0.archetypeId.entityName = :entityName0) "
                + "order by lookup0.name asc";
        ArchetypeQuery query = new ArchetypeQuery(
                new LongNameConstraint(
                        null, "lookup", null, false, false))
                .add(new CollectionNodeConstraint("target"))
                .add(new NodeSortConstraint("name", true))
                .setFirstResult(0)
                .setMaxResults(-1);

        checkQuery(query, expected);
    }

    /**
     * Test for ovpms-240
     */
    public void testOVPMS240()
            throws Exception {
        final String expected = "select product0 "
                + "from " + Product.class.getName() + " "
                + "as product0 "
                + "inner join product0.classifications as classifications0 "
                + "where ((product0.archetypeId.entityName = :entityName0 and "
                + "product0.archetypeId.concept = :concept0) and "
                + "product0.active = :active0 and "
                + "(classifications0.archetypeId.entityName = :entityName1 and "
                + "classifications0.archetypeId.concept = :concept1) and "
                + "(classifications0.code = :code0 or "
                + "classifications0.code = :code1))";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint("product.product", false,
                                        true))
                .add(new CollectionNodeConstraint(
                        "classifications",
                        new ShortNameConstraint("lookup.staff",
                                                false, false))
                        .add(new OrConstraint()
                        .add(new NodeConstraint("code", "equine"))
                        .add(new NodeConstraint("code", "all"))));
        checkQuery(query, expected);
    }

    /**
     * Test for ovpms-245
     */
    public void testOVPMS245()
            throws Exception {
        final String expected = "select product0 "
                + "from " + Product.class.getName() + " as product0 "
                + "left outer join product0.classifications as classifications0"
                + " where ((product0.archetypeId.entityName = :entityName0 and "
                + "product0.archetypeId.concept = :concept0) and "
                + "(classifications0.active = :active0 or "
                + "classifications0.active is NULL) and "
                + "(classifications0.archetypeId.concept is NULL or "
                + "(classifications0.archetypeId.concept = :concept1 and "
                + "classifications0.name = :name0)))";
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint("product.product", false,
                                        false))
                .add(new CollectionNodeConstraint("classifications", false)
                        .setJoinType(JoinConstraint.JoinType.LeftOuterJoin)
                        .add(new OrConstraint()
                                .add(new NodeConstraint("active",
                                                        RelationalOp.EQ, true))
                                .add(new NodeConstraint("active",
                                                        RelationalOp.IsNULL)))
                        .add(new OrConstraint()
                        .add(new ArchetypeNodeConstraint(
                                ArchetypeProperty.ConceptName,
                                RelationalOp.IsNULL))
                        .add(new AndConstraint()
                        .add(new ArchetypeNodeConstraint(
                                ArchetypeProperty.ConceptName, RelationalOp.EQ,
                                "species"))
                        .add(new NodeConstraint("name", RelationalOp.EQ,
                                                "Canine")))));
        checkQuery(query, expected);
    }

    /**
     * Test query across multiple tables, joined by link constraints.
     */
    public void testQueryAcrossMultipleTables() {
        final String expected = "select distinct act "
                + "from " + Act.class.getName() + " as act "
                + "inner join act.participations as participation, "
                + EntityRelationship.class.getName() + " as owner, "
                + Party.class.getName() + " as patient, "
                + Party.class.getName() + " as customer "
                + "where ((act.archetypeId.entityName = :entityName0 and "
                + "act.archetypeId.concept = :concept0) and "
                + "act.active = :active0 and "
                + "act.status = :status0 and "
                + "(participation.archetypeId.entityName = :entityName1 and "
                + "participation.archetypeId.concept = :concept1) and "
                + "participation.active = :active1 and "
                + "act.linkId = participation.act.linkId and "
                + "((owner.archetypeId.entityName = :entityName2 and "
                + "owner.archetypeId.concept = :concept2) and "
                + "owner.active = :active2) and "
                + "((patient.archetypeId.entityName = :entityName3 and "
                + "patient.archetypeId.concept = :concept3) and "
                + "patient.active = :active3) and "
                + "((customer.archetypeId.entityName = :entityName4 and "
                + "customer.archetypeId.concept like :concept4) and "
                + "customer.active = :active4) and "
                + "participation.entity.linkId = patient.linkId and "
                + "patient.linkId = owner.target.linkId and "
                + "customer.linkId = owner.source.linkId) " +
                "order by customer.name asc, patient.name asc";

        ShortNameConstraint act = new ShortNameConstraint("act",
                                                          "act.patientReminder",
                                                          true);
        ShortNameConstraint participation = new ShortNameConstraint(
                "participation", "participation.patient", true);
        ShortNameConstraint owner = new ShortNameConstraint(
                "owner", "entityRelationship.patientOwner", true);
        ShortNameConstraint patient = new ShortNameConstraint(
                "patient", "party.patientpet", true);
        ShortNameConstraint customer = new ShortNameConstraint(
                "customer", "party.customer*", true);

        ArchetypeQuery query = new ArchetypeQuery(act);
        query.setDistinct(true);

        query.add(new NodeConstraint("status", "IN_PROGRESS"));
        query.add(new CollectionNodeConstraint("patient", participation));
        query.add(new IdConstraint("act", "participation.act"));
        query.add(owner);
        query.add(patient);
        query.add(customer);
        query.add(new IdConstraint("participation.entity", "patient"));
        query.add(new IdConstraint("patient", "owner.target"));
        query.add(new IdConstraint("customer", "owner.source"));
        query.add(new NodeSortConstraint("customer", "name"));
        query.add(new NodeSortConstraint("patient", "name"));

        checkQuery(query, expected);
    }

    public void testMultipleSelect() {
        final String expected = "select estimation.name, "
                + "estimation.description, estimation.status, estimationItem "
                + "from " + Act.class.getName() + " as estimation "
                + "inner join estimation.sourceActRelationships as items, "
                + Act.class.getName() + " as estimationItem "
                + "where ((estimation.archetypeId.entityName = :entityName0 and"
                + " estimation.archetypeId.concept = :concept0) and "
                + "estimation.active = :active0 and "
                + "(items.archetypeId.entityName = :entityName1 and "
                + "items.archetypeId.concept = :concept1) and "
                + "items.active = :active1 and "
                + "((estimationItem.archetypeId.entityName = :entityName2 and "
                + "estimationItem.archetypeId.concept = :concept2) and "
                + "estimationItem.active = :active2) and "
                + "items.source.linkId = estimation.linkId and "
                + "items.target.linkId = estimationItem.linkId)";
        ShortNameConstraint estimation = new ShortNameConstraint(
                "estimation", "act.customerEstimation", false, true);
        ShortNameConstraint estimationItem = new ShortNameConstraint(
                "estimationItem", "act.customerEstimationItem", false, true);
        ShortNameConstraint items = new ShortNameConstraint(
                "items", "actRelationship.customerEstimationItem", false, true);
        ArchetypeQuery query = new ArchetypeQuery(estimation);
        query.add(new NodeSelectConstraint("estimation.name"));
        query.add(new NodeSelectConstraint("estimation.description"));
        query.add(new NodeSelectConstraint("estimation.status"));
        query.add(new ObjectSelectConstraint("estimationItem"));
        query.add(new CollectionNodeConstraint("items", items));
        query.add(estimationItem);
        query.add(new IdConstraint("items.source", "estimation"));
        query.add(new IdConstraint("items.target", "estimationItem"));

        checkQuery(query, expected);
    }

    /**
     * Verifies that a query matches that expected, and can be parsed by
     * hibernate.
     *
     * @throws QueryBuilderException if the query is invalid
     * @throws HibernateException    if the query is invalid
     */
    private void checkQuery(ArchetypeQuery query, String expected) {
        String hql = builder.build(query).getQueryString();
        assertEquals(expected, hql);
    }


}
