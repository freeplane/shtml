package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.SHTMLPanel.FontSizePicker;

/**
   * change a font size setting
   */
  public class FontSizeAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public FontSizeAction(SHTMLPanel panel) {
      super(SHTMLPanel.fontSizeAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      FontSizePicker fsp = ((FontSizePicker) ae.getSource());
      if(!fsp.ignore()) {
        this.panel.getEditor().applyAttributes(fsp.getValue(), false);
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