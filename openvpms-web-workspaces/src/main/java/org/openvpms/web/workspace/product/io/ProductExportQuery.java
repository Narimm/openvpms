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

package org.openvpms.web.workspace.product.io;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.ListCellRenderer;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.product.ProductQuery;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class ProductExportQuery extends ProductQuery {

    private DateRange range;

    public enum Prices {
        LATEST, ALL, RANGE
    }

    private Entity productType;
    private Prices prices = Prices.LATEST;


    private static final String[] SHORT_NAMES = new String[]{
            ProductArchetypes.MEDICATION, ProductArchetypes.SERVICE, ProductArchetypes.MERCHANDISE,
            ProductArchetypes.TEMPLATE, ProductArchetypes.PRICE_TEMPLATE};


    private static final Prices[] PRICES = {Prices.LATEST, Prices.ALL, Prices.RANGE};

    private final String[] PRICE_LABELS = {Messages.get("product.io.prices.latest"),
                                           Messages.get("product.io.prices.all"), Messages.get("product.io.prices.range")};

    /**
     * Constructs a {@link ProductExportQuery}.
     *
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public ProductExportQuery() {
        super(SHORT_NAMES);
    }

    public Prices getPrices() {
        return prices;
    }

    public void setPrices(Prices prices) {
        this.prices = prices;
        if (range != null) {
            range.setEnabled(prices == Prices.RANGE);
        }
    }

    public Date getFrom() {
        return (range != null) ? range.getFrom() : null;
    }

    public Date getTo() {
        return (range != null) ? range.getTo() : null;
    }

    /**
     * Creates a container component to lay out the query component in.
     * This implementation returns a new grid.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(8);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        addProductTypeSelector(container);
        addSpeciesSelector(container);
        addPriceSelector(container);
        addDateRange(container);
    }


    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Product> createResultSet(SortConstraint[] sort) {
        return new ProductExportResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), getSpecies(),
                                          productType, getStockLocation(), sort, getMaxResults());
    }


    private void addProductTypeSelector(Component container) {
        ArchetypeQuery query = new ArchetypeQuery(ProductArchetypes.PRODUCT_TYPE, true)
                .add(Constraints.sort("name"))
                .setMaxResults(ArchetypeQuery.ALL_RESULTS);
        final IMObjectListModel model = new IMObjectListModel(QueryHelper.query(query), true, false);
        final SelectField field = SelectFieldFactory.create(model);
        field.setCellRenderer(IMObjectListCellRenderer.NAME);
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setProductType((Entity) field.getSelectedItem());
            }
        });

        Label label = LabelFactory.create("product.io.productType");
        container.add(label);
        container.add(field);
        getFocusGroup().add(field);
    }

    private void setProductType(Entity type) {
        this.productType = type;
    }

    private void addSpeciesSelector(Component container) {
        LookupQuery query = new ArchetypeLookupQuery("lookup.species");
        final SelectField field = SelectFieldFactory.create(new LookupListModel(query, true));
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setSpecies((String) field.getSelectedItem());
            }
        });
        field.setCellRenderer(LookupListCellRenderer.INSTANCE);

        Label species = LabelFactory.create("product.io.species");
        container.add(species);
        container.add(field);
        getFocusGroup().add(field);
    }

    private void addPriceSelector(Component container) {
        final SelectField field = SelectFieldFactory.create(PRICES);
        field.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                setPrices((Prices) field.getSelectedItem());
            }
        });
        field.setCellRenderer(new ListCellRenderer() {
            @Override
            public Object getListCellRendererComponent(Component list, Object value, int index) {
                return PRICE_LABELS[index];
            }
        });
        container.add(LabelFactory.create("product.io.prices"));
        container.add(field);
        getFocusGroup().add(field);
    }

    private void addDateRange(Component container) {
        range = new DateRange(getFocusGroup(), false);
        range.getComponent();
        range.setEnabled(prices == Prices.RANGE);
        container.add(range.getComponent());
    }
}
