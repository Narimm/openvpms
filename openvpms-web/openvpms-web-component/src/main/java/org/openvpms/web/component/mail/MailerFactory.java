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

package org.openvpms.web.component.mail;

import org.springframework.mail.javamail.JavaMailSender;

/**
 * Factory for {@link Mailer} instances.
 *
 * @author Tim Anderson
 */
public interface MailerFactory {

    /**
     * Creates a new {@link Mailer}.
     *
     * @param context the mail context
     * @return a new {@link Mailer}
     */
    Mailer create(MailContext context);

    /**
     * Creates a new {@link Mailer} that uses the specified mail server settings.
     *
     * @param context the mail context
     * @param sender  the mail sender to use
     * @return a new {@link Mailer}
     */
    Mailer create(MailContext context, JavaMailSender sender);
}
