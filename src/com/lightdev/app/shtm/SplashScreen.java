/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 * Copyright (C) 2006 Karsten Pawlik
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import com.lightdev.app.shtm.SHTMLPanel;

/**
 * Class that displays a splash screen
 * Is run in a separate thread so that the applet continues to load in the background
 * @author Karsten Pawlik
 * @version stage 12, August 06, 2006
 */
public class SplashScreen extends JWindow{

    public SplashScreen() {
        try {
            JPanel panel = new JPanel(new BorderLayout());
            ImageIcon icon = new ImageIcon(SplashScreen.class.getResource(SHTMLPanel.dynRes.getResourceString(SHTMLPanel.resources, "splashImage")));
            panel.add(new JLabel(icon), BorderLayout.CENTER);
            getContentPane().add(panel);
            pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((int) (d.getWidth() - getWidth()) / 2, (int) (d.getHeight() - getHeight()) / 2);
            setVisible(true);
            getRootPane().paintImmediately(0, 0, getWidth(), getHeight());
        } catch (Exception e) {
        }
    }
}
