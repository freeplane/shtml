package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.SHTMLPanel.FontFamilyPicker;

/**
   * change a font family setting
   */
  public class FontFamilyAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public FontFamilyAction(SHTMLPanel panel) {
      super(SHTMLPanel.fontFamilyAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      FontFamilyPicker ffp = ((FontFamilyPicker) ae.getSource());
      if(!ffp.ignore()) {
        this.panel.getEditor().applyAttributes(ffp.getValue(), false);
      }
      this.panel.updateActions();
    }

    public void update() {
      if(this.panel.getTabbedPaneForDocuments().getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }