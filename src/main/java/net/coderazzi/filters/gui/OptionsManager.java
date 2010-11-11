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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * <p>Helper class to handle options in the filters</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
class OptionsManager implements TableModelListener, FilterEditor.OptionsManager 
{	
    /** 
     * If true, filter editors will automatically extract 
     * their content from the table's content 
     **/
	private boolean autoOptions=FilterSettings.autoOptions;

	private int autoOptionColumns[]=new int[0];
	private Map<Integer, FilterEditor> autoEditors = 
		new TreeMap<Integer, FilterEditor>();
	private Set<FilterEditor> editors = new HashSet<FilterEditor>();
	private JTable table;
	private TableModel listenedModel;
	
	@Override
	public JTable getTable() {
		return table;
	}
	
	/** Returns true if the editor defines auto options */
	@Override
	public boolean isAutoOptions(FilterEditor editor){
		return autoEditors.containsValue(editor);
	}
	
	/**
	 * Enables/disables the auto options feature and sets the options
	 * for the given editor
	 */
	@Override
	public void setOptions(FilterEditor editor, boolean autoOptions){
		fillOptions(editor, autoOptions);
		if (!autoOptions){
			unsetAutoOptions(editor);
		}
	}
	
	/** Returns true if there are auto options defined at global level*/
	public boolean isAutoOptions(){
		return autoOptions;
	}
	
	/**
	 * Enables/disables the auto options feature at global level. It resets
	 * all filters.
	 */
	public void setAutoOptions(boolean autoOptions){
		this.autoOptions=autoOptions;
		for (FilterEditor editor : editors){
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
		editors.add(editor);
		if (fillOptions(editor, isAutoOptions() && 
				editor.getOptions().size()<=8)){
    		editor.setMaxHistory(0);			
		}
	}

	/** Removes an existing editor */
	public void removeFilterEditor(FilterEditor editor){
		unsetAutoOptions(editor);
		editors.remove(editor);
	}
	
	private void unsetAutoOptions(FilterEditor editor){
		if (null!=autoEditors.remove(editor.getFilterPosition())){
			resetOptionColumsn();
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
		resetOptionColumsn();
		this.table=table;
	}
	
	private void resetOptionColumsn(){
		boolean attached = autoOptionColumns.length>0;
		autoOptionColumns = new int[autoEditors.size()];
		int n = 0;
		for (int i : autoEditors.keySet()){
			autoOptionColumns[n++]=i;
		}
		if (table!=null){
			//Note: if the table changes its model, all editors are
			//removed and then recreated. This means that we need to
			// 'remember' the original table model to be able to free it
			if (attached && n==0){
				listenedModel.removeTableModelListener(this);
			} else if (!attached && n>0){
				listenedModel = table.getModel();
				listenedModel.addTableModelListener(this);
			}
		}
	}
	
	/**
	 * Fills the options in the given editor, if the associated class is
	 * a boolean or enum, returning then true<br>
	 * If that is not the case, and autoOptions is set to true, it adds
	 * options as extracted from the table model, in a dynamic mode.
	 * @param editor
	 * @param autoOptions
	 * @return
	 */
	private boolean fillOptions(FilterEditor editor, boolean autoOptions){
        Class<?> c = table.getModel().getColumnClass(editor.getFilterPosition());
        List<Object> options;
        if (c.equals(Boolean.class)){
        	options = new ArrayList<Object>(3);
        	options.add(true);
        	options.add(false);
        } else if (c.isEnum()){
        	Object[] enums = c.getEnumConstants();
        	options = new ArrayList<Object>(enums.length+1);
        	for (Object each : enums){
        		options.add(each);
        	}
        } else {
    		if (autoOptions){
    			autoEditors.put(editor.getFilterPosition(), editor);
    			resetOptionColumsn();
    			extractFilterContentsFromModel(editor);
    		} else {
    			editor.clearOptions();
    		}
        	return false;
        }
    	editor.setOptions(options);
    	editor.setEditable(false);
    	return true;
	}
	
	/** TableModelListener interface */
	@Override public void tableChanged(TableModelEvent e) {
		int r = e.getFirstRow();			
		if (r != TableModelEvent.HEADER_ROW && 
				e.getType()!=TableModelEvent.DELETE){				
			int c = e.getColumn(); 				
			if (c == TableModelEvent.ALL_COLUMNS){
				for (FilterEditor editor : autoEditors.values()){
					extendFilterContentsFromModel(editor, r, e.getLastRow());
				}
			} else {
				FilterEditor editor = autoEditors.get(c);
				if (editor!=null){					
					extendFilterContentsFromModel(editor, r, e.getLastRow());
				}
			}
		}
	}
	
    private void extractFilterContentsFromModel(FilterEditor editor) {
    	editor.clearOptions();
    	extendFilterContentsFromModel(editor, 0, table.getModel().getRowCount()-1);
    }
    
    private void extendFilterContentsFromModel(FilterEditor editor, 
    		                                   int firstRow, 
    		                                   int lastRow){
    	List<Object> all = new ArrayList<Object>();
        int column = editor.getFilterPosition();
        TableModel regModel = table.getModel();

        lastRow = Math.min(regModel.getRowCount() - 1, lastRow);

        while (lastRow >= firstRow) {
            all.add(regModel.getValueAt(firstRow++, column));
        }
        editor.addOptions(all);
    }
}

