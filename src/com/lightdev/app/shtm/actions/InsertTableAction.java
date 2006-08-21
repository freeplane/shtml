package com.lightdev.app.shtm.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * insert a new table
   */
  public class InsertTableAction extends AbstractAction
				implements SHTMLAction
  {

    /**
     * 
     */
    private final SHTMLPanel panel;

    public InsertTableAction(SHTMLPanel panel) {
      super(SHTMLPanel.insertTableAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      Object input = Util.nameInput(parent, "3", "insertTableTitle","insertTableMsg");
      if(input != null) {
        int choice = Integer.parseInt(input.toString());
        if(choice > 0) {
          this.panel.getEditor().insertTable(choice);
        }
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