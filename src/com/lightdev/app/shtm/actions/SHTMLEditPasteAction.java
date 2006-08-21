package com.lightdev.app.shtm.actions;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/** just adds a normal name to the superclasse's action */
  public class SHTMLEditPasteAction extends DefaultEditorKit.PasteAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLEditPasteAction(SHTMLPanel panel) {
      super();
    this.panel = panel;
      putValue(Action.NAME, SHTMLPanel.pasteAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_V, KeyEvent.CTRL_MASK));
    }
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      this.panel.updateActions();
    }
    public void update() {
      try {
        Clipboard cb = this.panel.getToolkit().getSystemClipboard();
        Transferable data = cb.getContents(this);
        if(this.panel.getTabbedPaneForDocuments().getTabCount() > 0 && data != null) {
          setEnabled(true);
        }
        else {
          setEnabled(false);
        }
      }
      catch(Exception e) {
        setEnabled(false);
        Util.errMsg(null, null, e);
      }
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }