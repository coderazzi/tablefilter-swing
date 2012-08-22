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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.util.Calendar;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.RowFilter;
import javax.swing.UIManager;

import net.coderazzi.filters.examples.TableFilterExample;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.IFilterEditor;


public class Ages60sCustomChoice extends CustomChoice {

	private static final long serialVersionUID = -1643804582570086829L;
	
	Calendar cal = Calendar.getInstance();

    public Ages60sCustomChoice() {
        super("sixties",
            new ImageIcon(
                TableFilterExample.class.getResource("resources/60.png")));
    }

    @Override public RowFilter getFilter(IFilterEditor editor) {
        final int modelIndex = editor.getModelIndex();

        return new RowFilter() {
            @Override public boolean include(RowFilter.Entry entry) {
                Object o = entry.getValue(modelIndex);
                if (o instanceof Date) {
                    cal.setTime((Date) o);

                    int year = cal.get(Calendar.YEAR);

                    return (year >= 1960) && (year < 1969);
                }

                return false;
            }
        };
    }

    @Override public void decorateComponent(IFilterEditor editor,
                                            boolean       isSelected,
                                            JComponent    c,
                                            Graphics      g) {
        Icon icon = getIcon();
        if (icon != null) {
            Icon use;
            if (c.isEnabled()) {
                use = icon;
            } else {
                use = UIManager.getLookAndFeel().getDisabledIcon(c, icon);
            }

            Font font = editor.getLook()
                    .getCustomChoiceDecorator()
                    .getFont(this, editor, isSelected);
            FontMetrics metrics = g.getFontMetrics(font);
            int x = c.getWidth() - metrics.stringWidth(this.toString());
            int y = (c.getHeight() - use.getIconHeight()) / 2;
            use.paintIcon(c, g, x, y);
        }
    }
}
