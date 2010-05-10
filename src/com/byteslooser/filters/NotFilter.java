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

package com.byteslooser.filters;

import javax.swing.RowFilter;


/**
 * Composed set of filters, added via logical AND, and then NOT-ed the result
 *
 * @author  Luis M Pena - byteslooser@gmail.com
 */
public class NotFilter extends AndFilter {

    /**
     * Constructor built up out of none or more {@link com.byteslooser.filters.IFilterObservable}
     * instances
     */
    public NotFilter(IFilterObservable... observables) {
        super(observables);
    }

    /**
     * @see  RowFilter#include(javax.swing.RowFilter.Entry)
     */
    @Override public boolean include(RowFilter.Entry rowEntry) {
        return !super.include(rowEntry);
    }
}
