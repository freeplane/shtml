/*
 * Created on 16.12.2006
 * Copyright (C) 2006 Dimitri Polivaev
 */
package com.lightdev.app.shtm;

import java.awt.event.KeyEvent;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.CSH.DisplayHelpFromSource;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

class SHTMLHelpBroker {

    public static final String APP_HELP_NAME = "help";
    public static final String JAVA_HELP_EXT = ".hs";

    private SHTMLHelpBroker() {
    }

    /** our help broker */
    private static HelpBroker hb;

    /**
     * get the <code>HelpBroker</code> of our application
     *
     * @return the <code>HelpBroker</code> to be used for help display
     */
    private static HelpBroker getHelpBroker() {
        if(hb == null)
        {
            URL url = SHTMLPanelImpl.class.getResource(APP_HELP_NAME +
                    Util.URL_SEPARATOR + APP_HELP_NAME + JAVA_HELP_EXT);
            HelpSet hs;
            try {
                hs = new HelpSet(null, url);
            } catch (HelpSetException e) {
                return null;
            }
            hb = hs.createHelpBroker();
        }
        return hb;
    }

    static AbstractButton createHelpButton(String helpTopicId) {
        AbstractButton newButton;
        newButton = new JButton();
        CSH.setHelpIDString(newButton, helpTopicId);
        newButton.addActionListener(
                new CSH.DisplayHelpFromSource(SHTMLHelpBroker.getHelpBroker()));
        return newButton;
    }

    static void initJavaHelpItem(JMenuItem mi, String helpTopicId) {
          CSH.setHelpIDString(mi, helpTopicId);
          mi.addActionListener(new CSH.DisplayHelpFromSource(SHTMLHelpBroker.getHelpBroker()));
          mi.setIcon(DynamicResource.getIconForCommand(SHTMLPanelImpl.textResources, SHTMLPanelImpl.helpTopicsAction));
          mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
          mi.setEnabled(true);
    }
}
