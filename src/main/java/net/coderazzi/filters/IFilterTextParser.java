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


/**
 * Interface defining the requirements on text parsing for filter expressions.
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public interface IFilterTextParser {

	/**
	 * Associates a {@link TableModel} to the parser.<br>
	 * The parser can use it to extract the type associated to a given column, 
	 * or to define variables-the name of the column-. <br>
	 * The usage is specific to the final implementation of this interface.
	 */
    public void setTableModel(TableModel model);

    /**
     * Parses the text, corresponding to a column in the table model<br>
     * It returns a filter that can be applied to the table sorter.
     * @param expression the text to parse
     * @param modelPosition the position on the table model. 
     */
    public RowFilter parseText(String expression,
                               int modelPosition) throws ParseException;

    /**
     * Ignores case -if the operator is string based-
     */
    public void setIgnoreCase(boolean ignore);

    /**
     * Returns true if it ignores case
     */
    public boolean isIgnoreCase();

    /**
     * Defines the default operator when the user specifies none
     */
    public void setDefaultOperator(String s);

    /**
     * Returns the default operand
     */
    public String getDefaultOperator();

    /**
     * <p>Sets a specific comparator for a given class, that should override 
     * the default compare algorithm for the given class.</p>
     *
     * <p>If the class is not {@link java.lang.Comparable}, this method should 
     * be invoked to support any comparison operator, or any operation will 
     * be invoked on the string representation of the instance.</p>
     */
    public void setComparator(Class<?> c,
                              Comparator<?> cmp);

    /**
     * Returns the {@link Comparator} provided for the given class
     */
    public Comparator<?> getComparator(Class<?> c);

    /**
     * Provides the {@link Format} to build non basic types. <br>
     * If a table model defines some column without specified format, only 
     * string operations will apply.<br>
     * Please note that every IFilterTextParser can define its own set of 
     * predefined formats
     */
    public void setFormat(Class<?> c,
                          Format format);

    /**
     * Returns the {@link Format} for a given class
     */
    public Format getFormat(Class<?> c);

    /**
     * Adds a {@link PropertyChangeListener}. <br>
     * Any property change will be transmitted as an event
     */
	public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes an existing {@link PropertyChangeListener}
     */
	public void removePropertyChangeListener(PropertyChangeListener listener);

}