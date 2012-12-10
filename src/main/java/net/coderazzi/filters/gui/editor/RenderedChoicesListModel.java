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

package net.coderazzi.filters.gui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import net.coderazzi.filters.gui.CustomChoice;


/**
 * <p>List model to handle the choices in the popup menu, when they are rendered
 * (no string conversion).</p>
 */
class RenderedChoicesListModel extends ChoicesListModel {

    private static final long serialVersionUID = 1454276653556109664L;

    /**
     * The content includes a number of {@link CustomChoice} instances, and then
     * the rest of the content, sorted by the provided comparator.
     */
    private List content;
    private CustomChoiceAwareComparator comparator;

    public RenderedChoicesListModel(Comparator classComparator,
                                    Comparator stringComparator) {
        super(null, classComparator, stringComparator);
        this.content = new ArrayList();
        this.comparator = new CustomChoiceAwareComparator();
        clearContent();
    }

    /** {@link AbstractListModel} interface. */
    @Override public int getSize() {
        return content.size();
    }

    /** {@link AbstractListModel} interface. */
    @Override public Object getElementAt(int i) {
        return content.get(i);
    }

    /** Clears all content (but ALL matcher). */
    @Override public void clearContent() {
        int size = getSize();
        content.clear();
        content.add(CustomChoice.MATCH_ALL);
        fireIntervalRemoved(this, 1, size);
    }

    @Override public String getCompletion(String base, List addedList) {
        return ""; // no completion required (content is not used as text)
    }

    @Override public ChoiceMatch getClosestMatch(Object hint, boolean exact) {
        return ChoiceMatch.findExactOnContent(content, hint);
    }

    @Override public boolean addContent(Collection     addedContent,
                                        IChoicesParser parser) {
        boolean changed = false;
        for (Object o : addedContent) {
            if (o == null) {
                o = CustomChoice.MATCH_EMPTY;
            }

            changed = addContent(o) || changed;
        }

        if (changed) {
            fireContentsChanged(this, 0, getSize());
        }

        return changed;
    }

    private boolean addContent(Object o) {
        // using the wrapper comparator to handle also CustomChoices
        int pos = Collections.binarySearch(content, o, comparator);
        if (pos < 0) {
            content.add(-1 - pos, o);
            return true;
        }

        return false;
    }
}
