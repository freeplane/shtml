/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 * Copyright (C) 2006 Dimitri Polivaev
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.undo.CannotRedoException;

import com.lightdev.app.shtm.SHTMLEditorPane.PasteMode;
import com.lightdev.app.shtm.SHTMLPanelImpl.FontFamilyPicker;
import com.lightdev.app.shtm.SHTMLPanelImpl.FontSizePicker;
import com.sun.demo.ElementTreePanel;
import com.sun.demo.ExampleFileFilter;

import de.calcom.cclib.text.FindReplaceDialog;
import de.calcom.cclib.text.FindReplaceEvent;
import de.calcom.cclib.text.FindReplaceListener;

/** A class grouping actions. Most actions forward the operation to editor pane. */
class SHTMLEditorKitActions {
    /**
     * action to set the style
     */
    static class SetStyleAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;
        private boolean ignoreActions = false;

        public SetStyleAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.setStyleAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            if (!ignoreActions) {
                final StyleSelector styleSelector = (StyleSelector) ae.getSource();
                final AttributeSet attributeSet = styleSelector.getValue();
                if (attributeSet != null) {
                    panel.getSHTMLEditorPane().applyAttributes(attributeSet, true);
                }
                panel.updateActions();
            }
        }

        public void setIgnoreActions(final boolean ignore) {
            ignoreActions = ignore;
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * append a new table col
     */
    static class AppendTableColAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public AppendTableColAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.appendTableColAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().appendTableColumn();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * Applies a tag to the <i>paragraph element</i> surrounding the selection,
     * based on the paragraph tag previously stored in the tag selector; tag selector
     * is a combo box. If constructed when the tag name passed, it applies that tag.
     */
    static class SetTagAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;
        private boolean ignoreActions = false;
        private String tag = null;

        public SetTagAction(final SHTMLPanelImpl panel) {
        	this(panel, null);
        }

        public SetTagAction(final SHTMLPanelImpl panel, final String tag) {
            super();
            this.panel = panel;
            this.tag = tag;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.setTagAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            if (!ignoreActions) {
                if (tag != null) {
                    panel.getSHTMLEditorPane().applyParagraphTag(tag, null);
                    panel.updateActions();
                }
                else {
                    final String tagFromSelector = panel.getTagSelector().getSelectedTag();
                    panel.getSHTMLEditorPane().applyParagraphTag(tagFromSelector, panel.getTagSelector().getTags());
                    panel.updateActions();
                }
            }
        }

        public void setIgnoreActions(final boolean ignore) {
            ignoreActions = ignore;
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }

    }

    /**
     * append a new table row
     */
    static class AppendTableRowAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public AppendTableRowAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.appendTableRowAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().appendTableRow();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /*
     * Created on 20.08.2006
     * Copyright (C) 2006 Dimitri Polivaev
     */
    static class BoldAction extends StyledEditorKit.BoldAction implements SHTMLAction, AttributeComponent {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public BoldAction(final SHTMLPanelImpl panel) {
            //Action act = new StyledEditorKit.BoldAction();
            super();
            this.panel = panel;
            putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.fontBoldAction);
        }

        /**
         * do the format change for the associated attribute
         *
         * <p>This reverses the current setting for the associated attribute</p>
         *
         * @param  e  the ActionEvent describing the cause for this action
         */
        public void actionPerformed(final ActionEvent e) {
            super.actionPerformed(e);

            if (panel.getSHTMLEditorPane() != null) {
                final SHTMLDocument doc = panel.getSHTMLEditorPane().getDocument();
                if (doc != null) {
                    final AttributeSet a = doc.getCharacterElement(panel.getSHTMLEditorPane().getSelectionStart())
                        .getAttributes();
                    final boolean isBold = StyleConstants.isBold(a);
                    putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, isBold ? SHTMLPanelImpl.ACTION_SELECTED
                            : SHTMLPanelImpl.ACTION_UNSELECTED);
                }
            }

            panel.updateActions();
        }

        public void update() {
        	 this.setEnabled(panel.isWYSIWYGEditorActive());
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
        public boolean setValue(final AttributeSet a) {
            boolean isBold = StyleConstants.isBold(a);
            if (a.isDefined(CSS.Attribute.FONT_WEIGHT)) {
                final Object value = a.getAttribute(CSS.Attribute.FONT_WEIGHT);
                if (value.toString().equalsIgnoreCase(StyleConstants.Bold.toString())) {
                    isBold = true;
                }
            }
            if (isBold) {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_SELECTED);
            }
            else {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            }

            return true;
        }

        /**
         * get the value of this <code>AttributeComponent</code>
         *
         * @return the value selected from this component
         */
        public AttributeSet getValue() {
            final SimpleAttributeSet set = new SimpleAttributeSet();

            if (getValue(SHTMLPanelImpl.ACTION_SELECTED_KEY).toString().equals(SHTMLPanelImpl.ACTION_SELECTED)) {
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_WEIGHT, StyleConstants.Bold.toString());
            }
            else {
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_WEIGHT, Util.CSS_ATTRIBUTE_NORMAL);
            }
            return set;
        }

        public AttributeSet getValue(final boolean includeUnchanged) {
            return getValue();
        }
    }

    /**
     * Applies a text attribute. (Used to be ToggleAction.)
     */
    static class ApplyCSSAttributeAction extends AbstractAction implements SHTMLAction, AttributeComponent {
        /**
         *
         */
        private final SHTMLPanelImpl panel;
        /** the attribute this action represents values for */
        final Object attributeName;
        /** the value for the attribute being selected */
        final private Object attributeValue;
        private final boolean applyToParagraph;

        /**
         * Constructs a ToggleAttributeAction.
         * @param panel TODO
         * @param actionName  the name and command for this action
         * @param attributeName the name of the attribute to be modified
         * @param attributeValue the value the attribute should be set to
         * @param applyToParagraph TODO
         */
        public ApplyCSSAttributeAction(final SHTMLPanelImpl panel, final String actionName, final Object attributeName,
                                       final Object attributeValue, final boolean applyToParagraph) {
            super();
            this.panel = panel;
            putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            this.attributeName = attributeName;
            this.attributeValue = attributeValue;
            this.applyToParagraph = applyToParagraph;
            SHTMLPanelImpl.configureActionProperties(this, actionName);
        }

        /**
         * do the format change for the associated attribute
         *
         * <p>This reverses the current setting for the associated attribute</p>
         *
         * @param  ev  the ActionEvent describing the cause for this action
         */
        public void actionPerformed(final ActionEvent ev) {
            boolean performTheAction = false;
            if (ev.getSource() instanceof JToggleButton) {
                final JToggleButton button = (JToggleButton) ev.getSource();
                performTheAction = button.isSelected();
            }
            else {
                performTheAction = true;
            }
            if (performTheAction) {
                panel.getSHTMLEditorPane().applyAttributes(getValue(), applyToParagraph);
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_SELECTED);
                panel.updateActions();
            }
        }

        /**
         * get the attribute this action represents values for
         *
         * @return the attribute this action represents values for
         */
        public Object getAttributeName() {
            return attributeName;
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
        public boolean setValue(final AttributeSet a) {
            boolean success = false;
            if (a.isDefined(attributeName)) {
                final Object value = a.getAttribute(attributeName);
                if (value.toString().equalsIgnoreCase(attributeValue.toString())) {
                    putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_SELECTED);
                }
                else {
                    putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
                }
                success = true;
            }
            else {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            }
            return success;
        }

        /**
         * get the value of this <code>AttributeComponent</code>
         *
         * @return the value selected from this component
         */
        public AttributeSet getValue() {
            final SimpleAttributeSet attributeSet = new SimpleAttributeSet();
            Util.styleSheet().addCSSAttribute(attributeSet, (CSS.Attribute) getAttributeName(),
                attributeValue.toString());
            return attributeSet;
        }

        public AttributeSet getValue(final boolean includeUnchanged) {
            return getValue();
        }

        /** update the action's state */
        public void update() {
        	 this.setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * delete a table col
     */
    static class DeleteTableColAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public DeleteTableColAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.deleteTableColAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().deleteTableCol();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * Action that brings up a JFrame with a JTree showing the structure
     * of the document in the currently active DocumentPane.
     * will be hidden from menu if not in development mode (DEV_MODE = false)
     */
    static class ShowElementTreeAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;
        /** a frame for showing an element tree panel */
        private JFrame elementTreeFrame = null;

        public ShowElementTreeAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.elemTreeAction);
        }

        public void actionPerformed(final ActionEvent e) {
            if (elementTreeFrame == null) {
                final String title = Util.getResourceString("elementTreeTitle");
                elementTreeFrame = new JFrame(title);
                elementTreeFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(final WindowEvent we) {
                        elementTreeFrame.dispose();
                        elementTreeFrame = null;
                    }
                });
                final Container fContentPane = elementTreeFrame.getContentPane();
                fContentPane.setLayout(new BorderLayout());
                final ElementTreePanel elementTreePanel = new ElementTreePanel(panel.getSHTMLEditorPane());
                fContentPane.add(elementTreePanel);
                elementTreeFrame.pack();
            }
            elementTreeFrame.setVisible(true);
            panel.updateActions();
        }

        public void update() {
        }
    }

    /**
     * delete a table row
     */
    static class DeleteTableRowAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public DeleteTableRowAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.deleteTableRowAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().deleteTableRow();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * toggle list formatting for a given type of list on/off
     */
    static class ToggleListAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;
        private final HTML.Tag listTag;

        public ToggleListAction(final SHTMLPanelImpl panel, final String name, final HTML.Tag listTag) {
            super();
            this.panel = panel;
            this.listTag = listTag;
            SHTMLPanelImpl.configureActionProperties(this, name);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().toggleList(listTag.toString(), null,
            // What are the attributes good for? They break the appearance of nested numbered lists. --Dan
            //panel.getMaxAttributes(panel.getSHTMLEditorPane(), listTag.toString()),
                false);
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * set the title of the currently active document
     */
    static class DocumentTitleAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public DocumentTitleAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.documentTitleAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            String newTitle;
            final String currentTitle = panel.getSHTMLDocument().getDocumentTitle();
            if (currentTitle != null) {
                newTitle = currentTitle;
            }
            else {
                newTitle = "";
            }
            newTitle = Util.nameInput(JOptionPane.getFrameForComponent(panel), newTitle, ".*", "docTitleTitle",
                "docTitleQuery");
            if (newTitle != null && !newTitle.isEmpty()) {
                panel.getSHTMLDocument().setDocumentTitle(newTitle);
            }
        }

        public void update() {
        	 this.setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /*
     * Created on 20.08.2006
     * Copyright (C) 2006 Dimitri Polivaev
     */
    static class UnderlineAction extends StyledEditorKit.UnderlineAction implements SHTMLAction, AttributeComponent {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public UnderlineAction(final SHTMLPanelImpl panel) {
            //Action act = new StyledEditorKit.BoldAction();
            super();
            this.panel = panel;
            putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.fontUnderlineAction);
        }

        /**
         * do the format change for the associated attribute
         *
         * <p>This reverses the current setting for the associated attribute</p>
         *
         * @param  e  the ActionEvent describing the cause for this action
         */
        public void actionPerformed(final ActionEvent e) {
            super.actionPerformed(e);

            if (panel.getSHTMLEditorPane() != null) {
                final SHTMLDocument doc = panel.getSHTMLEditorPane().getDocument();
                if (doc != null) {
                    final AttributeSet a = doc.getCharacterElement(panel.getSHTMLEditorPane().getSelectionStart())
                        .getAttributes();
                    final boolean isUnderlined = StyleConstants.isUnderline(a);
                    if (isUnderlined) {
                        putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_SELECTED);
                    }
                    else {
                        putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
                    }
                }
            }
            panel.updateActions();
        }
        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
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
        public boolean setValue(final AttributeSet a) {
            boolean isUnderlined = StyleConstants.isUnderline(a);
            if (a.isDefined(CSS.Attribute.TEXT_DECORATION)) {
                final Object value = a.getAttribute(CSS.Attribute.TEXT_DECORATION);
                if (value.toString()
                    .equalsIgnoreCase(Util.CSS_ATTRIBUTE_UNDERLINE /*StyleConstants.Underline.toString()*/)) {
                    isUnderlined = true;
                }
            }
            if (isUnderlined) {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_SELECTED);
            }
            else {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            }
            return true;
        }

        /**
         * get the value of this <code>AttributeComponent</code>
         *
         * @return the value selected from this component
         */
        public AttributeSet getValue() {
            final SimpleAttributeSet set = new SimpleAttributeSet();
            if (getValue(SHTMLPanelImpl.ACTION_SELECTED_KEY).toString().equals(SHTMLPanelImpl.ACTION_SELECTED)) {
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.TEXT_DECORATION, Util.CSS_ATTRIBUTE_UNDERLINE);
            }
            else {
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.TEXT_DECORATION, Util.CSS_ATTRIBUTE_NONE);
            }
            return set;
        }

        public AttributeSet getValue(final boolean includeUnchanged) {
            return getValue();
        }
    }

    static class StrikeThroughAction extends AbstractAction implements SHTMLAction, AttributeComponent {
        private final SHTMLPanelImpl panel;

        public StrikeThroughAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.fontStrikeThroughAction);
        }

        public void actionPerformed(final ActionEvent e) {
            final SHTMLEditorPane editor = panel.getSHTMLEditorPane();
            if (editor != null) {
                final StyledDocument doc = editor.getDocument();
                final int start = editor.getSelectionStart();
                final int end = editor.getSelectionEnd();
                if (start != end) {
                    final AttributeSet a = doc.getCharacterElement(start).getAttributes();
                    final boolean isStrikeThrough = StyleConstants.isStrikeThrough(a);
                    final SimpleAttributeSet attr = new SimpleAttributeSet();
                    StyleConstants.setStrikeThrough(attr, !isStrikeThrough);
                    doc.setCharacterAttributes(start, end - start, attr, false);
                }
            }
            panel.updateActions();
        }

        public void update() {
            setEnabled(panel.isWYSIWYGEditorActive());
        }

        public boolean setValue(final AttributeSet a) {
            boolean isStrikeThrough = StyleConstants.isStrikeThrough(a);
            if (a.isDefined(CSS.Attribute.TEXT_DECORATION)) {
                final Object value = a.getAttribute(CSS.Attribute.TEXT_DECORATION);
                if (value.toString().equalsIgnoreCase(Util.CSS_ATTRIBUTE_LINE_THROUGH)) {
                    isStrikeThrough = true;
                }
            }
            if (isStrikeThrough) {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_SELECTED);
            }
            else {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            }
            return true;
        }

        public AttributeSet getValue() {
            final SimpleAttributeSet set = new SimpleAttributeSet();
            if (getValue(SHTMLPanelImpl.ACTION_SELECTED_KEY).toString().equals(SHTMLPanelImpl.ACTION_SELECTED)) {
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.TEXT_DECORATION, Util.CSS_ATTRIBUTE_LINE_THROUGH);
            }
            else {
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.TEXT_DECORATION, Util.CSS_ATTRIBUTE_NONE);
            }
            return set;
        }

        public AttributeSet getValue(final boolean includeUnchanged) {
            return getValue();
        }
    }

    abstract static class FontColorAction extends AbstractAction implements SHTMLAction {
    	protected static final ColorPanel hiddenColorPanel = new ColorPanel("Select Color");
    	protected final SHTMLPanelImpl panel;

        public FontColorAction(SHTMLPanelImpl panel) {
			super();
			this.panel = panel;
		}

		public void actionPerformed(final ActionEvent e) {
            final SHTMLEditorPane editorPane = panel.getSHTMLEditorPane();
            if (panel.isWYSIWYGEditorActive()) {
                final AttributeSet color = getColor();
				editorPane.applyAttributes(color, false); // apply the color setting to the editor
				panel.updateActions();
            }
        }

		abstract protected AttributeSet getColor();

    }

    static class FontColorByDialogAction extends FontColorAction implements SHTMLAction {
        private final CSS.Attribute cssAttributeName;

        public FontColorByDialogAction(String actionName, final SHTMLPanelImpl panel, CSS.Attribute cssAttributeName) {
            super(panel);
            this.cssAttributeName = cssAttributeName;
            SHTMLPanelImpl.configureActionProperties(this, actionName);
        }

		protected AttributeSet getColor() {
			final SHTMLEditorPane editorPane = panel.getSHTMLEditorPane();
			hiddenColorPanel.setValue(panel.getMaxAttributes(editorPane), cssAttributeName);
			hiddenColorPanel.actionPerformed(null); // show the color chooser
            return hiddenColorPanel.getValue(cssAttributeName);
		}

        public void update() {
        }
    }

    static class SelectedFontColorAction extends FontColorAction implements SHTMLAction {
        private final CSS.Attribute cssAttributeName;

        public SelectedFontColorAction(String actionName, final SHTMLPanelImpl panel, CSS.Attribute cssAttributeNam) {
            super(panel);
            cssAttributeName = cssAttributeNam;
            SHTMLPanelImpl.configureActionProperties(this, actionName);
        }

		protected AttributeSet getColor() {
			final Color color = hiddenColorPanel.getColor();
			final SimpleAttributeSet set = new SimpleAttributeSet();
			final String colorRGB = "#" + Integer.toHexString(color.getRGB()).substring(2);
			Util.styleSheet().addCSSAttribute(set, cssAttributeName, colorRGB);
			set.addAttribute(HTML.Attribute.COLOR, colorRGB);
			return set;
		}

        public void update() {
        }
    }

    static class FixedFontColorAction extends FontColorAction implements SHTMLAction {

        private final CSS.Attribute cssAttributeName;
        private final Color darkColor;
		private final Color lightColor;

		public FixedFontColorAction(final SHTMLPanelImpl panel, String name, CSS.Attribute cssAttributeName, Color darkColor, Color lightColor) {
            super(panel);
            this.cssAttributeName = cssAttributeName;
			this.lightColor = lightColor;
            SHTMLPanelImpl.configureActionProperties(this, name);
			this.darkColor = darkColor;
        }

		protected AttributeSet getColor() {
			Color color = getColorCloserToCaretColor();
			final SimpleAttributeSet set = new SimpleAttributeSet();
			final String colorRGB = "#" + Integer.toHexString(color.getRGB()).substring(2);
			Util.styleSheet().addCSSAttribute(set,  cssAttributeName, colorRGB);
			set.addAttribute(HTML.Attribute.COLOR, colorRGB);
			return set;
		}

		private Color getColorCloserToCaretColor() {
			Color color = panel.getEditorPane().getCaretColor();
			if(isDark(color))
				return darkColor;
			else
				return lightColor;
		}

		private boolean isDark(Color color) {
			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();

			double brightness = Math.sqrt(
			    0.299 * (r * r) +
			    0.587 * (g * g) +
			    0.114 * (b * b)
			    );

            return brightness <= 127.5;
		}

		public void update() {
        }
    }
    /**
       * action to edit anchors inside a document
       */
    static class EditAnchorsAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public EditAnchorsAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.editAnchorsAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            final AnchorDialog dlg = new AnchorDialog(parent, Util.getResourceString("anchorDialogTitle"),
                panel.getSHTMLDocument());
            Util.center(parent, dlg);
            dlg.setModal(true);
            dlg.setVisible(true);
            panel.updateActions();
        }

        public void update() {
        	 this.setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * action to edit a link
     */
    static class EditLinkAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public EditLinkAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.editLinkAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            final LinkDialog dialog = new LinkDialog(parent, Util.getResourceString("linkDialogTitle"),
                panel.getSHTMLEditorPane());
            if (parent != null) {
                Util.center(parent, dialog);
            }
            dialog.setModal(true);
            dialog.setVisible(true);
            if (dialog.getResult() == DialogShell.RESULT_OK) {
                // apply link here
                panel.getSHTMLEditorPane().setLink(dialog.getLinkText(), dialog.getHref(), dialog.getStyleName(),
                    dialog.getLinkImage(), dialog.getLinkImageSize());
            }
            panel.updateActions();
        }

        public void update() {
            if (panel.isHtmlEditorActive()) {
                this.setEnabled(false);
                return;
            }
            if (panel.getSHTMLEditorPane() != null) {
                if ((panel.getSHTMLEditorPane().getSelectionEnd() > panel.getSHTMLEditorPane().getSelectionStart())
                        || (panel.getSHTMLEditorPane().getCurrentLinkElement() != null)) {
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
    }

    /**
     * Is an action to open a hyperlink.
     */
    static class OpenLinkAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public OpenLinkAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.openLinkAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final String linkURL = panel.getSHTMLEditorPane().getURLOfExistingLink();
            if (linkURL != null) {
                panel.openHyperlink(linkURL);
            }
            panel.updateActions();
        }

        public void update() {
            if (panel.isHtmlEditorActive()) {
                setEnabled(false);
                return;
            }
            if (panel.getSHTMLEditorPane() != null) {
                if ((panel.getSHTMLEditorPane().getSelectionEnd() > panel.getSHTMLEditorPane().getSelectionStart())
                        || (panel.getSHTMLEditorPane().getCurrentLinkElement() != null)) {
                    setEnabled(true);
                }
                else {
                    setEnabled(false);
                }
            }
            else {
                this.setEnabled(false);
            }
        }
    }

    /**
     * UndoAction for the edit menu
     */
    static class UndoAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public UndoAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            setEnabled(false);
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.undoAction);
        }

        public void actionPerformed(final ActionEvent e) {
            if (panel.getCurrentDocumentPane().getSelectedTab() != DocumentPane.VIEW_TAB_LAYOUT) {
                return;
            }
            try {
                panel.getUndo().undo();
            }
            catch (final Exception ex) {
                Util.errMsg((Component) e.getSource(), Util.getResourceString("unableToUndoError") + ex, ex);
            }
            panel.updateActions();
        }

        public void update() {
            setEnabled(panel.isWYSIWYGEditorActive() && panel.getUndo().canUndo());
        }
    }

    /**
     * Change list formatting
     */
    static class FormatListAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public FormatListAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.formatListAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            panel.getSHTMLEditorPane().requestFocus();
            panel.getSHTMLEditorPane().getSelectionStart();
            final ListDialog dlg = new ListDialog(parent, Util.getResourceString("listDialogTitle"));
            final SimpleAttributeSet set = new SimpleAttributeSet(panel.getMaxAttributes(panel.getSHTMLEditorPane(),
                HTML.Tag.UL.toString()));
            set.addAttributes(panel.getMaxAttributes(panel.getSHTMLEditorPane(), HTML.Tag.OL.toString()));
            dlg.setListAttributes(set);
            final String currentTag = dlg.getListTag();
            Util.center(parent, dlg);
            dlg.setModal(true);
            dlg.setVisible(true);
            /* if the user made a selection, apply it to the document */
            if (dlg.getResult() == DialogShell.RESULT_OK) {
                final AttributeSet a = dlg.getListAttributes();
                final String newTag = dlg.getListTag();
                if (newTag == null) {
                    panel.getSHTMLEditorPane().toggleList(newTag, a, true);
                }
                else if (newTag.equalsIgnoreCase(currentTag)) {
                    if (a.getAttributeCount() > 0) {
                        panel.getSHTMLEditorPane().applyListAttributes(a);
                    }
                }
                else {
                    panel.getSHTMLEditorPane().toggleList(newTag, a, false);
                }
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * action to change the paragraph style
     */
    static class FormatParaAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public FormatParaAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.formatParaAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            final ParaStyleDialog dlg = new ParaStyleDialog(parent, Util.getResourceString("paraStyleDialogTitle"));
            Util.center(parent, dlg);
            dlg.setModal(true);
            //SHTMLDocument doc = (SHTMLDocument) dp.getDocument();
            final int caretPosition = panel.getSHTMLEditorPane().getCaretPosition();
            dlg.setValue(panel.getMaxParagraphAttributes(caretPosition));
            dlg.setVisible(true);
            /* if the user made a selection, apply it to the document */
            if (dlg.getResult() == DialogShell.RESULT_OK) {
                panel.getSHTMLEditorPane().applyAttributes(dlg.getValue(), true);
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * format table attributes
     */
    static class FormatTableAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public FormatTableAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.formatTableAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            final SHTMLEditorPane editor = panel.getSHTMLEditorPane();
            editor.requestFocus();
            editor.getSelectionStart();
            final TableDialog td = new TableDialog(parent, Util.getResourceString("tableDialogTitle"));
            td.setTableAttributes(panel.getMaxAttributes(editor, HTML.Tag.TABLE.toString()));
            td.setCellAttributes(panel.getMaxAttributes(editor, HTML.Tag.TD.toString()));
            Util.center(parent, td);
            td.setModal(true);
            td.setVisible(true);
            /* if the user made a selection, apply it to the document */
            if (td.getResult() == DialogShell.RESULT_OK) {
                final SHTMLDocument doc = editor.getDocument();
                doc.startCompoundEdit();
                AttributeSet a = td.getTableAttributes();
                if (a.getAttributeCount() > 0) {
                    editor.applyTableAttributes(a);
                }
                a = td.getCellAttributes();
                if (a.getAttributeCount() > 0) {
                    editor.applyCellAttributes(a, td.getCellRange());
                }
                doc.endCompoundEdit();
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * force a garbage collection. This can be helpful to find out
     * whether or not objects are properly disposed.
     * Without forcing a garbage collection, this would happen
     * at random intervals so although an object might be properly
     * disposed, it might still be around until the next GC.
     * will be hidden from menu if not in development mode (DEV_MODE = false)
     */
    static class GarbageCollectionAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public GarbageCollectionAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.gcAction);
        }

        public void actionPerformed(final ActionEvent e) {
            System.gc();
            panel.updateActions();
        }

        public void update() {
        }
    }

    static class InsertImageAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public InsertImageAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.insertImageAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            SHTMLDocument document = panel.getSHTMLDocument();
            final ImageDialog dlg = new ImageDialog(parent, Util.getResourceString("imageDialogTitle"), document);
            Util.center(parent, dlg);
            dlg.setModal(true);
            dlg.setVisible(true);
            /* if the user made a selection, apply it to the document */
            if (dlg.getResult() == DialogShell.RESULT_OK) {
                try {
                    document.insertBeforeStart(
                        document.getCharacterElement(panel.getSHTMLEditorPane().getSelectionEnd()),
                        dlg.getImageHTML());
                }
                catch (final Exception e) {
                    Util.errMsg(null, e.getMessage(), e);
                }
            }
            panel.updateActions();
        }

        public void update() {
            if (panel.isHtmlEditorActive()) {
                this.setEnabled(false);
                return;
            }
            this.setEnabled(panel.getSHTMLEditorPane() != null);
        }
    }

    /**
     * insert a new table
     */
    static class InsertTableAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public InsertTableAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.insertTableAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            Object input = null;
            final boolean showPopup = Util.preferenceIsTrue("table.popupBeforeInserting", "true");
            if (showPopup) {
                input = Util.nameInput(parent, "3", "\\d+", "insertTableTitle", "insertTableMsg");
            }
            if (input != null || !showPopup) {
                final int choice = input != null ? Integer.parseInt(input.toString()) : 3;
                if (choice > 0) {
                    panel.getSHTMLEditorPane().insertNewTable(choice);
                }
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * insert a new table column
     */
    static class InsertTableColAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public InsertTableColAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.insertTableColAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().insertTableColumn();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * insert a new table row
     */
    static class InsertTableRowAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;
        private final String forcedCellName;

        public InsertTableRowAction(final SHTMLPanelImpl panel, final String forcedCellName, final String titleID) {
            super();
            this.panel = panel;
            this.forcedCellName = forcedCellName;
            SHTMLPanelImpl.configureActionProperties(this, titleID);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().insertTableRow(forcedCellName);
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * Move theinsert a new table row
     */
    static class MoveTableRowUpAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public MoveTableRowUpAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.moveTableRowUpAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().moveTableRowUp();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * Moves the the table row up.
     */
    static class MoveTableRowDownAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public MoveTableRowDownAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.moveTableRowDownAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().moveTableRowDown();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * Moves the the table column left.
     */
    static class MoveTableColumnLeftAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public MoveTableColumnLeftAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.moveTableColumnLeftAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().moveTableColumnLeft();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * Moves the the table column right.
     */
    static class MoveTableColumnRightAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public MoveTableColumnRightAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.moveTableColumnRightAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().moveTableColumnRight();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * Turns a table data cell into a table header cell or vice versa.
     */
    static class ToggleTableHeaderCellAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public ToggleTableHeaderCellAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, null);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().toggleTableHeaderCell();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive()  && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    static class ItalicAction extends StyledEditorKit.ItalicAction implements SHTMLAction, AttributeComponent {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public ItalicAction(final SHTMLPanelImpl panel) {
            //Action act = new StyledEditorKit.BoldAction();
            super();
            this.panel = panel;
            putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.fontItalicAction);
        }

        /**
         * do the format change for the associated attribute
         *
         * <p>This reverses the current setting for the associated attribute</p>
         *
         * @param  e  the ActionEvent describing the cause for this action
         */
        public void actionPerformed(final ActionEvent e) {
            super.actionPerformed(e);
            if (panel.getSHTMLEditorPane() != null) {
                final SHTMLDocument doc = panel.getSHTMLEditorPane().getDocument();
                if (doc != null) {
                    final AttributeSet a = doc.getCharacterElement(panel.getSHTMLEditorPane().getSelectionStart())
                        .getAttributes();
                    final boolean isItalic = StyleConstants.isItalic(a);
                    if (isItalic) {
                        putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_SELECTED);
                    }
                    else {
                        putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
                    }
                }
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
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
        public boolean setValue(final AttributeSet a) {
            boolean isItalic = StyleConstants.isItalic(a);
            if (a.isDefined(CSS.Attribute.FONT_STYLE)) {
                final Object value = a.getAttribute(CSS.Attribute.FONT_STYLE);
                if (value.toString().equalsIgnoreCase(StyleConstants.Italic.toString())) {
                    isItalic = true;
                }
            }
            if (isItalic) {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_SELECTED);
            }
            else {
                putValue(SHTMLPanelImpl.ACTION_SELECTED_KEY, SHTMLPanelImpl.ACTION_UNSELECTED);
            }

            return true;
        }

        /**
         * get the value of this <code>AttributeComponent</code>
         *
         * @return the value selected from this component
         */
        public AttributeSet getValue() {
            final SimpleAttributeSet set = new SimpleAttributeSet();
            if (getValue(SHTMLPanelImpl.ACTION_SELECTED_KEY).toString().equals(SHTMLPanelImpl.ACTION_SELECTED)) {
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_STYLE, Util.CSS_ATTRIBUTE_NORMAL);
            }
            else {
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_STYLE, StyleConstants.Italic.toString());
            }
            return set;
        }

        public AttributeSet getValue(final boolean includeUnchanged) {
            return getValue();
        }
    }

    /**
     * action to move to the next cell in a table
     */
    static class NextTableCellAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public NextTableCellAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.nextTableCellAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Element cell = panel.getSHTMLEditorPane().getCurrentTableCell();
            if (cell != null) {
                panel.getSHTMLEditorPane().goNextCell(cell);
                panel.updateActions();
            }
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * action to move to the previous cell in a table
     */
    static class PrevTableCellAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public PrevTableCellAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.prevTableCellAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Element cell = panel.getSHTMLEditorPane().getCurrentTableCell();
            if (cell != null) {
                panel.getSHTMLEditorPane().goPrevCell(cell);
                panel.updateActions();
            }
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getSHTMLEditorPane().getCurrentTableCell() != null);
        }
    }

    /**
     * action to move to the previous cell in a table
     */
    static class PrintAction extends AbstractAction implements SHTMLAction {
        static private Method printMethod;
        static {
            Method printMethod = null;
            try {
                printMethod = JTextComponent.class.getMethod("print", new Class[] {});
            }
            catch (final Exception e) {
            }
            PrintAction.printMethod = printMethod;
        }

        static boolean canPrint() {
            return printMethod != null;
        }

        private final SHTMLPanelImpl panel;

        public PrintAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.printAction);
        }

        public void actionPerformed(final ActionEvent e) {
            if (PrintAction.canPrint()) {
                try {
                    printMethod.invoke(panel.getEditorPane(), new Object[] {});
                }
                catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
            else {
                JOptionPane.showMessageDialog(panel, Util.getResourceString("printing_not_supported"));
                setEnabled(false);
            }
        }

        public void update() {
        }
    }

    /**
     * RedoAction for the edit menu
     */
    static class RedoAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public RedoAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            setEnabled(false);
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.redoAction);
        }

        public void actionPerformed(final ActionEvent e) {
            if (panel.getCurrentDocumentPane().getSelectedTab() != DocumentPane.VIEW_TAB_LAYOUT) {
                return;
            }
            try {
                panel.getUndo().redo();
                panel.getSHTMLEditorPane();
            }
            catch (final CannotRedoException ex) {
                Util.errMsg((Component) e.getSource(), Util.getResourceString("unableToRedoError") + ex, ex);
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && panel.getUndo().canRedo());
        }
    }

    /** just adds a normal name to the superclasse's action */
    static class SHTMLEditCopyAction extends DefaultEditorKit.CopyAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SHTMLEditCopyAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.copyAction);
        }

        public void actionPerformed(final ActionEvent e) {
            super.actionPerformed(e);
            panel.updateActions();
        }

        public void update() {
            setEnabled(panel.getSHTMLEditorPane() != null);
        }
    }

    /** just adds a normal name to the superclasse's action */
    static class SHTMLEditCutAction extends DefaultEditorKit.CutAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SHTMLEditCutAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.cutAction);
        }

        public void actionPerformed(final ActionEvent e) {
            super.actionPerformed(e);
            panel.updateActions();
        }

        public void update() {
            setEnabled(panel.getSHTMLEditorPane() != null);
        }
    }

    /** just adds a normal name to the superclasse's action */
    static class SHTMLEditPasteAction extends DefaultEditorKit.PasteAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SHTMLEditPasteAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.pasteAction);
        }

        public void actionPerformed(final ActionEvent e) {
        	super.actionPerformed(e);
            panel.updateActions();
        }

        public void update() {
            setEnabled(panel.getSHTMLEditorPane() != null);
        }
    }

    /**
     * This action does either "Paste as HTML" or "Paste as Text", depending on default_paste_mode!
     * @author Felix Natter
     *
     */
    static class SHTMLEditPasteOtherAction extends DefaultEditorKit.PasteAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SHTMLEditPasteOtherAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, "pasteOther");
            updateActionName(PasteMode.getPasteModeFromPrefs().invert());
        }

        public void updateActionName(final PasteMode pm)
        {
        	if (pm == PasteMode.PASTE_HTML)
        	{
        		putValue(Action.NAME, Util.getResourceString("pasteHTMLLabel"));
        	}
        	else if (pm == PasteMode.PASTE_PLAIN_TEXT)
        	{
        		putValue(Action.NAME, Util.getResourceString("pastePlainTextLabel"));
        	}
        	else
        	{
        		throw new RuntimeException("Unknown SHTMLEditorPane.PasteMode: " + pm.toString());
        	}
        	panel.updateActions();
        }

        public void actionPerformed(final ActionEvent e) {
        	PasteMode pm = panel.getSHTMLEditorPane().getPasteMode().invert();
        	panel.getSHTMLEditorPane().setPasteMode(pm);

            super.actionPerformed(e);
            panel.updateActions();

            panel.getSHTMLEditorPane().setPasteModeFromPrefs();
        }

        public void update() {
            setEnabled(panel.getSHTMLEditorPane() != null);
        }
    }

    static class SHTMLEditPrefsAction extends AbstractAction implements SHTMLAction {
        private final SHTMLPanelImpl panel;

        public SHTMLEditPrefsAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.editPrefsAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            final PrefsDialog dlg = new PrefsDialog(parent, Util.getResourceString("prefsDialogTitle"));

            dlg.addPrefChangeListener(panel);

            Util.center(parent, dlg);
            dlg.setModal(true);
            dlg.setVisible(true);

            panel.updateActions();

            dlg.removePrefChangeListener(panel);
        }

        public void update() {
        }
    }

    static class SHTMLEditSelectAllAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SHTMLEditSelectAllAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.selectAllAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            if (panel.isHtmlEditorActive()) {
                panel.getDocumentPane().getHtmlEditor().selectAll();
            }
            else {
                panel.getSHTMLEditorPane().selectAll();
                panel.updateActions();
            }
        }

        public void update() {
            this.setEnabled(panel.getSHTMLEditorPane() != null);
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
    static class SHTMLFileCloseAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelMultipleDocImpl panel;
        private boolean exitApp = false;

        /** constructor
         * @param panel TODO*/
        public SHTMLFileCloseAction(final SHTMLPanelMultipleDocImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelMultipleDocImpl.closeAction);
        }

        /** close the currently active document, if there is one */
        public void actionPerformed(final ActionEvent ae) {
            if (panel.getSHTMLEditorPane() != null) { // if documents are open
                closeDocument(panel.getActiveTabNo(), ae, false); // close the active one
            }
            panel.updateActions();
        }

        /**
         * close a document by its tab index.
         *
         * <p>The method takes care of saving the document if necessary prior
         * to closing.</p>
         *
         * @param index the tab index number of the document in the tabbed pane.
         */
        public void closeDocument(final int index, final ActionEvent ae, final boolean ignoreChanges) {
            exitApp = ae.getActionCommand().contains(SHTMLPanelImpl.exitAction);
            final DocumentPane dp = (DocumentPane) panel.getTabbedPaneForDocuments().getComponentAt(index);
            if (!dp.saveInProgress()) { // if no save is going on and..
                if (ignoreChanges) {
                    closeDoc(dp);
                }
                else {
                    if (dp.needsSaving()) { // ..the document needs to be saved
                        panel.selectTabbedPane(index);
                        final String docName = dp.getDocumentName();
                        final int choice = Util.msgChoice(JOptionPane.YES_NO_CANCEL_OPTION, "confirmClosing",
                            "saveChangesQuery", docName, "\r\n\r\n");
                        switch (choice) {
                            case JOptionPane.YES_OPTION: // if the user wanted to save
                                if (dp.isNewDoc()) { //if the document is new
                                    panel.dynRes.getAction(SHTMLPanelMultipleDocImpl.saveAsAction).actionPerformed(ae); // 'save as'
                                }
                                else { // else
                                    panel.dynRes.getAction(SHTMLPanelMultipleDocImpl.saveAction).actionPerformed(ae); // 'save'
                                }
                                scheduleClose(dp); //..and wait until it is finished, then close
                                break;
                            case JOptionPane.NO_OPTION: // if the user don't like to save
                                closeDoc(dp); // close the document without saving
                                break;
                            case JOptionPane.CANCEL_OPTION: // if the user cancelled
                                break; // do nothing
                        }
                    }
                    else { // if the document does not need to be saved
                        closeDoc(dp); // close the document
                    }
                }
            }
            else { // save was going on upon close request, so
                scheduleClose(dp); // wait for completion, then close
            }
        }

        /**
         * schedule closing of a document.
         *
         * <p>This creates a <code>Timer</code> thread for which a
         * <code>TimerTask</code> is scheduled to periodically check
         * whether or not the save process for respective document commenced
         * successfully.</p>
         *
         * <p>If yes, Timer and TimerTask are disposed and the document
         * is closed. If not, the document remains open.</p>
         *
         * @param dp  the document to close
         */
        private void scheduleClose(final DocumentPane dp) {
            final java.util.Timer timer = new java.util.Timer();
            final TimerTask task = new TimerTask() {
                public void run() {
                    if (!dp.saveInProgress()) { // if done with saving
                        if (dp.saveSuccessful) { // and all went fine
                            closeDoc(dp); // close the document
                            this.cancel(); // dispose the task
                            timer.cancel(); // dispose the timer
                        }
                    }
                }
            };
            timer.schedule(task, 0, 400); // try to close every 400 milliseconds
        }

        /**
         * convenience method for closing a document
         */
        private void closeDoc(final DocumentPane dp) {
            try {
                dp.deleteTempDir();
                panel.unregisterDocument();
                panel.getTabbedPaneForDocuments().remove(dp);
            }
            catch (final IndexOutOfBoundsException e) { // if the tabs have changed meanwhile
                catchCloseErr(dp);
            }
            if (exitApp) { // if the doc close was caused by a request to exit the app
                if (panel.getTabbedPaneForDocuments().getTabCount() == 0) { // ..and if there are no open docs
                    System.exit(0); // exit the application
                }
            }
        }

        private void catchCloseErr(DocumentPane dp) {
            try {
                int i = panel.getTabbedPaneForDocuments().indexOfComponent(dp); // get the current tab index
                if (i < 0 && panel.getSHTMLEditorPane() != null) {
                    panel.setActiveTabNo(panel.getTabbedPaneForDocuments().getSelectedIndex());
                    dp = (DocumentPane) panel.getTabbedPaneForDocuments().getComponentAt(panel.getActiveTabNo());
                    i = panel.getTabbedPaneForDocuments().indexOfComponent(dp); // get the current tab index again
                    panel.unregisterDocument();
                    panel.getTabbedPaneForDocuments().remove(i); //now remove it
                }
                else {
                    while (i > 0 && i > panel.getTabbedPaneForDocuments().getTabCount()) { // while its still wrong
                        i = panel.getTabbedPaneForDocuments().indexOfComponent(dp); // get the current tab index again
                    }
                    panel.unregisterDocument();
                    panel.getTabbedPaneForDocuments().remove(i); //now remove it
                }
            }
            catch (final IndexOutOfBoundsException e) {
                catchCloseErr(dp);
            }
        }

        /** update the state of this action */
        public void update() {
            this.setEnabled(panel.getSHTMLEditorPane() != null);
        }
    }

    /**
     * close all documents currently shown.
     *
     * <p>This action simply loops through all open documents and uses an instance
     * of SHTMLFileCloseAction to perform the actual closing on each of them.</p>
     */
    static class SHTMLFileCloseAllAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelMultipleDocImpl panel;

        /** constructor
         * @param panel TODO*/
        public SHTMLFileCloseAllAction(final SHTMLPanelMultipleDocImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelMultipleDocImpl.closeAllAction);
        }

        /** close all open documents */
        public void actionPerformed(final ActionEvent ae) {
            final SHTMLFileCloseAction a = (SHTMLFileCloseAction) panel.dynRes
                .getAction(SHTMLPanelMultipleDocImpl.closeAction);
            for (int i = panel.getTabbedPaneForDocuments().getTabCount(); i > 0; i--) {
                a.closeDocument(i - 1, ae, false);
            }
            panel.updateActions();
        }

        public void update() {
            this.setEnabled(panel.getSHTMLEditorPane() != null);
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
    static class SHTMLFileExitAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelMultipleDocImpl panel;

        public SHTMLFileExitAction(final SHTMLPanelMultipleDocImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.exitAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            saveRelevantPrefs();
            new SHTMLFileCloseAllAction(panel).actionPerformed(ae);
            if (panel.getTabbedPaneForDocuments().getTabCount() == 0) {
                System.exit(0);
            }
            panel.updateActions();
        }

        public void saveRelevantPrefs() {
            /* ---- save splitpane sizes start -------------- */
            panel.savePrefs();
            /* ---- save splitpane sizes end -------------- */
        }

        public void update() {
        }
    }

    /** create a new empty document and show it */
    static class SHTMLFileNewAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelMultipleDocImpl panel;

        public SHTMLFileNewAction(final SHTMLPanelMultipleDocImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelMultipleDocImpl.newAction);
        }

        /** create a new empty document and show it */
        public void actionPerformed(final ActionEvent ae) {
            panel.createNewDocumentPane(); // create a new empty document
            panel.getTabbedPaneForDocuments().setSelectedComponent( // add the document to the
                panel.getTabbedPaneForDocuments().add(panel.getDocumentPane().getDocumentName(),
                    panel.getDocumentPane())); // tabbed pane for display
            panel.registerDocument();
            panel.updateActions();
        }

        public void update() {
        }
    }

    /** open an existing document from file and show it */
    static class SHTMLFileOpenAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelMultipleDocImpl panel;

        public SHTMLFileOpenAction(final SHTMLPanelMultipleDocImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelMultipleDocImpl.openAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Preferences prefs = Preferences.userNodeForPackage(panel.getClass());
            final JFileChooser chooser = new JFileChooser(); // create a file chooser
            final ExampleFileFilter filter = new ExampleFileFilter(); // create a filter
            filter.addExtension("htm");
            filter.addExtension("html");
            filter.setDescription(Util.getResourceString("htmlFileDesc"));
            chooser.setFileFilter(filter); // apply the file filter
            final String lastFileName = prefs.get(SHTMLPanelImpl.FILE_LAST_OPEN, "");
            if (!lastFileName.isEmpty()) {
                chooser.setCurrentDirectory(new File(lastFileName).getParentFile());
            }
            final int returnVal = // ..and show the file chooser
            chooser.showOpenDialog((Component) ae.getSource());
            if (returnVal == JFileChooser.APPROVE_OPTION) { // if a file was selected
                final File file = chooser.getSelectedFile();
                prefs.put(SHTMLPanelImpl.FILE_LAST_OPEN, file.getAbsolutePath());
                openDocument(file);
            }
            panel.updateActions();
        }

        public void openDocument(final File file) {
            openDocument(file, null);
        }

        public void openDocument(final File file, final DocumentPane.DocumentPaneListener listener) {
            int openDocNo = -1;
            try {
                openDocNo = getOpenDocument(file.toURL().toString());
            }
            catch (final MalformedURLException mue) {
            }
            if (openDocNo > -1) {
                panel.getTabbedPaneForDocuments().setSelectedIndex(openDocNo);
            }
            else {
                final FileLoader loader = new FileLoader(file, null, listener);
                loader.start();
            }
        }

        public int getOpenDocument(final String url) {
            int tabNo = -1;
            final int openDocCount = panel.getTabbedPaneForDocuments().getTabCount();
            int i = 0;
            while (i < openDocCount && tabNo < 0) {
                final URL source = ((DocumentPane) panel.getTabbedPaneForDocuments().getComponentAt(i)).getSource();
                if (source != null) {
                    if (source.toString().equalsIgnoreCase(url)) {
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
        public FileLoader createFileLoader(final URL url) {
            return new FileLoader(new File(url.getFile()), null);
        }

        /**
         * Helper class for being able to load a document in a separate thread.
         * Using a separate thread will not cause the application to block during
         * a lengthy load operation
         */
        class FileLoader extends Thread {
            final File file;
            final Component owner;
            DocumentPane.DocumentPaneListener l;

            public FileLoader(final File file, final Component owner) {
                this.file = file;
                this.owner = owner;
            }

            public FileLoader(final File file, final Component owner, final DocumentPane.DocumentPaneListener listener) {
                this(file, owner);
                l = listener;
            }

            public void run() {
                try {
                    JOptionPane.getFrameForComponent(panel);
                    panel.setDocumentPane(new DocumentPane(file.toURL(), 0/*, renderMode*/));
                    if (l != null) {
                        panel.getDocumentPane().addDocumentPaneListener(l);
                    }
                    panel.getTabbedPaneForDocuments().setSelectedComponent(
                        panel.getTabbedPaneForDocuments().add(panel.getDocumentPane().getDocumentName(),
                            panel.getDocumentPane()));
                    panel.registerDocument();
                }
                catch (final Exception e) {
                    Util.errMsg(owner, Util.getResourceString("unableToOpenFileError"), e);
                }
            }
        }

        public void update() {
        }
    }

    /** save a document */
    static class SHTMLFileSaveAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SHTMLFileSaveAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelMultipleDocImpl.saveAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            if (!panel.getDocumentPane().isNewDoc()) {
                final FileSaver saver = new FileSaver(panel.getDocumentPane());
                saver.setName("FileSaver");
                saver.start();
            }
            else {
                panel.dynRes.getAction(SHTMLPanelMultipleDocImpl.saveAsAction).actionPerformed(ae);
            }
            panel.updateActions();
        }

        /**
         * Helper class for being able to save a document in a separate thread.
         * Using a separate thread will not cause the application to block during
         * a lengthy save operation
         */
        class FileSaver extends Thread {
            final DocumentPane dp;
            Component owner;

            FileSaver(final DocumentPane dp) {
                setPriority(Thread.MIN_PRIORITY);
                this.dp = dp;
            }

            public void run() {
                panel.doSave(dp, dp.getSource());
            }
        }

        public void update() {
            final boolean isEnabled = panel.getSHTMLEditorPane() != null;
            boolean saveInProgress = false;
            boolean needsSaving = false;
            if (isEnabled) {
                saveInProgress = panel.getDocumentPane().saveInProgress();
                needsSaving = panel.getDocumentPane().needsSaving();
            }
            this.setEnabled(isEnabled && needsSaving && !saveInProgress);
        }
    }

    static class SHTMLFileSaveAllAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelMultipleDocImpl panel;

        public SHTMLFileSaveAllAction(final SHTMLPanelMultipleDocImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelMultipleDocImpl.saveAllAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final int count = panel.getTabbedPaneForDocuments().getTabCount();
            for (int i = 0; i < count; i++) {
                panel.getTabbedPaneForDocuments().setSelectedIndex(i);
                panel.setDocumentPane((DocumentPane) panel.getTabbedPaneForDocuments().getSelectedComponent());
                if (panel.getDocumentPane().needsSaving()) {
                    panel.dynRes.getAction(SHTMLPanelMultipleDocImpl.saveAction).actionPerformed(ae);
                }
            }
            panel.updateActions();
        }

        public void update() {
            this.setEnabled(panel.getSHTMLEditorPane() != null);
        }
    }

    /**
     * save a document under a different name and/or location
     *
     * <p>If a file already exists at the chosen location / name, the method
     * will ask the user if the existing file shall be overwritten.
     */
    static class SHTMLFileSaveAsAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelMultipleDocImpl panel;

        public SHTMLFileSaveAsAction(final SHTMLPanelMultipleDocImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelMultipleDocImpl.saveAsAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            boolean canSave = true;
            final Preferences prefs = Preferences.userNodeForPackage(panel.getClass());
            final JFileChooser chooser = new JFileChooser();
            final ExampleFileFilter filter = new ExampleFileFilter();
            filter.addExtension("htm");
            filter.addExtension("html");
            filter.setDescription(Util.getResourceString("htmlFileDesc"));
            chooser.setFileFilter(filter);
            final String lastSaveFileName = prefs.get(SHTMLPanelImpl.FILE_LAST_SAVE, "");
            if (!lastSaveFileName.isEmpty()) {
                chooser.setCurrentDirectory(new File(lastSaveFileName).getParentFile());
            }
            final URL sourceUrl = panel.getDocumentPane().getSource();
            String fName;
            if (sourceUrl != null) {
                fName = sourceUrl.getFile();
            }
            else {
                fName = panel.getDocumentPane().getDocumentName();
                fName = Util.removeChar(fName, ' ');
            }
            if (!fName.contains(Util.CLASS_SEPARATOR)) {
                chooser.setSelectedFile(new File(fName + ".htm"));
            }
            else {
                chooser.setSelectedFile(new File(fName));
            }
            final int result = chooser.showSaveDialog((Component) ae.getSource());
            if (result == JFileChooser.APPROVE_OPTION) {
                final File selection = chooser.getSelectedFile();
                prefs.put(SHTMLPanelImpl.FILE_LAST_SAVE, selection.getAbsolutePath());
                if (selection.exists()) {
                    final String newName = selection.getName();
                    canSave = Util.msg(JOptionPane.YES_NO_OPTION, "confirmSaveAs", "fileExistsQuery", newName, " ");
                }
                if (canSave) {
                    try {
                        final NewFileSaver saver = new NewFileSaver(panel.getDocumentPane(), selection.toURL(),
                            panel.getActiveTabNo());
                        saver.setName("NewFileSaver");
                        saver.start();
                    }
                    catch (final Exception ex) {
                        Util.errMsg((Component) ae.getSource(), Util.getResourceString("cantCreateURLError")
                                + selection.getAbsolutePath(), ex);
                    }
                }
            }
            panel.updateActions();
        }

        /**
         * Helper class for being able to save a document in a separate thread.
         * Using a separate thread will not cause the application to block during
         * a lengthy save operation
         */
        class NewFileSaver extends Thread {
            final DocumentPane dp;
            final URL url;
            final int activeTabNo;
            DocumentPane.DocumentPaneListener l;

            NewFileSaver(final DocumentPane dp, final URL url, final int activeTabNo) {
                this.dp = dp;
                this.url = url;
                this.activeTabNo = activeTabNo;
            }

            NewFileSaver(final DocumentPane dp, final URL url, final int activeTabNo,
                         final DocumentPane.DocumentPaneListener listener) {
                this(dp, url, activeTabNo);
                l = listener;
            }

            public void run() {
                panel.doSave(dp, url);
                if (dp.saveSuccessful) {
                    panel.getTabbedPaneForDocuments().setTitleAt(
                        panel.getTabbedPaneForDocuments().indexOfComponent(dp), dp.getDocumentName());
                    if (l != null) {
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
        public NewFileSaver createNewFileSaver(final URL url) {
            return new NewFileSaver(panel.getDocumentPane(), url, panel.getActiveTabNo());
        }

        /**
         * get a FileSaver object for the document currently active
         *
         * @param url  the url of the file to save
         */
        public NewFileSaver createNewFileSaver(final URL url, final DocumentPane.DocumentPaneListener listener) {
            return new NewFileSaver(panel.getDocumentPane(), url, panel.getActiveTabNo(), listener);
        }

        public void update() {
            final boolean isEnabled = panel.getSHTMLEditorPane() != null;
            boolean saveInProgress = false;
            if (isEnabled) {
                saveInProgress = panel.getDocumentPane().saveInProgress();
            }
            this.setEnabled(isEnabled && !saveInProgress);
        }
    }

    /**
     * a slot for testing certain things conveniently during development
     */
    static class SHTMLTestAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SHTMLTestAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.testAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLEditorPane().insertBreak();
        }

        public void update() {
        }
    }

    /** show information about SimplyHTML in a dialog */
    static class SHTMLHelpAppInfoAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SHTMLHelpAppInfoAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.aboutAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            final AboutBox dlg = new AboutBox(parent);
            Util.center(parent, dlg);
            dlg.setModal(true);
            dlg.setVisible(true);
            panel.repaint();
            panel.updateActions();
        }

        public void update() {
        }
    }

    /**
     * action to set a reference to the default style sheet
     * (for being able to use an already existing style sheet
     * without having to define named styles)
     */
    static class SetDefaultStyleRefAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SetDefaultStyleRefAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.setDefaultStyleRefAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            panel.getSHTMLDocument().insertStyleRef();
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive() && !panel.getSHTMLDocument().hasStyleRef());
        }
    }

    /**
     * action to change the paragraph style
     */
    static class EditNamedStyleAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public EditNamedStyleAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.editNamedStyleAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            final ParaStyleDialog dlg = new ParaStyleDialog(parent, Util.getResourceString("namedStyleDialogTitle"),
                panel.getSHTMLDocument());
            Util.center(parent, dlg);
            dlg.setModal(true);
            dlg.setValue(panel.getMaxAttributes(panel.getSHTMLEditorPane()));
            dlg.setVisible(true);
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

	static public class RemoveStyleAttributeAction extends AbstractAction implements SHTMLAction {
	    final private Object[] attributes;

	    private final SHTMLPanelImpl panel;

        public RemoveStyleAttributeAction(final SHTMLPanelImpl panel, String name, Object... attributes) {
	        super(name);
			this.panel = panel;
	        this.attributes = attributes;
	        SHTMLPanelImpl.configureActionProperties(this, name);
        }

		public void actionPerformed(ActionEvent e) {
		    if(!panel.isWYSIWYGEditorActive()){
		    	return;
		    }
		    final JEditorPane editor = panel.getSHTMLEditorPane();
		    final int selectionStart = editor.getSelectionStart();
		    final int selectionEnd = editor.getSelectionEnd();
		    if(selectionStart == selectionEnd){
		    	return;
		    }
		    for(Object attribute : attributes)
		    	SHTMLEditorKit.removeCharacterAttributes((StyledDocument) editor.getDocument(), attribute, selectionStart, selectionEnd - selectionStart);
	    }

        public void update() {
            this.setEnabled(panel.isWYSIWYGEditorActive());
        }
   }


    static class ClearFormatAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public ClearFormatAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.clearFormatAction);
        }

        /**
         * do the format change for the associated attribute
         *
         * <p>This reverses the current setting for the associated attribute</p>
         *
         * @param  e  the ActionEvent describing the cause for this action
         */
        public void actionPerformed(final ActionEvent e) {
            final SHTMLEditorPane editor = panel.getSHTMLEditorPane();
            if (editor != null) {
                if (editor.getSelectionStart() != editor.getSelectionEnd()) {
                    editor.removeCharacterAttributes();
                }
                else {
                    editor.removeParagraphAttributes();
                }
            }
            panel.updateActions();
        }
        public void update() {
        	 this.setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * action to find and replace a given text
     */
    static class MultipleDocFindReplaceAction extends AbstractAction implements SHTMLAction, FindReplaceListener {
        /**
         *
         */
        private final SHTMLPanelMultipleDocImpl panel;

        public MultipleDocFindReplaceAction(final SHTMLPanelMultipleDocImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelMultipleDocImpl.configureActionProperties(this, SHTMLPanelMultipleDocImpl.findReplaceAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            currentTab = panel.getTabbedPaneForDocuments().getSelectedIndex();
            caretPos = panel.getDocumentPane().getEditor().getCaretPosition();
            if (panel.getTabbedPaneForDocuments().getTabCount() > 1) {
                new FindReplaceDialog(panel.getMainFrame(), panel.getSHTMLEditorPane(), this);
            }
            else {
                new FindReplaceDialog(panel.getMainFrame(), panel.getSHTMLEditorPane());
            }
        }

        public void update() {
            if (panel.isHtmlEditorActive()) {
                this.setEnabled(false);
                return;
            }
            this.setEnabled(panel.getTabbedPaneForDocuments().getTabCount() > 0);
        }


        public void getNextDocument(final FindReplaceEvent e) {
            final FindReplaceDialog frd = (FindReplaceDialog) e.getSource();
            final int tabCount = panel.getTabbedPaneForDocuments().getTabCount();
            int curTab = panel.getTabbedPaneForDocuments().getSelectedIndex();
            System.out.println("FindReplaceAction.getNextDocument curTab=" + curTab + ", tabCount=" + tabCount);
            if (++curTab < tabCount) {
                System.out.println("FindReplaceAction.getNextDocument next tab no=" + curTab);
                resumeWithNewEditor(frd, curTab);
            }
            else {
                frd.terminateOperation();
            }
        }

        public void getFirstDocument(final FindReplaceEvent e) {
            final FindReplaceDialog frd = (FindReplaceDialog) e.getSource();
            resumeWithNewEditor(frd, 0);
        }

        public void findReplaceTerminated(final FindReplaceEvent e) {
            panel.getTabbedPaneForDocuments().setSelectedIndex(currentTab);
            final DocumentPane docPane = (DocumentPane) panel.getTabbedPaneForDocuments().getSelectedComponent();
            final JEditorPane editor = docPane.getEditor();
            editor.setCaretPosition(caretPos);
            editor.requestFocus();
        }

        private void resumeWithNewEditor(final FindReplaceDialog frd, final int tabNo) {
            panel.getTabbedPaneForDocuments().setSelectedIndex(tabNo);
            final DocumentPane docPane = (DocumentPane) panel.getTabbedPaneForDocuments().getComponentAt(tabNo);
            final JEditorPane editor = docPane.getEditor();
            editor.requestFocus();
            frd.setEditor(editor);
            frd.resumeOperation();
        }

        private int caretPos;
        private int currentTab;
    }

    /**
     * action to find and replace a given text
     */
    static class SingleDocFindReplaceAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public SingleDocFindReplaceAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.findReplaceAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            currentDocumentPane = panel.getDocumentPane();
            if (currentDocumentPane != null) {
                caretPos = currentDocumentPane.getEditor().getCaretPosition();
                new FindReplaceDialog(panel.getMainFrame(), panel.getSHTMLEditorPane());
            }
        }

        public void update() {
            if (panel.isHtmlEditorActive()) {
                this.setEnabled(false);
                return;
            }
            this.setEnabled(panel.getDocumentPane() != null);
        }

        public void findReplaceTerminated(final FindReplaceEvent e) {
            if (currentDocumentPane.isVisible()) {
                final JEditorPane editor = currentDocumentPane.getEditor();
                editor.setCaretPosition(caretPos);
                editor.requestFocus();
            }
        }

        private int caretPos;
        private DocumentPane currentDocumentPane;
    }

    /**
     * Show a dialog to format fonts
     */
    static class FontAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public FontAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.fontAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            panel.getSHTMLEditorPane().requestFocus();
            /* create a modal FontDialog, center and show it */
            final FontDialog fd = new FontDialog(parent, Util.getResourceString("fontDialogTitle"),
                panel.getMaxAttributes(panel.getSHTMLEditorPane()));
            Util.center(parent, fd);
            fd.setModal(true);
            fd.setVisible(true);
            /* if the user made a selection, apply it to the document */
            if (fd.getResult() == FontDialog.RESULT_OK) {
                panel.getSHTMLEditorPane().applyAttributes(fd.getAttributes(), false);
                panel.updateFormatControls();
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * change a font family setting
     */
    static class FontFamilyAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public FontFamilyAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.fontFamilyAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final FontFamilyPicker ffp = ((FontFamilyPicker) ae.getSource());
            if (!ffp.ignore()) {
                panel.getSHTMLEditorPane().applyAttributes(ffp.getValue(), false);
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * change a font size setting
     */
    static class FontSizeAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public FontSizeAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.fontSizeAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final FontSizePicker fsp = ((FontSizePicker) ae.getSource());
            if (!fsp.ignore()) {
                panel.getSHTMLEditorPane().applyAttributes(fsp.getValue(), false);
            }
            panel.updateActions();
        }

        public void update() {
        	setEnabled(panel.isWYSIWYGEditorActive());
        }
    }

    /**
     * change a font size setting
     */
    static class ChangeFontSizeAction extends AbstractAction implements SHTMLAction {
    	enum Change{INCREASE(1), DECREASE(-1);
    		final int changeAmount;

			Change(int changeAmount) {
				this.changeAmount = changeAmount;
			}
    	}

        /**
         *
         */
        private final SHTMLPanelImpl panel;
		private final Change change;

        ChangeFontSizeAction(final SHTMLPanelImpl panel, String name, Change change ) {
            super(name);
            this.panel = panel;
			this.change = change;
            SHTMLPanelImpl.configureActionProperties(this, name);

        }

        public void actionPerformed(final ActionEvent ae) {
			final SHTMLEditorPane editorPane = panel.getSHTMLEditorPane();
			final AttributeSet a = panel.getMaxAttributes(editorPane);
			final int size = Util.styleSheet().getFont(a).getSize();
			int index = 0;
			for (String availableSizeAsString : SHTMLPanelImpl.FONT_SIZES){
				final int availableSizeAsNumber = Integer.parseInt(availableSizeAsString);
				if(size < availableSizeAsNumber) {
					setSize(change == Change.INCREASE ? index + 1 : index);
					return;
				}
				else if(size == availableSizeAsNumber) {
					setSize(index + change.changeAmount);
					return;
				}
				else {
					index++;
					if(index == SHTMLPanelImpl.FONT_SIZES.length && change == Change.DECREASE) {
						setSize(index - 1);
						return;
					}
				}
			}
        }

        private void setSize(int index) {
        	if(index >= 0 && index < SHTMLPanelImpl.FONT_SIZES.length) {
                final SimpleAttributeSet set = new SimpleAttributeSet();
                Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_SIZE, SHTMLPanelImpl.FONT_SIZES[index] + "pt");
                panel.getSHTMLEditorPane().applyAttributes(set, false);
                panel.updateActions();
        	}
		}

		public void update() {
			 this.setEnabled(panel.isWYSIWYGEditorActive());
        }

    }

    static class FormatImageAction extends AbstractAction implements SHTMLAction {
        /**
         *
         */
        private final SHTMLPanelImpl panel;

        public FormatImageAction(final SHTMLPanelImpl panel) {
            super();
            this.panel = panel;
            SHTMLPanelImpl.configureActionProperties(this, SHTMLPanelImpl.formatImageAction);
        }

        public void actionPerformed(final ActionEvent ae) {
            final Frame parent = JOptionPane.getFrameForComponent(panel);
            final ImageDialog dlg = new ImageDialog(parent, Util.getResourceString("imageDialogTitle"), panel.getDocumentPane().getDocument());
            final Element img = panel.getSHTMLDocument().getCharacterElement(
                panel.getSHTMLEditorPane().getCaretPosition());
            if (img.getName().equalsIgnoreCase(HTML.Tag.IMG.toString())) {
                Util.center(parent, dlg);
                dlg.setImageAttributes(img.getAttributes());
                dlg.setModal(true);
                dlg.setVisible(true);
                /* if the user made a selection, apply it to the document */
                if (dlg.getResult() == DialogShell.RESULT_OK) {
                    try {
                        panel.getSHTMLDocument().setOuterHTML(img, dlg.getImageHTML());
                    }
                    catch (final Exception e) {
                        Util.errMsg(null, e.getMessage(), e);
                    }
                }
                panel.updateActions();
            }
        }

        public void update() {
            if (panel.isHtmlEditorActive()) {
                this.setEnabled(false);
                return;
            }
        }
    }
}
