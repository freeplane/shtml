/*
 * Created on 12 Jul 2023
 *
 * author dimitry
 */
package com.lightdev.app.shtm;

import javax.swing.text.ChangedCharSetException;
import javax.swing.text.html.HTML.UnknownTag;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.TagElement;

class SHTMLDocumentParser extends DocumentParser {
    boolean seenHtml = false;

    boolean seenBody = false;

    SHTMLDocumentParser(DTD dtd) {
        super(dtd);
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
}