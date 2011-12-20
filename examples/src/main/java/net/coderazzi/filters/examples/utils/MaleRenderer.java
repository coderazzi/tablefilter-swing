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

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import net.coderazzi.filters.examples.ActionHandler;


public class MaleRenderer implements TableCellRenderer {

    private ActionHandler main;
    private Border redBorder;
    private TableCellRenderer delegate;

    public MaleRenderer(ActionHandler main) {
        this.main = main;
        delegate = main.getTable().getDefaultRenderer(Boolean.class);
        redBorder = BorderFactory.createLineBorder(Color.red);
    }

    @Override public Component getTableCellRendererComponent(JTable  table,
                                                             Object  value,
                                                             boolean isSelected,
                                                             boolean hasFocus,
                                                             int     row,
                                                             int     column) {
        JComponent c = (JComponent) delegate.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        int modelRow = table.convertRowIndexToModel(row);
        TestTableModel tm = main.getTableModel();
        if (tm.isModified(tm.getRow(modelRow))) {
            c.setBorder(redBorder);
        }

        return c;
    }
}
