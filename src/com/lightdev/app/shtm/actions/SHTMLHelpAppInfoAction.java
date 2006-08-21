package com.lightdev.app.shtm.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.AboutBox;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/** show information about SimplyHTML in a dialog */
  public class SHTMLHelpAppInfoAction extends AbstractAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLHelpAppInfoAction(SHTMLPanel panel) {
      super(SHTMLPanel.aboutAction);
    this.panel = panel;
      getProperties();
    }
    public void actionPerformed(ActionEvent ae) {
      Frame parent = JOptionPane.getFrameForComponent(this.panel);
      AboutBox dlg = new AboutBox(parent);
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();
      this.panel.repaint();
      this.panel.updateActions();
    }
    public void update() {
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }