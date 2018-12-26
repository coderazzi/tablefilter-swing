/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 * <p>
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
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
import net.coderazzi.filters.examples.utils.EventsWindow;
import net.coderazzi.filters.examples.utils.TestData;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;


public class MenuUpdateThread extends JMenu implements ActionListener {

    private static final long serialVersionUID = -6772023112226757860L;
    private static String START_ACTION = "Start";
    private ActionHandler main;
    private static Timer timer;

    public MenuUpdateThread(ActionHandler main) {
        super("Update model thread");
        this.main = main;
        stopTimer();
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(START_ACTION);
        item.addActionListener(this);
        item.setSelected(false);
        group.add(item);
        this.add(item);
        item = new JRadioButtonMenuItem("Stop");
        item.addActionListener(this);
        item.setSelected(true);
        group.add(item);
        this.add(item);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // java.awt.event.ActionEvent[ACTION_PERFORMED,cmd=Start,when=1545779772107,modifiers=] on javax.swing.JRadioButtonMenuItem[,1,3,69x19,invalid,alignmentX=0.0,alignmentY=0.0,border=javax.swing.plaf.metal.MetalBorders$MenuItemBorder@342dfd4a,flags=264,maximumSize=,minimumSize=,preferredSize=,defaultIcon=,disabledIcon=,disabledSelectedIcon=,margin=javax.swing.plaf.InsetsUIResource[top=2,left=2,bottom=2,right=2],paintBorder=true,paintFocus=false,pressedIcon=,rolloverEnabled=false,rolloverIcon=,rolloverSelectedIcon=,selectedIcon=,text=Start]
        if (e.getActionCommand() == null) {
            updateModel();
        } else if (e.getActionCommand() == START_ACTION) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    private void updateModel() {
        main.getTableModel().addTestData(new TestData());
    }

    private void startTimer() {
        timer = new Timer(2000, this);
        timer.setRepeats(true);
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

}
