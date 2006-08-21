package com.lightdev.app.shtm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;

/**
   * a slot for testing certain things conveniently during development
   */
  public class SHTMLFileTestAction extends AbstractAction
	implements SHTMLAction
  {
    /**
     * 
     */
    private final SHTMLPanel panel;
    public SHTMLFileTestAction(SHTMLPanel panel) {
      super(SHTMLPanel.testAction);
    this.panel = panel;
      getProperties();
    }
    public void actionPerformed(ActionEvent ae) {

      //Util.errMsg(null, "no test action is implemented.", null);

      this.panel.getEditor().insertBreak();

      //GregorianCalendar gc = new GregorianCalendar(2003, 4, 31);
      //System.out.println(gc.getTime().getTime());

      /* list attributes
      Element elem = doc.getParagraphElement(editor.getCaretPosition());

      //System.out.println("\r\n\r\n element name=" + elem.getName());
      AttributeSet attrs = doc.getStyleSheet().getStyle(elem.getName());
      de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
      hd.listAttributes(attrs, 4);

      System.out.println("\r\n\r\n resolved element name=" + elem.getName());
      attrs = Util.resolveAttributes(doc.getStyleSheet().getStyle(elem.getName()));
      hd = new de.calcom.cclib.html.HTMLDiag();
      hd.listAttributes(attrs, 4);

      System.out.println("\r\n\r\n maxAttributes element name=" + elem.getName());
      attrs = getMaxAttributes(elem, doc.getStyleSheet());
      hd = new de.calcom.cclib.html.HTMLDiag();
      hd.listAttributes(attrs, 4);
      */

      /* switch editable
      SHTMLEditorPane editor = dp.getEditor();
      editor.setEditable(!editor.isEditable());
      updateActions();
      */
    }
    public void update() {
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }