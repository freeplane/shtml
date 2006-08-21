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

import java.io.*;
import javax.swing.text.*;
import javax.swing.text.html.*;


import java.util.prefs.*;

/**
 * Extensions to <code>HTMLEditorKit</code> for application SimplyHTML.
 *
 * <p>In stage 1 this only re-implements how style sheets are handled by
 * default.</p>
 *
 * <p>Stage 3 adds functionality for usage of the custom HTML document
 * and HTML reader used with SimplyHTML from this stage on.</p>
 *
 * <p>With stage 9 some additional views have been added to
 * the view factory as a workaround for bug id 4765271
 * (see http://developer.java.sun.com/developer/bugParade/bugs/4765271.html).</p>
 *
 * <p>OK, I give up: With release 2 of stage 9 above views are used no longer and
 * bug fixing is not done anymore. The HTML support is almost taken as is in the hope
 * that Sun will enhance it some day. To do compensation inside a single application
 * is not possible with a reasonable effort.</p>
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
 * @version stage 12, August 06, 2006
 */

public class SHTMLEditorKit extends HTMLEditorKit {

  public SHTMLEditorKit() {
    super();
    try {
      Preferences prefs = Preferences.userNodeForPackage(getClass().forName("com.lightdev.app.shtm.PrefsDialog"));
      this.renderMode = prefs.get(PrefsDialog.PREFSID_WRITE_MODE, PrefsDialog.PREFS_WRITE_MODE_HTML32);
    }
    catch(Exception e) {}
  }

  public SHTMLEditorKit(String renderMode) {
    this();
    this.renderMode = renderMode;
  }

  /* --------------- SHTMLDocument implementation start ------------ */

  /**
   * Create an uninitialized text storage model
   * that is appropriate for this type of editor.
   *
   * @return the model
   */
  public Document createDefaultDocument() {
    StyleSheet styles = getStyleSheet();
    StyleSheet ss = new StyleSheet();
    try {
      ss.importStyleSheet(getClass().forName("javax.swing.text.html.HTMLEditorKit").getResource(DEFAULT_CSS));
    }
    catch(Exception e) {}
    SHTMLDocument doc = new SHTMLDocument(ss);
    doc.setParser(getParser());
    doc.setAsynchronousLoadPriority(4);
    doc.setTokenThreshold(100);
    return doc;
  }

  /**
   * Inserts content from the given stream. If <code>doc</code> is
   * an instance of HTMLDocument, this will read
   * HTML 3.2 text. Inserting HTML into a non-empty document must be inside
   * the body Element, if you do not insert into the body an exception will
   * be thrown. When inserting into a non-empty document all tags outside
   * of the body (head, title) will be dropped.
   *
   * @param in  the stream to read from
   * @param doc the destination for the insertion
   * @param pos the location in the document to place the
   *   content
   * @exception IOException on any I/O error
   * @exception BadLocationException if pos represents an invalid
   *   location within the document
   * @exception RuntimeException (will eventually be a BadLocationException)
   *            if pos is invalid
   */
  public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
    if (doc instanceof SHTMLDocument) {
      SHTMLDocument hdoc = (SHTMLDocument) doc;
      Parser p = getParser();
      if (p == null) {
        throw new IOException("Can't load parser");
      }
      if (pos > doc.getLength()) {
        throw new BadLocationException("Invalid location", pos);
      }
      ParserCallback receiver = hdoc.getReader(pos);
      Boolean ignoreCharset = (Boolean)doc.getProperty("IgnoreCharsetDirective");
      p.parse(in, receiver, (ignoreCharset == null) ? false : ignoreCharset.booleanValue());
      receiver.flush();
    }
    else {
      super.read(in, doc, pos);
    }
  }

  /**
   * Write content from a document to the given stream
   * in a format appropriate for this kind of content handler.
   *
   * @param out  the stream to write to
   * @param doc  the source for the write
   * @param pos  the location in the document to fetch the
   *   content
   * @param len  the amount to write out
   * @exception IOException on any I/O error
   * @exception BadLocationException if pos represents an invalid
   *   location within the document
   */
  public void write(Writer out, Document doc, int pos, int len)
      throws IOException, BadLocationException {

    if (doc instanceof SHTMLDocument) {
      try {
           SHTMLWriter w = new SHTMLWriter(out, (SHTMLDocument) doc, pos,
                                                  len);
          w.write();
      }
      catch(Exception e) {
          e.printStackTrace();
      }
    }
    else if (doc instanceof StyledDocument) {
      MinimalHTMLWriter w = new MinimalHTMLWriter(out, (StyledDocument)doc, pos, len);
      w.write();
    }
    else {
      super.write(out, doc, pos, len);
    }
  }

  /* --------------- SHTMLDocument implementaion end --------------- */

  /* --------------- ViewFactory implementation start -------------- */

  /** Shared factory for creating HTML Views. */
  private static final ViewFactory defaultFactory = new SHTMLFactory();

  /**
   * Fetch a factory that is suitable for producing
   * views of any models that are produced by this
   * kit.
   *
   * @return the factory
   */
  public ViewFactory getViewFactory() {
    return defaultFactory;
  }

  public static class SHTMLFactory extends HTMLEditorKit.HTMLFactory
      implements ViewFactory
  {
    public View create(Element elem) {
      View view = null;
      Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
      if (o instanceof HTML.Tag) {
        HTML.Tag kind = (HTML.Tag) o;
        //System.out.println("SHTMLEditorKit.SHTMLFactory o is HTML.Tag kind=" + kind.toString());
        if (kind == HTML.Tag.TABLE) {
            view = super.create(elem);
        }
        else if (kind == HTML.Tag.COMMENT) {
          view = new InvisibleView(elem);
        }
        else if (kind instanceof HTML.UnknownTag) {
          view = new InvisibleView(elem);
        }
        else {
          view = super.create(elem);
        }
      }
      else {
        view = new LabelView(elem);
      }
      return view;
    }
  }

  /**
   * set the render mode
   *
   * <p>This influences how the ViewFactory creates view to render content</p>
   *
   * @param mode the mode, one of PrefsDialog.PREFS_WRITE_MODE_HTML32 or _HTML4
   */
  public void setRenderMode(String mode) {
    this.renderMode = mode;
  }

  private static String renderMode = PrefsDialog.PREFS_WRITE_MODE_HTML32;

  /* --------------- ViewFactory implementation end -------------- */

}
