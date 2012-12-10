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
import net.coderazzi.filters.gui.IFilterEditor;


public class MenuAlphaChoicesOrder extends AbstractMenuCheckBoxAction {

	public static String NAME = "choices alphabetically ordered";

	private static final long serialVersionUID = -2706190539697402225L;

    private IFilterEditor editor;

    /** @param  editor  can be null to act on the FilterHeader */
    public MenuAlphaChoicesOrder(ActionHandler main, IFilterEditor editor) {
        super(NAME, main);
        this.editor = editor;
        setSelected((editor == null)
                ? main.getFilterHeader().hasAlphabeticalOrderOnChoices()
                : editor.hasAlphabeticalOrderOnChoices());
    }

    @Override protected void actionPerformed(boolean selected) {
        if (editor == null) {
            main.getFilterHeader().setAlphabeticalOrderOnChoices(selected);
            main.updateFiltersInfo();
        } else {
            editor.setAlphabeticalOrderOnChoices(selected);
        }
    }

}
