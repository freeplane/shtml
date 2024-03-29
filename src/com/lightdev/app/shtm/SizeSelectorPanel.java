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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS;

/**
 * Panel to show and manipulate a CSS size value
 *
 * <p>Added support for negative integers in stage 8.</p>
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
class SizeSelectorPanel extends JPanel implements AttributeComponent, ActionListener {
    private final Object attributeKey;
    private final Object htmlAttrKey;
    private final JSpinner valueSelector;
    private JComboBox unitSelector;
    private JLabel unitName;
    //private LengthValue lv;
    private int setValueCalls = 0;
    private int originalValue = 0;
    private String originalUnit;
    private boolean allowNegative = false;
    public static final String UNIT_PT = "pt";
    public static final String UNIT_PERCENT = "%";
    public static final String[] UNIT_VALUES = { UNIT_PT, UNIT_PERCENT };
    public static final int UNIT_TYPE_PT = 0;
    public static final int UNIT_TYPE_PERCENT = 1;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_LABEL = 1;
    public static final int TYPE_COMBO = 2;

    /**
     * construct a basic SizeSelectorPanel with a
     * JSpinner to select a value
     *
     * @param key  the attribute key this instance of SizeSelectionPanel
     *      represents
     * @param allowNegative  true, if negative values are to be allowed in the
     *      panel, false if not
     */
    public SizeSelectorPanel(final Object key, final Object htmlKey, final boolean allowNegative) {
        super(new FlowLayout());
        attributeKey = key;
        htmlAttrKey = htmlKey;
        valueSelector = new JSpinner(new SpinnerNumberModel());
        add(valueSelector);
        originalUnit = getUnit();
        this.allowNegative = allowNegative;
    }

    /**
     * construct a SizeSelectorPanel with a
     * JSpinner to select a value and either a JComboBox to select a given
     * unit for the selection value or a JLabel showing a fixed unit.
     *
     * @param key  the attribute key this instance of SizeSelectionPanel
     *      represents
     * @param allowNegative  true, if negative values are to be allowed in the
     *      panel, false if not
     * @param type  the type of unit indicator, one of TYPE_LABEL and
     *      TYPE_COMBO
     */
    public SizeSelectorPanel(final Object key, final Object htmlKey, final boolean allowNegative, final int type) {
        this(key, htmlKey, allowNegative);
        switch (type) {
            case TYPE_LABEL:
                unitName = new JLabel(UNIT_PT);
                add(unitName);
                break;
            case TYPE_COMBO:
                unitSelector = new JComboBox(UNIT_VALUES);
                unitSelector.addActionListener(this);
                add(unitSelector);
                break;
        }
        originalUnit = getUnit();
    }

    public void actionPerformed(final ActionEvent ae) {
        if (ae.getSource().equals(unitSelector)) {
            adjustMinMax(unitSelector.getSelectedItem().toString());
        }
    }

    public void setValue(final String val) {
        final float aVal = Util.getAbsoluteAttrVal(val);
        String unit = Util.getLastAttrUnit();
        adjustMinMax(unit);
        if (unitSelector != null) {
            unitSelector.setSelectedItem(unit);
        }
        else if (unitName != null) {
            unitName.setText(unit);
        }
        int newVal = (int) aVal;
        valueSelector.setValue(newVal);

        if (++setValueCalls < 2) {
            originalValue = newVal;
            originalUnit = unit;
        }
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
    public boolean setValue(final AttributeSet a) {
        boolean success = false;
        Object valObj = a.getAttribute(attributeKey);
        if (valObj != null) {
            setValue(valObj.toString());
            success = true;
        }
        else {
            if (htmlAttrKey != null) {
                valObj = a.getAttribute(htmlAttrKey);
                if (valObj != null) {
                    setValue(valObj.toString());
                    success = true;
                }
            }
        }
        return success;
    }

    /**
     * adjust the minimum and maximum values of the component
     * according to the unit
     */
    private void adjustMinMax(final String unit) {
        final SpinnerNumberModel model = (SpinnerNumberModel) valueSelector.getModel();
        int minVal = 0;
        if (allowNegative) {
            minVal = Integer.MIN_VALUE;
        }
        if (unit.equalsIgnoreCase(UNIT_PERCENT)) {
            model.setMinimum(minVal);
            model.setMaximum(100);
        }
        else {
            model.setMinimum(minVal);
            model.setMaximum(Integer.MAX_VALUE);
        }
    }

    /**
     * get the unit string of this SizeSelectorPanel
     *
     * @return the unit string (one of UNIT_PT and UNIT_PERCENT)
     */
    public String getUnit() {
        String unit = null;
        if (unitSelector != null) {
            unit = unitSelector.getSelectedItem().toString();
        }
        else if (unitName != null) {
            unit = unitName.getText();
        }
        else {
            unit = UNIT_PT;
        }
        if (unit.equalsIgnoreCase(UNIT_PT)) {
            unit = "";
        }
        return unit;
    }

    public boolean valueChanged() {
        final Integer value = (Integer) valueSelector.getValue();
        return ((value != originalValue) || (getUnit() != originalUnit));
    }

    public String getAttr() {
        final Integer value = (Integer) valueSelector.getValue();
        final String unit = getUnit();
        return value.toString() + unit;
    }

    public Integer getIntValue() {
        return (Integer) valueSelector.getValue();
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
        final SimpleAttributeSet a = new SimpleAttributeSet();
        final Integer value = getIntValue();
        final String unit = getUnit();
        if (valueChanged()) {
            if (attributeKey instanceof CSS.Attribute) {
                //a.addAttribute(attributeKey, value.toString() + unit);
                Util.styleSheet().addCSSAttribute(a, (CSS.Attribute) attributeKey, value.toString() + unit);
            }
            else {
                a.addAttribute(attributeKey, value.toString());
                if (htmlAttrKey != null) {
                    a.addAttribute(htmlAttrKey, value.toString());
                }
            }
        }
        return a;
    }

    public AttributeSet getValue(final boolean includeUnchanged) {
        if (includeUnchanged) {
            final SimpleAttributeSet a = new SimpleAttributeSet();
            final Integer value = getIntValue();
            final String unit = getUnit();
            if (attributeKey instanceof CSS.Attribute) {
                Util.styleSheet().addCSSAttribute(a, (CSS.Attribute) attributeKey, value.toString() + unit);
            }
            else {
                a.addAttribute(attributeKey, value.toString());
                if (htmlAttrKey != null) {
                    a.addAttribute(htmlAttrKey, value.toString());
                }
            }
            return a;
        }
        else {
            return getValue();
        }
    }

    public JSpinner getValueSelector() {
        return valueSelector;
    }
}
