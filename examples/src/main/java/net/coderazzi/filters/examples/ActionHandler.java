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

package net.coderazzi.filters.examples;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JTable;

import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.TableFilterHeader;


/** Public interface of the {@link TableFilterExample}. */
public interface ActionHandler {
    TableFilterHeader getFilterHeader();
    TestTableModel getTableModel();
    JMenu getFilterMenu();
    JTable getTable();
    JFrame getJFrame();
    void initTableModel(int rows);

    /** Update the enabled flag associated to the filter header. */
    void updateEnabledFlag();

    /** Updates the filter information associated to all editors. */
    void updateFiltersInfo();

    /** Updates the filter information associated to the given editor. */
    void updateFilterInfo(IFilterEditor editor);
}
