package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * set the title of the currently active document
   */
  public class DocumentTitleAction extends AbstractAction
                                implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public DocumentTitleAction(SHTMLPanel panel) {
      super(SHTMLPanel.documentTitleAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      String newTitle;
      String currentTitle = this.panel.getSHTMLDocument().getDocumentTitle();
      if(currentTitle != null) {
        newTitle = currentTitle;
      }
      else {
        newTitle = "";
      }
      newTitle = Util.nameInput(JOptionPane.getFrameForComponent(this.panel), newTitle, "docTitleTitle", "docTitleQuery");
      if(newTitle != null && newTitle.length() > 0) {
        this.panel.getSHTMLDocument().setDocumentTitle(newTitle);
      }
    }

    public void update() {
        if(this.panel.isHtmlEditorActive()){
            this.setEnabled(false);
            return;
        }
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