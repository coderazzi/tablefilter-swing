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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.RowFilter;
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
public class FiltersHandler extends AndFilter implements TableModelListener 
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
    
	/** Instance to support adaptive options */
    private AdaptiveOptionsSupport adaptiveSupport;
    
	/** All the editors, mapped by their filter position */
    private Map<Integer, FilterEditor> editors=new HashMap<Integer, FilterEditor>();
	
    /** The associated table. */
    private JTable table;
	
	/** If any model is being listened, this variable holds it*/
	private TableModel listenedModel;

	/** Set true when adaptive options are enabled */
	boolean adaptiveOptions = FilterSettings.adaptiveOptions;

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
    }
    
    /**
     * Method to set the associated table. If the table had not defined its own
     * {@link javax.swing.RowSorter}, the default one is automatically created.
     */
    public void setTable(JTable table) {
		detachTableListener();
		JTable oldTable = this.table;
		this.table = table;
        autoSelector.replacedTable(oldTable, table);
    }

    /** Returns the associated table */
    public JTable getTable() {
        return table;
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
    	if (enableAdaptiveOptions!=isAdaptiveOptions()){
    		adaptiveOptions=enableAdaptiveOptions;
    		if (isEnabled()){
    			//if is enabled, enableNotifications(true) creates the
    			//adaptiveOptionsSupport
    			//Otherwise, it is needed to init the options of each editor
	    		enableNotifications(false);
	    		for (FilterEditor editor : editors.values()){
	    			updated(editor);
	    		}
	    		enableNotifications(true);
    		}
    	}
    }
    
    /** Returns the adaptive options mode */
    public boolean isAdaptiveOptions() {
        return adaptiveOptions;
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
    	enableNotifications(false);
    	super.addFilter(filtersToAdd);
    	enableNotifications(true);
    }
    
    @Override public void removeFilter(IFilter... filtersToRemove) {
    	enableNotifications(false);
    	super.removeFilter(filtersToRemove);
    	enableNotifications(true);
    }
    
    /** Adds a new filter editor */
    public void addFilterEditor(FilterEditor editor){
    	super.addFilter(editor.getFilter());
    	editors.put(editor.getModelPosition(), editor);
    	editor.setAutoOptions(autoOptions);
    }
    
    /** Removes an existing editor */
    public void removeFilterEditor(FilterEditor editor){
    	super.removeFilter(editor.getFilter());
    	editors.remove(editor.getModelPosition());
    }
    
	/**
	 * Method invoked by the FilterEditor when its autoOptions mode OR 
	 * userOptions change; in return, it will set the proper options on
	 * the specified editor. 
	 */
	public void updatedEditorOptions(FilterEditor editor){
		if (editors.containsValue(editor) && isEnabled()){
			updated(editor);
		}
    }
	
    @Override public void filterUpdated(IFilter filter) {
    	boolean wasEnabled=isEnabled();
    	boolean filterWasDisabled=isDisabled(filter);
    	if (adaptiveSupport!=null){
    		adaptiveSupport.update(filter);
    	}
    	super.filterUpdated(filter);
    	if (filter.isEnabled()){
    		if (filterWasDisabled){
    			if (isAdaptiveOptions()){
    				if (adaptiveSupport==null){
    					ensureAdaptiveOptionsSupport();
    				} else {
    					adaptiveSupport.initOptions(filter);
    				}
    			} else {
    				FilterEditor editor = getEditor(filter);
    				if (editor!=null){
    					initEditorOptions(editor);
    				}
    			}    			
    		}
    	} else if (wasEnabled && !isEnabled()){
			removeAdaptiveOptionsSupport();
			detachTableListener();    		
    	}
    }
    
    private FilterEditor getEditor(IFilter filter){
    	for (FilterEditor editor : editors.values()){
    		if (editor.getFilter()==filter){
    			return editor;
    		}
    	}
    	return null;
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
	        	if (!createAdaptiveOptionsSupport() && pendingNotifications){
	        		updateRowFilter();
	        	} 
	        }
        } else {
        	removeAdaptiveOptionsSupport();
        }
        return sendNotifications >= 0;
    }
    
    private void ensureAdaptiveOptionsSupport(){
    	if (sendNotifications==0){
    		createAdaptiveOptionsSupport();
    	}
    }
    
    private void removeAdaptiveOptionsSupport(){
    	if (adaptiveSupport!=null){
    		adaptiveSupport=null;
    		updateRowFilter();
    	}
    }

    private boolean createAdaptiveOptionsSupport(){
    	if (isAdaptiveOptions() && table!=null && isEnabled()){
    		Collection<FilterEditor> eds = editors.values();
	    	adaptiveSupport = new AdaptiveOptionsSupport(table.getModel(),
	    				eds.toArray(new FilterEditor[eds.size()]),
	    				getFilters());
	    	attachTableListener();
			updateRowFilter();
			return true;
    	}
    	return false;
    }

    /**
     * Internal method to send a notification to the observers, verifying 
     * first if the notifications are currently enabled.
     */
    void notifyUpdatedFilter() {
        if (sendNotifications < 0) {
            pendingNotifications = true;
        } else {
        	updateRowFilter();
        }
    }
    
    /** Internal method to set/update the filtering */
    private void updateRowFilter() {
    	pendingNotifications = autoSelector.sorter == null;
    	if (!pendingNotifications){
            //To reapply the filtering, it is enough to invoke again setRowFilter.
    		RowFilter filter = adaptiveSupport;
    		if (filter==null && isEnabled() && !isAdaptiveOptions()){
    			filter=this;
    		}
            autoSelector.sorter.setRowFilter(filter);
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


    
	/** 
	 * Handles an update on the editor properties, requiring a 
	 * reinitialization of its options  
	 */
	private void updated(FilterEditor editor) {
		if (isAdaptiveOptions()) {
			if (adaptiveSupport != null) {
				adaptiveSupport.editorUpdated(editor);
			}
		} else if (editor.isEnabled()){
			initEditorOptions(editor);
		}
	}
	
	/** Detachs the helper from listening to table events*/
	private void detachTableListener() {
		if (listenedModel != null) {
			System.out.println("Detaching table model listener");
			listenedModel.removeTableModelListener(this);
			listenedModel = null;
		}
	}

	/** 
	 * attachs the instance to listen to changes on the current table<br>
	 * It can be called multiple times, on the same or different models.
	 */
	private void attachTableListener() {
		if (table != null) {
			if (listenedModel != null) {
				if (listenedModel == table.getModel()) {
					return;
				}
				detachTableListener();
			}
			System.out.println("Attaching table model listener");
			listenedModel = table.getModel();
			listenedModel.addTableModelListener(this);
		}
	}

	@Override public void tableChanged(TableModelEvent e) {
		int firstRow = e.getFirstRow();
		if (firstRow != TableModelEvent.HEADER_ROW) {
			TableModel model = (TableModel) e.getSource();
			int lastRow = Math.min(model.getRowCount() - 1, e.getLastRow());
			int column = e.getColumn();
			if (isAdaptiveOptions()){
				if (adaptiveSupport!=null){
					adaptiveSupport.tableChanged(e.getType(), firstRow, lastRow, column);
				}
			} else {
				if (column != TableModelEvent.ALL_COLUMNS) {
					//a change in ONE column is always handled as an update
					//(every update is handled by re-extracting the options
					FilterEditor editor = editors.get(column);
					if (editor != null && editor.isEnabled()) {
						setOptionsFromModel(editor, model);
					}
				} else {
					boolean handled=false;
					for (FilterEditor editor : editors.values()) {
						if (editor.isEnabled() && AutoOptions.ENABLED == editor.getAutoOptions()) {
							//insert events can be handled by adding the
							//new model's values.
							//updates/deletes require reparsing the whole
							//table to obtain again the available options
							if (e.getType() == TableModelEvent.INSERT) {
								editor.addOptions(modelExtract(editor, model,
										firstRow, lastRow, new HashSet<Object>()));
							} else {
								setOptionsFromModel(editor, model);
							}
							handled=true;
						}
					}
					if (!handled){
						detachTableListener(); //nothing to do, just detach
					}
				}					
			}
		} 
	}

	/**
	 * Initializes the options in the given editor.<br>
	 * It can update the mode of the editor, from ENABLED to ENUMS (in case
	 * of enumerations), and from ENUMS to DISABLED (for no enumerations)
	 */
	private void initEditorOptions(FilterEditor editor) {
		AutoOptions autoOptions = editor.getAutoOptions();
		if (autoOptions == AutoOptions.DISABLED) {
			editor.setOptions(editor.getCustomChoices());
		} else {
			TableModel model = table.getModel();
			Class<?> c = model.getColumnClass(editor.getModelPosition());
			boolean asEnum = c.equals(Boolean.class) || c.isEnum();
			if (asEnum && autoOptions != AutoOptions.ENUMS) {
				editor.setAutoOptions(AutoOptions.ENUMS);
			} else if (!asEnum && autoOptions == AutoOptions.ENUMS) {
				editor.setAutoOptions(AutoOptions.DISABLED);
			} else if (asEnum) {
				Set options = editor.getCustomChoices();
				if (c.equals(Boolean.class)) {
					options.add(true);
					options.add(false);
				} else {
					for (Object each : c.getEnumConstants()) {
						options.add(each);
					}
				}
				editor.setOptions(options);
				editor.setEditable(false);
				if (options.size() <= 8) {
					editor.setMaxHistory(0);
				}
			} else {
				setOptionsFromModel(editor, model);
				attachTableListener();
			}
		}
	}

	/** Sets the content for the given editor from the model's values */
	private void setOptionsFromModel(FilterEditor editor, TableModel model) {
		editor.setOptions(modelExtract(editor, model,
				0, model.getRowCount() - 1, editor.getCustomChoices()));
	}

	/** 
	 * Extract content from the given range of rows in the model, adding
	 * the results to the provided Set, which is then returned 
	 */
	private Set modelExtract(FilterEditor editor, TableModel model, 
			int firstRow, int lastRow, Set fill) {
		int column = editor.getModelPosition();
		for (; lastRow >= firstRow; firstRow++) {
			fill.add(model.getValueAt(firstRow, column));
		}
		return fill;
	}
    
}