package com.lightdev.app.shtm.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;
import com.lightdev.app.shtm.DocumentPane.DocumentPaneListener;
import com.sun.demo.ExampleFileFilter;

/**
   * save a document under a different name and/or location
   *
   * <p>If a file already exists at the chosen location / name, the method
   * will ask the user if the existing file shall be overwritten.
   */
  public class SHTMLFileSaveAsAction extends AbstractAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLFileSaveAsAction(SHTMLPanel panel) {
      super(SHTMLPanel.saveAsAction);
    this.panel = panel;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      boolean canSave = true;
      Preferences prefs = Preferences.userNodeForPackage( this.panel.getClass() );
      JFileChooser chooser = new JFileChooser();
      ExampleFileFilter filter = new ExampleFileFilter();
      filter.addExtension("htm");
      filter.addExtension("html");
      filter.setDescription(DynamicResource.getResourceString(SHTMLPanel.resources, "htmlFileDesc"));
      chooser.setFileFilter(filter);
      String lastSaveFileName = prefs.get(SHTMLPanel.FILE_LAST_SAVE, "");
      if(lastSaveFileName.length() > 0) {
        chooser.setCurrentDirectory(new File(lastSaveFileName).getParentFile());
      }
      URL sourceUrl = this.panel.getDocumentPane().getSource();
      String fName;
      if(sourceUrl != null) {
        fName = sourceUrl.getFile();
      }
      else {
        fName = this.panel.getDocumentPane().getDocumentName();
        //System.out.println("SHTMLFileSaveAsAction fName=" + fName);
        fName = Util.removeChar(fName, ' ');
        //System.out.println("SHTMLFileSaveAsAction fName=" + fName);
      }
      if(fName.indexOf(Util.CLASS_SEPARATOR) < 0) {
        chooser.setSelectedFile(new File(fName + ".htm"));
      }
      else {
        chooser.setSelectedFile(new File(fName));
      }
      int result = chooser.showSaveDialog((Component) ae.getSource());
      if(result == JFileChooser.APPROVE_OPTION) {
        File selection = chooser.getSelectedFile();
        prefs.put(SHTMLPanel.FILE_LAST_SAVE, selection.getAbsolutePath());
        if(selection.exists()) {
          String newName = selection.getName();
          canSave = Util.msg(JOptionPane.YES_NO_OPTION, "confirmSaveAs", "fileExistsQuery", newName, " ");
        }
        if(canSave) {
          try {
            NewFileSaver saver = new NewFileSaver(
                                  this.panel.getDocumentPane(), selection.toURL(), this.panel.getActiveTabNo());
            saver.setName("NewFileSaver");
            saver.start();
          }
          catch(Exception ex) {
            Util.errMsg((Component) ae.getSource(),
                DynamicResource.getResourceString(SHTMLPanel.resources, "cantCreateURLError") +
                    selection.getAbsolutePath(),
                ex);
          }
        }
      }
      this.panel.updateActions();
    }

    /**
     * Helper class for being able to save a document in a separate thread.
     * Using a separate thread will not cause the application to block during
     * a lengthy save operation
     */
    public class NewFileSaver extends Thread {
      DocumentPane dp;
      URL url;
      int activeTabNo;
      DocumentPane.DocumentPaneListener l;
      NewFileSaver(DocumentPane dp, URL url, int activeTabNo) {
        this.dp = dp;
        this.url = url;
        this.activeTabNo = activeTabNo;
      }
      NewFileSaver(DocumentPane dp, URL url, int activeTabNo, DocumentPane.DocumentPaneListener listener) {
        this(dp, url, activeTabNo);
        this.l = listener;
      }
      public void run() {
        this.dp.setSource(url);
        SHTMLFileSaveAsAction.this.panel.doSave(this.dp);
        if(this.dp.saveSuccessful) {
          SHTMLFileSaveAsAction.this.panel.getTabbedPaneForDocuments().setTitleAt(SHTMLFileSaveAsAction.this.panel.getTabbedPaneForDocuments().indexOfComponent(this.dp),
					  this.dp.getDocumentName());
          if(l != null) {
            dp.addDocumentPaneListener(l);
          }
        }
      }
    }

    /**
     * get a FileSaver object for the document currently active
     *
     * @param url  the url of the file to save
     */
    public NewFileSaver createNewFileSaver(URL url) {
      return new NewFileSaver(this.panel.getDocumentPane(), url, this.panel.getActiveTabNo());
    }

    /**
     * get a FileSaver object for the document currently active
     *
     * @param url  the url of the file to save
     */
    public NewFileSaver createNewFileSaver(URL url, DocumentPane.DocumentPaneListener listener) {
      return new NewFileSaver(this.panel.getDocumentPane(), url, this.panel.getActiveTabNo(), listener);
    }

    public void update() {
      boolean isEnabled = this.panel.getTabbedPaneForDocuments().getTabCount() > 0;
      boolean saveInProgress = false;
      if(isEnabled) {
        saveInProgress = this.panel.getDocumentPane().saveInProgress();
      }
      this.setEnabled(isEnabled && !saveInProgress);
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }