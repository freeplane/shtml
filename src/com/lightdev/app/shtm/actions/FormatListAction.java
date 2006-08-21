package com.lightdev.app.shtm.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;

import com.lightdev.app.shtm.DialogShell;
import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.ListDialog;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * Change list formatting
   */
  public class FormatListAction extends AbstractAction
                                implements SHTMLAction
  {

    /**
     * 
     */
    private final SHTMLPanel panel;

    public FormatListAction(SHTMLPanel panel) {
      super(SHTMLPanel.formatListAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      this.panel.getEditor().requestFocus();
      int pos = this.panel.getEditor().getSelectionStart();
      ListDialog dlg = new ListDialog(parent,
                     DynamicResource.getResourceString(SHTMLPanel.resources, "listDialogTitle"));
      SimpleAttributeSet set = new SimpleAttributeSet(
          this.panel.getMaxAttributes(this.panel.getEditor(), HTML.Tag.UL.toString()));
      set.addAttributes(this.panel.getMaxAttributes(this.panel.getEditor(), HTML.Tag.OL.toString()));
      dlg.setListAttributes(set);
      String currentTag = dlg.getListTag();
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();

      /** if the user made a selection, apply it to the document */
      if(dlg.getResult() == DialogShell.RESULT_OK) {
        AttributeSet a = dlg.getListAttributes();
        String newTag = dlg.getListTag();
        if(newTag == null) {
          this.panel.getEditor().toggleList(newTag, a, true);
        }
        else if(newTag.equalsIgnoreCase(currentTag)) {
          if(a.getAttributeCount() > 0) {
            this.panel.getEditor().applyListAttributes(a);
          }
        }
        else {
          this.panel.getEditor().toggleList(newTag, a, false);
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