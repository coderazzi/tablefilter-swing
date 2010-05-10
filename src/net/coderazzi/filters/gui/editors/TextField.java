/**
 * Author:  Luis M Pena  ( dr.lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  dr.lu@coderazzi.net
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

package net.coderazzi.filters.gui.editors;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.RowFilter;

import net.coderazzi.filters.IFilterObservable;
import net.coderazzi.filters.IFilterObserver;
import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.IFilterTextParser;


/**
 * Class implementing all the filter logic via text parsing to a given JTextField
 *
 * @author  Luis M Pena - dr.lu@coderazzi.net
 */
abstract class TextField implements IFilterObservable {

    /** Default value for a wrong foreground color */
    public static final Color WRONG_COLOR = Color.red;

    /** Associated observers */
    private Set<IFilterObserver> observers = new HashSet<IFilterObserver>();

    /** Incorrect position, or -1 if the parsing is correct */
    private int incorrectPosition;

    /** Filter position, as associated to the RowFile.Entry index */
    private int filterPosition = IFilterTextParser.NO_FILTER_POSITION;

    /** The filter text parser instance */
    private IFilterTextParser parser;

    /** The filter created by the parser, if correct */
    private RowFilter filter;

    private Color foregroundColor;

    private Color backgroundColor;

    private Color wrongForegroundColor = WRONG_COLOR;

    private Color wrongBackgroundColor;

    /**
     * Cache entry, to avoid reparsing same content It is also used to reset the initial text when
     * pressing Escape
     */
    String cachedFilterText = "";

    /** The associated text field */
    JTextField editor;

    /**
     * Only constructor, requires the given text field.
     */
    public TextField(JTextField component) {
        editor = component;
        incorrectPosition = -1;
        foregroundColor = component.getForeground();
        backgroundColor = component.getBackground();

        //parse automatically the text when losing the focus on the text field
        editor.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    cachedFilterText = editor.getText();
                }

                @Override public void focusLost(FocusEvent e) {
                    propagateFilter(false);
                }
            });

        //handle automatically Enter key and Escape (reset the initial text)
        editor.addKeyListener(new KeyAdapter() {
                @Override public void keyPressed(KeyEvent e) {

                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        propagateFilter(true);
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        setText(cachedFilterText);
                    }
                }
            });
    }

    /**
     * Sets the parser used to parse the text.
     */
    public void setTextParser(IFilterTextParser parser) {
        this.parser = parser;
        propagateFilter(true);
    }

    /**
     * Returns the used parser
     */
    public IFilterTextParser getTextParser() {
        return parser;
    }


    /**
     * Sets the expression to parse
     */
    public void setText(String t) {
        editor.setText(t);
        propagateFilter(false);
    }

    /**
     * Sets the filter position (as associated to the RowFile.Entry index) associated to this editor
     */
    public void setFilterPosition(int filterPosition) {
        this.filterPosition = filterPosition;
    }

    /**
     * Returns the filter position
     */
    public int getFilterPosition() {
        return this.filterPosition;
    }

    /**
     * IFilterObservable interface
     */
    public void addFilterObserver(IFilterObserver listener) {
        observers.add(listener);
    }

    /**
     * IFilterObservable interface
     */
    public void removeFilterObserver(IFilterObserver listener) {
        observers.remove(listener);
    }

    /**
     * Sets the foreground color used to represent no error conditions.
     */
    public void setForeground(Color fg) {
        foregroundColor = fg;
        updateColors();
    }

    /**
     * Sets the background color used to represent no error conditions.
     */
    public void setBackground(Color bg) {
        backgroundColor = bg;
        updateColors();
    }

    /**
     * Sets the foreground color used to represent error conditions.
     */
    public void setErrorForeground(Color fg) {
        this.wrongForegroundColor = fg;
        updateColors();
    }

    /**
     * Returns the foreground color used to represent error conditions.
     */
    public Color getErrorForeground() {
        return this.wrongForegroundColor;
    }

    /**
     * Sets the background color used to represent error conditions.
     */
    public void setErrorBackground(Color bg) {
        this.wrongBackgroundColor = bg;
        updateColors();
    }

    /**
     * Returns the background color used to represent error conditions.
     */
    public Color getErrorBackground() {
        return this.wrongBackgroundColor;
    }

    /**
     * Enables/disables the text, propagating accordingly a filter
     */
    public void setEnabled(boolean enabled) {
        cachedFilterText = null;

        if (enabled) {
            propagateFilter(true);
        } else {
            reportFilterUpdatedToObservers(null);
        }        
        updateColors();
    }

    /**
     * Default update operation: it parses the text, even if it was already parsed, propagating the
     * created fiter
     */
    public void updateFilter() {
        propagateFilter(true);
    }


    /**
     * Internal method, invoked when the text or the environment changes It parses the text, if
     * needed or if the parameter force is true, and propagates the created filter to the observers.
     * It handles error and no error conditions, setting the colors, etc
     */
    void propagateFilter(boolean force) {
        String text = editor.getText();

        if ((parser != null) && (force || !text.equals(cachedFilterText))) {
            cachedFilterText = text;

            try {
                filter = parser.parseText(cachedFilterText, filterPosition);
                editor.setToolTipText(cachedFilterText);
                incorrectPosition = -1;
                newHistoricEntry(cachedFilterText);
            } catch (FilterTextParsingException ftpe) {
                filter = null;
                editor.setToolTipText(ftpe.getMessage());
                incorrectPosition = ftpe.getPosition();
                editor.setCaretPosition(incorrectPosition);
            }
            updateColors();
            reportFilterUpdatedToObservers(filter);
        }
    }
    
    
    /**
     * Updates the colors in the editor
     */
    private void updateColors()
    {
    	if (editor.isEnabled()){
	    	if (incorrectPosition==-1){
	            editor.setForeground(foregroundColor);
	            editor.setBackground(backgroundColor);    		
	    	}
	    	else{
	            if (wrongForegroundColor != null){
	                editor.setForeground(wrongForegroundColor);
	            }
	            if (wrongBackgroundColor != null){
	                editor.setBackground(wrongBackgroundColor);
	            }    		
	    	}
    	} else {
    		editor.setForeground(null);
    		editor.setBackground(null);    		
    	}
    }

    /**
     * reports to the observers that the filter has changed
     */
    protected void reportFilterUpdatedToObservers(RowFilter filter) {
        for (IFilterObserver obs : new ArrayList<IFilterObserver>(observers))
            obs.filterUpdated(this, filter);
    }

    /**
     * Hook method, adds a given text to the associated historic list
     */
    protected abstract void newHistoricEntry(String text);
}
