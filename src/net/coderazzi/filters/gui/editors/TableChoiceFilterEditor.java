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

import java.awt.Component;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import net.coderazzi.filters.gui.ITableFilterEditor;


/**
 * <p>Specialization of the {@link ChoiceFilterEditor} class that extracts the choices from the
 * contents of a table's column</p>
 * 
 * <p>Since version 1.5.0, the contents are automatically updated when the table changes</p>
 *
 * @author  Luis M Pena - dr.lu@coderazzi.net
 */
public class TableChoiceFilterEditor extends ChoiceFilterEditor {

	private static final long serialVersionUID = -7535026954584041799L;

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
        
    private final TableModelListener tableModelListener = new TableModelListener() {
		
		@Override
		public void tableChanged(TableModelEvent e) {
			int r = e.getFirstRow();
			
			if (r == TableModelEvent.HEADER_ROW){				
				extractFilterContentsFromModel(getSelectedItem());
			} else if (maxShow == Integer.MAX_VALUE && e.getType()!=TableModelEvent.DELETE){
				
				int c = e.getColumn(); 
				
				if (c == TableModelEvent.ALL_COLUMNS || c == filterPosition){
					
					int last = e.getLastRow();
					
					if (r==0 && last >= table.getRowCount()){
						extractFilterContentsFromModel(getSelectedItem());
					} else {
						extendFilterContentsFromModel(r, last);
					}
				}
			}
		}
	};

    /** Max number of elements to show as choices */
    private int maxShow = Integer.MAX_VALUE;

    /** The elements shown in the filter */
    private Set<Object> filterOptions;

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
        	extractFilterContentsFromModel(getSelectedItem());
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
            extractFilterContentsFromModel(NO_FILTER);
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

        		private static final long serialVersionUID = -5990815893475331934L;

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
     * Sets the comparator to sort elements in the combobox.
     * If a table is attached to the editor, it must be attached before invoking 
     * this method.
     */
    public void setComparator(Comparator<Object> comparator) {
    	TreeSet<Object> filterOptions = new TreeSet<Object>(comparator);
    	filterOptions.addAll(this.filterOptions);
        this.filterOptions = filterOptions;
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

    	if (this.table!=null){
    		this.table.getModel().removeTableModelListener(tableModelListener);
    	}
        this.table = table;
        setFilterPosition(modelColumn);
        filterOptions = null;
        extractFilterContentsFromModel(NO_FILTER);
        this.table.getModel().addTableModelListener(tableModelListener);
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
     * <p>Note that, if the table contents change, the filter is not automatically updated.</p>
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
        extractFilterContentsFromModel(getSelectedItem());
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
    void extractFilterContentsFromModel(Object selected) {
        Map<Object, Integer> columnContents = new HashMap<Object, Integer>();

        TableModel model = table.getModel();

        if (model != null) {
        	
        	if (filterOptions == null){
        		if ((model.getColumnCount() > filterPosition) &&
        				Comparable.class.isAssignableFrom(model.getColumnClass(filterPosition))){
        			filterOptions = new TreeSet<Object>();
        		}
        		else {
        			filterOptions = new TreeSet<Object>(defaultComparator);
        		}
        	}

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
                filterOptions.addAll(columnContents.keySet());
            } else {
                TreeSet<Integer> values = new TreeSet<Integer>(inverseComparator);
                values.addAll(columnContents.values());

                for (Integer value : values) {

                    for (Entry<Object, Integer> e : columnContents.entrySet())
                        if (e.getValue() == value)
                        	filterOptions.add(e.getKey());

                    if (filterOptions.size() >= maxShow)
                        break;
                }
            }
        }

        Object[] content = filterOptions.toArray();

        setChoiceModel((selected == null) ? NO_FILTER : selected, otherChoices, content);
    }
    
    /**
     * <p>Adds the contents of the table at the given rows in the filter.</p>
     * <p>Note that it does not check for the maximum number of elements to show</p>
     */
    void extendFilterContentsFromModel(int firstRow, int lastRow){
    	
        Set<Object> previousContents = new HashSet<Object>(filterOptions);

        TableModel tableModel = table.getModel();

        while (lastRow >= firstRow) {
        	filterOptions.add(tableModel.getValueAt(lastRow--, filterPosition));
        }
        
        if (filterOptions.size() > previousContents.size()){
        
	        DefaultComboBoxModel model = (DefaultComboBoxModel) getModel();
	        
	        int position = 1; //first element should be NO_FILTER
	        
	        for (Object c : filterOptions){
	        	if (!previousContents.contains(c)){
	        		model.insertElementAt(c, position);
	        	}
	        	++position;
	        }	        
        }
    }
    
    @Override
    public void setModel(ComboBoxModel aModel) {
    	super.setModel(aModel);
    	//if the user sets a model, no reason to continue listening for events
    	//note that this happens also if the user provides a list of choices
    	if (this.table!=null && !(aModel instanceof SpecificDefaultComboBoxModel)){
    		this.table.getModel().removeTableModelListener(tableModelListener);
    	}
    }

}
