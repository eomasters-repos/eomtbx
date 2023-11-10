/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.utils.FileIo;

/**
 * A collapsible panel. The component set by {@link #setContent(JComponent)} is collapsed when clicked on the title.
 */
public class CollapsiblePanel extends JPanel {

  private static final String EXPAND_CHAR = "▶";
  private static final String COLLAPSE_CHAR = "▼";
  private final JLabel toggleLabel;
  private final JComponent contentPanel;
  private final JPanel titlePanel;

  /**
   * Creates a new collapsible panel with the given title.
   *
   * @param title the title
   */
  public CollapsiblePanel(String title) {
    super(new BorderLayout(5, 5));
    setName("CollapsiblePanel." + title.replaceAll(" ", "_"));

    MouseAdapter collapser = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        setCollapsed(contentPanel.isVisible());
      }
    };

    titlePanel = new JPanel(new MigLayout("flowx", "[][][fill]"));
    titlePanel.addMouseListener(collapser);
    JLabel titleLabel = new JLabel(title);
    titleLabel.addMouseListener(collapser);
    titlePanel.add(titleLabel, "top, left");
    toggleLabel = new JLabel();
    toggleLabel.addMouseListener(collapser);
    titlePanel.add(toggleLabel, "top, left");

    add(titlePanel, BorderLayout.NORTH);

    contentPanel = new JPanel(new MigLayout("fill"));
    add(contentPanel, BorderLayout.CENTER);

    doLayout();

    setCollapsed(true);
  }

  /**
   * Creates a collapsible panel with a long text. And buttons to export the text to a file or the clipboard.
   *
   * @param title the title
   * @param text  the long text
   * @return the collapsible panel
   */
  public static CollapsiblePanel createLongTextPanel(String title, String text) {

    JTextArea textArea = new JTextArea(text);
    textArea.setColumns(70);
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    textArea.setTabSize(4);
    textArea.setEditable(false);
    textArea.setRows(20);
    JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel reportPreview = new JPanel(new MigLayout("fill"));
    reportPreview.add(scrollPane, "top, left, grow, wrap");
    CollapsiblePanel collapsiblePanel = new CollapsiblePanel(title);
    JButton exportBtn = createExportButton(collapsiblePanel, textArea);
    boolean headless = EomToolbox.isHeadless();
    reportPreview.add(exportBtn, "top, left" + (!headless ? ", split 2" : ""));
    if (!headless) {
      JButton clipboardBtn = new JButton("Copy to Clipboard");
      clipboardBtn.addActionListener(e -> {
        StringSelection contents = new StringSelection(textArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, contents);
      });
      reportPreview.add(clipboardBtn, "top, left");
    }

    collapsiblePanel.setContent(reportPreview);
    return collapsiblePanel;
  }

  /**
   * Sets the content of this panel which can be collapsed.
   *
   * @param content the content
   */
  public void setContent(JComponent content) {
    contentPanel.removeAll();
    contentPanel.add(content, "grow");
    contentPanel.invalidate();
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

  /**
   * Sets the collapsed state of this panel.
   *
   * @param collapse true to collapse, false to expand
   */
  public void setCollapsed(boolean collapse) {
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

  private static JButton createExportButton(JPanel contentPane, JTextArea textArea) {
    JButton exportBtn = new JButton("Export to File");
    exportBtn.addActionListener(e -> {
      FileIo exporter = new FileIo("Export Text to File");
      exporter.setParent(contentPane);
      exporter.setFileFilters(FileIo.createFileFilter("Text file", "txt"));
      exporter.save(outputStream -> outputStream.write(textArea.getText().getBytes(StandardCharsets.UTF_8)));
    });
    return exportBtn;
  }

  private int getCollapsedHeight() {
    return getInsets().top + titlePanel.getSize().height;
  }

  /**
   * Main method for testing.
   *
   * @param args the command line arguments
   */
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
