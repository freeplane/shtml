package com.lightdev.app.shtm.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotUndoException;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * UndoAction for the edit menu
   */
  public class UndoAction extends AbstractAction implements SHTMLAction {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public UndoAction(SHTMLPanel panel) {
      super(SHTMLPanel.undoAction);
    this.panel = panel;
      setEnabled(false);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
      if(this.panel.getCurrentDocumentPane().getSelectedTab() != DocumentPane.VIEW_TAB_LAYOUT){
         return;
      }
      try {
        this.panel.getUndo().undo();
      }
      catch(Exception ex) {
        Util.errMsg((Component) e.getSource(),
		  DynamicResource.getResourceString(SHTMLPanel.resources, "unableToUndoError") + ex, ex);
      }
      this.panel.updateActions();
    }

    public void update() {
        if(this.panel.isHtmlEditorActive()){
            this.setEnabled(false);
            return;
        }
      setEnabled(this.panel.getUndo().canUndo());
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }