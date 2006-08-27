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
 * @version stage 12, August 06, 2006
 */

public class SHTMLWriter extends HTMLWriter {
    private Element elem;

    public SHTMLWriter(Writer w, HTMLDocument doc, int pos, int len) {
        super(w, doc, pos, len);
    }

    public SHTMLWriter(Writer w, HTMLDocument doc) {
        super(w, doc,0 , doc.getLength());
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
        return next.getStartOffset() < next.getDocument().getLength()-5 && super.inRange(next);
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


}