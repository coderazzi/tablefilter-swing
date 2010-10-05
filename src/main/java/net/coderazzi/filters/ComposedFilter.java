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

package net.coderazzi.filters;

import java.util.HashSet;
import java.util.Set;


/**
 * <p>Abstract parent class to support the composition of multiple filters.</p>
 *
 * <p>The exact composition semantics (and / or / not) are not defined.</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
abstract public class ComposedFilter extends BaseFilter implements IFilterObserver {

    /** Set of associated IFilters */
    protected Set<IFilter> filters = new HashSet<IFilter>();

    /**
     * Constructor built up out of none or more 
     * {@link net.coderazzi.filters.IFilter} instances
     */
    protected ComposedFilter(IFilter... observables) {
        addFilter(observables);
    }

    /**
     * Detaches the instance from any observer
     */
    @Override public void detach() {
        super.detach();
        filters.clear();
    }

    /**
     * Subscribes one or more {@link net.coderazzi.filters.IFilter} instances to 
     * receive filter events from this composition filter.
     */
    public void addFilter(IFilter... filtersToAdd) {
        for (IFilter observable : filtersToAdd) {
            if (filters.add(observable)) {
                observable.addFilterObserver(this);
            }
        }
    }

    /**
     * Unsubscribes a {@link net.coderazzi.filters.IFilter} that was previously
     * subscribed to receive filter events
     */
    public void removeFilter(IFilter filter) {
        if (filters.remove(filter)) {
            reportFilterUpdatedToObservers();
        }
    }

    /**
     * Returns all {@link net.coderazzi.filters.IFilter} instances previously added.
     */
    public Set<IFilter> getFilterObservables() {
        return new HashSet<IFilter>(filters);
    }

    /**
     * @see  IFilterObserver#filterUpdated(IFilter)
     */
    @Override
	public void filterUpdated(IFilter producer) {
        reportFilterUpdatedToObservers();
    }

}
