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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.Date;
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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import net.coderazzi.filters.gui.ITableFilterEditor;
import net.coderazzi.filters.gui.ITableFilterHeaderObserver;
import net.coderazzi.filters.gui.ITableFilterTextBasedEditor;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.TableFilterHeader.EditorMode;
import net.coderazzi.filters.gui.editors.ChoiceFilterEditor;
import net.coderazzi.filters.gui.editors.TableChoiceFilterEditor;
import net.coderazzi.filters.gui_tests.TestData.ExamInformation;
import net.coderazzi.filters.gui_tests.resources.Messages;
import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.IFilterTextParser;
import net.coderazzi.filters.parser.ITypeBuilder;
import net.coderazzi.filters.parser.generic.DateHandler;
import net.coderazzi.filters.parser.generic.FilterTextParser;
import net.coderazzi.filters.parser.re.REFilterTextParser;


public class AppTestMain extends JFrame {

	private static final long serialVersionUID = 4521016104474569405L;

    JTable table;
    TestTableModel tableModel;
    TableFilterHeader filterHeader;
    JComboBox modeComboBox;
    JComboBox positionComboBox;
    JCheckBox filterEnabler;
    JCheckBox caseIgnorer;
    JCheckBox exactDateChecker;
    JButton changeTableButton;
    JButton resetFilterButton;
    JButton resetModelButton;
    JButton addMaleButton;
    JButton addFemaleButton;
    JButton removeButton;
    JTextArea eventsLog;

    public AppTestMain() {
        super(Messages.getString("Tests.Title")); //$NON-NLS-1$
        tableModel = TestTableModel.createTestTableModel();
        createGui();
        initGui();
        setTableRenderers();
        setupListeners();
        modeComboBox.setSelectedItem(EditorMode.CHOICE);
    }

    private void createGui() {
        JPanel main = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        JPanel south = new JPanel(new BorderLayout(16, 16));
        JPanel configPanel = new JPanel(new GridLayout(5, 2, 0, 0));
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 3, 20, 16));
        JPanel statusPanel = new JPanel(new BorderLayout());

        main.add(scrollPane, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);
        controlPanel.add(statusPanel, BorderLayout.CENTER);
        controlPanel.add(configPanel, BorderLayout.EAST);
        south.add(controlPanel, BorderLayout.NORTH);
        south.add(buttonsPanel, BorderLayout.SOUTH);

        table = new JTable(tableModel);
        scrollPane.setViewportView(table);
        filterHeader = new TableFilterHeader(table);
        eventsLog = new JTextArea();
        modeComboBox = new JComboBox();
        positionComboBox = new JComboBox();
        filterEnabler = new JCheckBox(Messages.getString("Tests.Enable"), true);
        caseIgnorer = new JCheckBox(Messages.getString("Tests.IgnoreCase"), false);
        exactDateChecker = new JCheckBox(Messages.getString("Tests.UseExactDate"), true);
        changeTableButton = new JButton(Messages.getString("Tests.ChangeTable"));
        resetFilterButton = new JButton(Messages.getString("Tests.ResetFilter"));
        resetModelButton = new JButton(Messages.getString("Tests.ResetModel"));
        addMaleButton = new JButton(Messages.getString("Tests.AddMale"));
        addFemaleButton = new JButton(Messages.getString("Tests.AddFemale"));
        removeButton = new JButton(Messages.getString("Tests.Remove"));
        south.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
        		BorderFactory.createLoweredBevelBorder(),
        		BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        statusPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("Tests.Events")));

        configPanel.add(new JLabel(Messages.getString("Tests.EditorsMode"), SwingConstants.RIGHT));
        configPanel.add(modeComboBox);
        configPanel.add(new JLabel(Messages.getString("Tests.EditorsPosition"), SwingConstants.RIGHT));
        configPanel.add(positionComboBox);
        configPanel.add(new JLabel());
        configPanel.add(filterEnabler);
        configPanel.add(new JLabel());
        configPanel.add(caseIgnorer);
        configPanel.add(new JLabel());
        configPanel.add(exactDateChecker);

        JScrollPane eventsLogPane = new JScrollPane(eventsLog);
        eventsLogPane.setPreferredSize(new Dimension(0, 0));
        statusPanel.add(eventsLogPane, BorderLayout.CENTER);
        
        eventsLog.setEditable(false);
        
        buttonsPanel.add(addMaleButton);
        buttonsPanel.add(addFemaleButton);
        buttonsPanel.add(removeButton);
        buttonsPanel.add(changeTableButton);
        buttonsPanel.add(resetFilterButton);
        buttonsPanel.add(resetModelButton);

        eventsLog.setFont(table.getFont().deriveFont(9f));
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
                parser.setIgnoreCase(caseIgnorer.isSelected());
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
        modeComboBox.setModel(new DefaultComboBoxModel(TableFilterHeader.EditorMode.values()));
        modeComboBox.addItem(Messages.getString("Tests.MixedMode"));
        modeComboBox.setSelectedItem(filterHeader.getMode());
        positionComboBox.setModel(new DefaultComboBoxModel(TableFilterHeader.Position.values()));
        positionComboBox.setSelectedItem(filterHeader.getPosition());

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
    	filterHeader.addHeaderObserver(new ITableFilterHeaderObserver() {
			
			public void tableFilterUpdated(TableFilterHeader header, ITableFilterEditor editor, Object newValue) {
				String what;
				if (newValue==null){
					what="(NULL)";
				} else if (newValue==ChoiceFilterEditor.NO_FILTER){
					what="(NO FILTER)";
				} else {
					what="("+newValue.getClass().getSimpleName()+"): "+newValue.toString();
				}
				addEvent("Set filter on column "+editor.getFilterPosition()+ " to "+ what);
			}
			
			public void tableFilterEditorExcluded(TableFilterHeader header, ITableFilterEditor editor) {
				addEvent("Removed editor of type "+editor.getClass().getSimpleName()+" on column "+editor.getFilterPosition());
			}
			
			public void tableFilterEditorCreated(TableFilterHeader header, ITableFilterEditor editor) {
				addEvent("Created editor of type "+editor.getClass().getSimpleName()+" on column "+editor.getFilterPosition());
			}

			private void addEvent(String what){
				eventsLog.insert(what+'\n',0);	
				eventsLog.setCaretPosition(0);
			}
		});

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

        positionComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                if (e.getStateChange() == ItemEvent.SELECTED) {
                    filterHeader.setPosition((TableFilterHeader.Position) e.getItem());
                }
            }
        });

        filterEnabler.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    filterHeader.setEnabled(filterEnabler.isSelected());
                }
            });


        changeTableButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tableModel.changeModel(table);
                    setTableRenderers();
                    if (Messages.getString("Tests.MixedMode").equals(
                                modeComboBox.getSelectedItem()))
                        createMixedHeaders();
                    setChoiceRenderers();
                }
            });

        resetFilterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    filterHeader.resetFilters();
                }
            });

        resetModelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	tableModel = TestTableModel.createTestTableModel();
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
        
        exactDateChecker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filterHeader.getTextParser().setComparator(Date.class,
                		exactDateChecker.isSelected()? null : DateHandler.getDefault());
                filterHeader.updateFilter();
            }
        });

        addMaleButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addTestData(true);
                }
            });

        addFemaleButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addTestData(false);
                }
            });

        removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tableModel.removeTestData();
                }
            });

    }

    private void addTestData(boolean male) {
        TestData td = new TestData();
        td.male = male;
        tableModel.addTestData(td);
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


    public final static void main(String[] args) {

//        try {
//			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}

        AppTestMain frame = new AppTestMain();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
