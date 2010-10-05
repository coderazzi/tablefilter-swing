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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;


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
	/** 
	 * content, converted to Strings. If the content's list is already Strings, 
	 * stringContent merely references the content's list 
	 * (i.e., stringContent = content).<br>
	 * This list is null if the content is considered non-text 
	 */
	private List stringContent;
	private Format formatter = defaultFormatter;
	private boolean ignoreCase;
	
	/** 
	 * Basic implementation, to convert any Object to String 
	 * using its defined toString method 
	 **/
	private static Format defaultFormatter = new Format(){
		private static final long serialVersionUID = 3313449946741441248L;
		@Override
		public Object parseObject(String source) throws ParseException {
			return source.toString();
		}
		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, 
				java.text.FieldPosition pos) {
			return toAppendTo.append(obj.toString());
		}
		@Override
		public Object parseObject(String source, java.text.ParsePosition pos) {
			return source;
		}
	};

	public OptionsListModel() {
		this.content = new ArrayList();
		this.stringContent = content;
	}

	@Override
	public int getSize() {
		return stringContent == null ? content.size() : stringContent.size();
	}

	@Override
	public Object getElementAt(int i) {
		return stringContent == null ? content.get(i) : stringContent.get(i);
	}

	/**
	 * Specifies how to handle the content. If no renderer is used, 
	 * the content is text based, and converted to Strings 
	 * -and displayed and searched as strings-. 
	 */
	public void setStringContent(boolean set) {
		if (set == false) {
			if (stringContent != null) {
				stringContent = null;
				fireContentsChanged(this, 0, getSize());
			}
		} else if (stringContent == null) {
			List oldContent = content;
			content = new ArrayList();
			stringContent = content;
			addContent(oldContent);
		}
	}

	/** Specifies the format used to convert Objects to Strings */
	public void setFormat(Format format) {
		this.formatter = format==null? defaultFormatter : format;
		ensureRightStringContent();
	}
	
	/** 
	 * Sets the flag to ignore case or be case sensitive<br>
	 * It affects to the algorithms to search for the best match on the content. 
	 */
	public void setIgnoreCase(boolean set){
		if (ignoreCase!=set){
			ignoreCase = set;
			ensureRightStringContent();
		}
	}
	
	private void ensureRightStringContent(){
		if (stringContent != null) {
			List oldContent = content;
			List oldStringContent = stringContent;
			content = new ArrayList();
			if (oldContent == oldStringContent) {
				stringContent = content;
			} else {
				stringContent = new ArrayList();
			}
			addContent(oldContent);
		}					
	}
	
	/** Returns true if the object is a valid option (as object, or string) */
	public boolean isValidOption(Object o){
		return (content.contains(o)) || 
					(stringContent!=null && 
							stringContent!=content && 
							(o instanceof String) && 
							stringContent.contains(o));
	}
	
	/** Commodity method to format objects using the current {@link Format} */
	public String format(Object o){
		return (o instanceof String)? (String)o : formatter.format(o);
	}

	/** @see PopupComponent#selectBestMatch(Object, boolean) */
	public PopupComponent.Match getClosestMatch(Object hint, boolean exact) {
		if (stringContent == null) {
			return new PopupComponent.Match(content.indexOf(hint));
		}
		return (hint instanceof String)? 
				findOnSortedContent((String)hint, exact) : null;
	}

	public void clearContent() {
		int size = getSize();
		if (size > 0) {
			content.clear();
			if (stringContent!=null){
				stringContent=content;
			}
			fireIntervalRemoved(this, 0, size);
		}
	}
	
	public boolean isEmpty(){
		return content.isEmpty();
	}

	/** 
	 * Adds additional options.<br>
	 * If the content is text-based, the options are converted into Strings, 
	 * and sorted.<br>
	 * Otherwise, no sorting is performed, although duplicates are still
	 * discarded
	 */
	public void addContent(Collection addedContent) {
		if (!addedContent.isEmpty()){
			if (isEmpty()){
				//it is needed to always have the null/empty filter
				content.add(EditorComponent.EMPTY_FILTER);
			}
			if (stringContent != null) {
				if (stringContent == content) {
					// this means that content contains only Strings. 
					// We need to ensure now that the addedContent is all 
					// Strings, otherwise we have to create a separate content 
					// for stringContent
					for (Object o : addedContent) {
						if (!(o instanceof String)) {
							stringContent = new ArrayList(content);
							break;
						}
					}
				}
				for (Object o : addedContent) {
					addStringContent(formatter.format(o));
				}
			}
			if (stringContent != content) {
				for (Object o : addedContent) {
					if (!content.contains(o)) {
						content.add(o);
					}
				}
			}
			fireContentsChanged(this, 0, getSize());
		}
	}

	/** Adds an string into the proper place (order) */
	private void addStringContent(String s) {
		int position = 0;
		for (Object o : stringContent) {
			String os = (String)o;
			int compare = ignoreCase? os.compareToIgnoreCase(s) : os.compareTo(s);
			if (compare == 0) {
				return;
			}
			if (compare > 0) {
				stringContent.add(position, s);
				return;
			}
			position++;
		}
		stringContent.add(s);
	}

	/** Creation of the Match, for text based, sorted content */
	private PopupComponent.Match findOnSortedContent(String strStart, 
			                                         boolean fullMatch) {
		PopupComponent.Match ret = new PopupComponent.Match();
		if (stringContent.isEmpty()) {
			ret.index = -1;
		} else {
			ret.len = strStart.length();
			ret.exact = ret.len == 0;
			int originalLen = ret.len;
			while (ret.len > 0) {
				for (Object o : stringContent) {
					String os = (String) o;
					int osLength = os.length();
					if (osLength >= ret.len) {
						String cmpStr = os.substring(0, ret.len); 
						int cmp = ignoreCase? 
								cmpStr.compareToIgnoreCase(strStart) 
								: cmpStr.compareTo(strStart);
						if (cmp == 0) {
							ret.exact = osLength == originalLen;
							return ret;
						} else if (cmp > 0) {
							break;
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
}
