/*
 * Created on 20.08.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
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

public class InsertImageAction extends AbstractAction
        implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public InsertImageAction(SHTMLPanel panel) {
      super();
    this.panel = panel;
      putValue(Action.NAME, SHTMLPanel.insertImageAction);
      getProperties();
      /*putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_A, KeyEvent.CTRL_MASK));*/
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      ImageDialog dlg = new ImageDialog(parent,
                                       DynamicResource.getResourceString(SHTMLPanel.resources,
                                       "imageDialogTitle"),
                                       this.panel.getDocumentPane().getImageDir());
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();

      /** if the user made a selection, apply it to the document */
      if(dlg.getResult() == DialogShell.RESULT_OK) {
        //System.out.println("imageHTML=\r\n\r\n" + dlg.getImageHTML());
        try {
          this.panel.getSHTMLDocument().insertBeforeStart(
              this.panel.getSHTMLDocument().getCharacterElement(this.panel.getEditor().getSelectionEnd()),
              dlg.getImageHTML());
        }
        catch(Exception e) {
          Util.errMsg(null, e.getMessage(), e);
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