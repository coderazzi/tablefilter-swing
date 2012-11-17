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

import net.coderazzi.filters.examples.ActionHandler;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.ChoiceRenderer;
import net.coderazzi.filters.gui.HtmlChoiceRenderer;


public class MenuHtmlCountry extends AbstractMenuCheckBoxAction {

    private static final long serialVersionUID = 9137226745345074319L;

    public MenuHtmlCountry(ActionHandler main) {
        super("use html renderer", main);
        setSelected(false);
    }

    @Override protected void actionPerformed(boolean set) {
        int column = main.getTableModel().getColumn(TestTableModel.HTML_COUNTRY);
        if (main.getTable().convertColumnIndexToView(column) != -1) {
        	ChoiceRenderer r = set ? new HtmlChoiceRenderer() : null;
            main.getFilterHeader().getFilterEditor(column).setRenderer(r);
        }
    }

}
