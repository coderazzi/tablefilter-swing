/**
 * Author:  Luis M Pena  ( dr.lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  dr.lu@coderazzi.net
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

package net.coderazzi.filters.parser;

import java.util.List;

import javax.swing.RowFilter;


/**
 * <p>Interface defining the requirements on text parsing for filter expressions.</p>
 *
 * <p>The assumption is the existence of multiple variables, where the filter can define any
 * associated condition. The type of each variable is known, and the interface provides means to
 * define associated parsers and comparators, which should improve the 'intelligence' of the
 * parser.</p>
 *
 * <p>This interface is influenced by the generic {@link javax.swing.RowFilter.Entry} class, used by
 * default to filter tables from Java 6 on. As such, each variable has associated a 'position',
 * which can be used to request the value of the variable to a {@link javax.swing.RowFilter.Entry}
 * (in this class, the notation calls it 'index')</p>
 *
 * @author  Luis M Pena - dr.lu@coderazzi.net
 */
public interface IFilterTextParser {

    /** Const definition to be used on parseText when there is no 'default' identifier. */
    public final int NO_FILTER_POSITION = -1;

    /**
     * <p>Parses the text, using the given default identifier.</p>
     *
     * <p>If the parsed expression includes conditions that specify no identifier, the default
     * identifier is used.</p>
     *
     * <p>Default identifier can be {@link IFilterTextParser#NO_FILTER_POSITION}, if the expression
     * must provide them specifically</p>
     *
     * <p>A filter supporting the interface {@link javax.swing.RowFilter} receives an instance of
     * type {@link javax.swing.RowFilter.Entry} that can enquiry to obtain the value at a specified
     * index; the filter position is that given index, and corresponds, in the case of a table, to
     * the column to which this editor is associated.</p>
     */
    public abstract RowFilter parseText(String expression, int defaultFilterPosition)
        throws FilterTextParsingException;

    /**
     * Defines the identifiers, and the types and indexes associated to them
     */
    public void setIdentifiers(List<IdentifierInfo> validIdentifiers);

    /**
     * Ignores case on the conditions associated to variables with String type
     */
    public void setIgnoreCase(boolean ignore);

}
