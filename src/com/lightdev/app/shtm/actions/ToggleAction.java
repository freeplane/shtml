package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS;

import com.lightdev.app.shtm.AttributeComponent;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * action to toggle an attribute
   */
  public class ToggleAction extends AbstractAction implements SHTMLAction,
        AttributeComponent
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    /** the attribute this action represents values for */
    Object attributeKey;

    /** the value for the attribute being selected */
    private Object selectedValue = null;

    /** the value for the attribute not being selected */
    private Object unselectedValue = null;

    /**
     * construct a ToggleFontAction
     *
     * @param name  the name and command for this action
     * @param key the attribute this action represents values for
     * @param sVal the value for the attribute being selected
     * @param uVal the value for the attribute not being selected
     * @param panel TODO
     */
    public ToggleAction(SHTMLPanel panel, String name, Object key, Object sVal, Object uVal)
    {
      super(name);
    this.panel = panel;
      putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_UNSELECTED);
      attributeKey = key;
      selectedValue = sVal;
      unselectedValue = uVal;
      getProperties();
    }

    /**
     * construct a ToggleFontAction
     *
     * @param name  the name and command for this action
     * @param key the attribute this action represents values for
     * @param sVal the value for the attribute being selected
     * @param panel TODO
     */
    public ToggleAction(SHTMLPanel panel, String name, Object key, Object sVal)
    {
      this(panel, name, key, sVal, null);
      //System.out.println("ToggleAction constructor sVal=" + sVal);
    }

    /**
     * do the format change for the associated attribute
     *
     * <p>This reverses the current setting for the associated attribute</p>
     *
     * @param  e  the ActionEvent describing the cause for this action
     */
    public void actionPerformed(ActionEvent e) {
      //System.out.println("ToggleAction getValue=" + getValue() + "selectedValue=" + selectedValue);
      this.panel.getEditor().applyAttributes(getValue(), (unselectedValue == null));
      if(unselectedValue != null) {
        if(getValue(SHTMLPanel.ACTION_SELECTED_KEY).toString().equals(SHTMLPanel.ACTION_UNSELECTED))
        {
          putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_SELECTED);
        }
        else {
          putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_SELECTED);
        }
      }
      else {
        putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_SELECTED);
      }
      this.panel.updateActions();
    }

    /**
     * get the attribute this action represents values for
     *
     * @return the attribute this action represents values for
     */
    public Object getAttributeKey() {
      return attributeKey;
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
    public boolean setValue(AttributeSet a) {
      boolean success = false;
      if(a.isDefined(attributeKey)) {
        Object value = a.getAttribute(attributeKey);
        if(value.toString().equalsIgnoreCase(selectedValue.toString())) {
          putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_SELECTED);
        }
        else {
          putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_UNSELECTED);
        }
        success = true;
      }
      else {
        putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_UNSELECTED);
      }
      return success;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      //System.out.println("ToggleAction getValue getValue(FrmMain.ACTION_SELECTED_KEY)=" + getValue(FrmMain.ACTION_SELECTED_KEY));
      SimpleAttributeSet set = new SimpleAttributeSet();
      if(unselectedValue != null) {
        if(getValue(SHTMLPanel.ACTION_SELECTED_KEY).toString().equals(
            SHTMLPanel.ACTION_SELECTED))
        {
          if(unselectedValue != null) {
            Util.styleSheet().addCSSAttribute(set,
                (CSS.Attribute) getAttributeKey(), unselectedValue.toString());
          }
        }
        else {
          Util.styleSheet().addCSSAttribute(set,
              (CSS.Attribute) getAttributeKey(), selectedValue.toString());
        }
      }
      else {
        Util.styleSheet().addCSSAttribute(set,
            (CSS.Attribute) getAttributeKey(), selectedValue.toString());
      }
      return set;
    }

    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }

    /** update the action's state */
    public void update() {
      if(this.panel.getTabbedPaneForDocuments().getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    /** get image, etc. from resource */
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }