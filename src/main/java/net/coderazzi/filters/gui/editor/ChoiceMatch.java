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

import java.util.Comparator;
import java.util.List;


/** Class to find matches in the lists (history / choices). */
class ChoiceMatch {
    // index in the list. Will be -1 if the content is empty or a fullMatch
    // is required and cannot be found
    int index;
    // length of the string that is matched. On fullMatch calls, this length
    // is the passed string's length
    int len;
    // exact is true if the given index corresponds to an string that fully
    // matches the passed string
    boolean exact;
    // the matched content
    Object content;

    public ChoiceMatch() {
        this.index = -1;
    }

    /** Finds the content to match in a list; match must be exact. */
    public static ChoiceMatch findExactOnContent(List list, Object match) {
        ChoiceMatch ret = new ChoiceMatch();
        ret.index = list.indexOf(match);
        if (ret.index != -1) {
            ret.exact = true;
            ret.content = match;
        }

        return ret;
    }

    /**
     * Finds the content to match in a list which can be unsorted<br>
     * The match does not have to be exact.
     */
    public static ChoiceMatch findOnUnsortedContent(List       content,
                                                    int        len,
                                                    Comparator strComparator,
                                                    String     strStart,
                                                    boolean    fullMatch) {
        int strLen = strStart.length();
        ChoiceMatch ret = new ChoiceMatch();
        while (len-- > 0) {
            ret.content = content.get(len);

            String str = ret.content.toString();
            int matchLen = getMatchingLength(strStart, str, strComparator);
            if ((matchLen > ret.len) || (ret.len == 0)) {
                ret.index = len;
                ret.len = matchLen;
                if ((matchLen == strLen) && (str.length() == strLen)) {
                    ret.exact = true;
                    return ret;
                }
            }
        }

        if (fullMatch) {
            ret.index = -1;
            ret.len = 0;
            ret.content = null;
        }

        return ret;
    }

    /** Returns the number of matching characters between two strings. */
    public static int getMatchingLength(String     a,
                                        String     b,
                                        Comparator stringComparator) {
        int max = Math.min(a.length(), b.length());
        for (int i = 0; i < max; i++) {
            char f = a.charAt(i);
            char s = b.charAt(i);
            if ((f != s)
                    && (stringComparator.compare(String.valueOf(f),
                            String.valueOf(s)) != 0)) {
                return i;
            }
        }

        return max;
    }

}
