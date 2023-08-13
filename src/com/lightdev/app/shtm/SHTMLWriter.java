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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;

/**
 * FixedHTMLWriter
 *
 *
 */
public class SHTMLWriter extends HTMLWriter {
    private static final char NB_SPACE = '\u00A0';
	private Element element;
    private Writer writer = null;
    private boolean replaceEntities;
    private boolean inTextArea;
    private int inPreLevel=0;
	private char[] tempChars;

    public SHTMLWriter(final Writer w, final HTMLDocument doc, final int pos, final int len) {
        super(w, doc, pos, len);
        writer = w;
    }

    /** Constructs the SHTMLWriter with a new StringWriter. See also the method
     * getWrittenString. */
    public SHTMLWriter(final HTMLDocument doc) {
        this(new StringWriter(), doc);
    }

    public SHTMLWriter(final Writer w, final HTMLDocument doc) {
        this(w, doc, 0, (doc instanceof SHTMLDocument) ? ((SHTMLDocument)doc).getLastDocumentPosition() : doc.getLength());
    }

    @Override
    protected ElementIterator getElementIterator() {
        if (element == null) {
            return super.getElementIterator();
        }
        return new ElementIterator(element);
    }

    @SuppressWarnings("serial")
	private final static Map<Character, String> HTML_CHAR_ENTITIES = new HashMap<Character, String>(){{
    	put('<', "&lt;");
    	put('>', "&gt;");
    	put('&', "&amp;");
    	put('"', "&quot;");
    	put('<', "&lt;");
    	put(NB_SPACE, "&nbsp;");
    }};

    @Override
    protected void output(char[] chars, int start, int length) throws IOException {
    	if (!replaceEntities) {
    		directOutput(chars, start, length);
    		return;
    	}

    	if(inPreLevel == 0)
    	    replaceMultipleSpacesByNonBreakingSpaces(chars, start, length);

        int last = start;
        length += start;
        for (int counter = start; counter < length; counter++) {
        	char c = chars[counter];
        	String replacement = entity(c);
			if(replacement != null) {
				directOutput(chars, last, counter - last);
				directOutput(replacement);
				last = counter + 1;
			}
        }

        if (last < length) {
        	directOutput(chars, last, length - last);
        }
    }

    protected String entity(char c) {
    	if (c < ' ')
    		return "&#x" + Integer.toHexString(c) + ';';
    	String knownEntity = HTML_CHAR_ENTITIES.get(c);
		return knownEntity;
    }

	private void replaceMultipleSpacesByNonBreakingSpaces(char[] chars, int start, int length) {
		if (chars[start] == ' ') {
		    chars[start] = NB_SPACE;
		}
		final int last = start + length - 1;
		for (int i = start + 1; i < last; i++) {
		    if (chars[i] == ' ' && (chars[i - 1] == NB_SPACE || chars[i + 1] == ' ')) {
		        chars[i] = NB_SPACE;
		    }
		}
	}

    private void directOutput(char[] content, int start, int length)
            throws IOException {
    	getWriter().write(content, start, length);
    	setCurrentLineLength(getCurrentLineLength() + length);
    }

    private void directOutput(String string) throws IOException {
        int length = string.length();
        if (tempChars == null || tempChars.length < length) {
            tempChars = new char[length];
        }
        string.getChars(0, length, tempChars, 0);
        directOutput(tempChars, 0, length);
    }

    @Override
    protected void startTag(final Element elem) throws IOException, BadLocationException {
        if (matchNameAttribute(elem.getAttributes(), HTML.Tag.PRE)) {
            inPreLevel++;
        }
        super.startTag(elem);
    }

    @Override
    protected void endTag(final Element elem) throws IOException {
        if (matchNameAttribute(elem.getAttributes(), HTML.Tag.PRE)) {
            inPreLevel--;
        }
        super.endTag(elem);
    }

    @Override
    protected void text(final Element elem) throws BadLocationException, IOException {
        boolean replaceEntitiesOld = replaceEntities;
        replaceEntities = true;
        super.text(elem);
        replaceEntities = replaceEntitiesOld;
    }
    @Override
    protected void textAreaContent(final AttributeSet attr) throws BadLocationException, IOException {
        boolean replaceEntitiesOld = replaceEntities;
        inTextArea = true;
        replaceEntities = true;
        super.textAreaContent(attr);
        inTextArea = false;
        replaceEntities = replaceEntitiesOld;
    }

    @Override
    public void write() throws IOException, BadLocationException {
        replaceEntities = false;
        super.write();
    }

    @Override
    protected void writeLineSeparator() throws IOException {
        final boolean pre = replaceEntities;
        replaceEntities = false;
        super.writeLineSeparator();
        replaceEntities = pre;
    }

    @Override
    protected void indent() throws IOException {
        if (inTextArea) {
            return;
        }
        final boolean pre = replaceEntities;
        replaceEntities = false;
        super.indent();
        replaceEntities = pre;
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
    synchronized void write(Element element) throws IOException, BadLocationException {
        this.element = element;
        try {
            write();
        }
        catch (final BadLocationException e) {
            element = null;
            throw e;
        }
        catch (final IOException e) {
            element = null;
            throw e;
        }
    }

    /**
     * invoke HTML creation for all children of a given element.
     *
     * @param parentElement  the element which children are to be written as HTML
     */
    public void writeChildElements(final Element parentElement) throws IOException, BadLocationException {
        Element childElement; //Not necessarily a paragraph element.
        for (int i = 0; i < parentElement.getElementCount(); i++) {
            childElement = parentElement.getElement(i);
            write(childElement);
        }
    }

    /**
     * Create an older style of HTML attributes.  This will
     * convert character level attributes that have a StyleConstants
     * mapping over to an HTML tag/attribute.  Other CSS attributes
     * will be placed in an HTML style attribute.
     */
    private static void convertStyleToHTMLStyle(final AttributeSet source, final MutableAttributeSet target) {
        if (source == null) {
            return;
        }
        final Enumeration sourceAttributeNames = source.getAttributeNames();
        String value = "";
        while (sourceAttributeNames.hasMoreElements()) {
            final Object sourceAttributeName = sourceAttributeNames.nextElement();
            if (sourceAttributeName instanceof CSS.Attribute) {
                // default is to store in a HTML style attribute
                if (value.length() > 0) {
                    value += "; ";
                }
                value += sourceAttributeName + ": " + source.getAttribute(sourceAttributeName);
            }
            else {
                target.addAttribute(sourceAttributeName, source.getAttribute(sourceAttributeName));
            }
        }
        if (value.length() > 0) {
            target.addAttribute(HTML.Attribute.STYLE, value);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLWriter#writeAttributes(javax.swing.text.AttributeSet)
     */
    @Override
    protected void writeAttributes(final AttributeSet attributeSet) throws IOException {
        final Object nameTag = (attributeSet != null) ? attributeSet.getAttribute(StyleConstants.NameAttribute) : null;
        final Object endTag = (attributeSet != null) ? attributeSet.getAttribute(HTML.Attribute.ENDTAG) : null;
        // write no attributes for end tags
        if (nameTag != null && endTag != null && (endTag instanceof String) && endTag.equals("true")) {
            return;
        }
        if (attributeSet instanceof Element) {
            final Element element = (Element) attributeSet;
            if (element.isLeaf() || element.getName().equalsIgnoreCase("p-implied")) {
                superWriteAttributes(attributeSet);
                return;
            }
        }
        //convertedAttributeSet.removeAttributes(convertedAttributeSet);
        final MutableAttributeSet convertedAttributeSet = new SimpleAttributeSet();
        SHTMLWriter.convertStyleToHTMLStyle(attributeSet, convertedAttributeSet);
        final Enumeration attributeNames = convertedAttributeSet.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            final Object attributeName = attributeNames.nextElement();
            if (attributeName instanceof HTML.Tag || attributeName instanceof StyleConstants
                    || attributeName == HTML.Attribute.ENDTAG) {
                continue;
            }
            write(" " + attributeName + "=\"" + convertedAttributeSet.getAttribute(attributeName) + "\"");
        }
    }


    private void superWriteAttributes(AttributeSet attr) throws IOException {
        // translate css attributes to html
        attr = convertToHTML(attr);

        Enumeration<?> names = attr.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            if (name instanceof HTML.Tag ||
                name instanceof StyleConstants ||
                name == HTML.Attribute.ENDTAG) {
                continue;
            }
            write(" " + name + "=\"" + attr.getAttribute(name) + "\"");
        }
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
    private void writeElementsUntil(final Element e, final Element end) throws IOException, BadLocationException {
        if (e.isLeaf()) {
            write(e);
        }
        else {
            if (e != end) {
                startTag(e);
                final int childCount = e.getElementCount();
                int index = 0;
                while (index < childCount) {
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
     * @param startElement  the element to start writing with
     * @param endElement  the last element to write
     */
    void write(final Element startElement, final Element endElement) throws IOException, BadLocationException {
        final Element parentElement = startElement.getParentElement();
        final int count = parentElement.getElementCount();
        int i = 0;
        Element e = parentElement.getElement(i);
        while (i < count && e != startElement) {
            e = parentElement.getElement(i++);
        }
        while (i < count) {
            writeElementsUntil(e, endElement);
            e = parentElement.getElement(i++);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLWriter#startTag(javax.swing.text.Element)
     */
    void writeStartTag(final Element elem) throws IOException, BadLocationException {
        // TODO Auto-generated method stub
        super.startTag(elem);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLWriter#endTag(javax.swing.text.Element)
     */
    void writeEndTag(final Element elem) throws IOException {
        // TODO Auto-generated method stub
        super.endTag(elem);
    }

    void writeEndTag(final String elementName) throws IOException {
        indent();
        write('<');
        write('/');
        write(elementName);
        write('>');
        writeLineSeparator();
    }

    void writeStartTag(final String elementName, final AttributeSet attributes) throws IOException {
        indent();
        write('<');
        write(elementName);
        if (attributes != null) {
            writeAttributes(attributes);
        }
        write('>');
        writeLineSeparator();
    }

    @Override
    public void write(final String string) throws IOException{
    	writer.write(string);
    }

    @Override
    public String toString() {
        if (writer instanceof StringWriter) {
            final StringWriter stringWriter = (StringWriter) writer;
            return stringWriter.getBuffer().toString();
        }
        return super.toString();
    }

    /** Gets the written string if the writer is a StringWriter, null otherwise. */
    String getWrittenString() {
        if (writer instanceof StringWriter) {
            final StringWriter stringWriter = (StringWriter) writer;
            return stringWriter.getBuffer().toString();
        }
        return null;
    }

    void removeLastWrittenNewline() {
        if (writer instanceof StringWriter) {
            final StringWriter stringWriter = (StringWriter) writer;
            int charIdx = stringWriter.getBuffer().length();
            while (stringWriter.getBuffer().charAt(--charIdx) <= 13) {
                stringWriter.getBuffer().deleteCharAt(charIdx);
            }
        }
    }


    /*
     * Stores all embedded tags. Embedded tags are tags that are
     * stored as attributes in other tags. Generally they're
     * character level attributes.  Examples include
     * &lt;b&gt;, &lt;i&gt;, &lt;font&gt;, and &lt;a&gt;.
     */
    private final Vector<HTML.Tag> tags = new Vector<HTML.Tag>(10);

    /**
     * Values for the tags.
     */
    private final Vector<Object> tagValues = new Vector<Object>(10);

     /*
     * This is used in closeOutUnwantedEmbeddedTags.
     */
    private final Vector<HTML.Tag> tagsToRemove = new Vector<HTML.Tag>(10);

    @Override
    protected void writeEmbeddedTags(AttributeSet attr) throws IOException {

        // translate css attributes to html
        AttributeSet htmlAttr = convertToHTML(attr);

        Enumeration<?> names = htmlAttr.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            if (name instanceof HTML.Tag) {
                HTML.Tag tag = (HTML.Tag)name;
                if (tag == HTML.Tag.FORM || tags.contains(tag)) {
                    continue;
                }
                write('<');
                write(tag.toString());
                Object o = htmlAttr.getAttribute(tag);
                if (o != null && o instanceof AttributeSet) {
                    writeAttributes((AttributeSet)o);
                }
                write('>');
                tags.addElement(tag);
                tagValues.addElement(o);
            }
        }
    }
    @Override
    protected void closeOutUnwantedEmbeddedTags(AttributeSet attr) throws IOException {

        tagsToRemove.removeAllElements();


        attr = convertToHTML(attr);

        HTML.Tag t;
        Object tValue;
        int firstIndex = -1;
        int size = tags.size();
        // First, find all the tags that need to be removed.
        for (int i = size - 1; i >= 0; i--) {
            t = tags.elementAt(i);
            tValue = tagValues.elementAt(i);
            if ((attr == null) || noMatchForTagInAttributes(attr, t, tValue)) {
                firstIndex = i;
                tagsToRemove.addElement(t);
            }
        }
        if (firstIndex != -1) {
            // Then close them out.
            boolean removeAll = ((size - firstIndex) == tagsToRemove.size());
            for (int i = size - 1; i >= firstIndex; i--) {
                t = tags.elementAt(i);
                if (removeAll || tagsToRemove.contains(t)) {
                    tags.removeElementAt(i);
                    tagValues.removeElementAt(i);
                }
                write('<');
                write('/');
                write(t.toString());
                write('>');
            }
            // Have to output any tags after firstIndex that still remaing,
            // as we closed them out, but they should remain open.
            size = tags.size();
            for (int i = firstIndex; i < size; i++) {
                t = tags.elementAt(i);
                write('<');
                write(t.toString());
                Object o = tagValues.elementAt(i);
                if (o != null && o instanceof AttributeSet) {
                    writeAttributes((AttributeSet)o);
                }
                write('>');
            }
        }
    }
    /**
     * Searches the attribute set for a tag, both of which
     * are passed in as a parameter.  Returns true if no match is found
     * and false otherwise.
     */
    private boolean noMatchForTagInAttributes(AttributeSet attr, HTML.Tag t,
                                              Object tagValue) {
        if (attr != null && attr.isDefined(t)) {
            Object newValue = attr.getAttribute(t);

            if ((tagValue == null) ? (newValue == null) :
                (newValue != null && tagValue.equals(newValue))) {
                return false;
            }
        }
        return true;
    }



    private final MutableAttributeSet convAttr = new SimpleAttributeSet();

    private AttributeSet convertToHTML(AttributeSet attr) {
        convAttr.removeAttributes(convAttr);
        convertToHTML40(attr, convAttr);
        return convAttr;
    }

    /**
     * Copies the given AttributeSet to a new set, converting
     * any CSS attributes found to arguments of an HTML style
     * attribute.
     */
    private void convertToHTML40(AttributeSet from, MutableAttributeSet to) {
        if (from == null) {
            return;
        }
        Enumeration<?> keys = from.getAttributeNames();
        String value = "";
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object attributeValue = from.getAttribute(key);
            if (key == CSS.Attribute.FONT_SIZE) {
                attributeValue = cssFontSizeFromHtmlFont(attributeValue);
            }
            if (key instanceof CSS.Attribute) {
                if(! containsExplicitTag(from.getAttributeNames(), (CSS.Attribute)key, attributeValue))
                    value = (value.isEmpty() ? key : value  + " " + key) + ": " + attributeValue + ";";
            } else if(key != HTML.Tag.FONT){
                to.addAttribute(key, attributeValue);
            }
        }
        if (value.length() > 0) {
            if (matchNameAttribute(from, HTML.Tag.CONTENT)) {
                SimpleAttributeSet styleAttribute = new SimpleAttributeSet();
                styleAttribute.addAttribute(HTML.Attribute.STYLE, value);
                to.addAttribute(HTML.Tag.SPAN, styleAttribute);
            }
            else
                to.addAttribute(HTML.Attribute.STYLE, value);
        }
    }

    private Object cssFontSizeFromHtmlFont(Object cssFontSizeValue) {
        if(cssFontSizeValue == null)
            return null;
        String cssFontSizeString = cssFontSizeValue.toString();
        if(cssFontSizeString.length() != 1)
            return cssFontSizeValue;

        int fontSizeIndex = cssFontSizeString.charAt(0) - '1';
        String[] cssFontSizes = {"xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large"};
        if(fontSizeIndex >= 0 && fontSizeIndex < cssFontSizes.length)
            return cssFontSizes[fontSizeIndex];
        return cssFontSizeValue;
    }

    @SuppressWarnings("serial")
    private static final Object[][] explicitTags = {
            {CSS.Attribute.FONT_WEIGHT, "bold", HTML.Tag.B},
            {CSS.Attribute.FONT_STYLE, "italic", HTML.Tag.I},
            {CSS.Attribute.TEXT_DECORATION, "underline", HTML.Tag.U},
            {CSS.Attribute.TEXT_DECORATION, "line-through", HTML.Tag.STRIKE},
            {CSS.Attribute.VERTICAL_ALIGN, "sup", HTML.Tag.SUP},
            {CSS.Attribute.VERTICAL_ALIGN, "sub", HTML.Tag.SUB},
            {CSS.Attribute.TEXT_DECORATION, "line-through", HTML.Tag.STRIKE},
    };


    private static boolean containsExplicitTag(Enumeration<?> keys, CSS.Attribute key, Object attributeValue) {
        for(Object[] cssTagTripple : explicitTags) {
            if(key == cssTagTripple[0]
                    && attributeValue.toString().equals(cssTagTripple[1])) {
                return containsValue(keys, cssTagTripple[2]);
            }
        }
        return false;

    }

    private static boolean containsValue(Enumeration<?> values, Object value) {
        while(values.hasMoreElements()) {
            if(values.nextElement().equals(value))
                return true;
        }
        return false;
    }
}
