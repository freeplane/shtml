/*
* SimplyHTML, a word processor based on Java, HTML and CSS
* Copyright (C) 2002 Ulrich Hilger
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.lightdev.app.shtm;

import javax.swing.JDialog;
import java.awt.event.ActionListener;
import java.awt.AWTEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import javax.swing.text.AttributeSet;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.html.CSS;
import javax.swing.text.SimpleAttributeSet;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.text.html.HTML;
import java.util.Enumeration;
import javax.swing.JTabbedPane;
import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Dialog to manipulate HTML table attributes.
 *
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * 
 */
public class TableDialog extends DialogShell {

  /** collection of all components with table related attributes */
  Vector tableComponents = new Vector();

  /** collection of all components with cell related attributes */
  Vector cellComponents = new Vector();

  /** selector for cell range to apply cell attributes to */
  JComboBox cellRange;

  /**
   * constructor
   *
   * @param parent  the main frame having the ResourceBundle
   * @param title  the title for this dialog
   * @param a  the set of attributes to show and manipulate
   */
  public TableDialog(Frame parent, String title) {
    super(parent, title);

    // add to content pane of DialogShell
    Container contentPane = super.getContentPane();
    contentPane.add(buildTablePanel(), BorderLayout.NORTH);
    contentPane.add(buildCellPanel(), BorderLayout.CENTER);

    // cause optimal placement of all elements
    pack();
  }

  public void setTableAttributes(AttributeSet a) {
    setComponentAttributes(tableComponents, a);
  }

  public void setCellAttributes(AttributeSet a) {
    setComponentAttributes(cellComponents, a);
  }

  public void setComponentAttributes(Vector v, AttributeSet a) {
    Enumeration components = v.elements();
    AttributeComponent ac;
    while(components.hasMoreElements()) {
      ac = (AttributeComponent) components.nextElement();
      ac.setValue(a);
    }
  }

  /**
   * get the set of attributes resulting from the settings on
   * this TableDialog.
   *
   * @return the set of attributes set in this TableDialog
   */
  public AttributeSet getTableAttributes() {
    return getComponentAttributes(tableComponents);
  }

  public AttributeSet getCellAttributes() {
    //System.out.println("TableDialog getCellattributes=" + getComponentAttributes(cellComponents));
    return getComponentAttributes(cellComponents);
  }

  private AttributeSet getComponentAttributes(Vector v) {
    SimpleAttributeSet attributes = new SimpleAttributeSet();
    Enumeration components = v.elements();
    AttributeComponent ac;
    while(components.hasMoreElements()) {
      ac = (AttributeComponent) components.nextElement();
      //System.out.println(ac.getValue());
      attributes.addAttributes(ac.getValue());
    }
    return attributes;
  }

  /**
   * build the contents of the cell panel
   *
   * this is moved to a separate method to make the code more
   * legible.
   */
  private JPanel buildCellPanel() {

    // have a grid bag layout ready to use
    GridBagLayout g = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    // construct cell format panel
    JPanel cellPanel = new JPanel(new BorderLayout());
    cellPanel.setBorder(new TitledBorder(new EtchedBorder(
                  EtchedBorder.LOWERED),
                  DynamicResource.getResourceString(SHTMLPanel.resources, "cellPanelTitle")));

    // construct tabbed pane for various cell settings
    JTabbedPane tp = new JTabbedPane();
    tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    // add general panel to tabbed pane
    StylePanel sp = new StylePanel(StylePanel.TYPE_TABLE_CELL);
    cellComponents.add(sp);
    tp.add(DynamicResource.getResourceString(SHTMLPanel.resources, "cellGenTabLabel"), sp);

    // add padding panel to cell components and tabbed pane
    MarginPanel mp = new MarginPanel();
    cellComponents.add(mp);
    tp.add(DynamicResource.getResourceString(SHTMLPanel.resources, "cellMarginTabLabel"),mp);

    // construct border panel
    BorderPanel bPanel = new BorderPanel();

    // add border width panel and border color panel to cell components
    cellComponents.add(bPanel);

    // add border panel to tabbed pane
    tp.add(DynamicResource.getResourceString(SHTMLPanel.resources, "cellBorderTabLabel"), bPanel);

    // create cell range panel
    JPanel crPanel = new JPanel();
    String[] cellRangeSelection = new String[] {
      DynamicResource.getResourceString(SHTMLPanel.resources, "thisCellRangeLabel"),
      DynamicResource.getResourceString(SHTMLPanel.resources, "thisColRangeLabel"),
      DynamicResource.getResourceString(SHTMLPanel.resources, "thisRowRangeLabel"),
      DynamicResource.getResourceString(SHTMLPanel.resources, "allCellsRangeLabel")};
    crPanel.add(new JLabel(DynamicResource.getResourceString(SHTMLPanel.resources, "applyCellAttrLabel")));
    cellRange = new JComboBox(cellRangeSelection);
    crPanel.add(cellRange);

    // get the preferred size of the tabbed pane
    /*
    int lastTabIndex = tp.getTabCount() - 1;
    Rectangle tabRect = tp.getBoundsAt(lastTabIndex);
    int prefWidth = tabRect.x + tabRect.width + 30;
    tp.setPreferredSize(new Dimension(prefWidth, 300));
    */

    // add tabbed pane and range selector to cell panel
    cellPanel.add(tp, BorderLayout.CENTER);
    cellPanel.add(crPanel, BorderLayout.SOUTH);

    return cellPanel;
  }

  /**
   * get the range of cells to apply cell attributes to
   */
  public int getCellRange() {
    return cellRange.getSelectedIndex();
  }

  /**
   * build the contents of the table panel
   *
   * this is moved to a separate method to make the code more
   * legible.
   */
  private JPanel buildTablePanel() {

    // layout and constraints to use
    GridBagLayout g = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    // table panel
    JPanel tablePanel = new JPanel(g);
    tablePanel.setBorder(new TitledBorder(new EtchedBorder(
		  EtchedBorder.LOWERED),
		  DynamicResource.getResourceString(SHTMLPanel.resources, "tablePanelTitle")));

    // table width label
    JLabel lb = new JLabel(DynamicResource.getResourceString(SHTMLPanel.resources, "tableWidthLabel"));
    Util.addGridBagComponent(tablePanel, lb, g, c, 0, 0, GridBagConstraints.EAST);

    // table width combo box
    SizeSelectorPanel ssp = new SizeSelectorPanel(
        CSS.Attribute.WIDTH,
        HTML.Attribute.WIDTH,
        false,
        SizeSelectorPanel.TYPE_COMBO);
    Util.addGridBagComponent(tablePanel, ssp, g, c, 1, 0, GridBagConstraints.WEST);
    tableComponents.addElement(ssp);

    // table background color label
    lb = new JLabel(DynamicResource.getResourceString(SHTMLPanel.resources, "tableBgColLabel"));
    Util.addGridBagComponent(tablePanel, lb, g, c, 0, 1, GridBagConstraints.EAST);

    // table background color panel
    ColorPanel cp = new ColorPanel(null, Color.white,
			    CSS.Attribute.BACKGROUND_COLOR);
    Util.addGridBagComponent(tablePanel, cp, g, c, 1, 1, GridBagConstraints.WEST);
    tableComponents.addElement(cp);

    // table alignment label
    lb = new JLabel(DynamicResource.getResourceString(SHTMLPanel.resources, "alignLabel"));
    Util.addGridBagComponent(tablePanel, lb, g, c, 0, 2, GridBagConstraints.EAST);

    // table alignment combo box
    String[] items = new String[] {
        DynamicResource.getResourceString(SHTMLPanel.resources, "alignLeft"),
        DynamicResource.getResourceString(SHTMLPanel.resources, "alignCenter"),
        DynamicResource.getResourceString(SHTMLPanel.resources, "alignRight") };
    String[] names = new String[] {"left", "center", "right"};
    AttributeComboBox tAlgn = new AttributeComboBox(items, names,
					        CSS.Attribute.TEXT_ALIGN, HTML.Attribute.ALIGN);
    Util.addGridBagComponent(tablePanel, tAlgn, g, c, 1, 2, GridBagConstraints.WEST);
    tableComponents.addElement(tAlgn);

    return tablePanel;
  }
}
