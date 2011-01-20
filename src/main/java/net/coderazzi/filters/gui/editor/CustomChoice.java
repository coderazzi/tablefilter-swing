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
import java.util.Comparator;

import javax.swing.Icon;
import javax.swing.RowFilter;

import net.coderazzi.filters.IFilterTextParser;
import net.coderazzi.filters.gui.FilterSettings;

/**
 * Class to specify a custom filter in the options list<br>
 * 
 * A custom filter can specify an icon and a textual representation, which,
 * if the icon is provided, can be hidden.<br>
 * The filter associated to the CustomChoice is, by default, the parsing of the 
 * given textual representation, although a custom filter can be provided by 
 * overriding the method {@see #getFilter(IFilterTextParser, int)} <br>
 * 
 * The order of the custom choices on the options list can be modified with
 * the precedence attribute. By default, custom choices are sorted by their
 * textual representation. 
 * 
 * @author Luismi
 */
public class CustomChoice implements Comparable<CustomChoice>{
	
	public final static int DEFAULT_PRECEDENCE = 0;
	
	/** Empty filter, returns all entries */
	public final static CustomChoice MATCH_ALL = new CustomChoice(""){
		RowFilter rf = new RowFilter(){
			@Override public boolean include(RowFilter.Entry entry) {
				return true;
			}
		};
		@Override public RowFilter getFilter(IFilterTextParser parser, int modelPosition) {
			return rf;
		}
	};
	
	/** Special empty filter, returns all entries with null or empty values*/
	public final static CustomChoice MATCH_EMPTY = new CustomChoice(
			FilterSettings.matchEmptyFilterString,
			FilterSettings.matchEmptyFilterIcon){
		@Override public RowFilter getFilter(IFilterTextParser parser, 
				final int modelPosition) {
			final Format format = parser.getFormat(
						parser.getTableModel().getColumnClass(modelPosition));
			return new RowFilter(){
				@Override public boolean include(RowFilter.Entry entry) {
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
	private int precedence;
	private final static Comparator<String> reprComparator = FilterSettings.getStringComparator(false);
	
	/** Full constructor */
	 public CustomChoice(String representation, Icon icon, int precedence){
		this.icon = icon;
		this.str = representation;
		this.precedence = precedence;
	}
	
	/**
	 * Creates a custom choice without associated icon, and with default
	 * precedence, to be handled exclusively as text.
	 */
	public CustomChoice(String representation){
		this(representation, null, DEFAULT_PRECEDENCE);
	}

	/**
	 * Creates a custom choice with associated icon and default precedence
	 */
	public CustomChoice(String representation, Icon icon){
		this(representation, icon, DEFAULT_PRECEDENCE);
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
	
	/** Override to define the filter, if parsed*/
	protected String getParsingExpression(){
		return str;
	}
	
	/** Returns the precedence value */ 
	public int getPrecedence(){
		return precedence;
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
	 * Returns the associated filter<br>
	 * By default, is the parser' handling of the textual representation
	 * associated to this choice. 
	 */
	public RowFilter getFilter(IFilterTextParser parser,
			int modelPosition) {
		try{
			return parser.parseText(getParsingExpression(), modelPosition);
		} catch (ParseException pex) {
			return null;
		}
	}
	
	@Override public String toString() {
		return getRepresentation();
	}
	
	@Override public int hashCode() {
		return getRepresentation().hashCode();
	}
	
	@Override public boolean equals(Object o) {
		return (o instanceof CustomChoice) && 
			((CustomChoice)o).getRepresentation().equals(getRepresentation());
	}
	
	@Override public int compareTo(CustomChoice o) {
		int ret = getPrecedence() - o.getPrecedence();
		return ret==0?
			reprComparator.compare(getRepresentation(), o.getRepresentation())
			: ret;
	}
}
