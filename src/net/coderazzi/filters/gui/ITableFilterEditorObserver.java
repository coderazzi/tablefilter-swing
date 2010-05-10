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

/**
 * <p>A ITableFilterEditorObserver instance receives notifications when the associated 
 * {@link net.coderazzi.filters.gui.ITableFilterEditor} instance updates the held filter.
 * </p>
 * <p>Its is uniquely a commodity class, its functionality could be carried out
 * using the original {@link net.coderazzi.filters.IFilterObserver} interface
 * </p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 * @since version 2.0
 */
public interface ITableFilterEditorObserver {

    /**
     * <p>Notification made by the {@link net.coderazzi.filters.gui.ITableFilterEditor}
     * when the filter's content is updated</p>
     * @param editor the observable instance
     * @param newValue the new content of the editor. Its type depends on the
     * filter editor, {@link net.coderazzi.filters.gui.ITableFilterEditor#getFilter()}
     */
    public void tableFilterUpdated(ITableFilterEditor editor, Object newValue);
}
