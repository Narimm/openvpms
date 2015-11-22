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

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.invoice.ChargeItemDocumentLinker;
import org.openvpms.archetype.rules.finance.tax.TaxRuleException;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.bound.BoundProperty;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditorFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.ClinicianParticipationEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.edit.act.TemplateProduct;
import org.openvpms.web.component.im.edit.reminder.ReminderEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientActEditor;
import org.openvpms.web.component.im.patient.PatientParticipationEditor;
import org.openvpms.web.component.im.product.BatchParticipationEditor;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.PriceActItemEditor;
import org.openvpms.web.workspace.patient.history.PatientInvestigationActEditor;
import org.openvpms.web.workspace.patient.mr.PatientMedicationActEditor;
import org.openvpms.web.workspace.patient.mr.PrescriptionMedicationActEditor;
import org.openvpms.web.workspace.patient.mr.Prescriptions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.math.BigDecimal.ZERO;
import static org.openvpms.archetype.rules.math.MathRules.ONE_HUNDRED;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MEDICATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MERCHANDISE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.SERVICE;
import static org.openvpms.archetype.rules.stock.StockArchetypes.STOCK_LOCATION_PARTICIPATION;


/**
 * An editor for {@link org.openvpms.component.business.domain.im.act.Act}s which have an archetype of
 * <em>act.customerAccountInvoiceItem</em>,
 * <em>act.customerAccountCreditItem</em>
 * or <em>act.customerAccountCounterItem</em>.
 *
 * @author Tim Anderson
 */
public abstract class CustomerChargeActItemEditor extends PriceActItemEditor {

    /**
     * Dispensing act editor. May be {@code null}
     */
    private DispensingActRelationshipCollectionEditor dispensing;

    /**
     * Investigation act editor. May be {@code null}
     */
    private ActRelationshipCollectionEditor investigations;

    /**
     * Reminders act editor. May be {@code null}
     */
    private ActRelationshipCollectionEditor reminders;

    /**
     * The medication, investigation and reminder act editor manager.
     */
    private EditorQueue editorQueue;

    /**
     * Listener for changes to the quantity.
     */
    private final ModifiableListener quantityListener;

    /**
     * Listener for changes to the medication act.
     */
    private final ModifiableListener dispensingListener;

    /**
     * Listener for changes to the start time.
     */
    private final ModifiableListener startTimeListener;

    /**
     * Listener for changes to the fixed price, quantity and unit price, to update the discount.
     */
    private final ModifiableListener discountListener;

    /**
     * Listener for changes to the total, so the tax amount can be recalculated, and print flag enabled/disabled.
     */
    private final ModifiableListener totalListener;

    /**
     * Listener for changes to the batch.
     */
    private final ModifiableListener batchListener;

    /**
     * Stock rules.
     */
    private StockRules rules;

    /**
     * Reminder rules.
     */
    private ReminderRules reminderRules;

    /**
     * The quantity.
     */
    private final Quantity quantity;

    /**
     * Selling units label.
     */
    private Label sellingUnits;

    /**
     * The prescriptions.
     */
    private Prescriptions prescriptions;

    /**
     * If {@code true}, prompt to use prescriptions.
     */
    private boolean promptForPrescription = true;

    /**
     * If {@code true}, enable medication editing to be cancelled when it is being dispensed from a prescription.
     */
    private boolean cancelPrescription;

    /**
     * The charge context.
     */
    private ChargeContext chargeContext;

    /**
     * Dispensing node name.
     */
    private static final String DISPENSING = "dispensing";

    /**
     * Reminders node name.
     */
    private static final String REMINDERS = "reminders";

    /**
     * Returned quantity node name.
     */
    private static final String RETURNED_QUANTITY = "returnedQuantity";

    /**
     * Received quantity node name.
     */
    private static final String RECEIVED_QUANTITY = "receivedQuantity";

    /**
     * Pharmacy order nodes.
     */
    private static final String[] ORDER_NODES = {RECEIVED_QUANTITY, RETURNED_QUANTITY};

    /**
     * Investigations node name.
     */
    private static final String INVESTIGATIONS = "investigations";

    /**
     * Fixed cost node name.
     */
    private static final String FIXED_COST = "fixedCost";

    /**
     * Unit cost node name.
     */
    private static final String UNIT_COST = "unitCost";

    /**
     * Tax node name.
     */
    private static final String TAX = "tax";

    /**
     * Total node name.
     */
    private static final String TOTAL = "total";

    /**
     * The print aggregate node name.
     */
    private static final String PRINT_AGGREGATE = "printAggregate";

    /**
     * Nodes to use when a product template is selected.
     */
    private static final ArchetypeNodes TEMPLATE_NODES = new ArchetypeNodes().exclude(
            QUANTITY, FIXED_PRICE, UNIT_PRICE, DISCOUNT, CLINICIAN, TOTAL, DISPENSING, INVESTIGATIONS,
            REMINDERS, "batch");


    /**
     * Constructs a {@link CustomerChargeActItemEditor}.
     * <p>
     * This recalculates the tax amount.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public CustomerChargeActItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM,
                            CustomerAccountArchetypes.CREDIT_ITEM,
                            CustomerAccountArchetypes.COUNTER_ITEM)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }

        Property quantityProperty = getProperty(QUANTITY);
        quantity = new Quantity(quantityProperty, act, getLayoutContext());

        setDisableDiscounts(getDisableDiscounts(getLocation()));
        dispensing = createDispensingCollectionEditor();
        investigations = createCollectionEditor(INVESTIGATIONS, act);
        reminders = createCollectionEditor(REMINDERS, act);

        rules = ServiceHelper.getBean(StockRules.class);
        reminderRules = ServiceHelper.getBean(ReminderRules.class);
        quantityListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onQuantityChanged();
            }
        };
        dispensingListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateQuantity();
                updateBatch();
            }
        };
        if (dispensing != null) {
            dispensing.addModifiableListener(dispensingListener);
        }

        sellingUnits = LabelFactory.create();

        if (act.isNew()) {
            // default the act start time to today
            act.setActivityStartTime(new Date());
        }

        calculateTax();

        Product product = getProduct();
        ArchetypeNodes nodes = getFilterForProduct(product, updatePrint(product));
        setArchetypeNodes(nodes);

        // add a listener to update the tax amount when the total changes
        totalListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onTotalChanged();
            }
        };
        getProperty(TOTAL).addModifiableListener(totalListener);

        // add a listener to update the discount amount when the quantity, fixed or unit price changes
        discountListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateDiscount();
            }
        };
        getProperty(FIXED_PRICE).addModifiableListener(discountListener);
        quantityProperty.addModifiableListener(discountListener);
        getProperty(UNIT_PRICE).addModifiableListener(discountListener);
        quantityProperty.addModifiableListener(quantityListener);

        startTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePatientActsStartTime();
            }
        };
        getProperty(START_TIME).addModifiableListener(startTimeListener);

        batchListener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                updateMedicationBatch(getStockLocationRef());
            }
        };
    }

    /**
     * Sets a product included from a template.
     *
     * @param product  the product. May be {@code null}
     * @param template the template that included the product. May be {@code null}
     */
    @Override
    public void setProduct(TemplateProduct product, Product template) {
        super.setProduct(product, template);
        if (product != null && !product.getPrint() && MathRules.isZero(getTotal())) {
            setPrint(false);
        }
    }

    /**
     * Determines if the quantity is a default for a product based on the patient's weight.
     *
     * @return {@code true} if the quantity is a default
     */
    public boolean isDefaultQuantity() {
        return quantity.isDefault();
    }

    /**
     * Returns the total.
     *
     * @return the total
     */
    public BigDecimal getTotal() {
        return getProperty(TOTAL).getBigDecimal(ZERO);
    }

    /**
     * Determines if an order has been placed for the item.
     *
     * @return {@code true} if an order has been placed
     */
    public boolean isOrdered() {
        Property ordered = getProperty("ordered");
        return ordered != null && ordered.getBoolean();
    }

    /**
     * Returns the received quantity.
     *
     * @return the received quantity
     */
    public BigDecimal getReceivedQuantity() {
        Property property = getProperty(RECEIVED_QUANTITY);
        return property != null ? property.getBigDecimal(ZERO) : ZERO;
    }

    /**
     * Sets the received quantity.
     *
     * @param quantity the received quantity
     */
    public void setReceivedQuantity(BigDecimal quantity) {
        Property property = getProperty(RECEIVED_QUANTITY);
        if (property != null) {
            property.setValue(quantity);
        }
    }

    /**
     * Returns the returned quantity.
     *
     * @return the returned quantity
     */
    public BigDecimal getReturnedQuantity() {
        Property property = getProperty(RETURNED_QUANTITY);
        return property != null ? property.getBigDecimal(ZERO) : ZERO;
    }

    /**
     * Sets the returned quantity.
     *
     * @param quantity the returned quantity
     */
    public void setReturnedQuantity(BigDecimal quantity) {
        Property property = getProperty(RETURNED_QUANTITY);
        if (property != null) {
            property.setValue(quantity);
        }
    }

    /**
     * Return any investigations associated with the charge item.
     *
     * @return the investigations
     */
    public List<Act> getInvestigations() {
        return (investigations != null) ? investigations.getActs() : Collections.<Act>emptyList();
    }

    public PatientInvestigationActEditor getInvestigation(IMObjectReference investigationRef) {
        if (investigations != null) {
            for (PatientInvestigationActEditor editor : getInvestigationActEditors()) {
                if (editor.getObject().getObjectReference().equals(investigationRef)) {
                    return editor;
                }
            }
        }
        return null;
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    @Override
    public void dispose() {
        super.dispose();
        if (dispensing != null) {
            dispensing.removeModifiableListener(dispensingListener);
        }

        getProperty(TOTAL).removeModifiableListener(totalListener);
        getProperty(FIXED_PRICE).removeModifiableListener(discountListener);
        getProperty(QUANTITY).removeModifiableListener(discountListener);
        getProperty(UNIT_PRICE).removeModifiableListener(discountListener);
        getProperty(QUANTITY).removeModifiableListener(quantityListener);
        getProperty(START_TIME).removeModifiableListener(startTimeListener);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    public boolean validate(Validator validator) {
        return (editorQueue == null || editorQueue.isComplete()) && super.validate(validator);
    }

    /**
     * Sets the popup editor manager.
     *
     * @param manager the popup editor manager
     */
    public void setEditorQueue(EditorQueue manager) {
        editorQueue = manager;
    }

    /**
     * Returns the popup editor manager.
     *
     * @return the popup editor manager
     */
    public EditorQueue getEditorQueue() {
        return editorQueue;
    }

    /**
     * Sets the prescriptions.
     *
     * @param prescriptions the prescriptions. May be {@code null}
     */
    public void setPrescriptions(Prescriptions prescriptions) {
        this.prescriptions = prescriptions;
        if (dispensing != null) {
            dispensing.setPrescriptions(prescriptions);
        }
    }

    /**
     * Returns the prescriptions.
     *
     * @return the prescriptions. May be {@code null}
     */
    public Prescriptions getPrescriptions() {
        return prescriptions;
    }

    /**
     * Determines if prescriptions should be prompted for.
     *
     * @param prompt if {@code true}, prompt for prescriptions, otherwise use them automatically
     */
    public void setPromptForPrescriptions(boolean prompt) {
        promptForPrescription = prompt;
    }

    /**
     * Determines if prescription editing may be cancelled.
     *
     * @param cancel if {@code true}, prescription editing may be cancelled
     */
    public void setCancelPrescription(boolean cancel) {
        cancelPrescription = cancel;
    }

    /**
     * Returns the reminders.
     *
     * @return the reminders
     */
    public List<Act> getReminders() {
        return (reminders != null) ? reminders.getCurrentActs() : Collections.<Act>emptyList();
    }

    /**
     * Returns a reference to the stock location.
     *
     * @return the stock location reference, or {@code null} if there is none
     */
    public IMObjectReference getStockLocationRef() {
        return getParticipantRef("stockLocation");
    }

    /**
     * Sets the charge context.
     *
     * @param context the charge context
     */
    public void setChargeContext(ChargeContext context) {
        this.chargeContext = context;
        // register the context to delete acts after everything else has been saved to avoid Hibernate errors.
        if (dispensing != null) {
            dispensing.getEditor().setRemoveHandler(context);
        }
        if (investigations != null) {
            investigations.getEditor().setRemoveHandler(context);
        }
        if (reminders != null) {
            reminders.getEditor().setRemoveHandler(context);
        }
    }

    /**
     * Notifies the editor that the product has been ordered via a pharmacy.
     * <p>
     * This refreshes the display to make the patient and product read-only, and display the received and returned
     * nodes if required.
     */
    public void ordered() {
        updateLayout(getProduct());
    }

    /**
     * Updates the discount and checks that it isn't less than the total cost.
     * <p>
     * If so, gives the user the opportunity to remove the discount.
     *
     * @return {@code true} if the discount was updated
     */
    @Override
    protected boolean updateDiscount() {
        boolean updated = super.updateDiscount();
        BigDecimal discount = getProperty(DISCOUNT).getBigDecimal(ZERO);
        if (updated && discount.compareTo(ZERO) != 0) {
            BigDecimal fixedPriceMaxDiscount = getFixedPriceMaxDiscount(null);
            BigDecimal unitPriceMaxDiscount = getUnitPriceMaxDiscount(null);
            if ((fixedPriceMaxDiscount != null && !MathRules.equals(fixedPriceMaxDiscount, ONE_HUNDRED))
                || (unitPriceMaxDiscount != null && !MathRules.equals(unitPriceMaxDiscount, ONE_HUNDRED))) {
                // if there is a fixed and/or unit price maximum discount present, and it is not 100%, check if the
                // sale price is less than the cost price

                BigDecimal quantity = getQuantity();
                BigDecimal fixedCost = getFixedCost();
                BigDecimal fixedPrice = getFixedPrice();
                BigDecimal unitCost = getUnitCost();
                BigDecimal unitPrice = getUnitPrice();
                BigDecimal costPrice = fixedCost.add(unitCost.multiply(quantity));
                BigDecimal salePrice = fixedPrice.add(unitPrice.multiply(quantity));
                if (costPrice.compareTo(salePrice.subtract(discount)) > 0) {
                    ConfirmationDialog dialog = new ConfirmationDialog(Messages.get("customer.charge.discount.title"),
                                                                       Messages.get("customer.charge.discount.message"),
                                                                       ConfirmationDialog.YES_NO);
                    dialog.addWindowPaneListener(new PopupDialogListener() {
                        @Override
                        public void onYes() {
                            getProperty(DISCOUNT).setValue(ZERO);
                            super.onYes();
                        }
                    });
                    editorQueue.queue(dialog);
                }
            }
        }
        return updated;
    }

    /**
     * Save any edits.
     * <p>
     * This implementation saves the current object before children, to ensure deletion of child acts
     * don't result in StaleObjectStateException exceptions.
     * <p>
     * This implementation will throw an exception if the product is an <em>product.template</em>.
     * Ideally, the act would be flagged invalid if this is the case, but template expansion only works for valid
     * acts. TODO
     *
     * @throws OpenVPMSException     if the save fails
     * @throws IllegalStateException if the product is a template
     */
    @Override
    protected void doSave() {
        if (TypeHelper.isA(getObject(), CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (chargeContext.getHistoryChanges() == null) {
                throw new IllegalStateException("PatientHistoryChanges haven't been registered");
            }
        }
        super.doSave();
    }

    /**
     * Saves the object.
     * <p>
     * For invoice items, this implementation also creates/deletes document acts related to the document templates
     * associated with the product, using {@link ChargeItemDocumentLinker}.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void saveObject() {
        IArchetypeService service = ServiceHelper.getArchetypeService();

        if (TypeHelper.isA(getObject(), CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (chargeContext == null) {
                throw new IllegalStateException("PatientHistoryChanges haven't been registered");
            }
            ChargeItemDocumentLinker linker = new ChargeItemDocumentLinker((FinancialAct) getObject(), service);
            linker.prepare(chargeContext.getHistoryChanges());
        }
        super.saveObject();
    }

    /**
     * Returns the dispensing node editor.
     *
     * @return the editor. May be {@code null}
     */
    protected ActRelationshipCollectionEditor getDispensingEditor() {
        return dispensing;
    }

    /**
     * Creates the layout strategy.
     *
     * @param fixedPrice the fixed price editor
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(FixedPriceEditor fixedPrice) {
        return new CustomerChargeItemLayoutStrategy(fixedPrice);
    }

    /**
     * Determines if an editor should be disposed on layout change.
     *
     * @param editor the editor
     * @return {@code true} if the editor isn't for dispensing, investigations, or reminders
     */
    @Override
    protected boolean disposeOnChangeLayout(Editor editor) {
        return editor != dispensing && editor != investigations && editor != reminders;
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();

        ProductParticipationEditor product = getProductEditor();
        if (product != null) {
            // register the location in order to determine service ratios
            product.setLocation(getLocation());
            product.setExcludeTemplateOnlyProducts(true);
        }

        PatientParticipationEditor patient = getPatientEditor();
        if (patient != null) {
            // add a listener to update the dispensing, investigation and reminder acts when the patient changes
            patient.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    updatePatientActsPatient();
                }
            });
        }

        ClinicianParticipationEditor clinician = getClinicianEditor();
        if (clinician != null) {
            // add a listener to update the dispensing, investigation and reminder acts when the clinician changes
            clinician.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    updatePatientActsClinician();
                }
            });
        }
        updateBatch(getProduct(), getStockLocationRef());
    }

    /**
     * Invoked when the product is changed, to update prices, dispensing and reminder acts.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        getProperty(FIXED_PRICE).removeModifiableListener(discountListener);
        getProperty(QUANTITY).removeModifiableListener(discountListener);
        getProperty(UNIT_PRICE).removeModifiableListener(discountListener);
        getProperty(TOTAL).removeModifiableListener(totalListener);
        super.productModified(product);

        boolean clearDefault = true;
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            Party patient = getPatient();
            if (patient != null) {
                BigDecimal dose = getDose(product, patient);
                if (!MathRules.isZero(dose)) {
                    quantity.setValue(dose, true);
                    clearDefault = false;
                }
            }
        }
        if (clearDefault) {
            // the quantity is not a default for the product, so turn off any higlighting
            quantity.clearDefault();
        }
        Property discount = getProperty(DISCOUNT);
        discount.setValue(ZERO);

        Property fixedPrice = getProperty(FIXED_PRICE);
        Property unitPrice = getProperty(UNIT_PRICE);
        Property fixedCost = getProperty(FIXED_COST);
        Property unitCost = getProperty(UNIT_COST);
        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            fixedPrice.setValue(ZERO);
            unitPrice.setValue(ZERO);
            fixedCost.setValue(ZERO);
            unitCost.setValue(ZERO);
            updateSellingUnits(null);
        } else {
            ProductPrice fixedProductPrice = null;
            ProductPrice unitProductPrice = null;
            if (product != null) {
                fixedProductPrice = getDefaultFixedProductPrice(product);
                unitProductPrice = getDefaultUnitProductPrice(product);
            }

            if (fixedProductPrice != null) {
                fixedPrice.setValue(getPrice(product, fixedProductPrice));
                fixedCost.setValue(getCost(fixedProductPrice));
            } else {
                fixedPrice.setValue(ZERO);
                fixedCost.setValue(ZERO);
            }
            if (unitProductPrice != null) {
                unitPrice.setValue(getPrice(product, unitProductPrice));
                unitCost.setValue(getCost(unitProductPrice));
            } else {
                unitPrice.setValue(ZERO);
                unitCost.setValue(ZERO);
            }
            IMObjectReference stockLocation = updateStockLocation(product);
            updateSellingUnits(product);
            updateBatch(product, stockLocation);
            updateDiscount();
        }
        updateTaxAmount();

        updatePatientMedication(product);
        updateInvestigations(product);
        updateReminders(product);

        boolean showPrint = updatePrint(product);

        // update the layout if nodes require filtering
        updateLayout(product, showPrint);

        notifyProductListener(product);
        getProperty(FIXED_PRICE).addModifiableListener(discountListener);
        getProperty(QUANTITY).addModifiableListener(discountListener);
        getProperty(UNIT_PRICE).addModifiableListener(discountListener);
        getProperty(TOTAL).addModifiableListener(totalListener);
    }

    /**
     * Calculates the tax amount.
     *
     * @throws ArchetypeServiceException for any archetype service error
     * @throws TaxRuleException          for any tax error
     */
    protected void calculateTax() {
        Party customer = getCustomer();
        if (customer != null && getProductRef() != null) {
            FinancialAct act = (FinancialAct) getObject();
            BigDecimal previousTax = act.getTaxAmount();
            BigDecimal tax = calculateTax(customer);
            if (tax.compareTo(previousTax) != 0) {
                Property property = getProperty("tax");
                property.refresh();
            }
        }
    }

    /**
     * Invoked when the total changes.
     */
    private void onTotalChanged() {
        updateTaxAmount();
        Product product = getProduct();
        boolean showPrint = updatePrint(product);
        updateLayout(product, showPrint);
    }

    /**
     * Calculates the tax amount.
     */
    private void updateTaxAmount() {
        try {
            calculateTax();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Updates any patient acts with the start time.
     */
    private void updatePatientActsStartTime() {
        Act parent = getObject();
        for (PatientActEditor editor : getMedicationActEditors()) {
            editor.setStartTime(parent.getActivityStartTime());
        }
        for (PatientInvestigationActEditor editor : getInvestigationActEditors()) {
            editor.setStartTime(parent.getActivityStartTime());
        }
        for (ReminderEditor editor : getReminderEditors()) {
            editor.setStartTime(parent.getActivityStartTime());
        }
        if (dispensing != null) {
            dispensing.refresh();
        }
        if (investigations != null) {
            investigations.refresh();
        }
        if (reminders != null) {
            reminders.refresh();
        }
    }

    /**
     * Invoked when the product changes to update patient medications.
     * <p>
     * If the new product is a medication and there is:
     * <ul>
     * <li>an existing act, the existing act will be updated.
     * <li>no existing act, a new medication will be created
     * </ul>
     * <p>
     * If the product is null, any existing act will be removed
     *
     * @param product the product. May be {@code null}
     */
    private void updatePatientMedication(Product product) {
        if (dispensing != null) {
            if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                Set<PrescriptionMedicationActEditor> medicationEditors = getMedicationActEditors();
                if (!medicationEditors.isEmpty()) {
                    // set the product on the existing acts
                    for (PrescriptionMedicationActEditor editor : medicationEditors) {
                        editor.setProduct(product);
                        changePrescription(editor);
                    }
                    dispensing.refresh();
                } else {
                    // add a new medication act
                    Act act = (Act) dispensing.create();
                    if (act != null) {
                        Act prescription = getPrescription();
                        if (prescription != null) {
                            checkUsePrescription(prescription, product, act);
                        } else {
                            createMedicationEditor(product, act);
                        }
                    }
                }
            } else {
                // product is not a medication or is null. Remove any existing act
                for (Act act : dispensing.getCurrentActs()) {
                    dispensing.remove(act);
                }
            }
        }
    }

    /**
     * Returns the prescription for the current patient and product, if one exists.
     *
     * @return the prescription, or {@code null} if none exists
     */
    private Act getPrescription() {
        Party patient = getPatient();
        Product product = getProduct();
        return prescriptions != null && patient != null && product != null ?
               prescriptions.getPrescription(patient, product) : null;
    }

    /**
     * Determines if a prescription should be dispensed.
     *
     * @param prescription the prescription
     * @param product      the product being dispensed
     * @param medication   the medication act
     */
    private void checkUsePrescription(final Act prescription, final Product product, final Act medication) {
        if (promptForPrescription) {
            ConfirmationDialog dialog = new ConfirmationDialog(Messages.get("customer.charge.prescription.title"),
                                                               Messages.format("customer.charge.prescription.message",
                                                                               product.getName()));
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    createPrescriptionMedicationEditor(medication, prescription);
                }

                @Override
                public void onCancel() {
                    createMedicationEditor(product, medication);
                }
            });
            editorQueue.queue(dialog);
        } else {
            createPrescriptionMedicationEditor(medication, prescription);
        }
    }

    /**
     * Creates an editor for an <em>act.patientMedication</em> that dispenses a prescription.
     *
     * @param medication   the medication
     * @param prescription the prescription
     */
    private void createPrescriptionMedicationEditor(Act medication, Act prescription) {
        PrescriptionMedicationActEditor editor = dispensing.createEditor(medication, createLayoutContext(medication));
        editor.setPrescription(prescription);
        dispensing.addEdited(editor);
        queuePatientActEditor(editor, false, cancelPrescription, dispensing); // queue editing of the act
    }

    /**
     * Creates an editor for an <em>act.patientMedication</em>.
     *
     * @param product the product
     * @param act     the medication act
     */
    private void createMedicationEditor(Product product, Act act) {
        boolean dispensingLabel = hasDispensingLabel(product);
        IMObjectEditor editor = createEditor(act, dispensing);
        if (editor instanceof PatientMedicationActEditor) {
            ((PatientMedicationActEditor) editor).setQuantity(quantity);
        }
        dispensing.addEdited(editor);
        if (dispensingLabel) {
            // queue editing of the act
            queuePatientActEditor(editor, false, false, dispensing);
        }
    }

    /**
     * Invoked when the product changes to update investigation acts.
     * <p>
     * This removes any existing investigations, and creates new ones, if required.
     *
     * @param product the product. May be {@code null}
     */
    private void updateInvestigations(Product product) {
        if (investigations != null) {
            for (Act act : investigations.getCurrentActs()) {
                investigations.remove(act);
            }
            if (product != null) {
                // add a new investigation act for each investigation type (if any)
                for (Entity investigationType : getInvestigationTypes(product)) {
                    Act act = (Act) investigations.create();
                    if (act != null) {
                        IMObjectEditor editor = createEditor(act, investigations);
                        if (editor instanceof PatientInvestigationActEditor) {
                            PatientInvestigationActEditor investigationEditor = (PatientInvestigationActEditor) editor;
                            investigationEditor.setInvestigationType(investigationType);
                            investigationEditor.setProduct(product);
                        }
                        investigations.addEdited(editor);

                        // queue editing of the act
                        queuePatientActEditor(editor, true, false, investigations);
                    }
                }
            }
        }
    }

    /**
     * Invoked when the product changes, to update reminders acts.
     * <p>
     * This removes any existing reminders, and creates new ones, if required.
     *
     * @param product the product. May be {@code null}
     */
    private void updateReminders(Product product) {
        if (reminders != null) {
            for (Act act : reminders.getCurrentActs()) {
                reminders.remove(act);
            }
            if (product != null) {
                Map<Entity, EntityRelationship> reminderTypes = getReminderTypes(product);
                for (Map.Entry<Entity, EntityRelationship> entry : reminderTypes.entrySet()) {
                    Entity reminderType = entry.getKey();
                    EntityRelationship relationship = entry.getValue();
                    Act act = (Act) reminders.create();
                    if (act != null) {
                        IMObjectEditor editor = createEditor(act, reminders);
                        if (editor instanceof ReminderEditor) {
                            ReminderEditor reminder = (ReminderEditor) editor;
                            Date startTime = getStartTime();
                            reminder.setStartTime(startTime);
                            reminder.setReminderType(reminderType);
                            reminder.setPatient(getPatient());
                            reminder.setProduct(product);

                            // marking matching reminders completed are handled via AbstractCustomerChargeActEditor.
                            // Need to disable it here to avoid the rule updating other reminders in the invoice.
                            reminder.setMarkMatchingRemindersCompleted(false);

                            // override the due date calculated from the reminder type
                            Date dueDate = reminderRules.calculateProductReminderDueDate(startTime, relationship);
                            reminder.setEndTime(dueDate);
                        }
                        reminders.addEdited(editor);
                        IMObjectBean bean = new IMObjectBean(relationship);
                        boolean interactive = bean.getBoolean("interactive");
                        if (interactive) {
                            // queue editing of the act
                            queuePatientActEditor(editor, true, false, reminders);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates an act editor, with a new help context.
     *
     * @param act     the act to editor
     * @param editors the editor collection
     * @return the editor
     */
    private IMObjectEditor createEditor(Act act, ActRelationshipCollectionEditor editors) {
        return editors.createEditor(act, createLayoutContext(act));
    }

    /**
     * Creates a layout context for editing an act.
     *
     * @param act the act being edited
     * @return a new layout context
     */
    private LayoutContext createLayoutContext(Act act) {
        LayoutContext context = getLayoutContext();
        return new DefaultLayoutContext(context, context.getHelpContext().topic(act, "edit"));
    }

    /**
     * Invoked when the product changes to update the layout, if required.
     *
     * @param product the product. May be {@code null}
     */
    private void updateLayout(Product product) {
        updateLayout(product, updatePrint(product));
    }

    /**
     * Invoked when the product changes to update the layout, if required.
     *
     * @param product   the product. May be {@code null}
     * @param showPrint if {@code true}, show the print node
     */
    private void updateLayout(Product product, boolean showPrint) {
        ArchetypeNodes currentNodes = getArchetypeNodes();
        ArchetypeNodes expectedFilter = getFilterForProduct(product, showPrint);
        if (!ObjectUtils.equals(currentNodes, expectedFilter)) {
            Component popupFocus = null;
            Component focus = FocusHelper.getFocus();
            Property focusProperty = null;
            if (editorQueue != null && !editorQueue.isComplete()) {
                popupFocus = focus;
            } else if (focus instanceof BoundProperty) {
                focusProperty = ((BoundProperty) focus).getProperty();
            }
            changeLayout(expectedFilter);  // this can move the focus away from the popups, if any
            if (editorQueue != null && editorQueue.isComplete()) {
                // no current popups, so move focus to the original property if possible, otherwise move it to the
                // product
                if (focusProperty != null) {
                    if (!setFocus(focusProperty)) {
                        moveFocusToProduct();
                    }
                } else {
                    moveFocusToProduct();
                }
            } else {
                // move the focus back to the popup
                FocusHelper.setFocus(popupFocus);
            }
        }
    }

    /**
     * Updates the selling units label.
     *
     * @param product the product. May be {@code null}
     */
    private void updateSellingUnits(Product product) {
        String units = "";
        if (product != null) {
            IMObjectBean bean = new IMObjectBean(product);
            String node = "sellingUnits";
            if (bean.hasNode(node)) {
                units = LookupNameHelper.getName(product, node);
            }
        }
        sellingUnits.setText(units);
    }

    /**
     * Updates the batch.
     *
     * @param product       the product. May be {@code null}
     * @param stockLocation the stock location. May be {@code null}
     */
    private void updateBatch(Product product, IMObjectReference stockLocation) {
        BatchParticipationEditor batchEditor = getBatchEditor();
        if (batchEditor != null) {
            try {
                batchEditor.removeModifiableListener(batchListener);
                batchEditor.setStockLocation(stockLocation);
                batchEditor.setProduct(product);
                updateMedicationBatch(stockLocation);
            } finally {
                batchEditor.addModifiableListener(batchListener);
            }
        }
    }

    /**
     * Helper to return the investigation types for a product.
     * <p>
     * If there are multiple investigation types, these will be sorted on name.
     *
     * @param product the product
     * @return a list of investigation types
     */
    private List<Entity> getInvestigationTypes(Product product) {
        List<Entity> result = Collections.emptyList();
        EntityBean bean = new EntityBean(product);
        final String node = "investigationTypes";
        if (bean.hasNode(node)) {
            result = bean.getNodeTargetEntities(node);
            Collections.sort(result, IMObjectSorter.getNameComparator(true));
        }
        return result;
    }

    /**
     * Helper to return the reminder types and their relationships for a product.
     * <p>
     * If there are multiple reminder types, these will be sorted on name.
     *
     * @param product the product
     * @return a the reminder type relationships
     */
    private Map<Entity, EntityRelationship> getReminderTypes(Product product) {
        Map<EntityRelationship, Entity> map = reminderRules.getReminderTypes(product);
        Map<Entity, EntityRelationship> result = new TreeMap<>(IMObjectSorter.getNameComparator(true));
        for (Map.Entry<EntityRelationship, Entity> entry : map.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }
        return result;
    }

    /**
     * Queues an editor for display in a popup dialog.
     * Use this when there may be multiple editors requiring display.
     * <p>
     * NOTE: all objects should be added to the collection prior to them being edited. If they are skipped,
     * they will subsequently be removed. This is necessary as the layout excludes nodes based on elements being
     * present.
     *
     * @param editor     the editor to queue
     * @param skip       if {@code true}, indicates that the editor may be skipped
     * @param cancel     if {@code true}, indicates that the editor may be cancelled
     * @param collection the collection to remove the object from, if the editor is skipped
     */
    private void queuePatientActEditor(final IMObjectEditor editor, boolean skip, boolean cancel,
                                       final ActRelationshipCollectionEditor collection) {
        if (editorQueue != null) {
            editorQueue.queue(editor, skip, cancel, new EditorQueue.Listener() {
                public void completed(boolean skipped, boolean cancelled) {
                    if (skipped || cancelled) {
                        collection.remove(editor.getObject());
                    }
                    if (editorQueue.isComplete()) {
                        moveFocusToProduct();

                        // force the parent collection editor to re-check the validation status of
                        // this editor, in order for the Add button to be enabled.
                        getListeners().notifyListeners(CustomerChargeActItemEditor.this);
                    }
                }
            });
        }
    }

    /**
     * Updates the medication quantity from the invoice.
     */
    private void onQuantityChanged() {
        if (dispensing != null && quantity.getValue() != null) {
            dispensing.removeModifiableListener(dispensingListener);
            try {
                PatientMedicationActEditor editor = (PatientMedicationActEditor) dispensing.getCurrentEditor();
                if (editor == null) {
                    List<Act> acts = dispensing.getActs();
                    if (!acts.isEmpty()) {
                        editor = (PatientMedicationActEditor) dispensing.getEditor(acts.get(0));
                    }
                }
                if (editor != null) {
                    editor.setQuantity(quantity);
                    dispensing.refresh();
                }
            } finally {
                dispensing.addModifiableListener(dispensingListener);
            }
        }
    }

    /**
     * Updates the invoice quantity when a medication act changes.
     */
    private void updateQuantity() {
        Property property = getProperty(QUANTITY);
        property.removeModifiableListener(quantityListener);
        try {
            if (dispensing != null) {
                Set<Act> acts = new HashSet<>(dispensing.getActs());
                PatientMedicationActEditor current = (PatientMedicationActEditor) dispensing.getCurrentEditor();
                if (current != null) {
                    acts.add(current.getObject());
                }
                if (!acts.isEmpty()) {
                    BigDecimal total = ZERO;
                    for (Act act : acts) {
                        ActBean bean = new ActBean(act);
                        BigDecimal quantity = bean.getBigDecimal(QUANTITY, ZERO);
                        total = total.add(quantity);
                    }
                    setQuantity(total);
                }
                dispensing.refresh();
            }
        } finally {
            property.addModifiableListener(quantityListener);
        }
    }

    /**
     * Updates the medication batch from the invoice.
     *
     * @param stockLocation the stock location. May be {@code null}
     */
    private void updateMedicationBatch(IMObjectReference stockLocation) {
        BatchParticipationEditor batchEditor = getBatchEditor();
        if (batchEditor != null && dispensing != null) {
            dispensing.removeModifiableListener(dispensingListener);
            try {
                for (PatientMedicationActEditor editor : getMedicationActEditors()) {
                    editor.setBatch(batchEditor.getEntity());
                    editor.setStockLocation(stockLocation);
                }
                dispensing.refresh();
            } finally {
                dispensing.addModifiableListener(dispensingListener);
            }
        }
    }

    /**
     * Updates the batch if the medication batch changes.
     */
    private void updateBatch() {
        BatchParticipationEditor batchEditor = getBatchEditor();
        if (batchEditor != null) {
            batchEditor.removeModifiableListener(batchListener);
            try {
                PatientMedicationActEditor editor = getMedicationActEditor();
                if (editor != null) {
                    batchEditor.setEntity(editor.getBatch());
                }
            } finally {
                batchEditor.addModifiableListener(batchListener);
            }
        }
    }

    /**
     * Updates any child patient acts with the patient.
     */
    private void updatePatientActsPatient() {
        IMObjectReference patient = getPatientRef();
        for (PrescriptionMedicationActEditor editor : getMedicationActEditors()) {
            editor.setPatient(patient);
            changePrescription(editor);
        }
        for (PatientInvestigationActEditor editor : getInvestigationActEditors()) {
            editor.setPatient(patient);
        }
        for (ReminderEditor editor : getReminderEditors()) {
            editor.setPatient(patient);
        }
    }

    /**
     * Changes the prescription for an editor, if one is available.
     *
     * @param editor the editor
     */
    private void changePrescription(final PrescriptionMedicationActEditor editor) {
        Act prescription = getPrescription();
        if (prescription != null) {
            if (promptForPrescription) {
                Product product = getProduct();
                String title = Messages.get("customer.charge.prescription.title");
                String message = Messages.format("customer.charge.prescription.message", product.getName());
                ConfirmationDialog dialog = new ConfirmationDialog(title, message);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        editorQueue.queue(editor, true, cancelPrescription, null);
                    }
                });
                editorQueue.queue(dialog);
            } else {
                editorQueue.queue(editor, true, cancelPrescription, null);
            }
        }
    }

    /**
     * Updates any child patient acts with the clinician.
     */
    private void updatePatientActsClinician() {
        IMObjectReference clinician = getClinicianRef();
        for (PatientActEditor editor : getMedicationActEditors()) {
            editor.setClinician(clinician);
        }
        for (PatientInvestigationActEditor editor : getInvestigationActEditors()) {
            editor.setClinician(clinician);
        }
    }

    /**
     * Returns the medication editor.
     *
     * @return the medication editor, or {@code null} if none exists
     */
    private PrescriptionMedicationActEditor getMedicationActEditor() {
        Set<PrescriptionMedicationActEditor> editors = getMedicationActEditors();
        return !editors.isEmpty() ? editors.iterator().next() : null;
    }

    /**
     * Returns editors for each of the <em>act.patientMedication</em> acts.
     *
     * @return the editors
     */
    private Set<PrescriptionMedicationActEditor> getMedicationActEditors() {
        return getActEditors(dispensing);
    }

    /**
     * Returns the editors for each of the <em>act.patientInvestigation</em> acts.
     *
     * @return the editors
     */
    private Set<PatientInvestigationActEditor> getInvestigationActEditors() {
        return getActEditors(investigations);
    }

    /**
     * Returns the editors for each of the <em>act.patientReminder</em> acts.
     *
     * @return the editors
     */
    private Set<ReminderEditor> getReminderEditors() {
        return getActEditors(reminders);
    }

    /**
     * Returns the act editors for the specified collection editor.
     *
     * @param editors the collection editor. May be {@code null}
     * @return a set of editors
     */
    @SuppressWarnings("unchecked")
    private <T extends IMObjectEditor> Set<T> getActEditors(ActRelationshipCollectionEditor editors) {
        Set<T> result = new HashSet<>();
        if (editors != null) {
            for (Act act : editors.getCurrentActs()) {
                T editor = (T) editors.getEditor(act);
                result.add(editor);
            }
        }
        return result;
    }

    /**
     * Determines if a medication product requires a dispensing label.
     *
     * @param product the product
     * @return {@code true} if the product requires a dispensing label
     */
    private boolean hasDispensingLabel(Product product) {
        IMObjectBean bean = new IMObjectBean(product);
        return bean.getBoolean("label");
    }

    /**
     * Updates the stock location associated with the product.
     *
     * @param product the new product
     * @return the stock location. May be {@code null}
     */
    private IMObjectReference updateStockLocation(Product product) {
        Party stockLocation = null;
        if (TypeHelper.isA(product, MEDICATION, MERCHANDISE)) {
            Act parent = (Act) getParent();
            if (parent != null) {
                ActBean bean = new ActBean(parent);
                Party location = (Party) getObject(bean.getNodeParticipantRef("location"));
                if (location != null) {
                    stockLocation = rules.getStockLocation(product, location);
                }
            }
        }
        ActBean bean = new ActBean(getObject());
        if (stockLocation != null) {
            bean.setParticipant(STOCK_LOCATION_PARTICIPATION, stockLocation);
        } else {
            bean.removeParticipation(STOCK_LOCATION_PARTICIPATION);
        }
        return stockLocation != null ? stockLocation.getObjectReference() : null;
    }

    /**
     * Returns the value of the cost node of a price.
     *
     * @param price the product price
     * @return the cost
     */
    private BigDecimal getCost(ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price);
        return bean.getBigDecimal("cost", ZERO);
    }

    /**
     * Returns a node filter for the specified product reference.
     * <p>
     * This excludes:
     * <ul>
     * <li>the dispensing node if the product isn't a <em>product.medication</em>
     * <li>the investigations node if the product isn't a <em>product.medication</em>, <em>product.merchandise</em>,
     * or <em>product.service</em>
     * <li>the reminders node is excluded if there are no reminders present.
     * <li>the discount node, if discounts are disabled</li>
     * </ul>
     *
     * @param product   the product. May be {@code null}
     * @param showPrint if {@code true} show the print node
     * @return a node filter for the product. If {@code null}, no nodes require filtering
     */
    private ArchetypeNodes getFilterForProduct(Product product, boolean showPrint) {
        ArchetypeNodes result = null;
        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            result = TEMPLATE_NODES;
        } else {
            List<String> filter = new ArrayList<>();
            filter.add(DISPENSING);
            filter.add(INVESTIGATIONS);
            filter.add(REMINDERS);
            if (getDisableDiscounts()) {
                filter.add(DISCOUNT);
            }
            boolean medication = TypeHelper.isA(product, MEDICATION);
            if (medication) {
                filter.remove(DISPENSING);
            }
            if (medication || TypeHelper.isA(product, MERCHANDISE, SERVICE)) {
                filter.remove(INVESTIGATIONS);
            }
            if (reminders != null && reminders.getCollection().size() > 0) {
                filter.remove(REMINDERS);
            }
            if (!filter.isEmpty()) {
                result = new ArchetypeNodes().exclude(filter);
            }
            if (showPrint) {
                if (result == null) {
                    result = new ArchetypeNodes();
                }
                result.simple(PRINT).order(PRINT, TAX);
            }
            if (isOrdered() && hasReceivedOrReturnedQuantity()) {
                if (result == null) {
                    result = new ArchetypeNodes();
                }
                result.simple(ORDER_NODES);
            }
        }
        return result;
    }

    /**
     * Determines if there is a received or returned quantity set.
     *
     * @return {@code true} if there is a received or returned quantity set
     */
    private boolean hasReceivedOrReturnedQuantity() {
        Property received = getProperty(RECEIVED_QUANTITY);
        Property returned = getProperty(RETURNED_QUANTITY);
        return (received != null && received.getBigDecimal() != null)
               || (returned != null && returned.getBigDecimal() != null);
    }

    /**
     * Updates the print flag when the product or total changes.
     *
     * @param product the product. May be {@code null}
     * @return {@code true} if the print flag should be shown
     */
    private boolean updatePrint(Product product) {
        boolean result = false;
        if (product != null) {
            BigDecimal total = getTotal();
            result = MathRules.equals(total, ZERO);
            if (result) {
                Product template = getTemplate();
                if (template != null) {
                    IMObjectBean bean = new IMObjectBean(template);
                    result = !bean.getBoolean(PRINT_AGGREGATE);
                }
            } else {
                setPrint(true);
            }
        }
        return result;
    }

    /**
     * Helper to create a collection editor for an act relationship node, if the node exists.
     * <p>
     * The returned editor is configured to not exclude default value objects.
     *
     * @param name the collection node name
     * @param act  the act
     * @return the collection editor, or {@code null} if the node doesn't exist
     */
    private ActRelationshipCollectionEditor createCollectionEditor(String name, Act act) {
        ActRelationshipCollectionEditor editor = null;
        CollectionProperty collection = (CollectionProperty) getProperty(name);
        if (collection != null && !collection.isHidden()) {
            editor = (ActRelationshipCollectionEditor) IMObjectCollectionEditorFactory.create(
                    collection, act, getLayoutContext());
            editor.setExcludeDefaultValueObject(false);
            getEditors().add(editor);
        }
        return editor;
    }

    /**
     * Creates an editor for the "dispensing" node.
     *
     * @return a new editor
     */
    private DispensingActRelationshipCollectionEditor createDispensingCollectionEditor() {
        DispensingActRelationshipCollectionEditor editor = null;
        CollectionProperty collection = (CollectionProperty) getProperty("dispensing");
        if (collection != null && !collection.isHidden()) {
            editor = new DispensingActRelationshipCollectionEditor(collection, getObject(), getLayoutContext());
            getEditors().add(editor);
        }
        return editor;
    }

    /**
     * Returns the product batch participation editor.
     *
     * @return the product batch participation, or {@code null}  if none exists
     */
    protected BatchParticipationEditor getBatchEditor() {
        return getBatchEditor(true);
    }

    /**
     * Returns the product batch participation editor.
     *
     * @param create if {@code true} force creation of the edit components if it hasn't already been done
     * @return the product batch participation, or {@code null} if none exists
     */
    protected BatchParticipationEditor getBatchEditor(boolean create) {
        ParticipationEditor<Entity> editor = getParticipationEditor("batch", create);
        return (BatchParticipationEditor) editor;
    }

    protected class CustomerChargeItemLayoutStrategy extends PriceItemLayoutStrategy {

        public CustomerChargeItemLayoutStrategy(FixedPriceEditor fixedPrice) {
            super(fixedPrice);
        }

        /**
         * Apply the layout strategy.
         * <p>
         * This renders an object in a {@code Component}, using a factory to create the child components.
         *
         * @param object     the object to apply
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component containing the rendered {@code object}
         */
        @Override
        public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            Row row = RowFactory.create(Styles.CELL_SPACING, quantity.getComponent(), sellingUnits);
            addComponent(new ComponentState(row, quantity.getProperty()));
            if (dispensing != null) {
                addComponent(new ComponentState(dispensing));
            }
            if (investigations != null) {
                addComponent(new ComponentState(investigations));
            }
            if (reminders != null) {
                addComponent(new ComponentState(reminders));
            }
            return super.apply(object, properties, parent, context);
        }

        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            ComponentState state;
            String name = property.getName();
            if ((PATIENT.equals(name) || PRODUCT.equals(name)) && isOrdered()) {
                // the item has been ordered via an HL7 service. The patient and product cannot be changed
                state = super.createComponent(createReadOnly(property), parent, context);
            } else {
                state = super.createComponent(property, parent, context);
            }
            return state;
        }
    }
}
