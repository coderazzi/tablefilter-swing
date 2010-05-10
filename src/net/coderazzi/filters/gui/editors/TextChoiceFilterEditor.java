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

import net.coderazzi.filters.IFilterObservable;
import net.coderazzi.filters.gui.ITableFilterEditor;
import net.coderazzi.filters.gui.ITableFilterTextBasedEditor;
import net.coderazzi.filters.parser.IFilterTextParser;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;


/**
 * <p>Table filter editor based on text parsing, represented by a {@link javax.swing.JComboBox} that
 * keeps track of the last used expressions.</p>
 *
 * <p>In addition, it supports the notion of 'choices', values provided as permanent entries in the
 * combobox.</p>
 *
 * @author  Luis M Pena - dr.lu@coderazzi.net
 */
public class TextChoiceFilterEditor extends JComboBox implements ITableFilterTextBasedEditor {

	private static final long serialVersionUID = 6599798587104542637L;

	/**
     * Default number of elements to keep on the contextual menu holding previous correctly parsed
     * entries.
     */
    public static final int DEFAULT_HISTORY = 5;

    /** The vector that backs up the combobox model */
    private Vector<String> comboBoxModelList = new Vector<String>();

    /**
     * The number of elements to keep on the contextual menu holding previous correctly parsed
     * entries.
     */
    private int history = DEFAULT_HISTORY;

    /** The number of choices inserted into the combobox model */
    private int choices = 0;
    
    /** The color currently set as background **/
    private Color backgroundColor;

    /** The TextField instance that handles all the work */
    TextField editor;

    /**
     * Default constructor. It is yet needed to set, at least, the text parser.
     */
    public TextChoiceFilterEditor() {
        setEditable(true);

        //add one 'empty' entry, to clear thh filter
        comboBoxModelList.add("");

        setModel(new DefaultComboBoxModel(comboBoxModelList));

        editor = new TextField((JTextField) (getEditor().getEditorComponent())) {
                @Override protected void newHistoricEntry(String historic) {
                    addToHistoric(historic);
                }
            };
        addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {

                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        editor.setText((String) e.getItem());
                    }
                }
            });
    }

    /**
     * Full constructor, where the filter position is {@link IFilterTextParser#NO_FILTER_POSITION}
     *
     * @see  TextChoiceFilterEditor#TextChoiceFilterEditor(IFilterTextParser, int)
     */
    public TextChoiceFilterEditor(IFilterTextParser parser) {
        this(parser, IFilterTextParser.NO_FILTER_POSITION, null);
    }

    /**
     * Full constructor. Note that the identifier can be {@link
     * IFilterTextParser#NO_FILTER_POSITION}, if no identifier is to be assumed
     *
     * @see  TextChoiceFilterEditor#setFilterPosition(int)
     */
    public TextChoiceFilterEditor(IFilterTextParser parser, int filterPosition) {
        this(parser, filterPosition, null);
    }

    /**
     * Extended constructor, setting up a set of prefixed choices.
     *
     * @param  choices  a list of Strings to be included directly in the combobox model, allowing
     *                  therefore to the user to select quickly predefined filters.
     *
     * @see    TextChoiceFilterEditor#TextChoiceFilterEditor(IFilterTextParser, int)
     * @see    TextChoiceFilterEditor#suggestChoices(String[])
     */
    public TextChoiceFilterEditor(IFilterTextParser parser, int filterPosition, String[] choices) {
        this();
        setTextParser(parser);
        setFilterPosition(filterPosition);

        if (choices != null)
            suggestChoices(choices);
    }

    /**
     * @see  ITableFilterTextBasedEditor#setTextParser(IFilterTextParser)
     */
    public void setTextParser(IFilterTextParser parser) {
        editor.setTextParser(parser);
    }

    /**
     * @see  ITableFilterTextBasedEditor#getTextParser()
     */
    public IFilterTextParser getTextParser() {
        return editor.getTextParser();
    }

    /**
     * <p>Sets the filter position associated to this editor.</p>
     *
     * <p>This is the filter position passed to the text parser; it corresponds, in the case of a
     * table, to the column to which this editor is associated.</p>
     *
     * <p>It is not mandatory to specify this position. In the case of a standalone editor, which
     * could be not associated to any specific column, this position can be given as or {@link
     * IFilterTextParser#NO_FILTER_POSITION}</p>
     *
     * @see  IFilterTextParser#parseText(String, int)
     */
    public void setFilterPosition(int identifier) {
        editor.setFilterPosition(identifier);
    }


    /**
     * Returns the filter position
     *
     * @see  TextChoiceFilterEditor#setFilterPosition(int)
     */
    public int getFilterPosition() {
        return editor.getFilterPosition();
    }

    /**
     * @see  ITableFilterEditor#getComponent()
     */
    public Component getComponent() {
        return this;
    }

    /**
     * <p>It reparses the text and propagates the filter to any observers.</p>
     *
     * @see  ITableFilterEditor#updateFilter()
     */
    public void updateFilter() {
        editor.updateFilter();
    }

    /**
     * <p>It sets the current filter text as an empty string, removing any previous entries from the
     * historic -from the associated popup menu-</p>
     *
     * <p>It does not remove the suggested choices</p>
     *
     * @see  ITableFilterEditor#updateFilter()
     */
    public void resetFilter() {
        int h = this.history;
        setHistoricLength(0);
        setHistoricLength(h);
        editor.setText("");
    }

    /**
     * @see  ITableFilterEditor#getFilterObservable()
     */
    public IFilterObservable getFilterObservable() {
        return editor;
    }

    /**
     * @see  ITableFilterTextBasedEditor#setErrorForeground(Color)
     */
    public void setErrorForeground(Color fg) {
        editor.setErrorForeground(fg);
    }

    /**
     * @see  ITableFilterTextBasedEditor#setErrorBackground(Color)
     */
    public void setErrorBackground(Color bg) {
        editor.setErrorBackground(bg);
    }


    /**
     * Returns the foreground color used to represent error conditions.
     */
    public Color getErrorForeground() {
        return editor.getErrorForeground();
    }

    /**
     * Returns the background color used to represent error conditions.
     */
    public Color getErrorBackground() {
        return editor.getErrorBackground();
    }

    /**
     * @see  ITableFilterTextBasedEditor#setFilterForeground(Color)
     */
    public void setFilterBackground(Color bg) {
    	editor.setBackground(bg);
    }
    
    /**
     * @see  ITableFilterTextBasedEditor#setFilterForeground(Color)
     */
    public void setFilterForeground(Color fg) {
    	editor.setForeground(fg);
    }


    @Override public void setBackground(Color bg) {
    	super.setBackground(bg);
    	this.backgroundColor=bg;
    }
    
    @Override public void setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            super.setEnabled(enabled);
            editor.setEnabled(enabled);
            super.setBackground(enabled? backgroundColor : null);
        }
    }

    /**
     * <p>Sets the historic length, that is, it controls the number of elements kept on the
     * contextual menu holding previous correctly parsed entries.</p>
     *
     * <p>If the length is smaller than the current number of entries, the oldest entries are
     * removed.</p>
     */
    public void setHistoricLength(int length) {
        this.history = length;

        while (comboBoxModelList.size() > (history + choices + 1)) {
            comboBoxModelList.removeElementAt(comboBoxModelList.size() - 1 - choices);
        }

        repaint();
    }


    /**
     * Returns the history length
     *
     * @see  TextChoiceFilterEditor#setHistoricLength(int)
     */
    public int getHistoricLength() {
        return history;
    }


    /**
     * <p>Adds an expression to the historic entries.</p>
     *
     * <p>If the number of expressions exceed the maximum set, the oldest expressions are
     * removed.</p>
     *
     * <p>This method is automatically invoked when the user enters a valid filter expression, but
     * can be invoked explicitly to provide initial values</p>
     */
    public void addToHistoric(String text) {

        if ((text.trim().length() > 0) && (history > 0) && !isChoice(text)) {

            if (!comboBoxModelList.remove(text)) {

                if (comboBoxModelList.size() > (history + choices)) {
                    comboBoxModelList.removeElementAt(comboBoxModelList.size() - 1 - choices);
                }
            }

            comboBoxModelList.add(1, text);
            repaint();
        }
    }

    /**
     * Detects whether a given expression is a predefined choice (choices are inserted always at the
     * end, after the last used entries)
     */
    private boolean isChoice(String text) {
        return comboBoxModelList.indexOf(text) >= (comboBoxModelList.size() - choices);
    }

    /**
     * <p>Sets entries that will be placed before the historic ones, always provided as options to
     * the user. These entries are not affected by normal operations.</p>
     *
     * <p>They should be correct expressions (that is, that can be correctly parsed by the installed
     * parser)</p>
     */
    public void suggestChoices(String... choice) {

        //remove previous choices
        while (comboBoxModelList.size() > (history + 1)) {
            comboBoxModelList.removeElementAt(comboBoxModelList.size() - 1);
        }

        choices = choice.length;

        for (String c : choice) {
            comboBoxModelList.add(c);
        }
    }
}
