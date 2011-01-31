package net.coderazzi.filters.gui;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.gui.editor.FilterEditor;

/** 
 * Interface implemented by the classes that handle the options on each
 * {@link FilterEditor}
 */
abstract class ChoicesHandler implements TableModelListener {		
	
	private TableModel listenedModel;
	protected FiltersHandler handler;
    
    protected ChoicesHandler(FiltersHandler handler) {
    	this.handler = handler;
	}    
        
	/** Returns the {@link RowFilter} associated to this handler */
    public abstract RowFilter getRowFilter();
    
    /** 
     * Sets/unsets the handler on interrupt mode<br>
     * On interrupt mode, the associated {@link FiltersHandler} is likely to
     * send many update events, which shouldn't be treated (if possible) 
     */
    public abstract boolean setInterrupted(boolean interrupted);

    /** Reports a {@link FilterEditor} update*/
    public abstract void editorUpdated(FilterEditor editor);

    /** Reports the beginning or end of {@link IFilter} add/remove operations*/
    public abstract void filterOperation(boolean start);

    /** Reports a {@link IFilter} update*/
    public abstract void filterUpdated(IFilter filter);

    /** Call triggered after a filter becomes enabled */
    public abstract void filterEnabled(IFilter filter);

    /** Call triggered after all filters become disabled */
    public abstract void allFiltersDisabled();
    
    /** Reports a table update*/
	protected abstract void tableUpdated(TableModel model, int eventType, int firstRow, int lastRow, int column);
	
	@Override public void tableChanged(TableModelEvent e) {
		int firstRow = e.getFirstRow();
		if (firstRow != TableModelEvent.HEADER_ROW) {
			TableModel model = (TableModel) e.getSource();
			int lastRow = Math.min(model.getRowCount() - 1, e.getLastRow());
			tableUpdated(model, e.getType(), firstRow, lastRow, e.getColumn());
		} 
	}

    /** Sets whether to send table model events to the {@link ChoicesHandler}*/ 
    protected void setEnableTableModelEvents(boolean set){
    	if (set){
    		JTable table=handler.getTable();
    		if (table != null) {
    			if (listenedModel != null) {
    				if (listenedModel == table.getModel()) {
    					return;
    				}
    				setEnableTableModelEvents(false);
    			}
    			listenedModel = table.getModel();
    			listenedModel.addTableModelListener(this);
    		}    		
    	} else if (listenedModel != null) {
			listenedModel.removeTableModelListener(this);
			listenedModel = null;
		} 
    }

}