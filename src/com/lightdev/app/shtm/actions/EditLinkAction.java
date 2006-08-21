package com.lightdev.app.shtm.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.DialogShell;
import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * action to edit a link
   */
  public class EditLinkAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public EditLinkAction(SHTMLPanel panel) {
      super(SHTMLPanel.editLinkAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      LinkDialog dlg = new LinkDialog(parent,
                                       DynamicResource.getResourceString(SHTMLPanel.resources,
                                       "linkDialogTitle"),
                                       this.panel.getSHTMLDocument(),
                                       this.panel.getEditor().getSelectionStart(),
                                       this.panel.getEditor().getSelectionEnd(),
                                       this.panel.getDocumentPane().getImageDir()/*,
                                       renderMode*/);
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();
      if(dlg.getResult() == DialogShell.RESULT_OK) {
        // apply link here
        this.panel.getEditor().setLink(dlg.getLinkText(), dlg.getHref(), dlg.getStyleName(), dlg.getLinkImage(), dlg.getLinkImageSize());
      }
      this.panel.updateActions();
    }

    public void update() {
      if(this.panel.getTabbedPaneForDocuments().getTabCount() > 0) {
        if((this.panel.getEditor().getSelectionEnd() > this.panel.getEditor().getSelectionStart()) ||
           (Util.findLinkElementUp(this.panel.getSHTMLDocument().getCharacterElement(this.panel.getEditor().getSelectionStart())) != null)) {
          this.setEnabled(true);
        }
        else {
          this.setEnabled(false);
        }
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }