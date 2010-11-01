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
package org.openvpms.esci.adapter.map.invoice;

import org.apache.commons.lang.StringUtils;
import org.oasis.ubl.InvoiceType;
import org.oasis.ubl.common.aggregate.AllowanceChargeType;
import org.oasis.ubl.common.aggregate.CustomerPartyType;
import org.oasis.ubl.common.aggregate.InvoiceLineType;
import org.oasis.ubl.common.aggregate.MonetaryTotalType;
import org.oasis.ubl.common.aggregate.OrderReferenceType;
import org.oasis.ubl.common.basic.ChargeTotalAmountType;
import org.oasis.ubl.common.basic.CustomerAssignedAccountIDType;
import org.oasis.ubl.common.basic.IssueDateType;
import org.oasis.ubl.common.basic.IssueTimeType;
import org.oasis.ubl.common.basic.NoteType;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.UBLDocument;
import org.openvpms.esci.adapter.map.UBLFinancialType;
import org.openvpms.esci.exception.ESCIException;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Wrapper for the UBL <tt>InvoiceType</tt> class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UBLInvoice extends UBLFinancialType implements UBLDocument {

    /**
     * The invoice.
     */
    private final InvoiceType invoice;

    /**
     * Supplier rules.
     */
    private final SupplierRules supplierRules;

    /**
     * Stock location archetype id.
     */
    private static final ArchetypeId STOCK_LOCATION = new ArchetypeId(StockArchetypes.STOCK_LOCATION);

    /**
     * Order archetype id.
     */
    private static final ArchetypeId ORDER = new ArchetypeId(SupplierArchetypes.ORDER);


    /**
     * Constructs an <tt>UBLInvoice</tt>.
     *
     * @param invoice       the invoice
     * @param currency      the expected currency for all amounts
     * @param service       the archetype service
     * @param supplierRules the supplier rules
     */
    public UBLInvoice(InvoiceType invoice, String currency, IArchetypeService service, SupplierRules supplierRules) {
        super(null, currency, service);
        this.invoice = invoice;
        this.supplierRules = supplierRules;
    }

    /**
     * Returns the type name.
     *
     * @return the type name
     */
    public String getType() {
        return "Invoice";
    }

    /**
     * Returns the invoice identifier.
     *
     * @return the invoice identifier
     * @throws ESCIException if the identifier isn't set
     */
    public String getID() {
        return getId(invoice.getID(), "ID");
    }

    /**
     * Determines if the {@link #getType type} and {@link #getID identifier} of this should be used for
     * error reporting. If not, then the parent should be used.
     *
     * @return <tt>true</tt>
     */
    @Override
    public boolean useForErrorReporting() {
        return true;
    }

    /**
     * Returns the UBL version.
     *
     * @return the UBL version
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the identifier isn't set
     */
    public String getUBLVersionID() {
        return getId(invoice.getUBLVersionID(), "UBLVersionID");
    }

    /**
     * Returns the invoice issue date/time.
     *
     * @return the issue date/time
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the issue date isn't set
     */
    public Date getIssueDatetime() {
        IssueDateType issueDate = getRequired(invoice.getIssueDate(), "IssueDate");
        XMLGregorianCalendar calendar = issueDate.getValue();
        checkRequired(calendar, "IssueDate");

        IssueTimeType issueTime = invoice.getIssueTime();
        if (issueTime != null) {
            XMLGregorianCalendar time = issueTime.getValue();
            if (time != null) {
                calendar.setHour(time.getHour());
                calendar.setMinute(time.getMinute());
                calendar.setSecond(time.getSecond());
                calendar.setMillisecond(time.getMillisecond());
            }
        }
        return calendar.toGregorianCalendar().getTime();
    }

    /**
     * Returns the invoice notes.
     * <p/>
     * If there are multiple notes, these will be concatenated, separated by newlines.
     *
     * @return the invoice note. May be <tt>null</tt>
     */
    public String getNotes() {
        String result = null;
        List<NoteType> notes = invoice.getNote();
        if (notes != null && !notes.isEmpty()) {
            StringBuilder buffer = new StringBuilder();
            for (NoteType note : notes) {
                if (buffer.length() != 0) {
                    buffer.append('\n');
                }
                if (!StringUtils.isEmpty(note.getValue())) {
                    buffer.append(note.getValue());
                }
            }
            result = buffer.toString();
        }
        return result;
    }

    /**
     * Returns the invoice supplier.
     *
     * @return the supplier
     * @throws org.openvpms.esci.exception.ESCIException
     *          if the supplier was not found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          for any archetype service error
     */
    public Party getSupplier() {
        return getSupplier(invoice.getAccountingSupplierParty(), "AccountingSupplierParty");
    }

    /**
     * Returns the invoice stock location.
     *
     * @return the stock location
     * @throws ESCIException if the stock location was not found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *                       for any archetype service error
     */
    public Party getStockLocation() {
        CustomerPartyType customerType = getRequired(invoice.getAccountingCustomerParty(), "AccountingCustomerParty");
        CustomerAssignedAccountIDType accountId = customerType.getCustomerAssignedAccountID();
        Party location = (Party) getObject(STOCK_LOCATION, accountId,
                                           "AccountingCustomerParty/CustomerAssignedAccountID");
        if (location == null) {
            Message message = ESCIAdapterMessages.invoiceInvalidStockLocation(
                    "AccountingCustomerParty/CustomerAssignedAccountID", getID(), accountId.getValue());
            throw new ESCIException(message.toString());
        }
        return location;
    }

    /**
     * Returns the order associated with the invoice.
     *
     * @return the order, or <tt>null</tt> if the invoice isn't associated with an order
     * @throws ESCIException if the order was specified, but could not be found
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *                       for any archetype service error
     */
    public FinancialAct getOrder() {
        FinancialAct result = null;
        OrderReferenceType orderRef = invoice.getOrderReference();
        if (orderRef != null) {
            result = (FinancialAct) getObject(ORDER, orderRef.getID(), "OrderReference");
            if (result == null) {
                Message message = ESCIAdapterMessages.invalidOrder("Invoice", getID(), orderRef.getID().getValue());
                throw new ESCIException(message.toString());
            }
        }
        return result;
    }

    /**
     * Returns the payable amount.
     *
     * @return the payable amount
     * @throws ESCIException if the payable amount is incorrectly specified
     */
    public BigDecimal getPayableAmount() {
        MonetaryTotalType monetaryTotal = getLegalMonetaryTotal();
        return getAmount(monetaryTotal.getPayableAmount(), "LegalMonetaryTotal/PayableAmount");
    }

    /**
     * Returns the line extension amount.
     *
     * @return the line extension amount
     * @throws ESCIException if the payable amount is incorrectly specified
     */
    public BigDecimal getLineExtensionAmount() {
        MonetaryTotalType monetaryTotal = getLegalMonetaryTotal();
        return getAmount(monetaryTotal.getLineExtensionAmount(), "LegalMonetaryTotal/LineExtensionAmount");
    }

    /**
     * Returns the total charges for the invoice.
     * <p/>
     * This corresponds to <em>Invoice/LegalMonetaryTotal/ChargeTotalAmount</em>.
     *
     * @return the total charges, or <tt>0.0</tt> if they aren't specified
     * @throws ESCIException if the total is incorrectly specified
     */
    public BigDecimal getChargeTotal() {
        BigDecimal result = BigDecimal.ZERO;
        MonetaryTotalType monetaryTotal = getLegalMonetaryTotal();
        ChargeTotalAmountType amount = monetaryTotal.getChargeTotalAmount();
        if (amount != null) {
            result = getAmount(amount, "LegalMonetaryTotal/ChargeTotalAmount");
        }
        return result;
    }

    /**
     * Returns the tax exclusive amount.
     * <p/>
     * This corresponds to <em>Invoice/LegalMonetaryTotal/TaxExclusiveAmount</em>.
     *
     * @return the tax exclusive amount
     * @throws ESCIException if the amount is incorrectly specified
     */
    public BigDecimal getTaxExclusiveAmount() {
        MonetaryTotalType monetaryTotal = getLegalMonetaryTotal();
        return getAmount(monetaryTotal.getTaxExclusiveAmount(), "LegalMonetaryTotal/TaxExclusiveAmount");
    }

    /**
     * Returns the total tax for the invoice.
     * <p/>
     * This corresponds to <em>Invoice/TaxTotal/TaxAmount</em> (i.e only one TaxTotal is supported).
     *
     * @return the total tax
     * @throws ESCIException if the tax is incorrectly specified
     */
    public BigDecimal getTaxAmount() {
        return getTaxAmount(invoice.getTaxTotal());
    }

    /**
     * Returns the invoice lines.
     *
     * @return the invoice lines
     */
    public List<UBLInvoiceLine> getInvoiceLines() {
        List<UBLInvoiceLine> result = new ArrayList<UBLInvoiceLine>(invoice.getInvoiceLine().size());
        for (InvoiceLineType line : invoice.getInvoiceLine()) {
            result.add(new UBLInvoiceLine(line, this, getCurrency(), getArchetypeService(), supplierRules));
        }
        return result;
    }

    /**
     * Returns the allowance/charges.
     *
     * @return the allowance/charges
     */
    public List<UBLAllowanceCharge> getAllowanceCharges() {
        List<UBLAllowanceCharge> result = new ArrayList<UBLAllowanceCharge>(invoice.getAllowanceCharge().size());
        for (AllowanceChargeType ac : invoice.getAllowanceCharge()) {
            result.add(new UBLAllowanceCharge(ac, this, getCurrency(), getArchetypeService()));
        }
        return result;
    }

    /**
     * Returns the legal monetary total.
     * <p/>
     * This corresponds to <em>Invoice/LegalMonetaryTotal</em>
     *
     * @return the legal monetary total
     * @throws ESCIException if it is not present
     */
    protected MonetaryTotalType getLegalMonetaryTotal() {
        return getRequired(invoice.getLegalMonetaryTotal(), "LegalMonetaryTotal");
    }

}

