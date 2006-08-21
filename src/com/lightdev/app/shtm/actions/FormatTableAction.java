package com.lightdev.app.shtm.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;

import com.lightdev.app.shtm.DialogShell;
import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.TableDialog;
import com.lightdev.app.shtm.Util;

/**
   * format table attributes
   */
  public class FormatTableAction extends AbstractAction
				implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;

    public FormatTableAction(SHTMLPanel panel) {
      super(SHTMLPanel.formatTableAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      this.panel.getEditor().requestFocus();
      int pos = this.panel.getEditor().getSelectionStart();
      TableDialog td = new TableDialog(parent,
                     DynamicResource.getResourceString(SHTMLPanel.resources, "tableDialogTitle"));
      td.setTableAttributes(this.panel.getMaxAttributes(this.panel.getEditor(), HTML.Tag.TABLE.toString()));
      td.setCellAttributes(this.panel.getMaxAttributes(this.panel.getEditor(), HTML.Tag.TD.toString()));
      Util.center(parent, td);
      td.setModal(true);
      td.show();

      /** if the user made a selection, apply it to the document */
      if(td.getResult() == DialogShell.RESULT_OK) {
        AttributeSet a = td.getTableAttributes();
        if(a.getAttributeCount() > 0) {
          this.panel.getEditor().applyTableAttributes(a);
        }
        a = td.getCellAttributes();
        if(a.getAttributeCount() > 0) {
          this.panel.getEditor().applyCellAttributes(a, td.getCellRange());
        }
      }
      this.panel.updateActions();
    }

    public void update() {
      if((this.panel.getTabbedPaneForDocuments().getTabCount() > 0) && (this.panel.getEditor().getCurTableCell() != null)) {
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