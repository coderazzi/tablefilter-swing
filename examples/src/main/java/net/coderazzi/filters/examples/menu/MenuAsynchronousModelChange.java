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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/** Change width or model directly. */
public class MenuAsynchronousModelChange extends AbstractMenuAction implements Runnable{

    private static final long serialVersionUID = 9137290045345048519L;

    public MenuAsynchronousModelChange(ActionHandler main) {
        super("Change number of rows asynchronously in 5 seconds", main);
    }

    @Override public void run() {
        Random rnd = new Random();
        int rows = main.getTableModel().getRowCount();
        if (rnd.nextBoolean()) {
            rows = Math.max(1, rows/4);
        } else {
            rows = rows * 5 / 4;
        }
        main.getTableModel().updateData(rows);
    }

    @Override public void actionPerformed(ActionEvent e) {
        new Thread(){
            @Override public void run() {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch(InterruptedException iex){}
                SwingUtilities.invokeLater(MenuAsynchronousModelChange.this);
            }
        }.start();
    }

}
