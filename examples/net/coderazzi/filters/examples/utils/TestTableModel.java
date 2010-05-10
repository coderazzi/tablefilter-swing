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
import java.util.List;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


public class TestTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -5453866058915361214L;

    public static final String NAME = "Name";
    public static final String TUTOR = "Tutor";
    public static final String MALE = "Male";
    public static final String COUNTRY = "Country";
    public static final String AGE = "Age";
    public static final String CLUB = "Club";
    public static final String LCNAME = "Kickname";
    public static final String DATE = "Date";

    private static String[] columnNames = {
            NAME, AGE, MALE, TUTOR, COUNTRY, CLUB, LCNAME, DATE
        };
    private static final Class<?>[] columnTypes = {
            String.class, Integer.class, Boolean.class, String.class, Icon.class,
            TestData.Club.class,
            String.class, Date.class
        };


    private static boolean changedModel;
    private List<TestData> data;
    private int[] columnsOrder;


    public static TestTableModel createTestTableModel() {
        return createTestTableModel(1000);
    }

    public static TestTableModel createTestTableModel(int elements) {
        TestData.resetRandomness();

        List<TestData> ltd = new ArrayList<TestData>();

        for (int i = 0; i < elements; i++)
            ltd.add(new TestData());

        return new TestTableModel(ltd);

    }


    public static TestTableModel createLargeTestTableModel(int elements) {
        changedModel = true;
        return createTestTableModel(elements);
    }


    public TestTableModel(List<TestData> data) {
        this.data = data;
        columnsOrder = getRandomOrder();
    }

    public void addTestData(TestData data) {
        this.data.add(0, data);
        fireTableRowsInserted(0, 0);
    }

    public void removeTestData() {
        if (data.size() > 0) {
            data.remove(0);
            fireTableRowsDeleted(0, 0);
        }
    }

    private int[] getRandomOrder() {
        Random random = new Random();
        int[] n = new int[columnNames.length];
        List<Integer> l = new ArrayList<Integer>();
        int prior = getColumnCount();
        n[0]=0; //keep always first column as the name
        for (int i = 1; i < prior; i++)
            l.add(new Integer(i));
        for (int i = 1; i < prior; i++)
            n[i] = l.remove(random.nextInt(l.size())).intValue();
        for (int i = prior; i < columnNames.length; i++)
            l.add(new Integer(i));
        for (int i = prior; i < columnNames.length; i++)
            n[i] = l.remove(random.nextInt(l.size())).intValue();

        return n;
    }

    public void changeModel(JTable table) {

        // we keep the same order
        int[] newColumnsOrder = columnsOrder.clone();
        for (int i = 0; i < table.getColumnCount(); i++) {
            newColumnsOrder[i] = columnsOrder[table.convertColumnIndexToModel(i)];
        }
        columnsOrder = newColumnsOrder;
        changedModel = !changedModel;
        fireTableStructureChanged();
    }

    public TestData getRow(int row) {
        return data.get(row);
    }

    public int getMaxColumnCount() {
        return columnNames.length;
    }

    public int getColumn(String name) {
        for (int i = 0; i < columnsOrder.length; i++)
            if (columnNames[columnsOrder[i]] == name)
                return i;

        return -1;
    }

    public int getColumnCount() {
        return changedModel ? columnNames.length : 5;
    }

    public int getRowCount() {
        return data.size();
    }

    public Object getValueAt(int rowIndex,
                             int columnIndex) {
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
        }

        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex,
                                  int columnIndex) {
        return columnsOrder[columnIndex] == 2;
    }

    @Override
    public void setValueAt(Object value,
                           int rowIndex,
                           int columnIndex) {

        data.get(rowIndex).male = (Boolean) value;
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnsOrder[columnIndex]];
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[columnsOrder[column]];
    }

}