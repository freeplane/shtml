package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * exit the application.
   *
   * <p>This will only exit the application, if<ul>
   * <li>no documents are open or </li>
   * <li>documents are open that do not need to be saved or </li>
   * <li>documents are open and are saved successfully prior to close or </li>
   * <li>documents are open for which the user explicitly opted not
   *        to save them </li>
   * </ul></p>
   */
  public class SHTMLFileExitAction extends AbstractAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLFileExitAction(SHTMLPanel panel) {
      super(SHTMLPanel.exitAction);
    this.panel = panel;
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      //System.out.println("FrmMain.SHTMLFileExitAction.actionPerformed");
      saveRelevantPrefs();
      new SHTMLFileCloseAllAction(this.panel).actionPerformed(ae);
      if(this.panel.getTabbedPaneForDocuments().getTabCount() == 0) {
        //removeAllListeners();
        System.exit(0);
      }
      this.panel.updateActions();
    }

    public void saveRelevantPrefs() {
      //System.out.println("FrmMain.SHTMLFileExitAction.saveRelevantPrefs");

      /* ---- save splitpane sizes start -------------- */

      this.panel.savePrefs();

      /* ---- save splitpane sizes end -------------- */
    }

    public void update() {
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }