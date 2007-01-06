package com.lightdev.app.shtm;

import javax.swing.text.*;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.html.*;

import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;


/**
 * FixedHTMLWriter
 *
 * 
 */

class SHTMLWriter extends HTMLWriter {
    private Element elem;

    /**
     * If true, the writer will emit CSS attributes in preference
     * to HTML tags/attributes (i.e. It will emit an HTML 4.0
     * style).
     */
    private boolean writeCSS;

    final private MutableAttributeSet oConvAttr  = new SimpleAttributeSet();

    final private MutableAttributeSet convAttr  = new SimpleAttributeSet();
    
    public SHTMLWriter(Writer w, HTMLDocument doc, int pos, int len) {
        super(w, doc, pos, len);
        String writeMode = Util.getWriteMode();
        if(writeMode.equalsIgnoreCase(PrefsDialog.PREFS_WRITE_MODE_HTML4)) {
            writeCSS = true;
        }
        else{
            writeCSS = false;
        }
    }

    public SHTMLWriter(Writer w, HTMLDocument doc) {
        this(w, doc,0 , doc.getLength());
     }

    protected ElementIterator getElementIterator() {
        if(elem == null)
            return super.getElementIterator();
        return new ElementIterator(elem);
    }

    /**
     * Iterates over the
     * Element tree and controls the writing out of
     * all the tags and its attributes.
     *
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *            location within the document.
     *
     */
    synchronized public void write(Element elem) throws IOException, BadLocationException {
        boolean writeCSSBackUp = writeCSS;
        writeCSS = false;
        this.elem = elem;
        try{
            write();
        }
        catch(BadLocationException e){
            elem = null;
            throw e;
        }
        catch(IOException e){
            elem = null;
            throw e;
        }
        finally{
            writeCSS = writeCSSBackUp; 
        }
    }
    /**
     * invoke HTML creation for all children of a given element.
     *
     * @param elem  the element which children are to be written as HTML
     */
    public void writeChildElements(Element elem)
        throws IOException, BadLocationException
    {
      Element para;
      for(int i = 0; i < elem.getElementCount(); i++) {
        para = elem.getElement(i);
        write(para);
      }
    }

    protected boolean inRange(Element next) {
        if(next.getStartOffset() >= ((SHTMLDocument)next.getDocument()).getLastDocumentPosition()){
            return false;
        }
        int startOffset = getStartOffset();
        int endOffset = getEndOffset();
        if ((next.getStartOffset() >= startOffset 
               && (next.getStartOffset()  < endOffset) || next.getEndOffset() - 1 == endOffset) 
          || (startOffset >= next.getStartOffset() && startOffset < next.getEndOffset())) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLWriter#endTag(javax.swing.text.Element)
     */
    public void endTag(Element elem) throws IOException {
        super.endTag(elem);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLWriter#startTag(javax.swing.text.Element)
     */
    public void startTag(Element elem) throws IOException, BadLocationException {
        super.startTag(elem);
    }

    /**
     * write an element and all its children. If a given element is reached,
     * writing stops with this element. If the end element is a leaf,
     * it is written as the last element, otherwise it is not written.
     *
     * @param e  the element to write including its children (if any)
     * @param end  the last leaf element to write or the branch element
     * to stop writing at (whatever applies)
     */
    public void writeElementsUntil(Element e, Element end) throws IOException, BadLocationException
    {
      if(e.isLeaf()) {
        write(e);
      }
      else {
        if(e != end) {
          startTag(e);
          int childCount = e.getElementCount();
          int index = 0;
          while(index < childCount) {
            writeElementsUntil(e.getElement(index), end); // drill down in recursion
            index++;
          }
          endTag(e);
        }
      }
    }

    /**
     * write elements and their children starting at a
     * given element until a given element is reached.
     * The end element is written as the last element,
     * if it is a leaf element.
     *
     * @param start  the element to start writing with
     * @param end  the last element to write
     */
    public void write(Element start, Element end) throws IOException, BadLocationException
    {
      Element parent = start.getParentElement();
      int count = parent.getElementCount();
      int i = 0;
      Element e = parent.getElement(i);
      while(i < count && e != start) {
        e = parent.getElement(i++);
      }
      while(i < count) {
        writeElementsUntil(e, end);
        e = parent.getElement(i++);
      }
    }

    public void endTag(String elementName) throws IOException{
        indent();
        write('<');
        write('/');
        write(elementName);
        write('>');
        writeLineSeparator();
    }

    public void startTag(String elementName, AttributeSet attributes) throws IOException{
        indent();
        write('<');
        write(elementName);
        if(attributes != null){
            writeAttributes(attributes);
        }
        write('>');
        writeLineSeparator();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLWriter#closeOutUnwantedEmbeddedTags(javax.swing.text.AttributeSet)
     */
    protected void closeOutUnwantedEmbeddedTags(AttributeSet attr) throws IOException {
        if(writeCSS){            
            convertToHTML40(attr, convAttr);
            super.closeOutUnwantedEmbeddedTags(convAttr);
        }
        else{
            super.closeOutUnwantedEmbeddedTags(attr);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLWriter#writeAttributes(javax.swing.text.AttributeSet)
     */
    protected void writeAttributes(AttributeSet attr) throws IOException {
        if(writeCSS){            
            convertToHTML40(attr, convAttr);
            super.writeAttributes(convAttr);
        }
        else{
            super.writeAttributes(attr);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLWriter#writeEmbeddedTags(javax.swing.text.AttributeSet)
     */
    protected void writeEmbeddedTags(AttributeSet attr) throws IOException {
        if(writeCSS){            
            convertToHTML40(attr, oConvAttr);
            super.writeEmbeddedTags(oConvAttr);
        }
        else{
            super.writeEmbeddedTags(attr);
        }
    }

    private void convertToHTML40(AttributeSet from, MutableAttributeSet to) {
        if( from == to){
            return;
        }
        if(from == null){
            to = null;
            return;
        }
        to.removeAttributes(to);
        Enumeration keys = from.getAttributeNames();
        String value = "";
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            String attribute = from.getAttribute(key).toString();
            if (key instanceof CSS.Attribute) {
                // default is to store in a HTML style attribute
                if (attribute.length() > 0) {
                    if(key == CSS.Attribute.FONT_SIZE) {
                        int fontNumber = Integer.parseInt(attribute); 
                        attribute = SHTMLPanelImpl.FONT_SIZES[fontNumber-1] + "pt";
                    }
                    value = value + "; ";
                }
                value = value + key + ": " + attribute;
            } else {
                to.addAttribute(key, attribute);
            }
        }
        if (value.length() > 0) {
            MutableAttributeSet spanAttr = (MutableAttributeSet) 
            to.getAttribute(HTML.Tag.SPAN);
            if (spanAttr == null) {
                spanAttr = new SimpleAttributeSet();
                to.addAttribute(HTML.Tag.SPAN, spanAttr);
            }
            spanAttr.addAttribute(HTML.Attribute.STYLE, value);
        }
    }
    
}