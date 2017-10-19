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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.internal.InsuranceFactory;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.identity.SingleIdentityCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Editor for <em>act.patientInsuranceClaim</em>.
 *
 * @author Tim Anderson
 */
public class ClaimEditor extends AbstractClaimEditor {

    /**
     * The attachments.
     */
    private final AttachmentCollectionEditor attachments;

    /**
     * The claim items.
     */
    private final ClaimItemCollectionEditor items;

    /**
     * The attachment generator.
     */
    private final AttachmentGenerator generator;

    /**
     * The insuranceId node editor, for non-eClaims.
     */
    private SingleIdentityCollectionEditor insuranceId;

    /**
     * Constructs an {@link ClaimEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public ClaimEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        super(act, parent, "amount", context);

        ActBean claimBean = new ActBean(act);
        Act policy = (Act) claimBean.getNodeTargetObject("policy");
        if (policy == null) {
            throw new IllegalStateException("Claim has no policy");
        }
        ActBean policyBean = new ActBean(policy);
        Party customer = (Party) policyBean.getNodeParticipant("customer");
        if (customer == null) {
            throw new IllegalStateException("Policy has no customer");
        }

        if (act.isNew()) {
            initParticipant("patient", context.getContext().getPatient());
            initParticipant("location", context.getContext().getLocation());
            initParticipant("clinician", context.getContext().getClinician());
        }
        Party patient = getPatient();
        if (patient == null) {
            throw new IllegalStateException("Claim has no patient");
        }

        Editors editors = getEditors();
        if (!canSubmitClaim(act)) {
            // users can't submit the claim via an InsuranceService, so allow the insuranceId to be edited
            CollectionProperty property = getCollectionProperty("insuranceId");
            if (property.getValues().isEmpty()) {
                IMObject identity = IMObjectCreator.create(InsuranceArchetypes.CLAIM_IDENTITY);
                property.add(identity);
            }
            insuranceId = new SingleIdentityCollectionEditor(property, act, context);
            editors.add(insuranceId);
        }
        attachments = new AttachmentCollectionEditor(getCollectionProperty("attachments"), act, context);
        items = new ClaimItemCollectionEditor(getCollectionProperty("items"), act, customer, patient,
                                              new Charges(), attachments, context);
        editors.add(attachments);
        editors.add(items);

        generator = new AttachmentGenerator(customer, patient, context.getContext());
        items.addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onItemsChanged();
            }
        });
    }

    public Party getPatient() {
        return (Party) getParticipant("patient");
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        return new ClaimEditor((FinancialAct) reload(getObject()), getParent(), getLayoutContext());
    }

    /**
     * Generates attachments.
     */
    public void generateAttachments() {
        generator.generate(attachments);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new ClaimLayoutStrategy(getPatient(), insuranceId, items, attachments);
    }

    /**
     * Returns the item acts to sum.
     *
     * @return the acts
     */
    @Override
    protected List<Act> getItemActs() {
        return items.getActs();
    }

    /**
     * Determines if a claim can be submitted via an {@link InsuranceService}.
     *
     * @param act the claim act
     * @return {@code true} if the claim can be submitted
     */
    private boolean canSubmitClaim(Act act) {
        Claim claim = ServiceHelper.getBean(InsuranceFactory.class).createClaim(act);
        return ServiceHelper.getBean(InsuranceServices.class).canSubmit(claim);
    }

}
