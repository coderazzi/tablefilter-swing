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
package com.byteslooser.filters.gui_tests;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import com.byteslooser.filters.AndFilter;
import com.byteslooser.filters.NotFilter;
import com.byteslooser.filters.OrFilter;
import com.byteslooser.filters.TableFilter;
import com.byteslooser.filters.gui.editors.TableChoiceFilterEditor;
import com.byteslooser.filters.gui.editors.TextChoiceFilterEditor;
import com.byteslooser.filters.gui.editors.TextFilterEditor;
import com.byteslooser.filters.gui_tests.resources.Messages;
import com.byteslooser.filters.parser.generic.FilterTextParser;
import com.byteslooser.filters.parser.generic.TableFilterHelper;


public class AppTestWithSeparateComponents extends JFrame {

    private JTable table;
    private TestTableModel tableModel;
    private TableFilter tableFilter;
    private TextFilterEditor freeFilterBottom;
    private TextChoiceFilterEditor freeFilterTop;
    private TableChoiceFilterEditor choiceFilter;

    public AppTestWithSeparateComponents() {
        super(Messages.getString("TestComponents.Title"));
        createGui();
        initGui();
        setTableRenderers();
    }

    private void initGui() {
        tableModel = TestTableModel.createTestTableModel(100);
        table.setModel(tableModel);
        tableFilter = new TableFilter();
        tableFilter.setTable(table);

        FilterTextParser parser = TableFilterHelper.createTextFilterParser(tableModel);

        freeFilterTop.setTextParser(parser);
        freeFilterBottom.setTextParser(parser);
        choiceFilter.setTable(table, tableModel.getColumn(TestTableModel.MALE));

        tableFilter.addFilterObservable(new OrFilter(freeFilterTop.getFilterObservable(),
                new AndFilter(freeFilterBottom.getFilterObservable(),
                    new NotFilter(choiceFilter.getFilterObservable()))));
    }

    private void setTableRenderers() {
        int countryColumn = tableModel.getColumn(TestTableModel.COUNTRY);

        if (tableModel.getColumnCount() > countryColumn) {
            table.getColumnModel().getColumn(table.convertColumnIndexToView(countryColumn)).setCellRenderer(
                new FlagRenderer());
        }

        int agesColumn = tableModel.getColumn(TestTableModel.AGE);

        if (tableModel.getColumnCount() > agesColumn) {
            table.getColumnModel().getColumn(table.convertColumnIndexToView(agesColumn)).setCellRenderer(
                new CenteredRenderer());
        }

        int examensColumn = tableModel.getColumn(TestTableModel.EXAMENS);

        if (tableModel.getColumnCount() > examensColumn) {
            table.getColumnModel().getColumn(table.convertColumnIndexToView(examensColumn)).setCellRenderer(
                new CenteredRenderer());
        }
    }

    private void createGui() {
        JSplitPane mainPane = new JSplitPane();
        JPanel left = new JPanel(new BorderLayout());
        JPanel orPanel = new JPanel(new BorderLayout());
        JPanel andPanel = new JPanel(new BorderLayout());
        JPanel notPanel = new JPanel(new BorderLayout());
        JScrollPane pane = new JScrollPane();


        table = new JTable();
        freeFilterBottom = new TextFilterEditor();
        choiceFilter = new TableChoiceFilterEditor();
        freeFilterTop = new TextChoiceFilterEditor();

        pane.setViewportView(table);

        setFilterBorder(Messages.getString("TestTableModel.Male"), choiceFilter);
        setFilterBorder(Messages.getString("TestComponents.Not"), notPanel);
        setFilterBorder(Messages.getString("TestComponents.And"), andPanel);
        setFilterBorder(Messages.getString("TestComponents.Or"), orPanel);

        notPanel.add(choiceFilter, BorderLayout.CENTER);
        mainPane.setRightComponent(pane);
        mainPane.setLeftComponent(left);
        orPanel.add(freeFilterTop, BorderLayout.NORTH);
        orPanel.add(andPanel, BorderLayout.SOUTH);
        left.add(orPanel, BorderLayout.NORTH);
        andPanel.add(freeFilterBottom, BorderLayout.NORTH);
        andPanel.add(notPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPane);
        pack();
        mainPane.setDividerLocation(250);
        setSize(800, getSize().height);
    }

    private void setFilterBorder(String text, JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(text),
                BorderFactory.createEmptyBorder(0, 16, 0, 0)));
    }

    public final static void main(String[] args) {
        AppTestWithSeparateComponents frame = new AppTestWithSeparateComponents();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
