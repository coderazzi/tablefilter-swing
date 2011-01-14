package net.coderazzi.filters.gui;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.IFilterTextParser;
import net.coderazzi.filters.gui.editor.CustomChoice;
import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * Helper class, only needed when the filter header has adaptive options support
 * It holds, for each row and filter (editor or user defined), 
 * a bit defining whether it is filtered in or out<br>
 */
class AdaptiveOptionsSupport extends RowFilter{

	/** A RowInfo for each row on the table model */
    private ArrayList<RowInfo> rows;
    /** A single instance to check the filters of every row/column*/
    private RowEntry rowEntry;
    /** 
     * An EditorHandle per editor (table model's column)<br>
     * The order is not important -it changes continuously while performing
     * updates
     **/ 
    private EditorHandle[] editorHandles;
    /** 
     * Each of the defined filters. <br>
     * Elsewhere, it is kept as a Set, but here, it is needed to associate
     * a column to each specific filter, so an array is chosen<br>
     * The first N filters correspond to the N filters of each editor, sorted
     * by column order.
     **/ 
    private RowInfo.Filter[] filters;

    public AdaptiveOptionsSupport(TableModel model,
                  FilterEditor[] editors,
                  Set<IFilter> allFilters) {
    	//note that the allFilters set will be modified
        int modelColumns = model.getColumnCount();
        this.rows = new ArrayList<RowInfo>(model.getRowCount()+1);
        this.filters=new RowInfo.Filter[allFilters.size()];
        this.editorHandles = new EditorHandle[editors.length];
        for (int i=0; i<modelColumns; i++){
        	FilterEditor editor=editors[i];
        	int column=editor.getModelPosition();
        	this.editorHandles[i] = new EditorHandle(editor, model);
            IFilter filter = editor.getFilter();
            allFilters.remove(filter);
            filters[column]=new RowInfo.Filter(filter, column);
        }
        for (IFilter filter : allFilters) {
            filters[modelColumns]=new RowInfo.Filter(filter, modelColumns);
            modelColumns++;
        }
        rowEntry = new RowEntry(model, editors);
        rowsAdded(0, model.getRowCount()-1);
    }

    /** Handles an table model event */
    public void tableChanged(int event, int firstRow, int lastRow, int column) {
    	if (column!=TableModelEvent.ALL_COLUMNS){
    		rowsUpdated(firstRow, lastRow, column);
    	} else if (event==TableModelEvent.UPDATE){
    		rowsUpdated(firstRow, lastRow, TableModelEvent.ALL_COLUMNS);
    	} else if (event==TableModelEvent.INSERT){
    		rowsAdded(firstRow, lastRow);
    	} else if (event==TableModelEvent.DELETE){
    		rowsDeleted(firstRow, lastRow);
		}
	}
    
    /** Handles a table model event after some rows are added */
    private void rowsAdded(int firstRow,
                           int lastRow) {
    	boolean added=false;
        rows.ensureCapacity(rows.size() + lastRow - firstRow + 1);
        for (int r =firstRow; r <= lastRow; r++) {
            RowInfo row = new RowInfo(filters.length);
            rows.add(r, row);
            rowEntry.row = r;
            boolean rowAdded=true;
            for (RowInfo.Filter filter : filters){
                if (!filter.include(rowEntry)) {
                	filter.set(row, false);
                	rowAdded=false;
                }
            }
            added=added||rowAdded;
        }
        if (added){
        	extractOptions(editorHandles.length, firstRow, lastRow);
        }
    }

    /** Handles a table model event after some rows are updated on 1 column */
    private void rowsUpdated(int firstRow,
                             int lastRow,
                             int column) {
		//if all rows being updated where filtered out, the event
		//can be handled as rows being added
    	boolean doAdd=true;
    	RowInfo.Filter filter=column==TableModelEvent.ALL_COLUMNS? null : filters[column];
        while(firstRow <= lastRow) {
            RowInfo row = rows.get(firstRow);
            if (doAdd && row.is()){
            	doAdd=false;
            }
            rowEntry.row = firstRow++;
            if (filter==null){
            	for (RowInfo.Filter f : filters){
            		f.set(row, f.include(rowEntry));
            	}
            } else {
            	filter.set(row, filter.include(rowEntry));            	
            }
        }
        if (!doAdd){ //just reread all the model
        	firstRow=0;
        	lastRow=-1;
        }
        extractOptions(editorHandles.length, firstRow, lastRow);
    }

    /** Handles a table model event after some rows are deleted*/
    private void rowsDeleted(int firstRow, int lastRow) {
    	//check first if the given rows were filtered out
    	//if any row was not filtered out, it is needed to read the whole model
    	boolean changed=false;
    	List<RowInfo> sublist = rows.subList(firstRow, lastRow+1);
    	for (RowInfo r : sublist){
    		if (r.is()){
    			changed=true;
    			break;
    		}
    	}
    	sublist.clear();
    	if (changed){
    		extractOptions(editorHandles.length, 0, -1);
    	}
    }

    /** Handles a change on a filter */
    public void update(IFilter iFilter) {
    	update(iFilter, false);
    }
    
    /** Handles a change on a filter */
    private void update(IFilter iFilter, boolean updateAssociatedEditor) {
    	boolean changed=false;
    	RowInfo.Filter filter = getFilter(iFilter);
        rowEntry.row=0;
        for (RowInfo ri : rows) {
        	changed = filter.set(ri, iFilter.include(rowEntry)) || changed;
            rowEntry.row++;
        }
        if (changed || updateAssociatedEditor){
        	int width = editorHandles.length;
            if (changed){
            	if (!updateAssociatedEditor && filter.column < width){
            		//update all editors except the associated editor,
            		//so move it to the end
            		switchHandle(getEditorHandle(filter.column), --width);
            	}
            } else{
            	//update only the associated editor, move it at the beginning
        		switchHandle(getEditorHandle(filter.column), 0);
        		width=1;
            }
    		extractOptions(width, 0, -1);        	
        }
    }
    
    /** Reports an update on the properties of an editor */
    public void editorUpdated(FilterEditor fe) {
    	int handle=getEditorHandle(fe.getModelPosition());
    	rowEntry.updatedEditor(editorHandles[handle]);
    	update(fe.getFilter(), true);
    }

    /** Returns the filter with the given {@link IFilter} */
    private RowInfo.Filter getFilter(IFilter filter) {
    	for (RowInfo.Filter f : filters){
    		if (filter==f.filter){
    			return f;
    		}
    	}
        return null;
    }

    /** 
     * Handles all the rows between firstRow and lastRow, updating the
     * options of the first handles in the editorHandles instance' variable.<br>
     * That is, the order of the variables in the editorHandlers variable is
     * modified, so that only the first handles are updated
     * @param lastRow can be -1 to represent the whole model
     **/
    private void extractOptions(int handles, int firstRow, int lastRow) {
    	int rows=rowEntry.getModelRowCount()-1;
    	if (lastRow==-1){
    		lastRow=rows;
    	}
    	boolean fullMode=firstRow==0 && lastRow==rows;
    	int check = handles;
    	for (int i = 0; i<check;){
    		if (editorHandles[i].startIteration(fullMode)){
    			//if startIteration returns true, this editor will require
    			//no additional iteration (move it to the end)
    			switchHandle(i, --check);
    		} else {
    			++i;
    		}
    	}
    	if (check>0){
    		iterateRows(check, firstRow, lastRow);
    	}
        while(handles-->0){
        	editorHandles[handles].iterationCompleted(fullMode);
    	}
    }
    
    /** handle all the rows in [firstRow, lastRow) */
    private void iterateRows(int handles, int firstRow, int lastRow) {
        for (; firstRow <= lastRow; firstRow++) {
        	rowEntry.row=firstRow;
        	RowInfo row = rows.get(firstRow); 
        	for (int i = 0; i < handles; ){
        		EditorHandle handle=editorHandles[i++];
        		if (filters[handle.column].is(row)){
	            	if (handle.handleRow(rowEntry)){
	        			//if handleRow returns true, this editor will require
	        			//no additional iteration (move it to the end)
	            		//if no handles remain, just return
	            		switchHandle(--i, --handles);
	            		if (handles==0){
	            			return;
	            		}
	            	}
	            }
        	}
        }
    }
    
    /** Moves the handle at the given position to the target */
    private void switchHandle(int source, int target){
    	if (target!=source){
	    	EditorHandle move = editorHandles[target];
	    	editorHandles[target]=editorHandles[source];
	    	editorHandles[source]=move;
    	}
    }
    
    /** Returns the EditorHandle with the given column */
    private int getEditorHandle(int column){
		int len=editorHandles.length;
		while (len-->0){
			if (editorHandles[len].column==column){
				return len;
			}
		}
    	return len;
    }
    
    @Override public boolean include(RowFilter.Entry entry) {
        return rows.get((Integer) entry.getIdentifier()).is();
    }
    
    public boolean include(int row){
        return rows.get(row).is();    	
    }
    

    /**
     * Helper class to handle an editor. It is very associated to the
     * algorithm used in {@link AdaptiveOptionsSupport#extractOptions(int, int, int)}
     */
    static class EditorHandle{
    	/** the model position of the associated editor */
    	int column;
    	/** The associated FilterEditor*/ 
    	private FilterEditor editor;
    	/** The maximum number of options the editor can have (enums) */
    	private int maxOptions;
    	/** Temporal variable for maxOptions, inside an iteration */
    	private int maxIterationOptions; //temporal variable for maxOptions
    	/** The options defined for the editor, with the associated filter */
    	private Map<CustomChoice, RowFilter> customChoices;
    	/** On an iteration, the choices not yet added */
    	private Map<CustomChoice, RowFilter> missingChoices;
    	/** The options that will be set on the editor */
    	private Set options = new HashSet();
    	
    	public EditorHandle(FilterEditor editor, TableModel model){
    		this.editor=editor;
    		this.column=editor.getModelPosition();
    		init(model);
    	}
    	
    	/** Updates the formatter associated to this editor */
    	public void updateFormatter(TableModel model, Format[] formatters){
        	formatters[column]=editor.getFormat();
        	init(model);
    		
    	}
    	/** Initializes the member's variables */ 
    	private void init(TableModel model){
    		Set<CustomChoice> options = editor.getCustomChoices();
    		IFilterTextParser parser = editor.getTextParser();
	        Class<?> c = model.getColumnClass(column);
	        if (c.equals(Boolean.class)){
	        	maxOptions=2;
	        } else {
		        Object o[]=c.getEnumConstants();
		        maxOptions = o==null? Integer.MAX_VALUE : o.length;
	        }
    		if (!options.isEmpty()){
    			customChoices = new HashMap<CustomChoice, RowFilter>();
    			for (CustomChoice cc : options){
    				customChoices.put(cc, cc.getFilter(parser, column));
    			}
    			if (maxOptions!=Integer.MAX_VALUE){
    				maxOptions+=customChoices.size();
    			}
    		}    		
    	}
    	
    	/** 
    	 * Starts an iteration in {@link AdaptiveOptionsSupport#extractOptions(int, int, int)}
    	 * @param fullMode set to true if the outcome of the iteration is to set
    	 *  options to the editor, or false if the outcome is to add options.
    	 * @return true if the handle requires no further iteration steps
    	 */
    	public boolean startIteration(boolean fullMode){
    		options.clear();
    		if (fullMode){
	    		missingChoices=customChoices==null? Collections.EMPTY_MAP : new HashMap<CustomChoice, RowFilter>(customChoices);    		
	    		maxIterationOptions=maxOptions;
    		} else {
        		maxIterationOptions=maxOptions - editor.getOptions().size();
    		}
    		return maxIterationOptions==0;
    	}
    	
    	/** 
    	 * Handles a given row during the iteration  
    	 * @param entry
    	 * @return true if the handle requires no further iteration steps
    	 */
    	public boolean handleRow(RowEntry entry){
    		if (!missingChoices.isEmpty()){
    			Iterator<Map.Entry<CustomChoice, RowFilter>> it = missingChoices.entrySet().iterator();
    			while (it.hasNext()){
    				Map.Entry<CustomChoice, RowFilter> o = it.next();
    				if (o.getValue().include(entry)){
    					options.add(o.getKey());
    					it.remove();
    				}
    			}
    		}
    		if (AutoOptions.DISABLED!=editor.getAutoOptions()){
    			options.add(entry.getValue(column));
    		}
    		return maxIterationOptions==options.size();
    	}
    	
    	/**  Final step on the iteration process, updating the editor' options */
    	public void iterationCompleted(boolean fullMode){
    		if (fullMode){
    			editor.setOptions(options);
    		} else {
    			editor.addOptions(options);
    		}
    	}
    }
    
   /**
     * Class to hold the filter information on a single row.<br>
     * This information is a bit per column, defining whether the
     * column is filtered out or not 
     */
    static class RowInfo {
        static final byte SET = (byte) 255;
        byte[] info;

        RowInfo(int columns) {
            int length = 1 + (columns >> 3);
            this.info = new byte[length];
            while (length-- > 0)
                this.info[length] = SET;
        }

        /** returns true if all the bits are set to 1 */
        public boolean is() {
            int length = info.length;
            while (length-- > 0) {
                if (info[length] != SET) {
                    return false;
                }
            }
            return true;
        }

        /** Defines a column in the RowInfo, associated to a filter */
    	static class Filter {
    		private int col, bit;
    		int column;
    		IFilter filter;

    		Filter(IFilter filter, int column) {
    			this.column=column;
    			this.filter = filter;
    			col = column >> 3;
    			bit = 1 << (column & 7);
    		}

    		public boolean include(RowFilter.Entry rowEntry){
    			return filter.include(rowEntry);
    		}
    		
    		public boolean set(RowInfo row, boolean set) {
            	byte info[]=row.info;
    			byte now = info[col];
    			if (set) {
    				info[col] |= bit;
    			} else {
    				info[col] &= (SET ^ bit);
    			}
    			return now != info[col];
    		}

            /**
             * returns true if all the bits in the row, with the possible
             * exception of THIS column, are set to 1
             */
            public boolean is(RowInfo row) {
            	byte info[]=row.info;
                boolean now = 0 != (info[col] & bit);
                set(row, true);
                boolean ret = row.is();
                set(row, now);
                return ret;
            }
    	}
//        @Override
//        public String toString() {
//            StringBuffer sb = new StringBuffer();
//            for (int i = 0; i < (info.length * 8); i++) {
//                if (sb.length() > 0)
//                    sb.append('-');
//    			int col = i >> 3;
//    			int bit = 1 << (i & 7);
//    			sb.append((info[col] & bit)==0? 0 : 1);
//            }
//            return sb.toString();
//        }
    }

    /**
     * Basic RowFilter.Entry instance, used internally to handle the RowFilter 
     * default filtering
     */
    static class RowEntry extends RowFilter.Entry {
        private TableModel model;
        private int count;
        private Format[] formatters;
        public int row;

        public RowEntry(TableModel model,
                     FilterEditor[] editors) {
            this.model = model;
            this.count = model.getColumnCount();
            int len = editors.length;
            formatters = new Format[len];
            while (len-- > 0) {
                formatters[len] = editors[len].getFormat();
            }
        }
        
        public int getModelRowCount(){
        	return model.getRowCount();
        }
        
        public void updatedEditor(EditorHandle editorHandle){
        	editorHandle.updateFormatter(model, formatters);
        }

        @Override public Object getIdentifier() {
            return row;
        }

        @Override public Object getModel() {
            return model;
        }

        @Override public Object getValue(int index) {
            return model.getValueAt(row, index);
        }

        @Override public int getValueCount() {
            return count;
        }

        @Override public String getStringValue(int index) {
            return formatters[index].format(getValue(index));
        }
    }


}