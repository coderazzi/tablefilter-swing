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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import net.coderazzi.filters.examples.ActionHandler;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.TableFilterHeader.Position;


public class MenuPosition extends JMenu implements ActionListener {

    private static final long serialVersionUID = -6772023653226757860L;
    private static final Map<String, Position> options =
        new HashMap<String, Position>();

    private ActionHandler main;
    private JPanel filterHeaderPanel;
    private JPanel tablePanel;

    public MenuPosition(ActionHandler main, JPanel tablePanel) {
        super("position");
        this.main = main;
        this.tablePanel = tablePanel;
        options.put("top (automatic)", Position.TOP);
        options.put("inline (automatic)", Position.INLINE);
        options.put("bottom (manual)", Position.NONE);

        Position select = main.getFilterHeader().getPosition();
        ButtonGroup group = new ButtonGroup();
        for (Map.Entry<String, Position> s : options.entrySet()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(s.getKey());
            item.addActionListener(this);
            this.add(item);
            group.add(item);
            if (select == s.getValue()) {
                item.setSelected(true);
            }
        }

        setPosition(select);
    }

    @Override public void actionPerformed(ActionEvent e) {
        Position p = options.get(((JRadioButtonMenuItem) e.getSource())
                    .getText());
        if (p != null) {
            setPosition(p);
        }
    }

    private void setPosition(Position position) {
        TableFilterHeader filterHeader = main.getFilterHeader();
        if ((filterHeader.getPosition() == Position.NONE)
                && (filterHeaderPanel != null)) {
            filterHeaderPanel.remove(filterHeader);
            tablePanel.remove(filterHeaderPanel);
            tablePanel.revalidate();
        }

        filterHeader.setPosition(position);
        if (filterHeader.getPosition() == Position.NONE) {
            filterHeaderPanel = new JPanel(new BorderLayout());
            filterHeaderPanel.add(filterHeader, BorderLayout.CENTER);
            filterHeaderPanel.setBorder(BorderFactory.createLineBorder(
                    filterHeader.getDisabledForeground(), 1));
            tablePanel.add(filterHeaderPanel, BorderLayout.SOUTH);
            tablePanel.revalidate();
        }
    }


}
