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
package net.coderazzi.filters.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.coderazzi.filters.Filter;
import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.examples.utils.CenteredRenderer;
import net.coderazzi.filters.examples.utils.EventsWindow;
import net.coderazzi.filters.examples.utils.FlagRenderer;
import net.coderazzi.filters.examples.utils.TestData;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.TableFilterHeader.Position;
import net.coderazzi.filters.gui.editor.FilterEditor;


@SuppressWarnings("serial")
public class TableFilterExample extends JFrame {

	private static final long serialVersionUID = 4521016104474569405L;

    TestTableModel tableModel;
    JTable table;
    JPanel tablePanel;    
    JPanel filterHeaderPanel;
    TableFilterHeader filterHeader;
    
    
    public TableFilterExample() {
        super("Table Filter Example");
        getContentPane().add(createGui());
        setJMenuBar(createMenu());
        customizeTable();
    }
    
    private JPanel createGui(){
    	tableModel = TestTableModel.createTestTableModel();
    	table = new JTable(tableModel);
    	tablePanel = new JPanel(new BorderLayout());
    	tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
    	tablePanel.setBorder(BorderFactory.createCompoundBorder(
    			BorderFactory.createLoweredBevelBorder(),
    			BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    	filterHeader = new TableFilterHeader(table){
    		
    		@Override
    		protected void customizeEditor(FilterEditor editor) {
    			super.customizeEditor(editor);
    			//enter here any code to customize the editor
    		}
    	};
    	return tablePanel;
    }
    
    private JMenuBar createMenu(){
    	JMenuBar menu = new JMenuBar();
    	menu.add(createTableMenu());
    	menu.add(createHeaderMenu());
    	menu.add(createMiscellaneousMenu());
    	return menu;
    }
    
    private JMenu createTableMenu(){
    	JCheckBoxMenuItem autoResize = 
    		new JCheckBoxMenuItem(new AbstractAction("Auto resize") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				table.setAutoResizeMode(source.isSelected()? 
						JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
				table.doLayout();
			}
		});
		autoResize.setSelected(table.getAutoResizeMode()!=JTable.AUTO_RESIZE_OFF);
    	final AbstractAction removeElement = 
    		new AbstractAction("Remove top row") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.removeTestData();
				if (tableModel.getRowCount()==0){
					((JComponent)e.getSource()).setEnabled(false);
				}
			}
		};
				
    	JMenu tableMenu = new JMenu("Table");
    	tableMenu.setMnemonic(KeyEvent.VK_T);
    	
    	tableMenu.add(new JMenuItem(
    			new AbstractAction("Create male row [first position]") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				addTestData(true);
		        removeElement.setEnabled(true);
			}
		}));
    	tableMenu.add(new JMenuItem(
    			new AbstractAction("Create female row [first position]") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				addTestData(false);
		        removeElement.setEnabled(true);
			}
		}));
    	
		tableMenu.add(new JMenuItem(removeElement));
		tableMenu.addSeparator();
		tableMenu.add(autoResize);
		tableMenu.addSeparator();
		tableMenu.add(new JMenuItem(new AbstractAction("Change model") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.changeModel(table);
                customizeTable();
                removeElement.setEnabled(true);
			}
		}));
		tableMenu.add(new JMenuItem(new AbstractAction("Use new model") {			
			@Override
			public void actionPerformed(ActionEvent e) {
            	tableModel = TestTableModel.createTestTableModel();
                table.setModel(tableModel);
                customizeTable();
                removeElement.setEnabled(true);
			}
		}));
    	return tableMenu;
    }

    private JMenu createHeaderMenu(){    	
    	JCheckBoxMenuItem ignoreCase=new JCheckBoxMenuItem(
    			new AbstractAction("ignore case") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				filterHeader.getTextParser().setIgnoreCase(source.isSelected());				
			}
		});
    	
    	JCheckBoxMenuItem autoOptions=new JCheckBoxMenuItem(
    			new AbstractAction("auto options") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				filterHeader.setAutoOptions(source.isSelected());				
			}
		});
    	
    	JCheckBoxMenuItem enabled=new JCheckBoxMenuItem(
    			new AbstractAction("enabled") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				filterHeader.setEnabled(source.isSelected());				
			}
		});
    	
    	JCheckBoxMenuItem visible=new JCheckBoxMenuItem(
    			new AbstractAction("visible") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				filterHeader.setVisible(source.isSelected());				
			}
		});
    	
    	JMenuItem reset = new JMenuItem(new AbstractAction("reset") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				filterHeader.resetFilter();
			}
		});
    	ignoreCase.setMnemonic(KeyEvent.VK_C);
    	ignoreCase.setSelected(filterHeader.getTextParser().isIgnoreCase());
    	autoOptions.setSelected(filterHeader.isAutoOptions());
    	enabled.setSelected(filterHeader.isEnabled());
    	visible.setSelected(filterHeader.isVisible());

    	JMenu ret = new JMenu("Filter Header");
    	ret.setMnemonic(KeyEvent.VK_H);
    	ret.add(ignoreCase);
    	ret.add(autoOptions);
    	ret.add(enabled);
    	ret.addSeparator();
    	ret.add(visible);
    	ret.add(createPositionMenu());
    	ret.add(createAppearanceMenu());
    	ret.addSeparator();
    	ret.add(createMaxRowsMenu());
    	ret.add(createMaxHistoryMenu());
    	ret.addSeparator();
    	ret.add(reset);
    	return ret;
    }
    
    private JMenu createMiscellaneousMenu(){
    	
		final IFilter userFilter = new Filter() {
			int nameColumn=tableModel.getColumn(TestTableModel.NAME);
			@Override
			public boolean include(Entry entry) {
				return -1!=entry.getStringValue(nameColumn).indexOf('e');
			}
		};
		
		JCheckBoxMenuItem enableUserFilter=new JCheckBoxMenuItem(
				new AbstractAction("enable user filter") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				if(source.isSelected()){
					filterHeader.addFilter(userFilter);
					JOptionPane.showMessageDialog(TableFilterExample.this, 
							"Filtering out any row where the name does not "+
							"contain a lower case 'e'");
				} else {
					filterHeader.removeFilter(userFilter);					
				}
			}
		});
    	
    	JMenuItem events = new JMenuItem(new AbstractAction("events window") {
    		EventsWindow window;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (window==null || !window.isVisible()){
					window = new EventsWindow(TableFilterExample.this, 
							filterHeader);
					window.setVisible(true);
				} else {
					window.requestFocus();
				}
			}
		});

    	JMenu ret = new JMenu("Miscellaneous");
    	ret.setMnemonic(KeyEvent.VK_M);
    	ret.add(events);
    	ret.addSeparator();
    	ret.add(enableUserFilter);
    	return ret;
    }
    
    private JMenu createPositionMenu(){
    	JRadioButtonMenuItem top = new JRadioButtonMenuItem(
    			new AbstractAction("top (automatic)") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				setPosition(Position.TOP);
			}
		});
    	JRadioButtonMenuItem inline = new JRadioButtonMenuItem(
    			new AbstractAction("inline (automatic)") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				setPosition(Position.INLINE);
			}
		});
    	JRadioButtonMenuItem manual = new JRadioButtonMenuItem(
    			new AbstractAction("bottom (manual)") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				setPosition(Position.NONE);
			}
		});
    	ButtonGroup group = new ButtonGroup();
    	group.add(top);
    	group.add(inline);
    	group.add(manual);
    	switch(filterHeader.getPosition()){
    		case TOP: 
    			top.setSelected(true);
    			break;
    		case INLINE: 
    			inline.setSelected(true);
    			break;
    		case NONE: 
    			manual.setSelected(true);
    			break;
    	}
    	setPosition(filterHeader.getPosition());
    	JMenu ret = new JMenu("position");
    	ret.add(top);
    	ret.add(inline);
    	ret.add(manual);
    	return ret;
    }
    	
    private JMenu createAppearanceMenu(){
    	JMenu ret = new JMenu("appearance");
    	ret.add(new JMenuItem(new AbstractAction("background color ...") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select header's background color", 
						filterHeader.getBackground());
				if (ret!=null){
					filterHeader.setBackground(ret);
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("foreground color ...") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select header's foreground color", 
						filterHeader.getForeground());
				if (ret!=null){
					filterHeader.setForeground(ret);
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("disabled color ...") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select header's disabled color", 
						filterHeader.getDisabledForeground());
				if (ret!=null){
					filterHeader.setDisabledForeground(ret);
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("error color ...") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select header's error color", 
						filterHeader.getErrorForeground());
				if (ret!=null){
					filterHeader.setErrorForeground(ret);
				}
			}
		}));
    	ret.addSeparator();
    	ret.add(createFontSizeMenu());			
    	return ret;
    }
    
    private JMenu createFontSizeMenu(){
    	int RELATIVE_FONT_SIZES[]={-2, -1, 0, 1, 2, 4, 8, 16};
    	int size=filterHeader.getFont().getSize();
    	JMenu ret = new JMenu("font size");
    	ButtonGroup group = new ButtonGroup();
    	for (int i : RELATIVE_FONT_SIZES){
    		JRadioButtonMenuItem item = createFontSizeMenuItem(size+i);
    		ret.add(item);
    		group.add(item);
    		if (i==0){
    			item.setSelected(true);
    		}
    	}
    	return ret;
    }
    
    private JRadioButtonMenuItem createFontSizeMenuItem(final int size){
    	return new JRadioButtonMenuItem(new AbstractAction(String.valueOf(size)) {			
			@Override
			public void actionPerformed(ActionEvent e) {
				filterHeader.setFont(
						filterHeader.getFont().deriveFont((float)(size)));
			}
		});
    }
    
    private JMenu createMaxRowsMenu (){
    	JMenu ret = new JMenu("max visible rows on popup");
    	ButtonGroup group = new ButtonGroup();
    	for (int i=4; i<16;i++){
    		JRadioButtonMenuItem item = createMaxRowsMenuItem(i);
    		if (i==filterHeader.getMaxVisibleRows()){
    			item.setSelected(true);
    		}
    		group.add(item);
    		ret.add(item);
    	}
    	return ret;
    }
    
    private JRadioButtonMenuItem createMaxRowsMenuItem(final int i){
    	return new JRadioButtonMenuItem(new AbstractAction(String.valueOf(i)) {			
			@Override
			public void actionPerformed(ActionEvent e) {
				filterHeader.setMaxVisibleRows(i);
			}
		});
    }
    
    private JMenu createMaxHistoryMenu (){
    	JMenu ret = new JMenu("max history on country column");

    	for (int i=0; i<10;i++){
    		ret.add(createMaxHistoryMenuItem(i));
    	}
    	return ret;
    }
    
    private JMenuItem createMaxHistoryMenuItem(final int i){
    	return new JMenuItem(new AbstractAction(String.valueOf(i)) {			
			@Override
			public void actionPerformed(ActionEvent e) {
				filterHeader.getFilterEditor(tableModel.getColumn(
						TestTableModel.COUNTRY)).setMaxHistory(i);
			}
		});
    }
    
    void customizeTable() {
        int countryColumn = tableModel.getColumn(TestTableModel.COUNTRY);

        if (tableModel.getColumnCount() > countryColumn) {
            table.getColumnModel().getColumn(
            		table.convertColumnIndexToView(countryColumn)).
            		setCellRenderer(new FlagRenderer());

        	filterHeader.getFilterEditor(countryColumn).setAutoOptions(tableModel);
        	filterHeader.setTableCellRenderer(countryColumn, new FlagRenderer());
        }

        int agesColumn = tableModel.getColumn(TestTableModel.AGE);

        if (tableModel.getColumnCount() > agesColumn) {
            table.getColumnModel().getColumn(
            		table.convertColumnIndexToView(agesColumn)).
            		setCellRenderer(new CenteredRenderer());
        }

        int datesColumn = tableModel.getColumn(TestTableModel.DATE);

        if (tableModel.getColumnCount() > datesColumn) {
            table.getColumnModel().getColumn(
            		table.convertColumnIndexToView(datesColumn)).
            		setCellRenderer(new DefaultTableCellRenderer(){

            			private static final long serialVersionUID = 
            				8042527267257156699L;
            			Format parser = 
            				FilterSettings.types.getFormat(Date.class);

            			@Override
            			public Component getTableCellRendererComponent(
            					JTable table, Object value, boolean isSelected, 
            					boolean hasFocus, int row, int column) {
            				if (value instanceof Date){
            					value = parser.format(value);
            				}
            				return super.getTableCellRendererComponent(table, 
            						value, isSelected, hasFocus, row, column);
            			}			
            		});
        }

    }

    void addTestData(boolean male) {
        TestData td = new TestData();
        td.male = male;
        tableModel.addTestData(td);
    }

	void setPosition(Position position) {
		if (filterHeader.getPosition()==Position.NONE){
			filterHeaderPanel.remove(filterHeader);
			tablePanel.remove(filterHeaderPanel);
		}
		filterHeader.setPosition(position);
		if (filterHeader.getPosition()==Position.NONE){
			filterHeaderPanel = new JPanel(new BorderLayout());
			filterHeaderPanel.add(filterHeader, BorderLayout.CENTER);
			filterHeaderPanel.setBorder(BorderFactory.createLineBorder(
					filterHeader.getDisabledForeground(),1));
			tablePanel.add(filterHeaderPanel, BorderLayout.SOUTH);
		}
		tablePanel.revalidate();
	}
	
    public final static void main(String[] args) {

//        try {
//			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
    	FilterSettings.autoOptions=true;
        TableFilterExample frame = new TableFilterExample();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
