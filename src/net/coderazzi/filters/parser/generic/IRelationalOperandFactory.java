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

package net.coderazzi.filters.parser.generic;

import java.util.Comparator;


/**
 * <p>This interface allows to control the relational operands used in the parser</p>
 *
 * <p>There are two sets of filter operands: logical operators, used to compose complex filters, and
 * relational operators, used to define basic filter expressions.</p>
 *
 * <p>By construction, the logical operators are built in in the {@link FilterTextParser}, which
 * relies on this factory to provide information for the relational ones</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public interface IRelationalOperandFactory {

    /**
     * Returns the operand that exists at the beginning of the substring all[index:]
     *
     * @param   c      the character at all[index]
     * @param   all    the whole string
     * @param   index  the position to start parsing
     *
     * @return  null if the given position does not define a valid operand
     */
    public IRelationalOperand getOperand(char c, String all, int index);

    /**
     * Returns the default operand to apply on a given class
     *
     * @param  c        the type's class
     * @param  nullOp   true if the value to be associated to the operand is the null instance.
     * @param  rightId  true if the comparison is against a second identifier
     */
    public IRelationalOperand getDefaultOperand(Class<?> c, boolean nullOp, boolean rightId);

    /**
     * Defines a comparator for a given class. This comparator must be propagated to the operands,
     * to modify their behaviour when comparating objects of this class.
     */
    public void setComparator(Class<?> c, Comparator<?> cmp);

    /**
     * Defines whether the created operand should be case insensitive. This comparator must be
     * propagated to the operands, to modify their behaviour when comparating string objects.
     */
    public void setIgnoreCase(boolean ignore);
}
