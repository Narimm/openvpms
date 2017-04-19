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

package org.openvpms.smartflow.event.impl;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Note;
import org.openvpms.smartflow.model.NotesList;
import org.openvpms.smartflow.model.event.NotesEvent;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.shortName;

/**
 * Processes {@link NotesEvent} events.
 *
 * @author Tim Anderson
 */
public class NotesEventProcessor extends EventProcessor<NotesEvent> {

    /**
     * Constructs a {@link NotesEventProcessor}.
     *
     * @param service the archetype service
     */
    public NotesEventProcessor(IArchetypeService service) {
        super(service);
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(NotesEvent event) {
        NotesList notes = event.getObject();
        if (notes != null && notes.getNotes() != null) {
            for (Note note : notes.getNotes()) {
                process(note);
            }
        }
    }

    /**
     * Processes a note event.
     *
     * @param event the event
     */
    protected void process(Note event) {
        IArchetypeService service = getService();
        Act visit = getVisit(event.getHospitalizationId(), null);
        Act act = getNote(event, visit);
        String status = event.getStatus();
        List<IMObject> toSave = new ArrayList<>();
        if (Note.REMOVED_STATUS.equals(status)) {
            if (act != null) {
                if (!ActStatus.POSTED.equals(act.getStatus())) {
                    service.remove(act);
                } else {
                    Party patient = getPatient(visit);
                    String text = FlowSheetMessages.cannotDeleteFinalisedNote();
                    addAddendum(visit, act, patient, text, toSave);
                }
            }
        } else {
            ActBean bean;
            Party patient = getPatient(visit);
            if (act == null) {
                act = (Act) service.create(PatientArchetypes.CLINICAL_NOTE);

                bean = new ActBean(act, service);
                bean.addNodeParticipation("patient", patient);
                ActIdentity identity = createIdentity(event, service);
                act.addIdentity(identity);

                ActBean visitBean = new ActBean(visit, service);
                visitBean.addNodeRelationship("items", act);
                toSave.add(visit);
            } else {
                bean = new ActBean(act, service);
            }
            if (ActStatus.POSTED.equals(act.getStatus())) {
                addAddendum(visit, act, patient, event.getText(), toSave);
            } else {
                bean.setValue("note", event.getText());
                toSave.add(act);
            }
        }
        if (!toSave.isEmpty()) {
            service.save(toSave);
        }
    }

    private void addAddendum(Act visit, Act note, Party patient, String text, List<IMObject> toSave) {
        IArchetypeService service = getService();
        ActBean bean = new ActBean(note, service);
        Act addendum = createAddendum(patient, text);

        bean.addNodeRelationship("addenda", addendum);
        ActBean visitBean = new ActBean(visit, service);
        visitBean.addNodeRelationship("items", addendum);

        toSave.add(note);
        toSave.add(addendum);
        toSave.add(visit);
    }

    private Act createAddendum(Party patient, String note) {
        IArchetypeService service = getService();
        Act addendum = (Act) service.create(PatientArchetypes.CLINICAL_ADDENDUM);
        ActBean addendumBean = new ActBean(addendum, service);
        addendumBean.setNodeParticipant("patient", patient);
        addendumBean.setValue("note", note);
        return addendum;
    }

    private ActIdentity createIdentity(Note event, IArchetypeService service) {
        ActIdentity identity = (ActIdentity) service.create("actIdentity.smartflowsheet");
        identity.setIdentity(event.getNoteGuid());
        return identity;
    }

    private Act getNote(Note note, Act visit) {
        ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_NOTE);
        query.add(join("event").add(eq("source", visit.getObjectReference())));
        query.add(join("identities", shortName("actIdentity.smartflowsheet")).add(eq("identity", note.getNoteGuid())));
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(getService(), query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

}
