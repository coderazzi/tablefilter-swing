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
	private Object lastAdded;
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
	
	public Comparator<String> getStringComparator(){
		return stringComparator;
	}

	@Override public Object getElementAt(int index) {
		return history.get(index);
	}

	/** Adds an element, Returning true if the number of elements changes */
	public boolean add(Object st) {
		//never add the passed element (which is now selected on the
		//editor). We will added when the next element is passed
		boolean ret = false;
		boolean removed = history.remove(st);
		if (maxHistory > 0 && lastAdded!=null && !lastAdded.equals(st)) {
			history.add(0, lastAdded);
			int size = history.size();
			if (size > maxHistory) {
				history.remove(--size);
				removed=true;
			} else {
				ret=true;
				if (!removed){
					fireIntervalAdded(this, 0, 0);
				}
			}
		}
		if (removed){
			fireContentsChanged(this, 0, history.size());	
			ret=true;
		}
		lastAdded = st;
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
		lastAdded=null;
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
			return PopupComponent.Match.findOnUnsortedContent(history, 
					history.size(), stringComparator, (String)hint, exact); 			
		}
		return new PopupComponent.Match(history.indexOf(hint));
	}
	
}
