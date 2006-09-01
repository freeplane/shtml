package com.lightdev.app.shtm.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotRedoException;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * RedoAction for the edit menu
   */
  public class RedoAction extends AbstractAction implements SHTMLAction {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public RedoAction(SHTMLPanel panel) {
      super(SHTMLPanel.redoAction);
    this.panel = panel;
      setEnabled(false);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
      if(this.panel.getCurrentDocumentPane().getSelectedTab() != DocumentPane.VIEW_TAB_LAYOUT){
         return;
      }
      try {
        this.panel.getUndo().redo();
      }
      catch(CannotRedoException ex) {
        Util.errMsg((Component) e.getSource(),
	      DynamicResource.getResourceString(SHTMLPanel.resources, "unableToRedoError") + ex, ex);
      }
      this.panel.updateActions();
    }

    public void update() {
        if(this.panel.isHtmlEditorActive()){
            this.setEnabled(false);
            return;
        }
      setEnabled(this.panel.getUndo().canRedo());
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }