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

import java.awt.*;
import javax.swing.border.*;
import javax.swing.*;
import java.util.prefs.*;

/**
 * A panel to manage a pluggable panel layout, i.e. around
 * a center panel, other panels can be placed very similar
 * to BorderLayout. The difference of this class to BorderLayout is
 * that it creates JSplitPanes for each panel.
 *
 * <p>This uses JTabbedPanes for each of the panels surrounding
 * the center panel.</p>
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

public class SplitPanel extends JPanel {

  /**
   * Constructor
   */
  public SplitPanel() {
    super();
    setLayout(new BorderLayout());
    for(int i = 0; i < 4; i++) {
      panels[i] = new JSplitPane();
      panels[i].setBorder(new EmptyBorder(0, 0, 0, 0));
      panels[i].setContinuousLayout(true);
    }
    buildLayout();
    restorePrefs();
  }

  /**
   * set up the panels
   */
  private void buildLayout() {
    this.removeAll();
    panels[NORTH].setOrientation(JSplitPane.VERTICAL_SPLIT);
    panels[SOUTH].setOrientation(JSplitPane.VERTICAL_SPLIT);
    panels[WEST].setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    panels[EAST].setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    if(majorAxis == this.MAJOR_AXIS_VERTICAL) {
      //System.out.println("SplitPanel.buildLayout majorAxis = vertical");
      panels[SOUTH].setTopComponent(panels[NORTH]);
      panels[WEST].setRightComponent(panels[SOUTH]);
      panels[EAST].setLeftComponent(panels[WEST]);
      this.add(panels[EAST], BorderLayout.CENTER);
    }
    else {
      //System.out.println("SplitPanel.buildLayout majorAxis = horizontal");
      panels[SOUTH].setTopComponent(panels[NORTH]);
      panels[WEST].setRightComponent(panels[SOUTH]);
      panels[EAST].setLeftComponent(panels[WEST]);
      this.add(panels[SOUTH], BorderLayout.CENTER);
    }
  }

  /**
   * remove panels surrounding the center panel
   */
  public void removeAllOuterPanels() {
    JTabbedPane p;
    if(majorAxis == MAJOR_AXIS_VERTICAL) {
      p = (JTabbedPane) panels[NORTH].getTopComponent();
      p.removeAll();
      p.setVisible(false);
      p = (JTabbedPane) panels[WEST].getLeftComponent();
      p.removeAll();
      p.setVisible(false);
      p = (JTabbedPane) panels[SOUTH].getBottomComponent();
      p.removeAll();
      p.setVisible(false);
      p = (JTabbedPane) panels[EAST].getRightComponent();
      p.removeAll();
      p.setVisible(false);
    }
    else {
      // pending...
    }
  }

  /**
   * set the location of a given divider
   *
   * @param panel  the panel to set the location for
   * @param loc  the relative location of the divider (0, 0.1, ..., 0.9, 1)
   */
  public void setDivLoc(int panel, double loc) {
    //System.out.println("SplitPanel.setDivLoc panel=" + panel + ", loc=" + loc);
    panels[panel].setDividerLocation(loc);
  }

  /**
   * get the divider location for a given panel
   *
   * @param panel the panel to get the divider location for
   *
   * @return the divider location
   */
  public int getDivLoc(int panel) {
    //System.out.println("SplitPanel.getDivLoc panel=" + panel + ", loc=" + panels[panel].getDividerLocation());
    return panels[panel].getDividerLocation();
  }

  /**
   * save divider locations to preferences
   */
  public void savePrefs() {
    Preferences prefs = Preferences.userNodeForPackage( getClass() );
    for(int i = 0; i < 4; i++) {
      prefs.putInt("divLoc" + i, panels[i].getDividerLocation());
      //System.out.println("SplitPanel.savePrefs divLoc" + i + "=" + panels[i].getDividerLocation());
    }
  }

  /**
   * restore divider locations from preferences
   */
  public void restorePrefs() {
    Preferences prefs = Preferences.userNodeForPackage( getClass() );
    for(int i = 0; i < 4; i++) {
      panels[i].setDividerLocation(prefs.getInt("divLoc" + i, 300));
      //System.out.println("SplitPanel.restorePrefs divLoc" + i + "=" + prefs.getInt("divLoc" + i, 300));
    }
  }

  /**
   * get the plug-in container for a given panel
   *
   * @param location  the location of the desired container (SplitPanel.NORTH, SOUTH, etc.)
   *
   * @return the plug-in container
   */
  public JTabbedPane getPanel(int location) {
    JTabbedPane c = null;
    switch(location) {
      case NORTH:
        c = (JTabbedPane) panels[NORTH].getTopComponent();
        break;
      case EAST:
        c = (JTabbedPane) panels[EAST].getRightComponent();
        break;
      case SOUTH:
        c = (JTabbedPane) panels[SOUTH].getBottomComponent();
        break;
      case WEST:
        c = (JTabbedPane) panels[WEST].getLeftComponent();
        break;
    }
    return c;
  }

  /**
   * show/hide dividers according to visibility of
   * their associated plug-in containers
   */
  public void adjustDividerSizes() {
    JTabbedPane tp;
    JSplitPane sp;

    sp = panels[NORTH];
    if(sp.getTopComponent().isVisible()) {
      //System.out.println("north panel top is visible, showing divider");
      sp.setDividerSize(2);
    }
    else {
      sp.setDividerSize(0);
    }

    sp = panels[WEST];
    if(sp.getLeftComponent().isVisible()) {
      //System.out.println("west panel left is visible, showing divider");
      sp.setDividerSize(2);
    }
    else {
      sp.setDividerSize(0);
    }

    sp = panels[SOUTH];
    if(sp.getBottomComponent().isVisible()) {
      //System.out.println("south panel bottom is visible, showing divider");
      sp.setDividerSize(2);
    }
    else {
      sp.setDividerSize(0);
    }

    sp = panels[EAST];
    if(sp.getRightComponent().isVisible()) {
      //System.out.println("south panel right is visible, showing divider");
      sp.setDividerSize(2);
    }
    else {
      sp.setDividerSize(0);
    }

  }

  /**
   * add a plug-in container to this SplitPanel ata given location
   *
   * @param c  the plug-in container to add
   * @param location  the location to add to (SplitPanel.NORTH, SOUTH, etc.)
   */
  public void addComponent(JTabbedPane c, int location) {
    if(majorAxis == this.MAJOR_AXIS_VERTICAL) {
      //System.out.println("SplitPanel.addComponent majorAxis = vertical");
      switch(location) {
        case CENTER:
          //System.out.println("SplitPanel.addComponent CENTER");
          panels[NORTH].remove(panels[NORTH].getBottomComponent());
          panels[NORTH].setBottomComponent(c);
          break;
        case NORTH:
          //System.out.println("SplitPanel.addComponent NORTH");
          panels[NORTH].remove(panels[NORTH].getTopComponent());
          panels[NORTH].setTopComponent(c);
          break;
        case WEST:
          //System.out.println("SplitPanel.addComponent WEST");
          panels[WEST].remove(panels[WEST].getLeftComponent());
          panels[WEST].setLeftComponent(c);
          break;
        case SOUTH:
          //System.out.println("SplitPanel.addComponent SOUTH");
          panels[SOUTH].remove(panels[SOUTH].getBottomComponent());
          panels[SOUTH].setBottomComponent(c);
          break;
        case EAST:
          //System.out.println("SplitPanel.addComponent EAST");
          panels[EAST].remove(panels[EAST].getRightComponent());
          panels[EAST].setRightComponent(c);
          break;
      }
    }
    else {
      // pending...
      switch(location) {
        case CENTER:
          break;
        case NORTH:
          break;
        case WEST:
          break;
        case SOUTH:
          break;
        case EAST:
          break;
      }
    }
  }

  /* --------------- class fields start --------------- */

  /** constant for the major axis being the horizontal one */
  public static final int MAJOR_AXIS_HORIZONTAL = 1;

  /** constant for the major axis being the vertical one */
  public static final int MAJOR_AXIS_VERTICAL = 2;

  /** constant for the north plug-in container of this SplitPanel */
  public static final int NORTH = 0;

  /** constant for the east plug-in container of this SplitPanel */
  public static final int EAST = 1;

  /** constant for the south plug-in container of this SplitPanel */
  public static final int SOUTH = 2;

  /** constant for the west plug-in container of this SplitPanel */
  public static final int WEST = 3;

  /** constant for the center panel of this SplitPanel */
  public static final int CENTER = 4;

  /** the outer panels of this SplitPanel */
  private JSplitPane[] panels = new JSplitPane[4];

  /** current setting for major axis of this SplitPanel */
  private int majorAxis = this.MAJOR_AXIS_VERTICAL;

  /* ------ class fields end ------------------ */
}
