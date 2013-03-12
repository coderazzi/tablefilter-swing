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

import java.util.HashSet;
import java.util.Set;

import javax.swing.RowFilter;

import net.coderazzi.filters.examples.ActionHandler;
import net.coderazzi.filters.examples.utils.TestData;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.IFilterEditor;


public class MenuMaleCustomChoices extends AbstractMenuCheckBoxAction {

    private static final long serialVersionUID = 9137226745345048519L;

    private IFilterEditor editor;

    public MenuMaleCustomChoices(ActionHandler main, IFilterEditor editor) {
        super("Specific custom choices", main);
        this.editor = editor;
    }

    @Override public void actionPerformed(boolean selected) {
        Set<CustomChoice> choices = new HashSet<CustomChoice>();
        if (selected) {
            // specific code.
            // the checkbox for male/female can be modified.
            // if the associated filter is set to true or false, updating
            // this
            // checkbox value would make the row vanish
            // To avoid that, we setup specific custom choices that do not
            // filter out modified values
            CustomChoice obsoleteChoice = new CustomChoice("True +") {

                private static final long serialVersionUID = 5531933637893891168L;

				@Override public RowFilter getFilter(IFilterEditor fe) {
                    return new RowFilter() {
                        @Override public boolean include(Entry entry) {
                            int row = (Integer) entry.getIdentifier();
                            TestTableModel tm = main.getTableModel();
                            TestData td = tm.getRow(row);

                            return td.male || tm.isModified(td);
                        }
                    };
                }
            };

            CustomChoice nonObsoleteChoice = new CustomChoice("False +") {

                private static final long serialVersionUID = -4183027765686996202L;

				@Override public RowFilter getFilter(IFilterEditor fe) {
                    return new RowFilter() {
                        @Override public boolean include(Entry entry) {
                            int row = (Integer) entry.getIdentifier();
                            TestTableModel tm = main.getTableModel();
                            TestData td = tm.getRow(row);

                            return !td.male || tm.isModified(td);
                        }
                    };
                }
            };

            choices.add(obsoleteChoice);
            choices.add(nonObsoleteChoice);
            editor.setAutoChoices(AutoChoices.DISABLED);
            editor.setEditable(false);
            editor.setCustomChoices(choices);
        } else {
            editor.setCustomChoices(choices);
            editor.setAutoChoices(AutoChoices.ENUMS);
            editor.setEditable(true);
        }

        main.updateFilterInfo(editor);
    }


}
