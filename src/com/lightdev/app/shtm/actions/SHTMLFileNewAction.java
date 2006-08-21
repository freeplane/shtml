package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/** create a new empty document and show it */
  public class SHTMLFileNewAction extends AbstractAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLFileNewAction(SHTMLPanel panel) {
      super(SHTMLPanel.newAction);
    this.panel = panel;
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_N, KeyEvent.CTRL_MASK));
    }

    /** create a new empty document and show it */
    public void actionPerformed(ActionEvent ae) {
      this.panel.createNewDocumentPane();   // create a new empty document
      this.panel.getTabbedPaneForDocuments().setSelectedComponent(                   // add the document to the
            this.panel.getTabbedPaneForDocuments().add(this.panel.getDocumentPane().getDocumentName(), this.panel.getDocumentPane()));   // tabbed pane for display

      this.panel.registerDocument();

      this.panel.updateActions();
    }

    public void update() {
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }