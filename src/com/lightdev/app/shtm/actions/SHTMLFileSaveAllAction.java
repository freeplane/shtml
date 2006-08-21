/*
 * Created on 20.08.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

public class SHTMLFileSaveAllAction extends AbstractAction
        implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public SHTMLFileSaveAllAction(SHTMLPanel panel) {
      super(SHTMLPanel.saveAllAction);
    this.panel = panel;
      getProperties();
      /*putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_S, KeyEvent.CTRL_MASK));*/
    }

    public void actionPerformed(ActionEvent ae) {
      int count = this.panel.getTabbedPaneForDocuments().getTabCount();
      for(int i = 0; i < count; i++) {
        this.panel.getTabbedPaneForDocuments().setSelectedIndex(i);
        this.panel.setDocumentPane((DocumentPane) this.panel.getTabbedPaneForDocuments().getSelectedComponent());
        if(this.panel.getDocumentPane().needsSaving()) {
            panel.dynRes.getAction(SHTMLPanel.saveAction).actionPerformed(ae);
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