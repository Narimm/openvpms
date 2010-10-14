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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Contains the results of mapping an UBL Invoice to an <em>act.supplierDelivery</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Delivery {

    /**
     * The order that the delivery applies to.
     */
    private FinancialAct order;

    /**
     * The delivery.
     */
    private FinancialAct delivery;

    /**
     * The delivery items.
     */
    private List<FinancialAct> deliveryItems = new ArrayList<FinancialAct>();

    /**
     * The mapping of delivery item reference to order reference.
     */
    private Map<IMObjectReference, IMObjectReference> deliveryToOrder
            = new HashMap<IMObjectReference, IMObjectReference>();

    /**
     * Sets the order.
     *
     * @param order the order. May be <tt>null</tt>
     */
    public void setOrder(FinancialAct order) {
        this.order = order;
    }

    /**
     * Returns the order.
     *
     * @return the order. May be <tt>null</tt>
     */
    public FinancialAct getOrder() {
        return order;
    }

    /**
     * Sets the delivery.
     *
     * @param delivery the delivery
     */
    public void setDelivery(FinancialAct delivery) {
        this.delivery = delivery;
    }

    /**
     * Returns the delivery.
     *
     * @return the delivery
     */
    public FinancialAct getDelivery() {
        return delivery;
    }

    /**
     * Adds a delivery item.
     *
     * @param item a delivery item
     */
    public void addDeliveryItem(FinancialAct item) {
        deliveryItems.add(item);
    }

    /**
     * Returns the delivery items.
     *
     * @return the delivery items
     */
    public List<FinancialAct> getDeliveryItems() {
        return deliveryItems;
    }

    /**
     * Registers a mapping between a delivery item and an order item.
     *
     * @param deliveryItem the delivery item reference
     * @param orderItem    the order item reference
     */
    public void addDeliveryOrderMapping(FinancialAct deliveryItem, IMObjectReference orderItem) {
        deliveryToOrder.put(deliveryItem.getObjectReference(), orderItem);
    }

    /**
     * Returns the order reference corresponding to a delivery item reference.
     *
     * @param deliveryItem the delivery item reference
     * @return the corresponding order item reference, or <tt>null</tt> if none is found
     */
    public IMObjectReference getOrderItem(FinancialAct deliveryItem) {
        return deliveryToOrder.get(deliveryItem.getObjectReference());
    }

    /**
     * Returns the delivery acts.
     *
     * @return the delivery acts
     */
    public List<FinancialAct> getActs() {
        List<FinancialAct> acts = new ArrayList<FinancialAct>();
        acts.add(delivery);
        acts.addAll(deliveryItems);
        return acts;
    }
}
