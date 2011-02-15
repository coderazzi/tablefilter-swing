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

package net.coderazzi.filters.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;


/** Class representing the current {@link TableFilterHeader} appearance. */
public class Look {
    Color foreground;
    Color background;
    Color selectionForeground;
    Color selectionBackground;
    Color disabledForeground;
    Color disabledBackground;
    Color errorForeground;
    Color textSelection;
    Color gridColor;
    Font font;
    int maxVisiblePopupRows = FilterSettings.maxVisiblePopupRows;
    CustomChoiceDecorator customChoiceDecorator = FilterSettings
            .newCustomChoiceDecorator();

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }

    public Color getSelectionForeground() {
        return selectionForeground;
    }

    public Color getSelectionBackground() {
        return selectionBackground;
    }

    public Color getDisabledForeground() {
        return disabledForeground;
    }

    public Color getDisabledBackground() {
        return disabledBackground;
    }

    public Color getErrorForeground() {
        return errorForeground;
    }

    public Color getTextSelection() {
        return textSelection;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public Font getFont() {
        return font;
    }

    public int getMaxVisiblePopupRows() {
        return maxVisiblePopupRows;
    }

    public CustomChoiceDecorator getCustomChoiceDecorator() {
        return customChoiceDecorator;
    }

    /**
     * Prepares the provided component to have the expected appearance<br>
     * Only the background, foreground and font are updated.
     */
    public void setupComponent(Component c,
                               boolean   isSelected,
                               boolean   isEnabled) {
        Color bg;
        Color fg;
        if (isEnabled) {
            if (isSelected) {
                bg = getSelectionBackground();
                fg = getSelectionForeground();
            } else {
                bg = getBackground();
                fg = getForeground();
            }
        } else {
            bg = getDisabledBackground();
            fg = getDisabledForeground();
        }

        if (bg != c.getBackground()) {
            c.setBackground(bg);
        }

        if (fg != c.getForeground()) {
            c.setForeground(fg);
        }

        if (c.getFont() != getFont()) {
            c.setFont(getFont());
        }
    }

}
