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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import net.coderazzi.filters.gui.ChoiceRenderer;
import net.coderazzi.filters.gui.IFilterEditor;


public class FlagRenderer extends JLabel implements ChoiceRenderer,
    TableCellRenderer {

    private static final long serialVersionUID = -6640707874060161068L;

    public FlagRenderer() {
        setOpaque(true);
    }

    @Override public Component getTableCellRendererComponent(JTable  table,
                                                             Object  value,
                                                             boolean isSelected,
                                                             boolean hasFocus,
                                                             int     row,
                                                             int     column) {
        Component ret = setup(value);
        if (isSelected) {
            ret.setBackground(table.getSelectionBackground());
            ret.setForeground(table.getSelectionForeground());
        } else {
            ret.setBackground(table.getBackground());
            ret.setForeground(table.getForeground());
        }

        ret.setFont(table.getFont());

        return ret;
    }

    @Override public Component getRendererComponent(IFilterEditor editor,
                                                    Object        value,
                                                    boolean       isSelected) {
        Component ret = setup(value);
        editor.getLook()
            .setupComponent(ret, isSelected, editor.getFilter().isEnabled());

        return ret;
    }

    private Component setup(Object value) {

        ImageIcon icon = (ImageIcon) value;
        setIcon(icon);
        setHorizontalAlignment(SwingConstants.CENTER);
        setText("");

        if (icon != null) {
            setToolTipText(icon.getDescription());
        }

        return this;
    }

}
