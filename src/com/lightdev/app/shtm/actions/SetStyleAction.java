package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.AttributeSet;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.StyleSelector;

/**
   * action to set the style
   */
  public class SetStyleAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    private boolean ignoreActions = false;

    public SetStyleAction(SHTMLPanel panel) {
      super(SHTMLPanel.setStyleAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      if(!ignoreActions) {
        StyleSelector styleSelector = (StyleSelector) ae.getSource();
        AttributeSet a = styleSelector.getValue();
        if(a != null) {
          //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
          //hd.listAttributes(a, 2);
          this.panel.getEditor().applyAttributes(a, true);
        }
        this.panel.updateActions();
      }
    }

    public void setIgnoreActions(boolean ignore) {
      ignoreActions = ignore;
    }

    public void update() {
        if(this.panel.isHtmlEditorActive()){
            this.setEnabled(false);
            return;
        }
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