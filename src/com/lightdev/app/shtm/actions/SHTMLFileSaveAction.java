package com.lightdev.app.shtm.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/** save a document */
  public class SHTMLFileSaveAction extends AbstractAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLFileSaveAction(SHTMLPanel panel) {
      super(SHTMLPanel.saveAction);
    this.panel = panel;
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      if(!this.panel.getDocumentPane().isNewDoc()) {
        FileSaver saver = new FileSaver(this.panel.getDocumentPane());
        saver.setName("FileSaver");
        saver.start();
      }
      else {
          panel.dynRes.getAction(SHTMLPanel.saveAsAction).actionPerformed(ae);
      }
      this.panel.updateActions();
    }

    /**
     * Helper class for being able to save a document in a separate thread.
     * Using a separate thread will not cause the application to block during
     * a lengthy save operation
     */
    class FileSaver extends Thread {
      DocumentPane dp;
      Component owner;
      FileSaver(DocumentPane dp) {
        setPriority(Thread.MIN_PRIORITY);
        this.dp = dp;
      }
      public void run() {
        SHTMLFileSaveAction.this.panel.doSave(this.dp);
      }
    }

    public void update() {
      boolean isEnabled = this.panel.getTabbedPaneForDocuments().getTabCount() > 0;
      boolean saveInProgress = false;
      boolean needsSaving = false;
      if(isEnabled) {
        saveInProgress = this.panel.getDocumentPane().saveInProgress();
        needsSaving = this.panel.getDocumentPane().needsSaving();
      }
      this.setEnabled(isEnabled && needsSaving && !saveInProgress);
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }