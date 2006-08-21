package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * delete a table col
   */
  public class DeleteTableColAction extends AbstractAction
                                implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public DeleteTableColAction(SHTMLPanel panel) {
      super(SHTMLPanel.deleteTableColAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      this.panel.getEditor().deleteTableCol();
    }

    public void update() {
      if((this.panel.getTabbedPaneForDocuments().getTabCount() > 0) && (this.panel.getEditor().getCurTableCell() != null)) {
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