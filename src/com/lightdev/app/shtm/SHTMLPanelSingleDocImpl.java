/*
 * Created on 07.10.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm;

import java.awt.BorderLayout;

class SHTMLPanelSingleDocImpl extends SHTMLPanelImpl {

    public SHTMLPanelSingleDocImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#initDocumentPane()
     */
    protected void initDocumentPane() {
        super.initDocumentPane();
        setDocumentPane(new DocumentPane(null, 1));
        setEditorPane(getDocumentPane().getEditor());
        doc = (SHTMLDocument) getEditorPane().getDocument();
        registerDocument();
        getDocumentPane().getEditor().setCaretPosition(0);
        splitPanel.addComponent(getDocumentPane(), SplitPanel.CENTER);
    }
    protected void initActions() {
        super.initActions();
        dynRes.addAction(findReplaceAction, new SHTMLEditorKitActions.SingleDocFindReplaceAction(this));
    }
}
