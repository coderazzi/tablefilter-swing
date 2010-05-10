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
package com.byteslooser.filters.parser.generic;

import com.byteslooser.filters.parser.FilterTextParsingException;
import com.byteslooser.filters.parser.IFilterTextParser;
import com.byteslooser.filters.parser.IdentifierInfo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.RowFilter;


public class TestFilterTextParser {
    private static void print(FilterTextParser.FilterNode base, StringBuilder s) {

        if (base != null) {

            if (base.info == null) {

                if ((base.left == null) || (base.right == null))
                    throw new RuntimeException("F****ck!");

                s.append("( ").append(base.and ? "and " : "or ");
                print(base.left, s);
                s.append(" ");
                print(base.right, s);
                s.append(" )");
            } else {
                FilterTextParser.AbstractFilterLeaf leaf = base.getContent();
                s.append('{').append(leaf.toString()).append('}');
            }
        }
    }


    public final static void main(String[] args) throws FilterTextParsingException {
        String what = "A | B & ( ( id> 12 ) & E & (F) & G)";

        what = "def ~ pe\\(*rro";
        try {
            IFilterTextParser parser = new FilterTextParser();
            IdentifierInfo id = new IdentifierInfo("id", Integer.class, 0);
            IdentifierInfo ref = new IdentifierInfo("ref", String.class, 1);
            IdentifierInfo def = new IdentifierInfo("def", Boolean.class, 2);
            IdentifierInfo def2 = new IdentifierInfo("def2", Boolean.class, 3);
            List<IdentifierInfo> identifiers = new ArrayList<IdentifierInfo>();
            identifiers.add(id);
            identifiers.add(ref);
            identifiers.add(def);
            identifiers.add(def2);
            parser.setIdentifiers(identifiers);

            RowFilter g = parser.parseText(what, ref.filterPosition);
            StringBuilder sb = new StringBuilder();
            print((FilterTextParser.FilterNode) g, sb);
            System.out.println(sb.toString());
        } catch (FilterTextParsingException pex) {
            System.out.println("Error on position: " + pex.getPosition());
            throw pex;
        }
    }
}
