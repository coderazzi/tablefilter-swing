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

import net.coderazzi.filters.IFilterObservable;

import java.awt.Component;


/**
 * Interface to be implemented by any filter editor used by the {@link TableFilterHeader}
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public interface ITableFilterEditor {

    /**
     * Provides the GUI component associated to the given editor
     */
    public Component getComponent();

    /**
     * Provides the {@link net.coderazzi.filters.IFilterObservable} associated to the given
     * instance
     */
    public IFilterObservable getFilterObservable();

    /**
     * <p>Performs an update of the filter.</p>
     *
     * <p>Each editor defines its own semantics for this operation, but is always ensured that the
     * current filter is propagated to any observers.</p>
     */
    public void updateFilter();

    /**
     * Resets the filter. The exact semantics for this operation depend on the editor itself, but is
     * always ensured that this operation will remove any filtering currently performed by this
     * editor.
     */
    public void resetFilter();
    
    /**
     * <p>Sets the content of the filter.</p>
     * <p>The content's type must conform to the type expected by the editor</p>
     * @since version 2.0
     */
    public void setFilter(Object content);

    /**
     * <p>Returns the content of the filter.</p>
     * <p>The content's type conforms to the type expected by the editor</p>
     * @since version 2.0
     */
    public Object getFilter();
    
    /**
     * Returns the associated column in the table
     */
    public int getFilterPosition();
    
    /**
     * Sets the associated column in the table
     */
    public void setFilterPosition(int filterPosition);
    
    /**
     * Adds a new observer to the editor
     * @since version 2.0
     */
    public void addTableFilterObserver(ITableFilterEditorObserver observer);
    
    /**
     * Removes an observer from the editor
     * @since version 2.0
     */
    public void removeTableFilterObserver(ITableFilterEditorObserver observer);

    /**
     * <p>Detaches the editor from the header</p>
     * @since version 2.2
     */
    public void detach();
    
}
