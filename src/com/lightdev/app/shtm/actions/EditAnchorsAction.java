package com.lightdev.app.shtm.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * action to edit anchors inside a document
   */
  public class EditAnchorsAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public EditAnchorsAction(SHTMLPanel panel) {
      super(SHTMLPanel.editAnchorsAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      AnchorDialog dlg = new AnchorDialog(
          parent,
          DynamicResource.getResourceString(SHTMLPanel.resources, "anchorDialogTitle"),
          this.panel.getSHTMLDocument());
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();
      this.panel.updateActions();
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