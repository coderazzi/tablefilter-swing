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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import net.coderazzi.filters.gui.CustomChoice;


abstract class ChoicesListModel extends AbstractListModel {

    private static final long serialVersionUID = 2233861800925325272L;
    private Format format;
    Comparator choicesComparator;
    Comparator stringComparator;
    
    public static ChoicesListModel getCustom(ChoicesListModel current, 
    		Format format, boolean renderedContent,
    		Comparator choicesComparator, Comparator stringComparator){
    	if (current!=null 
    			&& (renderedContent == (current instanceof RenderedChoicesListModel))
    			&& current.matches(format, choicesComparator, stringComparator)){
    		return current;
    	}
    	if (renderedContent){
    		return new RenderedChoicesListModel(choicesComparator, stringComparator);
    	}
    	if (stringComparator.equals(choicesComparator)){
    		return new StringChoicesListModel(format, stringComparator);
    	}
    	return new GenericChoicesListModel(format, choicesComparator, stringComparator);
    }

    public ChoicesListModel(Format format, Comparator choicesComparator, Comparator stringComparator) {
        this.format = format;
        this.choicesComparator = choicesComparator;
        this.stringComparator = stringComparator;
    }

    /** Clears all content (but ALL matcher). */
    public abstract void clearContent();

    public boolean matches(Format format, Comparator choicesComparator, Comparator stringComparator){
    	if (this.choicesComparator.equals(choicesComparator) && this.stringComparator.equals(stringComparator)){
    		return this.format==null? format==null : this.format.equals(format);
    	}
    	return false;
    }

    /**
     * Adds additional choices.<br>
     * If the content is text-based, the choices are converted into Strings, and
     * sorted; additionally, choices are also escaped.<br>
     * Otherwise, no sorting is performed, although duplicates are still
     * discarded
     *
     * @return  true if there are any changes after the operation
     */
    public boolean addContent(Collection addedContent, IChoicesParser parser) {
        boolean changed = false;
        for (Object o : addedContent) {
            String s = null;
            if (!(o instanceof CustomChoice)) {
                if (o == null) {
                    o = CustomChoice.MATCH_EMPTY;
                } else {
                    s = (format == null) ? o.toString() : format.format(o);
                    if (s.length() == 0) {
                        o = CustomChoice.MATCH_EMPTY;
                    } else {
                        s = parser.escapeChoice(s);
                    }
                }
            }
            changed = addContent(o, s) || changed;
        }

        if (changed) {
            fireContentsChanged(this, 0, getSize());
        }

        return changed;
    }


    /** @see  PopupComponent#selectBestMatch(Object, boolean) */
    public abstract ChoiceMatch getClosestMatch(Object hint, boolean exact);

    /**
     * Returns the text that could complete the given string<br>
     * The completion string is the larger string that matches all existing
     * options in this model or the added list.
     */
    public abstract String getCompletion(String base, List addedList);

    /**
     * Method invoked by addContent.
     *
     * @param   content
     * @param   repr
     *
     * @return
     */
    protected boolean addContent(Object content, String repr) {
        return false;
    }

    /**
     * Returns the text that could complete the given string<br>
     * The completion string is the larger string that matches all existing
     * options in this model or the added list.
     */
    protected String getCompletion(List       sortedContent,
                                   Object     baseAsContent,
                                   Comparator sortingComparator,
                                   List       unsortedContent,
                                   List       additionalUnsortedContent) {
        int pos = Collections.binarySearch(sortedContent, baseAsContent,
                sortingComparator);
        if (pos >= 0) {
            // exact match, do nothing else
            return "";
        }

        String base = baseAsContent.toString();
        String ret = base;
        int len = ret.length();
        int cs = sortedContent.size();
        pos = -pos - 1;
        if (pos < cs) {
            String use = sortedContent.get(pos).toString();
            // the position found should start with the base string.
            // if not, no choice start with it
            if (ChoiceMatch.getMatchingLength(base, use, stringComparator)
                    >= len) {
                ret = use;

                int maxLen = ret.length();
                while (++pos < cs) {
                    use = sortedContent.get(pos).toString();

                    int m = ChoiceMatch.getMatchingLength(ret, use,
                            stringComparator);
                    if (m < len) {
                        // options are sorted, as soon as one does not start
                        // with the base string, no one else will
                        break;
                    } else if (m == len) {
                        return "";
                    } else if (m < maxLen) {
                        maxLen = m;
                    }
                }

                ret = ret.substring(0, maxLen);
            }
        }

        for (int i = 1; i <= 2; i++) {
            List l = (i == 1) ? unsortedContent : additionalUnsortedContent;
            if (l != null) {
                for (Object o : l) {
                    String s = o.toString();
                    int m = ChoiceMatch.getMatchingLength(ret, s,
                            stringComparator);
                    if (m == len) {
                        if (ret != base) {
                            return ""; // exact match!
                        }

                        ret = s;
                    } else if (m > len) {
                        ret = ret.substring(0, m);
                    }
                }
            }
        }

        return ret.substring(len);
    }

    /** Creation of the Match, for text based, sorted content. */
    protected <T> T findOnSortedContent(ChoiceMatch base,
                                        List<T>     sortedContent,
                                        Comparator  sortingComparator,
                                        T           hint,
                                        boolean     fullMatch) {
        T found = null;
        int pos = Collections.binarySearch(sortedContent, hint,
                sortingComparator);
        if (pos >= 0) {
            // found it, do nothing else (it is exact)
            base.exact = true;
            base.index = pos;
            found = sortedContent.get(pos);
        } else if (!fullMatch) {
            String strStart = hint.toString();
            // try the two positions around
            int suggested = -pos - 1;
            if (suggested < sortedContent.size()) {
                T suggestion = sortedContent.get(suggested);
                int len = ChoiceMatch.getMatchingLength(strStart,
                        suggestion.toString(), stringComparator);
                if ((len > base.len) || (base.len == 0)) {
                    base.index = suggested;
                    found = suggestion;
                    base.len = len;
                }
            }
            // if suggested is in the custom choices, no need to try
            if (--suggested >= 0) {
                T suggestion = sortedContent.get(suggested);
                int len = ChoiceMatch.getMatchingLength(strStart,
                        suggestion.toString(), stringComparator);
                if ((len > base.len) || (base.len == 0)) {
                    base.index = suggested;
                    base.len = len;
                    found = suggestion;
                }
            }
        }

        return found;
    }


    /**
     * Comparator for collections containing {@link CustomChoice} instances:
     * those instances are sorted by their own precedence definitions, and have
     * precedence over the rest of instances in the collection
     */
    class CustomChoiceAwareComparator implements Comparator<Object> {

        @Override public int compare(Object o1, Object o2) {
            if (o1 instanceof CustomChoice) {
                if (o2 instanceof CustomChoice) {
                    CustomChoice c1 = (CustomChoice) o1;
                    CustomChoice c2 = (CustomChoice) o2;
                    int ret = c1.getPrecedence() - c2.getPrecedence();
                    if (ret == 0) {
                        ret = stringComparator.compare(c1.toString(),
                                c2.toString());
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
}
