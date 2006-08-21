package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

/**
   * close a document.
   *
   * <p>the action takes into account whether or not a document needs to be
   * saved.</p>
   *
   * <p>By having the actual closing task in a separate public method of this
   * action, the close functionality can be shared with action 'close all' or
   * others that might need it.</p>
   */
  public class SHTMLFileCloseAction extends AbstractAction
	implements SHTMLAction
  {

    /**
     * 
     */
    private final SHTMLPanel panel;
    private boolean exitApp = false;

    /** constructor 
     * @param panel TODO*/
    public SHTMLFileCloseAction(SHTMLPanel panel) {
      super(SHTMLPanel.closeAction);
    this.panel = panel;
      getProperties();
    }

    /** close the currently active document, if there is one */
    public void actionPerformed(ActionEvent ae) {
      if(this.panel.getTabbedPaneForDocuments().getTabCount() > 0) {                   // if documents are open
        closeDocument(this.panel.getActiveTabNo(), ae, false);  // close the active one
      }
      this.panel.updateActions();
    }

    /**
     * close a document by its tab index.
     *
     * <p>The method takes care of saving the document if necessary prior
     * to closing.</p>
     *
     * @param the tab index number of the document in the tabbed pane.
     * @return true, if the document was closed successfully.
     */
    public void closeDocument(final int index, ActionEvent ae, boolean ignoreChanges) {
      //System.out.println("closeDocument index=" + index);
      exitApp = ae.getActionCommand().indexOf(SHTMLPanel.exitAction) > -1;
      final DocumentPane dp = (DocumentPane) this.panel.getTabbedPaneForDocuments().getComponentAt(index);
      if(!dp.saveInProgress()) {            // if no save is going on and..
        //System.out.println("closeDocument: no save is going on");
        if(ignoreChanges) {
          closeDoc(dp);
        }
        else {
          if(dp.needsSaving()) {              // ..the document needs to be saved
            //System.out.println("closeDocument: " + dp.getDocumentName() + " needsSaving");
            this.panel.selectTabbedPane(index);  
            String docName = dp.getDocumentName();
            int choice = Util.msgChoice(JOptionPane.YES_NO_CANCEL_OPTION, "confirmClosing", "saveChangesQuery", docName, "\r\n\r\n");
            switch(choice) {
              case JOptionPane.YES_OPTION:           // if the user wanted to save
                if(dp.isNewDoc()) {                     //if the document is new
                  panel.dynRes.getAction(SHTMLPanel.saveAsAction).actionPerformed(ae); // 'save as'
                }
                else {                                             // else
                    panel.dynRes.getAction(SHTMLPanel.saveAction).actionPerformed(ae);   // 'save'
                }
                scheduleClose(dp);    //..and wait until it is finshed, then close
                break;
              case JOptionPane.NO_OPTION:       // if the user don't like to save
                closeDoc(dp);       // close the document without saving
                break;
              case JOptionPane.CANCEL_OPTION:             // if the user cancelled
                //System.out.println("closeDocument: save cancelled for " + dp.getDocumentName());
                break;                                    // do nothing
            }
          }
          else {                      // if the document does not need to be saved
            //System.out.println("closeDocument: " + dp.getDocumentName() + " NOT needsSaving");
            closeDoc(dp);             // close the document
          }
        }
      }
      else {                  // save was going on upon close request, so
        //System.out.println("closeDocument: a save is going on, wait");
        scheduleClose(dp);    // wait for completion, then close
      }
    }

    /**
     * schedule closing of a document.
     *
     * <p>This creates a <code>Timer</code> thread for which a
     * <code>TimerTask</code> is scheduled to peridically check
     * whether or not the save process for respective document commenced
     * successfully.</p>
     *
     * <p>If yes, Timer and TimerTask are disposed and the document
     * is closed. If not, the document remains open.</p>
     *
     * @param dp  the document to close
     * @param index  the number of the tab for that document
     */
    private void scheduleClose(final DocumentPane dp) {
      //System.out.println("scheduleClose for " + dp.getDocumentName());
      final java.util.Timer timer = new java.util.Timer();
      TimerTask task = new TimerTask() {
        public void run() {
          if(!dp.saveInProgress()) {                   // if done with saving
            if(dp.saveSuccessful) {                     // and all went fine
              closeDoc(dp);                          // close the document
              this.cancel();                            // dispose the task
              timer.cancel();                           // dispose the timer
            }
          }
        }
      };
      timer.schedule(task, 0, 400); // try to close every 400 milliseconds
    }

    /**
     * convenience method for closing a document
     */
    private void closeDoc(DocumentPane dp) {
      //System.out.println("closeDoc for document " + dp.getDocumentName());
      try {
        dp.deleteTempDir();
        this.panel.unregisterDocument();
        //jtpDocs.remove(jtpDocs.indexOfComponent(dp));   // try to close the doc
        this.panel.getTabbedPaneForDocuments().remove(dp);
      }
      catch(IndexOutOfBoundsException e) { // if the tabs have changed meanwhile
        catchCloseErr(dp);
      }
      if(exitApp) { // if the doc close was caused by a request to exit the app
        if(this.panel.getTabbedPaneForDocuments().getTabCount() == 0) {      // ..and if there are no open docs
          System.exit(0);                               // exit the application
        }
      }
    }

    private void catchCloseErr(DocumentPane dp) {
      try {
        int i = this.panel.getTabbedPaneForDocuments().indexOfComponent(dp);       // get the current tab index
        if(i < 0 && this.panel.getTabbedPaneForDocuments().getTabCount() > 0) {
          this.panel.setActiveTabNo(this.panel.getTabbedPaneForDocuments().getSelectedIndex());
          dp = (DocumentPane) this.panel.getTabbedPaneForDocuments().getComponentAt(this.panel.getActiveTabNo());
          i = this.panel.getTabbedPaneForDocuments().indexOfComponent(dp);   // get the current tab index again
          this.panel.unregisterDocument();
          this.panel.getTabbedPaneForDocuments().remove(i);                                      //now remove it
        }
        else {
          while(i > 0 && i > this.panel.getTabbedPaneForDocuments().getTabCount()) {     // while its still wrong
            i = this.panel.getTabbedPaneForDocuments().indexOfComponent(dp);   // get the current tab index again
          }
          this.panel.unregisterDocument();
          this.panel.getTabbedPaneForDocuments().remove(i);                                      //now remove it
        }
      }
      catch(IndexOutOfBoundsException e) {
        catchCloseErr(dp);
      }
    }

    /** update the state of this action */
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