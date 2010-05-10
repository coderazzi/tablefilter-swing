/**
 * Author:  Luis M Pena  ( sen@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  sen@coderazzi.net
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

/**
 * <p>Class holding specific information for a given identifier.</p>
 *
 * <p>A generic parse handles filter expressions handled as composition of subexpressions concerning
 * an indentifier, an operand, an a value. An identifier in such expressions is defined through this
 * class, as a name, a class, and its filter position.</p>
 *
 * <p>The filter position corresponds, in the case of a table, to the column to which this editor is
 * associated.</p>
 *
 * @author  Luis M Pena - sen@coderazzi.net
 */
public class IdentifierInfo {

    /** The identifier name */
    public String name;

    /** Its class */
    public Class<?> associatedClass;

    /** Filter position, as associated to the {@link javax.swing.RowFilter.Entry} index */
    public int filterPosition;

    /**
     * Unique constructor, providing the full identifier contents
     */
    public IdentifierInfo(String identifier, Class<?> associatedClass, int filterPosition) {
        this.name = identifier;
        this.associatedClass = associatedClass;
        this.filterPosition = filterPosition;
    }
}
