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

package net.coderazzi.filters;

import java.beans.PropertyChangeListener;
import java.text.Format;
import java.text.ParseException;
import java.util.Comparator;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.TableFilterHeader;


/**
 * Interface defining the requirements on text parsing for filter expressions.
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public interface IFilterTextParser {
	
	/** Property fired when the ignore case value changes */
	final public static String IGNORE_CASE_PROPERTY = "ignoreCase"; 

	/** Property fired when the table model value changes */
	final public static String TABLE_MODEL_PROPERTY = "tableModel"; 

	/** Property fired when any class' comparator changes */
	final public static String COMPARATOR_PROPERTY = "comparator"; 

	/** Property fired when any class' format changes */
	final public static String FORMAT_PROPERTY = "format"; 

	/**
	 * Associates a {@link TableModel} to the parser.<br>
	 * The parser can use it to extract the type associated to a given column, 
	 * or to define variables-the name of the column-. <br>
	 * The usage is specific to the final implementation of this interface.<br>
	 * The property {@link IFilterTextParser#TABLE_MODEL_PROPERTY} is fired, 
	 * with the old / new table models.<br>
	 * Final users should not invoke this method, as it should be always the
	 * same as defined in the table / filter header<br>
	 */
    public void setTableModel(TableModel model);
    
    /** Returns the associated table model */
    public TableModel getTableModel();

    /**
     * Parses the text, corresponding to a column in the table model<br>
     * It returns a filter that can be applied to the table sorter.
     * @param expression the text to parse
     * @param modelPosition the position on the table model. 
     */
    public RowFilter parseText(String expression,
                               int modelPosition) throws ParseException;

    /**
     * Ignores case -if the operator is string based-<br>
	 * Changing this value fires the property 
	 * {@link IFilterTextParser#IGNORE_CASE_PROPERTY},
	 * with a boolean value associated.
     */
    public void setIgnoreCase(boolean ignore);

    /**
     * Returns true if it ignores case
     */
    public boolean isIgnoreCase();

    /**
     * <p>Sets a specific comparator for a given class, that should override 
     * the default compare algorithm for the given class.</p>
     *
     * <p>If the class is not {@link java.lang.Comparable}, this method should 
     * be invoked to support any comparison operator, or any operation will 
     * be invoked on the string representation of the instance.</p>
     * 
     * <p>Changing this value fires the property 
     * {@link IFilterTextParser#COMPARATOR_PROPERTY},
	 * with a Class value associated -the class being changed-.</p>
     */
    public void setComparator(Class<?> c,
                              Comparator<?> cmp);

    /**
     * Returns the {@link Comparator} provided for the given class
     */
    public Comparator<?> getComparator(Class<?> c);

    /**
     * <p>Provides the {@link Format} to build any type. </p>
     * <p>If a table model defines some column without specified format, only 
     * string operations will apply.</p>
     * <p>Please note that every IFilterTextParser can define its own set of 
     * predefined formats. It is only ensured that there will always be
     * a format for type String, that will be used to format text on
     * unknown types</p>
     * <p>Changing this value fires the property 
     * {@link IFilterTextParser#FORMAT_PROPERTY},
	 * with a Class value associated -the class being changed-.</p>
     */
    public void setFormat(Class<?> c,
                          Format format);

    /**
     * Returns the {@link Format} for a given class<br>
     * By default, only one such format exists, for type String. This is
     * the format that will be used to format text on unknown types.<br>
     * The default text parser created using the 
     * {@link FilterSettings#newTextParser()} method (as it is the case of
     * the filter in the {@link TableFilterHeader} instances define already
     * formats for all basic types.
     */
    public Format getFormat(Class<?> c);
    
    /** Clones the parser */
    public IFilterTextParser clone();
    
    /** 
     * Escapes a given expression, such that, when parsed, the parser will
     * make no character/operator substitutions.  
     */
    public String escape(String s, int modelPosition);

    /**
     * Adds a {@link PropertyChangeListener}. <br>
     * Any property change will be transmitted as an event
     */
	public void addPropertyChangeListener(PropertyChangeListener listener);

    /** Removes an existing {@link PropertyChangeListener} */
	public void removePropertyChangeListener(PropertyChangeListener listener);

}