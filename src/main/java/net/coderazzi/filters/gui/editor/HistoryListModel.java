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

package net.coderazzi.filters.gui.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;

import net.coderazzi.filters.gui.FilterSettings;


/**
 * List model to handle the history in the popup menu.<br>
 * When the user specifies a {@link ListCellRenderer}, 
 * history elements are considered non-text; this affects to the 
 * search algorithm to find the best matches 
 * {@link PopupComponent#selectBestMatch(Object, boolean)}
 */
class HistoryListModel extends AbstractListModel {
	private static final long serialVersionUID = -374115548677017807L;
	private List<Object> history = new ArrayList<Object>();
	/** history, converted to lower case. Null if content is not text-based */
	private List<Object> historyLC;
	private int maxHistory = FilterSettings.maxVisiblePopupRows;
	private boolean stringContent = true;
	private boolean ignoreCase;
	
	/** 
	 * Sets the flag to ignore case or be case sensitive<br>
	 * It affects to the algorithms to search for the best match on the content. 
	 */
	public void setIgnoreCase(boolean set){
		ignoreCase = set;
	}
	
	/**
	 * Specifies how to handle the content. If no renderer is used, the content 
	 * is text based, and converted to Strings 
	 * -and displayed and searched as strings-. 
	 */
	public boolean setStringContent(boolean set){
		if (stringContent!=set){
			stringContent = set;
			clear();
			return true;
		}
		return false;
	}

	public Object getElementAt(int index) {
		return history.get(index);
	}

	/** Adds an element, Returning true if the number of elements changes */
	public boolean add(Object st) {
		boolean ret = false;
		if (maxHistory > 0) {
			history.remove(st);
			history.add(0, st);
			int size = history.size();
			if (size > maxHistory) {
				history.remove(--size);
				fireContentsChanged(this, 0, maxHistory);
			} else {
				ret=true;
				fireIntervalAdded(this, 0, 0);
			}
			historyLC=null;
		}
		return ret;
	}
	
	public boolean isEmpty(){
		return history.isEmpty();
	}
	
	public void clear(){
		int size = history.size();
		if (size>0){
			history.clear();
			fireIntervalRemoved(this, 0, size);
			historyLC=null;
		}
	}

	public int getSize() {
		return history.size();
	}

	/** Sets the max history; returns true if the number of elements changes */
	public boolean setMaxHistory(int size) {
		maxHistory = size;
		int current=history.size();
		if (current>size){
			for (int i=current-1;i>=size;i--){
				history.remove(i);
			}
			fireContentsChanged(this, maxHistory, current);
			historyLC=null;
			return true;
		}
		return false;
	}

	public int getMaxHistory() {
		return maxHistory;
	}

	/** @see PopupComponent#selectBestMatch(Object, boolean) */
	public PopupComponent.Match getClosestMatch(Object hint, boolean exact) {
		if (stringContent && (hint instanceof String)) {
			List list = history;
			if (ignoreCase){
				if (historyLC==null){
					historyLC = new ArrayList<Object>(history.size()+1);
					for(Object o : history){
						if (o instanceof String){
							o = ((String)o).toLowerCase();
						}
						historyLC.add(o);
					}
				}
				list = historyLC;
				hint = ((String) hint).toLowerCase();
			}
			return findOnUnsortedContent(list, (String)hint, exact);
		}
		return new PopupComponent.Match(history.indexOf(hint));
	}
	
	/** 
	 * Method to find the best match on a given unsorted list 
	 * -search is case sensitive- 
	 **/
	private PopupComponent.Match findOnUnsortedContent(List list, 
			String strStart, boolean fullMatch) {
		PopupComponent.Match ret = new PopupComponent.Match();
		if (list.isEmpty()) {
			ret.index = -1;
		} else {
			ret.len = strStart.length();
			ret.index = list.indexOf(strStart);
			if (ret.index != -1) {
				ret.exact=true;
			} else {
				ret.index = 0;
				ret.exact = ret.len == 0;
				int originalLen = ret.len;
				while (ret.len > 0) {
					for (Object o : list) {
						if (o instanceof String){
							String os = (String) o;
							if (os.startsWith(strStart)) {
								ret.exact = os.length() == originalLen;
								return ret;
							}
						}
						++ret.index;
					}
					if (fullMatch) {
						ret.index = -1;
						break;
					}
					ret.index = 0;
					strStart = strStart.substring(0, --ret.len);
				}
			}
		}
		return ret;
	}
}
