/**
 * Author:  Luis M Pena  ( sen@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  sen@coderazzi.net
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

package net.coderazzi.filters.parser.generic;

import net.coderazzi.filters.parser.IFilterTextParser;
import net.coderazzi.filters.parser.IdentifierInfo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;


/**
 * Helper class, implementing funcionality commonly used, associated to the creation and usage of
 * {@link IFilterTextParser}
 *
 * @author  Luis M Pena - sen@coderazzi.net
 */
abstract public class TableFilterHelper {

    /**
     * Obtains the identifiers from the table headers
     */
    public static List<IdentifierInfo> extractIdentifiersFromTableColumnNames(TableModel model) {
        return extractIdentifiersFromTableColumnNames(model, model.getColumnCount());
    }

    /**
     * Obtains the identifiers from the table headers
     */
    public static List<IdentifierInfo> extractIdentifiersFromTableColumnNames(TableModel model,
        int columns) {
        List<IdentifierInfo> identifiers = new ArrayList<IdentifierInfo>();

        for (int i = 0; i < columns; i++) {
            identifiers.add(new IdentifierInfo(model.getColumnName(i), model.getColumnClass(i), i));
        }

        return identifiers;
    }

    /**
     * Creates a generic {@link FilterTextParser} that uses the identifiers provided by the passed
     * table model (as extracted from the table headers)
     */
    public static FilterTextParser createTextFilterParser(TableModel model) {
        FilterTextParser ret = new FilterTextParser();
        ret.setIdentifiers(extractIdentifiersFromTableColumnNames(model));

        return ret;
    }


}
