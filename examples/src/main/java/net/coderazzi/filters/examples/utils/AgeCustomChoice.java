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

package net.coderazzi.filters.examples.utils;

import java.util.HashSet;
import java.util.Set;

import javax.swing.RowFilter;

import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.IFilterEditor;


public class AgeCustomChoice extends CustomChoice {

    private static final long serialVersionUID = -4580882606646752756L;
    
	int min;
    int max;

    private AgeCustomChoice(String text, int min, int max, int precedence) {
        super(text, null, precedence);
        this.min = min;
        this.max = max;
    }

    @Override public RowFilter getFilter(IFilterEditor editor) {
        final int modelIndex = editor.getModelIndex();

        return new RowFilter() {
            @Override public boolean include(Entry entry) {
                Object value = entry.getValue(modelIndex);
                if (value instanceof Integer) {
                    int age = (Integer) value;

                    return (age >= min) && (age <= max);
                }

                return false;
            }
        };
    }

    public static Set<CustomChoice> getCustomChoices() {
        Set<CustomChoice> ret = new HashSet<CustomChoice>();
        ret.add(new AgeCustomChoice("below 20", 0, 19, DEFAULT_PRECEDENCE + 1));
        ret.add(new AgeCustomChoice("20-34", 20, 34, DEFAULT_PRECEDENCE + 2));
        ret.add(new AgeCustomChoice("35-50", 35, 50, DEFAULT_PRECEDENCE + 3));
        ret.add(new AgeCustomChoice("over 50", 51, 1000,
                DEFAULT_PRECEDENCE + 4));

        return ret;
    }

}
