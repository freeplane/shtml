/*
 * Created on 10.09.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.text.html.HTMLDocument;

public abstract class SHTMLPanel extends JPanel {

    SHTMLPanel(LayoutManager layout) {
        super(layout);
    }
    
    public static SHTMLPanel createSHTMLPanel(){
        return new SHTMLPanelImpl();
    }

   public abstract String getDocumentText();

    public abstract boolean needsSaving();

    public abstract void setContentPanePreferredSize(Dimension dimension);

    public abstract void setCurrentDocumentContent(String string, String note);

    public static void setResources(ResourceBundle resources) {
        SHTMLPanelImpl.setResources(resources);        
    }

    public abstract HTMLDocument getDocument();

    public static ResourceBundle getResources() {
        return SHTMLPanelImpl.resources;
    }
}
