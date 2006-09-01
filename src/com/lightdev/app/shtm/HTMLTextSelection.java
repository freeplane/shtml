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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;

/**
 * A transferable for HTML text.
 *
 * <p>It can be used in drag and drop operations or in copy and paste
 * operations. Additional to <code>HTMLText</code> it supports the
 * <code>String</code> data flavor.</p>
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
 *
 * @see java.awt.datatransfer.DataFlavor.stringFlavor
 * @see java.awt.datatransfer.DataFlavor.plainTextFlavor
 * @see com.lightdev.app.shtm.HTMLText
 */

public class HTMLTextSelection  implements Transferable, ClipboardOwner
{

  /** index of HTML text data flavor */
  private static final int HTML_TEXT = 0;

  /** index of String data flavor */
  private static final int STRING = 1;

  /** the data to transfer */
  private HTMLText data;

  /** the data flavor of this transferable */
  private static final DataFlavor[] flavors = {
    new DataFlavor(com.lightdev.app.shtm.HTMLText.class, "HTMLText"),
    DataFlavor.stringFlavor
  };

  /**
   * construct a <code>HTMLTextSelection</code> with a chunk
   * of styled text.
   *
   * @param data - a HTMLText object
   *
   * @see com.lightdev.app.shtm.HTMLText
   */
  public HTMLTextSelection(HTMLText data) {
    this.data = data;
  }

  /* ---- start of Transferable implementation ----------------------------*/

  /**
   * Returns an array of DataFlavor objects indicating the flavors the data
   * can be provided in.  The array should be ordered according to preference
   * for providing the data (from most richly descriptive to least descriptive).
   * @return an array of data flavors in which this data can be transferred
   */
  public DataFlavor[] getTransferDataFlavors() {
    return (DataFlavor[])flavors.clone();
  }

  /**
   * Returns whether or not the specified data flavor is supported for
   * this object.
   * @param flavor the requested flavor for the data
   * @return boolean indicating wjether or not the data flavor is supported
   */
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    for (int i = 0; i < flavors.length; i++) {
      if (flavors[i].equals(flavor)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an object which represents the data to be transferred.  The class
   * of the object returned is defined by the representation class of the flavor.
   *
   * @param flavor the requested flavor for the data
   * @see DataFlavor#getRepresentationClass
   * @exception IOException                if the data is no longer available
   *              in the requested flavor.
   * @exception UnsupportedFlavorException if the requested data flavor is
   *              not supported.
   */
  public Object getTransferData(DataFlavor flavor) throws
      UnsupportedFlavorException, IOException
  {
    if (flavor.equals(flavors[HTML_TEXT])) {
      return (Object) data;
    }
    else if (flavor.equals(flavors[STRING])) {
      return (Object) data.toString();
    }
    else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  /* ----------- end of Transferable implementation ------------------- */

  /* ----------- start of ClipboardOwner implementation --------------- */

  /**
   * Notifies this object that it is no longer the owner of
   * the contents of the clipboard.
   * @param clipboard the clipboard that is no longer owned
   * @param contents the contents which this owner had placed on the clipboard
   */
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
  }

  /* ------------ end of ClipboardOwner implementation ---------------- */
}
