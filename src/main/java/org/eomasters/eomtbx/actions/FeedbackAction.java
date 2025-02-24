/*-
 * ========================LICENSE_START=================================
 * EOMTBX Basic - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
 * ======================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Help", id = "EOMTBX_Feedback")
@ActionRegistration(displayName = "#FeedbackActionName", lazy = false)
@ActionReference(path = "Menu/Help", position = 310)
@NbBundle.Messages({"FeedbackActionName=EOMTBX Feedback"})

public class FeedbackAction extends AbstractAction {

    // Constructor to set the action properties
    public FeedbackAction() {
        super(Bundle.FeedbackActionName(), Icons.EOMASTERS.getImageIcon(Icon.SIZE_16));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Open the feedback dialog when the action is triggered
        FeedbackDialog feedbackDialog = new FeedbackDialog();
        feedbackDialog.showDialog();
    }
}
