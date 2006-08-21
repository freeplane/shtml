package com.lightdev.app.shtm.actions;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.lightdev.app.shtm.DocumentPane;
import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;
import com.lightdev.app.shtm.DocumentPane.DocumentPaneListener;
import com.sun.demo.ExampleFileFilter;

/** open an existing document from file and show it */
  public class SHTMLFileOpenAction extends AbstractAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLFileOpenAction(SHTMLPanel panel) {
      super(SHTMLPanel.openAction);
    this.panel = panel;
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      Preferences prefs = Preferences.userNodeForPackage( this.panel.getClass() );
      JFileChooser chooser = new JFileChooser();        // create a file chooser
      ExampleFileFilter filter = new ExampleFileFilter();     // create a filter
      filter.addExtension("htm");
      filter.addExtension("html");
      filter.setDescription(DynamicResource.getResourceString(SHTMLPanel.resources, "htmlFileDesc"));
      chooser.setFileFilter(filter);                    // apply the file filter
      String lastFileName = prefs.get(SHTMLPanel.FILE_LAST_OPEN, "");
      if(lastFileName.length() > 0) {
        chooser.setCurrentDirectory(new File(lastFileName).getParentFile());
      }
      int returnVal =                             // ..and show the file chooser
        chooser.showOpenDialog((Component) ae.getSource());
      if(returnVal == JFileChooser.APPROVE_OPTION) {   // if a file was selected
        File file = chooser.getSelectedFile();
        prefs.put(SHTMLPanel.FILE_LAST_OPEN, file.getAbsolutePath());
        openDocument(file);
      }
      this.panel.updateActions();
    }

    public void openDocument(File file) {
      openDocument(file, null);
    }

    public void openDocument(File file, DocumentPane.DocumentPaneListener listener) {
      int openDocNo = -1;
      try {
        openDocNo = getOpenDocument(file.toURL().toString());
      }
      catch(MalformedURLException mue) {}
      if(openDocNo > -1) {
        //System.out.println("FrmMain.SHTMLFileOpenAction.openAction setting to open doc no " + openDocNo);
        this.panel.getTabbedPaneForDocuments().setSelectedIndex(openDocNo);
      }
      else {
        //System.out.println("FrmMain.SHTMLFileOpenAction.openAction loading file " + file);
        FileLoader loader = new FileLoader(file, null, listener);
        loader.start();
      }
    }

    public int getOpenDocument(String url) {
      int tabNo = -1;
      int openDocCount = this.panel.getTabbedPaneForDocuments().getTabCount();
      int i = 0;
      while(i < openDocCount && tabNo < 0) {
        URL source = ((DocumentPane) this.panel.getTabbedPaneForDocuments().getComponentAt(i)).getSource();
        if(source != null) {
          if(source.toString().equalsIgnoreCase(url)) {
            tabNo = i;
          }
        }
        i++;
      }
      return tabNo;
    }

    /**
     * get a FileLoader object for the document currently active
     *
     * @param url  the url of the file to open
     */
    public FileLoader createFileLoader(URL url) {
      return new FileLoader(new File(url.getFile()), null);
    }

    /**
     * Helper class for being able to load a document in a separate thread.
     * Using a separate thread will not cause the application to block during
     * a lengthy load operation
     */
    public class FileLoader extends Thread {
      File file;
      Component owner;
      DocumentPane.DocumentPaneListener l;
      public FileLoader(File file, Component owner) {
        this.file = file;
        this.owner = owner;
      }
      public FileLoader(File file, Component owner, DocumentPane.DocumentPaneListener listener) {
        this(file, owner);
        this.l = listener;
      }
      public void run() {
        try {
          Frame parent =JOptionPane.getFrameForComponent(panel);
          SHTMLFileOpenAction.this.panel.setDocumentPane(new DocumentPane(file.toURL(), 0/*, renderMode*/));
          if(l != null) {
            SHTMLFileOpenAction.this.panel.getDocumentPane().addDocumentPaneListener(l);
          }
          SHTMLFileOpenAction.this.panel.getTabbedPaneForDocuments().setSelectedComponent(
              SHTMLFileOpenAction.this.panel.getTabbedPaneForDocuments().add(SHTMLFileOpenAction.this.panel.getDocumentPane().getDocumentName(), SHTMLFileOpenAction.this.panel.getDocumentPane()));
	  SHTMLFileOpenAction.this.panel.registerDocument();
        }
        catch(Exception e) {
          Util.errMsg(owner, DynamicResource.getResourceString(SHTMLPanel.resources, "unableToOpenFileError"), e);
        }
      }
    }

    public void update() {
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }