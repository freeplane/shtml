/*
 * Created on 10.09.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm;

import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JEditorPane;
import javax.swing.JMenuBar;
import javax.swing.text.html.HTMLDocument;

/**
 * Class for using SimplyHTML as as component  
 * 
 * @author Dimitri Polivaev
 * 14.01.2007
 */
public abstract class SHTMLPanel extends JPanel {

    SHTMLPanel(LayoutManager layout) {
        super(layout);
    }
    
    public static SHTMLPanel createSHTMLPanel(){
        return new SHTMLPanelSingleDocImpl();
    }

   public abstract String getDocumentText();

    public abstract boolean needsSaving();

    public abstract void setContentPanePreferredSize(Dimension dimension);

    public abstract void setCurrentDocumentContent(String sText);

    public static void setResources(TextResources resources) {
        SHTMLPanelImpl.setTextResources(resources);        
    }

    public abstract HTMLDocument getDocument();

    public abstract JEditorPane getEditorPane();

    public static TextResources getResources() {
       return SHTMLPanelImpl.getResources(); }

    abstract public int getCaretPosition();

    public abstract JMenuBar getMenuBar();

}
