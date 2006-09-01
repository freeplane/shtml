package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.html.HTML;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * toggle list formatting for a given type of list on/off
   */
  public class ToggleListAction extends AbstractAction
                                implements SHTMLAction
  {

    /**
     * 
     */
    private final SHTMLPanel panel;
    private HTML.Tag listTag;

    public ToggleListAction(SHTMLPanel panel, String name, HTML.Tag listTag) {
      super(name);
    this.panel = panel;
      this.listTag = listTag;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      this.panel.getEditor().toggleList(listTag.toString(),
                        this.panel.getMaxAttributes(this.panel.getEditor(), listTag.toString()),
                        false);
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