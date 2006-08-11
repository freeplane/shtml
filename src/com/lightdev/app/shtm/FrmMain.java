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
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.help.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;
import java.util.*;
import com.sun.demo.ElementTreePanel;
import com.sun.demo.ExampleFileFilter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.datatransfer.*;
import com.lightdev.app.shtm.plugin.SHTMLPlugin;
import com.lightdev.app.shtm.plugin.PluginManager;
import com.lightdev.app.shtm.plugin.ManagePluginsAction;
import java.util.prefs.*;
import de.calcom.cclib.text.*;

/**
 * Main window of application SimplyHTML.
 *
 * <p>This class constructs the main window and all of its GUI elements
 * such as menus, etc.</p>
 *
 * <p>It defines a set of inner classes creating actions which can be
 * connected to menus, buttons or instantiated individually.</p>
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

public class FrmMain extends JFrame implements CaretListener, ChangeListener {

  //private int renderMode = SHTMLEditorKit.RENDER_MODE_JAVA;

  /* some public constants */
  public static final String APP_NAME = "SimplyHTML";
  public static final String APP_HELP_NAME = "help";
  public static final String APP_TEMP_DIR = "temp";
  public static final String IMAGE_DIR = "images";
  public static final String JAVA_HELP_EXT = ".hs";
  public static final String ACTION_SELECTED_KEY = "selected";
  public static final String ACTION_SELECTED = "true";
  public static final String ACTION_UNSELECTED = "false";
  public static final String FILE_LAST_OPEN = "lastOpenFileName";
  public static final String FILE_LAST_SAVE = "lastSaveFileName";

  /** static reference to this instance of class FrmMain */
  public static Frame mainFrame;

  /** single instance of a dynamic resource for use by all */
  public static DynamicResource dynRes =
      new DynamicResource();

  /** SimplyHTML's main resource bundle (plug-ins use their own) */
  public static ResourceBundle resources;

  /** the plug-in manager of SimplyHTML */
  public static PluginManager pluginManager; // = new PluginManager(mainFrame);

  /**
   * static at this point ensures resources are available when
   * inner classes need them upon construction
   */
  static {
    try {
      resources = ResourceBundle.getBundle(
        "com.lightdev.app.shtm.resources.SimplyHTML", Locale.getDefault());
    }
    catch(MissingResourceException mre) {
      Util.errMsg(null, "resources/SimplyHTML.properties not found", mre);
    }
  }

  /** number of currently active tab */
  private int activeTabNo;

  /** currently active DocumentPane */
  private DocumentPane dp;

  /** currently active SHTMLEditorPane */
  private SHTMLEditorPane editor;

  /** currently active SHTMLDocument */
  private SHTMLDocument doc;

  /** tool bar for formatting commands */
  private JToolBar formatToolBar;

  /** tool bar for formatting commands */
  private JToolBar paraToolBar;

  /** a frame for showing an element tree panel */
  private JFrame elementTreeFrame = null;

  /** the tabbed pane for adding documents to show to */
  private JTabbedPane jtpDocs;

  /** our help broker */
  private static HelpBroker hb;

  /** plugin menu ID */
  public final String pluginMenuId = "plugin";

  /** help menu ID */
  public final String helpMenuId = "help";

  /** id in ResourceBundle for a relative path to an empty menu icon */
  private String emptyIcon = "emptyIcon";

  /** watch for repeated key events */
  private RepeatKeyWatcher rkw = new RepeatKeyWatcher(40);

  /** counter for newly created documents */
  private int newDocCounter = 0;

  /** reference to applicatin temp directory */
  private static File appTempDir;

  /** tool bar selector for styles */
  private StyleSelector styleSelector;

  /** tool bar selector for certain tags */
  private TagSelector tagSelector;

  /** panel for plug-in display */
  private SplitPanel sp;

  /** indicates, whether document activation shall be handled */
  private boolean ignoreActivateDoc = false;

  /**
   * action names
   *
   * these have to correspond with the keys in the
   * resource bundle to allow for dynamic
   * menu creation and control
   */
  public static final String newAction = "new";
  public static final String openAction = "open";
  public static final String closeAction = "close";
  public static final String closeAllAction = "closeAll";
  private final String saveAction = "save";
  public static final String saveAsAction = "saveAs";
  private final String exitAction = "exit";
  private final String undoAction = "undo";
  private final String redoAction = "redo";
  private final String cutAction = "cut";
  private final String copyAction = "copy";
  private final String pasteAction = "paste";
  private final String selectAllAction = "selectAll";
  private final String fontAction = "font";
  private final String fontFamilyAction = "fontFamily";
  private final String fontSizeAction = "fontSize";
  private final String fontBoldAction = "fontBold";
  private final String fontItalicAction = "fontItalic";
  private final String fontUnderlineAction = "fontUnderline";
  public final String helpTopicsAction = "helpTopics";
  private final String aboutAction = "about";
  private final String gcAction = "gc";
  private final String elemTreeAction = "elemTree";
  private final String testAction = "test";
  private final String insertTableAction = "insertTable";
  private final String formatTableAction = "formatTable";
  private final String insertTableColAction = "insertTableCol";
  private final String insertTableRowAction = "insertTableRow";
  private final String appendTableRowAction = "appendTableRow";
  private final String appendTableColAction = "appendTableCol";
  private final String deleteTableRowAction = "deleteTableRow";
  private final String deleteTableColAction = "deleteTableCol";
  public static final String nextTableCellAction = "nextTableCell";
  public static final String prevTableCellAction = "prevTableCell";
  //private final String nextCellAction = "nextCell";
  //private final String prevCellAction = "prevCell";
  private final String toggleBulletsAction = "toggleBullets";
  private final String toggleNumbersAction = "toggleNumbers";
  private final String formatListAction = "formatList";
  public final String editPrefsAction = "editPrefs";
  private final String insertImageAction = "insertImage";
  private final String formatImageAction = "formatImage";
  private final String setStyleAction = "setStyle";
  private final String formatParaAction = "formatPara";
  private final String editNamedStyleAction = "editNamedStyle";
  private final String paraAlignLeftAction = "paraAlignLeft";
  private final String paraAlignCenterAction = "paraAlignCenter";
  private final String paraAlignRightAction = "paraAlignRight";
  private final String insertLinkAction = "insertLink";
  private final String editLinkAction = "editLink";
  private final String setTagAction = "setTag";
  private final String editAnchorsAction = "editAnchors";
  public static final String saveAllAction = "saveAll";
  public static final String documentTitleAction = "documentTitle";
  public static final String setDefaultStyleRefAction = "setDefaultStyleRef";
  public static final String findReplaceAction = "findReplace";

  /** construct a new main application frame */
  public FrmMain() {
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    SplashScreen splash = new SplashScreen();
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    initActions();
    setJMenuBar(dynRes.createMenubar(resources, "menubar"));
    customizeFrame();
    initAppTempDir();
    initPlugins();
    updateActions();
    initJavaHelp();
    splash.dispose();
    mainFrame = this;
    dynRes.getAction(newAction).actionPerformed(null);
    dp.getEditor().setCaretPosition(0);
  }

  /**
   * get the DynamicResource used in this instance of FrmMain
   *
   * @return the DynamicResource
   */
  public DynamicResource getDynRes() {
    return dynRes;
  }

  /**
   * get the resource bundle of this instance of FrmMain
   *
   * @return the bundle of resources
   */
  public ResourceBundle getResources() {
    return resources;
  }

  /**
   * get the temporary directory of SimplyHTML
   *
   * @return the temp dir
   */
  public static File getAppTempDir() {
    return appTempDir;
  }

  /**
   * get the file object for the document shown in the currently open DocumentPane
   *
   * @return the document file
   */
  public File getCurrentFile() {
    File file = null;
    URL url = dp.getSource();
    if(url != null) {
      file = new File(url.getFile());
    }
    return file;
  }

  /**
   * get the name of the file for the document shown in the currently open DocumentPane
   *
   * @return the document name
   */
  public String getCurrentDocName() {
    return dp.getDocumentName();
  }

  public SHTMLEditorPane getEditor() {
    return editor;
  }

  /**
   * get the DocumentPane object that is currently active
   *
   * @return the active DocumentPane
   */
  public DocumentPane getCurrentDocumentPane() {
    return dp;
  }

  /**
   * add a DocumentPaneListener from the currently active DocumentPane (if any)
   */
  public void addDocumentPaneListener(DocumentPane.DocumentPaneListener listener) {
    if(dp != null) {
      //System.out.println("FrmMain.addDocumentPaneListener dp.source=" + dp.getSource());
      dp.addDocumentPaneListener(listener);
    }
    else {
      //System.out.println("FrmMain.addDocumentPaneListener dp is null, did not add");
    }
  }

  /**
   * remove a DocumentPaneListener from the currently active DocumentPane (if any)
   */
  public void removeDocumentPaneListener(DocumentPane.DocumentPaneListener listener) {
    if(dp != null) {
      dp.removeDocumentPaneListener(listener);
    }
  }

  /**
   * initialize SimplyHTML's temporary directory
   */
  private void initAppTempDir() {
    appTempDir = new File(System.getProperty("user.home") +
                          File.separator + APP_NAME +
                          File.separator + APP_TEMP_DIR + File.separator);
    if(!appTempDir.exists()) {
      appTempDir.mkdirs();
    }
  }

  /**
   * find plug-ins and load them accordingly,
   * i.e. display / dock components and add
   * plug-in menus.
   */
  public void initPlugins() {
    pluginManager = new PluginManager(this);
    JMenu pMenu = dynRes.getMenu(pluginMenuId);
    JMenu hMenu;
    if(pMenu != null) {
      Container contentPane = getContentPane();
      pluginManager.loadPlugins();
      Enumeration plugins = pluginManager.plugins();
      SHTMLPlugin pi;
      JComponent pc;
      JMenuItem pluginMenu;
      JMenuItem helpMenu;
      while(plugins.hasMoreElements()) {
        pi = (SHTMLPlugin) plugins.nextElement();
        if(pi.isActive()) {
          refreshPluginDisplay(pi);
        }
      }
    }
    adjustDividers();
  }

  /**
   * adjust the divider sizes of SimplyHTML's SplitPanel
   * according to visibility
   */
  public void adjustDividers() {
    sp.adjustDividerSizes();
  }

  /**
   * watch for key events that are automatically repeated
   * due to the user holding down a key.
   *
   * <p>When a key is held down by the user, every keyPressed
   * event is followed by a keyTyped event and a keyReleased
   * event although the key is actually still down. I.e. it
   * can not be determined by a keyReleased event if a key
   * actually is released, which is why this implementation
   * is necessary.</p>
   */
  public class RepeatKeyWatcher implements KeyListener {

    /** timer for handling keyReleased events */
    private java.util.Timer releaseTimer = new java.util.Timer();

    /** the next scheduled task for a keyReleased event */
    private ReleaseTask nextTask;

    /** time of the last keyPressed event */
    private long lastWhen = 0;

    /** time of the current KeyEvent */
    private long when;

    /** delay to distinguish between single and repeated events */
    private long delay;

    /** indicates whether or not a KeyEvent currently occurs repeatedly */
    private boolean repeating = false;

    /**
     * construct a <code>RepeatKeyWatcher</code>
     *
     * @param delay  the delay in milliseconds until a
     * keyReleased event should be handled
     */
    public RepeatKeyWatcher(long delay) {
      super();
      this.delay = delay;
    }

    /**
     * handle a keyPressed event by cancelling the previous
     * release task (if any) and indicating repeated key press
     * as applicable.
     */
    public void keyPressed(KeyEvent e) {
      if(nextTask != null) {
        nextTask.cancel();
      }
      when = e.getWhen();
      if((when - lastWhen) <= delay) {
        repeating = true;
      }
      else {
        repeating = false;
      }
      lastWhen = when;
    }

    /**
     * handle a keyReleased event by scheduling a
     * <code>ReleaseTask</code>.
     */
    public void keyReleased(KeyEvent e) {
      nextTask = new ReleaseTask();
      releaseTimer.schedule(nextTask, delay);
    }

    public void keyTyped(KeyEvent e) { }

    /**
     * indicate whether or not a key is being held down
     *
     * @return true if a key is being held down, false if not
     */
    public boolean isRepeating() {
      return repeating;
    }

    /**
     * Task to be executed when a key is released
     */
    private class ReleaseTask extends TimerTask {
      public void run() {
        repeating = false;
        updateFormatControls();
      }
    }
  }

  public void clearDockPanels() {
    sp.removeAllOuterPanels();
  }

  /**
   * refresh the display for a given plug-in
   *
   * @param pi  the plug-in to refresh
   */
  public void refreshPluginDisplay(SHTMLPlugin pi) {
    JMenu pMenu = dynRes.getMenu(pluginMenuId);
    JMenu hMenu = dynRes.getMenu(helpMenuId);
    JMenuItem pluginMenu = pi.getPluginMenu();
    JMenuItem helpMenu = pi.getHelpMenu();
    JTabbedPane p = null;
    Preferences prefs;
    if(pi.isActive()) {
      JComponent pc = pi.getComponent();
      if(pc != null) {
        int panelNo = SplitPanel.WEST;
        double loc = 0.3;
        switch(pi.getDockLocation()) {
          case SHTMLPlugin.DOCK_LOCATION_LEFT:
            break;
          case SHTMLPlugin.DOCK_LOCATION_RIGHT:
            panelNo = SplitPanel.EAST;
            loc = 0.7;
            break;
          case SHTMLPlugin.DOCK_LOCATION_BOTTOM:
            panelNo = SplitPanel.SOUTH;
            loc = 0.7;
            break;
          case SHTMLPlugin.DOCK_LOCATION_TOP:
            panelNo = SplitPanel.NORTH;
            break;
        }
        p = (JTabbedPane) sp.getPanel(panelNo);
        p.setVisible(true);
        p.add(pi.getGUIName(), pc);
        if(((panelNo == SplitPanel.WEST) && sp.getDivLoc(panelNo) < this.getWidth() / 10) ||
           ((panelNo == SplitPanel.NORTH) && sp.getDivLoc(panelNo) < this.getHeight() / 10) ||
           ((panelNo == SplitPanel.EAST) && sp.getDivLoc(panelNo) > this.getWidth() - (this.getWidth() / 10)) ||
           ((panelNo == SplitPanel.SOUTH) && sp.getDivLoc(panelNo) > this.getHeight() - (this.getHeight() / 10)))
        {
          sp.setDivLoc(panelNo, loc);
        }
      }
      if(pluginMenu != null) {
        Icon menuIcon = pluginMenu.getIcon();
        if(menuIcon == null) {
          URL url = dynRes.getResource(resources, emptyIcon);
          if (url != null) {
            menuIcon = new ImageIcon(url);
            pluginMenu.setIcon(new ImageIcon(url));
          }
        }
        pMenu.add(pluginMenu);
      }
      if(helpMenu != null) {
        //System.out.println("FrmMain.refreshPluginDisplay insert helpMenu");
        if(helpMenu.getSubElements().length > 0) {
          Icon menuIcon = helpMenu.getIcon();
          if(menuIcon == null) {
            URL url = dynRes.getResource(resources, emptyIcon);
            if (url != null) {
              menuIcon = new ImageIcon(url);
              helpMenu.setIcon(new ImageIcon(url));
            }
          }
        }
        hMenu.insert(helpMenu, hMenu.getItemCount() - 2);
      }
      SwingUtilities.invokeLater(new PluginInfo(pi));
    }
    else {
      if(pluginMenu != null) {
        pMenu.remove(pluginMenu);
      }
      if(helpMenu != null) {
        hMenu.remove(helpMenu);
      }
    }
  }

  class PluginInfo implements Runnable {
    SHTMLPlugin pi;
    public PluginInfo(SHTMLPlugin pi) {
      this.pi = pi;
    }
    public void run() {
      pi.showInitialInfo();
    }
  }

  /**
   * get a <code>HelpBroker</code> for our application,
   * store it for later use and connect it to the help menu.
   */
  private void initJavaHelp() {
    try {
      URL url = this.getClass().getResource(APP_HELP_NAME +
          Util.URL_SEPARATOR + APP_HELP_NAME + JAVA_HELP_EXT);
      HelpSet hs = new HelpSet(null, url);
      hb = hs.createHelpBroker();
      JMenuItem mi = dynRes.getMenuItem(helpTopicsAction);
      CSH.setHelpIDString(mi, "item15");
      mi.addActionListener(new CSH.DisplayHelpFromSource(getHelpBroker()));
      mi.setIcon(dynRes.getIconForCommand(resources, helpTopicsAction));
      mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
      mi.setEnabled(true);
    }
    catch (Exception e) {
      Util.errMsg(this,
                  dynRes.getResourceString(resources, "helpNotFoundError"),
                  e);
    }
  }

  /**
   * get the <code>HelpBroker</code> of our application
   *
   * @return the <code>HelpBroker</code> to be used for help display
   */
  public static HelpBroker getHelpBroker() {
    return hb;
  }

  /**
   * instantiate Actions and put them into the commands
   * Hashtable for later use along with their action commands.
   *
   * This is hard coded as Actions need to be instantiated
   * hard coded anyway, so we do the storage in <code>commands</code>
   * right away.
   */
  private void initActions() {
    dynRes.addAction(findReplaceAction, new FindReplaceAction());
    dynRes.addAction(setDefaultStyleRefAction, new SetDefaultStyleRefAction());
    dynRes.addAction(documentTitleAction, new DocumentTitleAction());
    dynRes.addAction(saveAllAction, new SHTMLFileSaveAllAction());
    dynRes.addAction(editAnchorsAction, new EditAnchorsAction());
    dynRes.addAction(setTagAction, new SetTagAction());
    dynRes.addAction(editLinkAction, new EditLinkAction());
    dynRes.addAction(prevTableCellAction, new PrevTableCellAction());
    dynRes.addAction(nextTableCellAction, new NextTableCellAction());
    dynRes.addAction(editNamedStyleAction, new EditNamedStyleAction());
    dynRes.addAction(formatParaAction, new FormatParaAction());
    dynRes.addAction(setStyleAction, new SetStyleAction());
    dynRes.addAction(formatImageAction, new FormatImageAction());
    dynRes.addAction(insertImageAction, new InsertImageAction());
    dynRes.addAction(editPrefsAction, new SHTMLEditPrefsAction());
    dynRes.addAction(toggleBulletsAction, new ToggleListAction(toggleBulletsAction, HTML.Tag.UL));
    dynRes.addAction(toggleNumbersAction, new ToggleListAction(toggleNumbersAction, HTML.Tag.OL));
    dynRes.addAction(formatListAction, new FormatListAction());
    dynRes.addAction(ManagePluginsAction.managePluginsAction,
                     new ManagePluginsAction());
    dynRes.addAction(newAction, new SHTMLFileNewAction());
    dynRes.addAction(openAction, new SHTMLFileOpenAction());
    dynRes.addAction(closeAction, new SHTMLFileCloseAction());
    dynRes.addAction(closeAllAction, new SHTMLFileCloseAllAction());
    dynRes.addAction(saveAction, new SHTMLFileSaveAction());
    dynRes.addAction(saveAsAction, new SHTMLFileSaveAsAction());
    dynRes.addAction(exitAction, new SHTMLFileExitAction());
    dynRes.addAction(elemTreeAction, new ShowElementTreeAction());
    dynRes.addAction(gcAction, new GCAction());
    dynRes.addAction(testAction, new SHTMLFileTestAction());
    dynRes.addAction(undoAction, new UndoAction());
    dynRes.addAction(redoAction, new RedoAction());
    dynRes.addAction(cutAction, new SHTMLEditCutAction());
    dynRes.addAction(copyAction, new SHTMLEditCopyAction());
    dynRes.addAction(pasteAction, new SHTMLEditPasteAction());
    dynRes.addAction(selectAllAction, new SHTMLEditSelectAllAction());
    dynRes.addAction(aboutAction, new SHTMLHelpAppInfoAction());
    dynRes.addAction(fontAction, new FontAction());
    dynRes.addAction(fontFamilyAction, new FontFamilyAction());
    dynRes.addAction(fontSizeAction, new FontSizeAction());
    dynRes.addAction(insertTableAction, new InsertTableAction());
    dynRes.addAction(insertTableRowAction, new InsertTableRowAction());
    dynRes.addAction(insertTableColAction, new InsertTableColAction());
    dynRes.addAction(appendTableColAction, new AppendTableColAction());
    dynRes.addAction(appendTableRowAction, new AppendTableRowAction());
    dynRes.addAction(deleteTableRowAction, new DeleteTableRowAction());
    dynRes.addAction(deleteTableColAction, new DeleteTableColAction());
    dynRes.addAction(formatTableAction, new FormatTableAction());
    dynRes.addAction(fontBoldAction, new BoldAction());
    dynRes.addAction(fontItalicAction, new ItalicAction());
    dynRes.addAction(fontUnderlineAction, new UnderlineAction());
    dynRes.addAction(paraAlignLeftAction, new ToggleAction(paraAlignLeftAction,
              CSS.Attribute.TEXT_ALIGN, Util.CSS_ATTRIBUTE_ALIGN_LEFT));
    dynRes.addAction(paraAlignCenterAction, new ToggleAction(paraAlignCenterAction,
              CSS.Attribute.TEXT_ALIGN, Util.CSS_ATTRIBUTE_ALIGN_CENTER));
    dynRes.addAction(paraAlignRightAction, new ToggleAction(paraAlignRightAction,
              CSS.Attribute.TEXT_ALIGN, Util.CSS_ATTRIBUTE_ALIGN_RIGHT));
  }

  /**
   * update all actions
   */
  public void updateActions() {
    Action action;
    Enumeration actions = dynRes.getActions();
    while(actions.hasMoreElements()) {
      action = (Action) actions.nextElement();
      if(action instanceof SHTMLAction) {
        ((SHTMLAction) action).update();
      }
    }
  }

  /** customize the frame to our needs */
  private void customizeFrame() {
    setSize(new Dimension(800, 600));
    setTitle(APP_NAME);

    setIconImage(Toolkit.getDefaultToolkit().createImage(FrmMain.dynRes.getResource(FrmMain.resources, "appIcon")));

    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    sp = new SplitPanel();
    for(int i = 0; i < 4; i++) {
      JTabbedPane p = new JTabbedPane();
      p.setVisible(false);
      sp.addComponent(p, i);
    }

    jtpDocs = new JTabbedPane();
    jtpDocs.addChangeListener(this);
    sp.addComponent(jtpDocs, SplitPanel.CENTER);

    JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
    toolBarPanel.add(createToolBar("toolBar"));
    formatToolBar = createToolBar("formatToolBar");
    paraToolBar = createToolBar("paraToolBar");
    toolBarPanel.add(formatToolBar);
    toolBarPanel.add(paraToolBar);
    contentPane.add(toolBarPanel, BorderLayout.NORTH);
    //contentPane.add(workPanel, BorderLayout.CENTER);
    contentPane.add(sp, BorderLayout.CENTER);
    //contentPane.add(workPanel);
  }

  /**
   * catch requests to close the application's main frame to
   * ensure proper clean up before the application is
   * actually terminated.
   */
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      dynRes.getAction(exitAction).actionPerformed(
                new ActionEvent(this, 0, exitAction));
      if(jtpDocs.getTabCount() == 0) {
        super.processWindowEvent(e);
      }
    }
    else {
      super.processWindowEvent(e);
    }
  }

  /**
   * Create a tool bar.  This reads the definition of a tool bar
   * from the associated resource file.
   *
   * @param nm  the name of the tool bar definition in the resource file
   *
   * @return the created tool bar
   */
  public JToolBar createToolBar(String nm) {
    ToggleBorderListener tbl = new ToggleBorderListener();
    ButtonGroup bg = new ButtonGroup();
    Action action;
    AbstractButton newButton;
    Dimension buttonSize = new Dimension(24, 24);
    Dimension comboBoxSize = new Dimension(300, 24);
    Dimension separatorSize = new Dimension(3, 24);
    JSeparator separator;
    String[] itemKeys = Util.tokenize(
        dynRes.getResourceString(resources, nm), " ");
    JToolBar toolBar = new JToolBar();
    toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE );
    for (int i = 0; i < itemKeys.length; i++) {
      /** special handling for separators */
      if (itemKeys[i].equals(dynRes.menuSeparatorKey)) {
        separator = new JSeparator(JSeparator.VERTICAL);
        separator.setMaximumSize(separatorSize);
        toolBar.add(separator);
      }
      /**
       * special handling for list elements in the
       * tool bar
       */
      else if(itemKeys[i].equalsIgnoreCase(fontFamilyAction)) {
	FontFamilyPicker fontFamily = new FontFamilyPicker();
        fontFamily.setPreferredSize(new Dimension(180, 23));
        fontFamily.setAction(dynRes.getAction(fontFamilyAction));
        fontFamily.setMaximumSize(comboBoxSize);
	toolBar.add(fontFamily);
      }
      else if(itemKeys[i].equalsIgnoreCase(fontSizeAction)) {
	FontSizePicker fontSize = new FontSizePicker(CSS.Attribute.FONT_SIZE);
        fontSize.setPreferredSize(new Dimension(50, 23));
        fontSize.setAction(dynRes.getAction(fontSizeAction));
        fontSize.setMaximumSize(comboBoxSize);
	toolBar.add(fontSize);
      }
      else if(itemKeys[i].equalsIgnoreCase(setStyleAction)) {
        styleSelector = new StyleSelector(HTML.Attribute.CLASS);
        styleSelector.setPreferredSize(new Dimension(110, 23));
        styleSelector.setAction(dynRes.getAction(setStyleAction));
        styleSelector.setMaximumSize(comboBoxSize);
        jtpDocs.addChangeListener(styleSelector);
        toolBar.add(styleSelector);
      }
      else if(itemKeys[i].equalsIgnoreCase(setTagAction)) {
        tagSelector = new TagSelector();
        tagSelector.setAction(dynRes.getAction(setTagAction));
        /*
        styleSelector = new StyleSelector(HTML.Attribute.CLASS);
        styleSelector.setPreferredSize(new Dimension(110, 23));
        styleSelector.setAction(dynRes.getAction(setStyleAction));
        styleSelector.setMaximumSize(comboBoxSize);
        jtpDocs.addChangeListener(styleSelector);
        */
        toolBar.add(tagSelector);
      }
      else if(itemKeys[i].equalsIgnoreCase(helpTopicsAction)) {
        newButton = new JButton();
        try {
          CSH.setHelpIDString(newButton, "item15");
          newButton.addActionListener(
              new CSH.DisplayHelpFromSource(getHelpBroker()));
          newButton.setIcon(dynRes.getIconForCommand(resources, itemKeys[i]));
          newButton.setToolTipText(dynRes.getResourceString(
              resources, itemKeys[i] + dynRes.toolTipSuffix));
          toolBar.add(newButton);
        }
        catch(Exception e) {}
      }
      else {
        action = dynRes.getAction(itemKeys[i]);
        /**
         * special handling for JToggleButtons in the tool bar
         */
        if(action instanceof AttributeComponent) {
          newButton =
              new JToggleButton("", (Icon) action.getValue(Action.SMALL_ICON));
          newButton.addMouseListener(tbl);
          newButton.setAction(action);
          newButton.setText("");
          //newButton.setActionCommand("");
          newButton.setBorderPainted(false);
          action.addPropertyChangeListener(new ToggleActionChangedListener((JToggleButton) newButton));
          Icon si = dynRes.getIconForName(resources, action.getValue(action.NAME) + DynamicResource.selectedIconSuffix);
          if(si != null) {
            newButton.setSelectedIcon(si);
          }
          newButton.setMargin(new Insets(0, 0, 0, 0));
          newButton.setIconTextGap(0);
          newButton.setContentAreaFilled(false);
          newButton.setHorizontalAlignment(SwingConstants.CENTER);
          newButton.setVerticalAlignment(SwingConstants.CENTER);
          toolBar.add(newButton);
          if(itemKeys[i].equalsIgnoreCase(paraAlignLeftAction) ||
             itemKeys[i].equalsIgnoreCase(paraAlignCenterAction) ||
             itemKeys[i].equalsIgnoreCase(paraAlignRightAction))
          {
            bg.add(newButton);
          }
        }
        /**
         * this is the usual way to add tool bar buttons finally
         */
        else {
          newButton = toolBar.add(action);
        }
        newButton.setMinimumSize(buttonSize);
        newButton.setPreferredSize(buttonSize);
        newButton.setMaximumSize(buttonSize);
        newButton.setFocusPainted(false);
        newButton.setRequestFocusEnabled(false);
      }
    }
    return toolBar;
  }

  /**
   * displays or removes an etched border around JToggleButtons
   * this listener is registered with.
   */
  private class ToggleBorderListener implements MouseListener {
    private EtchedBorder border = new EtchedBorder(EtchedBorder.LOWERED);
    private JToggleButton button;
    public void mouseClicked(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) {
      Object src = e.getSource();
      if(src instanceof JToggleButton) {
        button = (JToggleButton) src;
        if(button.isEnabled()) {
          ((JToggleButton) src).setBorder(border);
        }
      }
    }
    public void mouseExited(MouseEvent e) {
      Object src = e.getSource();
      if(src instanceof JToggleButton) {
        ((JToggleButton) src).setBorder(null);
      }
    }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
  }

  /**
   * register FrmMain as an object which has interest
   * in events from a given document pane
   */
  public void registerDocument() {
    doc.addUndoableEditListener(undoHandler);
    editor.addCaretListener(this);
    editor.addKeyListener(rkw);
    ((SHTMLDocument) dp.getDocument()).getStyleSheet().addChangeListener(styleSelector);
  }

  /**
   * remove FrmMain as a registered object from a given
   * document pane and its components
   *
   * remove all plug-ins owned by this FrmMain from
   * SimplyHTML objects too
   */
  public void unregisterDocument() {
    editor.removeCaretListener(this);
    editor.removeKeyListener(rkw);
    if(doc != null) {
      doc.removeUndoableEditListener(undoHandler);
    }
    dp.removeAllListeners(); // for plug-in removal from any dp that is about to close
    doc.getStyleSheet().removeChangeListener(styleSelector);
    //System.out.println("FrmMain unregister document dp.name=" + dp.getDocumentName());
  }

  /**
   * save a document and catch possible errors
   *
   * this is shared by save and saveAs so we put it here to avoid redundancy
   *
   * @param dp  the document pane containing the document to save
   */
  public void doSave(DocumentPane dp) {
    try {
      dp.saveDocument();
    }
    /**
     * this exception should never happen as the menu allows to save a
     * document only if a name has been set. For new documents, whose
     * name is not set, only save as is enabled anyway.
     *
     * Just in case this is changed without remembering why it was designed
     * that way, we catch the exception here.
     */
    catch(DocNameMissingException e) {
      Util.errMsg(this, dynRes.getResourceString(resources, "docNameMissingError"), e);
    }
  }

  /**
   * get action properties from the associated resource bundle
   *
   * @param action the action to apply properties to
   * @param cmd the name of the action to get properties for
   */
  public static void getActionProperties(Action action, String cmd) {
    Icon icon = dynRes.getIconForCommand(resources, cmd);
    if (icon != null) {
      action.putValue(Action.SMALL_ICON, icon);
    }
    /*else {
      action.putValue(Action.SMALL_ICON, emptyIcon);
    }*/
    String toolTip = dynRes.getResourceString(resources, cmd + dynRes.toolTipSuffix);
    if(toolTip != null) {
      action.putValue(Action.SHORT_DESCRIPTION, toolTip);
    }
  }

  public int getActiveTabNo() {
    return activeTabNo;
  }

  /**
   * change listener to be applied to our tabbed pane
   * so that always the currently active components
   * are known
   */
  public void stateChanged(ChangeEvent e) {
    activeTabNo = jtpDocs.getSelectedIndex();
    dp = (DocumentPane) jtpDocs.getComponentAt(activeTabNo);
    editor = dp.getEditor();
    //System.out.println("FrmMain stateChanged docName now " + dp.getDocumentName());
    doc = (SHTMLDocument) editor.getDocument();
    //fireDocumentChanged();
    if(!ignoreActivateDoc) {
      dp.fireActivated();
    }
  }

  /* ---------- undo/redo implementation ----------------------- */

  /** Listener for edits on a document. */
  private UndoableEditListener undoHandler = new UndoHandler();

  /** UndoManager that we add edits to. */
  private UndoManager undo = new UndoManager();

  /** inner class for handling undoable edit events */
  public class UndoHandler implements UndoableEditListener {
    /**
     * Messaged when the Document has created an edit, the edit is
     * added to <code>undo</code>, an instance of UndoManager.
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      undo.addEdit(e.getEdit());
    }
  }

  /**
   * UndoAction for the edit menu
   */
  public class UndoAction extends AbstractAction implements SHTMLAction {
    public UndoAction() {
      super(undoAction);
      setEnabled(false);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
      try {
        undo.undo();
      }
      catch(CannotUndoException ex) {
        Util.errMsg((Component) e.getSource(),
		  dynRes.getResourceString(resources, "unableToUndoError") + ex, ex);
      }
    }

    public void update() {
      setEnabled(undo.canUndo());
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * RedoAction for the edit menu
   */
  public class RedoAction extends AbstractAction implements SHTMLAction {
    public RedoAction() {
      super(redoAction);
      setEnabled(false);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_Z, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
    }

    public void actionPerformed(ActionEvent e) {
      try {
        undo.redo();
      }
      catch(CannotRedoException ex) {
        Util.errMsg((Component) e.getSource(),
	      dynRes.getResourceString(resources, "unableToRedoError") + ex, ex);
      }
      updateActions();
    }

    public void update() {
      setEnabled(undo.canRedo());
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /* ---------- undo/redo implementation end ------------------- */



  /* ---------- application actions start ----------------
         (see also undo/redo implementation above) */

  /** just adds a normal name to the superclasse's action */
  public class SHTMLEditCutAction extends DefaultEditorKit.CutAction
	implements SHTMLAction
  {
    public SHTMLEditCutAction() {
      super();
      putValue(Action.NAME, cutAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_X, KeyEvent.CTRL_MASK));
    }
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      updateActions();
    }
    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        setEnabled(true);
      }
      else {
        setEnabled(false);
      }
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /** just adds a normal name to the superclasse's action */
  public class SHTMLEditCopyAction extends DefaultEditorKit.CopyAction
	implements SHTMLAction
  {
    public SHTMLEditCopyAction() {
      super();
      putValue(Action.NAME, copyAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    }
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      updateActions();
    }
    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        setEnabled(true);
      }
      else {
        setEnabled(false);
      }
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /** just adds a normal name to the superclasse's action */
  public class SHTMLEditPasteAction extends DefaultEditorKit.PasteAction
	implements SHTMLAction
  {
    public SHTMLEditPasteAction() {
      super();
      putValue(Action.NAME, pasteAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_V, KeyEvent.CTRL_MASK));
    }
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      updateActions();
    }
    public void update() {
      try {
        Clipboard cb = getToolkit().getSystemClipboard();
        Transferable data = cb.getContents(this);
        if(jtpDocs.getTabCount() > 0 && data != null) {
          setEnabled(true);
        }
        else {
          setEnabled(false);
        }
      }
      catch(Exception e) {
        setEnabled(false);
        Util.errMsg(null, null, e);
      }
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  public class SHTMLEditSelectAllAction extends AbstractAction
        implements SHTMLAction
  {
    public SHTMLEditSelectAllAction() {
      super();
      putValue(Action.NAME, selectAllAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_A, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      editor.selectAll();
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /** create a new empty document and show it */
  public class SHTMLFileNewAction extends AbstractAction
	implements SHTMLAction
  {
    public SHTMLFileNewAction() {
      super(newAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_N, KeyEvent.CTRL_MASK));
    }

    /** create a new empty document and show it */
    public void actionPerformed(ActionEvent ae) {
      Component gp = getGlassPane();                  // show wait cursor
      Cursor savedCursor = gp.getCursor();
      gp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      gp.setVisible(true);

      Frame parent = (Frame) getRootPane().getParent();
      dp = new DocumentPane(null, ++newDocCounter/*, renderMode*/);   // create a new empty document
      jtpDocs.setSelectedComponent(                   // add the document to the
            jtpDocs.add(dp.getDocumentName(), dp));   // tabbed pane for display

      registerDocument();

      gp.setCursor(savedCursor);                      // restore cursor
      gp.setVisible(false);
      updateActions();
    }

    public void update() {
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /** open an existing document from file and show it */
  public class SHTMLFileOpenAction extends AbstractAction
	implements SHTMLAction
  {
    public SHTMLFileOpenAction() {
      super(openAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      Preferences prefs = Preferences.userNodeForPackage( getClass() );
      JFileChooser chooser = new JFileChooser();        // create a file chooser
      ExampleFileFilter filter = new ExampleFileFilter();     // create a filter
      filter.addExtension("htm");
      filter.addExtension("html");
      filter.setDescription(dynRes.getResourceString(resources, "htmlFileDesc"));
      chooser.setFileFilter(filter);                    // apply the file filter
      String lastFileName = prefs.get(FILE_LAST_OPEN, "");
      if(lastFileName.length() > 0) {
        chooser.setCurrentDirectory(new File(lastFileName).getParentFile());
      }
      int returnVal =                             // ..and show the file chooser
        chooser.showOpenDialog((Component) ae.getSource());
      if(returnVal == JFileChooser.APPROVE_OPTION) {   // if a file was selected
        File file = chooser.getSelectedFile();
        prefs.put(FILE_LAST_OPEN, file.getAbsolutePath());
        openDocument(file);
      }
      updateActions();
    }

    public void openDocument(File file) {
      openDocument(file, null);
    }

    public void openDocument(File file, DocumentPane.DocumentPaneListener listener) {
      int openDocNo = -1;
      try {
        openDocNo = getOpenDocument(file.toURL().toString());
      }
      catch(MalformedURLException mue) {}
      if(openDocNo > -1) {
        //System.out.println("FrmMain.SHTMLFileOpenAction.openAction setting to open doc no " + openDocNo);
        jtpDocs.setSelectedIndex(openDocNo);
      }
      else {
        //System.out.println("FrmMain.SHTMLFileOpenAction.openAction loading file " + file);
        FileLoader loader = new FileLoader(file, null, listener);
        loader.start();
      }
    }

    public int getOpenDocument(String url) {
      int tabNo = -1;
      int openDocCount = jtpDocs.getTabCount();
      int i = 0;
      while(i < openDocCount && tabNo < 0) {
        URL source = ((DocumentPane) jtpDocs.getComponentAt(i)).getSource();
        if(source != null) {
          if(source.toString().equalsIgnoreCase(url)) {
            tabNo = i;
          }
        }
        i++;
      }
      return tabNo;
    }

    /**
     * get a FileLoader object for the document currently active
     *
     * @param url  the url of the file to open
     */
    public FileLoader createFileLoader(URL url) {
      return new FileLoader(new File(url.getFile()), null);
    }

    /**
     * Helper class for being able to load a document in a separate thread.
     * Using a separate thread will not cause the application to block during
     * a lengthy load operation
     */
    public class FileLoader extends Thread {
      File file;
      Component owner;
      DocumentPane.DocumentPaneListener l;
      public FileLoader(File file, Component owner) {
        this.file = file;
        this.owner = owner;
      }
      public FileLoader(File file, Component owner, DocumentPane.DocumentPaneListener listener) {
        this(file, owner);
        this.l = listener;
      }
      public void run() {
        try {
          Frame parent = (Frame) getRootPane().getParent();
          dp = new DocumentPane(file.toURL(), 0/*, renderMode*/);
          if(l != null) {
            dp.addDocumentPaneListener(l);
          }
          jtpDocs.setSelectedComponent(
              jtpDocs.add(dp.getDocumentName(), dp));
	  registerDocument();
        }
        catch(Exception e) {
          Util.errMsg(owner, dynRes.getResourceString(resources, "unableToOpenFileError"), e);
        }
      }
    }

    public void update() {
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * close a document.
   *
   * <p>the action takes into account whether or not a document needs to be
   * saved.</p>
   *
   * <p>By having the actual closing task in a separate public method of this
   * action, the close functionality can be shared with action 'close all' or
   * others that might need it.</p>
   */
  public class SHTMLFileCloseAction extends AbstractAction
	implements SHTMLAction
  {

    private boolean exitApp = false;

    /** constructor */
    public SHTMLFileCloseAction() {
      super(closeAction);
      getProperties();
    }

    /** close the currently active document, if there is one */
    public void actionPerformed(ActionEvent ae) {
      if(jtpDocs.getTabCount() > 0) {                   // if documents are open
        closeDocument(activeTabNo, ae, false);  // close the active one
      }
      updateActions();
    }

    /**
     * close a document by its tab index.
     *
     * <p>The method takes care of saving the document if necessary prior
     * to closing.</p>
     *
     * @param the tab index number of the document in the tabbed pane.
     * @return true, if the document was closed successfully.
     */
    public void closeDocument(final int index, ActionEvent ae, boolean ignoreChanges) {
      //System.out.println("closeDocument index=" + index);
      exitApp = ae.getActionCommand().indexOf(exitAction) > -1;
      final DocumentPane dp = (DocumentPane) jtpDocs.getComponentAt(index);
      if(!dp.saveInProgress()) {            // if no save is going on and..
        //System.out.println("closeDocument: no save is going on");
        if(ignoreChanges) {
          closeDoc(dp);
        }
        else {
          if(dp.needsSaving()) {              // ..the document needs to be saved
            //System.out.println("closeDocument: " + dp.getDocumentName() + " needsSaving");
            ignoreActivateDoc = true;
            jtpDocs.setSelectedIndex(index);
            ignoreActivateDoc = false;
            String docName = dp.getDocumentName();
            int choice = Util.msgChoice(JOptionPane.YES_NO_CANCEL_OPTION, "confirmClosing", "saveChangesQuery", docName, "\r\n\r\n");
            switch(choice) {
              case JOptionPane.YES_OPTION:           // if the user wanted to save
                if(dp.isNewDoc()) {                     //if the document is new
                  dynRes.getAction(saveAsAction).actionPerformed(ae); // 'save as'
                }
                else {                                             // else
                  dynRes.getAction(saveAction).actionPerformed(ae);   // 'save'
                }
                scheduleClose(dp);    //..and wait until it is finshed, then close
                break;
              case JOptionPane.NO_OPTION:       // if the user don't like to save
                closeDoc(dp);       // close the document without saving
                break;
              case JOptionPane.CANCEL_OPTION:             // if the user cancelled
                //System.out.println("closeDocument: save cancelled for " + dp.getDocumentName());
                break;                                    // do nothing
            }
          }
          else {                      // if the document does not need to be saved
            //System.out.println("closeDocument: " + dp.getDocumentName() + " NOT needsSaving");
            closeDoc(dp);             // close the document
          }
        }
      }
      else {                  // save was going on upon close request, so
        //System.out.println("closeDocument: a save is going on, wait");
        scheduleClose(dp);    // wait for completion, then close
      }
    }

    /**
     * schedule closing of a document.
     *
     * <p>This creates a <code>Timer</code> thread for which a
     * <code>TimerTask</code> is scheduled to peridically check
     * whether or not the save process for respective document commenced
     * successfully.</p>
     *
     * <p>If yes, Timer and TimerTask are disposed and the document
     * is closed. If not, the document remains open.</p>
     *
     * @param dp  the document to close
     * @param index  the number of the tab for that document
     */
    private void scheduleClose(final DocumentPane dp) {
      //System.out.println("scheduleClose for " + dp.getDocumentName());
      final java.util.Timer timer = new java.util.Timer();
      TimerTask task = new TimerTask() {
        public void run() {
          if(!dp.saveInProgress()) {                   // if done with saving
            if(dp.saveSuccessful) {                     // and all went fine
              closeDoc(dp);                          // close the document
              this.cancel();                            // dispose the task
              timer.cancel();                           // dispose the timer
            }
          }
        }
      };
      timer.schedule(task, 0, 400); // try to close every 400 milliseconds
    }

    /**
     * convenience method for closing a document
     */
    private void closeDoc(DocumentPane dp) {
      //System.out.println("closeDoc for document " + dp.getDocumentName());
      try {
        dp.deleteTempDir();
        unregisterDocument();
        //jtpDocs.remove(jtpDocs.indexOfComponent(dp));   // try to close the doc
        jtpDocs.remove(dp);
      }
      catch(IndexOutOfBoundsException e) { // if the tabs have changed meanwhile
        catchCloseErr(dp);
      }
      Component gp = getGlassPane();                   // restore defualt cursor
      gp.setVisible(true);
      gp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      gp.setVisible(false);
      if(exitApp) { // if the doc close was caused by a request to exit the app
        if(jtpDocs.getTabCount() == 0) {      // ..and if there are no open docs
          System.exit(0);                               // exit the application
        }
      }
    }

    private void catchCloseErr(DocumentPane dp) {
      try {
        int i = jtpDocs.indexOfComponent(dp);       // get the current tab index
        if(i < 0 && jtpDocs.getTabCount() > 0) {
          activeTabNo = jtpDocs.getSelectedIndex();
          dp = (DocumentPane) jtpDocs.getComponentAt(activeTabNo);
          i = jtpDocs.indexOfComponent(dp);   // get the current tab index again
          unregisterDocument();
          jtpDocs.remove(i);                                      //now remove it
        }
        else {
          while(i > 0 && i > jtpDocs.getTabCount()) {     // while its still wrong
            i = jtpDocs.indexOfComponent(dp);   // get the current tab index again
          }
          unregisterDocument();
          jtpDocs.remove(i);                                      //now remove it
        }
      }
      catch(IndexOutOfBoundsException e) {
        catchCloseErr(dp);
      }
    }

    /** update the state of this action */
    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * close all documents currently shown.
   *
   * <p>This action simply loops through all open documents and uses an instance
   * of SHTMLFileCloseAction to perform the actual closing on each of them.</p>
   */
  public class SHTMLFileCloseAllAction extends AbstractAction
	implements SHTMLAction
  {

    /** constructor */
    public SHTMLFileCloseAllAction() {
      super(closeAllAction);
      getProperties();
    }

    /** close all open documents */
    public void actionPerformed(ActionEvent ae) {
      SHTMLFileCloseAction a = (SHTMLFileCloseAction)dynRes.getAction(closeAction);
      for(int i = jtpDocs.getTabCount(); i > 0; i--) {
        //System.out.println("CloseAll, close tab no " + i);
        a.closeDocument(i-1, ae, false);
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  public class SHTMLFileSaveAllAction extends AbstractAction
        implements SHTMLAction
  {
    public SHTMLFileSaveAllAction() {
      super(saveAllAction);
      getProperties();
      /*putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_S, KeyEvent.CTRL_MASK));*/
    }

    public void actionPerformed(ActionEvent ae) {
      int count = jtpDocs.getTabCount();
      for(int i = 0; i < count; i++) {
        jtpDocs.setSelectedIndex(i);
        dp = (DocumentPane) jtpDocs.getSelectedComponent();
        if(dp.needsSaving()) {
          dynRes.getAction(saveAction).actionPerformed(ae);
        }
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /** save a document */
  public class SHTMLFileSaveAction extends AbstractAction
	implements SHTMLAction
  {
    public SHTMLFileSaveAction() {
      super(saveAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      if(!dp.isNewDoc()) {
        FileSaver saver = new FileSaver(dp);
        saver.setName("FileSaver");
        saver.start();
      }
      else {
        dynRes.getAction(saveAsAction).actionPerformed(ae);
      }
      updateActions();
    }

    /**
     * Helper class for being able to save a document in a separate thread.
     * Using a separate thread will not cause the application to block during
     * a lengthy save operation
     */
    class FileSaver extends Thread {
      DocumentPane dp;
      Component owner;
      FileSaver(DocumentPane dp) {
        setPriority(Thread.MIN_PRIORITY);
        this.dp = dp;
      }
      public void run() {
        doSave(this.dp);
      }
    }

    public void update() {
      boolean isEnabled = jtpDocs.getTabCount() > 0;
      boolean saveInProgress = false;
      boolean needsSaving = false;
      if(isEnabled) {
        saveInProgress = dp.saveInProgress();
        needsSaving = dp.needsSaving();
      }
      this.setEnabled(isEnabled && needsSaving && !saveInProgress);
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * save a document under a different name and/or location
   *
   * <p>If a file already exists at the chosen location / name, the method
   * will ask the user if the existing file shall be overwritten.
   */
  public class SHTMLFileSaveAsAction extends AbstractAction
	implements SHTMLAction
  {
    public SHTMLFileSaveAsAction() {
      super(saveAsAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      boolean canSave = true;
      Preferences prefs = Preferences.userNodeForPackage( getClass() );
      JFileChooser chooser = new JFileChooser();
      ExampleFileFilter filter = new ExampleFileFilter();
      filter.addExtension("htm");
      filter.addExtension("html");
      filter.setDescription(dynRes.getResourceString(resources, "htmlFileDesc"));
      chooser.setFileFilter(filter);
      String lastSaveFileName = prefs.get(FILE_LAST_SAVE, "");
      if(lastSaveFileName.length() > 0) {
        chooser.setCurrentDirectory(new File(lastSaveFileName).getParentFile());
      }
      URL sourceUrl = dp.getSource();
      String fName;
      if(sourceUrl != null) {
        fName = sourceUrl.getFile();
      }
      else {
        fName = dp.getDocumentName();
        //System.out.println("SHTMLFileSaveAsAction fName=" + fName);
        fName = Util.removeChar(fName, ' ');
        //System.out.println("SHTMLFileSaveAsAction fName=" + fName);
      }
      if(fName.indexOf(Util.CLASS_SEPARATOR) < 0) {
        chooser.setSelectedFile(new File(fName + ".htm"));
      }
      else {
        chooser.setSelectedFile(new File(fName));
      }
      int result = chooser.showSaveDialog((Component) ae.getSource());
      if(result == JFileChooser.APPROVE_OPTION) {
        File selection = chooser.getSelectedFile();
        prefs.put(FILE_LAST_SAVE, selection.getAbsolutePath());
        if(selection.exists()) {
          String newName = selection.getName();
          canSave = Util.msg(JOptionPane.YES_NO_OPTION, "confirmSaveAs", "fileExistsQuery", newName, " ");
        }
        if(canSave) {
          try {
            NewFileSaver saver = new NewFileSaver(
                                  dp, selection.toURL(), activeTabNo);
            saver.setName("NewFileSaver");
            saver.start();
          }
          catch(Exception ex) {
            Util.errMsg((Component) ae.getSource(),
                dynRes.getResourceString(resources, "cantCreateURLError") +
                    selection.getAbsolutePath(),
                ex);
          }
        }
      }
      updateActions();
    }

    /**
     * Helper class for being able to save a document in a separate thread.
     * Using a separate thread will not cause the application to block during
     * a lengthy save operation
     */
    public class NewFileSaver extends Thread {
      DocumentPane dp;
      URL url;
      int activeTabNo;
      DocumentPane.DocumentPaneListener l;
      NewFileSaver(DocumentPane dp, URL url, int activeTabNo) {
        this.dp = dp;
        this.url = url;
        this.activeTabNo = activeTabNo;
      }
      NewFileSaver(DocumentPane dp, URL url, int activeTabNo, DocumentPane.DocumentPaneListener listener) {
        this(dp, url, activeTabNo);
        this.l = listener;
      }
      public void run() {
        this.dp.setSource(url);
        doSave(this.dp);
        if(this.dp.saveSuccessful) {
          jtpDocs.setTitleAt(jtpDocs.indexOfComponent(this.dp),
					  this.dp.getDocumentName());
          if(l != null) {
            dp.addDocumentPaneListener(l);
          }
        }
      }
    }

    /**
     * get a FileSaver object for the document currently active
     *
     * @param url  the url of the file to save
     */
    public NewFileSaver createNewFileSaver(URL url) {
      return new NewFileSaver(dp, url, activeTabNo);
    }

    /**
     * get a FileSaver object for the document currently active
     *
     * @param url  the url of the file to save
     */
    public NewFileSaver createNewFileSaver(URL url, DocumentPane.DocumentPaneListener listener) {
      return new NewFileSaver(dp, url, activeTabNo, listener);
    }

    public void update() {
      boolean isEnabled = jtpDocs.getTabCount() > 0;
      boolean saveInProgress = false;
      if(isEnabled) {
        saveInProgress = dp.saveInProgress();
      }
      this.setEnabled(isEnabled && !saveInProgress);
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * exit the application.
   *
   * <p>This will only exit the application, if<ul>
   * <li>no documents are open or </li>
   * <li>documents are open that do not need to be saved or </li>
   * <li>documents are open and are saved successfully prior to close or </li>
   * <li>documents are open for which the user explicitly opted not
   *        to save them </li>
   * </ul></p>
   */
  public class SHTMLFileExitAction extends AbstractAction
	implements SHTMLAction
  {
    public SHTMLFileExitAction() {
      super(exitAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      //System.out.println("FrmMain.SHTMLFileExitAction.actionPerformed");
      saveRelevantPrefs();
      new SHTMLFileCloseAllAction().actionPerformed(ae);
      if(jtpDocs.getTabCount() == 0) {
        //removeAllListeners();
        System.exit(0);
      }
      updateActions();
    }

    public void saveRelevantPrefs() {
      //System.out.println("FrmMain.SHTMLFileExitAction.saveRelevantPrefs");

      /* ---- save splitpane sizes start -------------- */

      sp.savePrefs();

      /* ---- save splitpane sizes end -------------- */
    }

    public void update() {
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * a slot for testing certain things conveniently during development
   */
  public class SHTMLFileTestAction extends AbstractAction
	implements SHTMLAction
  {
    public SHTMLFileTestAction() {
      super(testAction);
      getProperties();
    }
    public void actionPerformed(ActionEvent ae) {

      //Util.errMsg(null, "no test action is implemented.", null);

      getEditor().insertBreak();

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
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * insert a new table
   */
  public class InsertTableAction extends AbstractAction
				implements SHTMLAction
  {

    public InsertTableAction() {
      super(insertTableAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      Object input = Util.nameInput(parent, "3", "insertTableTitle","insertTableMsg");
      if(input != null) {
        int choice = Integer.parseInt(input.toString());
        if(choice > 0) {
          editor.insertTable(choice);
        }
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * insert a new table column
   */
  public class InsertTableColAction extends AbstractAction
                                implements SHTMLAction
  {
    public InsertTableColAction() {
      super(insertTableColAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      editor.insertTableColumn();
    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * append a new table row
   */
  public class AppendTableRowAction extends AbstractAction
                                implements SHTMLAction
  {
    public AppendTableRowAction() {
      super(appendTableRowAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      editor.appendTableRow();
    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * delete a table row
   */
  public class DeleteTableRowAction extends AbstractAction
                                implements SHTMLAction
  {
    public DeleteTableRowAction() {
      super(deleteTableRowAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      editor.deleteTableRow();
    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * set the title of the currently active document
   */
  public class DocumentTitleAction extends AbstractAction
                                implements SHTMLAction
  {
    public DocumentTitleAction() {
      super(documentTitleAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      String newTitle;
      String currentTitle = doc.getDocumentTitle();
      if(currentTitle != null) {
        newTitle = currentTitle;
      }
      else {
        newTitle = "";
      }
      newTitle = Util.nameInput(FrmMain.mainFrame, newTitle, "docTitleTitle", "docTitleQuery");
      if(newTitle != null && newTitle.length() > 0) {
        doc.setDocumentTitle(newTitle);
      }
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * append a new table col
   */
  public class AppendTableColAction extends AbstractAction
                                implements SHTMLAction
  {
    public AppendTableColAction() {
      super(appendTableColAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      editor.appendTableColumn();
    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * delete a table col
   */
  public class DeleteTableColAction extends AbstractAction
                                implements SHTMLAction
  {
    public DeleteTableColAction() {
      super(deleteTableColAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      editor.deleteTableCol();
    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * insert a new table row
   */
  public class InsertTableRowAction extends AbstractAction
                                implements SHTMLAction
  {
    public InsertTableRowAction() {
      super(insertTableRowAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      editor.insertTableRow();
    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * format table attributes
   */
  public class FormatTableAction extends AbstractAction
				implements SHTMLAction
  {
    public FormatTableAction() {
      super(formatTableAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      editor.requestFocus();
      int pos = editor.getSelectionStart();
      TableDialog td = new TableDialog(parent,
                     dynRes.getResourceString(resources, "tableDialogTitle"));
      td.setTableAttributes(getMaxAttributes(editor, HTML.Tag.TABLE.toString()));
      td.setCellAttributes(getMaxAttributes(editor, HTML.Tag.TD.toString()));
      Util.center(parent, td);
      td.setModal(true);
      td.show();

      /** if the user made a selection, apply it to the document */
      if(td.getResult() == DialogShell.RESULT_OK) {
        AttributeSet a = td.getTableAttributes();
        if(a.getAttributeCount() > 0) {
          editor.applyTableAttributes(a);
        }
        a = td.getCellAttributes();
        if(a.getAttributeCount() > 0) {
          editor.applyCellAttributes(a, td.getCellRange());
        }
      }
      updateActions();
    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * toggle list formatting for a given type of list on/off
   */
  public class ToggleListAction extends AbstractAction
                                implements SHTMLAction
  {

    private HTML.Tag listTag;

    public ToggleListAction(String name, HTML.Tag listTag) {
      super(name);
      this.listTag = listTag;
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      editor.toggleList(listTag.toString(),
                        getMaxAttributes(editor, listTag.toString()),
                        false);
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * Change list formatting
   */
  public class FormatListAction extends AbstractAction
                                implements SHTMLAction
  {

    public FormatListAction() {
      super(formatListAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      editor.requestFocus();
      int pos = editor.getSelectionStart();
      ListDialog dlg = new ListDialog(parent,
                     dynRes.getResourceString(resources, "listDialogTitle"));
      SimpleAttributeSet set = new SimpleAttributeSet(
          getMaxAttributes(editor, HTML.Tag.UL.toString()));
      set.addAttributes(getMaxAttributes(editor, HTML.Tag.OL.toString()));
      dlg.setListAttributes(set);
      String currentTag = dlg.getListTag();
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();

      /** if the user made a selection, apply it to the document */
      if(dlg.getResult() == DialogShell.RESULT_OK) {
        AttributeSet a = dlg.getListAttributes();
        String newTag = dlg.getListTag();
        if(newTag == null) {
          editor.toggleList(newTag, a, true);
        }
        else if(newTag.equalsIgnoreCase(currentTag)) {
          if(a.getAttributeCount() > 0) {
            editor.applyListAttributes(a);
          }
        }
        else {
          editor.toggleList(newTag, a, false);
        }
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /** show information about SimplyHTML in a dialog */
  public class SHTMLHelpAppInfoAction extends AbstractAction
	implements SHTMLAction
  {
    public SHTMLHelpAppInfoAction() {
      super(aboutAction);
      getProperties();
    }
    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      AboutBox dlg = new AboutBox(parent);
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();
      repaint();
      updateActions();
    }
    public void update() {
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * Action that brings up a JFrame with a JTree showing the structure
   * of the document in the currently active DocumentPane.
   *
   * will be hidden from menu if not in development mode (DEV_MODE = false)
   */
  public class ShowElementTreeAction extends AbstractAction implements SHTMLAction {
    ShowElementTreeAction() {
      super(elemTreeAction);
      getProperties();
    }
    public void actionPerformed(ActionEvent e) {
      if(elementTreeFrame == null) {
        String title = dynRes.getResourceString(resources, "elementTreeTitle");
        elementTreeFrame = new JFrame(title);
        elementTreeFrame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent we) {
            elementTreeFrame.dispose();
            elementTreeFrame = null;
          }
        });
        Container fContentPane = elementTreeFrame.getContentPane();
        fContentPane.setLayout(new BorderLayout());
        int activeTabNo = jtpDocs.getSelectedIndex();
        ElementTreePanel elementTreePanel = new ElementTreePanel(editor);
        fContentPane.add(elementTreePanel);
        elementTreeFrame.pack();
      }
      elementTreeFrame.show();
      updateActions();
    }
    public void update() {
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * force a garbage collection. This can be helpful to find out
   * whether or not objects are properly disposed.
   *
   * Without forcing a garbage collection, this would happen
   * at random intervals so although an object might be properly
   * disposed, it might still be around until the next GC.
   *
   * will be hidden from menu if not in development mode (DEV_MODE = false)
   */
  public class GCAction extends AbstractAction implements SHTMLAction {
    GCAction() {
      super(gcAction);
      getProperties();
    }
    public void actionPerformed(ActionEvent e) {
      System.gc();
      updateActions();
    }
    public void update() {
    }
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  public class SHTMLEditPrefsAction extends AbstractAction
        implements SHTMLAction
  {
    public SHTMLEditPrefsAction() {
      super();
      putValue(Action.NAME, editPrefsAction);
      getProperties();
      /*putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_A, KeyEvent.CTRL_MASK));*/
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      PrefsDialog dlg = new PrefsDialog(parent,
                                       dynRes.getResourceString(resources,
                                       "prefsDialogTitle"));
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();

      /** if the user made a selection, apply it to the document */
      if(dlg.getResult() == DialogShell.RESULT_OK) {
      }
      updateActions();
    }

    public void update() {
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  public class InsertImageAction extends AbstractAction
        implements SHTMLAction
  {
    public InsertImageAction() {
      super();
      putValue(Action.NAME, insertImageAction);
      getProperties();
      /*putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_A, KeyEvent.CTRL_MASK));*/
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      ImageDialog dlg = new ImageDialog(parent,
                                       dynRes.getResourceString(resources,
                                       "imageDialogTitle"),
                                       dp.getImageDir());
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();

      /** if the user made a selection, apply it to the document */
      if(dlg.getResult() == DialogShell.RESULT_OK) {
        //System.out.println("imageHTML=\r\n\r\n" + dlg.getImageHTML());
        try {
          doc.insertBeforeStart(
              doc.getCharacterElement(editor.getSelectionEnd()),
              dlg.getImageHTML());
        }
        catch(Exception e) {
          Util.errMsg(null, e.getMessage(), e);
        }
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  public class FormatImageAction extends AbstractAction
        implements SHTMLAction
  {
    public FormatImageAction() {
      super();
      putValue(Action.NAME, formatImageAction);
      getProperties();
      /*putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_A, KeyEvent.CTRL_MASK));*/
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      ImageDialog dlg = new ImageDialog(parent,
                                       dynRes.getResourceString(resources,
                                       "imageDialogTitle"),
                                       dp.getImageDir(),
                                       (SHTMLDocument) dp.getDocument());
      Element img = doc.getCharacterElement(editor.getCaretPosition());
      if(img.getName().equalsIgnoreCase(HTML.Tag.IMG.toString())) {
        Util.center(parent, dlg);
        dlg.setImageAttributes(img.getAttributes());
        dlg.setModal(true);
        dlg.show();

        /** if the user made a selection, apply it to the document */
        if(dlg.getResult() == DialogShell.RESULT_OK) {
          //System.out.println("imageHTML=\r\n\r\n" + dlg.getImageHTML());
          try {
            doc.setOuterHTML(img, dlg.getImageHTML());
          }
          catch(Exception e) {
            Util.errMsg(null, e.getMessage(), e);
          }
        }
        updateActions();
      }
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        Element img = doc.getCharacterElement(editor.getCaretPosition());
        if(img.getName().equalsIgnoreCase(HTML.Tag.IMG.toString())) {
          this.setEnabled(true);
        }
        else {
          this.setEnabled(false);
        }
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /* ---------- other application actions end ------------------ */

  /* ---------- font manipulation code start ------------------ */

  /**
   * caret listener implementation to track format changes
   */
  public void caretUpdate(CaretEvent e) {
    if(!rkw.isRepeating()) {
      updateFormatControls();
    }
  }

  /**
   * update any controls that relate to formats at the
   * current caret position
   */
  private void updateFormatControls() {
    updateAToolBar(formatToolBar);
    updateAToolBar(paraToolBar);
    Element e = doc.getParagraphElement(editor.getCaretPosition());
    SetTagAction sta = (SetTagAction) tagSelector.getAction();
    sta.setIgnoreActions(true);
    tagSelector.setSelectedTag(e.getName());
    sta.setIgnoreActions(false);
  }

  private void updateAToolBar(JToolBar bar) {
    Component c;
    Action action;
    int count = bar.getComponentCount();
    AttributeSet a = getMaxAttributes(editor, null);
    for(int i = 0; i < count; i++) {
      c = bar.getComponentAtIndex(i);
      if(c instanceof AttributeComponent) {
        if(c instanceof StyleSelector) {
          SetStyleAction ssa = (SetStyleAction) ((StyleSelector) c).getAction();
          ssa.setIgnoreActions(true);
          ((AttributeComponent) c).setValue(a);
          ssa.setIgnoreActions(false);
        }
        else {
          ((AttributeComponent) c).setValue(a);
        }
      }
      else if(c instanceof AbstractButton) {
        action = ((AbstractButton) c).getAction();
        if((action != null) && (action instanceof AttributeComponent)) {
          ((AttributeComponent) action).setValue(a);
        }
      }
    }
  }

  /**
   * a JComboBox for selecting a font family names
   * from those available in the system.
   */
  public class FontFamilyPicker extends JComboBox implements AttributeComponent {

    /** switch for the action listener */
    private boolean ignoreActions = false;

    FontFamilyPicker() {

      /**
       * add the font family names available in the system
       * to the combo box
       */
      super(GraphicsEnvironment.getLocalGraphicsEnvironment().
				      getAvailableFontFamilyNames());
    }

    public boolean ignore() {
      return ignoreActions;
    }

    /**
     * set the value of this <code>AttributeComponent</code>
     *
     * @param a  the set of attributes possibly having an
     *          attribute this component can display
     *
     * @return true, if the set of attributes had a matching attribute,
     *            false if not
     */
    public boolean setValue(AttributeSet a) {
      ignoreActions = true;
      boolean success = false;
      if(a.isDefined(CSS.Attribute.FONT_FAMILY)) {
	setSelectedItem(a.getAttribute(CSS.Attribute.FONT_FAMILY).toString());
	success = true;
      }
      ignoreActions = false;
      return success;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      SimpleAttributeSet set = new SimpleAttributeSet();
      Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_FAMILY,
				(String) getSelectedItem());
      return set;
    }

    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }
  }

  /**
   * a JComboBox for selecting a font size
   */
  public class FontSizePicker extends JComboBox  implements AttributeComponent {
    private boolean ignoreActions = false;
    private Object key;
    FontSizePicker(Object key) {
      /**
       * add font sizes to the combo box
       */
      super(new String[] {"8", "10", "12", "14", "18", "24"} );
      this.key = key;
    }

    public boolean ignore() {
      return ignoreActions;
    }

    /**
     * set the value of this combo box
     *
     * @param a  the set of attributes possibly having a
     *          font size attribute this pick list could display
     *
     * @return true, if the set of attributes had a font size attribute,
     *            false if not
     */
    public boolean setValue(AttributeSet a) {
      ignoreActions = true;
      boolean success = false;
      Object attr = a.getAttribute(key);
      if(attr != null) {
        //System.out.println("FontSizePicker setValue attribute=" + a.getAttribute(key));
        int val = (int) Util.getAttrValue(a.getAttribute(key));
        if(val > 0) {
          success = true;
          setSelectedItem(new Integer(val).toString());
        }
        else {
          setSelectedItem("12");
        }
      }
      else {
        setSelectedItem("12");
      }
      ignoreActions = false;
      return success;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      SimpleAttributeSet set = new SimpleAttributeSet();
      Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_SIZE,
		      (String) getSelectedItem() /*+ "pt"*/);
      //set.addAttribute(HTML.Attribute.SIZE);
      return set;
    }
    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }
  }

  /**
   * Show a dialog to format fonts
   */
  public class FontAction extends AbstractAction implements SHTMLAction
  {
    public FontAction() {
      super(fontAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      editor.requestFocus();

      /** create a modal FontDialog, center and show it */
      FontDialog fd = new FontDialog(parent,
				    dynRes.getResourceString(resources, "fontDialogTitle"),
                                    getMaxAttributes(editor, null));
      Util.center(parent, fd);
      fd.setModal(true);
      fd.show();

      /** if the user made a selection, apply it to the document */
      if(fd.getResult() == FontDialog.RESULT_OK) {
        editor.applyAttributes(fd.getAttributes(), false);
        updateFormatControls();
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * change a font family setting
   */
  public class FontFamilyAction extends AbstractAction implements SHTMLAction
  {
    public FontFamilyAction() {
      super(fontFamilyAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      FontFamilyPicker ffp = ((FontFamilyPicker) ae.getSource());
      if(!ffp.ignore()) {
        editor.applyAttributes(ffp.getValue(), false);
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * action to set the style
   */
  public class SetStyleAction extends AbstractAction implements SHTMLAction
  {
    private boolean ignoreActions = false;

    public SetStyleAction() {
      super(setStyleAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      if(!ignoreActions) {
        StyleSelector styleSelector = (StyleSelector) ae.getSource();
        AttributeSet a = styleSelector.getValue();
        if(a != null) {
          //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
          //hd.listAttributes(a, 2);
          editor.applyAttributes(a, true);
        }
        updateActions();
      }
    }

    public void setIgnoreActions(boolean ignore) {
      ignoreActions = ignore;
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * action to find and replace a given text
   */
  public class FindReplaceAction extends AbstractAction implements SHTMLAction, FindReplaceListener
  {
    public FindReplaceAction() {
      super(findReplaceAction);
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      currentTab = jtpDocs.getSelectedIndex();
      caretPos = dp.getEditor().getCaretPosition();
      if(jtpDocs.getTabCount() > 1) {
        System.out.println("FindReplaceAction.actionPerformed with Listener");
        FindReplaceDialog frd = new FindReplaceDialog(mainFrame, getEditor(), this);
      }
      else {
        System.out.println("FindReplaceAction.actionPerformed NO Listener");
        FindReplaceDialog frd = new FindReplaceDialog(mainFrame, getEditor());
      }
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }

    public void getNextDocument(FindReplaceEvent e) {
      FindReplaceDialog frd = (FindReplaceDialog) e.getSource();
      int tabCount = jtpDocs.getTabCount();
      int curTab = jtpDocs.getSelectedIndex();
      System.out.println("FindReplaceAction.getNextDocument curTab=" + curTab + ", tabCount=" + tabCount);
      if(++curTab < tabCount) {
        System.out.println("FindReplaceAction.getNextDocument next tab no=" + curTab);
        resumeWithNewEditor(frd, curTab);
        /*
        jtpDocs.setSelectedIndex(curTab);
        DocumentPane docPane = (DocumentPane) jtpDocs.getComponentAt(curTab);
        JEditorPane editor = docPane.getEditor();
        editor.requestFocus();
        frd.setEditor(editor);
        frd.resumeOperation();
        */
      }
      else {
        frd.terminateOperation();
      }
    }

    public void getFirstDocument(FindReplaceEvent e) {
      FindReplaceDialog frd = (FindReplaceDialog) e.getSource();
      resumeWithNewEditor(frd, 0);
      /*DocumentPane docPane = (DocumentPane) jtpDocs.getComponentAt(0);
      jtpDocs.setSelectedIndex(0);
      JEditorPane editor = docPane.getEditor();
      editor.requestFocus();
      frd.setEditor(editor);
      frd.resumeOperation();*/
    }

    public void findReplaceTerminated(FindReplaceEvent e) {
      jtpDocs.setSelectedIndex(currentTab);
      DocumentPane docPane = (DocumentPane) jtpDocs.getSelectedComponent();
      JEditorPane editor = docPane.getEditor();
      editor.setCaretPosition(caretPos);
      editor.requestFocus();
    }

    private void resumeWithNewEditor(FindReplaceDialog frd, int tabNo) {
      jtpDocs.setSelectedIndex(tabNo);
      DocumentPane docPane = (DocumentPane) jtpDocs.getComponentAt(tabNo);
      JEditorPane editor = docPane.getEditor();
      editor.requestFocus();
      frd.setEditor(editor);
      frd.resumeOperation();
    }

    private int caretPos;
    private int currentTab;
  }

  /**
   * action to set the tag type
   */
  public class SetTagAction extends AbstractAction implements SHTMLAction
  {
    private boolean ignoreActions = false;

    public SetTagAction() {
      super(setTagAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      if(!ignoreActions) {
        String tag = tagSelector.getSelectedTag();
        editor.applyTag(tag, tagSelector.getTags());
        updateActions();
      }
    }

    public void setIgnoreActions(boolean ignore) {
      ignoreActions = ignore;
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * action to change the paragraph style
   */
  public class FormatParaAction extends AbstractAction implements SHTMLAction
  {
    public FormatParaAction() {
      super(formatParaAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      ParaStyleDialog dlg = new ParaStyleDialog(parent,
                                       dynRes.getResourceString(resources,
                                       "paraStyleDialogTitle"));
      Util.center(parent, dlg);
      dlg.setModal(true);
      //SHTMLDocument doc = (SHTMLDocument) dp.getDocument();
      dlg.setValue(getMaxAttributes(doc.getParagraphElement(editor.getCaretPosition()), doc.getStyleSheet()));
      dlg.show();

      /** if the user made a selection, apply it to the document */
      if(dlg.getResult() == DialogShell.RESULT_OK) {
        editor.applyAttributes(dlg.getValue(), true);
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * action to change the paragraph style
   */
  public class EditNamedStyleAction extends AbstractAction implements SHTMLAction
  {
    public EditNamedStyleAction() {
      super(editNamedStyleAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      ParaStyleDialog dlg = new ParaStyleDialog(parent,
                                       dynRes.getResourceString(resources,
                                       "namedStyleDialogTitle"),
                                       doc);
      Util.center(parent, dlg);
      dlg.setModal(true);
      //SHTMLDocument doc = (SHTMLDocument) dp.getDocument();
      dlg.setValue(getMaxAttributes(doc.getParagraphElement(editor.getCaretPosition()), doc.getStyleSheet()));
      //dlg.setValue(getMaxAttributes(editor, null));
      dlg.show();
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * action to edit a link
   */
  public class EditLinkAction extends AbstractAction implements SHTMLAction
  {
    public EditLinkAction() {
      super(editLinkAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      LinkDialog dlg = new LinkDialog(parent,
                                       dynRes.getResourceString(resources,
                                       "linkDialogTitle"),
                                       doc,
                                       editor.getSelectionStart(),
                                       editor.getSelectionEnd(),
                                       dp.getImageDir()/*,
                                       renderMode*/);
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();
      if(dlg.getResult() == DialogShell.RESULT_OK) {
        // apply link here
        editor.setLink(dlg.getLinkText(), dlg.getHref(), dlg.getStyleName(), dlg.getLinkImage(), dlg.getLinkImageSize());
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        if((editor.getSelectionEnd() > editor.getSelectionStart()) ||
           (Util.findLinkElementUp(doc.getCharacterElement(editor.getSelectionStart())) != null)) {
          this.setEnabled(true);
        }
        else {
          this.setEnabled(false);
        }
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * action to set a reference to the default style sheet
   * (for being able to use an already existing style sheet
   * without having to define named styles)
   */
  public class SetDefaultStyleRefAction extends AbstractAction implements SHTMLAction
  {
    public SetDefaultStyleRefAction() {
      super(setDefaultStyleRefAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      doc.insertStyleRef();
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0 && !doc.hasStyleRef()) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * action to edit anchors inside a document
   */
  public class EditAnchorsAction extends AbstractAction implements SHTMLAction
  {
    public EditAnchorsAction() {
      super(editAnchorsAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      Frame parent = (Frame) getRootPane().getParent();
      AnchorDialog dlg = new AnchorDialog(
          parent,
          FrmMain.dynRes.getResourceString(FrmMain.resources, "anchorDialogTitle"),
          doc/*,
          renderMode*/);
      Util.center(parent, dlg);
      dlg.setModal(true);
      dlg.show();
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * change a font size setting
   */
  public class FontSizeAction extends AbstractAction implements SHTMLAction
  {
    public FontSizeAction() {
      super(fontSizeAction);
      getProperties();
    }

    public void actionPerformed(ActionEvent ae) {
      FontSizePicker fsp = ((FontSizePicker) ae.getSource());
      if(!fsp.ignore()) {
        editor.applyAttributes(fsp.getValue(), false);
      }
      updateActions();
    }

    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * a listener for property change events on ToggleFontActions
   */
  private class ToggleActionChangedListener implements PropertyChangeListener {

    JToggleButton button;

    ToggleActionChangedListener(JToggleButton button) {
      super();
      this.button = button;
    }

    public void propertyChange(PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if (e.getPropertyName().equals(FrmMain.ACTION_SELECTED_KEY)) {
        //System.out.println("propertyName=" + propertyName + " newValue=" + e.getNewValue());
        if(e.getNewValue().toString().equals(FrmMain.ACTION_SELECTED)) {
          button.setSelected(true);
        }
        else {
          button.setSelected(false);
        }
      }
    }
  }

  /**
   * action to move to the previous cell in a table
   */
  public class PrevTableCellAction extends AbstractAction implements SHTMLAction
  {
    public PrevTableCellAction() {
      super(prevTableCellAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK));
    }

    public void actionPerformed(ActionEvent ae) {
      Element cell = editor.getCurTableCell();
      if(cell != null) {
        editor.goPrevCell(cell);
        updateActions();
      }
    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * action to move to the next cell in a table
   */
  public class NextTableCellAction extends AbstractAction implements SHTMLAction
  {
    public NextTableCellAction() {
      super(nextTableCellAction);
      getProperties();
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
          KeyEvent.VK_TAB, 0));
    }

    public void actionPerformed(ActionEvent ae) {

      Element cell = editor.getCurTableCell();
      if(cell != null) {
        editor.goNextCell(cell);
        updateActions();
      }

    }

    public void update() {
      if((jtpDocs.getTabCount() > 0) && (editor.getCurTableCell() != null)) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  public class ItalicAction extends StyledEditorKit.ItalicAction implements SHTMLAction, AttributeComponent {
    public ItalicAction() {
      //Action act = new StyledEditorKit.BoldAction();
      super();
      putValue(Action.NAME, fontItalicAction);
      putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      getActionProperties(this, fontItalicAction);
    }
    /**
     * do the format change for the associated attribute
     *
     * <p>This reverses the current setting for the associated attribute</p>
     *
     * @param  e  the ActionEvent describing the cause for this action
     */
    public void actionPerformed(ActionEvent e) {
      //System.out.println("ToggleAction getValue=" + getValue() + "selectedValue=" + selectedValue);
      //editor.applyAttributes(getValue(), (unselectedValue == null));
      super.actionPerformed(e);
      //if(unselectedValue != null) {
      if(editor != null) {
        SHTMLDocument doc = (SHTMLDocument) editor.getDocument();
        if (doc != null) {
          AttributeSet a = doc.getCharacterElement(editor.getSelectionStart()).
              getAttributes();
          boolean isItalic = StyleConstants.isItalic(a);
          //if(a.isDefined(attributeKey)) {
          //Object value = a.getAttribute(attributeKey);
          if (isItalic) {
            putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
          }
          else {
            putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
          }
        }
      }
      /*}
      else {
        putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
      }*/
      updateActions();
    }

    public void getProperties() {
      getActionProperties(this, fontItalicAction);
    }
    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }
    /**
     * set the value of this <code>AttributeComponent</code>
     *
     * @param a  the set of attributes possibly having an
     *          attribute this component can display
     *
     * @return true, if the set of attributes had a matching attribute,
     *            false if not
     */
    public boolean setValue(AttributeSet a) {
      boolean success = false;
      boolean isItalic = StyleConstants.isItalic(a);
      if(a.isDefined(CSS.Attribute.FONT_STYLE)) {
        Object value = a.getAttribute(CSS.Attribute.FONT_STYLE);
        if (value.toString().equalsIgnoreCase(StyleConstants.Italic.toString())) {
          isItalic = true;
        }
      }
      //System.out.println("ItalicAction setValue isItalic=" + isItalic);
      //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
      //hd.listAttributes(a, 6);
      //if(a.isDefined(attributeKey)) {
        //Object value = a.getAttribute(attributeKey);
        if(isItalic) {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
        }
        else {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
        }
        success = true;
      //}
      //else {
      //  putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      //}
      return success;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      //System.out.println("ToggleAction getValue getValue(FrmMain.ACTION_SELECTED_KEY)=" + getValue(FrmMain.ACTION_SELECTED_KEY));
      SimpleAttributeSet set = new SimpleAttributeSet();
      //if(unselectedValue != null) {
      if (getValue(FrmMain.ACTION_SELECTED_KEY).toString().equals(
          FrmMain.ACTION_SELECTED)) {
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_STYLE,
                                          Util.CSS_ATTRIBUTE_NORMAL.toString());
      }
      else {
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_STYLE,
                                          StyleConstants.Italic.toString());
      }
      /*}
             else {
        Util.styleSheet().addCSSAttribute(set,
            (CSS.Attribute) getAttributeKey(), selectedValue.toString());
             }*/
      return set;
    }
    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }

  }

  public class BoldAction extends StyledEditorKit.BoldAction implements SHTMLAction, AttributeComponent {
    public BoldAction() {
      //Action act = new StyledEditorKit.BoldAction();
      super();
      putValue(Action.NAME, fontBoldAction);
      putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      getActionProperties(this, fontBoldAction);
    }
    /**
     * do the format change for the associated attribute
     *
     * <p>This reverses the current setting for the associated attribute</p>
     *
     * @param  e  the ActionEvent describing the cause for this action
     */
    public void actionPerformed(ActionEvent e) {
      //System.out.println("ToggleAction getValue=" + getValue() + "selectedValue=" + selectedValue);
      //editor.applyAttributes(getValue(), (unselectedValue == null));
      super.actionPerformed(e);
      //if(unselectedValue != null) {
      if(editor != null) {
        SHTMLDocument doc = (SHTMLDocument) editor.getDocument();
        if (doc != null) {
          AttributeSet a = doc.getCharacterElement(editor.getSelectionStart()).
              getAttributes();
          boolean isBold = StyleConstants.isBold(a);
          //if(a.isDefined(attributeKey)) {
          //Object value = a.getAttribute(attributeKey);
          if (isBold) {
            putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
          }
          else {
            putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
          }
        }
      }
      /*}
      else {
        putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
      }*/
      updateActions();
    }

    public void getProperties() {
      getActionProperties(this, fontItalicAction);
    }
    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }
    /**
     * set the value of this <code>AttributeComponent</code>
     *
     * @param a  the set of attributes possibly having an
     *          attribute this component can display
     *
     * @return true, if the set of attributes had a matching attribute,
     *            false if not
     */
    public boolean setValue(AttributeSet a) {
      boolean success = false;
      boolean isBold = StyleConstants.isBold(a);
      if(a.isDefined(CSS.Attribute.FONT_WEIGHT)) {
        Object value = a.getAttribute(CSS.Attribute.FONT_WEIGHT);
        if (value.toString().equalsIgnoreCase(StyleConstants.Bold.toString())) {
          isBold = true;
        }
      }
      //System.out.println("ItalicAction setValue isItalic=" + isItalic);
      //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
      //hd.listAttributes(a, 6);
      //if(a.isDefined(attributeKey)) {
        //Object value = a.getAttribute(attributeKey);
        if(isBold) {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
        }
        else {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
        }
        success = true;
      //}
      //else {
      //  putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      //}
      return success;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      //System.out.println("ToggleAction getValue getValue(FrmMain.ACTION_SELECTED_KEY)=" + getValue(FrmMain.ACTION_SELECTED_KEY));
      SimpleAttributeSet set = new SimpleAttributeSet();
      //if(unselectedValue != null) {
      if (getValue(FrmMain.ACTION_SELECTED_KEY).toString().equals(
          FrmMain.ACTION_SELECTED)) {
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_WEIGHT,
                                          Util.CSS_ATTRIBUTE_NORMAL.toString());
      }
      else {
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_WEIGHT,
                                          StyleConstants.Bold.toString());
      }
      /*}
             else {
        Util.styleSheet().addCSSAttribute(set,
            (CSS.Attribute) getAttributeKey(), selectedValue.toString());
             }*/
      return set;
    }
    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }

  }

  public class UnderlineAction extends StyledEditorKit.UnderlineAction implements SHTMLAction, AttributeComponent {
    public UnderlineAction() {
      //Action act = new StyledEditorKit.BoldAction();
      super();
      putValue(Action.NAME, fontUnderlineAction);
      putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      getActionProperties(this, fontUnderlineAction);
    }
    /**
     * do the format change for the associated attribute
     *
     * <p>This reverses the current setting for the associated attribute</p>
     *
     * @param  e  the ActionEvent describing the cause for this action
     */
    public void actionPerformed(ActionEvent e) {
      //System.out.println("ToggleAction getValue=" + getValue() + "selectedValue=" + selectedValue);
      //editor.applyAttributes(getValue(), (unselectedValue == null));
      super.actionPerformed(e);
      //if(unselectedValue != null) {
      if(editor != null) {
        SHTMLDocument doc = (SHTMLDocument) editor.getDocument();
        if (doc != null) {
          AttributeSet a = doc.getCharacterElement(editor.getSelectionStart()).
              getAttributes();
          boolean isUnderlined = StyleConstants.isUnderline(a);
          //if(a.isDefined(attributeKey)) {
          //Object value = a.getAttribute(attributeKey);
          if (isUnderlined) {
            putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
          }
          else {
            putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
          }
        }
      }
      /*}
      else {
        putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
      }*/
      updateActions();
    }

    public void getProperties() {
      getActionProperties(this, fontUnderlineAction);
    }
    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }
    /**
     * set the value of this <code>AttributeComponent</code>
     *
     * @param a  the set of attributes possibly having an
     *          attribute this component can display
     *
     * @return true, if the set of attributes had a matching attribute,
     *            false if not
     */
    public boolean setValue(AttributeSet a) {
      boolean success = false;
      boolean isUnderlined = StyleConstants.isUnderline(a);
      if(a.isDefined(CSS.Attribute.TEXT_DECORATION)) {
        Object value = a.getAttribute(CSS.Attribute.TEXT_DECORATION);
        if (value.toString().equalsIgnoreCase(Util.CSS_ATTRIBUTE_UNDERLINE /*StyleConstants.Underline.toString()*/)) {
          isUnderlined = true;
        }
      }
      //System.out.println("ItalicAction setValue isItalic=" + isItalic);
      //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
      //hd.listAttributes(a, 6);
      //if(a.isDefined(attributeKey)) {
        //Object value = a.getAttribute(attributeKey);
        if(isUnderlined) {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
        }
        else {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
        }
        success = true;
      //}
      //else {
      //  putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      //}
      return success;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      //System.out.println("ToggleAction getValue getValue(FrmMain.ACTION_SELECTED_KEY)=" + getValue(FrmMain.ACTION_SELECTED_KEY));
      SimpleAttributeSet set = new SimpleAttributeSet();
      //if(unselectedValue != null) {
      if (getValue(FrmMain.ACTION_SELECTED_KEY).toString().equals(FrmMain.ACTION_SELECTED)) {
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.TEXT_DECORATION, Util.CSS_ATTRIBUTE_UNDERLINE);
      }
      else {
        Util.styleSheet().addCSSAttribute(set, CSS.Attribute.TEXT_DECORATION, Util.CSS_ATTRIBUTE_NONE);
      }
      /*}
             else {
        Util.styleSheet().addCSSAttribute(set,
            (CSS.Attribute) getAttributeKey(), selectedValue.toString());
             }*/
      return set;
    }
    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }

  }
  /**
   * action to toggle an attribute
   */
  public class ToggleAction extends AbstractAction implements SHTMLAction,
        AttributeComponent
  {
    /** the attribute this action represents values for */
    private Object attributeKey;

    /** the value for the attribute being selected */
    private Object selectedValue = null;

    /** the value for the attribute not being selected */
    private Object unselectedValue = null;

    /**
     * construct a ToggleFontAction
     *
     * @param name  the name and command for this action
     * @param key the attribute this action represents values for
     * @param sVal the value for the attribute being selected
     * @param uVal the value for the attribute not being selected
     */
    public ToggleAction(String name, Object key, Object sVal, Object uVal)
    {
      super(name);
      putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      attributeKey = key;
      selectedValue = sVal;
      unselectedValue = uVal;
      getProperties();
    }

    /**
     * construct a ToggleFontAction
     *
     * @param name  the name and command for this action
     * @param key the attribute this action represents values for
     * @param sVal the value for the attribute being selected
     */
    public ToggleAction(String name, Object key, Object sVal)
    {
      this(name, key, sVal, null);
      //System.out.println("ToggleAction constructor sVal=" + sVal);
    }

    /**
     * do the format change for the associated attribute
     *
     * <p>This reverses the current setting for the associated attribute</p>
     *
     * @param  e  the ActionEvent describing the cause for this action
     */
    public void actionPerformed(ActionEvent e) {
      //System.out.println("ToggleAction getValue=" + getValue() + "selectedValue=" + selectedValue);
      editor.applyAttributes(getValue(), (unselectedValue == null));
      if(unselectedValue != null) {
        if(getValue(FrmMain.ACTION_SELECTED_KEY).toString().equals(FrmMain.ACTION_UNSELECTED))
        {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
        }
        else {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
        }
      }
      else {
        putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
      }
      updateActions();
    }

    /**
     * get the attribute this action represents values for
     *
     * @return the attribute this action represents values for
     */
    public Object getAttributeKey() {
      return attributeKey;
    }

    /**
     * set the value of this <code>AttributeComponent</code>
     *
     * @param a  the set of attributes possibly having an
     *          attribute this component can display
     *
     * @return true, if the set of attributes had a matching attribute,
     *            false if not
     */
    public boolean setValue(AttributeSet a) {
      boolean success = false;
      if(a.isDefined(attributeKey)) {
        Object value = a.getAttribute(attributeKey);
        if(value.toString().equalsIgnoreCase(selectedValue.toString())) {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_SELECTED);
        }
        else {
          putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
        }
        success = true;
      }
      else {
        putValue(FrmMain.ACTION_SELECTED_KEY, FrmMain.ACTION_UNSELECTED);
      }
      return success;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      //System.out.println("ToggleAction getValue getValue(FrmMain.ACTION_SELECTED_KEY)=" + getValue(FrmMain.ACTION_SELECTED_KEY));
      SimpleAttributeSet set = new SimpleAttributeSet();
      if(unselectedValue != null) {
        if(getValue(FrmMain.ACTION_SELECTED_KEY).toString().equals(
            FrmMain.ACTION_SELECTED))
        {
          if(unselectedValue != null) {
            Util.styleSheet().addCSSAttribute(set,
                (CSS.Attribute) getAttributeKey(), unselectedValue.toString());
          }
        }
        else {
          Util.styleSheet().addCSSAttribute(set,
              (CSS.Attribute) getAttributeKey(), selectedValue.toString());
        }
      }
      else {
        Util.styleSheet().addCSSAttribute(set,
            (CSS.Attribute) getAttributeKey(), selectedValue.toString());
      }
      return set;
    }

    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }

    /** update the action's state */
    public void update() {
      if(jtpDocs.getTabCount() > 0) {
        this.setEnabled(true);
      }
      else {
        this.setEnabled(false);
      }
    }

    /** get image, etc. from resource */
    public void getProperties() {
      getActionProperties(this, (String) getValue(Action.NAME));
    }
  }

  /**
   * Get all attributes that can be found in the element tree
   * starting at the highest parent down to the character element
   * at the current position in the document. Combine element
   * attributes with attributes from the style sheet.
   *
   * @param editor  the editor pane to combine attributes from
   *
   * @return the resulting set of combined attributes
   */
  public AttributeSet getMaxAttributes(SHTMLEditorPane editor,
                                       String elemName)
  {
    Element e = doc.getCharacterElement(editor.getSelectionStart());
    if(elemName != null && elemName.length() > 0) {
      e = Util.findElementUp(elemName, e);
    }
    StyleSheet s = doc.getStyleSheet();//((SHTMLEditorKit) editor.getEditorKit()).getStyleSheet();
    return getMaxAttributes(e, s);
  }

  public static AttributeSet getMaxAttributes(Element e, StyleSheet s) {
    SimpleAttributeSet a = new SimpleAttributeSet();
    Element cElem = e;
    AttributeSet attrs;
    Vector elements = new Vector();
    Object classAttr;
    String styleName;
    String elemName;
    while(e != null) {
      elements.insertElementAt(e, 0);
      e = e.getParentElement();
    }
    for(int i = 0; i < elements.size(); i++) {
      e = (Element) elements.elementAt(i);
      classAttr = e.getAttributes().getAttribute(HTML.Attribute.CLASS);
      elemName = e.getName();
      styleName = elemName;
      if(classAttr != null) {
        styleName = elemName + "." + classAttr.toString();
        a.addAttribute(HTML.Attribute.CLASS, classAttr);
      }
      //System.out.println("getMaxAttributes name=" + styleName);
      attrs = s.getStyle(styleName);
      if(attrs != null) {
        a.addAttributes(Util.resolveAttributes(attrs));
      }
      else {
        attrs = s.getStyle(elemName);
        if(attrs != null) {
          a.addAttributes(Util.resolveAttributes(attrs));
        }
      }
      a.addAttributes(Util.resolveAttributes(e.getAttributes()));
    }
    if(cElem != null) {
      //System.out.println("getMaxAttributes cElem.name=" + cElem.getName());
      a.addAttributes(cElem.getAttributes());
    }
    //System.out.println(" ");
    //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
    //hd.listAttributes(a, 4);
    return new AttributeMapper(a).getMappedAttributes(AttributeMapper.toJava);
  }

  /* ---------- font manipulation code end ------------------ */

}
