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


package org.openvpms.component.business.service.archetype;

// java-core
import java.io.Serializable;

//openevpms-framework
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.Archetype;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.archetype.Node;


/**
 * This class maintains a mapping between a short name, the archetype 
 * identity and the archetype details. It also has some convenienc functions
 * for retrieving details from the archetype details.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeRecord implements Serializable {

    /**
     * SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The archetype identity
     */
    private ArchetypeId archetypeId;
    
    /**
     * The fully qualified java class name
     */
    private String className;
    
    /**
     * The archetype details 
     */
    private Archetype archetype;
    
    /**
     * Construct a record using a short name, ans archetype id and the 
     * associated information model class
     * 
     * @param id
     *            the archetype id
     * @param className
     *            the java class name
     * @param archetype
     *            the archetype details            
     */
    public ArchetypeRecord(ArchetypeId id, String className, Archetype archetype) {
        this.archetypeId = id;
        this.className = className; 
        this.archetype = archetype;
    }

    /**
     * @return Returns the archetypeId.
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * @param archetypeId The archetypeId to set.
     */
    public void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
    }

    /**
     * @return Returns the archetype.
     */
    public Archetype getArchetype() {
        return archetype;
    }

    /**
     * @param archetype The archetype to set.
     */
    public void setArchetype(Archetype archetype) {
        this.archetype = archetype;
    }
    
    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Retrieve the node for a particular path or null if one doesn't
     * exist.
     * 
     * @param path
     *            the path to search for
     * @return Node            
     */
    public Node getNodeAt(String path) {
        return recursivelySearchForNodeAt(getArchetype().getNode(), path);
    }
    
    /**
     * This is a reentrant method that searchs for a node with the specified
     * path. This method will return the matching node or null if one is not
     * found.
     * 
     * @param nodes
     *            the array of nodes to search
     * @param path
     *            this is the path to search for
     * @return Node                        
     */
    private Node recursivelySearchForNodeAt(Node[] nodes, String path) {
        for (Node node : nodes) {
            if (node.getPath().equals(path)) {
                return node;
            }
            
            if (node.getNode() != null) {
                Node anode = recursivelySearchForNodeAt(node.getNode(), path);
                if (anode != null) {
                    return anode;
                }
            }
        }
        
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("archetypeId", archetypeId)
            .append("className", className)
            .toString();
    }
}
