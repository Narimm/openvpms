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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper.lookup;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.archetype.AssertionDescriptor;
import org.openvpms.component.model.archetype.NodeDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Returns lookups for assertions that refer to the lookup by archetype short
 * name.
 * <p/>
 * The assertion must be named <em>"lookup"<em>, and contain a <em>"type"</em>
 * property with value <em>"lookup"</em> and a <em>"source"</em> property
 * specifying the lookup shortname.
 * E.g:
 * <pre>
 *   &lt;node name="country" path="/details/country" type="java.lang.String"&gt;
 *     &lt;assertion name="lookup"&gt;
 * 	     &lt;property name="type" value="lookup" /&gt;
 *       &lt;property name="source" value="lookup.country" /&gt;
 *     &lt;/assertion&gt;
 *   &lt;/node&gt;
 * </pre>
 *
 * @author Tim Anderson
 */
public class RemoteLookup extends AbstractLookupAssertion {

    /**
     * The lookup type.
     */
    public static final String TYPE = "lookup"; // NON-NLS

    /**
     * The node descriptor.
     */
    private final NodeDescriptor descriptor;

    /**
     * The lookup shortname.
     */
    private final String source;


    /**
     * Constructs a {@link RemoteLookup}.
     *
     * @param descriptor    the node descriptor
     * @param assertion     the assertion descriptor
     * @param service       the archetype service
     * @param lookupService the lookup service
     */
    public RemoteLookup(NodeDescriptor descriptor, AssertionDescriptor assertion, IArchetypeService service,
                        ILookupService lookupService) {
        super(assertion, TYPE, service, lookupService);
        this.descriptor = descriptor;
        source = getProperty("source");
        if (StringUtils.isEmpty(source)) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.SourceNotSpecified, new Object[]{assertion.getName(), "lookup"});
        }
    }

    /**
     * Returns the lookup archetype short name.
     *
     * @return the lookup archetype short name
     */
    public String getShortName() {
        return source;
    }

    /**
     * Returns the lookups for this assertion.
     *
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if this method is unsupported by the lookup type
     */
    public List<Lookup> getLookups() {
        Collection<Lookup> lookups = getLookupService().getLookups(source);
        return new ArrayList<>(lookups);
    }

    /**
     * Returns the lookups for this assertion.
     *
     * @param context the context
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Collection<Lookup> getLookups(IMObject context) {
        Collection<Lookup> lookups = super.getLookups(context);
        org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor d
                = (org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor) descriptor;
        Object value = d.getValue(context);
        String code = (value instanceof String) ? (String) value : null;
        if (code != null) {
            // if the code refers to an inactive lookup, ensure it is included
            boolean found = false;
            for (Lookup lookup : lookups) {
                if (code.equals(lookup.getCode())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Lookup lookup = getLookup(code);
                if (lookup != null) {
                    lookups.add(lookup);
                }
            }
        }
        return lookups;
    }

    /**
     * Returns the lookup with the specified code.
     *
     * @return the lookup matching {@code code}, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Lookup getLookup(String code) {
        return getLookupService().getLookup(source, code, false);
    }

    /**
     * Returns the default lookup.
     *
     * @return the default lookup or {@code null} if there is no default
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Lookup getDefault() {
        return getLookupService().getDefaultLookup(source);
    }

}
