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
import java.text.ParseException;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.coderazzi.filters.IFilterTextParser;


/**
 * Private interface, defining the editor component [usually a text field]<br>
 * There are two such implementations, the usual one, represented by a
 * {@link JTextField}, and a non-text-based one, which renders the content using
 * a {@link ListCellRenderer} component.<br>
 */
interface EditorComponent {

    public static final String EMPTY_FILTER = "";

    /** Returns the swing component associated to the editor */
    public JComponent getComponent();

    /** 
     * Call always before {@link #getFilter()} to verify if the filter
     * has been updated. 
     * @param forceUpdate set to true if the filter must been updated, 
     *   even when the editor's content is not
     * @return the new filter  
     */
    public RowFilter checkFilterUpdate(boolean forceUpdate);

    /**
     * Returns the filter associated to the current content.<br> 
     * Always invoked after {@link #checkFilterUpdate(boolean)}
     */
    public RowFilter getFilter();

    /** 
     * Informs that the editor has received the focus.
     * @return true if the associated popup should be shown 
     */
    public boolean focusGained(boolean gained);
    
    /** Enables/disables the editor */
    public void setEnabled(boolean enabled);

    /** Sets the {@link IFilterTextParser} associated to the editor */
    public void setTextParser(IFilterTextParser parser);

    /** Return the associated {@link IFilterTextParser}*/
    public IFilterTextParser getTextParser();

    /** Sets the content of the editor */
    public void setContent(Object content);

    /** Returns the definition associated to the current editor */
    public Object getContent();
    
    /** 
     * Defines the filter position associated to this editor.<br>
     * It corresponds to the table's model column
     **/
    public void setPosition(int position);

    /** Returns the filter position associated to this editor */
    public int getPosition();

    /** Sets the editable flag. If editable, the user can enter any content */
    public void setEditable(boolean set);

    /** Returns the editable flag*/
    public boolean isEditable();

    /** Sets the color used to show filter's errors (invalid syntax) */
    public void setErrorForeground(Color fg);

    /** Returns the color used to show filter's errors */
    public Color getErrorForeground();

    /** Sets the color used to represent disabled state */
    public void setDisabledForeground(Color fg);

    /** Returns the color used to represent disabled state */
    public Color getDisabledForeground();

    /** Sets the foreground color used*/
    public void setForeground(Color fg);

    /** Returns the foreground color used*/
    public Color getForeground();

    /**
     * EditorComponent for text edition, backed up with a {@link JTextField}<br>
     * It is editable by default.
     */
    static final class Text extends DocumentFilter 
    		implements DocumentListener, EditorComponent {

        private JTextField textField = new JTextField(15);
        private IFilterTextParser parser;
        private RowFilter cachedFilter;
        private String cachedContent;
        private int filterPosition;
        private boolean editable;
        private boolean enabled;
        private Color errorColor = Color.red;
        private Color foreground;
        private Color disabledColor;
        PopupComponent popup;
        /** 
         * This variable is set to true if the content is being set from inside, 
         * to avoid raising some events 
         **/
        private boolean controlledSet;

        public Text(PopupComponent popupComponent) {
            this.popup = popupComponent;
            setEditable(true);
            //if the user moves the cursor on the editor, the focus passes 
            //automatically back to the editor (from the popup)
            textField.addCaretListener(new CaretListener() {
                    @Override
					public void caretUpdate(CaretEvent e) {
                        popup.setPopupFocused(false);
                    }
                });
        }

        @Override public JComponent getComponent() {
            return textField;
        }

        @Override public RowFilter checkFilterUpdate(boolean forceUpdate) {
            String content = textField.getText().trim();
            if (forceUpdate || !content.equals(cachedContent)) {
	            cachedContent = content;
	            cachedFilter = parseText(content);
            }
            return cachedFilter;
        }
        
        @Override public RowFilter getFilter() {
            return cachedFilter;
        }

        @Override public boolean focusGained(boolean gained) {
    		textField.setCaretPosition(0);
        	if (gained){
        		textField.moveCaretPosition(textField.getText().length());
        	}
            return !editable;
        }

        @Override public void setEnabled(boolean enabled) {
        	this.enabled=enabled;
        	textField.setFocusable(enabled);
        	ensureCorrectForegroundColor();
        }

        @Override public void setTextParser(IFilterTextParser parser) {
            this.parser = parser;
            if (textField.isEnabled()) {
                checkFilterUpdate(true);
            }
        }
        
        @Override public IFilterTextParser getTextParser() {
            return parser;
        }

        @Override public void setContent(Object content) {
            setControlledSet();
            // the filterEditor verifies already that the content is a string
            textField.setText((String) content);
        }

        @Override public void setPosition(int position) {
            this.filterPosition = position;
        }

        @Override public int getPosition() {
            return filterPosition;
        }

        @Override public Object getContent() {
            return textField.getText();
        }

        @Override public void setEditable(boolean set) {
        	//dispose first the current listeners
            if (isEditable()) {
                textField.getDocument().removeDocumentListener(this);
            } else {
                ((AbstractDocument) textField.getDocument()).
                	setDocumentFilter(null);
            }
            editable = set;
            if (set) {
                textField.getDocument().addDocumentListener(this);
            } else {
                // ensure that the text contains something okay
                String proposal = getProposalOnEdition(textField.getText(), 
                		                               false);
                textField.setText((proposal == null) ? EMPTY_FILTER : proposal);
                ((AbstractDocument) textField.getDocument()).
                	setDocumentFilter(this);
            }
            controlledSet = false;
        }

        @Override public boolean isEditable() {
            return editable;
        }

        @Override public void setErrorForeground(Color fg) {
            errorColor = fg;
        	ensureCorrectForegroundColor();
        }

        @Override public Color getErrorForeground() {
            return errorColor;
        }

        @Override public void setDisabledForeground(Color fg) {
        	disabledColor=fg;
        	ensureCorrectForegroundColor();        	
        }

        @Override public Color getDisabledForeground() {
            return disabledColor;
        }

        @Override public void setForeground(Color fg) {
            this.foreground = fg;
        	ensureCorrectForegroundColor();
        }

        @Override public Color getForeground() {
            return foreground;
        }

        /** Ensures that the correct foreground is on use*/
        private void ensureCorrectForegroundColor(){
        	if (enabled){
        		parseText(textField.getText());
        	} else {
        		textField.setForeground(disabledColor);
        	}
        }

        /** 
         * Returns the filter associated to the current content, 
         * and sets the foreground color -showing errors, if existing-
         **/
        private RowFilter parseText(String content) {
        	RowFilter ret;
            Color color = getForeground();
            if (content.length() == 0) {
                ret = null;
            } else {
                try {
                    ret = parser.parseText(cachedContent, filterPosition);
                } catch (ParseException pex) {
                    ret = null;
                    color = getErrorForeground();
                }
            }
            textField.setForeground(color);
            return ret;
        }

        /** defines that the content is being set from inside */
        public void setControlledSet() {
            controlledSet = true;
        }

        /** Returns a proposal for the current edition*/
        private String getProposalOnEdition(String hint, boolean perfectMatch) {
            String ret = popup.selectBestMatch(hint, perfectMatch);
            popup.setPopupFocused(false);
            return ret;
        }

        /** {@link DocumentFilter}: method called if handler is not editable */
        @Override
        public void insertString(FilterBypass fb,
                                 int offset,
                                 String string,
                                 AttributeSet attr) {
            // we never use it, we never invoke Document.insertString
            // note that normal (non programmatically) editing only invokes
            // replace/remove
        }

        /** {@link DocumentFilter}: method called if handler is not editable */
        @Override
        public void replace(FilterBypass fb,
                            int offset,
                            int length,
                            String text,
                            AttributeSet attrs) throws BadLocationException {
            String buffer = textField.getText();
            String newContentBegin = buffer.substring(0, offset) + text;
            String newContent = 
            	newContentBegin + buffer.substring(offset + length);
            String proposal = getProposalOnEdition(newContent, true);
            if (proposal == null) {
                // why this part? Imagine having text "se|cond" with the cursor
                // at "|". Nothing is selected.
                // if the user presses now 'c', the code above would imply
                // getting "seccond", which is probably wrong, so we try now
                // to get a proposal starting at 'sec' ['sec|ond']
                proposal = getProposalOnEdition(newContentBegin, true);
                if (proposal == null) {
                    return;
                }
                newContent = newContentBegin;
            }
            int caret;
            if (controlledSet) {
                controlledSet = false;
                caret = 0;
            } else {
                caret = 1 + Math.min(textField.getCaret().getDot(), 
                		             textField.getCaret().getMark());
            }
            super.replace(fb, 0, buffer.length(), proposal, attrs);
            textField.setCaretPosition(proposal.length());
            textField.moveCaretPosition(caret);
        }

        /** {@link DocumentFilter}: method called if handler is not editable */
        @Override
        public void remove(FilterBypass fb,
                           int offset,
                           int length) throws BadLocationException {
            int caret = textField.getCaret().getDot();
            int mark = textField.getCaret().getMark();
            String buffer = textField.getText();
            String newContent = buffer.substring(0, offset) 
            	+ buffer.substring(offset + length);
            String proposal = getProposalOnEdition(newContent, true);
            if (!newContent.equals(proposal)) {
                if (proposal == null) {
                    proposal = getProposalOnEdition(newContent, false);
                    if (proposal == null) {
                        return;
                    }
                }
                if (matchCount(proposal, newContent) 
                		<= matchCount(buffer, newContent)) {
                    proposal = buffer;
                }
            }
            super.replace(fb, 0, buffer.length(), proposal, null);
            
            //special case if the removal is due to BACK SPACE
    		AWTEvent ev = EventQueue.getCurrentEvent();
    		if ((ev instanceof KeyEvent) 
    				&& ((KeyEvent)ev).getKeyCode() == KeyEvent.VK_BACK_SPACE){
                if (caret > mark) {
                    caret = mark;
                } else if (buffer == proposal) {
                    --caret;
                } else if (caret == mark) {
                    caret = offset;
                }    			
    		} 
            textField.setCaretPosition(proposal.length());
            textField.moveCaretPosition(caret);
        }

        /** returns the number of starting chars matching among both pars */
        private int matchCount(String a,
                               String b) {
            int max = Math.min(a.length(), b.length());
            for (int i = 0; i < max; i++) {
                if (a.charAt(i) != b.charAt(i)) {
                    return i;
                }
            }
            return max;
        }

        /** {@link DocumentListener}: method called when handler is editable */
        @Override
		public void changedUpdate(DocumentEvent e) {
            // no need to handle updates
        }

        /** {@link DocumentListener}: method called when handler is editable */
        @Override
		public void removeUpdate(DocumentEvent e) {
            getProposalOnEdition(textField.getText(), false);
        }

        /** {@link DocumentListener}: method called when handler is editable */
        @Override
		public void insertUpdate(DocumentEvent e) {
            getProposalOnEdition(textField.getText(), false);
        }

    }

    /**
     * Editor component for cell rendering
     */
    static final class Rendered extends JComponent implements EditorComponent {

        private static final long serialVersionUID = -7162028817569553287L;

        private Object content = EMPTY_FILTER;
        private FilterListCellRenderer renderer;
        private CellRendererPane painter = new CellRendererPane();
        private IFilterTextParser parser;
        private RowFilter filter;
        private Color errorColor;
        private Color disabledColor;
        Object cachedContent;
        int filterPosition;

        public Rendered(FilterListCellRenderer renderer) {
            this.renderer = renderer;
        }
        
        @Override public JComponent getComponent() {
            return this;
        }

        @Override public RowFilter checkFilterUpdate(boolean forceUpdate) {
            Object currentContent = getContent();
            if (forceUpdate || (currentContent != cachedContent)) {
	            cachedContent = currentContent;
	            if (EMPTY_FILTER.equals(cachedContent)) {
	                filter = null;
	            } else {
	                filter = new RowFilter() {
	                        @Override
	                        public boolean include(RowFilter.Entry entry) {
	                            Object val = entry.getValue(filterPosition);
	                            return (val == null) ? (cachedContent == null) 
	                            		: val.equals(cachedContent);
	                        }
	                    };
	            }
            }
            return filter;
        }

        @Override public RowFilter getFilter() {
            return filter;
        }

        @Override public boolean focusGained(boolean gained) {
            return true;
        }

        @Override public void setEnabled(boolean enabled) {
        	renderer.setEnabled(enabled);
        }

        @Override public void setTextParser(IFilterTextParser parser) {
            this.parser = parser;
            cachedContent = null;
        }

        @Override public IFilterTextParser getTextParser() {
            return parser;
        }
      
        @Override public void setContent(Object content) {
            this.content = (content == null) ? EMPTY_FILTER : content;
            repaint();
        }

        @Override public Object getContent() {
            return content;
        }

        @Override public void setPosition(int position) {
            this.filterPosition = position;
        }

        @Override public int getPosition() {
            return filterPosition;
        }

        @Override public void setEditable(boolean set) {
            // cannot apply to the rendered component -at least, not currently
        }

        @Override public boolean isEditable() {
            return false;
        }

        @Override public void setErrorForeground(Color fg) {
            this.errorColor = fg;
        }

        @Override public Color getErrorForeground() {
            return errorColor;
        }

        @Override public void setDisabledForeground(Color fg) {
            disabledColor = fg;
        }

        @Override public Color getDisabledForeground() {
            return disabledColor;
        }

        @Override public boolean isShowing() {
            return true;
        }

        @Override protected void paintComponent(Graphics g) {
            Component c = renderer.getCellRendererComponent(content, getWidth());
            painter.paintComponent(g, c, this, 0, 0, getWidth(), getHeight());
        }
    }
}