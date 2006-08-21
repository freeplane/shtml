package com.lightdev.app.shtm.actions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import com.lightdev.app.shtm.DynamicResource;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.SHTMLPanel;
import com.sun.demo.ElementTreePanel;

/**
   * Action that brings up a JFrame with a JTree showing the structure
   * of the document in the currently active DocumentPane.
   *
   * will be hidden from menu if not in development mode (DEV_MODE = false)
   */
  public class ShowElementTreeAction extends AbstractAction implements SHTMLAction {
    /**
     * 
     */
    private final SHTMLPanel panel;
    /** a frame for showing an element tree panel */
    private JFrame elementTreeFrame = null;

    public ShowElementTreeAction(SHTMLPanel panel) {
      super(SHTMLPanel.elemTreeAction);
    this.panel = panel;
      getProperties();
    }
    public void actionPerformed(ActionEvent e) {
      if(this.elementTreeFrame == null) {
        String title = DynamicResource.getResourceString(SHTMLPanel.resources, "elementTreeTitle");
        this.elementTreeFrame = new JFrame(title);
        this.elementTreeFrame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent we) {
            ShowElementTreeAction.this.elementTreeFrame.dispose();
            ShowElementTreeAction.this.elementTreeFrame = null;
          }
        });
        Container fContentPane = this.elementTreeFrame.getContentPane();
        fContentPane.setLayout(new BorderLayout());
        int activeTabNo = this.panel.getTabbedPaneForDocuments().getSelectedIndex();
        ElementTreePanel elementTreePanel = new ElementTreePanel(this.panel.getEditor());
        fContentPane.add(elementTreePanel);
        this.elementTreeFrame.pack();
      }
      this.elementTreeFrame.show();
      this.panel.updateActions();
    }
    public void update() {
    }
    public void getProperties() {
      SHTMLPanel.getActionProperties(this, (String) getValue(Action.NAME));
    }
  }