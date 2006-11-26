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
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;


import java.util.*;

/**
 * Component to select styles
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

class StyleSelector extends JComboBox
    implements AttributeComponent, ChangeListener
{

  /** the CSS attribute key this AttributeComponent object represents */
  private HTML.Attribute key;

  /** indicates whether or not to ignore change events */
  private boolean ignoreChanges = false;

  private String standardStyleName = Util.getResourceString(SHTMLPanelImpl.textResources, "standardStyleName");

  /**
   * construct a <code>StyleSelector</code>
   *
   * @param key  the attribute this component represents
   */
  public StyleSelector(HTML.Attribute key) {
    this.key = key;
  }

  /**
   * set the value of this combo box
   *
   * @param a  the set of attributes possibly having a
   *          font size attribute this pick list could display
   *
   * @return true, if the set of attributes had a matching attribute,
   *            false if not
   */
  public boolean setValue(AttributeSet a) {
    boolean success = false;
    Object attr = a.getAttribute(key);
    if(attr != null) {
      setSelectedItem(attr.toString());
      success = true;
    }
    else {
      setSelectedItem(standardStyleName);
    }
    return success;
  }

  /**
   * get the value of this <code>AttributeComponent</code>
   *
   * @return the value selected from this component
   */
  public AttributeSet getValue() {
    SimpleAttributeSet set = new SimpleAttributeSet();
    set.addAttribute(key, getSelectedItem());
    return set;
  }

  public AttributeSet getValue(boolean includeUnchanged) {
    if(includeUnchanged) {
      SimpleAttributeSet set = new SimpleAttributeSet();
      set.addAttribute(key, getSelectedItem());
      return set;
    }
    else {
      return getValue();
    }
  }

  /* --------------- ChangeListener implementation start --------------- */

  /**
   * this method listens and reacts to changes to either the JTabbedPane of FrmMain or
   * a given StyleSheet this component was registered with. Once either one changes
   * the list of styles of this componment is refreshed accordingly.
   */
  public void stateChanged(ChangeEvent e) {
    Object src = e.getSource();
    if(src instanceof JTabbedPane)
    {
      Component c = ((JTabbedPane) src).getSelectedComponent();
      if(c != null) {
        int activeTabNo = ((JTabbedPane) src).getSelectedIndex();
        DocumentPane dp = (DocumentPane) ((JTabbedPane) src).getComponentAt(activeTabNo);
        Vector styleNames = Util.getStyleNamesForTag(((SHTMLDocument) dp.getDocument()).getStyleSheet(), HTML.Tag.P.toString());
        styleNames.insertElementAt(standardStyleName, 0);
        setModel(new DefaultComboBoxModel(styleNames));
      }
    }
    else if(src instanceof StyleContext.NamedStyle) {
      Vector styleNames = Util.getStyleNamesForTag((AttributeSet) src, HTML.Tag.P.toString());
      styleNames.insertElementAt(standardStyleName, 0);
      setModel(new DefaultComboBoxModel(styleNames));
    }

  }

  /* --------------- ChangeListener implementation end ----------------- */
}
