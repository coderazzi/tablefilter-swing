/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 * <p>
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coderazzi.filters.examples.utils;

import net.coderazzi.filters.Filter;
import net.coderazzi.filters.examples.ActionHandler;


public class UserFilter extends Filter {

    private final ActionHandler main;

    public UserFilter(ActionHandler main) {
        this.main = main;
    }

    @Override
    public boolean include(Entry entry) {
        int n = main.getTableModel().getColumn(TestTableModel.NAME);

        return (n == -1) || (-1 != entry.getStringValue(n).indexOf('e'));
    }
}
