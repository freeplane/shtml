/*
 * Created on 8 Jul 2023
 *
 * author dimitry
 */
package com.lightdev.app.shtm;

import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;
import javax.swing.text.html.CSS.Attribute;

class ColorAttributePanel implements AttributeComponent {
    private final CSS.Attribute attributeName;
    private final ColorPanel panel;



    public ColorAttributePanel(ColorPanel panel, Attribute attributeName) {
        super();
        this.attributeName = attributeName;
        this.panel = panel;
    }

    @Override
    public boolean setValue(AttributeSet a) {
        return panel.setValue(a, attributeName);
    }

    @Override
    public AttributeSet getValue() {
        return panel.getValue(attributeName);
    }

    @Override
    public AttributeSet getValue(boolean includeUnchanged) {
        return panel.getValue(attributeName, includeUnchanged);
    }

}
