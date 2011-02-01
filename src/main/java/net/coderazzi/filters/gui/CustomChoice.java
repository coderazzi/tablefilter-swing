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

import java.text.Format;

import javax.swing.Icon;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;

/**
 * Class to specify a custom filter in the choices list<br>
 * 
 * A custom choice allows to specify custom filters that can be selected as
 * a choice in the filter editor. A custom choice is associated always to
 * a text, which the user can input to select the given choice.<br>
 * 
 * It is also possible to specify how the custom filter is rendered; by
 * default it is displayed the icon -if provided-, and the text, unless the
 * user provides a custom renderer.<br> 
 * 
 * The order of the custom choices on the choices list can be modified with
 * the precedence attribute. By default, custom choices are sorted by their
 * textual representation. 
 * 
 * @author Luismi
 */
public abstract class CustomChoice {
	
	public final static int DEFAULT_PRECEDENCE = 0;
	
	/** Empty filter, returns all entries */
	public final static CustomChoice MATCH_ALL = new CustomChoice(""){
		RowFilter rf = new RowFilter(){
			@Override public boolean include(RowFilter.Entry entry) {
				return true;
			}
		};
		@Override public RowFilter getFilter(IFilterEditor editor) {
			return rf;
		}
	};
	
	/** Special empty filter, returns all entries with null or empty values*/
	public final static CustomChoice MATCH_EMPTY = new CustomChoice(
			FilterSettings.matchEmptyFilterString,
			FilterSettings.matchEmptyFilterIcon){
		@Override public RowFilter getFilter(final IFilterEditor editor) {
			final int modelIndex = editor.getModelIndex();
			return new RowFilter(){
				@Override public boolean include(RowFilter.Entry entry) {
					Object o = entry.getValue(modelIndex);
					if (o==null) return true;
					Format format = editor.getFormat();
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
	
	/** Returns the renderer for this choice, or null to use the default one */
	public ListCellRenderer getRenderer(){
		return null;
	}
	
	/**
	 * Return an icon associated to this filter.<br>
	 * It can be null if there is no associated graphic representation for this
	 * filter
	 */
	public Icon getIcon(){
		return icon;
	}
	
	/** 
	 * Returns true if the text should be displayed on the choices, when
	 * there is an icon present<br>
	 * This method is only called if an icon is available.
	 */
	public boolean renderText(){
		return true;
	}
	
	/** Returns the precedence value */ 
	public int getPrecedence(){
		return precedence;
	}
	
	/** Returns the associated filter */
	public abstract RowFilter getFilter(IFilterEditor editor);
	
	/** Returns the string representation of the filter */	
	@Override final public String toString() {
		return str;
	}
	
	@Override public int hashCode() {
		return str.hashCode();
	}
	
	@Override public boolean equals(Object o) {
		return (o instanceof CustomChoice) && 
			(((CustomChoice)o).str).equals(str);
	}	
}
