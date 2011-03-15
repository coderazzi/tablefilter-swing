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

import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.coderazzi.filters.examples.ActionHandler;


public class MenuLookAndFeel extends JMenu implements ActionListener {

    private static final long serialVersionUID = -6772023653226757860L;

    private ActionHandler main;
    private Map<String, String> lookMap = new HashMap<String, String>();

    public MenuLookAndFeel(ActionHandler main) {
        super("Look And Feel");
        this.main = main;

        LookAndFeel now = UIManager.getLookAndFeel();
        ButtonGroup group = new ButtonGroup();

        for (LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()) {
            lookMap.put(lfi.getName(), lfi.getClassName());

            JRadioButtonMenuItem item = new JRadioButtonMenuItem(lfi.getName());
            item.addActionListener(this);
            group.add(item);
            this.add(item);
            if (lfi.getName().equals(now.getName())) {
                item.setSelected(true);
            }
        }

        if (group.getButtonCount() < 2) {
            this.setEnabled(false);
        }
    }

    @Override public void actionPerformed(ActionEvent e) {
        String className = lookMap.get(((JRadioButtonMenuItem) e.getSource())
                    .getText());
        if (className != null) {
            try {
                UIManager.setLookAndFeel(className);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
            }
        }

        SwingUtilities.updateComponentTreeUI(main.getJFrame());
        main.getJFrame().pack();
    }

}
