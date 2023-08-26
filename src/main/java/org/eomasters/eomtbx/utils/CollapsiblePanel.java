package org.eomasters.eomtbx.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

public class CollapsiblePanel extends JPanel {

  public static final String EXPAND_CHAR = "▶";
  public static final String COLLAPSE_CHAR = "▼";
  private final JLabel toggleLabel;
  private final JComponent contentPanel;
  private final JPanel titlePanel;
  private final JSeparator separator;

  public CollapsiblePanel(String title) {
    super(new BorderLayout(5, 5));
    setName("CollapsiblePanel." + title.replaceAll(" ", "_"));
    titlePanel = new JPanel(new MigLayout());

    MouseAdapter collapser = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        collapse(contentPanel.isVisible());
      }
    };

    JLabel titleLabel = new JLabel(title);
    titleLabel.addMouseListener(collapser);
    titlePanel.add(titleLabel);
    toggleLabel = new JLabel();
    toggleLabel.addMouseListener(collapser);
    titlePanel.add(toggleLabel);

    separator = new JSeparator();
    titlePanel.add(separator, "growx, wrap");

    add(titlePanel, BorderLayout.NORTH);

    contentPanel = new JPanel(new MigLayout("fill"));
    add(contentPanel, BorderLayout.CENTER);

    doLayout();

    collapse(true);
  }

  public void setContent(JComponent content) {
    this.contentPanel.removeAll();
    contentPanel.add(content, "grow");
    contentPanel.invalidate();
  }

  public void showSeparator(boolean show) {
    separator.setVisible(show);
    invalidate();
  }

  @Override
  public void revalidate() {
    super.revalidate();
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if (windowAncestor != null) {
      windowAncestor.pack();
      windowAncestor.setLocationRelativeTo(null);
    }
  }

  private void collapse(boolean collapse) {
    if (collapse) {
      toggleLabel.setText(EXPAND_CHAR);
      contentPanel.setVisible(false);
      setPreferredSize(new Dimension(getSize().width, getCollapsedHeight()));
    } else {
      toggleLabel.setText(COLLAPSE_CHAR);
      contentPanel.setVisible(true);
      setPreferredSize(null);
    }
    revalidate();
  }

  private int getCollapsedHeight() {
    return getInsets().top + titlePanel.getSize().height;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Collapsible Panel Example");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    CollapsiblePanel panel = new CollapsiblePanel("Title");
    panel.setContent(new JLabel("Content"));
    frame.getContentPane().add(panel);

    frame.pack();
    frame.setVisible(true);
  }
}
