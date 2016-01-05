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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import org.openvpms.web.component.service.CurrentLocationMailService;
import org.openvpms.web.system.ServiceHelper;


/**
 * Default implementation of the {@link Mailer} interface.
 *
 * @author Tim Anderson
 */
public class DefaultMailer extends AbstractMailer {

    /**
     * Constructs a {@link DefaultMailer}.
     *
     * @param context the mail context
     */
    public DefaultMailer(MailContext context) {
        super(context, ServiceHelper.getBean(CurrentLocationMailService.class), ServiceHelper.getDocumentHandlers());
    }

}
