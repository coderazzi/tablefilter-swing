/**
 * Author:  Luis M Pena  ( byteslooser@gmail.com )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  byteslooser@gmail.com
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

package com.byteslooser.filters.parser.generic;


/**
 * <p>Definition of a relational operand, as used in the {@link FilterTextParser}</p>
 *
 * <p>An operand has the following features:
 *
 * <ul>
 * <li>It is relational binary: it expresses a relational operation between two elements, whose
 * results has a boolean type</li>
 * <li>It applies to specific types (classes)</li>
 * <li>It can require to be applied on the string representation of the objects, not the objects
 * themselves</li>
 * </ul>
 * </p>
 *
 * @author  Luis M Pena - byteslooser@gmail.com
 */
public interface IRelationalOperand {

    /**
     * Returns the length of the operand as a text symbol. Note that the interface does not preclude
     * the need for the operand to know its string representation. In fact, this information is only
     * required in the {@link IRelationalOperandFactory}
     */
    public int symbolLength();

    /**
     * Specific filter, to define on which types this operand applies.
     */
    public boolean appliesOnType(Class<?> c);

    /**
     * If true, the operand applies on the string representation of the objects, not the objects
     * themselves.
     */
    public boolean stringBased();

    /**
     * Creates an operator by applying an object on an operand. For example, in 'a < 3', the operand
     * is '<', and creates the operator '<3' when associated to the right object 3. Returns null if
     * the right object is not valid.
     */
    public IOperator createOperator(Object right);

}
