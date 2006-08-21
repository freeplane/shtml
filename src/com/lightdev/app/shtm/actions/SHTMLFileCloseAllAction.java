package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * close all documents currently shown.
   *
   * <p>This action simply loops through all open documents and uses an instance
   * of SHTMLFileCloseAction to perform the actual closing on each of them.</p>
   */
  public class SHTMLFileCloseAllAction extends AbstractAction
	implements SHTMLAction
  {

    /**
     * 
     */
    private final SHTMLPanel panel;
    /** constructor 
     * @param panel TODO*/
    public SHTMLFileCloseAllAction(SHTMLPanel panel) {
      super(SHTMLPanel.closeAllAction);
    this.panel = panel;
      getProperties();
    }

    /** close all open documents */
    public void actionPerformed(ActionEvent ae) {
      SHTMLFileCloseAction a = (SHTMLFileCloseAction)panel.dynRes.getAction(SHTMLPanel.closeAction);
      for(int i = this.panel.getTabbedPaneForDocuments().getTabCount(); i > 0; i--) {
        //System.out.println("CloseAll, close tab no " + i);
        a.closeDocument(i-1, ae, false);
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