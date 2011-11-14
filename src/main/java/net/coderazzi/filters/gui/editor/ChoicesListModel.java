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

import java.text.Collator;
import java.text.Format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import net.coderazzi.filters.IParser;
import net.coderazzi.filters.gui.CustomChoice;


/**
 * <p>List model to handle the choices in the popup menu.</p>
 *
 * <p>When the user specifies a Renderer, choices are considered non-text;
 * otherwise, content is converted, if needed, to Strings, and sorted. This
 * class includes functionality to find the best match for a given string,
 * returning the choice that more closely matches the input. This functionality
 * works only for text input (i.e., no Renderer specified)</p>
 *
 * <p>The model handles also specifically {@link CustomChoice} instances, which
 * are placed at the beginning of the list, sorted by their own precedence.</p>
 *
 * <p>By design, it is not expected to have many {@link CustomChoice} instances
 * associated to an editor; searching for a custom choice from a given string is
 * done therefore linearly -with obvious performance drawbacks in cases with
 * many many custom choices-.</p>
 */
class ChoicesListModel extends AbstractListModel {

    private static final long serialVersionUID = 3523952153693100563L;
    private List content;
    private Format format;
    private int customChoices;
    boolean useFormatter;
    Comparator comparator;

    public ChoicesListModel() {
        this.content = new ArrayList();
        setStringContent(null, Collator.getInstance());
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
    public void clearContent() {
        int size = getSize();
        content.clear();
        content.add(CustomChoice.MATCH_ALL);
        customChoices = 1;
        fireIntervalRemoved(this, 1, size);
    }

    /** Returns true if the object is a valid choice (as object, or string). */
    public boolean isValidChoice(Object o) {
        return content.contains(o);
    }

    /** Returns the current choices. */
    public Collection<?> getChoices() {
        return content;
    }

    /** @see  PopupComponent#selectBestMatch(Object, boolean) */
    public ChoiceMatch getClosestMatch(Object hint, boolean exact) {
        return (useFormatter && (hint instanceof String))
            ? findOnSortedContent((String) hint, exact)
            : ChoiceMatch.findExactOnContent(content, hint);
    }

    /**
     * Returns the text that could complete the given string<br>
     * The completion string is the larger string that matches all existing
     * options in this model or the added list.
     */
    public String getCompletion(String base, List addedList) {
        int cs = content.size();
        int pos = Collections.binarySearch(content.subList(customChoices, cs),
                base, comparator);
        if (pos >= 0) {
            // exact match, do nothing else
            return "";
        }

        String ret = base;
        int len = base.length();
        pos = customChoices - pos - 1;
        if (pos < cs) {
            String use = content.get(pos).toString();
            // the position found should start with the base string.
            // if not, no choice start with it
            if (ChoiceMatch.getMatchingLength(base, use, comparator) >= len) {
                ret = use;

                int maxLen = ret.length();
                while (++pos < cs) {
                    use = content.get(pos).toString();

                    int m = ChoiceMatch.getMatchingLength(ret, use, comparator);
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

        List use[] = { content.subList(0, customChoices), addedList };
        for (List l : use) {
            for (Object o : l) {
                String s = o.toString();
                int m = ChoiceMatch.getMatchingLength(ret, s, comparator);
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

        return ret.substring(len);
    }

    /** Specifies that the content is to be handled as strings. */
    public boolean setStringContent(Format     format,
                                    Comparator stringComparator) {
        boolean ret = !useFormatter || (format != this.format)
                || (comparator != stringComparator);
        if (ret) {
            useFormatter = true;
            this.format = format;
            this.comparator = stringComparator;
            clearContent();
        }

        return ret;
    }

    /** Specifies that the content requires no conversion to strings. */
    public boolean setRenderedContent(Comparator classComparator) {
        boolean ret = useFormatter || (comparator != classComparator);
        if (ret) {
            useFormatter = false;
            this.format = null;
            this.comparator = classComparator;
            clearContent();
        }

        return ret;
    }

    /**
     * Adds additional choices.<br>
     * If the content is text-based, the choices are converted into Strings, and
     * sorted; if escapeParser is not null, choices are also escaped.<br>
     * Otherwise, no sorting is performed, although duplicates are still
     * discarded
     *
     * @return  true if there are any changes after the operation
     */
    public boolean addContent(Collection addedContent, IParser escapeParser) {
        boolean changed = false;
        for (Object o : addedContent) {
            if (!(o instanceof CustomChoice)) {
                if (o == null) {
                    o = CustomChoice.MATCH_EMPTY;
                } else if (useFormatter) {
                    String s = (format == null) ? o.toString()
                                                : format.format(o);
                    if (s.length() == 0) {
                        o = CustomChoice.MATCH_EMPTY;
                    } else if (escapeParser == null) {
                        o = s.trim();
                    } else {
                        o = escapeParser.escape(s);
                    }
                }
            }

            changed = addContent(o) || changed;
        }

        if (changed) {
            addContent(CustomChoice.MATCH_ALL);
            fireContentsChanged(this, 0, getSize());
        }

        return changed;
    }

    private boolean addContent(Object o) {
        // using the wrapper comparator to handle also CustomChoices
        int pos = Collections.binarySearch(content, o, wrapperComparator);
        if (pos < 0) {
            content.add(-1 - pos, o);
            if (o instanceof CustomChoice) {
                customChoices++;
            }

            return true;
        }

        return false;
    }

    /** Creation of the Match, for text based, sorted content. */
    private ChoiceMatch findOnSortedContent(String  strStart,
                                            boolean fullMatch) {
        ChoiceMatch ret;
        if (content.isEmpty()) {
            ret = new ChoiceMatch();
        } else {
            // search first among the custom choices
            ret = ChoiceMatch.findOnUnsortedContent(content, customChoices,
                    comparator, strStart, fullMatch);
            if (!ret.exact) {
                // not exact, search (exact) among the non custom choices too
                int pos = Collections.binarySearch(content.subList(
                            customChoices, content.size()), strStart,
                        comparator);
                if (pos >= 0) {
                    // found it, do nothing else (it is exact)
                    ret.exact = true;
                    ret.index = customChoices + pos;
                    ret.content = content.get(ret.index);
                } else if (!fullMatch) {
                    // try the two positions around
                    int suggested = customChoices - pos - 1;
                    if (suggested < content.size()) {
                        String suggestion = content.get(suggested).toString();
                        int len = ChoiceMatch.getMatchingLength(strStart,
                                suggestion, comparator);
                        if ((len > ret.len) || (ret.len == 0)) {
                            ret.index = suggested;
                            ret.len = len;
                        }
                    }
                    // if suggested is in the custom choices, no need to try
                    if (--suggested >= customChoices) {
                        String suggestion = content.get(suggested).toString();
                        int len = ChoiceMatch.getMatchingLength(strStart,
                                suggestion, comparator);
                        if ((len > ret.len) || (ret.len == 0)) {
                            ret.index = suggested;
                            ret.len = len;
                        }
                    }
                    ret.content = content.get(ret.index);
                }
            }
        }

        return ret;
    }

    private Comparator wrapperComparator = new Comparator() {
        @Override public int compare(Object o1, Object o2) {
            if (o1 instanceof CustomChoice) {
                if (o2 instanceof CustomChoice) {
                    CustomChoice c1 = (CustomChoice) o1;
                    CustomChoice c2 = (CustomChoice) o2;
                    int ret = c1.getPrecedence() - c2.getPrecedence();
                    if (ret == 0) {
                        if (useFormatter) {
                            // in this case, the comparator is string comparator
                            ret = comparator.compare(c1.toString(),
                                    c2.toString());
                        } else {
                            ret = o1.hashCode() - o2.hashCode();
                        }
                    }

                    return ret;
                }

                return -1;
            } else if (o2 instanceof CustomChoice) {
                return 1;
            }

            return comparator.compare(o1, o2);
        }
    };

}
