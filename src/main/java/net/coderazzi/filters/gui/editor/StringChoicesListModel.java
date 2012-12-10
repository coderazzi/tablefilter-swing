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

import java.text.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import net.coderazzi.filters.gui.CustomChoice;


/**
 * <p>List model to handle the choices in the popup menu when those choices are
 * handled as strings, sorted alphabetically.</p>
 */
class StringChoicesListModel extends ChoicesListModel {

    private static final long serialVersionUID = 3523952153693100563L;

    /**
     * The content includes a number -defined by customChoices- of {@link
     * CustomChoice} instances, and then the strings, sorted alphabetically.
     */
    private List content = new ArrayList();
    private int customChoices;
    private Comparator ccComparator; // comparator supporting CustomChoices


    StringChoicesListModel(Format format, Comparator stringComparator) {    	
        super(format, stringComparator, stringComparator);
        this.ccComparator = new CustomChoiceAwareComparator();
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
        addContent(CustomChoice.MATCH_ALL,
            CustomChoice.MATCH_ALL.getRepresentation());
        customChoices = 1;
        fireIntervalRemoved(this, 1, size);
    }

    /** @see  PopupComponent#selectBestMatch(Object, boolean) */
    @Override public ChoiceMatch getClosestMatch(Object hint, boolean exact) {
        if (hint instanceof String) {
            String start = (String) hint;
            // search first on the custom choices (unsorted)
            ChoiceMatch ret = ChoiceMatch.findOnUnsortedContent(content,
                    customChoices, stringComparator, start, exact);
            if (!ret.exact) {
                // if no perfect match, find best match on the rest of content
                Object alternative = findOnSortedContent(ret,
                        content.subList(customChoices, content.size()),
                        stringComparator, start, exact);
                if (alternative != null) {
                    ret.index += customChoices;
                    ret.content = alternative.toString();
                }
            }

            return ret;
        }
        // if hint is not a string, should be a CustomChoice
        return ChoiceMatch.findExactOnContent(content.subList(0, customChoices),
                hint);

    }

    /** @see  ChoicesListModel#getCompletion(String, List) */
    @Override public String getCompletion(String base, List addedList) {
        int cs = content.size();
        return getCompletion(content.subList(customChoices, cs), // (sorted)
                base,                          // the element in the content to
                                               // find
                stringComparator,              // the comparator to use
                content.subList(0, customChoices), // (unsorted)
                addedList);
    }

    @Override protected boolean addContent(Object o, String s) {
        // using the wrapper comparator to handle also CustomChoices
    	Object add = (o instanceof CustomChoice)? o : s;
        int pos = Collections.binarySearch(content, add, ccComparator);
        if (pos < 0) {
            content.add(-1 - pos, add);
            if (o instanceof CustomChoice) {
                customChoices++;
            }
            return true;
        }
        return false;
    }

}
