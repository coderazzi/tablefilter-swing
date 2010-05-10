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
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import net.coderazzi.filters.gui.ITableFilterEditor;
import net.coderazzi.filters.gui.ITableFilterTextBasedEditor;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.editors.TableChoiceFilterEditor;
import net.coderazzi.filters.gui_tests.TestData.ExamInformation;
import net.coderazzi.filters.gui_tests.resources.Messages;
import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.IFilterTextParser;
import net.coderazzi.filters.parser.ITypeBuilder;
import net.coderazzi.filters.parser.generic.DateHandler;
import net.coderazzi.filters.parser.generic.FilterTextParser;
import net.coderazzi.filters.parser.re.REFilterTextParser;


public class AppTestProperties extends JFrame {

	private static final long serialVersionUID = 4521016108974569405L;

    JTable table;
    TestTableModel tableModel;
    TableFilterHeader filterHeader;

    public AppTestProperties() {
        super(Messages.getString("Tests.Title")); //$NON-NLS-1$
        tableModel = TestTableModel.createLargeTestTableModel(100);
        createGui();
        initGui();
        setTableRenderers();
        setChoiceRenderers();
    }

    private void createGui() {
        JPanel main = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();

        main.add(scrollPane, BorderLayout.CENTER);

        table = new JTable(tableModel);
        scrollPane.setViewportView(table);
        filterHeader = new TableFilterHeader(table);
        
        filterHeader.setFont(table.getFont().deriveFont(10f));

        getContentPane().add(main);
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

        int datesColumn = tableModel.getColumn(TestTableModel.DATE);

        if (tableModel.getColumnCount() > datesColumn) {
            table.getColumnModel().getColumn(table.convertColumnIndexToView(datesColumn)).setCellRenderer(
                DateHandler.getDefault().getTableCellRenderer());
        }
    }

    void setChoiceRenderers() {
        int countryColumn = tableModel.getColumn(TestTableModel.COUNTRY);

        if ((tableModel.getColumnCount() > countryColumn) &&
                (filterHeader.getFilterEditor(countryColumn) instanceof TableChoiceFilterEditor)) {
            ((TableChoiceFilterEditor)filterHeader.getFilterEditor(countryColumn)).setChoiceRenderer(new FlagRenderer());
        }

        int examensColumn = tableModel.getColumn(TestTableModel.EXAMENS);

        if ((tableModel.getColumnCount() > examensColumn) &&
                (filterHeader.getFilterEditor(examensColumn) instanceof TableChoiceFilterEditor)) {
            ((TableChoiceFilterEditor)filterHeader.getFilterEditor(examensColumn)).setChoices(ExamenChoice.ALL_EXAM_CHOICES);
        }

        examensColumn = tableModel.getColumn(TestTableModel.EXAMENSH);

        if ((tableModel.getColumnCount() > examensColumn) &&
                (filterHeader.getFilterEditor(examensColumn) instanceof TableChoiceFilterEditor)) {
            ((TableChoiceFilterEditor)filterHeader.getFilterEditor(examensColumn)).setChoices(ExamenChoice.ALL_EXAM_CHOICES);
        }

        int agesColumn = tableModel.getColumn(TestTableModel.AGE);

        if ((tableModel.getColumnCount() > agesColumn) &&
                (filterHeader.getFilterEditor(agesColumn) instanceof TableChoiceFilterEditor)) {
            ((TableChoiceFilterEditor)filterHeader.getFilterEditor(agesColumn)).setChoiceRenderer(
                new CenteredRenderer());
        }

        //We set the parser in name as a regular expression parser
        int nameColumn = tableModel.getColumn(TestTableModel.LCNAME);

        if (tableModel.getColumnCount() > nameColumn) {
            ITableFilterEditor editor = filterHeader.getFilterEditor(nameColumn);
            if (editor instanceof ITableFilterTextBasedEditor) {
                IFilterTextParser parser = new REFilterTextParser();
                ((ITableFilterTextBasedEditor) editor).setTextParser(parser);
            }
        }

        int datesColumn = tableModel.getColumn(TestTableModel.DATE);

        if ((tableModel.getColumnCount() > datesColumn) &&
                (filterHeader.getFilterEditor(datesColumn) instanceof TableChoiceFilterEditor)) {
            ((TableChoiceFilterEditor)filterHeader.getFilterEditor(datesColumn)).setChoiceRenderer(
                DateHandler.getDefault().getTableCellRenderer());
        }
    }


    private void initGui() {

		IFilterTextParser parser = (IFilterTextParser) filterHeader.getTextParser();

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
    
    static class PropertiesDialog extends JDialog{
    	public PropertiesDialog(String properties[]) {
    		super((JDialog)null, Messages.getString("TestProperties.Title"), true);
    		setContentPane(initGui(properties));
		}
    	
    	private JPanel initGui(String properties[]){
    		final PropertiesTableModel model = new  PropertiesTableModel(properties);
    		JPanel ret = new JPanel(new BorderLayout(0, 16));
    		JTable table = new JTable();
    		JScrollPane pane = new JScrollPane();
    		pane.setViewportView(table);
    		table.setModel(model);
    		ret.add(pane, BorderLayout.CENTER);
    		return ret;
    	}
    }
    
    
    static class PropertiesTableModel extends AbstractTableModel{
    	
    	String properties[][];
    	
    	public PropertiesTableModel(String properties[]){
    		this.properties=new String[properties.length][2];
    		for (int i=0;i<properties.length;i++){
    			this.properties[i][0]=properties[i];
    		}
    	}
    	    	
    	@Override
    	public String getColumnName(int column) {
    		return column==0? "Property" : "Value";
    	}
    	
    	@Override
    	public int getColumnCount() {
    		return 2;
    	}
    	
    	@Override
    	public int getRowCount() {
    		return properties.length;
    	}
    	
    	@Override
    	public Object getValueAt(int rowIndex, int columnIndex) {
    		return properties[rowIndex][columnIndex];
    	}
    	
    	@Override
    	public void setValueAt(Object value, int rowIndex, int columnIndex) {
    		String s=(String)value;
    		if (s!=null && s.length()==0){
    			s=null;
    		}
    		properties[rowIndex][columnIndex]=s;
			System.setProperty("net.coderazzi.filters."+properties[rowIndex][0], s);
    	}
    	
    	@Override
    	public Class<?> getColumnClass(int columnIndex) {
    		return String.class;
    	}
    	
    	@Override
    	public boolean isCellEditable(int rowIndex, int columnIndex) {
    		return columnIndex==1;
    	}
    }

    private static String ALL_EDITABLE_PROPERTIES[]={
    	"TextParser.BooleanFalse",
    	"TextParser.BooleanTrue",
        "TextParser.NullFilterText",
    	"TextParser.IgnoreCase",
        "TextParser.DateFormat",
    	"TextParser.CompareDatesAsRendered",
    	"TextParser.class",
    	"TableFilter.AutoSelection",
        "Header.Mode",
        "Header.Position",
        "ChoiceFilterEditor.EmptyValue",
        "ChoiceFilterEditor.NullValue"
    };


    public final static void main(String[] args) {
//      try {
//		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//	} catch (Exception ex) {
//		ex.printStackTrace();
//	}
	
	

    	PropertiesDialog dialog = new PropertiesDialog(ALL_EDITABLE_PROPERTIES);
    	dialog.setBounds(100, 100, 600, 300);
    	dialog.setVisible(true);


        AppTestProperties frame = new AppTestProperties();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setBounds(100, 100, 1000, 500);
        frame.setVisible(true);
    }
}
