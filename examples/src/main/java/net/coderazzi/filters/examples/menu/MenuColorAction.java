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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JMenuItem;

import net.coderazzi.filters.examples.ActionHandler;
import net.coderazzi.filters.gui.TableFilterHeader;


public class MenuColorAction extends JMenuItem implements ActionListener {

    private static final long serialVersionUID = -6772023653226757860L;

    private ActionHandler main;

    public enum Target {
        BACKGROUND, FOREGROUND, DISABLED_FOREGROUND, DISABLED_BACKGROUND,
        SELECTION_FOREGROUND, SELECTION_BACKGROUND, TEXT_SELECTION, GRID_COLOR,
        ERROR_COLOR, WARNING_COLOR
    }

    private Target color;

    private static String getStr(Target target) {
        return target.toString().toLowerCase().replace("_", " ");
    }


    public MenuColorAction(ActionHandler listener, Target color) {
        super(getStr(color) + " ...");
        this.main = listener;
        this.color = color;
        addActionListener(this);
    }

    @Override final public void actionPerformed(ActionEvent e) {
        Color ret = JColorChooser.showDialog(main.getJFrame(),
                "Select " + getStr(color), getColor(main.getFilterHeader()));
        if (ret != null) {
            setColor(main.getFilterHeader(), ret);
        }
    }

    private Color getColor(TableFilterHeader header) {
        switch (color) {

        case BACKGROUND:
            return header.getBackground();

        case FOREGROUND:
            return header.getForeground();

        case DISABLED_FOREGROUND:
            return header.getDisabledForeground();

        case DISABLED_BACKGROUND:
            return header.getDisabledBackground();

        case SELECTION_BACKGROUND:
            return header.getSelectionBackground();

        case SELECTION_FOREGROUND:
            return header.getSelectionForeground();

        case TEXT_SELECTION:
            return header.getTextSelectionColor();

        case GRID_COLOR:
            return header.getGridColor();

        case ERROR_COLOR:
            return header.getErrorForeground();

        case WARNING_COLOR:
            return header.getWarningForeground();
        }

        return null;
    }

    private void setColor(TableFilterHeader header, Color c) {
        switch (color) {

        case BACKGROUND:
            header.setBackground(c);

            break;

        case FOREGROUND:
            header.setForeground(c);

            break;

        case DISABLED_FOREGROUND:
            header.setDisabledForeground(c);

            break;

        case DISABLED_BACKGROUND:
            header.setDisabledBackground(c);

            break;

        case SELECTION_BACKGROUND:
            header.setSelectionBackground(c);

            break;

        case SELECTION_FOREGROUND:
            header.setSelectionForeground(c);

            break;

        case TEXT_SELECTION:
            header.setTextSelectionColor(c);

            break;

        case GRID_COLOR:
            header.setGridColor(c);

            break;

        case ERROR_COLOR:
            header.setErrorForeground(c);

            break;

        case WARNING_COLOR:
            header.setWarningForeground(c);

            break;
        }
    }

}
