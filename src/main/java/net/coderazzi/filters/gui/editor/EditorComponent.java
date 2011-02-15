/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coderazzi.filters.gui.editor;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.text.ParseException;

import java.util.Comparator;

import javax.swing.CellRendererPane;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.coderazzi.filters.IParser;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.Look;


/**
 * <p>Component representing the filter editor itself, where the user can enter
 * the filter text (if not rendered) and is displayed the current filter
 * choice.</p>
 *
 * <p>The underlying component is a {@link JTextField}, even when the content is
 * rendered.</p>
 */
public class EditorComponent extends JTextField {

    private static final long serialVersionUID = -2196080442586435546L;

    private Controller controller;
    private boolean focus;
    FilterEditor filterEditor;
    PopupComponent popupComponent;

    public EditorComponent(FilterEditor   editor,
                           PopupComponent popupComponent) {
        super(15); // created with 15 columns
        this.filterEditor = editor;
        this.popupComponent = popupComponent;
        this.controller = new EditableTextController();
    }

    @Override public void setUI(TextUI ui) {
        super.setUI(ui);
        // whatever the LookAndFeel, display no border
        setBorder(null);
    }

    @Override protected void paintComponent(Graphics g) {
        controller.paintComponent(g);
    }

    /** Updates the current look. */
    public void updateLook() {
        controller.updateLook();
    }

    /**
     * Returns the filter associated to the current content.<br>
     * Always invoked after {@link #checkFilterUpdate(boolean)}
     */
    public RowFilter getFilter() {
        return controller.getFilter();
    }

    /** Returns the definition associated to the current editor. */
    public Object getContent() {
        return controller.getContent();
    }

    /**
     * Sets the editor content<br>
     * The parameters escapeIt must be true if the content could require
     * escaping (otherwise, it will be always treated literally).<br>
     */
    public void setContent(Object content, boolean escapeIt) {
        controller.setContent(content, escapeIt);
    }

    /** Requests an update on the text parser used by the editor. */
    public void updateParser() {
        if (controller instanceof TextController) {
            ((TextController) controller).setParser(filterEditor.getParser());
        }
    }

    /** Returns the editable flag. */
    public boolean isEditableContent() {
        return controller instanceof EditableTextController;
    }

    /** Sets the text mode and editable flag. */
    public void setTextMode(boolean editable) {
        if (controller != null) {
            if (editable && (controller instanceof EditableTextController)) {
                return;
            }

            if (!editable
                    && (controller instanceof NonEditableTextController)) {
                return;
            }

            controller.detach();
        }

        if (editable) {
            controller = new EditableTextController();
        } else {
            controller = new NonEditableTextController();
        }

        updateParser();
    }

    /** Sets the render mode. */
    public void setRenderMode() {
        if (controller != null) {
            if (controller instanceof RenderedController) {
                return;
            }

            controller.detach();
        }

        controller = new RenderedController();
    }


    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (controller != null) {
            setCaretPosition(0);
            moveCaretPosition(0);
            updateLook();
            setFocusable(enabled);
        }
    }

    /** Returns true if the content is valid. */
    public boolean isValidContent() {
        return controller.isValidContent();
    }

    /**
     * Called always before {@link #getFilter()} to verify if the filter has
     * been updated.
     *
     * @param   forceUpdate  set to true if the filter must been updated, even
     *                       when the editor's content is not
     *
     * @return  the new filter
     */
    public RowFilter checkFilterUpdate(boolean forceUpdate) {
        return controller.checkFilterUpdate(forceUpdate);
    }

    /** Informs that the editor has received the focus. */
    public void focusMoved(boolean gained) {
        focus = gained;
        controller.focusMoved(gained);
    }


    boolean isFocused() {
        return focus;
    }

    Look prepareComponentLook(CustomChoice cc) {
        return popupComponent.getFilterRenderer().prepareComponentLook(this,
                isFocused(), cc);
    }

    void superPaintComponent(Graphics g) {
        super.paintComponent(g);
    }


    /** The JTextField is controlled via this interface. */
    private interface Controller {

        /**
         * Called to replace the basic {@link
         * JTextField#paintComponents(Graphics)} functionality.
         */
        void paintComponent(Graphics g);

        /** Detaches the controller, not to be used again. */
        void detach();

        /** @see  EditorComponent#setContent(Object, boolean) */
        void setContent(Object content, boolean escapeIt);

        /** @see  EditorComponent#getContent() */
        Object getContent();

        /** @see  EditorComponent#isValidContent() */
        boolean isValidContent();

        /** @see  EditorComponent#getFilter() */
        RowFilter getFilter();

        /** @see  EditorComponent#checkFilterUpdate(boolean) */
        RowFilter checkFilterUpdate(boolean forceUpdate);

        /** @see  EditorComponent#updateLook() */
        void updateLook();

        /** @see  EditorComponent#focusMoved(boolean) */
        void focusMoved(boolean gained);
    }


    /** Controller interface to handle editors with content rendered. */
    private class RenderedController extends MouseAdapter
        implements Controller {

        RowFilter filter;
        private Object content = CustomChoice.MATCH_ALL;
        Object cachedContent;
        private CellRendererPane painter = new CellRendererPane();

        RenderedController() {
            addMouseListener(this);
            setEditable(false);
        }

        @Override public void paintComponent(Graphics g) {
            Component c = popupComponent.getFilterRenderer()
                    .getCellRendererComponent(content, getWidth(), isFocused());
            painter.paintComponent(g, c, EditorComponent.this, 0, 0, getWidth(),
                getHeight());
        }

        @Override public void detach() {
            removeMouseListener(this);
        }

        @Override public void setContent(Object content, boolean escapeIt) {
            this.content = (content == null) ? CustomChoice.MATCH_ALL : content;
            repaint();
        }

        @Override public Object getContent() {
            return content;
        }

        @Override public boolean isValidContent() {
            return true;
        }

        @Override public RowFilter getFilter() {
            return filter;
        }

        @Override public RowFilter checkFilterUpdate(boolean forceUpdate) {
            Object currentContent = getContent();
            if (forceUpdate || (currentContent != cachedContent)) {
                cachedContent = currentContent;
                if (cachedContent instanceof CustomChoice) {
                    filter = ((CustomChoice) cachedContent).getFilter(
                            filterEditor);
                } else {
                    filter = new RowFilter() {
                        @Override public boolean include(
                                RowFilter.Entry entry) {
                            Object val = entry.getValue(
                                    filterEditor.getModelIndex());

                            return (val == null) ? (cachedContent == null)
                                                 : val.equals(cachedContent);
                        }
                    };
                }
            }

            return filter;
        }

        @Override public void updateLook() {
            prepareComponentLook(null);
        }

        @Override public void focusMoved(boolean gained) {
            repaint();
        }

        /** @see  MouseAdapter#mouseClicked(MouseEvent) */
        @Override public void mouseClicked(MouseEvent e) {
            if (isEnabled()) {
                filterEditor.triggerPopup(filterEditor);
            }
        }

    }


    /** Parent class of controllers with text enabled edition. */
    private abstract class TextController implements Controller, CaretListener {

        protected IParser textParser;
        private Object content;
        private RowFilter filter;
        private boolean error;
        private boolean useCustomDecoration;

        TextController() {
            setEditable(true);
            setText(CustomChoice.MATCH_ALL.toString());
            addCaretListener(this);
        }

        /**
         * Sets the parser used in the filter. This controller is not functional
         * until this parser is set
         */
        public void setParser(IParser textParser) {
            this.textParser = textParser;
            if (isEnabled()) {
                checkFilterUpdate(true);
            }
        }

        @Override public void paintComponent(Graphics g) {
            superPaintComponent(g);
            if (useCustomDecoration && (content instanceof CustomChoice)) {
                filterEditor.getLook().getCustomChoiceDecorator()
                    .decorateComponent((CustomChoice) content, filterEditor,
                        isFocused(), EditorComponent.this, g);
            }
        }

        @Override public void detach() {
            removeCaretListener(this);
        }

        @Override public Object getContent() {
            checkFilterUpdate(false);

            return content;
        }

        @Override public boolean isValidContent() {
            return !error;
        }

        @Override public RowFilter getFilter() {
            return filter;
        }

        @Override public RowFilter checkFilterUpdate(boolean forceUpdate) {
            String text = getText().trim();
            if (forceUpdate) {
                updateFilter(text);
            } else if (content instanceof CustomChoice) {
                if (!((CustomChoice) content).toString().equals(text)) {
                    updateFilter(text);
                }
            } else if (!text.equals(content)) {
                updateFilter(text);
            }

            return filter;
        }

        @Override public void updateLook() {
            CustomChoice cc =
                (useCustomDecoration && (content instanceof CustomChoice))
                ? (CustomChoice) content : null;
            Look look = prepareComponentLook(cc);
            if (isEnabled() && error) {
                Color foreground = look.getErrorForeground();
                if (foreground != getForeground()) {
                    setForeground(foreground);
                }
            }

            Color selection = look.getTextSelection();
            if (getSelectionColor() != selection) {
                setSelectionColor(selection);
            }
        }

        @Override public void focusMoved(boolean gained) {
            // whether we lose or gain focus, we try to reactivate the
            // decoration -listeners only required if gaining it
            // without focus, there is no need to check for changes on
            // the text when painting the decoration, so set text to null
            if (!activateCustomDecoration()) {
                updateLook();
            }
        }


        /** @see  CaretListener#caretUpdate(CaretEvent) */
        @Override public void caretUpdate(CaretEvent e) {
            // if the user moves the cursor on the editor, the focus passes
            // automatically back to the editor (from the popup)
            if (isEnabled()) {
                popupComponent.setPopupFocused(false);
                deactivateCustomDecoration();
            }
        }

        /** Reports that the current content is wrong. */
        protected void setError(boolean error) {
            this.error = error;
            if (isEnabled()) {
                updateLook();
            }
        }

        /** Returns a proposal for the current edition. */
        protected String getProposalOnEdition(String  hint,
                                              boolean perfectMatch) {
            String ret = popupComponent.selectBestMatch(hint, perfectMatch);
            popupComponent.setPopupFocused(false);

            return ret;
        }

        /**
         * Activates, if possible, the custom decoration, that is, if the
         * content is a CustomChoice and has an associated icon.
         */
        protected boolean activateCustomDecoration() {
            boolean ret = false;
            if (!useCustomDecoration && (content instanceof CustomChoice)) {
                useCustomDecoration = true;
                updateLook();
                repaint();
                ret = true;
            }

            return ret;
        }

        /** Deactivates the custom decoration. */
        protected void deactivateCustomDecoration() {
            if (useCustomDecoration) {
                useCustomDecoration = false;
                updateLook();
                repaint();
            }
        }


        /**
         * Subclasses must define this method to ensure that text is properly
         * escaped (needed in no editable controllers).
         */
        protected abstract boolean requiresTextEscaping();

        /** Updates the filter / text is expected trimmed. */
        private void updateFilter(String text) {
            boolean error = false;
            CustomChoice cc = (text.length() == 0)
                ? CustomChoice.MATCH_ALL : popupComponent.getCustomChoice(text);
            if (cc != null) {
                content = cc;
                activateCustomDecoration();
                filter = cc.getFilter(filterEditor);
            } else {
                deactivateCustomDecoration();
                content = text;
                try {
                    if (requiresTextEscaping()) {
                        // for editable columns, the text was already
                        // escaped when the content was defined
                        // (see setContent)
                        text = textParser.escape(text);
                    }

                    filter = textParser.parseText(text);
                } catch (ParseException pex) {
                    filter = null;
                    error = true;
                }
            }

            setError(error);
        }
    }


    /** TextController for editable content. */
    private class EditableTextController extends TextController
        implements DocumentListener {

        EditableTextController() {
            getDocument().addDocumentListener(this);
        }

        @Override public boolean requiresTextEscaping() {
            return false;
        }

        @Override public void detach() {
            super.detach();
            getDocument().removeDocumentListener(this);
        }

        @Override public void setContent(Object content, boolean escapeIt) {
            String text;
            if (content instanceof CustomChoice) {
                // never escape custom choices
                text = ((CustomChoice) content).toString();
            } else if (content instanceof String) {
                text = (String) content;
                if (escapeIt) {
                    text = textParser.escape(text);
                }
            } else {
                text = null;
            }

            setText(text);
            activateCustomDecoration();
        }

        /** {@link DocumentListener}: method called when handler is editable */
        @Override public void changedUpdate(DocumentEvent e) {
            // no need to handle updates
        }

        /** {@link DocumentListener}: method called when handler is editable */
        @Override public void removeUpdate(DocumentEvent e) {
            deactivateCustomDecoration();
            setError(false);
            getProposalOnEdition(getText(), false);
        }

        /** {@link DocumentListener}: method called when handler is editable */
        @Override public void insertUpdate(DocumentEvent e) {
            deactivateCustomDecoration();
            setError(false);
            getProposalOnEdition(getText(), false);
        }

    }


    /** TextController for non editable content. */
    private class NonEditableTextController extends TextController {

        boolean onSetText;

        NonEditableTextController() {
            ((AbstractDocument) getDocument()).setDocumentFilter(
                new ControllerDocumentFilter());
        }

        @Override public boolean requiresTextEscaping() {
            return true;
        }

        @Override public void detach() {
            super.detach();
            ((AbstractDocument) getDocument()).setDocumentFilter(null);
        }

        @Override public void setContent(Object content, boolean escapeIt) {
            String text;
            if (content instanceof CustomChoice) {
                // never escape custom choices
                text = ((CustomChoice) content).toString();
            } else if (content instanceof String) {
                // if no editable, the choices are directly handled, no need
                // to escape it. When the text is parsed, on no editable
                // columns, it will be then escaped.
                text = (String) content;
            } else {
                text = null;
            }

            onSetText = true;
            setText(text);
            onSetText = false;
            activateCustomDecoration();
        }

        /**
         * DocumentFilter instance to handle any user's input, ensuring that the
         * text always match any of the available choices.
         */
        class ControllerDocumentFilter extends DocumentFilter {

            /**
             * {@link DocumentFilter}: method called if handler is not editable
             */
            @Override public void insertString(FilterBypass fb,
                                               int          offset,
                                               String       string,
                                               AttributeSet attr) {
                // we never use it, we never invoke Document.insertString
                // note that normal (non programmatically) editing only invokes
                // replace/remove
            }

            /**
             * {@link DocumentFilter}: method called if handler is not editable
             */
            @Override public void replace(FilterBypass fb,
                                          int          offset,
                                          int          length,
                                          String       text,
                                          AttributeSet attrs)
                                   throws BadLocationException {
                String buffer = getText();
                String newContentBegin = buffer.substring(0, offset) + text;
                String newContent = newContentBegin
                    + buffer.substring(offset + length);
                String proposal = getProposalOnEdition(newContent, true);
                if (proposal == null) {

                    // why this part? Imagine having text "se|cond" with the
                    // cursor at "|". Nothing is selected. if the user presses
                    // now 'c', the code above would imply getting "seccond",
                    // which is probably wrong, so we try now to get a proposal
                    // starting at 'sec' ['sec|ond']
                    proposal = getProposalOnEdition(newContentBegin, true);
                    if (proposal != null) {
                        newContent = newContentBegin;
                    } else {
                        proposal = getProposalOnEdition(newContent, false);
                        if (proposal != null) {
                            // on text content, the string comparator cannot
                            // be null
                            if ((proposal.length() < newContentBegin.length())
                                    || (0
                                        != popupComponent.getStringComparator()
                                        .compare(newContentBegin,
                                            proposal.substring(0,
                                                newContentBegin.length())))) {
                                proposal = null;
                            }
                        }

                        if (proposal == null) {
                            return;
                        }

                        newContent = proposal;
                    }
                }

                int caret;
                if (onSetText) {
                    caret = 0;
                } else {
                    caret = 1
                        + Math.min(getCaret().getDot(), getCaret().getMark());
                }

                super.replace(fb, 0, buffer.length(), proposal, attrs);
                setCaretPosition(proposal.length());
                moveCaretPosition(caret);
                deactivateCustomDecoration();
            }

            /**
             * {@link DocumentFilter}: method called if handler is not editable
             */
            @Override public void remove(FilterBypass fb,
                                         int          offset,
                                         int          length)
                                  throws BadLocationException {
                int caret = getCaret().getDot();
                int mark = getCaret().getMark();
                String buffer = getText();
                String newContent = buffer.substring(0, offset)
                    + buffer.substring(offset + length);
                // on text content, this comparator cannot be null
                Comparator<String> comparator =
                    popupComponent.getStringComparator();
                String proposal = getProposalOnEdition(newContent, true);
                if ((proposal == null)
                        || (comparator.compare(newContent, proposal) != 0)) {
                    proposal = getProposalOnEdition(newContent, false);
                    if (proposal == null) {
                        return;
                    }

                    if (
                        PopupComponent.Match.getMatchingLength(proposal,
                                newContent, comparator)
                            <= PopupComponent.Match.getMatchingLength(buffer,
                                newContent, comparator)) {
                        proposal = buffer;
                    }
                }

                super.replace(fb, 0, buffer.length(), proposal, null);

                // special case if the removal is due to BACK SPACE
                AWTEvent ev = EventQueue.getCurrentEvent();
                if ((ev instanceof KeyEvent)
                        && (((KeyEvent) ev).getKeyCode()
                            == KeyEvent.VK_BACK_SPACE)) {
                    if (caret > mark) {
                        caret = mark;
                    } else if (buffer == proposal) {
                        --caret;
                    } else if (caret == mark) {
                        caret = offset;
                    }
                }

                setCaretPosition(proposal.length());
                moveCaretPosition(caret);
                deactivateCustomDecoration();
            }
        }
    }
}
