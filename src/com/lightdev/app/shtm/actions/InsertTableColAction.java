package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * insert a new table column
   */
  public class InsertTableColAction extends AbstractAction
                                implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public InsertTableColAction(SHTMLPanel panel) {
      super(SHTMLPanel.insertTableColAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      this.panel.getEditor().insertTableColumn();
    }

    public void update() {
        if(this.panel.isHtmlEditorActive()){
            this.setEnabled(false);
            return;
        }
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