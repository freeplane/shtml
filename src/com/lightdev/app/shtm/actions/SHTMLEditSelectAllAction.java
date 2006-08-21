/*
 * Created on 20.08.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

public class SHTMLEditSelectAllAction extends AbstractAction
        implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLEditSelectAllAction(SHTMLPanel panel) {
      super();
    this.panel = panel;
      putValue(Action.NAME, SHTMLPanel.selectAllAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_A, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      this.panel.getEditor().selectAll();
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