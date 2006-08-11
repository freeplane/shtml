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

import java.awt.*;
import java.awt.Canvas;

/**
 * A splash screen for application SimplyHTML to be shown during startup.
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
 * @version stage 11, April 27, 2003
 */

public class SplashScreen extends Canvas {

  private Window win;
  private Image image;
  private Image offscreenImg;
  private Graphics offscreenGfx;

  public SplashScreen() {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    image = getToolkit().getImage(
            getClass().getResource(FrmMain.dynRes.getResourceString(FrmMain.resources, "splashImage")));
    MediaTracker tracker = new MediaTracker(this);
    tracker.addImage(image,0);
    try {
      tracker.waitForAll();
    }
    catch(Exception e) {
      Util.errMsg(this, e.getMessage(), e);
    }
    win = new Window(new Frame());
    Dimension screen = getToolkit().getScreenSize();
    Dimension size = new Dimension(image.getWidth(this) + 2,
            image.getHeight(this) + 2);
    win.setSize(size);
    win.setLayout(new BorderLayout());
    win.add(BorderLayout.CENTER,this);
    win.setLocation((screen.width - size.width) / 2,
            (screen.height - size.height) / 2);
    win.validate();
    win.show();
  }

  public synchronized void paint(Graphics g) {
    Dimension size = getSize();
    if(offscreenImg == null)
    {
      offscreenImg = createImage(size.width,size.height);
      offscreenGfx = offscreenImg.getGraphics();
    }
    offscreenGfx.setColor(Color.black);
    offscreenGfx.drawRect(0,0,size.width - 1,size.height - 1);
    offscreenGfx.drawImage(image,1,1,this);
    g.drawImage(offscreenImg,0,0,this);
    notify();
  }

  public void dispose() {
    win.dispose();
  }

  public void update(Graphics g) {
    paint(g);
  }
}
