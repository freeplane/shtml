/*
 * Created on 07.10.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm;

class SHTMLPanelSingleDocImpl extends SHTMLPanelImpl {

    public SHTMLPanelSingleDocImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#initDocumentPane()
     */
    protected void initDocumentPane() {
        super.initDocumentPane();
        dp = new DocumentPane(null, 1);
        editor = dp.getEditor();
        doc = (SHTMLDocument) editor.getDocument();
        registerDocument();
        dp.getEditor().setCaretPosition(0);
        splitPanel.addComponent(dp, SplitPanel.CENTER);
    }
    protected void initActions() {
        super.initActions();
        dynRes.addAction(findReplaceAction, new SHTMLEditorKitActions.SingleDocFindReplaceAction(this));
    }
}
