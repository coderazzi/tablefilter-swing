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

import javax.swing.Icon;
import javax.swing.RowFilter;

import net.coderazzi.filters.IFilterTextParser;
import net.coderazzi.filters.gui.FilterSettings;

/**
 * Class to specify a custom filter in the options list
 * @author Luismi
 *
 */
public class CustomChoice {
	
	/** Empty filter, returns all entries */
	public final static CustomChoice MATCH_ALL = new CustomChoice(""){
		RowFilter rf = new RowFilter(){
			@Override
			public boolean include(RowFilter.Entry entry) {
				return true;
			}
		};
		@Override
		public RowFilter getFilter(IFilterTextParser parser, int modelPosition) {
			return rf;
		}
	};
	
	/** Special empty filter, returns all entries with null or empty values*/
	public final static CustomChoice MATCH_EMPTY = new CustomChoice(
			FilterSettings.matchEmptyFilterString,
			FilterSettings.matchEmptyFilterIcon){
		@Override
		public RowFilter getFilter(IFilterTextParser parser, 
				final int modelPosition) {
			final Format format = parser.getFormat(
						parser.getTableModel().getColumnClass(modelPosition));
			return new RowFilter(){
				@Override
				public boolean include(RowFilter.Entry entry) {
					Object o = entry.getValue(modelPosition);
					if (o==null) return true;
					if (format!=null){
						String s=format.format(o);
						return s==null || s.trim().length()==0;
					}
					return false;
				}				
			};
		}		
	};
	
	private Icon icon;
	private String str;
	
	/**
	 * Creates a custom choice without associated icon, to be handled
	 * exclusively as text
	 */
	public CustomChoice(String representation){
		this(representation, null);
	}

	/**
	 * Creates a custom choice with associated icon
	 */
	public CustomChoice(String representation, Icon icon){
		this.icon = icon;
		this.str = representation;
	}
	
	/**
	 * Return an icon associated to this filter.<br>
	 * It can be null if there is no associated graphic representation for this
	 * filter
	 */
	public Icon getIcon(){
		return icon;
	}
	
	/** Returns the string representation of the filter */
	public String getRepresentation(){
		return str;
	}
	
	/** 
	 * Returns true if the text should be displayed on the options, when
	 * there is an icon present<br>
	 * This method is only called if an icon is available.
	 * @param userRendererSet set to true if the user has specified any 
	 *    special render for the associated column
	 */
	public boolean renderTextInOptions(boolean userRendererSet){
		return !userRendererSet;
	}
	
	/**
	 * Returns true if the user filter matches the given filter text,
	 * which is provided without side spaces.
	 */
	public boolean matchesFilterText(String trimmedFilterText, 
			boolean ignoreCase){
		return ignoreCase? 
			getRepresentation().equalsIgnoreCase(trimmedFilterText) : 
			getRepresentation().equals(trimmedFilterText);
	}
	
	/** Returns the associated filter */
	public RowFilter getFilter(IFilterTextParser parser,
			int modelPosition) {
		try{
			return parser.parseText(getRepresentation(), modelPosition);
		} catch (ParseException pex) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return getRepresentation();
	}
	
	@Override
	public int hashCode() {
		return getRepresentation().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof CustomChoice) && 
			((CustomChoice)o).getRepresentation().equals(getRepresentation());
	}
}
