/*
 * Created on 04.10.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTML;

class SHTMLPanelMultipleDocImpl extends SHTMLPanelImpl implements ChangeListener  {
    public static final String newAction = "new";
    public static final String openAction = "open";
    public static final String closeAction = "close";
    public static final String closeAllAction = "closeAll";
    public static  final String saveAction = "save";
    public static final String saveAsAction = "saveAs";

    /** the tabbed pane for adding documents to show to */
    private JTabbedPane jtpDocs;

    /** tool bar selector for styles */
    private StyleSelector styleSelector;


    /** number of currently active tab */
    private int activeTabNo;


    public SHTMLPanelMultipleDocImpl() {
        super();
    }

    protected void initDocumentPane() {
        dynRes.getAction(newAction).actionPerformed(null);
        dp.getEditor().setCaretPosition(0);
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#initActions()
     */
    protected void initActions() {
        super.initActions();
        dynRes.addAction(findReplaceAction, new SHTMLEditorKitActions.MultipleDocFindReplaceAction(this));
        dynRes.addAction(setStyleAction, new SHTMLEditorKitActions.SetStyleAction(this));
        dynRes.addAction(newAction, new SHTMLEditorKitActions.SHTMLFileNewAction(this));
        dynRes.addAction(openAction, new SHTMLEditorKitActions.SHTMLFileOpenAction(this));
        dynRes.addAction(closeAction, new SHTMLEditorKitActions.SHTMLFileCloseAction(this));
        dynRes.addAction(closeAllAction, new SHTMLEditorKitActions.SHTMLFileCloseAllAction(this));
        dynRes.addAction(saveAction, new SHTMLEditorKitActions.SHTMLFileSaveAction(this));
        dynRes.addAction(saveAllAction, new SHTMLEditorKitActions.SHTMLFileSaveAllAction(this));
        dynRes.addAction(saveAsAction, new SHTMLEditorKitActions.SHTMLFileSaveAsAction(this));
        dynRes.addAction(exitAction, new SHTMLEditorKitActions.SHTMLFileExitAction(this));
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#customizeFrame()
     */
    protected void customizeFrame() {
        jtpDocs = new JTabbedPane();
        super.customizeFrame();
        jtpDocs.addChangeListener(this);
        splitPanel.addComponent(jtpDocs, SplitPanel.CENTER);
     }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#createToolbarItem(javax.swing.JToolBar, java.lang.String)
     */
    protected void createToolbarItem(JToolBar toolBar, String itemKey) {
        if(itemKey.equalsIgnoreCase(setStyleAction)) {
            styleSelector = new StyleSelector(this, HTML.Attribute.CLASS);
            styleSelector.setPreferredSize(new Dimension(110, 23));
            styleSelector.setAction(dynRes.getAction(setStyleAction));
            final Dimension comboBoxSize = new Dimension(300, 24);
            styleSelector.setMaximumSize(comboBoxSize);
            jtpDocs.addChangeListener(styleSelector);
            toolBar.add(styleSelector);
        }
        else {
            super.createToolbarItem(toolBar, itemKey);
        }
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#registerDocument()
     */
    protected void registerDocument() {
        super.registerDocument();
        ((SHTMLDocument) getDocumentPane().getDocument()).getStyleSheet().addChangeListener(styleSelector);
            }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#unregisterDocument()
     */
    protected void unregisterDocument() {
        super.unregisterDocument();
        ((SHTMLDocument) getDocumentPane().getDocument()).getStyleSheet().removeChangeListener(styleSelector);
    }

    /**
     * catch requests to close the application's main frame to
     * ensure proper clean up before the application is
     * actually terminated.
     */
    boolean close() {
        dynRes.getAction(exitAction).actionPerformed(
                  new ActionEvent(this, 0, exitAction));
        return jtpDocs.getTabCount() == 0;
    }

    /**
     * change listener to be applied to our tabbed pane
     * so that always the currently active components
     * are known
     */
    public void stateChanged(ChangeEvent e) {
      activeTabNo = jtpDocs.getSelectedIndex();
      if(activeTabNo >= 0){
          dp = (DocumentPane) jtpDocs.getComponentAt(activeTabNo);
          editor = dp.getEditor();
          //System.out.println("FrmMain stateChanged docName now " + dp.getDocumentName());
          doc = (SHTMLDocument) getEditor().getDocument();
          //fireDocumentChanged();
          if(!ignoreActivateDoc) {
              dp.fireActivated();
          }
      }
      else{
          dp = null;
          editor = null;
          doc = null;
      }
    }
    /**
     * @return Returns the jtpDocs.
     */
    JTabbedPane getTabbedPaneForDocuments() {
        return jtpDocs;
    }


    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#updateFormatControls()
     */
    void updateFormatControls() {
        super.updateFormatControls();
        styleSelector.update();
    }

    void incNewDocCounter() {
        newDocCounter++;
    }

    void createNewDocumentPane() {
        setDocumentPane(new DocumentPane(null, ++newDocCounter));
    }

    void selectTabbedPane(int index) {
        ignoreActivateDoc = true;
        getTabbedPaneForDocuments().setSelectedIndex(index);
        ignoreActivateDoc = false;
    }


    int getActiveTabNo() {
      return activeTabNo;
    }

    /**
     * @param activeTabNo The activeTabNo to set.
     */
    void setActiveTabNo(int activeTabNo) {
        this.activeTabNo = activeTabNo;
    }
}
