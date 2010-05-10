/**
 * Author:  Luis M Pena  ( byteslooser@gmail.com )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  byteslooser@gmail.com
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

package com.byteslooser.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.RowFilter;


/**
 * Commodity class implementing the interface {@link com.byteslooser.filters.IFilterObservable} on a
 * {@link javax.swing.RowFilter}
 *
 * @author  Luis M Pena - byteslooser@gmail.com
 */
abstract public class AbstractObservableRowFilter extends RowFilter implements IFilterObservable {

    /** The set of currently subscribed observers */
    protected Set<IFilterObserver> filterObservers = new HashSet<IFilterObserver>();

    /**
     * Detaches the instance from any observer/observable
     */
    public void detach() {
        filterObservers.clear();
    }

    /**
     * @see  IFilterObservable#addFilterObserver(IFilterObserver)
     */
    public void addFilterObserver(IFilterObserver observer) {
        filterObservers.add(observer);
    }

    /**
     * @see  IFilterObservable#removeFilterObserver(IFilterObserver)
     */
    public void removeFilterObserver(IFilterObserver observer) {
        filterObservers.remove(observer);
    }

    /**
     * Returns all the registered {@link IFilterObserver} instances
     */
    public Set<IFilterObserver> getFilterObservers() {
        return new HashSet<IFilterObserver>(filterObservers);
    }

    /**
     * Method to be called by subclasses to report to the observers that the filter has changed
     */
    protected void reportFilterUpdatedToObservers() {
        for (IFilterObserver obs : new ArrayList<IFilterObserver>(filterObservers))
            obs.filterUpdated(this, this);
    }
}
