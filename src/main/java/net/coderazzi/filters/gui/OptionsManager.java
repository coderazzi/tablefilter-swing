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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * <p>Helper class to handle options in the filters</p>
 */
class OptionsManager implements TableModelListener, FilterEditor.OptionsManager 
{	
    /** 
     * If true, filter editors will automatically extract 
     * their content from the table's content 
     **/
	private AutoOptions autoOptions=FilterSettings.autoOptions;

	private Map<FilterEditor, AutoOptions> autoEditors =  new HashMap<FilterEditor, AutoOptions>();
	private Map<Integer, FilterEditor> editors = new HashMap<Integer, FilterEditor>();
	private JTable table;
	private TableModel listenedModel;
	
	@Override
	public JTable getTable() {
		return table;
	}
	
	/** Returns true if the editor defines auto options */
	@Override public AutoOptions getAutoOptions(FilterEditor editor){
		AutoOptions ret = autoEditors.get(editor);
		return ret==null? AutoOptions.DISABLED : ret;
	}
	
	/**
	 * Enables/disables the auto options feature and sets the options
	 * for the given editor
	 */
	@Override
	public void setOptions(FilterEditor editor, AutoOptions autoOptions){
		//this check is needed to avoid setting the options for the
		//same column multiple times when is created. The creation of
		//a filter editor is initially done, then the parser is set,
		//and both operations would trigger this operation
		if (editor==editors.get(editor.getFilterPosition())){
			autoOptions=fillOptions(editor, autoOptions);
			updateAutoEditors(editor, autoOptions);
		}
	}
	
	/** Returns the autoOptions mode*/
	public AutoOptions getAutoOptions(){
		return autoOptions;
	}
	
	/**
	 * Enables/disables the auto options feature at global level. It resets
	 * all filters.
	 */
	public void setAutoOptions(AutoOptions autoOptions){
		this.autoOptions=autoOptions;
		for (FilterEditor editor : editors.values()){			
			//it is probably not really needed to set it to null
			//a better solution would be verifying if the current content
			//matches any of the options
			editor.setContent(null);
			setOptions(editor, autoOptions);
		}
	}
	
	/**
	 * Informs of a new filter editor.<br>
	 * It will automatically populate its options if the associated class is
	 * boolean or enum, or autoOptions is defined
	 */
	public void addFilterEditor(FilterEditor editor){
		editors.put(editor.getFilterPosition(), editor);
		setOptions(editor, autoOptions);
	}

	/** Removes an existing editor */
	public void removeFilterEditor(FilterEditor editor){
		if (editor==editors.get(editor.getFilterPosition())){
			updateAutoEditors(editor, AutoOptions.DISABLED);
			editors.remove(editor.getFilterPosition());
		}
	}
	
	/** 
	 * Sets the table for the model.<br>
	 * It must be called before adding any editor.<br>
	 * I can be invoked with a null value to detach this instance from
	 * any listeners, etc.
	 */
	public void setTable(JTable table) {
		editors.clear();
		autoEditors.clear();
		if (listenedModel!=null){
			listenedModel.removeTableModelListener(this);
			listenedModel = null;
		}
		this.table=table;
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
				for (Entry<FilterEditor, AutoOptions> entry : autoEditors.entrySet()){
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
}

