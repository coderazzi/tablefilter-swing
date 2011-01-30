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
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;

import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.FilterSettings;


/**
 * List model to handle the history in the popup menu.<br>
 * When the user specifies a {@link ListCellRenderer}, 
 * history elements are considered non-text; this affects to the 
 * search algorithm to find the best matches 
 * {@link PopupComponent#selectBestMatch(Object, boolean)}<br>
 * Otherwise, content is always pure strings (@link {@link CustomChoice} are
 * not inserted into the history)
 */
class HistoryListModel extends AbstractListModel {
	private static final long serialVersionUID = -374115548677017807L;
	private List<Object> history = new ArrayList<Object>();
	private Comparator stringComparator;
	private int maxHistory = FilterSettings.maxVisiblePopupRows;
	
	/**
	 * Specifies how to handle the content. If there is a string comparator,
	 * content is handled as strings and it is possible to look for
	 * best matches; otherwise, it is treated as abstract objects, matching
	 * is done by identity.  
	 */
	public void setStringContent(Comparator stringComparator){
		if (this.stringComparator!=stringComparator){
			this.stringComparator = stringComparator;
			clear();
		}
	}

	@Override public Object getElementAt(int index) {
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
		}
	}

	@Override public int getSize() {
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
			return true;
		}
		return false;
	}

	public int getMaxHistory() {
		return maxHistory;
	}

	/** @see PopupComponent#selectBestMatch(Object, boolean) */
	public PopupComponent.Match getClosestMatch(Object hint, boolean exact) {
		if (stringComparator!=null && (hint instanceof String)) {			
			return findOnUnsortedContent((String)hint, exact);
		}
		return new PopupComponent.Match(history.indexOf(hint));
	}
	
	/** 
	 * Method to find the best match on a given unsorted list 
	 * -search is case sensitive- 
	 **/
	private PopupComponent.Match findOnUnsortedContent(String strStart, boolean fullMatch) {
		PopupComponent.Match ret = new PopupComponent.Match();
		if (history.isEmpty()) {
			ret.index = -1;
		} else {
			int strLen = strStart.length();
			if (strLen > 0){
				ret.index = history.indexOf(strStart);
				if (ret.index != -1) {
					ret.len = strLen;
				} else {
					int i = history.size();
					while (--i>0){
						Object o = history.get(i);
						if (o instanceof String){
							int match = getMatchingLength(strStart, (String) o);
							if (match>ret.len){
								ret.index=i;
								ret.len=match;
								if (match==strLen){
									break;
								}
							}
						}
					}
				}
			}
			if (ret.len==strLen){
				ret.exact=true;
			} else if (fullMatch){
				ret.index=-1;
				ret.len=0;
			}
		}
		return ret;
	}

	/** Returns the number of characters matching between two strings */
	private int getMatchingLength(String a, String b){
		int max = Math.min(a.length(), b.length());
		for (int i=0; i<max; i++){
			if (0!=stringComparator.compare(a.substring(i, i+1), b.substring(i, i+1))){
				return i;
			}
		}
		return max;
	}
	
}
