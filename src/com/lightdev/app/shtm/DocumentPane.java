/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.lightdev.app.shtm;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.sun.demo.ExampleFileFilter;
import java.util.*;
import java.util.prefs.*;

/**
 * GUI representation of a document.
 *
 * <p>Swing already uses three types of classes to implement a model, view,
 * controller (MVC) approach for a document:</p>
 *
 * <p>JTextComponent - the view implementation<br>
 * Document - the model implementation<br>
 * EditorKit - the controller implementation</p>
 *
 * <p>For a GUI representation of a document, additional parts are needed, such
 * as a JScrollPane as well as listeners and fields to track the state of
 * the document while it is represented on a GUI.</p>
 *
 * <p><code>DocumentPane</code> wraps all those elements to implement a single
 * document centric external view to all elements.</p>
 *
 * <p>If for instance an application wants to create a new document, it simply
 * creates an instance of this class instead of having to implement own
 * methods for instatiating each element (editor pane, scroll pane, etc.)
 * separately.</p>
 *
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * 
 */

public class DocumentPane extends JPanel implements DocumentListener, ChangeListener {

  /** the editor displaying the document in layout view */
  private SHTMLEditorPane editor;

  /** the editor displaying the document in HTML code view */
  private SyntaxPane htmlEditor;

  /** temporary storage location for this document */
  private File docTempDir = null;

  /** the save thread, if a save operation is in progress */
  public Thread saveThread = null;

  /** indicator if a save operation was succesful */
  public boolean saveSuccessful = false;

  /** indicates if the document text has changed */
  private boolean textChanged;

  /**
   * @param textChanged The textChanged to set.
   */
  private void setTextChanged(boolean textChanged) {
      this.textChanged = textChanged;
  }

  /**
   * @return Returns the textChanged.
   */
  private boolean isTextChanged() {
      return textChanged;
  }

  /** the name of the document */
  private String docName;

  /** the file the current style sheet was loaded from, if any */
  private File loadedStyleSheet = null;

  /** the URL the document was loaded from (if applicable)*/
  private URL sourceUrl = null;

  /** JTabbedPane for our views */
  private JTabbedPane tpView;

  public static final int VIEW_TAB_LAYOUT = 0;
  public static final int VIEW_TAB_HTML = 1;

  /**
   * a save place for sourceUrl, when a document is to be saved
   * under a new name and this fails
   */
  private URL savedUrl = null;

  /** indicates if this document was loaded froma file */
  private boolean loadedFromFile = false;

  /** default document name */
  private String DEFAULT_DOC_NAME = "Untitled";

  /** default name for style sheet, when saved */
  public static String DEFAULT_STYLE_SHEET_NAME = "style.css";

  /** number for title of a new document */
  private int newDocNo;


  //private int renderMode;

  /**
   * construct a new <code>DocumentPane</code>.
   *
   * <p>A document still has to be either created or loaded after using
   * this constructor, so it is better to use the constructor doing this
   * right away instead.</p>
   */
  public DocumentPane(/*int renderMode*/) {
    super();

    // EditorPane and ScrollPane for layout view
    editor = new SHTMLEditorPane();
    SHTMLEditorKit kit = new SHTMLEditorKit(/*renderMode*/);
    //kit.resetStyleSheet();
    editor.setEditorKit(kit);
    JScrollPane sp = new JScrollPane();   // create a new JScrollPane,
    sp.getViewport().setView(editor);     // ..add the editor pane to it

    // EditorPane and ScrollPane for html view
    htmlEditor = new SyntaxPane();
    htmlEditor.setFont(new Font("Monospaced", Font.PLAIN, 12));
    //htmlEditor.addKeyListener(rkw);
    JScrollPane htmlSp = new JScrollPane();
    htmlSp.getViewport().setView(htmlEditor);

    // tabbed pane for HTML and layout views
    tpView = new JTabbedPane();
    tpView.setTabPlacement(JTabbedPane.BOTTOM);
    tpView.add(sp, VIEW_TAB_LAYOUT);
    tpView.add(htmlSp, VIEW_TAB_HTML);
    tpView.setTitleAt(VIEW_TAB_LAYOUT, DynamicResource.getResourceString(SHTMLPanel.resources, "layoutTabTitle"));
    tpView.setTitleAt(VIEW_TAB_HTML, DynamicResource.getResourceString(SHTMLPanel.resources, "htmlTabTitle"));
    tpView.addChangeListener(this);

    // add comnponents to content pane
    setLayout(new BorderLayout());        // a simple border layout is enough
    add(tpView, BorderLayout.CENTER);         // ..and add both to this DocumentPane
    setTextChanged(false);                  // no changes so far
    setPreferredSize(new Dimension(550, 550));
  }

  /**
   * construct a new DocumentPane with either a new Document or an exisiting
   * Document that is to be loaded into the DocumentPane upon construction.
   *
   * @param docToLoad the document to be loaded. If this is null, a new
   *      Document is created upon construction of the DocumentPane
   * @param newDocNo  the number a new document shall have in the
   * title as long as it is not saved (such as in 'Untitled1'). If an
   * existing document shall be loaded, this number is ignored
   */
  public DocumentPane(URL docToLoad, int newDocNo/*, int renderMode*/) {
    this(/*renderMode*/);
    DEFAULT_DOC_NAME = DynamicResource.getResourceString(
        SHTMLPanel.resources, "defaultDocName");
    if(docToLoad != null) {
      loadDocument(docToLoad);
    }
    else {
      this.newDocNo = newDocNo;
      createNewDocument();
    }
  }

  /**
   * get the <code>JEditorPane</code> of this <code>DocumentPane</code>
   *
   * @return the JEditorPane of this DocumentPane
   */
  public SHTMLEditorPane getEditor() {
    return editor;
  }

  /**
   * get the <code>SyntaxPane</code> of this <code>DocumentPane</code>
   *
   * @return the SyntaxPane of this DocumentPane
   */
  public SyntaxPane getHtmlEditor() {
    return htmlEditor;
  }

  /**
   * @return the selected tab index
   */
  public int getSelectedTab(){
      return tpView.getSelectedIndex();
  }
  /**
   * create a new HTMLDocument and attach it to the editor
   */
  public void createNewDocument() {
    try {
      SHTMLEditorKit kit = (SHTMLEditorKit) editor.getEditorKit();
      SHTMLDocument doc = (SHTMLDocument) kit.createDefaultDocument();
      //insertStyleRef(doc); // create style sheet reference in HTML header tag
      //styles = kit.getStyleSheet();
      doc.addDocumentListener(this); // listen to changes
      doc.setBase(createTempDir());
      editor.setDocument(doc); // let the document be edited in our editor
      //doc.putProperty(Document.TitleProperty, getDocumentName());
      Preferences prefs = Preferences.userNodeForPackage(getClass().forName("com.lightdev.app.shtm.PrefsDialog"));
      boolean useStyle = prefs.getBoolean(PrefsDialog.PREFS_USE_STD_STYLE_SHEET, false);
      if(useStyle) {
        doc.insertStyleRef();
      }
    }
    catch(Exception e) {
      Util.errMsg(this, e.getMessage(), e);
    }
  }

  public void setDocument(Document docToSet) {
    try {
      SHTMLEditorKit kit = (SHTMLEditorKit) editor.getEditorKit();
      HTMLDocument doc = (HTMLDocument) getDocument();
      if(doc != null) {
        doc.removeDocumentListener(this);
      }
      docToSet.addDocumentListener(this); // listen to changes
      editor.setDocument(docToSet); // let the document be edited in our editor
    }
    catch(Exception e) {
      Util.errMsg(this, e.getMessage(), e);
    }
  }

  /**
   * create temporary directory for a newly created document
   * so that images can be stored and referenced until the document
   * is saved.
   *
   * @return URL of created temporary document directoy
   */
  private URL createTempDir() throws MalformedURLException {
    docTempDir = new File(SHTMLPanel.getAppTempDir().getAbsolutePath() +
                               File.separator +
                               getDocumentName() + File.separator);
    if(!docTempDir.exists()) {
      docTempDir.mkdirs();
    }
    return docTempDir.toURL();
  }

  /**
   * remove the temporary storage created for this <code>DocumentPane</code>
   */
  public void deleteTempDir() {
    if(docTempDir != null) {
      Util.deleteDir(docTempDir);
      docTempDir = null;
    }
  }

  /**
   * load a document found at a certain URL.
   *
   * @param url the URL to look for the document
   */
  public void loadDocument(URL url) {
    try {
      SHTMLEditorKit kit = (SHTMLEditorKit) editor.getEditorKit();
      SHTMLDocument doc = (SHTMLDocument) kit.createDefaultDocument();
      doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
      doc.setBase(new File(url.getPath()).getParentFile().toURL()); // set the doc base
      InputStream in = url.openStream(); // get an input stream
      kit.read(in, doc, 0); // ..and read the document contents from it
      in.close(); // .. then properly close the stream
      doc.addDocumentListener(this); // listen to changes
      editor.setDocument(doc); // let the document be edited in our editor
      setSource(url); // remember where the document came from
      loadedFromFile = true;
    }
    catch (Exception ex) {
      Util.errMsg(this, "An exception occurred while loading the file", ex);
      ex.printStackTrace();
    }
  }

  /**
   * load the rules from a given style sheet file into a new <code>StyleSheet</code> object.
   *
   * @param  cssFile  the file object referring to the style sheet to load from
   *
   * @return the style sheet with rules loaded
   */
  private StyleSheet loadStyleSheet(File cssFile)
        throws MalformedURLException, IOException
  {
    StyleSheet s = new StyleSheet();
    s.importStyleSheet(cssFile.toURL());
    return s;
  }

  /**
   * saves the document to the file specified in the source of the
   * <code>DocumentPane</code> and creates the associated style sheet.
   *
   * The actual save process only is done, when there is a name to save
   * to. The class(es) calling this method have to make sure that a
   * name for new documents is requested from the user, for instance.
   *
   * The desired name and location for the save need then to be set using method
   * setSource prior to a call to this method
   *
   * @throws DocNameMissingException to ensure the caller gets notified
   *        that a save did not take place because of a missing name
   *        and location
   */
  public void saveDocument() throws DocNameMissingException {
    if(!saveInProgress()) {
      saveThread = Thread.currentThread(); // store thread for saveInProgress
      saveSuccessful = false; // if something goes wrong, this remains false
      File file = null;
      try {
        if(sourceUrl != null) {
          /* write the HTML document */
          if(tpView.getSelectedIndex() == VIEW_TAB_HTML) {
            editor.setText(htmlEditor.getText());
          }
          SHTMLDocument doc = (SHTMLDocument) getDocument();
          OutputStream os = new FileOutputStream(sourceUrl.getPath());
          OutputStreamWriter osw = new OutputStreamWriter(os);
          Preferences prefs = Preferences.userNodeForPackage(getClass().forName("com.lightdev.app.shtm.PrefsDialog"));
          String writeMode = prefs.get(PrefsDialog.PREFSID_WRITE_MODE, PrefsDialog.PREFS_WRITE_MODE_HTML32);
          if(writeMode.equalsIgnoreCase(PrefsDialog.PREFS_WRITE_MODE_HTML4)) {
            SHTMLWriter hw = new SHTMLWriter(osw, doc);
            hw.write();
          }
          else {
            SHTMLWriter hw = new SHTMLWriter(osw, doc);
            //HTMLWriter hw = new HTMLWriter(osw, doc);
            //System.out.println("DocumentPane.saveDocument saving with title=" + doc.getProperty(Document.TitleProperty));
            hw.write();
          }
          osw.flush();
          osw.close();
          os.flush();
          os.close();

          /* write the style sheet */
          if(doc.hasStyleRef()) {
            saveStyleSheet();
          }

          /*
            copy image directory,
            if new document or saved from different location
          */
          saveImages();

          /* clean up */
          //System.out.println("DocumentPane textChanged = false");
          setTextChanged(false); // indicate no changes pending anymore after the save
          file = new File(sourceUrl.getPath()).getParentFile();
          ((HTMLDocument) getDocument()).setBase(file.toURL()); // set the doc base
          deleteTempDir();
          //System.out.println("DocumentPane saveSuccessful = true");
          saveSuccessful = true; // signal that saving was successful
        }
        else {
          saveThread = null;
          throw new DocNameMissingException();
        }
      }
      catch(MalformedURLException mue) {
        if(file != null) {
          Util.errMsg(this, "Can not create a valid URL for\n" + file.getAbsolutePath(), mue);
        }
        else {
          Util.errMsg(this, mue.getMessage(), mue);
        }
      }
      catch(Exception e) {
        if(savedUrl != null) {
          sourceUrl = savedUrl;
        }
        Util.errMsg(this, "An exception occurred while saving the file", e);
      }
      saveThread = null;
      savedUrl = sourceUrl;
    }
  }

  /**
   * determine the directory this <code>DocumentPane</code> references image
   * files from
   *
   * @return the directory image files referenced by this
   * <code>DocumentPane</code> are found
   */
  public File getImageDir() {
    File srcDir = null;
    if(savedUrl == null && newDocNo > 0) {
      // new Document: use temp dir as source
      srcDir = new File(
          docTempDir +
          File.separator +
          SHTMLPanel.IMAGE_DIR +
          File.separator);
    }
    else {
      if(savedUrl == null) {
        // document has been saved before: source is 'sourceUrl'
        srcDir = new File(
            new File(sourceUrl.getPath()).getParent() +
            File.separator +
            SHTMLPanel.IMAGE_DIR +
            File.separator);
      }
      else {
        /*
           document has been saved before but now is
           to be saved under new name: source is 'old' url
        */
        srcDir = new File(
            new File(savedUrl.getPath()).getParent() +
            File.separator +
            SHTMLPanel.IMAGE_DIR +
            File.separator);
      }
    }
    return srcDir;
  }

  /**
   * save image files
   */
  private void saveImages() {
    File srcDir = getImageDir();
    File destDir = new File(
        new File(sourceUrl.getPath()).getParent() +
        File.separator +
        SHTMLPanel.IMAGE_DIR +
        File.separator);
    try {
      if(srcDir.exists()) {
        ExampleFileFilter filter = new ExampleFileFilter();
        filter.addExtension("gif");
        filter.addExtension("jpg");
        filter.addExtension("jpeg");
        File[] imgFiles = srcDir.listFiles();
        for(int i = 0; i < imgFiles.length; i++) {
          Util.copyFile(imgFiles[i], new File(destDir.getAbsolutePath() +
          File.separator + imgFiles[i].getName()));
        }
      }
    }
    catch(Exception e) {
      Util.errMsg(this, e.getMessage(), e);
    }
  }

  /**
   * indicates whether or not a save process is in progress
   *
   * @return true, if a save process is going on, else false
   */
  public boolean saveInProgress() {
    //System.out.println("DocumentPane.saveInProgress=" + (saveThread != null) + " for document " + getDocumentName());
    return saveThread != null;
  }

  /**
   * save the style sheet of this document to a CSS file.
   *
   * <p>With stage 8 this saves a style sheet by merging with an existing
   * one with the same name/location. Styles in this style sheet overwrite
   * styles in the existing style sheet.</p>
   */
  public void saveStyleSheet() throws IOException {
    SHTMLDocument doc = (SHTMLDocument) getDocument();
    StyleSheet styles = doc.getStyleSheet();
    String styleSheetName = getStyleSheetName();
    if(styleSheetName != null) {
      File styleSheetFile = new File(new URL(styleSheetName).getFile());
      if(!styleSheetFile.exists()) {
        // no styles present at save location, create new style sheet
        styleSheetFile.createNewFile();
      }
      else {
        if(loadedFromFile) {
          if((savedUrl == null) || (!savedUrl.getPath().equals(sourceUrl.getPath())))
          {
          /*
              this style sheet was loaded from somewhere else and now is
              being saved at a new location where a style sheet exists
              havig the same name --> merge
          */
            mergeStyleSheets(loadStyleSheet(styleSheetFile), styles);
          }
          else {
          /*
              same location where styles originally came
              from, overwrite existing styles with new version
          */
            styleSheetFile.delete();
            styleSheetFile.createNewFile();
          }
        }
        else {
          /*
              this style sheet was newly created and now is
              being saved at a location where a style sheet exists
              havig the same name --> merge
          */
          mergeStyleSheets(loadStyleSheet(styleSheetFile), styles);
        }
      }
      OutputStream os = new FileOutputStream(styleSheetFile);
      OutputStreamWriter osw = new OutputStreamWriter(os);
      CSSWriter cssWriter;
      cssWriter = new CSSWriter(osw, styles);
      cssWriter.write();
      osw.close();
      os.close();
    }
  }

  /**
   * merge two style sheets by adding all rules found
   * in a given source StyleSheet that are not contained
   * in a given destination StyleSheet. Assumes rules
   * of src and dest are already loaded.
   *
   * @param src  the source StyleSheet
   * @param dest  the destination StyleSheet
   */
  private void mergeStyleSheets(StyleSheet src, StyleSheet dest) throws IOException {
    String name;
    Object elem;
    Vector srcNames = Util.getStyleNames(src);
    Vector destNames = Util.getStyleNames(dest);
    StringWriter sw = new StringWriter();
    StringBuffer buf = sw.getBuffer();
    CSSWriter cw = new CSSWriter(sw, null);
    for(int i = 0; i < srcNames.size(); i++) {
      elem = srcNames.get(i);
      name = elem.toString();
      if(destNames.indexOf(elem) < 0) {
        buf.delete(0, buf.length());
        cw.writeRule(name, src.getStyle(name));
        dest.removeStyle(name);
        dest.addRule(buf.toString());
      }
    }
  }

  /**
   * get the URL of the style sheet of this document
   *
   * <p>The name is built by<ol>
   * <li>get the style sheet reference, if none, use default style
   * sheet name</li>
   * <li>get the document base</li>
   * <li>if the style sheet reference is a relative path, resolve base
   * and relative path</li>
   * <li>else simply concatenate doc base and style sheet reference</li>
   * </ol></p>
   *
   * @return the URL of the style sheet
   */
  private String getStyleSheetName() throws MalformedURLException {
    String name = DEFAULT_STYLE_SHEET_NAME; //SHTMLEditorKit.DEFAULT_CSS;
    SHTMLDocument doc = (SHTMLDocument) getDocument();
    String styleRef = doc.getStyleRef();
    File file = new File(sourceUrl.getPath()).getParentFile();
    String newDocBase = null;
    try {
      newDocBase = file.toURL().toString();
    }
    catch(Exception e) {
      if(file != null) {
        Util.errMsg(this, "Can not create a valid URL for\n" + file.getAbsolutePath(), e);
      }
      else {
        Util.errMsg(this, e.getMessage(), e);
      }
    }
    if(styleRef != null) {
      name = Util.resolveRelativePath(styleRef, newDocBase);
    }
    else {
      name = null; // Util.resolveRelativePath(name, newDocBase);
    }
    //System.out.println("DocumentPane.getStyleSheetName=" + name);
    return name;
  }

  /**
   * get the name of the document of this pane.
   *
   * @return  the name of the document
   */
  public String getDocumentName() {
    String theName;
    if(docName==null || docName.length() < 1) {
      theName = DEFAULT_DOC_NAME + " " + Integer.toString(newDocNo);
    }
    else {
      theName = docName;
    }
    return theName;
  }

  /**
   * indicates whether or not the document needs to be saved.
   *
   * @return  true, if changes need to be saved
   */
  public boolean needsSaving() {
    //System.out.println("DocumentPane.needsSaving=" + textChanged + " for document " + getDocumentName());
    return isTextChanged();
  }

  /**
   * set the source this document is to be loaded from
   *
   * <p>This is only to be used when it is made sure,
   * that the document is saved at the location specified
   * by 'source'.</p>
   *
   * @param the URL of the source this document is to be loaded from
   */
  public void setSource(URL source) {
    savedUrl = sourceUrl;
    sourceUrl = source;
    String fName = source.getFile();
    docName = fName.substring(fName.lastIndexOf("/") + 1);
    fireNameChanged();
  }

  /**
   * get the source, this document was having before its current sourceUrl
   * was set.
   *
   * @return the source URL before a name change
   */
  public URL getOldSource() {
    if(savedUrl == null) {
      return sourceUrl;
    }
    else {
      return savedUrl;
    }
  }

  /**
   * get the source this document can be loaded from
   *
   * @return the URL this document can be loaded from
   */
  public URL getSource() {
    return sourceUrl;
  }

  /**
   * indicates whether or not this document was newly created and not saved so
   * far.
   *
   * @return true, if this is a new document that has not been saved so far
   */
  public boolean isNewDoc() {
    return sourceUrl == null;
  }

  /**
   * get the document of this <code>DocumentPane</code>
   *
   * @return the <code>Document</code> of this <code>DocumentPane</code>
   */
  public Document getDocument() {
    return editor.getDocument();
  }

  HTMLDocument getHTMLDocument() {
    return (HTMLDocument)htmlEditor.getDocument();
  }

  /**
   * switch the DocumentPane to HTML view
   */
  private void setHTMLView() {
    try {
      editor.getDocument().removeDocumentListener(this);
      StringWriter sw = new StringWriter();
      SHTMLDocument lDoc = (SHTMLDocument) editor.getDocument();
      SHTMLEditorKit kit = (SHTMLEditorKit) editor.getEditorKit();
      kit.write(sw, lDoc, 0, lDoc.getLength());
      sw.close();
      htmlEditor.setText(sw.toString());
      htmlEditor.getDocument().addDocumentListener(this);
      htmlEditor.addCaretListener(htmlEditor);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * switch the DocumentPane to layout view
   */
  private void setLayoutView() {
    htmlEditor.getDocument().removeDocumentListener(this);
    htmlEditor.removeCaretListener(htmlEditor);
    editor.setText(htmlEditor.getText());
    editor.setCaretPosition(0);
    editor.getDocument().addDocumentListener(this);
  }

  /**
   * Convenience method for obtaining the document text
   * @return returns the document text as string.
   */
  String getDocumentText() {
      if(tpView.getSelectedIndex() == VIEW_TAB_HTML){
          editor.setText(htmlEditor.getText());
      }
      return editor.getText();
  }

  /**
   * Convenience method for setting the document text
   */
  void setDocumentText(String docName, String sText) {
      this.docName = docName;
      switch(tpView.getSelectedIndex()) {
      case VIEW_TAB_LAYOUT:
          editor.setText(sText);
        break;
      case VIEW_TAB_HTML:
          htmlEditor.setText(sText);
        break;
    }
      setTextChanged(false);
  }


  /* ----------------- changeListener implementation start ---------------------- */

  public void stateChanged(ChangeEvent e) {
    Object src = e.getSource();
    if(src.equals(tpView)) {
      switch(tpView.getSelectedIndex()) {
        case VIEW_TAB_LAYOUT:
          setLayoutView();
          break;
        case VIEW_TAB_HTML:
          setHTMLView();
          break;
      }
    }
    SHTMLPanel.getOwnerSHTMLPanel(this).updateActions();
  }


  /* ----------------- changeListener implementation end ------------------------ */

  /* -------- DocumentListener implementation start ------------*/

  /**
   * listens to inserts into the document to track whether or not the document
   * needs to be saved.
   */
  public void insertUpdate(DocumentEvent e) {
    //System.out.println("insertUpdate setting textChanged=true for " + getDocumentName());
    setTextChanged(true);
    /*if (tpView.getSelectedIndex() == VIEW_TAB_HTML) {
      StyledDocument sDoc = (StyledDocument) e.getDocument();
      htmlEditor.setMarks(sDoc, 0, sDoc.getLength(), this);
    }*/
  }

  /**
   * listens to removes from the document to track whether or not the document
   * needs to be saved.
   */
  public void removeUpdate(DocumentEvent e) {
    //System.out.println("removeUpdate setting textChanged=true for " + getDocumentName());
    setTextChanged(true);
  }

  /**
   * listens to changes on the document to track whether or not the document
   * needs to be saved.
   */
  public void changedUpdate(DocumentEvent e) {
    //System.out.println("changedUpdate setting textChanged=true for " + getDocumentName());
    if (tpView.getSelectedIndex() == VIEW_TAB_LAYOUT) {
      setTextChanged(true);
    }
  }

  /* -------- DocumentListener implementation end ------------*/

  /* -------- DocumentPaneListener definition start --------------- */

  /**
   * interface to be implemented for being notified of
   * changes to the name of this document
   */
  public interface DocumentPaneListener {
    public void nameChanged(DocumentPaneEvent e);
    public void activated(DocumentPaneEvent e);
  }

  /** the event object definition for DocumentPaneEvents */
  public class DocumentPaneEvent extends EventObject {
    public DocumentPaneEvent(Object source) {
      super(source);
    }
  }

  /** listeners for DocumentPaneEvents */
  private Vector dpListeners = new Vector();

  /**
   * add a DocumentPaneListener to this Document
   *
   * @param listener the listener object to add
   */
  public void addDocumentPaneListener(DocumentPaneListener listener) {
    if(!dpListeners.contains(listener)) {
      dpListeners.addElement(listener);
    }
    //System.out.println("DocumentPane.addDocumentPaneListener docName=" + getDocumentName() + ", listener.count=" + dpListeners.size());
  }

  /**
   * remove a DocumentPaneListener from this Document
   *
   * @param listener  the listener object to remove
   */
  public void removeDocumentPaneListener(DocumentPaneListener listener) {
    dpListeners.remove(listener);
  }

  /**
   * fire a DocumentPaneEvent to all registered DocumentPaneListeners
   */
  public void fireNameChanged() {
    Enumeration listenerList = dpListeners.elements();
    while(listenerList.hasMoreElements()) {
      ((DocumentPaneListener) listenerList.nextElement()).nameChanged(new DocumentPaneEvent(this));
    }
  }

  /**
   * fire a DocumentPaneEvent to all registered DocumentPaneListeners
   */
  public void fireActivated() {
    Enumeration listenerList = dpListeners.elements();
    while(listenerList.hasMoreElements()) {
      ((DocumentPaneListener) listenerList.nextElement()).activated(new DocumentPaneEvent(this));
    }
  }

  /**
   * remove all listeners
   */
  public void removeAllListeners() {
    dpListeners.clear();
  }

  /* -------- DocumentPaneListener definition end --------------- */
}
