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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS;

/**
 * a panel to display and change a color setting
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
class ColorPanel extends JPanel implements ActionListener {
    /** the component showing the chosen color */
    final JTextField colorDisplay = new JTextField();
    /** value to compare for determining changes */
    private Color originalColor;
    /** indicates if setValue is called initially */
    private int setValCount = 0;
    private final JColorChooser colorChooserPane = new JColorChooser();

    /**
    * construct a color panel
    *
    * @param title  the title of the color panel
    */
    public ColorPanel(final String title) {
        super(new BorderLayout(5, 5));

        Dimension dim = new Dimension(20, 15);
        colorDisplay.setMinimumSize(dim);
        colorDisplay.setMaximumSize(dim);
        colorDisplay.setPreferredSize(dim);
        colorDisplay.setEditable(false);

        final JButton browseButton = new JButton();
        browseButton.setText("...");
        dim = new Dimension(20, 15);
        browseButton.setMinimumSize(dim);
        browseButton.setMaximumSize(dim);
        browseButton.setPreferredSize(dim);
        browseButton.addActionListener(this);

        final JPanel eastPanel = new JPanel(new FlowLayout());
        eastPanel.add(colorDisplay);
        eastPanel.add(browseButton);

        if ((title != null) && (!title.isEmpty())) {
            final JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(UIManager.getFont("TextField.font"));
            add(titleLabel, BorderLayout.WEST);
            add(eastPanel, BorderLayout.EAST);
        }
        else {
            add(eastPanel, BorderLayout.WEST);
        }
    }

    /**
     * get the selected color
     *
     * @return the selected color
     */
    public Color getColor() {
        return colorDisplay.getBackground();
    }

    /**
     * set the selected color
     *
     * @param color the selected color
     */
    private void setColor(final Color color) {
        colorDisplay.setBackground(color);
        if (++setValCount < 2) {
            originalColor = color;
        }
        fireColorChanged();
    }

    /**
     * open a color chooser when a 'Browse' button
     * is clicked and change the associated color
     * display accordingly, when another color
     * is selected from the color chooser
     */
    public void actionPerformed(final ActionEvent e) {
        final Color color = showColorChooserDialog();
        if (color != null) {
            setValCount++;
            setColor(color);
        }
    }

    private Color showColorChooserDialog() {
        // the listener for OK button of the Color Choose Dialog:
        class ColorTracker implements ActionListener {
            final JColorChooser chooser;
            Color color;

            public ColorTracker(final JColorChooser c) {
                chooser = c;
            }

            public void actionPerformed(final ActionEvent e) {
                color = chooser.getColor();
            }
        }
        colorChooserPane.setColor(colorDisplay.getBackground()); // setting up the current text color
        final ColorTracker ok = new ColorTracker(colorChooserPane);
        final JDialog dialog = JColorChooser.createDialog(this, "Select Color", true, colorChooserPane, ok, null);
        dialog.setVisible(true); // blocks until user brings dialog down...
        dialog.dispose();
        return ok.color;
    }

    /**
    * set the value of this <code>AttributeComponent</code>
    *
    * @param a  the set of attributes possibly having an
    *          attribute this component can display
    *
    * @return true, if the set of attributes had a matching attribute,
    *            false if not
    */
    public boolean setValue(final AttributeSet a, CSS.Attribute attributeKey) {
        Color newSelection = null;
        if (attributeKey == CSS.Attribute.COLOR) {
            newSelection = Util.styleSheet().getForeground(a);
        }
        if (attributeKey == CSS.Attribute.BACKGROUND_COLOR) {
            newSelection = Util.styleSheet().getBackground(a);
            if (newSelection == null) {
                newSelection = SystemColor.text;
            }
        }
        if (newSelection != null) {
            setColor(newSelection);
            return true;
        }
        return false;
    }

    public void setValue(final String value) {
        try {
            setColor(new Color(Integer.parseInt(value.substring(1).toUpperCase(), 16)));
        }
        catch (final Exception e) {
            try {
                setColor(Color.getColor(value));
            }
            catch (final Exception e2) {
                Util.errMsg(null, null, e2);
            }
        }
    }

    public String getAttr() {
        return "#" + Integer.toHexString(getColor().getRGB()).substring(2);
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue(CSS.Attribute attributeKey) {
        final SimpleAttributeSet set = new SimpleAttributeSet();
        final Color value = getColor();
        if (value != originalColor) {
            final String color = "#" + Integer.toHexString(value.getRGB()).substring(2);
            Util.styleSheet().addCSSAttribute(set, attributeKey, color);
        }
        return set;
    }

    public AttributeSet getValue(CSS.Attribute attributeKey, final boolean includeUnchanged) {
        if (includeUnchanged) {
            final SimpleAttributeSet set = new SimpleAttributeSet();
            final Color value = getColor();
            final String color = "#" + Integer.toHexString(value.getRGB()).substring(2);
            try {
                Util.styleSheet().addCSSAttribute(set, attributeKey, color);
            }
            catch (final Exception e) {
                set.addAttribute(attributeKey, color);
            }

            return set;
        }
        else {
            return getValue(attributeKey);
        }
    }


    /* -------------- event listener implementation start ----------- */
    /** the listeners for ColorPanelEvents */
    private final Vector listeners = new Vector(0);

    /**
     * add an event listener.
     *
     * @param  listener  the event listener to add
     */
    public void addColorPanelListener(final ColorPanelListener listener) {
        listeners.addElement(listener);
    }

    /**
     * remove an event listener.
     *
     * @param  listener  the event listener to remove
     */
    public void removeColorPanelListener(final ColorPanelListener listener) {
        listeners.removeElement(listener);
    }

    /** fire a color changed event to all registered listeners */
    void fireColorChanged() {
        final Enumeration listenerList = listeners.elements();
        while (listenerList.hasMoreElements()) {
            ((ColorPanelListener) listenerList.nextElement()).colorChanged(new ColorPanelEvent(this));
        }
    }

    /** the event object definition for ColorPanels */
    static class ColorPanelEvent extends EventObject {
        public ColorPanelEvent(final Object source) {
            super(source);
        }
    }

    /** the event listener definition for ColorPanels */
    interface ColorPanelListener extends EventListener {
        void colorChanged(ColorPanelEvent e);
    }
    /* -------------- event listener implementation end ----------- */
}
