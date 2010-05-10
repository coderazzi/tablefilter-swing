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

import com.byteslooser.filters.gui.ITableFilterEditor;

import java.awt.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;


/**
 * <p>Specialization of the {@link ChoiceFilterEditor} class that extracts the choices from the
 * contents of a table's column</p>
 *
 * @author  Luis M Pena - byteslooser@gmail.com
 */
public class TableChoiceFilterEditor extends ChoiceFilterEditor {

    /** Comparator of integers where higher numbers are selected first */
    private final static Comparator<Integer> inverseComparator = new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        };

    /** Default comparator between choices, using their string representation */
    private final static Comparator<Object> defaultComparator = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {

                if (o1 == null)
                    return (o2 == null) ? 0 : -1;

                return (o2 == null) ? 1 : o1.toString().compareTo(o2.toString());
            }
        };

    /** Max number of elements to show as choices */
    private int maxShow = Integer.MAX_VALUE;

    /** The comparator between objects, to know in which order to display them */
    private Comparator<Object> comparator;

    /** The associated table */
    JTable table;

    /**
     * Default constructor
     */
    public TableChoiceFilterEditor() {
    }

    /**
     * <p>Special constructor that provides no additional capabilities than a simpler {@link
     * ChoiceFilterEditor}, as the choices are prefixed and not obtained from the model.</p>
     *
     * <p>As a rule of thump, better use the {@link ChoiceFilterEditor} class than this class when
     * this constructor is invoked</p>
     */
    public TableChoiceFilterEditor(int filterPosition, Object... choices) {
        super(filterPosition, null, choices);
    }

    /**
     * <p>Creates a choice filter editor for the provided column in the model associated to the
     * specified table, where the options to choose are extracted from the model's column.</p>
     *
     * <p>Note that, once the editor is built, it will not track changes in the model, so the
     * displayed options are static. The user can call {@link
     * TableChoiceFilterEditor#updateFilter()} or {@link TableChoiceFilterEditor#resetFilter()} to
     * re-read the model</p>
     *
     * <p>The model column will be used as filter position.</p>
     */
    public TableChoiceFilterEditor(JTable table, int modelColumn) {
        setTable(table, modelColumn);
    }

    /**
     * Updates the choice contents, by re-reading the contents of the associated column in the table
     * model, and it propagates the current filter to any listeners
     *
     * @see  ITableFilterEditor#updateFilter()
     */
    @Override public void updateFilter() {
        if (this.table != null) {
            extractColumnContentsFromModel(getSelectedItem());
        }
        super.updateFilter();
    }

    /**
     * Updates the choice contents, by re-reading the contents of the associated column in the table
     * model, and selects the 'no filter' choice, that is, any current filtering is automatically
     * removed.
     *
     * @see  ITableFilterEditor#resetFilter()
     */
    @Override public void resetFilter() {

        if (this.table == null) {
            getModel().setSelectedItem(NO_FILTER);
        } else {
            extractColumnContentsFromModel(NO_FILTER);
        }
        filter.propagateFilterChange(false);
    }

    /**
     * <p>Sets as renderer for the editor a generic {@link TableCellRenderer}, as used by the {@link
     * JTable}</p>
     *
     * <p>This method allows reusing a renderer already written for a table as the editor's
     * renderer, but it has an important restriction: it only works if the renderer does not depend
     * on the cell coordinates</p>
     */
    public void setChoiceRenderer(final TableCellRenderer renderer) {
        setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {

                    if (value == NO_FILTER)
                        return super.getListCellRendererComponent(list, value, index, isSelected,
                                cellHasFocus);

                    return renderer.getTableCellRendererComponent(table, value, isSelected,
                            cellHasFocus, 1, filterPosition);
                }
            });
    }

    /**
     * Sets the comparator to sort elements in the combobox
     */
    public void setComparator(Comparator<Object> comparator) {
        this.comparator = comparator;
    }

    /**
     * <p>Sets the table and the filterPosition (the column in the associated table) to use on the
     * editor.</p>
     *
     * <p>It automatically reads the given column' contents to set the choices in the combobox</p>
     *
     * @see  ChoiceFilterEditor#setFilterPosition(int)
     */
    public void setTable(JTable table, int modelColumn) {

        this.table = table;
        setFilterPosition(modelColumn);
        extractColumnContentsFromModel(NO_FILTER);
    }

    /**
     * <p>Sets the default choice mode, that shows each different value in the table's column as a
     * choice</p>
     */
    public void setDefaultChoiceMode() {
        setFixedChoiceMode(Integer.MAX_VALUE);
    }

    /**
     * <p>Sets the maximum number of choices to display.</p>
     *
     * <p>The algorithm used is to display the more frequent choices.</p>
     */
    public void setFixedChoiceMode(int maxShow) {
        setFixedChoiceMode(null, maxShow);
    }

    /**
     * <p>Sets the maximum number of choices to display, and the object to select the choices not
     * shown.</p>
     *
     * <p>The algorithm used is to display the more frequent choices.</p>
     */
    public void setFixedChoiceMode(Object lessFrequentChoices, int maxShow) {
        this.maxShow = maxShow;
        otherChoices = lessFrequentChoices;
        extractColumnContentsFromModel(getSelectedItem());
    }

    /**
     * <p>Sets the elements to be used as choices, and the object to be used to get the elements not
     * covered by those choices(can be null)</p>
     */
    public void setFixedChoiceMode(Object labelForOtherChoices, Object... choices) {
        super.setChoices(labelForOtherChoices, choices);
    }

    /**
     * Sets exactly the choices to be displayed. This method is equivalent to {@link
     * TableChoiceFilterEditor#setFixedChoiceMode(Object, Object[])}
     */
    @Override public void setChoices(Object labelForOtherChoices, Object... choices) {
        table = null;
        setFixedChoiceMode(labelForOtherChoices, choices);
    }

    /**
     * Method to extract the filterPosition contents from the model
     */
    void extractColumnContentsFromModel(Object selected) {
        Map<Object, Integer> columnContents = new HashMap<Object, Integer>();
        Set<Object> modelContents = new HashSet<Object>();

        TableModel model = table.getModel();

        if (model != null) {

            if (model.getColumnCount() > filterPosition) {
                int row = model.getRowCount();

                while (row-- > 0) {
                    Object s = model.getValueAt(row, filterPosition);
                    Integer i = columnContents.get(s);

                    if (i == null)
                        i = new Integer(1);
                    else
                        i = new Integer(1 + i.intValue());

                    columnContents.put(s, i);
                }
            }

            if (maxShow >= columnContents.size()) {
                modelContents.addAll(columnContents.keySet());
            } else {
                TreeSet<Integer> values = new TreeSet<Integer>(inverseComparator);
                values.addAll(columnContents.values());

                for (Integer value : values) {

                    for (Entry<Object, Integer> e : columnContents.entrySet())
                        if (e.getValue() == value)
                            modelContents.add(e.getKey());

                    if (modelContents.size() >= maxShow)
                        break;
                }
            }
        }

        Object[] content = modelContents.toArray();

        try {
            Arrays.sort(content, comparator);
        } catch (Exception ex) {
            Arrays.sort(content, defaultComparator);
        }

        setChoiceModel((selected == null) ? NO_FILTER : selected, otherChoices, content);
    }

}
