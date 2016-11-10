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
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.LooseParserModel;
import net.coderazzi.filters.gui.ParserModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MenuParser extends JMenu implements ActionListener {

    private static final long serialVersionUID = -6772023656716757860L;

    private ActionHandler main;

    private static final String DEFAULT_OPTION="default";
    private static final String LOOSE_OPTION="loose";

    public MenuParser(ActionHandler main) {
        super("parser");
        this.main = main;

        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(DEFAULT_OPTION);
        item.addActionListener(this);
        this.add(item);
        group.add(item);
        item.setSelected(ParserModel.class.equals(FilterSettings.parserModelClass));
        item = new JRadioButtonMenuItem(LOOSE_OPTION);
        item.addActionListener(this);
        this.add(item);
        group.add(item);
        item.setSelected(LooseParserModel.class.equals(FilterSettings.parserModelClass));
    }

    @Override public void actionPerformed(ActionEvent e) {
        String selected = ((JRadioButtonMenuItem) e.getSource()) .getText();
        if (selected.equals(DEFAULT_OPTION)) {
            FilterSettings.parserModelClass = ParserModel.class;
        } else if (selected.equals(LOOSE_OPTION)) {
            FilterSettings.parserModelClass = LooseParserModel.class;
        } else {
            return;
        }
        main.recreate();
    }

}
