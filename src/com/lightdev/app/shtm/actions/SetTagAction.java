package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * action to set the tag type
   */
  public class SetTagAction extends AbstractAction implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    private boolean ignoreActions = false;

    public SetTagAction(SHTMLPanel panel) {
      super(SHTMLPanel.setTagAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      if(!ignoreActions) {
        String tag = this.panel.getTagSelector().getSelectedTag();
        this.panel.getEditor().applyTag(tag, this.panel.getTagSelector().getTags());
        this.panel.updateActions();
      }
    }

    public void setIgnoreActions(boolean ignore) {
      ignoreActions = ignore;
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