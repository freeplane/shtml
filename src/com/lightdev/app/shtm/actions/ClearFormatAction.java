/*
 * Created on 20.08.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;

import com.lightdev.app.shtm.AttributeComponent;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLDocument;
import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.Util;

public class ClearFormatAction extends StyledEditorKit.BoldAction implements SHTMLAction{
    /**
     * 
     */
    private final SHTMLPanel panel;
    public ClearFormatAction(SHTMLPanel panel) {
      //Action act = new StyledEditorKit.BoldAction();
      super();
    this.panel = panel;
      putValue(Action.NAME, SHTMLPanel.clearFormatAction);
      putValue(SHTMLPanel.ACTION_SELECTED_KEY, SHTMLPanel.ACTION_UNSELECTED);
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
              KeyEvent.VK_B, KeyEvent.CTRL_MASK));
      SHTMLPanel.getActionProperties(this, SHTMLPanel.clearFormatAction);
    }
    /**
     * do the format change for the associated attribute
     *
     * <p>This reverses the current setting for the associated attribute</p>
     *
     * @param  e  the ActionEvent describing the cause for this action
     */
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      final SHTMLEditorPane editor = this.panel.getEditor();
      if(editor != null) {
          editor.removeCharacterAttributes();
      }
      this.panel.updateActions();
    }

    public void getProperties() {
        SHTMLPanel.getActionProperties(this, SHTMLPanel.clearFormatAction);
      }
    public void update() {
        if(this.panel.isHtmlEditorActive()){
            this.setEnabled(false);
            return;
        }
        final SHTMLEditorPane editor = this.panel.getEditor();
        this.setEnabled(editor.getSelectionStart() != editor.getSelectionEnd());
    }

  }