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
package net.coderazzi.filters.examples.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import net.coderazzi.filters.examples.utils.TestData.Flag;


public class TestTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -5453866058915361214L;

    public static final String NAME = "Name";
    public static final String TUTOR = "Tutor";
    public static final String MALE = "Male";
    public static final String COUNTRY = "Country";
    public static final String AGE = "Age";
    public static final String CLUB = "Club";
    public static final String LCNAME = "Nickname";
    public static final String DATE = "Date";
    public static final String NOTE = "Notes";
    public static final String HTML_COUNTRY = "Country (html)";

    public static String columnNames[] = {
            NAME, AGE, MALE, TUTOR, COUNTRY, 
            CLUB, LCNAME, DATE, NOTE, HTML_COUNTRY
        };
    private static final Class<?> columnTypes[] = {
            String.class, Integer.class, Boolean.class, TestData.Tutor.class,
            Flag.class, TestData.Club.class, String.class, 
            Date.class, String.class, String.class
        };

    private static final int BASIC_MODEL_COLUMNS = 5;
    private static final int LARGE_MODEL_COLUMNS = 9;
    private static final int FULL_MODEL_COLUMNS = 10;
    private static int expectedColumns = BASIC_MODEL_COLUMNS;
    private List<TestData> data;
    private Set<TestData> modifiedTestData = new HashSet<TestData>();
    private int columnsOrder[];

    public static void setLargeModel(boolean enable) {
        expectedColumns = enable ? LARGE_MODEL_COLUMNS : BASIC_MODEL_COLUMNS;
    }

    public static void setFullModel() {
        expectedColumns = FULL_MODEL_COLUMNS;
    }

    public static void setModelWidth(int w) {
        expectedColumns = w;
    }

    public static TestTableModel createTestTableModel(int elements) {
        return new TestTableModel(getTestData(elements));
    }


    private static List<TestData> getTestData(int elements) {
        TestData.resetRandomness();

        List<TestData> ltd = new ArrayList<TestData>();

        for (int i = 0; i < elements; i++) {
            ltd.add(new TestData());
        }

        return ltd;
    }


    public TestTableModel(List<TestData> data) {
        this.data = data;
        columnsOrder = getRandomOrder();
    }

    public void addTestData(TestData data) {
        this.data.add(0, data);
        fireTableRowsInserted(0, 0);
    }

    public TestData removeTestData() {
        TestData ret = null;
        if (data.size() > 0) {
            ret = data.remove(0);
            fireTableRowsDeleted(0, 0);
        }

        return ret;
    }

    public TestData updateTestData() {
        TestData ret = null;
        if (data.size() > 0) {
            ret = data.get(0);
            ret.male = !ret.male;
            fireTableRowsUpdated(0, 0);
        }

        return ret;
    }

    private int[] getRandomOrder() {
        Random random = new Random();
        int n[] = new int[columnNames.length];
        List<Integer> l = new ArrayList<Integer>();
        int prior = getColumnCount();
        n[0] = 0; // keep always first column as the name
        n[LARGE_MODEL_COLUMNS] = LARGE_MODEL_COLUMNS; // keep always html country as last one
        for (int i = 1; i < prior; i++) {
            l.add(new Integer(i));
        }

        for (int i = 1; i < prior; i++) {
            n[i] = l.remove(random.nextInt(l.size())).intValue();
        }

        for (int i = prior; i < LARGE_MODEL_COLUMNS; i++) {
            l.add(new Integer(i));
        }

        for (int i = prior; i < LARGE_MODEL_COLUMNS; i++) {
            n[i] = l.remove(random.nextInt(l.size())).intValue();
        }

        return n;
    }

    public void changeModel(JTable table) {

        // we keep the same order
        int newColumnsOrder[] = columnsOrder.clone();
        for (int i = 0; i < table.getColumnCount(); i++) {
            newColumnsOrder[i] = columnsOrder[table
                        .convertColumnIndexToModel(i)];
        }

        columnsOrder = newColumnsOrder;
        setLargeModel(expectedColumns == BASIC_MODEL_COLUMNS);
        fireTableStructureChanged();
    }

    public void shuffleModel() {
        columnsOrder = getRandomOrder();
        fireTableStructureChanged();
    }

    public TestData getRow(int row) {
        return data.get(row);
    }

    public void updateData(int rows) {
        data = getTestData(rows);
        fireTableDataChanged();
    }

    public int getColumn(String name) {
        for (int i = 0; i < columnsOrder.length; i++) {
            if (columnNames[columnsOrder[i]] == name) {
                return i;
            }
        }

        return -1;
    }

    public boolean isModified(TestData td) {
        return modifiedTestData.contains(td);
    }

    @Override public int getColumnCount() {
        return expectedColumns;
    }

    @Override public int getRowCount() {
        return data.size();
    }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        TestData td = data.get(rowIndex);

        switch (columnsOrder[columnIndex]) {

        case 0:
            return td.name;

        case 1:
            return td.age;

        case 2:
            return td.male;

        case 3:
            return td.tutor;

        case 4:
            return td.flag;

        case 5:
            return td.club;

        case 6:
            return td.firstName;

        case 7:
            return td.date;

        case 8:
            return td.note;
            
        case 9:
        	return td.htmlFlag;
        }

        return null;
    }

    @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnsOrder[columnIndex] == 2;
    }

    @Override public void setValueAt(Object value,
                                     int    rowIndex,
                                     int    columnIndex) {

        TestData td = data.get(rowIndex);
        Boolean set = (Boolean) value;
        if (td.male != set) {
            if (!modifiedTestData.add(td)) {
                modifiedTestData.remove(td);
            }

            td.male = set;
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnsOrder[columnIndex]];
    }

    @Override public String getColumnName(int column) {
        return columnNames[columnsOrder[column]];
    }

}
