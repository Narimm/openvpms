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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;

/**
 * A popup window that displays an {@link IMObjectEditor}.
 *
 * @author Tim Anderson
 */
public class EditDialog extends AbstractEditDialog {

    /**
     * Constructs an {@code EditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     * @param help    the help context
     */
    public EditDialog(IMObjectEditor editor, Context context, HelpContext help) {
        this(editor, true, context, help);
    }

    /**
     * Constructs an {@code EditDialog}.
     *
     * @param editor  the editor
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param context the context
     * @param help    the help context
     */
    public EditDialog(IMObjectEditor editor, boolean save, Context context, HelpContext help) {
        this(editor, save, false, context, help);
    }

    /**
     * Constructs an {@code EditDialog}.
     *
     * @param editor  the editor
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param skip    if {@code true} display a 'Skip' button that simply closes the dialog
     * @param context the context
     * @param help    the help context
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean skip, Context context, HelpContext help) {
        this(editor, save, true, skip, context, help);
    }

    /**
     * Constructs an {@code EditDialog}.
     *
     * @param editor  the editor
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param apply   if {@code true}, display an 'Apply' button
     * @param skip    if {@code true} display a 'Skip' button that simply closes the dialog
     * @param context the context
     * @param help    the help context
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean apply, boolean skip, Context context,
                      HelpContext help) {
        this(editor, save, apply, true, skip, context, help);
    }

    /**
     * Constructs an {@code EditDialog}.
     *
     * @param editor the editor
     * @param save   if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param apply  if {@code true}, display an 'Apply' button
     * @param cancel if {@code true}, display a 'Cancel' button
     * @param skip   if {@code true} display a 'Skip' button that simply closes the dialog
     * @param context the context
     * @param help   the help context
     */
    public EditDialog(IMObjectEditor editor, boolean save, boolean apply, boolean cancel, boolean skip,
                      Context context, HelpContext help) {
        super(editor, getButtons(apply, cancel, skip), save, context, help);
    }

    /**
     * Constructs an {@code EditDialog}.
     *
     * @param editor  the editor
     * @param buttons the buttons to display
     * @param save    if {@code true}, saves the editor when the 'OK' or 'Apply' buttons are pressed.
     * @param context the context
     * @param help    the help context
     */
    public EditDialog(IMObjectEditor editor, String[] buttons, boolean save, Context context, HelpContext help) {
        super(editor, buttons, save, context, help);
    }

}
