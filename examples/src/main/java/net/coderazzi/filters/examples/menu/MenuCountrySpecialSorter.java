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

package net.coderazzi.filters.examples.menu;

import java.util.Comparator;

import net.coderazzi.filters.examples.ActionHandler;
import net.coderazzi.filters.examples.utils.TestData;
import net.coderazzi.filters.examples.utils.TestData.Flag;
import net.coderazzi.filters.examples.utils.TestTableModel;


public class MenuCountrySpecialSorter extends AbstractMenuCheckBoxAction {

    private static final long serialVersionUID = 9137226745345048519L;

    public MenuCountrySpecialSorter(ActionHandler main) {
        super("country column sorted by red proportion", main);
        setSelected(false);
    }

    @Override protected void actionPerformed(boolean set) {
        int column = main.getTableModel().getColumn(TestTableModel.COUNTRY);
        if (main.getTable().convertColumnIndexToView(column) != -1) {
            Comparator<TestData.Flag> comp = set ? new RedComparator()
                                                 : new DefaultComparator();
            main.getFilterHeader().getFilterEditor(column).setComparator(comp);
        }
    }

    public static class RedComparator implements Comparator<Flag> {
        @Override public int compare(Flag o1, Flag o2) {
            double d = o1.getRedAmount() - o2.getRedAmount();

            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }
    }

    public static class DefaultComparator implements Comparator<Flag> {
        @Override public int compare(Flag o1, Flag o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }

}
