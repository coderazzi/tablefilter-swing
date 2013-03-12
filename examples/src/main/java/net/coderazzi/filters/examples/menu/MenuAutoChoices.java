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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import net.coderazzi.filters.examples.ActionHandler;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.IFilterEditor;


public class MenuAutoChoices extends JMenu implements ActionListener {

    public static String NAME = "auto choices";

    private static final long serialVersionUID = -6772023653226757860L;

    private ActionHandler main;
    private IFilterEditor editor;

    /** @param  editor  set to null if the menu applies to the filter header */
    public MenuAutoChoices(ActionHandler listener, IFilterEditor editor) {
        super(NAME);
        this.main = listener;
        this.editor = editor;

        AutoChoices preselected = (editor == null)
            ? listener.getFilterHeader().getAutoChoices()
            : editor.getAutoChoices();

        ButtonGroup group = new ButtonGroup();
        for (AutoChoices ao : AutoChoices.values()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(ao.toString()
                        .toLowerCase());
            item.addActionListener(this);
            group.add(item);
            if (preselected == ao) {
                item.setSelected(true);
            }

            this.add(item);
        }
    }

    @Override public void actionPerformed(ActionEvent e) {
        AutoChoices ao = getAutoChoice(((JRadioButtonMenuItem) e.getSource())
                    .getText());
        if (editor == null) {
            main.getFilterHeader().setAutoChoices(ao);
            main.updateFiltersInfo();
        } else {
            editor.setAutoChoices(ao);
            main.updateFilterInfo(editor);
        }
    }

    private AutoChoices getAutoChoice(String lowerText) {
        for (AutoChoices a : AutoChoices.values()) {
            if (a.toString().toLowerCase().equals(lowerText)) {
                return a;
            }
        }

        return null;

    }

}
