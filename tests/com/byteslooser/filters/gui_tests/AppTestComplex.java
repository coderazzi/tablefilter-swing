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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.byteslooser.filters.gui.ITableFilterEditor;
import com.byteslooser.filters.gui.ITableFilterTextBasedEditor;
import com.byteslooser.filters.gui.TableFilterHeader;
import com.byteslooser.filters.gui.editors.ChoiceFilterEditor;
import com.byteslooser.filters.gui.editors.TableChoiceFilterEditor;
import com.byteslooser.filters.gui.editors.TextFilterEditor;
import com.byteslooser.filters.gui_tests.TestData.ExamInformation;
import com.byteslooser.filters.gui_tests.resources.Messages;
import com.byteslooser.filters.parser.FilterTextParsingException;
import com.byteslooser.filters.parser.IFilterTextParser;
import com.byteslooser.filters.parser.ITypeBuilder;
import com.byteslooser.filters.parser.generic.FilterTextParser;
import com.byteslooser.filters.parser.generic.TableFilterHelper;
import com.byteslooser.filters.parser.re.REFilterTextParser;


public class AppTestComplex extends JFrame implements ListSelectionListener {

    static ExamenChoice[] ALL_EXAM_CHOICES = {
            new ExamenChoice(ExamenChoice.Fraction.ONE_FIFTH),
            new ExamenChoice(ExamenChoice.Fraction.TWO_FIFTH),
            new ExamenChoice(ExamenChoice.Fraction.THREE_FIFTH),
            new ExamenChoice(ExamenChoice.Fraction.FOUR_FIFTH),
            new ExamenChoice(ExamenChoice.Fraction.FIVE_FIFTH)
        };

    JTable table;
    TestTableModel tableModel;
    TableFilterHeader filterHeader;
    JComboBox modeComboBox;
    JCheckBox filterEnabler;
    JCheckBox caseIgnorer;
    JButton resetFilterButton;
    JButton resetModelButton;
    JTextField nameInfo;
    JTextField clubInfo;
    JTextField scienceInfo;
    JTextField humanitiesInfo;
    TextFilterEditor addedFilter;

    public AppTestComplex() {
        super(Messages.getString("TestAdvanced.Title")); //$NON-NLS-1$
        tableModel = createModel();
        createGui();
        initGui();
        setTableRenderers();
        setupListeners();
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            TestData data = getSelectedData();
            if (data == null) {
                nameInfo.setText("");
                clubInfo.setText("");
                humanitiesInfo.setText("");
                scienceInfo.setText("");
            } else {
                nameInfo.setText(data.firstName);
                clubInfo.setText(data.club.toString());

                TestData.ExamInformation exam = data.examensHumanities;
                humanitiesInfo.setText((exam == null) ? "" : exam.toString());
                exam = data.examensSciences;
                scienceInfo.setText((exam == null) ? "" : exam.toString());
            }
        }
    }

    private TestData getSelectedData() {
        int selected = table.getSelectedRow();
        if (selected == -1)
            return null;

        return tableModel.getRow(table.convertRowIndexToModel(selected));
    }

    private void createGui() {
        JPanel main = new JPanel(new BorderLayout());
        JPanel complexFilter = new JPanel(new BorderLayout(4, 0));
        JPanel tableAndFilterPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        JPanel south = new JPanel(new BorderLayout(0, 0));
        JPanel info = new JPanel(new GridLayout(2, 4, 10, 2));
        JPanel southButtonsPanel = new JPanel(new BorderLayout(10, 0));
        JPanel southLeft = new JPanel(new GridLayout(2, 2, 10, 16));
        JPanel southRight = new JPanel(new GridLayout(2, 1, 10, 16));

        main.add(tableAndFilterPanel, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);
        southButtonsPanel.add(southLeft, BorderLayout.WEST);
        southButtonsPanel.add(southRight, BorderLayout.EAST);
        south.add(info, BorderLayout.CENTER);
        south.add(southButtonsPanel, BorderLayout.EAST);

        table = new JTable(tableModel);
        scrollPane.setViewportView(table);
        filterHeader = new TableFilterHeader();
        filterHeader.setFilterBackground(Color.yellow);
        filterHeader.setFilterForeground(Color.blue);
        filterHeader.setErrorBackground(Color.black);
        filterHeader.setErrorForeground(Color.red);
        modeComboBox = new JComboBox();
        filterEnabler = new JCheckBox(Messages.getString("Tests.Enable"), true);
        caseIgnorer = new JCheckBox(Messages.getString("Tests.IgnoreCase"), false);
        resetFilterButton = new JButton(Messages.getString("Tests.ResetFilter"));
        resetModelButton = new JButton(Messages.getString("Tests.ResetModel"));
        southButtonsPanel.setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));

        addedFilter = new TextFilterEditor(filterHeader.getTextParser());
        complexFilter.add(new JLabel(Messages.getString("TestAdvanced.ComplexFilter")),
            BorderLayout.WEST);
        complexFilter.add(addedFilter, BorderLayout.CENTER);
        complexFilter.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        nameInfo = new JTextField(5);
        clubInfo = new JTextField(5);
        scienceInfo = new JTextField(5);
        humanitiesInfo = new JTextField(5);
        nameInfo.setEditable(false);
        clubInfo.setEditable(false);
        scienceInfo.setEditable(false);
        humanitiesInfo.setEditable(false);
        info.add(new JLabel(TestTableModel.LCNAME + ":"));
        info.add(nameInfo);
        info.add(new JLabel(TestTableModel.CLUB + ":"));
        info.add(clubInfo);
        info.add(new JLabel(TestTableModel.EXAMENSH + ":"));
        info.add(humanitiesInfo);
        info.add(new JLabel(TestTableModel.EXAMENS + ":"));
        info.add(scienceInfo);
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8))));

        tableAndFilterPanel.add(scrollPane, BorderLayout.CENTER);
        tableAndFilterPanel.add(filterHeader, BorderLayout.NORTH);
        tableAndFilterPanel.add(complexFilter, BorderLayout.SOUTH);

        southLeft.add(new JLabel(Messages.getString("Tests.EditorsMode")));
        southLeft.add(modeComboBox);
        southLeft.add(filterEnabler);
        southLeft.add(caseIgnorer);
        southRight.add(resetFilterButton);
        southRight.add(resetModelButton);


        filterHeader.setMode(TableFilterHeader.EditorMode.NULL);
        filterHeader.setTable(table);
        filterHeader.setFont(table.getFont().deriveFont(10f));

        getContentPane().add(main);
        pack();
    }


    void setTableRenderers() {
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

        examensColumn = tableModel.getColumn(TestTableModel.EXAMENSH);

        if (tableModel.getColumnCount() > examensColumn) {
            table.getColumnModel().getColumn(table.convertColumnIndexToView(examensColumn)).setCellRenderer(
                new CenteredRenderer());
        }
    }

    void setChoiceRenderers() {
        int countryColumn = tableModel.getColumn(TestTableModel.COUNTRY);

        if ((tableModel.getColumnCount() > countryColumn) &&
                (filterHeader.getFilterEditor(countryColumn) instanceof TableChoiceFilterEditor)) {
            filterHeader.setChoiceFilterEditor(countryColumn).setChoiceRenderer(new FlagRenderer());
        }

        int examensColumn = tableModel.getColumn(TestTableModel.EXAMENS);

        if ((tableModel.getColumnCount() > examensColumn) &&
                (filterHeader.getFilterEditor(examensColumn) instanceof TableChoiceFilterEditor)) {
            filterHeader.setChoiceFilterEditor(examensColumn).setChoices(ALL_EXAM_CHOICES);
        }

        examensColumn = tableModel.getColumn(TestTableModel.EXAMENSH);

        if ((tableModel.getColumnCount() > examensColumn) &&
                (filterHeader.getFilterEditor(examensColumn) instanceof TableChoiceFilterEditor)) {
            filterHeader.setChoiceFilterEditor(examensColumn).setChoices(ALL_EXAM_CHOICES);
        }

        int agesColumn = tableModel.getColumn(TestTableModel.AGE);

        if ((tableModel.getColumnCount() > agesColumn) &&
                (filterHeader.getFilterEditor(agesColumn) instanceof TableChoiceFilterEditor)) {
            filterHeader.setChoiceFilterEditor(agesColumn).setChoiceRenderer(
                new CenteredRenderer());
        }

        //We set the parser in name as a regular expression parser
        int nameColumn = tableModel.getColumn(TestTableModel.LCNAME);

        if (tableModel.getColumnCount() > nameColumn) {
            ITableFilterEditor editor = filterHeader.getFilterEditor(nameColumn);
            if (editor instanceof ITableFilterTextBasedEditor) {
                IFilterTextParser parser = new REFilterTextParser();
                parser.setIgnoreCase(caseIgnorer.isSelected());
                ((ITableFilterTextBasedEditor) editor).setTextParser(parser);
            }
        }

    }


    private void initGui() {

        filterHeader.getTableFilter().addFilterObservable(addedFilter.getFilterObservable());
        modeComboBox.setModel(new DefaultComboBoxModel(TableFilterHeader.EditorMode.values()));
        modeComboBox.addItem(Messages.getString("Tests.MixedMode"));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);

        FilterTextParser parser = (FilterTextParser) filterHeader.getTextParser();
        parser.setIdentifiers(TableFilterHelper.extractIdentifiersFromTableColumnNames(tableModel,
                tableModel.getMaxColumnCount()));

        parser.setTypeBuilder(TestData.ExamInformation.class, new ITypeBuilder() {
                Pattern p = Pattern.compile("\\s*(\\d+)\\s*/\\s*(\\d+)\\s*");

                public Object parse(String text) throws FilterTextParsingException {
                    Matcher m = p.matcher(text);

                    if (!m.matches())
                        return null;

                    TestData.ExamInformation info = new TestData.ExamInformation();
                    info.testsDone = Integer.valueOf(m.group(2)).intValue();
                    info.testsPassed = Integer.valueOf(m.group(1)).intValue();

                    return info;
                }
            });
        parser.setComparator(TestData.ExamInformation.class,
            new Comparator<TestData.ExamInformation>() {
                public int compare(ExamInformation o1, ExamInformation o2) {

                    if ((o1.testsDone == o2.testsDone) && (o1.testsPassed == o2.testsPassed))
                        return 0;

                    double f1 = (o1.testsDone == 0) ? 0 : (o1.testsPassed / (double) o1.testsDone);
                    double f2 = (o2.testsDone == 0) ? 0 : (o2.testsPassed / (double) o2.testsDone);

                    return (f2 > f1) ? -1 : 1;
                }
            });
    }

    private void setupListeners() {
        modeComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {

                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        Object s = e.getItem();
                        if (s instanceof TableFilterHeader.EditorMode) {
                            filterHeader.resetMode((TableFilterHeader.EditorMode) s);
                        } else {
                            createMixedHeaders();
                        }
                        setChoiceRenderers();
                    }
                }
            });

        filterEnabler.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    filterHeader.setEnabled(filterEnabler.isSelected());
                }
            });


        resetFilterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    filterHeader.resetFilters();
                }
            });

        resetModelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tableModel = createModel();
                    table.setModel(tableModel);
                    setTableRenderers();
                    setChoiceRenderers();
                }
            });

        caseIgnorer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    filterHeader.getTextParser().setIgnoreCase(caseIgnorer.isSelected());
                    filterHeader.updateFilter();
                }
            });

    }

    void createMixedHeaders() {
        //country->Choice
        //Male->Basic
        //Tutor->Slim
        //Name->None
        //Age->Basic
        // the rest-> not modified

        int countryColumn = tableModel.getColumn(TestTableModel.COUNTRY);
        int maleColumn = tableModel.getColumn(TestTableModel.MALE);
        int tutorColumn = tableModel.getColumn(TestTableModel.TUTOR);
        int nameColumn = tableModel.getColumn(TestTableModel.NAME);
        int ageColumn = tableModel.getColumn(TestTableModel.AGE);

        if (tableModel.getColumnCount() > countryColumn) {
            filterHeader.setChoiceFilterEditor(countryColumn);
        }

        if (tableModel.getColumnCount() > maleColumn) {
            filterHeader.setBasicFilterEditor(maleColumn);
        }

        if (tableModel.getColumnCount() > tutorColumn) {
            filterHeader.setSlimFilterEditor(tutorColumn);
        }

        if (tableModel.getColumnCount() > nameColumn) {
            filterHeader.setFilterEditor(nameColumn, null);
        }

        if (tableModel.getColumnCount() > ageColumn) {
            filterHeader.setBasicFilterEditor(ageColumn);
        }
    }


    TestTableModel createModel() {
        TestData.resetRandomness();

        List<TestData> ltd = new ArrayList<TestData>();

        for (int i = 0; i < 1000; i++)
            ltd.add(new TestData());

        return new TestTableModel(ltd);
    }


    public final static void main(String[] args) {

        try {
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        AppTestComplex frame = new AppTestComplex();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static class ExamenChoice implements ChoiceFilterEditor.IChoice {
        enum Fraction {
            ONE_FIFTH, TWO_FIFTH, THREE_FIFTH, FOUR_FIFTH, FIVE_FIFTH
        }

        Fraction f;

        ExamenChoice(Fraction f) {
            this.f = f;
        }

        public boolean matches(Object value) {
            TestData.ExamInformation info = (TestData.ExamInformation) value;

            if ((info == null) || (info.testsDone == 0))
                return true;

            double d = info.testsPassed / (double) info.testsDone;

            switch (f) {

            case ONE_FIFTH:
                return d <= .2;

            case TWO_FIFTH:
                return (d > .2) && (d <= .4);

            case THREE_FIFTH:
                return (d > .4) && (d <= .6);

            case FOUR_FIFTH:
                return (d > .6) && (d <= .8);

            case FIVE_FIFTH:
                return d > .8;
            }

            return false;
        }

        @Override public String toString() {

            switch (f) {

            case ONE_FIFTH:
                return "< 20/100";

            case TWO_FIFTH:
                return "< 40/100";

            case THREE_FIFTH:
                return "< 60/100";

            case FOUR_FIFTH:
                return "< 80/100";

            case FIVE_FIFTH:
                return "< 100/100";
            }

            return null;
        }
    }
}
