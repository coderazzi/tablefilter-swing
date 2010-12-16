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

package net.coderazzi.filters.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultRowSorter;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.coderazzi.filters.AndFilter;
import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.IFilterObserver;
import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * <p>TableFilter represents a {@link javax.swing.RowFilter} instance that can 
 * be attached to a {@link javax.swing.JTable} to compose dynamically the 
 * outcome of one or more filter editors. As such, it is a dynamic filter, 
 * which updates the table when there are changes in any of the composed 
 * sub filters.</p>
 *
 * <p>Users have, after version 3.2, no direct use for this class</p>
 *
 * <p>In Java 6, a filter is automatically associated to a 
 * {@link javax.swing.RowSorter}, so {@link javax.swing.JTable} instances with 
 * a TableFilter must define their own {@link javax.swing.RowSorter}. 
 * Being this not the case, the TableFilter will automatically set the 
 * default {@link javax.swing.RowSorter} in that table. That is, tables with 
 * a TableFilter will always have sorting enabled.</p>
 *
 * <p>The {@link javax.swing.RowSorter} interface does not support filtering 
 * capabilities, which are only enabled via the 
 * {@link javax.swing.DefaultRowSorter} class. If the registered table uses any
 * sorter that does not subclass the {@link javax.swing.DefaultRowSorter} class, 
 * the TableFilter will perform <b>no filtering at all</b>.</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class TableFilter extends AndFilter implements TableModelListener, FilterEditor.OptionsManager
{

    /** The associated table, if any. */
    JTable table;

    /**
     * sendNotifications is used internally as a semaphore to disable 
     * temporarily notifications to the filter observers. Notifications 
     * are only sent to the observers when this variable is non negative.
     */
    int sendNotifications = 0;

    /**
     * pendingNotifications keeps track of notifications to be sent to the 
     * observers, but were discarded because the variable sendNotifications 
     * was negative.
     */
    private boolean pendingNotifications;

    /** 
     * The class performing the autoSelection, and following sorter changes.<br>
     * Note that autoSelection has no relationship with autoOptions, it is
     * the feature to automatically select a row if the current filter filters
     * out all the rows but one. 
     **/
    private AutoSelector autoSelector = new AutoSelector();

    /** 
     * The listened model. Note: even if table<>null, this model can be null if
     * there is not need to listen for model changes (no auto options)
     **/
	private TableModel listenedModel;

	/**  The autoOptions mode **/
	private AutoOptions autoOptions=FilterSettings.autoOptions;

	/** editors with not DISABLED autoOptions */
	private Map<FilterEditor, AutoOptions> autoEditors =  new HashMap<FilterEditor, AutoOptions>();
	
	/** All the editors, mapped by their filter position */
	private Map<Integer, FilterEditor> editors = new HashMap<Integer, FilterEditor>();
	
    /**
     * Default constructor
     */
    public TableFilter() {

        // create an observer instance to notify the associated table when there
        // are filter changes.
        addFilterObserver(new IFilterObserver() {
                @Override public void filterUpdated(IFilter obs) {
                    notifyUpdatedFilter(false);
                }
            });
    }

    /**
     * Method to set the associated table. If the table had not defined its own
     * {@link javax.swing.RowSorter}, the default one is automatically created.
     */
    public void setTable(JTable table) {
        JTable oldTable = this.table;
        this.table = table;
        autoSelector.replacedTable(oldTable, table);
		editors.clear();
		autoEditors.clear();
		if (listenedModel!=null){
			listenedModel.removeTableModelListener(this);
			listenedModel = null;
		}
    }

    /** Returns the associated table */
    @Override public JTable getTable() {
        return table;
    }
    
    /** Adds a new filter editor */
    public void addFilterEditor(FilterEditor editor) {
    	super.addFilter(editor.getFilter());
		editors.put(editor.getFilterPosition(), editor);
		setOptions(editor, autoOptions);
    }

    /** Removes an existing editor */
    public void removeFilterEditor(FilterEditor editor) {
    	super.removeFilter(editor.getFilter());
		if (editor==editors.get(editor.getFilterPosition())){
			updateAutoEditors(editor, AutoOptions.DISABLED);
			editors.remove(editor.getFilterPosition());
		}
    }

    /**
     * <p>Temporarily enable/disable notifications to the observers, including 
     * the registered {@link javax.swing.JTable}.</p>
     *
     * <p>Multiple calls to this method can be issued, but the caller must 
     * ensure that there are as many calls with true parameter as with false 
     * parameter, as the notifications are only re-enabled when the zero 
     * balance is reached.</p>
     */
    public boolean enableNotifications(boolean enable) {
        sendNotifications += enable ? 1 : -1;
        if (enable && (sendNotifications == 0) && pendingNotifications) {
            notifyUpdatedFilter(false);
        }

        return sendNotifications >= 0;
    }
    
	/** Returns editor' auto options mode*/
    @Override public AutoOptions getAutoOptions(FilterEditor editor) {
		AutoOptions ret = autoEditors.get(editor);
		return ret==null? AutoOptions.DISABLED : ret;
    }
    
    @Override public void setOptions(FilterEditor editor, AutoOptions autoOptions) {
		//this check is needed to avoid setting the options for the
		//same column multiple times when is created. The creation of
		//a filter editor is initially done, then the parser is set,
		//and both operations would trigger this operation
		if (editor==editors.get(editor.getFilterPosition())){
			autoOptions=fillOptions(editor, autoOptions);
			updateAutoEditors(editor, autoOptions);
		}
    }
    
	/** Sets/unsets the auto options flag */
    public void setAutoOptions(AutoOptions mode){
    	if (mode!=autoOptions){
	        enableNotifications(false);
			this.autoOptions=mode;
			for (FilterEditor editor : editors.values()){			
				//it is probably not really needed to set it to null
				//a better solution would be verifying if the current content
				//matches any of the options
				editor.setContent(null);
				setOptions(editor, mode);
			}
	        enableNotifications(true);
    	}
    }
    
	/** Returns the auto options mode */
	public AutoOptions getAutoOptions(){
		return autoOptions;
	}
	
    /**
     * <p>Sets the autoselection mode</p>
     *
     * <p>if autoSelection is true, if there is only one possible 
     * row to select on the table, it will be selected.
     */
    public void setAutoSelection(boolean enable) {
        autoSelector.setAutoSelection(enable);
    }

    /**
     * Returns the autoselection mode
     *
     * @see    TableFilter#setAutoSelection(boolean)
     */
    public boolean isAutoSelection() {
        return autoSelector.autoSelection;
    }

    /**
     * Internal method to send a notification to the observers, verifying 
     * first if the notifications are currently enabled.
     */
    void notifyUpdatedFilter(boolean forced) {
        if (forced || (sendNotifications < 0)) {
            pendingNotifications = true;

            return;
        }
        pendingNotifications = sendFilterUpdateNotification();
    }

    /**
     * Internal method to send without further checks a notification 
     * to the observers.
     */
    private boolean sendFilterUpdateNotification() {
        if (autoSelector.sorter == null) {
            return true;
        }
        //To reapply the filtering, it is enough to invoke again setRowFilter. 
        //Alternatively, it could just be invoked sort()
        autoSelector.sorter.setRowFilter(this);
        return false;
    }
    
	private void updateAutoEditors(FilterEditor editor, AutoOptions newMode){
		if (newMode==AutoOptions.DISABLED){
			autoEditors.remove(editor);
			if (listenedModel!=null && !requiresListener()){
				listenedModel.removeTableModelListener(this);
				listenedModel = null;				
			}
		} else {
			autoEditors.put(editor, newMode);
			if (listenedModel==null && table!=null && requiresListener(newMode)){
				listenedModel = table.getModel();
				listenedModel.addTableModelListener(this);				
			}
		}
	}
	
	private boolean requiresListener(){
		for (AutoOptions mode : autoEditors.values()){
			if (requiresListener(mode)){
				return true;
			}			
		}
		return false;
	}
	
	private boolean requiresListener(AutoOptions mode){
		return mode==AutoOptions.EXACT || mode==AutoOptions.EXTENDED;
	}
	
	/**
	 * Fills the options in the given editor, using the provided AutoOptions
	 * mode<br>
	 * It is returned the real options mode set (it only changes for
	 * enumerations and booleans, where setting EXTENDED is equivalent to BASIC)
	 */
	private AutoOptions fillOptions(FilterEditor editor, AutoOptions autoOptions){
        List<Object> options;
        if (autoOptions==AutoOptions.DISABLED){
        	options=Collections.emptyList();
        } else if (autoOptions==AutoOptions.EXACT){
        	options = getModelContents(editor);
        } else {
            Class<?> c = table.getModel().getColumnClass(editor.getFilterPosition());
            if (c.equals(Boolean.class)){
            	autoOptions=AutoOptions.BASIC;
            	options = new ArrayList<Object>(3);
            	options.add(true);
            	options.add(false);
            } else if (c.isEnum()){
            	autoOptions=AutoOptions.BASIC;
            	Object[] enums = c.getEnumConstants();
            	options = new ArrayList<Object>(enums.length+1);
            	for (Object each : enums){
            		options.add(each);
            	}
            } else if (autoOptions==AutoOptions.BASIC){
            	options=Collections.emptyList();
            	autoOptions = AutoOptions.DISABLED;
            } else {
            	options = getModelContents(editor);
            }        	
        }
        editor.setOptions(options);
        if (autoOptions==AutoOptions.BASIC){
        	editor.setEditable(false);
        	if (options.size()<=8){
        		editor.setMaxHistory(0);
        	}
        }
    	return autoOptions;
	}
	
	/** TableModelListener interface */
	@Override public void tableChanged(TableModelEvent e) {
		int firstRow = e.getFirstRow();		
		if (firstRow != TableModelEvent.HEADER_ROW){
			int lastRow = e.getLastRow();
			int c = e.getColumn();
			if (c == TableModelEvent.ALL_COLUMNS){
				for (Map.Entry<FilterEditor, AutoOptions> entry : autoEditors.entrySet()){
					updateColumn(entry.getKey(), entry.getValue(), firstRow, lastRow, e.getType());
				}
			} else {
				FilterEditor editor = editors.get(c);
				if (editor!=null){
					updateColumn(editor, autoEditors.get(editor), firstRow, lastRow, e.getType());
				}
			}
		}
	}
	
	private void updateColumn(FilterEditor editor, AutoOptions mode, int firstRow, int lastRow, int eventType){
		if (mode!=AutoOptions.BASIC){
			if (eventType==TableModelEvent.INSERT || (eventType==TableModelEvent.UPDATE && mode==AutoOptions.EXTENDED)){
				editor.addOptions(getModelContents(editor, firstRow, lastRow));
			} else if (mode==AutoOptions.EXACT){
				editor.setOptions(getModelContents(editor));
			}
		}
	}
	
    private List<Object> getModelContents(FilterEditor editor){
    	return getModelContents(editor, 0, table.getModel().getRowCount()-1);
    }
    
    private List<Object> getModelContents(FilterEditor editor, 
    		                                   int firstRow, 
    		                                   int lastRow){
    	List<Object> all = new ArrayList<Object>();
        int column = editor.getFilterPosition();
        TableModel regModel = table.getModel();

        lastRow = Math.min(regModel.getRowCount() - 1, lastRow);

        while (lastRow >= firstRow) {
            all.add(regModel.getValueAt(firstRow++, column));
        }
        return all;
    }

    /**
     * <p>Class performing the auto selection.</p> 
     * <p>Note that it depends only on the model, and not on the filters.<br>
     * If the model contains one single row, it will be automatically selected, 
     * even if the filters are empty.</p>
     */
    class AutoSelector implements RowSorterListener, Runnable, 
                                  PropertyChangeListener {

        /** The associated sorter, if any. */
        DefaultRowSorter<?, ?> sorter;
        
        /** Autoselection mode * */
        boolean autoSelection = FilterSettings.autoSelection;
        
		public void replacedTable(JTable oldTable,
                                  JTable newTable) {
			String EVENTS[]={"rowSorter", "model"};
        	for (String e : EVENTS){
                if (oldTable != null) {
            		oldTable.removePropertyChangeListener(e, this);
            	}
                if (newTable != null) {
            		newTable.addPropertyChangeListener(e, this);
                }
            }
            setSorter(newTable);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setSorter((JTable) evt.getSource());
        }

        private void setSorter(JTable table) {
            if (this.sorter != null) {
                this.sorter.removeRowSorterListener(this);
                this.sorter.setRowFilter(null);
                this.sorter=null;
            }
            DefaultRowSorter sorter;
            try{
            	sorter = table==null? null : (DefaultRowSorter) table.getRowSorter();
            } catch (ClassCastException ccex){
            	throw new RuntimeException("Invalid RowSorter on JTable: filter header requires a DefaultRowSorter class");
            }
            if (table!=null && (sorter==null || sorter.getModel()!=table.getModel())){
            	table.setRowSorter(new TableRowSorter(table.getModel()));
            }
            else {
            	this.sorter = sorter;
            	if (sorter != null) {
	                if (sendNotifications >= 0) {
	                    sorter.setRowFilter(TableFilter.this);
	                }
	                if (autoSelection) {
	                    sorter.addRowSorterListener(this);
	                }
            	}
            }
        }

        public void setAutoSelection(boolean enable) {
            if ((autoSelection != enable) && (sorter != null)) {
                if (enable) {
                    sorter.addRowSorterListener(this);
                } else {
                    sorter.removeRowSorterListener(this);
                }
            }
            autoSelection = enable;
        }

        @Override
        public void run() {
            if ((sorter != null) && (sorter.getViewRowCount() == 1)) {
                table.getSelectionModel().setSelectionInterval(0, 0);
            }
        }

        @Override
        public void sorterChanged(RowSorterEvent e) {
            if ((e.getType() == RowSorterEvent.Type.SORTED) && 
            	(e.getSource().getViewRowCount() == 1)) {
                SwingUtilities.invokeLater(this);
            }
        }
    }

}