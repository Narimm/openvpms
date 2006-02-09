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


package org.openvpms.component.business.domain.im.common;

// java core
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * A class representing an activity that is being done, has been done, 
 * can be done, or is intended or requested to be done.  An Act instance 
 * is a record of an intentional business action.  

 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Act extends IMObject {

    /**
     * Generated SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Text that defines the modality of the Act i.e Definition, Intent, 
     * Event, Goal. The mood of an Act does not change. To describe the 
     * progression of an Act from defined through to executed you create 
     * different Acts connected via ActRelationships.
     * 
     * TODO Change to use terminology service
     */
    private String mood;
    
    /**
     * Represents the title of the act.
     * 
     * TODO Change to use terminology service
     */
    private String title;
    
    /**
     * The start time of this act
     */
    private Date activityStartTime;
    
    /**
     * The end time of this activity
     */
    private Date activityEndTime;
    
    /**
     * Text representing the reason for the Act. Often this is beter 
     * represented by a realtionship to another Act of type "has reason".
     */
    private String reason;
    
    /**
     * An interval of integers stating the minimal and maximum nymber of Act 
     * repetitions. 
     */
    private int repeatNumber;

    /**
     * A String representing the status or state of the Act. (i.e  Normal, 
     * Aborted, Completed, Suspended, Cancelled etc
     */
    private String status;
    
    /**
     * Describes the specific details of the act, whether it is clinical,
     * financial or other.
     */
    private DynamicAttributeMap details;
    
    /**
     * The {@link Participations} for this act.
     */
    private Set<Participation> participations =
        new HashSet<Participation>();
    
    /**
     * Holds all the {@link ActRelationship}s that this act is a source off.
     */
    private Set<ActRelationship> sourceActRelationships =
        new HashSet<ActRelationship>();
    
    /**
     * Holds all the {@link ActRelationship}s that this act is a target off.
     */
    private Set<ActRelationship> targetActRelationships =
        new HashSet<ActRelationship>();

    /**
     * Default constructor
     */
    public Act() {
        // do nothing
    }
    
    /**
     * Constructs an instance of an act.
     * 
     * @param archetypeI
     *            the archetype id constraining this object
     * @param details
     *            dynamic details of the act.
     */
    public Act(ArchetypeId archetypeId, DynamicAttributeMap details) {
        super(archetypeId);
        this.details = details;
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /**
     * @return Returns the activityEndTime.
     */
    public Date getActivityEndTime() {
        return activityEndTime;
    }

    /**
     * @param activityEndTime The activityEndTime to set.
     */
    public void setActivityEndTime(Date activityEndTime) {
        this.activityEndTime = activityEndTime;
    }

    /**
     * @return Returns the activityStartTime.
     */
    public Date getActivityStartTime() {
        return activityStartTime;
    }

    /**
     * @param activityStartTime The activityStartTime to set.
     */
    public void setActivityStartTime(Date activityStartTime) {
        this.activityStartTime = activityStartTime;
    }

    /**
     * @return Returns the mood.
     */
    public String getMood() {
        return mood;
    }

    /**
     * @param mood The mood to set.
     */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /**
     * @return Returns the reason.
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason The reason to set.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return Returns the repeatNumber.
     */
    public int getRepeatNumber() {
        return repeatNumber;
    }

    /**
     * @param repeatNumber The repeatNumber to set.
     */
    public void setRepeatNumber(int repeatNumber) {
        this.repeatNumber = repeatNumber;
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Returns the sourceActRelationships.
     */
    public Set<ActRelationship> getSourceActRelationships() {
        return sourceActRelationships;
    }

    /**
     * @param sourceActRelationships The sourceActRelationships to set.
     */
    public void setSourceActRelationships(
            Set<ActRelationship> sourceActRelationships) {
        this.sourceActRelationships = sourceActRelationships;
    }

    /**
     * Add a source {@link ActRelationship}.
     * 
     * @param source 
     */
    public void addSourceActRelationship(ActRelationship source) {
        this.sourceActRelationships.add(source);
    }

    /**
     * Remove a source {@link ActRelationship}.
     * 
     * @param source
     */
    public void removeSourceActRelationship(ActRelationship source) {
        this.sourceActRelationships.remove(source);
    }

    /**
     * @return Returns the targetActRelationships.
     */
    public Set<ActRelationship> getTargetActRelationships() {
        return targetActRelationships;
    }

    /**
     * Set this act to be a targt of an {@link ActRelationship}.
     * 
     * @param targetActRelationships The targetActRelationships to set.
     */
    public void setTargetActRelationships(
            Set<ActRelationship> targetActRelationships) {
        this.targetActRelationships = targetActRelationships;
    }
    
    /**
     * Add a target {@link ActRelationship}.
     * 
     * @param target 
     *            add a new target.
     */
    public void addTargetActRelationship(ActRelationship target) {
        this.targetActRelationships.add(target);
    }

    /**
     * Remove a target {@link ActRelationship}.
     * 
     * @param target
     */
    public void removeTargetActRelationship(ActRelationship target) {
        this.targetActRelationships.remove(target);
    }

    /**
     * Add a relationship to this act. It will determine whether it is a 
     * source or target relationship before adding it.
     * 
     * @param actRel
     *            the act relationship to add
     * @throws EntityException
     *            if this relationship cannot be added to this act            
     */
    public void addActRelationship(ActRelationship actRel) {
        if ((actRel.getSource().getUid() == this.getUid()) &&
            (actRel.getSource().getArchetypeId().equals(this.getArchetypeId()))){
            addSourceActRelationship(actRel);
        } else if ((actRel.getTarget().getUid() == this.getUid()) &&
            (actRel.getTarget().getArchetypeId().equals(this.getArchetypeId()))){
            addTargetActRelationship(actRel);
        } else {
            throw new EntityException(
                    EntityException.ErrorCode.FailedToAddActRelationship,
                    new Object[] { actRel.getSource().getUid(), 
                            actRel.getTarget().getUid()});
        }
    }

    /**
     * Remove a relationship to this act. It will determine whether it is a 
     * source or target relationship before removing it.
     * 
     * @param actRel
     *            the act relationship to remove
     * @throws EntityException
     *            if this relationship cannot be removed from this act            
     */
    public void removeActRelationship(ActRelationship actRel) {
        if ((actRel.getSource().getUid() == this.getUid()) &&
            (actRel.getSource().getArchetypeId().equals(this.getArchetypeId()))){
            removeSourceActRelationship(actRel);
        } else if ((actRel.getTarget().getUid() == this.getUid()) &&
            (actRel.getTarget().getArchetypeId().equals(this.getArchetypeId()))){
            removeTargetActRelationship(actRel);
        } else {
            throw new EntityException(
                    EntityException.ErrorCode.FailedToRemoveActRelationship,
                    new Object[] { actRel.getSource().getUid(), 
                            actRel.getTarget().getUid()});
        }
    }
    
    /**
     * Return all the act relationships. Do not use the returned set to 
     * add and remove act relationships. Instead use {@link #addActRelationship(ActRelationship)}
     * and {@link #removeActRelationship(ActRelationship)} repsectively.
     * 
     * @return Set<ActRelationship>
     */
    public Set<ActRelationship> getActRelationships() {
        Set<ActRelationship> relationships = 
            new HashSet<ActRelationship>(sourceActRelationships);
        relationships.addAll(targetActRelationships);
        
        return relationships;
    }
    /**
     * Return the associated {@link Participantion} instances.
     * 
     * @return Participation
     */
    public Set<Participation> getParticipations() {
        return participations;
    }

    /**
     * @param participations The participations to set.
     */
    public void setParticipations(Set<Participation> participations) {
        this.participations = participations;
    }
    
    /**
     * Add a {@link Participation}.
     * 
     * @param participation 
     */
    public void addParticipation(Participation participation) {
        this.participations.add(participation);
    }

    /**
     * Remove a {@link Participation}.
     * 
     * @param source
     */
    public void removeParticipation(Participation participation) {
        this.participations.remove(participation);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Act copy = (Act)super.clone();
        
        copy.activityEndTime = (Date)(this.activityEndTime == null ?
                null : this.activityEndTime.clone());
        copy.activityStartTime = (Date)(this.activityStartTime == null ?
                null : this.activityStartTime.clone());
        copy.details = (DynamicAttributeMap)(this.details == null ?
                null : this.details.clone());
        copy.mood = this.mood;
        copy.participations = new HashSet<Participation>(this.participations);
        copy.reason = this.reason;
        copy.repeatNumber = this.repeatNumber;
        copy.sourceActRelationships = new HashSet<ActRelationship>(this.sourceActRelationships);
        copy.status = this.status;
        copy.targetActRelationships = new HashSet<ActRelationship>(this.targetActRelationships);
        copy.title = this.title;
        
        return copy;
    }
}
