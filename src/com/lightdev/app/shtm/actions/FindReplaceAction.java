package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

import de.calcom.cclib.text.FindReplaceDialog;
import de.calcom.cclib.text.FindReplaceEvent;
import de.calcom.cclib.text.FindReplaceListener;

/**
   * action to find and replace a given text
   */
  public class FindReplaceAction extends AbstractAction implements SHTMLAction, FindReplaceListener
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public FindReplaceAction(SHTMLPanel panel) {
      super(SHTMLPanel.findReplaceAction);
    this.panel = panel;
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      currentTab = this.panel.getTabbedPaneForDocuments().getSelectedIndex();
      caretPos = this.panel.getDocumentPane().getEditor().getCaretPosition();
      if(this.panel.getTabbedPaneForDocuments().getTabCount() > 1) {
        System.out.println("FindReplaceAction.actionPerformed with Listener");
        FindReplaceDialog frd = new FindReplaceDialog(this.panel.getMainFrame(), this.panel.getEditor(), this);
      }
      else {
        System.out.println("FindReplaceAction.actionPerformed NO Listener");
        FindReplaceDialog frd = new FindReplaceDialog(this.panel.getMainFrame(), this.panel.getEditor());
      }
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

    public void getNextDocument(FindReplaceEvent e) {
      FindReplaceDialog frd = (FindReplaceDialog) e.getSource();
      int tabCount = this.panel.getTabbedPaneForDocuments().getTabCount();
      int curTab = this.panel.getTabbedPaneForDocuments().getSelectedIndex();
      System.out.println("FindReplaceAction.getNextDocument curTab=" + curTab + ", tabCount=" + tabCount);
      if(++curTab < tabCount) {
        System.out.println("FindReplaceAction.getNextDocument next tab no=" + curTab);
        resumeWithNewEditor(frd, curTab);
        /*
        jtpDocs.setSelectedIndex(curTab);
        DocumentPane docPane = (DocumentPane) jtpDocs.getComponentAt(curTab);
        JEditorPane editor = docPane.getEditor();
        editor.requestFocus();
        frd.setEditor(editor);
        frd.resumeOperation();
        */
      }
      else {
        frd.terminateOperation();
      }
    }

    public void getFirstDocument(FindReplaceEvent e) {
      FindReplaceDialog frd = (FindReplaceDialog) e.getSource();
      resumeWithNewEditor(frd, 0);
      /*DocumentPane docPane = (DocumentPane) jtpDocs.getComponentAt(0);
      jtpDocs.setSelectedIndex(0);
      JEditorPane editor = docPane.getEditor();
      editor.requestFocus();
      frd.setEditor(editor);
      frd.resumeOperation();*/
    }

    public void findReplaceTerminated(FindReplaceEvent e) {
      this.panel.getTabbedPaneForDocuments().setSelectedIndex(currentTab);
      DocumentPane docPane = (DocumentPane) this.panel.getTabbedPaneForDocuments().getSelectedComponent();
      JEditorPane editor = docPane.getEditor();
      editor.setCaretPosition(caretPos);
      editor.requestFocus();
    }

    private void resumeWithNewEditor(FindReplaceDialog frd, int tabNo) {
      this.panel.getTabbedPaneForDocuments().setSelectedIndex(tabNo);
      DocumentPane docPane = (DocumentPane) this.panel.getTabbedPaneForDocuments().getComponentAt(tabNo);
      JEditorPane editor = docPane.getEditor();
      editor.requestFocus();
      frd.setEditor(editor);
      frd.resumeOperation();
    }

    private int caretPos;
    private int currentTab;
  }