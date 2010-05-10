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

import net.coderazzi.filters.parser.IFilterTextParser;

import java.awt.Color;


/**
 * Interface to be implemented by {@link ITableFilterEditor} instances that are based on a text
 * parser.
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public interface ITableFilterTextBasedEditor extends ITableFilterEditor {

    /**
     * Sets the parser used to parse the text.
     */
    public void setTextParser(IFilterTextParser parser);

    /**
     * Returns the used parser
     */
    public IFilterTextParser getTextParser();

    /**
     * Sets the foreground color to be used on error conditions
     */
    public void setErrorForeground(Color fg);

    /**
     * Sets the background color to be used on error conditions
     */
    public void setErrorBackground(Color bg);

    /**
     * Sets the foreground color to be used on no error conditions
     */
    public void setFilterForeground(Color fg);

    /**
     * Sets the background color to be used on no error conditions
     */
    public void setFilterBackground(Color bg);

}
