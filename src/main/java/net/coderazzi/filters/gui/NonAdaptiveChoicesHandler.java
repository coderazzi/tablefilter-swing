package net.coderazzi.filters.gui;

import java.util.HashSet;
import java.util.Set;

import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.gui.editor.FilterEditor;

class NonAdaptiveChoicesHandler extends ChoicesHandler{
	
	private boolean interrupted=true;
	
	public NonAdaptiveChoicesHandler(FiltersHandler handler) {
		super(handler);
	}
	
	@Override public boolean setInterrupted(boolean interrupted) {
		if (this.interrupted!=interrupted){
			this.interrupted=interrupted;
			if (interrupted){
				setEnableTableModelEvents(false);
			} else {
	    		for (FilterEditor editor : handler.getEditors()){
	    			editorUpdated(editor);
	    		}				
			}
		}
		return !interrupted; //filter should be updated
	}
	
    @Override public RowFilter getRowFilter() {
    	return handler;
    }
    
    @Override public void filterUpdated(IFilter filter) {
    	//nothing to do
    }
    
    @Override public void filterOperation(boolean start) {
    	handler.enableNotifications(!start);  	
    }
    
    @Override public void filterEnabled(IFilter filter){
    	for (FilterEditor editor : handler.getEditors()){
    		if (editor.getFilter()==filter){
    			initEditorOptions(editor);
    			break;
    		}
    	}
    }
    
    @Override public void allFiltersDisabled(){
    	setEnableTableModelEvents(false);    			    	
    }
    
    @Override public void editorUpdated(FilterEditor editor) {
		if (editor.isEnabled()){
			initEditorOptions(editor);
		}
	}
    
	@Override public void tableUpdated(TableModel model, int eventType, int firstRow, int lastRow, int column) {
		if (column != TableModelEvent.ALL_COLUMNS) {
			//a change in ONE column is always handled as an update
			//(every update is handled by re-extracting the options
			FilterEditor editor = handler.getEditor(column);
			if (editor != null && editor.isEnabled()) {
				setOptionsFromModel(editor, model);
			}
		} else {
			boolean handled=false;
			for (FilterEditor editor : handler.getEditors()) {
				if (editor.isEnabled() && AutoChoices.ENABLED == editor.getAutoOptions()) {
					//insert events can be handled by adding the
					//new model's values.
					//updates/deletes require reparsing the whole
					//table to obtain again the available options
					if (eventType == TableModelEvent.INSERT) {
						editor.addOptions(modelExtract(editor, model,
								firstRow, lastRow, new HashSet<Object>()));
					} else {
						setOptionsFromModel(editor, model);
					}
					handled=true;
				}
			}
			if (!handled){
				//lazy mode: if the instance was listening to table model
				//events and all editors became AutoOptions.DISABLED, the
				//instance will keep listening for table model events, until
				//it discovers that it does not need it.
				setEnableTableModelEvents(false);
			}
		}
	}		
	
	/**
	 * Initializes the options in the given editor.<br>
	 * It can update the mode of the editor, from ENABLED to ENUMS (in case
	 * of enumerations), and from ENUMS to DISABLED (for no enumerations)
	 */
	private void initEditorOptions(FilterEditor editor) {
		AutoChoices autoOptions = editor.getAutoOptions();
		if (autoOptions == AutoChoices.DISABLED) {
			editor.setOptions(editor.getCustomChoices());
		} else {
			TableModel model = handler.getTable().getModel();
			Class<?> c = model.getColumnClass(editor.getModelIndex());
			boolean asEnum = c.equals(Boolean.class) || c.isEnum();
			if (asEnum && autoOptions != AutoChoices.ENUMS) {
				editor.setAutoOptions(AutoChoices.ENUMS);
			} else if (!asEnum && autoOptions == AutoChoices.ENUMS) {
				editor.setAutoOptions(AutoChoices.DISABLED);
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
			} else {
				setOptionsFromModel(editor, model);
				setEnableTableModelEvents(true);
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
		int column = editor.getModelIndex();
		for (; lastRow >= firstRow; firstRow++) {
			fill.add(model.getValueAt(firstRow, column));
		}
		return fill;
	}		
}