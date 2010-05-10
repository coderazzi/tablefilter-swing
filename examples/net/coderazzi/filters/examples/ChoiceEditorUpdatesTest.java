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
package net.coderazzi.filters.gui_tests;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;

import net.coderazzi.filters.IFilterObservable;
import net.coderazzi.filters.IFilterObserver;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.TableFilterHeader.EditorMode;
import net.coderazzi.filters.gui.editors.ChoiceFilterEditor;
import net.coderazzi.filters.gui.editors.TableChoiceFilterEditor;
import net.coderazzi.filters.gui_tests.TestData.ExamInformation;
import net.coderazzi.filters.gui_tests.resources.Messages;
import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.ITypeBuilder;
import net.coderazzi.filters.parser.generic.FilterTextParser;


public class ChoiceEditorUpdatesTest extends JFrame implements ItemListener, IFilterObserver{

	private static final long serialVersionUID = 4521016104474569405L;

    JTable table;
    TestTableModel tableModel;
    TableFilterHeader filterHeader;
    JButton addMaleButton;
    JButton addFemaleButton;
    JButton removeButton;
    JTextField eventField = new JTextField();
    TableChoiceFilterEditor eventFocus;

    public ChoiceEditorUpdatesTest() {
        super(Messages.getString("ChoiceEditorUpdatesTest.Title")); //$NON-NLS-1$
        tableModel = TestTableModel.createTestTableModel(0);
        createGui();
        initGui();
        setTableRenderers();
        setChoiceRenderers();
        setupListeners();
        removeButton.setEnabled(tableModel.getRowCount()>0);
    }

    private void createGui() {
        JPanel main = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        JPanel south = new JPanel(new GridLayout(2, 1, 0, 16));
        JPanel southNorth = new JPanel(new BorderLayout(40, 0));
        JPanel southNorthLeft = new JPanel(new BorderLayout(10, 0));
        JPanel southSouth = new JPanel(new GridLayout(1, 3, 16, 0));

        main.add(scrollPane, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);
        southNorth.add(southNorthLeft, BorderLayout.WEST);
        southNorth.add(eventField, BorderLayout.CENTER);
        south.add(southNorth);
        south.add(southSouth);
        
        Object selections[]=new Object[tableModel.getColumnCount()+1];
        for (int i=0;i<tableModel.getColumnCount();i++){
        	selections[i+1]=new Integer(i);
        }
        selections[0]=new String("None");
        JComboBox columnSelector = new JComboBox(selections);
        columnSelector.addItemListener(this);

        table = new JTable(tableModel);
        scrollPane.setViewportView(table);
        filterHeader = new TableFilterHeader(table, EditorMode.CHOICE);
        addMaleButton = new JButton(Messages.getString("Tests.AddMale"));
        addFemaleButton = new JButton(Messages.getString("Tests.AddFemale"));
        removeButton = new JButton(Messages.getString("Tests.Remove"));
        southNorthLeft.add(new JLabel(Messages.getString("TestsChoices.EventsColumn")), BorderLayout.WEST);
        southNorthLeft.add(columnSelector, BorderLayout.EAST);
        south.setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));

        southSouth.add(addMaleButton);
        southSouth.add(addFemaleButton);
        southSouth.add(removeButton);


        filterHeader.setFont(table.getFont().deriveFont(10f));
        eventField.setEditable(false);

        getContentPane().add(main);
        pack();
    }
    
    @Override
    public void itemStateChanged(ItemEvent e) {
    	if (e.getStateChange()==ItemEvent.SELECTED){
			eventField.setText("");
    		if (eventFocus!=null){
    			//stop listening for events now
    			eventFocus.getFilterObservable().removeFilterObserver(this);
    		}
    		Object selection=e.getItem();
    		if (selection instanceof Integer){
    			int column=(Integer)selection;
    			eventFocus=(TableChoiceFilterEditor) filterHeader.getFilterEditor(column);
    			eventFocus.getFilterObservable().addFilterObserver(this);
    		} else {
    			eventFocus=null;
    		}
    	}
    }
    
    @Override
    public void filterUpdated(IFilterObservable obs, RowFilter newValue) {
    	StringBuilder sb = new StringBuilder();
    	Object selection = eventFocus.getSelectedItem();
    	if (selection==null){
    		sb.append("(null) selection");
    	}else if (selection==ChoiceFilterEditor.NO_FILTER){
        	sb.append("(no filter)");
    	} else {
    		sb.append("(").append(selection.getClass().getName()).append(") ");
    		sb.append(selection.toString());
    	}
    	eventField.setText(sb.toString());
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

        if (tableModel.getColumnCount() > countryColumn)  {
            filterHeader.setChoiceFilterEditor(countryColumn).setChoiceRenderer(new FlagRenderer());
        }

        int examensColumn = tableModel.getColumn(TestTableModel.EXAMENS);

        if (tableModel.getColumnCount() > examensColumn) {
            filterHeader.setChoiceFilterEditor(examensColumn).setChoices(ExamenChoice.ALL_EXAM_CHOICES);
        }

        examensColumn = tableModel.getColumn(TestTableModel.EXAMENSH);

        if (tableModel.getColumnCount() > examensColumn) {
            filterHeader.setChoiceFilterEditor(examensColumn).setChoices(ExamenChoice.ALL_EXAM_CHOICES);
        }

        int agesColumn = tableModel.getColumn(TestTableModel.AGE);

        if (tableModel.getColumnCount() > agesColumn) {
            filterHeader.setChoiceFilterEditor(agesColumn).setChoiceRenderer(
                new CenteredRenderer());
        }

    }


    private void initGui() {
        FilterTextParser parser = (FilterTextParser) filterHeader.getTextParser();

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
        addMaleButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addTestData(true);
                	removeButton.setEnabled(true);
                }
            });

        addFemaleButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addTestData(false);
                	removeButton.setEnabled(true);
                }
            });

        removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tableModel.removeTestData();
                    if (tableModel.getRowCount()==0){
                    	removeButton.setEnabled(false);
                    }
                }
            });

    }

    private void addTestData(boolean male) {
        TestData td = new TestData();
        td.male = male;
        tableModel.addTestData(td);
    }


    public final static void main(String[] args) {

        ChoiceEditorUpdatesTest frame = new ChoiceEditorUpdatesTest();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
