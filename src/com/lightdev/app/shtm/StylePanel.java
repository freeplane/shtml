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
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.util.*;

/**
 * Panel to set general text style attributes such as indent or alignment.
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
 * @version stage 12, August 06, 2006
 */

public class StylePanel extends AttributePanel {

  public static final int TYPE_PARAGRAPH = 1;
  public static final int TYPE_TABLE_CELL = 2;

  private AttributeComboBox ctAlgn;
  private AttributeComboBox cAlgn;

  public StylePanel(int type) {
    super();

    JLabel lb;

    // have a grid bag layout ready to use
    GridBagLayout g = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();

    this.setLayout(g);

    if(type == TYPE_TABLE_CELL) {
      // background color label
      lb = new JLabel(DynamicResource.getResourceString(SHTMLPanel.resources, "tableBgColLabel"));
      Util.addGridBagComponent(this, lb, g, c, 0, 0, GridBagConstraints.EAST);

      // background color panel
      ColorPanel cp = new ColorPanel(null, Color.white,
                                     CSS.Attribute.BACKGROUND_COLOR);
      Util.addGridBagComponent(this, cp, g, c, 1, 0, GridBagConstraints.WEST);
    }

    // text alignment label
    lb = new JLabel(DynamicResource.getResourceString(SHTMLPanel.resources, "alignLabel"));
    Util.addGridBagComponent(this, lb, g, c, 0, 1, GridBagConstraints.EAST);

    // text align combo box
    String[] items = new String[] {
        DynamicResource.getResourceString(SHTMLPanel.resources, "alignLeft"),
        DynamicResource.getResourceString(SHTMLPanel.resources, "alignCenter"),
        DynamicResource.getResourceString(SHTMLPanel.resources, "alignRight") };
    String[] names = new String[] {"left", "center", "right"};
    ctAlgn = new AttributeComboBox(items, names,
        CSS.Attribute.TEXT_ALIGN, HTML.Attribute.ALIGN);
    Util.addGridBagComponent(this, ctAlgn, g, c, 1, 1, GridBagConstraints.WEST);

    // vertical alignment label
    lb = new JLabel(DynamicResource.getResourceString(SHTMLPanel.resources, "valignLabel"));
    Util.addGridBagComponent(this, lb, g, c, 0, 2, GridBagConstraints.EAST);

    // vertical alignment combo box
    items = new String[] {
      DynamicResource.getResourceString(SHTMLPanel.resources, "valignTop"),
      DynamicResource.getResourceString(SHTMLPanel.resources, "valignMiddle"),
      DynamicResource.getResourceString(SHTMLPanel.resources, "valignBottom"),
      DynamicResource.getResourceString(SHTMLPanel.resources, "valignBaseline")};
    names = new String[] {"top", "middle", "bottom", "baseline"};
    cAlgn = new AttributeComboBox(items, names,
        CSS.Attribute.VERTICAL_ALIGN, HTML.Attribute.VALIGN);
    Util.addGridBagComponent(this, cAlgn, g, c, 1, 2, GridBagConstraints.WEST);

    switch(type) {
      case TYPE_PARAGRAPH:
        addSizeSelector(DynamicResource.getResourceString(
            SHTMLPanel.resources, "textIndentLabel"), CSS.Attribute.TEXT_INDENT, null, true, g, c);
        break;
      case TYPE_TABLE_CELL:
        addSizeSelector(DynamicResource.getResourceString(
            SHTMLPanel.resources, "tableWidthLabel"), CSS.Attribute.WIDTH, HTML.Attribute.WIDTH, false, g, c);
        break;
    }
  }

  public void reset() {
    ctAlgn.reset();
    cAlgn.reset();
  }

  private void addSizeSelector(String text, CSS.Attribute ca, HTML.Attribute ha, boolean negVals,
                               GridBagLayout g, GridBagConstraints c)
  {
    // label
    JLabel lb = new JLabel(text);
    Util.addGridBagComponent(this, lb, g, c, 0, 3, GridBagConstraints.EAST);

    // selector
    SizeSelectorPanel ssp = new SizeSelectorPanel(
        ca,
        ha,
        negVals,
        SizeSelectorPanel.TYPE_COMBO);
    Util.addGridBagComponent(this, ssp, g, c, 1, 3, GridBagConstraints.WEST);
  }
}
