/**
 * Author:  Luis M Pena  ( byteslooser@gmail.com )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  byteslooser@gmail.com
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

package com.byteslooser.filters.gui.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.byteslooser.filters.IFilterObservable;
import com.byteslooser.filters.gui.ITableFilterEditor;
import com.byteslooser.filters.gui.ITableFilterTextBasedEditor;
import com.byteslooser.filters.parser.IFilterTextParser;


/**
 * Table filter editor based on text parsing, represented by a {@link javax.swing.JTextField} with a
 * contextual menu to hold previous parsing expressions.
 *
 * @author  Luis M Pena - byteslooser@gmail.com
 */
public class TextFilterEditor extends JTextField implements ITableFilterTextBasedEditor {

    /**
     * Default number of elements to keep on the contextual menu holding previous correctly parsed
     * entries.
     */
    public static final int DEFAULT_HISTORY = 3;

    /**
     * The number of elements to keep on the contextual menu holding previous correctly parsed
     * entries.
     */
    private int history = DEFAULT_HISTORY;

    /** The TextField instance that handles all the work */
    TextField editor;

    JPopupMenu popupMenu;


    /**
     * Default constructor. It is yet needed to set, at least, the text parser.
     */
    public TextFilterEditor() {
        editor = new TextField(this) {
                @Override protected void newHistoricEntry(String historic) {
                    addToHistoric(historic);
                }
            };
        createPopupMenu();
    }

    /**
     * Full constructor, where the filter position is {@link IFilterTextParser#NO_FILTER_POSITION}
     *
     * @see  TextFilterEditor#TextFilterEditor(IFilterTextParser, int)
     */
    public TextFilterEditor(IFilterTextParser parser) {
        this(parser, IFilterTextParser.NO_FILTER_POSITION);
    }

    /**
     * Full constructor. Note that the identifier can be {@link
     * IFilterTextParser#NO_FILTER_POSITION}, if no identifier is to be assumed
     *
     * @see  TextFilterEditor#setFilterPosition(int)
     */
    public TextFilterEditor(IFilterTextParser parser, int filterPosition) {
        this();
        setTextParser(parser);
        setFilterPosition(filterPosition);
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
     * @see  TextFilterEditor#setFilterPosition(int)
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
     * @see  ITableFilterEditor#updateFilter()
     */
    public void resetFilter() {
        editor.setText("");

        int oldHistory = history;
        setHistoricLength(0);
        setHistoricLength(oldHistory);
        editor.setText("");
    }

    /**
     * @see  ITableFilterEditor#getFilterObservable()
     */
    public IFilterObservable getFilterObservable() {
        return editor;
    }

    @Override public void setEnabled(boolean enabled) {
        if (enabled != isEnabled()) {
            super.setEnabled(enabled);
            if (editor != null)
                editor.setEnabled(enabled);
        }
    }

    public void setBackgroundColor(Color bg) {
        super.setBackground(bg);
    }

    public void setForegroundColor(Color fg) {
        super.setForeground(fg);
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
     * Creates the popup menu
     */
    private void createPopupMenu() {
        popupMenu = new JPopupMenu();
        addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {

                    if (e.isPopupTrigger() && (popupMenu.getComponentCount() > 0)) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
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

        while (popupMenu.getComponentCount() > length) {
            Component c = popupMenu.getComponent(length);

            if (c instanceof MenuItem) {
                popupMenu.remove(c);
            } else {
                break;
            }
        }
    }

    /**
     * Returns the history length
     *
     * @see  TextFilterEditor#setHistoricLength(int)
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

        //the implementation does not assume that all the entries in the menu
        //are historic entries (in case that the menu is eventually exposed)
        if ((history > 0) && (text.trim().length() > 0)) {
            int length = history - 1;

            for (int j = 0; j < popupMenu.getComponentCount(); j++) {
                Component c = popupMenu.getComponent(j);

                if (c instanceof MenuItem) {

                    if (length == 0) {
                        popupMenu.remove(c);

                        break;
                    }

                    if (((MenuItem) c).getText().equals(text)) {
                        popupMenu.remove(c);
                        popupMenu.insert(c, 0);

                        return;
                    }

                    --length;
                } else {
                    break;
                }
            }

            popupMenu.insert(new MenuItem(text), 0);
        }
    }

    /**
     * Class to represent each MenuItem
     */
    private final class MenuItem extends JMenuItem {
        MenuItem(String text) {
            super(text);
            setFont(getFont().deriveFont(Font.ITALIC));
            addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        editor.setText(getText());
                    }
                });
        }
    }
}
