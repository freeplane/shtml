package com.lightdev.app.shtm.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.ParaStyleDialog;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * action to change the paragraph style
   */
  public class EditNamedStyleAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public EditNamedStyleAction(SHTMLPanel panel) {
      super(SHTMLPanel.editNamedStyleAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      ParaStyleDialog dlg = new ParaStyleDialog(parent,
                                       DynamicResource.getResourceString(SHTMLPanel.resources,
                                       "namedStyleDialogTitle"),
                                       this.panel.getSHTMLDocument());
      Util.center(parent, dlg);
      dlg.setModal(true);
      //SHTMLDocument doc = (SHTMLDocument) dp.getDocument();
      dlg.setValue(SHTMLPanel.getMaxAttributes(this.panel.getSHTMLDocument().getParagraphElement(this.panel.getEditor().getCaretPosition()), this.panel.getSHTMLDocument().getStyleSheet()));
      //dlg.setValue(getMaxAttributes(editor, null));
      dlg.show();
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