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

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;

import net.coderazzi.filters.gui.FilterSettings;


/**
 * List model to handle the options in the popup menu.<br>
 * When the user specifies a {@link ListCellRenderer}, options are 
 * considered non-text; otherwise, content is converted, if needed, 
 * to Strings, and sorted.<br>
 * This is needed to always show the popup's matches in sequential order. 
 */
public class OptionsListModel extends AbstractListModel {

	private static final long serialVersionUID = 3523952153693100563L;
	private List content;
	private Format formatter;
	private boolean useFormatter;
	private boolean ignoreCase;
	private int customChoices;
	private Class associatedClass;
	private Comparator comparator;
	private Comparator userComparator;
	
	public OptionsListModel(Class associatedClass) {
		this.content = new ArrayList();
		this.associatedClass = associatedClass;
		this.useFormatter = associatedClass==String.class;
		fixComparator();
	}
	
	@Override public int getSize() {
		return content.size();
	}

	@Override public Object getElementAt(int i) {
		return content.get(i);
	}

	/** Returns the class associated to the list model */
	public Class getAssociatedClass(){
		return associatedClass;
	}

	/**
	 * Specifies how to handle the content. If no renderer is used, 
	 * the content is text based, and converted to Strings 
	 * -and displayed and searched as strings-. 
	 */
	public void setStringContent(boolean set) {
		if (useFormatter!=set){
			useFormatter=set;
			fixComparator();
			clearContent();
		} 
	}

	/** Specifies the format used to convert Objects to Strings */
	public void setFormat(Format format) {
		if (format != this.formatter && String.class!=associatedClass){
			this.formatter = format;
			if (useFormatter){
				clearContent();
			}
		}
	}
	
	/** Specifies the comparator to sort the content on the list */
	public void setRendererComparator(Comparator comparator) {
		if (userComparator!=comparator){
			userComparator = comparator;
			fixComparator();
			clearContent();
		}
	}
	
	/** Returns the renderer comparator, if defined */ 
	public Comparator getRendererComparator(){
		return userComparator;
	}
	
	/** 
	 * Sets the flag to ignore case or be case sensitive<br>
	 * It affects to the algorithms to search for the best match on the content. 
	 */
	public void setIgnoreCase(boolean set){
		if (ignoreCase!=set){
			ignoreCase = set;
			fixComparator();
			clearContent();
		}
	}
	
	/** Returns true if the object is a valid option (as object, or string) */
	public boolean isValidOption(Object o){
		return content.contains(o);
	}
	
	/** @see PopupComponent#selectBestMatch(Object, boolean) */
	public PopupComponent.Match getClosestMatch(Object hint, boolean exact) {
		return (useFormatter && (hint instanceof String))? 
				findOnSortedContent((String)hint, exact) : 
					new PopupComponent.Match(content.indexOf(hint));
	}

	public void clearContent() {
		int size = getSize();
		if (size > 0) {
			customChoices = 0;
			content.clear();
			fireIntervalRemoved(this, 0, size);
		}
	}
	
	public boolean isEmpty(){
		return content.isEmpty();
	}

	/** Returns the current options */
	public Collection<?> getOptions(){
		return content;
	}

	/** Returns the list of subchoices in the whole content */
	public Collection<CustomChoice> getCustomChoices(){
		return content.subList(0, customChoices);
	}

	/** 
	 * Adds additional options.<br>
	 * If the content is text-based, the options are converted into Strings, 
	 * and sorted.<br>
	 * Otherwise, no sorting is performed, although duplicates are still
	 * discarded
	 * @return true if there are any changes after the operation
	 */
	public boolean addContent(Collection addedContent) {
		boolean changed=false;
		List choices = null;
		for (Object o : addedContent){
			boolean custom = o instanceof CustomChoice;
			if (!custom){
				if (o!=null && useFormatter){
					String s = formatter==null? o.toString() : formatter.format(o);	
					o = s.length()==0? null : s;
				}
				if (o==null){
					o=CustomChoice.MATCH_EMPTY;
				} else {
					if (choices==null){
						choices = content.subList(customChoices, content.size());
					}
					int pos = Collections.binarySearch(choices, o, comparator);
					if (pos<0){
						choices.add(-1-pos, o);
						changed=true;
					}
					continue;
				}
			}
			if (addCustomChoice((CustomChoice)o)){
				choices=null;
				changed=true;
			}
		}
		if (changed){
			addCustomChoice(CustomChoice.MATCH_ALL);
			fireContentsChanged(this, 0, getSize());
		}
		return changed;
	}
	
	/** Adds a CustomChoice, if not yet present */
	private boolean addCustomChoice(CustomChoice cf){
		List cc = content.subList(0, customChoices);
		int pos = Collections.binarySearch(cc, cf);
		if (pos<0){
			cc.add(-1-pos, cf);
			customChoices+=1;
			return true;
		}
		return false;
	}

	/** Creation of the Match, for text based, sorted content */
	private PopupComponent.Match findOnSortedContent(String strStart, 
			                                         boolean fullMatch) {
		PopupComponent.Match ret = new PopupComponent.Match();
		if (content.isEmpty()) {
			ret.index = -1;
		} else {
			ret.len = strStart.length();
			ret.exact = ret.len == 0;
			int originalLen = ret.len;
			while (ret.len > 0) {
				for (Object o : content) {
					String os;
					boolean customFilter = o instanceof CustomChoice; 
					if (customFilter){
						os = ((CustomChoice) o).getRepresentation();
					} else {
						os = (String) o;
					}
					if (os!=null){
						int osLength = os.length();
						if (osLength >= ret.len) {
							String cmpStr = os.substring(0, ret.len); 
							int cmp = comparator.compare(cmpStr, strStart); 
							if (cmp == 0) {
								ret.exact = osLength == originalLen;
								return ret;
							} else if (cmp > 0 && !customFilter) {
								break;
							}
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
		return ret;
	}
	
	/** Ensures that comparator variable has a meaningful content */
	private void fixComparator(){
		if (useFormatter){
			//string based, user comparator won't be used
	        comparator = FilterSettings.getStringComparator(ignoreCase);
		} else {
			//ignore case not used
			if (userComparator==null){
		        if (Comparable.class.isAssignableFrom(associatedClass)) {
		            comparator = DEFAULT_COMPARABLE_COMPARATOR;
		        } else {
		        	comparator = DEFAULT_COMPARATOR;
		        }				
			} else {
				comparator = userComparator;
			}
		}
	}
	
	private static Comparator DEFAULT_COMPARATOR = new Comparator() {
		@Override public int compare(Object o1, Object o2) {
			//on a JTable, sorting will use the string representation, but here
			//is not enough to distinguish on string representation, as it is
			//only used for cases where the content is not converted to String
			int ret = o1.toString().compareTo(o2.toString());
			if (ret==0 && !o1.equals(o2)){
				ret = o1.hashCode()-o2.hashCode();
				if (ret==0){
					ret=System.identityHashCode(o1)-System.identityHashCode(o2);
				}
			}
			return ret;
		}
	};

	private static Comparator DEFAULT_COMPARABLE_COMPARATOR = 
		new Comparator<Comparable>() {

		@Override public int compare(Comparable o1, Comparable o2) {
			return o1.compareTo(o2);
		}
	};	
}
