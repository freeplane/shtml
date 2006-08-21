/*
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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Main window of application SimplyHTML.
 *
 * <p>This class constructs the main panel and all of its GUI elements
 * such as menus, etc.</p>
 *
 * <p>It defines a set of inner classes creating actions which can be
 * connected to menus, buttons or instantiated individually.</p>
 *
 * @author Ulrich Hilger
 * @author Dimitri Polivaev
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
public class FrmMain extends JFrame {
    public static final String APP_NAME = "SimplyHTML";
    /** static reference to this instance of class FrmMain */
    public static Frame mainFrame;
    private SHTMLPanel mainPane;

    public FrmMain(){
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        mainFrame = this;
        mainPane = new SHTMLPanel();
        setSize(new Dimension(800, 600));
        setTitle(APP_NAME);

        setIconImage(Toolkit.getDefaultToolkit().createImage(DynamicResource.getResource(SHTMLPanel.resources, "appIcon")));
        getContentPane().add(mainPane);
    }

    /**
     * catch requests to close the application's main frame to
     * ensure proper clean up before the application is
     * actually terminated.
     */
    protected void processWindowEvent(WindowEvent e) {
      if (! (e.getID() == WindowEvent.WINDOW_CLOSING) || mainPane.close()) {
        super.processWindowEvent(e);
      }
    }

}
