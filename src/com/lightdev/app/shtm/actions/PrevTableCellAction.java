package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Element;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * action to move to the previous cell in a table
   */
  public class PrevTableCellAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public PrevTableCellAction(SHTMLPanel panel) {
      super(SHTMLPanel.prevTableCellAction);
    this.panel = panel;
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      Element cell = this.panel.getEditor().getCurTableCell();
      if(cell != null) {
        this.panel.getEditor().goPrevCell(cell);
        this.panel.updateActions();
      }
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