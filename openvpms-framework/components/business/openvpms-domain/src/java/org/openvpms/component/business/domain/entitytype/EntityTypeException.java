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

//
package org.openvpms.component.business.domain.entitytype;

// commons-resources
import org.apache.commons.resources.Messages;

// openvpms-common-exception
import org.openvpms.component.common.exception.ExceptionHelper;
import org.openvpms.component.common.exception.OpenVPMSException;

/**
 * Extends the base exception class {@link OpenVPMSException} and defines error
 * codes specific to this package.
 * 
 * @see OpenVPMSException
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $Revision$
 */
public class EntityTypeException extends RuntimeException implements
        OpenVPMSException {

    /**
     * Define the error codes that are generated by this exception
     */
    public enum ErrorCode {
        INVALID_PROPERTY_TYPE, PROPERTY_TYPE_ALREADY_EXISTS;
    }

    /**
     * Fault in the messages for this package.
     */
    private final static Messages messages = Messages
            .getMessages(EntityTypeException.class.getPackage().getName() + "/"
                    + OpenVPMSException.ERRMESSAGES_FILE);

    /**
     * Generated SUID.
     */
    private static final long serialVersionUID = 4124966375305125417L;

    /**
     * The single constructor is used to create an exception passing the 
     * error code and a set of parameters. If this is a secondary exception
     * then we can also pass across the cause.
     * <p>
     * The params are used to render the message associated with the errorCode,
     * which is stored in a package-level property file
     * 
     * @param errorCode
     * @param params
     * @param cause
     */
    public EntityTypeException(ErrorCode errorCode, Object[] params,
            Throwable cause) {
        super(ExceptionHelper.renderMessage(messages, errorCode.toString(),
                params), cause);
    }
}
