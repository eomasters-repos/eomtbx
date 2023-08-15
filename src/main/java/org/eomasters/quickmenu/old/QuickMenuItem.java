package org.eomasters.quickmenu.old;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.EventListenerList;

public class QuickMenuItem {
  private final String displayName;
  private final String path;
  private long clicks;
  EventListenerList listenerList = new EventListenerList();

  public QuickMenuItem(String path, String displayName) {
    this.displayName = displayName;
    this.path = path;
    clicks = 0;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPath() {
    return path;
  }

  public void incrementClicks() {
    clicks++;
    PropertyChangeEvent clicksEvent = new PropertyChangeEvent(this, "clicks", clicks - 1, clicks);
    listenerList.getListeners(PropertyChangeListener.class)[0].propertyChange(clicksEvent);
  }

  public long getClicks() {
    return clicks;
  }

  void setClicks(long clicks) {
    this.clicks = clicks;
  }

  public void addClicksChangeListener(PropertyChangeListener listener) {
    listenerList.add(PropertyChangeListener.class, listener);
  }

  public void removeClicksChangeListener(PropertyChangeListener listener) {
    listenerList.remove(PropertyChangeListener.class, listener);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    QuickMenuItem that = (QuickMenuItem) o;
    return Objects.equals(displayName, that.displayName) && Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, path);
  }

  public JMenuItem getJMenuItem() {
    return new JMenuItem(getDisplayName());
  }
}
