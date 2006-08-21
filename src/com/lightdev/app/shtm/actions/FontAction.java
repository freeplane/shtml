package com.lightdev.app.shtm.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.FontDialog;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * Show a dialog to format fonts
   */
  public class FontAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public FontAction(SHTMLPanel panel) {
      super(SHTMLPanel.fontAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      this.panel.getEditor().requestFocus();

      /** create a modal FontDialog, center and show it */
      FontDialog fd = new FontDialog(parent,
				    DynamicResource.getResourceString(SHTMLPanel.resources, "fontDialogTitle"),
                                    this.panel.getMaxAttributes(this.panel.getEditor(), null));
      Util.center(parent, fd);
      fd.setModal(true);
      fd.show();

      /** if the user made a selection, apply it to the document */
      if(fd.getResult() == FontDialog.RESULT_OK) {
        this.panel.getEditor().applyAttributes(fd.getAttributes(), false);
        this.panel.updateFormatControls();
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