/*
 * Created on 20.08.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;

import com.lightdev.app.shtm.AttributeComponent;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLDocument;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

public class BoldAction extends StyledEditorKit.BoldAction implements SHTMLAction, AttributeComponent {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public BoldAction(SHTMLPanel panel) {
      //Action act = new StyledEditorKit.BoldAction();
      super();
    this.panel = panel;
      putValue(Action.NAME, SHTMLPanel.fontBoldAction);
      putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_UNSELECTED);
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
              KeyEvent.VK_B, KeyEvent.CTRL_MASK));
      SHTMLPanel.getActionProperties(this, SHTMLPanel.fontBoldAction);
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
      //editor.applyAttributes(getValue(), (unselectedValue == null));
      super.actionPerformed(e);
      //if(unselectedValue != null) {
      if(this.panel.getEditor() != null) {
        SHTMLDocument doc = (SHTMLDocument) this.panel.getEditor().getDocument();
        if (doc != null) {
          AttributeSet a = doc.getCharacterElement(this.panel.getEditor().getSelectionStart()).
              getAttributes();
          boolean isBold = StyleConstants.isBold(a);
          //if(a.isDefined(attributeKey)) {
          //Object value = a.getAttribute(attributeKey);
          if (isBold) {
            putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_SELECTED);
          }
          else {
            putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_UNSELECTED);
          }
        }
      }
      /*}
      else {
        putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
      }*/
      this.panel.updateActions();
    }

    public void getProperties() {
      SHTMLPanel.getActionProperties(this, SHTMLPanel.fontItalicAction);
    }
    public void update() {
      if(this.panel.getTabbedPaneForDocuments().getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
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
    public boolean setValue(AttributeSet a) {
      boolean success = false;
      boolean isBold = StyleConstants.isBold(a);
      if(a.isDefined(CSS.Attribute.FONT_WEIGHT)) {
        Object value = a.getAttribute(CSS.Attribute.FONT_WEIGHT);
        if (value.toString().equalsIgnoreCase(StyleConstants.Bold.toString())) {
          isBold = true;
        }
      }
      //System.out.println("ItalicAction setValue isItalic=" + isItalic);
      //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
      //hd.listAttributes(a, 6);
      //if(a.isDefined(attributeKey)) {
        //Object value = a.getAttribute(attributeKey);
        if(isBold) {
          putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_SELECTED);
        }
        else {
          putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_UNSELECTED);
        }
        success = true;
      //}
      //else {
      //  putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      //}
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
      //if(unselectedValue != null) {
      if (getValue(SHTMLPanel.ACTION_SELECTED_KEY).toString().equals(
          SHTMLPanel.ACTION_SELECTED)) {
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_WEIGHT,
                                          Util.CSS_ATTRIBUTE_NORMAL.toString());
      }
      else {
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_WEIGHT,
                                          StyleConstants.Bold.toString());
      }
      /*}
             else {
        Util.styleSheet().addCSSAttribute(set,
            (CSS.Attribute) getAttributeKey(), selectedValue.toString());
             }*/
      return set;
    }
    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }

  }