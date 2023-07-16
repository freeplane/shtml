/*
 * Created on 12 Jul 2023
 *
 * author dimitry
 */
package com.lightdev.app.shtm;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.text.AttributeSet;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTML.UnknownTag;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.TagElement;

class SHTMLDocumentParser extends DocumentParser {
    boolean seenHtml = false;

    boolean seenBody = false;

    private final LinkedList<List<String>> openedUnknownElementStack;


    SHTMLDocumentParser(DTD dtd) {
        super(dtd);
        openedUnknownElementStack = new LinkedList<>();
    }


    @Override
    protected void startTag(TagElement tag) throws ChangedCharSetException {
        if(! seenBody) {
            if (tag.getHTMLTag() instanceof UnknownTag) {
                if(! seenHtml) {
                    super.startTag(makeTag(dtd.html, true));
                    seenHtml = true;
                }
                if(! seenBody) {
                    super.startTag(makeTag(dtd.body, true));
                    seenBody = true;
                }
            }
            else if(! seenHtml && tag.getElement() == dtd.html)
                seenHtml = true;
            else if(tag.getElement() == dtd.body)
                seenBody = true;
        }
        super.startTag(tag);
    }

    @Override
    protected void handleStartTag(TagElement tag) {
        if(tag.getHTMLTag() != HTML.Tag.HTML)
            pushOpenUnknownElementStack();
        super.handleStartTag(tag);
    }

    private void pushOpenUnknownElementStack() {
       openedUnknownElementStack.add(Collections.emptyList());
    }

    private void popOpenUnknownElementStack() {
        openedUnknownElementStack.removeLast();
    }

    @Override
    protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
        Tag htmlTag = tag.getHTMLTag();
        if(htmlTag instanceof UnknownTag) {
            boolean isEndTag = isEndTag();
            if(isEndTag) {
                closeUnknownTags();
                popOpenUnknownElementStack();
                unregisterUnknownElement((UnknownTag) htmlTag);
            }
            else {
                registerUnknownElement((UnknownTag) htmlTag);
                pushOpenUnknownElementStack();
            }
            int i = 0;
        }
        super.handleEmptyTag(tag);
    }

    private void closeUnknownTags() {
        List<String> tagList = openedUnknownElementStack.getLast();
        if(tagList.isEmpty())
            return;

        boolean isEndtag = isEndTag();
        SimpleAttributeSet attributes = getAttributes();
        if(! isEndtag) {
            attributes.addAttribute(HTML.Attribute.ENDTAG, "true");
        }
        ListIterator<String> reverseIterator = tagList.listIterator(tagList.size());
        while(reverseIterator.hasPrevious()) {
            String closedElement = reverseIterator.previous();
            try {
                super.handleEmptyTag(makeTag(closedElement));
            } catch (ChangedCharSetException e) {
                throw new RuntimeException(e);
            }

        }
        if(! isEndtag)
            attributes.removeAttribute(HTML.Attribute.ENDTAG);

    }

    private TagElement makeTag(String unknownElementName) {
        Element element = dtd.getElement("unknown");
        element.name = unknownElementName;
        return makeTag(element, false);
    }

    private void registerUnknownElement(UnknownTag tag) {
        List<String> tagList = openedUnknownElementStack.getLast();
        if(tagList.isEmpty() && ! (tagList instanceof LinkedList)) {
            openedUnknownElementStack.removeLast();
            tagList = new LinkedList<>();
            openedUnknownElementStack.add(tagList);
        }
        tagList.add(tag.toString());
    }

    private void unregisterUnknownElement(UnknownTag tag) {
        List<String> tagList = openedUnknownElementStack.getLast();
        if(! tagList.isEmpty()) {
            LinkedList<String> linkedTagList = (LinkedList<String>)tagList;
            String removedElement = linkedTagList.removeLast();
            String tagName = tag.toString();
            if(! removedElement.equals(tagName)) {
                linkedTagList.removeLastOccurrence(tagName);
                linkedTagList.add(removedElement);
            }
         }
     }

    boolean isEndTag() {
        AttributeSet as = getAttributes();
        if (as != null) {
            Object end = as.getAttribute(HTML.Attribute.ENDTAG);
            if (end != null && (end instanceof String) &&
                ((String)end).equals("true")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void handleEndTag(TagElement tag) {
        Tag htmlTag = tag.getHTMLTag();
        if(htmlTag == HTML.Tag.BODY)
            closeAllUnknownTags();
        else if(htmlTag != HTML.Tag.HTML){
            closeUnknownTags();
            popOpenUnknownElementStack();
        }
        super.handleEndTag(tag);
    }

    private void closeAllUnknownTags() {
        while(! openedUnknownElementStack.isEmpty()){
            closeUnknownTags();
            popOpenUnknownElementStack();
        }
    }




}