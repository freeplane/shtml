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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import com.sun.demo.ExampleFileFilter;

import static java.lang.Boolean.TRUE;

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
 * methods for instantiating each element (editor pane, scroll pane, etc.)
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
class DocumentPane extends JPanel implements DocumentListener, ChangeListener {
    /** the editor displaying the document in layout view */
    private final SHTMLEditorPane editorPane;
    /** the editor displaying the document in HTML code view */
    private final SyntaxPane sourceEditorPane;
    /** the save thread, if a save operation is in progress */
    public Thread saveThread = null;
    /** indicator if a save operation was successful */
    public boolean saveSuccessful = false;
    /** indicates if the document text has changed */
    private boolean documentChanged = false;

    /**
     * @param documentChanged The documentChanged to set.
     */
    private void setDocumentChanged(final boolean documentChanged) {
        this.documentChanged = documentChanged;
    }

    /**
     * @return Returns the documentChanged.
     */
    private boolean isDocumentChanged() {
        return documentChanged;
    }

    /** indicates if the document text has changed */
    private boolean htmlChanged = true;

    /** indicates if the HTML source was directly edited (vs changes from layout view) */
    private boolean htmlSourceEdited = false;

    /**
     * @param htmlChanged The htmlChanged to set.
     */
    private void setHtmlChanged(final boolean htmlChanged) {
        this.htmlChanged = htmlChanged;
    }

    /**
     * @return Returns the htmlChanged.
     */
    private boolean isHtmlChanged() {
        return htmlChanged;
    }

    /**
     * @param htmlSourceEdited The htmlSourceEdited to set.
     */
    private void setHtmlSourceEdited(final boolean htmlSourceEdited) {
        this.htmlSourceEdited = htmlSourceEdited;
    }

    /**
     * @return Returns the htmlSourceEdited.
     */
    private boolean isHtmlSourceEdited() {
        return htmlSourceEdited;
    }

    /** the name of the document */
    private String docName;
    /** JTabbedPane for our views */
    private final JComponent paneHoldingScrollPanes;
    private final JScrollPane richViewScrollPane;
    private final JScrollPane sourceViewScrollPane;
    public static final int VIEW_TAB_LAYOUT = 0;
    public static final int VIEW_TAB_HTML = 1;
    /** indicates if this document was loaded from a file */
    private boolean loadedFromFile = false;
    /** default document name */
    private String DEFAULT_DOC_NAME = "Untitled";
    /** default name for style sheet, when saved */
    public static final String DEFAULT_STYLE_SHEET_NAME = "style.css";
    /** number for title of a new document */
    private int newDocNo;
    private int activeView;

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
        editorPane = new SHTMLEditorPane();
        richViewScrollPane = new JScrollPane(); // create a new JScrollPane,
        richViewScrollPane.getViewport().setView(editorPane); // ..add the editor pane to it
        // EditorPane and ScrollPane for html view
        sourceEditorPane = new SyntaxPane();
        sourceEditorPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        //sourceEditorPane.addKeyListener(rkw);
        sourceViewScrollPane = new JScrollPane();
        sourceViewScrollPane.getViewport().setView(sourceEditorPane);
        // Tabbed pane for HTML and layout views
        if (Util.showViewsInTabs()) {
            paneHoldingScrollPanes = new JTabbedPane();
            paneHoldingScrollPanes.add(richViewScrollPane, VIEW_TAB_LAYOUT);
            paneHoldingScrollPanes.add(sourceViewScrollPane, VIEW_TAB_HTML);
            final JTabbedPane tabbedPane = (JTabbedPane) paneHoldingScrollPanes;
            tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
            tabbedPane.setTitleAt(VIEW_TAB_LAYOUT, Util.getResourceString("layoutTabTitle"));
            tabbedPane.setTitleAt(VIEW_TAB_HTML, Util.getResourceString("htmlTabTitle"));
            tabbedPane.addChangeListener(this);
            setLayout(new BorderLayout());
            add(paneHoldingScrollPanes, BorderLayout.CENTER);
        }
        else {
            paneHoldingScrollPanes = new JPanel(new BorderLayout());
            paneHoldingScrollPanes.add(richViewScrollPane, BorderLayout.CENTER);
            activeView = VIEW_TAB_LAYOUT;
            //BorderLayout DOES NOT allow two parts with ..CENTER.
            setLayout(new BorderLayout());
            add(paneHoldingScrollPanes, BorderLayout.CENTER);
        }
        setDocumentChanged(false); // no changes so far
        setPreferredSize(new Dimension(550, 550));
    }

    /**
     * construct a new DocumentPane with either a new Document or an existing
     * Document that is to be loaded into the DocumentPane upon construction.
     *
     * @param docToLoad the document to be loaded. If this is null, a new
     *      Document is created upon construction of the DocumentPane
     * @param newDocNo  the number a new document shall have in the
     * title as long as it is not saved (such as in 'Untitled1'). If an
     * existing document shall be loaded, this number is ignored
     */
    public DocumentPane(final URL docToLoad, final int newDocNo/*, int renderMode*/) {
        this(/*renderMode*/);
        DEFAULT_DOC_NAME = Util.getResourceString("defaultDocName");
        if (docToLoad != null) {
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
        return editorPane;
    }

    /**
     * get the <code>SyntaxPane</code> of this <code>DocumentPane</code>
     *
     * @return the SyntaxPane of this DocumentPane
     */
    public SyntaxPane getHtmlEditor() {
        return sourceEditorPane;
    }

    /**
     * @return the selected tab index
     */
    public int getSelectedTab() {
        if (paneHoldingScrollPanes instanceof JTabbedPane) {
            return ((JTabbedPane) paneHoldingScrollPanes).getSelectedIndex();
        }
        return activeView;
    }

    /**
     * create a new HTMLDocument and attach it to the editor
     */
    public void createNewDocument() {
        try {
            final SHTMLEditorKit kit = (SHTMLEditorKit) editorPane.getEditorKit();
            final SHTMLDocument doc = (SHTMLDocument) kit.createDefaultDocument();
            doc.addDocumentListener(this); // listen to changes
            docTempDir = new File(SHTMLPanelImpl.getAppTempDir().getAbsolutePath() + File.separator + getDocumentName()
            + File.separator);
            URL tempDocumentUrl = new URL(docTempDir.toURL(), getDocumentName() + ".htm");
            doc.setBase(tempDocumentUrl);
            editorPane.setDocument(doc); // let the document be edited in our editor
            updateFileName();
            final boolean useStyle = Util.useSteStyleSheet();
            if (useStyle) {
                doc.insertStyleRef();
            }
        }
        catch (final Exception e) {
            Util.errMsg(this, e.getMessage(), e);
        }
    }

    public void setDocument(final Document docToSet) {
        try {
            editorPane.getEditorKit();
            final HTMLDocument doc = getDocument();
            if (doc != null) {
                doc.removeDocumentListener(this);
            }
            docToSet.addDocumentListener(this); // listen to changes
            editorPane.setDocument(docToSet); // let the document be edited in our editor
        }
        catch (final Exception e) {
            Util.errMsg(this, e.getMessage(), e);
        }
    }

    /**
     * remove the temporary storage created for this <code>DocumentPane</code>
     */
    public void deleteTempDir() {
        if (docTempDir != null) {
            Util.deleteDir(docTempDir);
            docTempDir = null;
        }
    }

    /**
     * load a document found at a certain URL.
     *
     * @param url the URL to look for the document
     */
    public void loadDocument(final URL url) {
        try {
            final SHTMLEditorKit kit = (SHTMLEditorKit) editorPane.getEditorKit();
            final SHTMLDocument doc = (SHTMLDocument) kit.createDefaultDocument();
            doc.putProperty("IgnoreCharsetDirective", TRUE);
            doc.setBase(url); // set the doc base
            try (final InputStream in = url.openStream()){
                kit.read(in, doc, 0); // ..and read the document contents from it
            }
            doc.addDocumentListener(this); // listen to changes
            editorPane.setDocument(doc); // let the document be edited in our editor
            updateFileName(); // remember where the document came from
            loadedFromFile = true;
        }
        catch (final Exception ex) {
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
    private StyleSheet loadStyleSheet(final File cssFile) throws MalformedURLException, IOException {
        final StyleSheet s = new StyleSheet();
        s.importStyleSheet(cssFile.toURL());
        return s;
    }

    /**
     * saves the document to the file specified in the source of the
     * <code>DocumentPane</code> and creates the associated style sheet.
     * The actual save process only is done, when there is a name to save
     * to. The class(es) calling this method have to make sure that a
     * name for new documents is requested from the user, for instance.
     * The desired name and location for the save need then to be set using method
     * setSource prior to a call to this method
     *
     */
    public void saveDocument(URL targetUrl) {
        if (!saveInProgress()) {
            saveThread = Thread.currentThread(); // store thread for saveInProgress
            saveSuccessful = false; // if something goes wrong, this remains false
            try {
                    /* write the HTML document */
                    if (getSelectedTab() == VIEW_TAB_HTML) {
                        editorPane.setText(sourceEditorPane.getText());
                    }
                    final SHTMLDocument doc = getDocument();
                    try (final OutputStream os = Files.newOutputStream(new File(targetUrl.getPath()).toPath());
                         final OutputStreamWriter osw = new OutputStreamWriter(os)) {
                    final SHTMLWriter hw = new SHTMLWriter(osw, doc);
                        hw.write();
                    }
                    /* write the style sheet */
                    if (doc.hasStyleRef()) {
                        saveStyleSheet(targetUrl);
                    }
                    /*
                      copy image directory,
                      if new document or saved from different location
                    */
                    saveImages(targetUrl);
                    /* clean up */
                    setDocumentChanged(false); // indicate no changes pending anymore after the save
                    getDocument().setBase(targetUrl); // set the doc base
                    updateFileName();
                    deleteTempDir();
                    saveSuccessful = true; // signal that saving was successful
            }
             catch (final Exception e) {
                Util.errMsg(this, "An exception occurred while saving the file", e);
            }
            saveThread = null;
        }
    }



    private File getImageDir() {
        return getDocument().getImageDirectory();
    }

    /**
     * save image files
     */
    private void saveImages(URL targetUrl) {
        final File srcDir = getImageDir();
        final File destDir = SHTMLDocument.getImageDirectory(targetUrl);
        try {
            if (srcDir.exists()) {
                final ExampleFileFilter filter = new ExampleFileFilter();
                filter.addExtension("gif");
                filter.addExtension("jpg");
                filter.addExtension("jpeg");
                final File[] imgFiles = srcDir.listFiles();
                for (int i = 0; i < imgFiles.length; i++) {
                    Util.copyFile(imgFiles[i],
                        new File(destDir.getAbsolutePath() + File.separator + imgFiles[i].getName()));
                }
            }
        }
        catch (final Exception e) {
            Util.errMsg(this, e.getMessage(), e);
        }
    }

    /**
     * indicates whether or not a save process is in progress
     *
     * @return true, if a save process is going on, else false
     */
    public boolean saveInProgress() {
        return saveThread != null;
    }

    /**
     * Saves the style sheet of this document to a CSS file.
     *
     * <p>With stage 8, saves a style sheet by merging it with the existing
     * one with the same name/location. Styles in this style sheet overwrite
     * styles in the already existing style sheet.</p>
     */
    public void saveStyleSheet(URL targetUrl) throws IOException {
        final SHTMLDocument doc = getDocument();
        final StyleSheet styles = doc.getStyleSheet();
        final URL styleSheetName = getStyleSheetName();
        if (styleSheetName != null) {
            final File styleSheetFile = new File(styleSheetName.getFile());
            if (!styleSheetFile.exists()) {
                // no styles present at save location, create new style sheet
                styleSheetFile.createNewFile();
            }
            else {
                if (loadedFromFile) {
                    if ((!getDocumentUrl().getPath().equals(targetUrl.getPath()))) {
                        /*
                            this style sheet was loaded from somewhere else and now is
                            being saved at a new location where a style sheet exists
                            having the same name --> merge
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
                        having the same name --> merge
                    */
                    mergeStyleSheets(loadStyleSheet(styleSheetFile), styles);
                }
            }
            try (final OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(styleSheetFile.toPath()))) {
                CSSWriter cssWriter = new CSSWriter(osw, styles);
                cssWriter.write();
            }
        }
    }

    /**
     * Merges two style sheets by adding all the rules found
     * in the source style sheet that are not contained
     * in the destination style sheet. Assumes the rules
     * of src and dest are already loaded.
     *
     * @param sourceStyleSheet  the source StyleSheet
     * @param destinationStyleSheet  the destination StyleSheet
     */
    private void mergeStyleSheets(final StyleSheet sourceStyleSheet, final StyleSheet destinationStyleSheet)
            throws IOException {
        String name;
        Object elem;
        final Vector srcNames = Util.getStyleNames(sourceStyleSheet);
        final Vector destNames = Util.getStyleNames(destinationStyleSheet);
        final StringWriter sw = new StringWriter();
        final StringBuffer buf = sw.getBuffer();
        final CSSWriter cssWriter = new CSSWriter(sw, null);
        for (int i = 0; i < srcNames.size(); i++) {
            elem = srcNames.get(i);
            name = elem.toString();
            if (!destNames.contains(elem)) {
                buf.delete(0, buf.length());
                cssWriter.writeRule(name, sourceStyleSheet.getStyle(name));
                destinationStyleSheet.removeStyle(name);
                destinationStyleSheet.addRule(buf.toString());
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
    private URL getStyleSheetName() throws MalformedURLException {
        final SHTMLDocument doc = getDocument();
        final String styleRef = doc.getStyleRef();
        if (styleRef != null) {
           return new URL(doc.getBase(), styleRef);
        }
        return null;
    }

    /**
     * get the name of the document of this pane.
     *
     * @return  the name of the document
     */
    public String getDocumentName() {
        String theName;
        if (docName == null || docName.isEmpty()) {
            theName = DEFAULT_DOC_NAME + " " + newDocNo;
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
        return isDocumentChanged();
    }

    private void updateFileName() {
        final String fName = getDocumentUrl().getPath();
        docName = fName.substring(fName.lastIndexOf("/") + 1);
        fireNameChanged();
    }

    /**
     * get the source this document can be loaded from
     *
     * @return the URL this document can be loaded from
     */
    public URL getSource() {
        return isNewDoc() ? null : getDocumentUrl();
    }

    private URL getDocumentUrl() {
        return getDocument().getBase();
    }

    /**
     * indicates whether or not this document was newly created and not saved so
     * far.
     *
     * @return true, if this is a new document that has not been saved so far
     */
    public boolean isNewDoc() {
        return docTempDir != null;
    }

    /**
     * get the document of this <code>DocumentPane</code>
     *
     * @return the <code>Document</code> of this <code>DocumentPane</code>
     */
    public SHTMLDocument getDocument() {
        return editorPane.getDocument();
    }

    HTMLDocument getHTMLDocument() {
        return getDocument();
    }

    /**
     * Switches between the rich text view and the source view, given
     * tabbed panes are not used. Has no corresponding action; calling
     * this method is up to the caller application of SimplyHTML; the
     * application should call the method of the same name available at
     * SHTMLPanel.
     */
    public void switchViews() {
        if (paneHoldingScrollPanes instanceof JTabbedPane) {
            return;
        }
        // [ Tabbed pane not used ]
        if (activeView == VIEW_TAB_LAYOUT) {
            setHTMLView();
            paneHoldingScrollPanes.remove(richViewScrollPane);
            paneHoldingScrollPanes.add(sourceViewScrollPane);
            activeView = VIEW_TAB_HTML;
        }
        else {
            setLayoutView();
            paneHoldingScrollPanes.remove(sourceViewScrollPane);
            paneHoldingScrollPanes.add(richViewScrollPane);
            activeView = VIEW_TAB_LAYOUT;
        }
    }

    /**
     * Switches the DocumentPane to HTML view.
     */
    private void setHTMLView() {
        try {
            editorPane.getDocument().removeDocumentListener(this);
            final StringWriter stringWriter = new StringWriter();
            if (isHtmlChanged()) {
                editorPane.getEditorKit().write(stringWriter, editorPane.getDocument(), 0,
                    editorPane.getDocument().getLength());
                String newText = stringWriter.toString();
                if (!Util.preferenceIsTrue("writeHead", "true")) {
                    newText = newText.replaceAll("(?ims)<head>.*?(<body)", "$1");
                }
                sourceEditorPane.setText(newText);
                setHtmlChanged(false);
            }
            sourceEditorPane.getDocument().addDocumentListener(this);
            sourceEditorPane.addCaretListener(sourceEditorPane);
            setHtmlChanged(false);
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Switches the DocumentPane to layout view.
     */
    private void setLayoutView() {
        sourceEditorPane.getDocument().removeDocumentListener(this);
        sourceEditorPane.removeCaretListener(sourceEditorPane);

        if (isHtmlSourceEdited()) {
            editorPane.setText(sourceEditorPane.getText());
            setHtmlChanged(false);
            setHtmlSourceEdited(false);
        }

        editorPane.setCaretPosition(0);
        editorPane.getDocument().addDocumentListener(this);
        editorPane.requestFocus();
    }

    /**
     * Convenience method for obtaining the document text
     * @return returns the document text as string.
     */
    String getDocumentText() {
        if (getSelectedTab() == VIEW_TAB_HTML && isHtmlSourceEdited()) {
            editorPane.setText(sourceEditorPane.getText());
            setHtmlSourceEdited(false);
        }
        return editorPane.getText();
    }

    /**
     * Convenience method for setting the document text
     */
    void setDocumentText(final String sText) {
        switch (getSelectedTab()) {
            case VIEW_TAB_LAYOUT:
                editorPane.getDocument().removeDocumentListener(this);
                editorPane.setText(sText);
                editorPane.getDocument().addDocumentListener(this);
                // Views are now out of sync - layout view has new content, HTML view has old content
                setHtmlChanged(true);
                setHtmlSourceEdited(false);
                break;
            case VIEW_TAB_HTML:
                sourceEditorPane.getDocument().removeDocumentListener(this);
                sourceEditorPane.setText(sText);
                sourceEditorPane.getDocument().addDocumentListener(this);
                // Views are synchronized - HTML source set directly, layout will render same content
                setHtmlChanged(false);
                setHtmlSourceEdited(true);
                break;
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                setDocumentChanged(false);
            }
        });
    }

    /* ----------------- changeListener implementation start ---------------------- */
    public void stateChanged(final ChangeEvent e) {
        final Object src = e.getSource();
        if (src.equals(paneHoldingScrollPanes)) {
            switch (getSelectedTab()) {
                case VIEW_TAB_LAYOUT:
                    setLayoutView();
                    break;
                case VIEW_TAB_HTML:
                    setHTMLView();
                    break;
            }
        }
        SHTMLPanelImpl.getOwnerSHTMLPanel(this).updateActions();
    }

    /* ----------------- changeListener implementation end ------------------------ */
    /* -------- DocumentListener implementation start ------------*/
    /**
     * listens to inserts into the document to track whether or not the document
     * needs to be saved.
     */
    public void insertUpdate(final DocumentEvent e) {
        setDocumentChanged();
    }

    /**
     * listens to removes from the document to track whether or not the document
     * needs to be saved.
     */
    public void removeUpdate(final DocumentEvent e) {
        setDocumentChanged();
    }

    /**
     * listens to changes on the document to track whether or not the document
     * needs to be saved.
     */
    public void changedUpdate(final DocumentEvent e) {
        // changedUpdate is called for attribute changes (like syntax highlighting)
        // Only treat as real content change if we're in layout view
        if (getSelectedTab() == VIEW_TAB_LAYOUT) {
            editorPane.updateInputAttributes();
            setDocumentChanged();
        }
        // Ignore changedUpdate in HTML view - it's just syntax highlighting
    }

	private void setDocumentChanged() {
		if (getSelectedTab() == VIEW_TAB_LAYOUT) {
            setHtmlChanged(true);
        } else if (getSelectedTab() == VIEW_TAB_HTML) {
            setHtmlSourceEdited(true);
        }
        setDocumentChanged(true);
	}

    /* -------- DocumentListener implementation end ------------*/
    /* -------- DocumentPaneListener definition start --------------- */
    /**
     * interface to be implemented for being notified of
     * changes to the name of this document
     */
    public interface DocumentPaneListener {
        void nameChanged(DocumentPaneEvent e);

        void activated(DocumentPaneEvent e);
    }

    /** the event object definition for DocumentPaneEvents */
    static class DocumentPaneEvent extends EventObject {
        public DocumentPaneEvent(final Object source) {
            super(source);
        }
    }

    /** listeners for DocumentPaneEvents */
    private final Vector dpListeners = new Vector();
    private File docTempDir;

    /**
     * add a DocumentPaneListener to this Document
     *
     * @param listener the listener object to add
     */
    public void addDocumentPaneListener(final DocumentPaneListener listener) {
        if (!dpListeners.contains(listener)) {
            dpListeners.addElement(listener);
        }
    }

    /**
     * remove a DocumentPaneListener from this Document
     *
     * @param listener  the listener object to remove
     */
    public void removeDocumentPaneListener(final DocumentPaneListener listener) {
        dpListeners.remove(listener);
    }

    /**
     * fire a DocumentPaneEvent to all registered DocumentPaneListeners
     */
    public void fireNameChanged() {
        final Enumeration listenerList = dpListeners.elements();
        while (listenerList.hasMoreElements()) {
            ((DocumentPaneListener) listenerList.nextElement()).nameChanged(new DocumentPaneEvent(this));
        }
    }

    /**
     * fire a DocumentPaneEvent to all registered DocumentPaneListeners
     */
    public void fireActivated() {
        final Enumeration listenerList = dpListeners.elements();
        while (listenerList.hasMoreElements()) {
            ((DocumentPaneListener) listenerList.nextElement()).activated(new DocumentPaneEvent(this));
        }
    }

    /**
     * remove all listeners
     */
    public void removeAllListeners() {
        dpListeners.clear();
    }

    public JEditorPane getMostRecentFocusOwner() {
        switch (getSelectedTab()) {
            case VIEW_TAB_LAYOUT:
                return editorPane;
            case VIEW_TAB_HTML:
                return sourceEditorPane;
        }
        return null;
    }

    public void setContentPanePreferredSize(final Dimension prefSize) {
        setPreferredSize(null);
        paneHoldingScrollPanes.setPreferredSize(null);
        for (int i = 0; i < paneHoldingScrollPanes.getComponentCount(); i++) {
            final JScrollPane scrollPane = (JScrollPane) paneHoldingScrollPanes.getComponent(i);
            scrollPane.setPreferredSize(prefSize);
            scrollPane.invalidate();
        }
    }
    /* -------- DocumentPaneListener definition end --------------- */
}
