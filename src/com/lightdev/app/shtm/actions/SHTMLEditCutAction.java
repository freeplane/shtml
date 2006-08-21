package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/** just adds a normal name to the superclasse's action */
  public class SHTMLEditCutAction extends DefaultEditorKit.CutAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLEditCutAction(SHTMLPanel panel) {
      super();
    this.panel = panel;
      putValue(Action.NAME, SHTMLPanel.cutAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_X, KeyEvent.CTRL_MASK));
    }
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      this.panel.updateActions();
    }
    public void update() {
      if(this.panel.getTabbedPaneForDocuments().getTabCount() > 0) {
        setEnabled(true);
      }
      else {
        setEnabled(false);
      }
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }