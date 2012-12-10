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
import java.util.TreeSet;

import javax.swing.AbstractListModel;

import net.coderazzi.filters.gui.CustomChoice;


/**
 * <p>Most general list model to handle the choices in the popup menu: it can
 * handle any type -included Strings-, but is not suited for Rendered
 * content.</p>
 */
class GenericChoicesListModel extends ChoicesListModel {

    private static final long serialVersionUID = -8838108372475174432L;

    /** the content, always sorted -by classComparator- */
    private TreeSet<Wrapper> contentSet;

    /**
     * the contentSet, as a more convenient list. It becomes null when new
     * content is added, and only if needed -because the user interacts with
     * this editor-, this list is recreated.
     */
    private List<Wrapper> contentList;

    /** the contentSet, sorted alphabetically in a list. */
    private List<Wrapper> contentListAlphaSorted;

    /** the comparator to search text on the wrappers. */
    private Comparator wrapperTextComparator;

    public GenericChoicesListModel(Format     format,
                                   Comparator choicesComparator,
                                   Comparator stringComparator) {
        super(format, choicesComparator, stringComparator);
        this.contentSet = new TreeSet<Wrapper>(new WrapperComparator(
                    choicesComparator, stringComparator));
        this.wrapperTextComparator = new WrapperTextComparator(
                stringComparator);
        clearContent();
    }

    /** {@link AbstractListModel} interface. */
    @Override public int getSize() {
        return contentSet.size();
    }

    /** {@link AbstractListModel} interface. */
    @Override public Object getElementAt(int i) {
        Wrapper ret = getContentAsList().get(i);
        return (ret.o instanceof CustomChoice) ? ret.o : ret.s;
    }

    /** Clears all content (but ALL matcher). */
    @Override public void clearContent() {
        int size = getSize();
        contentSet.clear();
        addContent(CustomChoice.MATCH_ALL,
            CustomChoice.MATCH_ALL.getRepresentation());
        fireIntervalRemoved(this, 1, size);
    }

    /** @see  PopupComponent#selectBestMatch(Object, boolean) */
    @Override public ChoiceMatch getClosestMatch(Object hint, boolean exact) {
        // hint could be a CustomChoice or a String
        if (hint instanceof CustomChoice) {
            CustomChoice cc = (CustomChoice) hint;
            Wrapper w = new Wrapper(cc, cc.getRepresentation());
            if (contentSet.contains(w)) {
                ChoiceMatch ret = new ChoiceMatch();
                ret.content = w.o;
                ret.exact = true;
                ret.index = getContentAsList().indexOf(w);
                ret.len = w.s.length();
                return ret;
            }

            hint = w.s;
        }

        return findOnSortedContent((String) hint, exact);
    }

    /** @see  ChoicesListModel#getCompletion(String, List) */
    @Override public String getCompletion(String base, List addedList) {
        return getCompletion(getAlphabeticallySorteredContent(),
                new Wrapper(null, base), wrapperTextComparator, null,
                addedList);
    }

    @Override protected boolean addContent(Object o, String s) {
        if (o instanceof CustomChoice) {
            s = ((CustomChoice) o).getRepresentation();
        }
        boolean ret = contentSet.add(new Wrapper(o, s));
        if (ret) {
            contentList = contentListAlphaSorted = null;
        }

        return ret;
    }

    private List<Wrapper> getContentAsList() {
        if (contentList == null) {
            contentList = new ArrayList<Wrapper>(contentSet);
        }

        return contentList;
    }

    private List<Wrapper> getAlphabeticallySorteredContent() {
        if (contentListAlphaSorted == null) {
            contentListAlphaSorted = new ArrayList<Wrapper>(contentSet);
            Collections.sort(contentListAlphaSorted, wrapperTextComparator);
        }

        return contentListAlphaSorted;
    }

    private ChoiceMatch findOnSortedContent(String start, boolean fullMatch) {
        ChoiceMatch ret = new ChoiceMatch();
        Wrapper found = findOnSortedContent(ret,
                getAlphabeticallySorteredContent(), wrapperTextComparator,
                new Wrapper(null, start), fullMatch);
        if (found != null) {
            ret.index = getContentAsList().indexOf(found);
            ret.content = (found.o instanceof CustomChoice) ? found.o : found.s;
        }

        return ret;
    }

    /** Comparator aware of the CustomChoice, suited for Wrapper instances. */
    static class WrapperComparator implements Comparator<Wrapper> {

        private Comparator choicesComparator;
        private Comparator stringComparator;

        public WrapperComparator(Comparator choicesComparator,
                                 Comparator stringComparator) {
            this.choicesComparator = choicesComparator;
            this.stringComparator = stringComparator;
        }

        @Override public int compare(Wrapper w1, Wrapper w2) {
            Object o1 = w1.o;
            Object o2 = w2.o;
            if (o1 instanceof CustomChoice) {
                if (o2 instanceof CustomChoice) {
                    CustomChoice c1 = (CustomChoice) o1;
                    CustomChoice c2 = (CustomChoice) o2;
                    int ret = c1.getPrecedence() - c2.getPrecedence();
                    if (ret == 0) {
                        // in this case, the comparator is string comparator
                        ret = stringComparator.compare(w1.s, w2.s);
                    }

                    return ret;
                }

                return -1;
            } else if (o2 instanceof CustomChoice) {
                return 1;
            }
            return choicesComparator.compare(o1, o2);
        }
    }

    /** Comparator to compare Wrappers by their string member. */
    static class WrapperTextComparator implements Comparator<Wrapper> {

        private Comparator stringComparator;

        public WrapperTextComparator(Comparator stringComparator) {
            this.stringComparator = stringComparator;
        }

        @Override public int compare(Wrapper w1, Wrapper w2) {
            return stringComparator.compare(w1.s, w2.s);
        }
    }

    /** The class that wraps the instances in the list model. */
    static class Wrapper {
        Object o;
        String s;

        Wrapper(Object o, String s) {
            this.o = o;
            this.s = s;
            if (s == null) {
                System.out.println("Arg " + o);
            }
        }

        @Override public String toString() {
            return s;
        }

        @Override public int hashCode() {
            return o.hashCode();
        }

        @Override public boolean equals(Object obj) {
            Wrapper w = (Wrapper) obj;
            return (o == null) ? s.equals(w.s) : o.equals(w.o);
        }
    }

}
