/*-
 * ========================LICENSE_START=================================
 * EOMTBX PRO - EOMasters Toolbox PRO for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class MultiStateButton extends JPanel {

  private final DefaultListModel<State> model;
  private final JButton button;
  private int currentStateIndex;
  private final EventListenerList stateListenerList;

  public MultiStateButton(State initial, State... other) {
    this(createModel(initial, other));
  }

  private static DefaultListModel<State> createModel(State initial, State[] other) {
    DefaultListModel<State> listModel = new DefaultListModel<>();
    listModel.addElement(initial);
    listModel.addAll(List.of(other));
    return listModel;
  }


  public MultiStateButton(DefaultListModel<State> model) {
    this.model = model;
    if (model.isEmpty()) {
      throw new IllegalStateException("Model should not be empty");
    }
    stateListenerList = new EventListenerList();
    setLayout(new BorderLayout());
    button = new JButton();
    add(button, BorderLayout.CENTER);
    currentStateIndex = 0;
    updateButtonState(model.get(currentStateIndex));
    button.addActionListener(new ButtonClickedHandler(model));
    model.addListDataListener(new ModelChangeHandler(model));
  }

  public State getCurrentState() {
    return model.get(currentStateIndex);
  }

  private void updateButtonState(State state) {
    button.setText(state.getText());
    button.setIcon(state.getIcon());
    button.setToolTipText(state.getToolTipText());
  }

  public void addStateListener(StateListener listener) {
    stateListenerList.add(StateListener.class, listener);
  }

  public void removeStateListener(StateListener listener) {
    stateListenerList.remove(StateListener.class, listener);
  }

  public DefaultListModel<State> getMultiStateModel() {
    return model;
  }

  private class ButtonClickedHandler implements ActionListener {

    private final DefaultListModel<State> model;

    public ButtonClickedHandler(DefaultListModel<State> model) {
      this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      StateListener[] listeners = stateListenerList.getListeners(StateListener.class);
      State oldState = model.get(currentStateIndex);
      currentStateIndex = (currentStateIndex + 1) % model.size();
      State newState = model.get(currentStateIndex);
      updateButtonState(newState);
      for (StateListener listener : listeners) {
        listener.stateChanged(oldState, newState);
      }
    }
  }

  private class ModelChangeHandler implements ListDataListener {

    private final DefaultListModel<State> model;

    public ModelChangeHandler(DefaultListModel<State> model) {
      this.model = model;
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
      updateButtonState(model.get(currentStateIndex));
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
      updateButtonState(model.get(currentStateIndex));
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
      updateButtonState(model.get(currentStateIndex));
    }
  }
}
