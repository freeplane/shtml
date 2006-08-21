package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * force a garbage collection. This can be helpful to find out
   * whether or not objects are properly disposed.
   *
   * Without forcing a garbage collection, this would happen
   * at random intervals so although an object might be properly
   * disposed, it might still be around until the next GC.
   *
   * will be hidden from menu if not in development mode (DEV_MODE = false)
   */
  public class GCAction extends AbstractAction implements SHTMLAction {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public GCAction(SHTMLPanel panel) {
      super(SHTMLPanel.gcAction);
    this.panel = panel;
      getProperties();
    }
    public void actionPerformed(ActionEvent e) {
      System.gc();
      this.panel.updateActions();
    }
    public void update() {
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }