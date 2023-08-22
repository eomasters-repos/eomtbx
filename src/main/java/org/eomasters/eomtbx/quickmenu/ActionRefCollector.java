/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.quickmenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eomasters.eomtbx.utils.ErrorHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

class ActionRefCollector {

  private static final String[] EXCLUDE_ACTION_IDS = {"separator", "spacer", "master-help"};
  public static final String[] EXCLUDE_ELEMENTS_CONTAINING = {"Quick Menu", "Reopen Product"};

  static List<ActionRef> collect() {
    return doCollect(new ArrayList<>(), FileUtil.getConfigFile("Menu"));
  }

  private static List<ActionRef> doCollect(ArrayList<ActionRef> actionRefs, FileObject menuFolder) {
    String path = menuFolder.getPath() + "/";
    for (FileObject fileObject : menuFolder.getChildren()) {
      try {
        if (fileObject.isFolder()) {
          doCollect(actionRefs, fileObject);
        } else {
          String actionId = fileObject.getName();
          if (shallExclude(EXCLUDE_ACTION_IDS, actionId.toLowerCase())) {
            continue;
          }
          Object originalFileObject = fileObject.getAttribute("originalFile");
          if (originalFileObject == null) {
            continue;
          }
          String originalFile = originalFileObject.toString();
          FileObject delegatesTo = FileUtil.getConfigFile(originalFile);
          if (delegatesTo == null) {
            continue;
          }
          String displayName = (String) delegatesTo.getAttribute("displayName");
          if (displayName == null || displayName.isBlank()) {
            continue;
          }
          if (shallExclude(EXCLUDE_ELEMENTS_CONTAINING, path + "/" + displayName)) {
            continue;
          }
          actionRefs.stream().
              filter(aref -> aref.getActionId().equals(actionId))
              .findAny().
              ifPresentOrElse(ref -> ref.addMenuRef(new MenuRef(path, displayName)),
                  () -> actionRefs.add(new ActionRef(actionId, new MenuRef(path, displayName))));

        }
      } catch (Exception e) {
        ErrorHandler.handle("Error while collecting actions for " + QuickMenuAction.QUICK_MENU_NAME, e);
      }
    }
    return actionRefs;
  }

  private static boolean shallExclude(String[] excludeElementsContaining, String fullPath) {
    return Arrays.stream(excludeElementsContaining).anyMatch(fullPath::contains);
  }
}
