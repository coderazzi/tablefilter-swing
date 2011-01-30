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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableRowSorter;

import net.coderazzi.filters.AndFilter;
import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.IFilterObserver;
import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * <p>FiltersHandler represents a {@link javax.swing.RowFilter} instance that 
 * can be attached to a {@link javax.swing.JTable} to compose dynamically the 
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
public class FiltersHandler extends AndFilter implements PropertyChangeListener
{

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

	/**  The autoOptions mode **/
	private AutoOptions autoOptions=FilterSettings.autoOptions;
	
    /** 
     * The class performing the autoSelection, and following sorter changes.<br>
     * Note that autoSelection has no relationship with autoOptions, it is
     * the feature to automatically select a row if the current filter filters
     * out all the rows but one. 
     **/
    private AutoSelector autoSelector = new AutoSelector();
    
	/** All the editors, mapped by their filter position */
    private Map<Integer, FilterEditor> editors=new HashMap<Integer, FilterEditor>();
	
    /** The associated table. */
    private JTable table;
	
    /** Instance to handle options (choices) on each FilterEditor */
	private OptionsHandler optionsHandler;
	
	/** The associated filter model */
	private IParserModel parserModel;

    /**
     * Only constructor
     */
    FiltersHandler() {
    	// create an observer instance to notify the associated table when there
        // are filter changes.
        addFilterObserver(new IFilterObserver() {
                @Override public void filterUpdated(IFilter obs) {
                    notifyUpdatedFilter();
                }
            });
        setAdaptiveOptions(FilterSettings.adaptiveOptions);
    }
    
    /**
     * Method to set the associated table. If the table had not defined its own
     * {@link javax.swing.RowSorter}, the default one is automatically created.
     */
    public void setTable(JTable table) {
    	optionsHandler.setInterrupted(true);
		JTable oldTable = this.table;
		this.table = table;
        autoSelector.replacedTable(oldTable, table);
    }

    /** Returns the associated table */
    public JTable getTable() {
        return table;
    }
    
    public void setParserModel(IParserModel parserModel){
    	if (parserModel!=null && parserModel!=this.parserModel){
    		if (this.parserModel!=null){
    			this.parserModel.removePropertyChangeListener(this);
    		}
    		this.parserModel=parserModel;
    		this.parserModel.addPropertyChangeListener(this);
        	enableNotifications(false);
        	for (FilterEditor editor : editors.values()){
        		editor.resetFilter();
        	}
        	enableNotifications(true);
    	}
    	this.parserModel = parserModel;
    }

    public IParserModel getParserModel(){
    	return parserModel;
    }
    
    /** {@link PropertyChangeListener} interface, for changes on {@link IParserModel} */
    @Override public void propertyChange(PropertyChangeEvent evt) {
    	Class target;
    	boolean formatChange=false;
    	if (evt.getPropertyName()==IParserModel.IGNORE_CASE_PROPERTY){
    		target=null;
    	} else {
    		if (evt.getPropertyName()==IParserModel.FORMAT_PROPERTY){
    			formatChange=true;
    		} else  if (evt.getPropertyName()!=IParserModel.COMPARATOR_PROPERTY){
    			return;
    		}
    		Object cl = evt.getNewValue();
    		if (cl instanceof Class){
    			target=(Class)cl;
    		}else{
    			return;
    		}
    	}
    	enableNotifications(false);
    	for (FilterEditor editor : editors.values()){
    		if (target==null){
    			editor.setIgnoreCase(parserModel.isIgnoreCase());
    		} else if (editor.getModelClass()==target){
    			if (formatChange){
    				editor.setFormat(parserModel.getFormat(target));
    			} else {
    				editor.setComparator(parserModel.getComparator(target));    				
    			}
    		}
    	}
    	enableNotifications(true);
    }
    
    /** Enables/Disables the filtering */
    @Override public void setEnabled(boolean enabled) {
    	enableNotifications(false);
    	super.setEnabled(enabled);
    	enableNotifications(true);
    }
    
	/** Sets/unsets the auto options flag */
    public void setAutoOptions(AutoOptions mode){
    	if (mode!=autoOptions){
    		enableNotifications(false);
			this.autoOptions=mode;
			for (FilterEditor editor : editors.values()){
				//after this call, the editor will request its options
				editor.setAutoOptions(mode);
			}
    		enableNotifications(true);
    	}
    }
    
	/** Returns the auto options mode */
	public AutoOptions getAutoOptions(){
		return autoOptions;
	}
	
    /** Sets the adaptive options mode */
    public void setAdaptiveOptions(boolean enableAdaptiveOptions) {
    	boolean reenable=false;
    	if (optionsHandler != null){
        	if (enableAdaptiveOptions==isAdaptiveOptions()){
        		return;
        	}
    		enableNotifications(false);
    		if (optionsHandler!=null){
    			optionsHandler.setInterrupted(true);
    		}
    		reenable=true;
    	}
		if (enableAdaptiveOptions){
			optionsHandler = new AdaptiveOptionsHandler(this);
		} else {
			optionsHandler = new NonAdaptiveOptionsHandler(this);    			
		}
		if (reenable){
			enableNotifications(true);    		
    	}
    }
    
    /** Returns the adaptive options mode */
    public boolean isAdaptiveOptions() {
        return optionsHandler instanceof AdaptiveOptionsHandler;
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
     * @see    FiltersHandler#setAutoSelection(boolean)
     */
    public boolean isAutoSelection() {
        return autoSelector.autoSelection;
    }

    @Override public void addFilter(IFilter... filtersToAdd) {
    	optionsHandler.filterOperation(true);
    	super.addFilter(filtersToAdd);
    	optionsHandler.filterOperation(false);
    }
    
    @Override public void removeFilter(IFilter... filtersToRemove) {
    	optionsHandler.filterOperation(true);
    	super.removeFilter(filtersToRemove);
    	optionsHandler.filterOperation(false);
    }
    
    /** Adds a new filter editor */
    public void addFilterEditor(FilterEditor editor){
    	super.addFilter(editor.getFilter());
    	editors.put(editor.getModelIndex(), editor);
    	editor.setAutoOptions(autoOptions);
    }
    
    /** Removes an existing editor */
    public void removeFilterEditor(FilterEditor editor){
    	super.removeFilter(editor.getFilter());
    	editors.remove(editor.getModelIndex());
    }
    
	/**
	 * Method invoked by the FilterEditor when its autoOptions mode OR 
	 * userOptions change; in return, it will set the proper options on
	 * the specified editor. 
	 */
	public void updatedEditorOptions(FilterEditor editor){
		if (editors.containsValue(editor) && isEnabled()){
			optionsHandler.editorUpdated(editor);
		}
    }
	
    @Override public void filterUpdated(IFilter filter) {
    	boolean wasEnabled=isEnabled();
    	boolean filterWasDisabled=isDisabled(filter);
    	optionsHandler.filterUpdated(filter);
    	super.filterUpdated(filter);
    	if (filterWasDisabled && filter.isEnabled()){
	    	optionsHandler.filterEnabled(filter);
    	} else if (wasEnabled && !isEnabled()){
    		optionsHandler.allFiltersDisabled();
    	}
    }
    
    /** Method to set/update the filtering */
    public void updateTableFilter() {
    	pendingNotifications = autoSelector.sorter == null;
    	if (!pendingNotifications){
            //To reapply the filtering, it is enough to invoke again setRowFilter.
    		RowFilter rf = isEnabled()? optionsHandler.getRowFilter() : null;
    		if (rf!=null || autoSelector.sorter.getRowFilter()!=null){
    			autoSelector.sorter.setRowFilter(rf);
    		}
    	}
    }
    
    public Collection<FilterEditor> getEditors(){
    	return editors.values();
    }
    
    public FilterEditor getEditor(int column){
    	return editors.get(column);
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
    public void enableNotifications(boolean enable) {
        sendNotifications += enable ? 1 : -1;
        if (enable){
	        if (sendNotifications == 0) {
	        	//adding/removing filter editors is not done as separate processes
	        	//when the user sets/changes the TableModel, all columns are
	        	//removed and then recreated.
	        	//At the beginning of the process, there are calls to 
	        	//enableNotifications(false)/(true) than only balance out when the
	        	//whole model is setup. In that moment, we build the 
	        	//AdaptiveOptionsSupport, if needed. 
	        	//We use the same mechanism whenever it would be needed to
	        	//recreate the adaptive support or because it could be more 
	        	//efficient doing so.
	        	if (optionsHandler.setInterrupted(false) || pendingNotifications){
	        		updateTableFilter();
	        	}
	        }
        } else if (optionsHandler.setInterrupted(true)){
        	updateTableFilter();
        }
    }
    
    /**
     * Internal method to send a notification to the observers, verifying 
     * first if the notifications are currently enabled.
     */
    void notifyUpdatedFilter() {
        if (sendNotifications < 0) {
            pendingNotifications = true;
        } else {
        	updateTableFilter();
        }
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
    	TableRowSorter sorter;
        
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

        @Override public void propertyChange(PropertyChangeEvent evt) {
            setSorter((JTable) evt.getSource());
        }

        private void setSorter(JTable table) {
            if (this.sorter != null) {
                this.sorter.removeRowSorterListener(this);
                this.sorter.setRowFilter(null);
                this.sorter=null;
            }
            TableRowSorter sorter;
            try{
            	sorter = table==null? null : (TableRowSorter) table.getRowSorter();
            } catch (ClassCastException ccex){
            	throw new RuntimeException("Invalid RowSorter on JTable: filter header requires a TableRowSorter class");
            }
            if (table!=null && (sorter==null || sorter.getModel()!=table.getModel())){
            	this.sorter=new TableRowSorter(table.getModel());
            	table.setRowSorter(this.sorter);
            }
            else {
            	this.sorter = sorter;
            	if (sorter != null) {
            		notifyUpdatedFilter();
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

        @Override public void run() {
            if ((sorter != null) && (sorter.getViewRowCount() == 1)) {
                getTable().getSelectionModel().setSelectionInterval(0, 0);
            }
        }

        @Override public void sorterChanged(RowSorterEvent e) {
            if ((e.getType() == RowSorterEvent.Type.SORTED) && 
            	(e.getSource().getViewRowCount() == 1)) {
                SwingUtilities.invokeLater(this);
            }
        }
    }
    
}