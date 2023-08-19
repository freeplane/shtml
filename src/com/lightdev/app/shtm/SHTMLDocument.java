/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 * Copyright (C) 2006 Dimitri Polivaev
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

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.stream.Stream;

import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit.Parser;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

/**
 * Extends <code>HTMLDocument</code> by a custom reader which supports
 * the SPAN tag.
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
public class SHTMLDocument extends HTMLDocument {
    static final Parser defaultParser = new ParserDelegator() {
        @Override
        public void parse(Reader r, ParserCallback cb, boolean ignoreCharSet) throws IOException {
            setDefaultDTD();
            DocumentParser documentParser = new SHTMLDocumentParser(DTD.getDTD("html32"));
            documentParser.parse(r, cb, ignoreCharSet);
        }
    };
	public static final String SUFFIX = "&nbsp;";
    private CompoundEdit compoundEdit;
    private int compoundEditDepth;
    private boolean inSetParagraphAttributes = false;
    private CopiedImageSources copiedExternalImagesSources = CopiedImageSources.NONE;
    private int suffixLength = 0;

    /**
     * Constructs an SHTMLDocument.
     */
    public SHTMLDocument() {
        this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleSheet());
    }

    /**
     * Constructs an SHTMLDocument with the default content
     * storage implementation and the given style/attribute
     * storage mechanism.
     *
     * @param styles  the styles
     */
    public SHTMLDocument(final StyleSheet styles) {
        this(new GapContent(BUFFER_SIZE_DEFAULT), styles);
    }

    /**
     * Constructs an SHTMLDocument with the given content
     * storage implementation and the given style/attribute
     * storage mechanism.
     *
     * @param c  the container for the content
     * @param styles the styles
     */
    public SHTMLDocument(final Content c, final StyleSheet styles) {
        super(c, styles);
        compoundEdit = null;
        setParser(defaultParser);
    }

    /**
     * Creates the root element to be used to represent the
     * default document structure.
     *
     * @return the element base
     */
    protected AbstractElement createDefaultRoot() {
        writeLock();
        MutableAttributeSet a = new SimpleAttributeSet();
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.HTML);
        BlockElement html = new BlockElement(null, a.copyAttributes());
        a.removeAttributes(a);
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.BODY);
        BlockElement body = new BlockElement(html, a.copyAttributes());
        a.removeAttributes(a);
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
//        getStyleSheet().addCSSAttributeFromHTML(a, CSS.Attribute.MARGIN_TOP, "0");
        BlockElement paragraph = new BlockElement(body, a.copyAttributes());
        a.removeAttributes(a);
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
        RunElement brk = new RunElement(paragraph, a, 0, 1);
        Element[] buff = new Element[1];
        buff[0] = brk;
        paragraph.replace(0, 0, buff);
        buff[0] = paragraph;
        body.replace(0, 0, buff);
        buff[0] = body;
        html.replace(0, 0, buff);
        writeUnlock();
        return html;
    }


    void setSuffix(String suffix) {
        try {
            Element root = getDefaultRootElement();
            Element body = root.getElement(root.getElementCount() - 1);
            super.insertBeforeEnd(body, suffix);
            Element suffixParagraph = body.getElement(body.getElementCount() - 1);
            suffixLength = getLength() - suffixParagraph.getStartOffset();
        }
        catch (final BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }


 	/**
     * apply a set of attributes to a given document element
     *
     * @param e  the element to apply attributes to
     * @param a  the set of attributes to apply
     */
    public void addAttributes(final Element e, final AttributeSet a) {
        if ((e != null) && (a != null)) {
            try {
                writeLock();
                //System.out.println("SHTMLDocument addAttributes e=" + e);
                //System.out.println("SHTMLDocument addAttributes a=" + a);
                final int start = e.getStartOffset();
                final DefaultDocumentEvent changes = new DefaultDocumentEvent(start, e.getEndOffset() - start,
                    DocumentEvent.EventType.CHANGE);
                final AttributeSet sCopy = a.copyAttributes();
                final MutableAttributeSet attr = (MutableAttributeSet) e.getAttributes();
                changes.addEdit(new AttributeUndoableEdit(e, sCopy, false));
                attr.addAttributes(a);
                changes.end();
                fireChangedUpdate(changes);
                fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
            }
            finally {
                writeUnlock();
            }
        }
    }

    /**
     * Removes a consecutive group of child elements.
     *
     * @param element  the parent element to remove child elements from
     * @param index  the index of the first child element to remove
     * @param count  the number of child elements to remove
     */
    public void removeElements(final Element element, final int index, final int count) throws BadLocationException {
        writeLock();
        final int start = element.getElement(index).getStartOffset();
        final int end = element.getElement(index + count - 1).getEndOffset();
        try {
            final Element[] removed = new Element[count];
            final Element[] added = new Element[0];
            for (int counter = 0; counter < count; counter++) {
                removed[counter] = element.getElement(counter + index);
            }
            final DefaultDocumentEvent defaultDocumentEvent = new DefaultDocumentEvent(start, end - start,
                DocumentEvent.EventType.REMOVE);
            ((AbstractDocument.BranchElement) element).replace(index, removed.length, added);
            defaultDocumentEvent.addEdit(new ElementEdit(element, index, removed, added));
            final UndoableEdit undoableEdit = getContent().remove(start, end - start);
            if (undoableEdit != null) {
                defaultDocumentEvent.addEdit(undoableEdit);
            }
            postRemoveUpdate(defaultDocumentEvent);
            defaultDocumentEvent.end();
            fireRemoveUpdate(defaultDocumentEvent);
            if (undoableEdit != null) {
                fireUndoableEditUpdate(new UndoableEditEvent(this, defaultDocumentEvent));
            }
        }
        finally {
            writeUnlock();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLDocument#setOuterHTML(javax.swing.text.Element, java.lang.String)
     */
    public void setOuterHTML(final Element paragraphElement, final String htmlText) throws BadLocationException,
            IOException {
        try {
            startCompoundEdit();
            if (paragraphElement.getName().equalsIgnoreCase("p-implied")) {
                //What has to be replaced is the HTML of the parent of this implied element.
                final Element parentElement = paragraphElement.getParentElement();
                final SHTMLWriter writer = new SHTMLWriter(this);
                final int indexOfElement = parentElement.getElementIndex(paragraphElement.getStartOffset());
                writer.writeStartTag(parentElement);
                for (int i = 0; i < indexOfElement; i++) {
                    writer.write(parentElement.getElement(i));
                }
                writer.write(htmlText);
                for (int i = indexOfElement + 1; i < parentElement.getElementCount(); i++) {
                    writer.write(parentElement.getElement(i));
                }
                writer.writeEndTag(parentElement);
                super.setOuterHTML(parentElement, writer.toString());
            }
            else {
                super.setOuterHTML(paragraphElement, htmlText);
            }
        }
        finally {
            endCompoundEdit();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLDocument#insertAfterEnd(javax.swing.text.Element, java.lang.String)
     */
    public void insertAfterEnd(final Element elem, final String htmlText) throws BadLocationException, IOException {
        try {
            startCompoundEdit();
            super.insertAfterEnd(elem, htmlText);
        }
        finally {
            endCompoundEdit();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLDocument#insertAfterStart(javax.swing.text.Element, java.lang.String)
     */
    public void insertAfterStart(final Element elem, final String htmlText) throws BadLocationException, IOException {
        try {
            startCompoundEdit();
            super.insertAfterStart(elem, htmlText);
        }
        finally {
            endCompoundEdit();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLDocument#insertBeforeEnd(javax.swing.text.Element, java.lang.String)
     */
    public void insertBeforeEnd(final Element elem, final String htmlText) throws BadLocationException, IOException {
        try {
            startCompoundEdit();
            super.insertBeforeEnd(elem, htmlText);
        }
        finally {
            endCompoundEdit();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLDocument#insertBeforeStart(javax.swing.text.Element, java.lang.String)
     */
    public void insertBeforeStart(final Element elem, final String htmlText) throws BadLocationException, IOException {
        try {
            startCompoundEdit();
            super.insertBeforeStart(elem, htmlText);
        }
        finally {
            endCompoundEdit();
        }
    }

    @FunctionalInterface
    public interface  ThrowingRunnable<T extends Exception> {
        void run() throws T;
    }

    public <T extends Exception> void copyingExternalImages(CopiedImageSources copiedExternalImagesSources, ThrowingRunnable<T> runnable) throws T{
        CopiedImageSources copiedExternalImagesSourcesBackup = this.copiedExternalImagesSources;
        this.copiedExternalImagesSources = copiedExternalImagesSources;
        try {
            runnable.run();
        }
        finally {
        	this.copiedExternalImagesSources = copiedExternalImagesSourcesBackup;
        }
    }

    @Override
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
        copyExternalImages(data);
        super.insert(offset, data);
    }

    private void copyExternalImages(ElementSpec[] data) {
        if(copiedExternalImagesSources == CopiedImageSources.NONE)
            return;
        URL base = getBase();
        if(base == null || ! base.getProtocol().equalsIgnoreCase("file"))
            return;
        try {
            Stream.of(data).forEach(this::copyExternalImagesForElementSpec);
        }
        catch (final UnknownDocumentBaseException e) {
            Util.errMsg(null, e.getMessage(), null);
        }

    }

    private void copyExternalImagesForElementSpec(ElementSpec data) {
        AttributeSet attributes = data.getAttributes();
        if(!(attributes instanceof MutableAttributeSet) || !HTML.Tag.IMG.equals(attributes.getAttribute(StyleConstants.NameAttribute))) {
         return;
        }
        final String source = (String) attributes.getAttribute(HTML.Attribute.SRC);
        if(! copiedExternalImagesSources.includes(source))
            return;
        try {
            URL sourceUrl = new URL(getBase(), source);
            File imageDirectory = getImageDirectory().getCanonicalFile();
            if(sourceUrl.getProtocol().equalsIgnoreCase("file")) {
                File sourceFile = new File(sourceUrl.getPath()).getCanonicalFile();
                String basePath = imageDirectory.getPath() + File.separatorChar;
                if(sourceFile.getPath().startsWith(basePath)) {
                    String updatedSource = sourceFile.getPath().substring(basePath.length()).replace(File.separatorChar, '/');
                    if(! updatedSource.equals(source))
                        ((MutableAttributeSet)attributes).addAttribute(HTML.Attribute.SRC, updatedSource);
                    return;
                }
            }
            URLConnection connection = sourceUrl.openConnection();
            
            String contentType = connection.getContentType();
            String imageExtension = getExtensionFromContentType(contentType);
            if(imageExtension == null)
            	return;

            imageDirectory.mkdirs();
            File imageCopy = File.createTempFile("image-", "." + imageExtension, imageDirectory);
            try(
                    ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                    FileOutputStream fos = new FileOutputStream(imageCopy)){
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                ((MutableAttributeSet)attributes).addAttribute(HTML.Attribute.SRC, imageDirectory.getName() + '/' + imageCopy.getName());
            }
        }
        catch (UnknownDocumentBaseException e) {
            ((MutableAttributeSet)attributes).addAttribute(HTML.Attribute.SRC, "");
            throw e;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private static String getExtensionFromContentType(String contentType) {
        if (contentType != null && contentType.toLowerCase().startsWith("image/")) {
            return contentType.substring("image/".length()).toLowerCase();
        }
        return null;
    }

    /** */
    public void replaceHTML(final Element firstElement, final int number, final String htmlText)
            throws BadLocationException, IOException {
        if (number > 1) {
            if (firstElement != null && firstElement.getParentElement() != null && htmlText != null) {
                final int start = firstElement.getStartOffset();
                final Element parent = firstElement.getParentElement();
                final int removeIndex = parent.getElementIndex(start);
                try {
                    startCompoundEdit();
                    removeElements(parent, removeIndex, number - 1);
                    setOuterHTML(parent.getElement(removeIndex), htmlText);
                }
                finally {
                    endCompoundEdit();
                }
            }
        }
        else if (number == 1) {
            setOuterHTML(firstElement, htmlText);
        }
    }

    public void startCompoundEdit() {
        compoundEditDepth++;
    }

    public void endCompoundEdit() {
        if (compoundEditDepth != 0) {
            compoundEditDepth--;
            if (compoundEditDepth == 0 && compoundEdit != null) {
                compoundEdit.end();
                super.fireUndoableEditUpdate(new UndoableEditEvent(this, compoundEdit));
                compoundEdit = null;
            }
        }
    }

    protected void fireUndoableEditUpdate(final UndoableEditEvent e) {
        if (compoundEditDepth == 0) {
            super.fireUndoableEditUpdate(e);
        }
        else {
            if (compoundEdit == null) {
                compoundEdit = new CompoundEdit();
            }
            compoundEdit.addEdit(e.getEdit());
        }
    }

    /* ------------------ custom document title handling start -------------------- */
    /**
     * set the title of this SHTMLDocument
     *
     * @param title  the title this document shall have
     */
    public void setDocumentTitle(final String title) {
        try {
            final String titleHTML = "<title></title>";
            final Element defaultRoot = getDefaultRootElement();
            final Element head = Util.findElementDown(HTML.Tag.HEAD.toString(), defaultRoot);
            if (head != null) {
                final Element pImpl = Util.findElementDown(HTML.Tag.IMPLIED.toString(), head);
                if (pImpl != null) {
                    final Element tElem = Util.findElementDown(HTML.Tag.TITLE.toString(), pImpl);
                    if (tElem == null) {
                        insertBeforeEnd(pImpl, titleHTML);
                    }
                }
            }
            else {
                final Element body = Util.findElementDown(HTML.Tag.BODY.toString(), defaultRoot);
                insertBeforeStart(body, "<head>" + titleHTML + "</head>");
            }
            putProperty(Document.TitleProperty, title);
        }
        catch (final Exception e) {
            Util.errMsg(null, "An exception occurred while trying to insert the title", e);
        }
    }

    /**
     * get the title of this SHTMLDocument
     *
     * @return  the title of this document or null if none was set so far
     */
    public String getDocumentTitle() {
        final Object title = getProperty(Document.TitleProperty);
        if (title != null) {
            return title.toString();
        }
        else {
            return null;
        }
    }

    /* ------------------ custom document title handling end -------------------- */
    /* ------------------ custom style sheet reference handling start -------------------- */
    /**
     * insert a style sheet reference into the head of this SHTMLDocument
     */
    public void insertStyleRef() {
        try {
            final String styleRef = "  <link rel=stylesheet type=\"text/css\" href=\""
                    + DocumentPane.DEFAULT_STYLE_SHEET_NAME + "\">";
            final Element defaultRoot = getDefaultRootElement();
            final Element head = Util.findElementDown(HTML.Tag.HEAD.toString(), defaultRoot);
            if (head != null) {
                final Element pImpl = Util.findElementDown(HTML.Tag.IMPLIED.toString(), head);
                if (pImpl != null) {
                    final Element link = Util.findElementDown(HTML.Tag.LINK.toString(), pImpl);
                    if (link != null) {
                        setOuterHTML(link, styleRef);
                    }
                    else {
                        insertBeforeEnd(pImpl, styleRef);
                    }
                }
            }
            else {
                final Element body = Util.findElementDown(HTML.Tag.BODY.toString(), defaultRoot);
                insertBeforeStart(body, "<head>" + styleRef + "</head>");
            }
        }
        catch (final Exception e) {
            Util.errMsg(null, "An exception occurred while trying to insert the style sheet reference link", e);
        }
    }

    /**
     * check whether or not this SHTMLDocument has an explicit style sheet reference
     *
     * @return true, if a style sheet reference was found, false if not
     */
    public boolean hasStyleRef() {
        return (getStyleRef() != null);
    }

    /**
     * get the style sheet reference of the document in this
     * <code>DocumentPane</code>.
     *
     * @return the reference to this document's style sheet or
     *    null if none is found
     */
    public String getStyleRef() {
        String linkName = null;
        final Element link = Util.findElementDown(HTML.Tag.LINK.toString(), getDefaultRootElement());
        if (link != null) {
            final Object href = link.getAttributes().getAttribute(HTML.Attribute.HREF);
            if (href != null) {
                linkName = href.toString();
            }
        }
        return linkName;
    }

    /* ------------------ custom style sheet reference handling end -------------------- */
    public Element getParagraphElement(final int pos) {
        return getParagraphElement(pos, inSetParagraphAttributes);
    }

    /** Gets the current paragraph element, retracing out of p-implied if the parameter
     * noImplied is true.
     * @see javax.swing.text.DefaultStyledDocument#getParagraphElement(int)
     */
    public Element getParagraphElement(final int pos, final boolean noPImplied) {
        Element element = super.getParagraphElement(pos);
        if (noPImplied) {
            while (element != null && element.getName().equalsIgnoreCase("p-implied")) {
                element = element.getParentElement();
            }
        }
        return element;
    }

    public int getLastDocumentPosition() {
        final int length = getLength();
        return length - suffixLength;
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLDocument#setParagraphAttributes(int, int, javax.swing.text.AttributeSet, boolean)
     */
    public void setParagraphAttributes(final int offset, final int length, final AttributeSet s, final boolean replace) {
        startCompoundEdit();
        super.setParagraphAttributes(offset, length, s, replace);
        inSetParagraphAttributes = true;
        super.setParagraphAttributes(offset, length, s, replace);
        inSetParagraphAttributes = false;
        endCompoundEdit();
    }

    public void removeParagraphAttributes(final int offset, final int length) {
        startCompoundEdit();
        // clear all paragraph attributes in selection
        for (int i = offset; i < offset + length;) {
            final Element paragraphElement = super.getParagraphElement(i);
            removeParagraphAtributes(paragraphElement);
            i = paragraphElement.getEndOffset();
        }
        endCompoundEdit();
    }

    private void removeParagraphAtributes(final Element paragraphElement) {
        if (paragraphElement != null && paragraphElement.getName().equalsIgnoreCase("p-implied")) {
            removeParagraphAtributes(paragraphElement.getParentElement());
            return;
        }
        final StringWriter writer = new StringWriter();
        final SHTMLWriter htmlStartWriter = new SHTMLWriter(writer, this);
        try {
            htmlStartWriter.writeStartTag(paragraphElement.getName(), null);
            htmlStartWriter.writeChildElements(paragraphElement);
            htmlStartWriter.writeEndTag(paragraphElement.getName());
            setOuterHTML(paragraphElement, writer.toString());
        }
        catch (final IOException | BadLocationException e) {
            e.printStackTrace();
        }
    }

    private SimpleAttributeSet getEndingAttributeSet() {
        final SimpleAttributeSet set = new SimpleAttributeSet();
        if (Util.preferenceIsTrue("gray_row_below_end")) {
            StyleConstants.setBackground(set, Color.GRAY);
        }
        return set;
    }

    public File getImageDirectory() {
        return SHTMLDocument.getImageDirectory(getBase());
    }

    public static File getImageDirectory(URL base) {
        try {
            return new File(new URL(base, getImageDirectoryName(base)).getPath());
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getImageDirectoryName() {
        return SHTMLDocument.getImageDirectoryName(getBase());
    }

    public static String getImageDirectoryName(URL documentUrl) {
        if(documentUrl == null)
            throw new UnknownDocumentBaseException(Util.getResourceString("unknownBaseUrlImageInsertionError"));
        String path = documentUrl.getPath();
        int filenameStart = path.lastIndexOf('/') + 1;
        int filenameEnd = path.lastIndexOf('.');
        if(filenameEnd < filenameStart)
            filenameEnd = path.length();
        if(filenameStart < filenameEnd)
            return path.substring(filenameStart, filenameEnd) + "_files";
        else
            throw new UnknownDocumentBaseException(Util.getResourceString("unknownBaseUrlImageInsertionError"));
    }

}
