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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.TableFilterHeader;


/** Test case to handle the bugs on version 4.1.4 */
public class IssuesFrom4_1_4 extends JFrame {

	static final int ROWS=20;
	/**
	 * To verify that the issue 17
	 * https://bitbucket.org/coderazzi/tablefilter-swing/issue/
	 *   17/setfilteronupdates-true-not-working-on
	 * is a JDK bug, 
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6791934
	 * set the next variable to false.
	 * Then, sort on the column B and press repeatedly the 'Update first row'
	 * button, the content won't be displayed properly; it is needed to
	 * repaint the table (button Solve issue)
	 */
	static boolean USE_FILTER_HEADER_SET_IT_TO_FALSE_TO_SEE_ISSUE=true;

	private static final long serialVersionUID = -3790311013681333241L;
	ExampleModel model = new ExampleModel();
	JTable table = new JTable(model);

	public IssuesFrom4_1_4() {
		super("Case for 4.1.4 bugs");
		getContentPane().add(initGui());
	}
	
	private JPanel initGui(){
		int buttons = USE_FILTER_HEADER_SET_IT_TO_FALSE_TO_SEE_ISSUE? 3 : 2;
		JPanel panel = new JPanel(new BorderLayout(0, 8));
		panel.add(new JScrollPane(table), BorderLayout.CENTER);		
		JPanel south = new JPanel(new GridLayout(buttons, 1, 0, 8));
		panel.add(south, BorderLayout.SOUTH);
		south.add(new JButton(new AbstractAction("Update first row") {
			
			private static final long serialVersionUID = 40845634740319997L;

			@Override
			public void actionPerformed(ActionEvent e) {
				model.update(table.convertRowIndexToModel(0));
			}
		}), BorderLayout.SOUTH);
		south.add(new JButton(new AbstractAction("Solve (17) issue") {
			
			private static final long serialVersionUID = 40845634740319998L;

			@Override
			public void actionPerformed(ActionEvent e) {
				table.repaint();
			}
		}), BorderLayout.SOUTH);	
		if (USE_FILTER_HEADER_SET_IT_TO_FALSE_TO_SEE_ISSUE){
			final IFilterEditor editor = setupFilterHeader();
			south.add(new JButton(new AbstractAction("Toggle UI on column 0") {
				
				private static final long serialVersionUID = 40845634740319999L;

				@Override
				public void actionPerformed(ActionEvent e) {
					editor.setUserInteractionEnabled(
							!editor.isUserInteractionEnabled());
				}
			}), BorderLayout.SOUTH);	
		} else {
			table.setAutoCreateRowSorter(true);
			((DefaultRowSorter)table.getRowSorter()).setSortsOnUpdates(true);
		}
		return panel;
	}
	
	private IFilterEditor setupFilterHeader(){
		TableFilterHeader header = new TableFilterHeader(table);
		header.setFilterOnUpdates(true);			
		CustomChoice cc = new CustomChoice("C* | A*") {
			
			private static final long serialVersionUID = 5455838951562934186L;

			@Override
			public RowFilter getFilter(final IFilterEditor editor) {
				return new RowFilter(){
					@Override
					public boolean include(Entry entry) {
						String c = entry.getStringValue(0);
						return c.startsWith("A") || c.startsWith("C");
					}
				};
			}
		};
		HashSet<CustomChoice> ccs = new HashSet<CustomChoice>();
		ccs.add(cc);
		IFilterEditor editor = header.getFilterEditor(0);
		editor.setCustomChoices(ccs);
		return editor;
	}
	
	static class ExampleModel extends DefaultTableModel{
		private static final long serialVersionUID = -2801256563570071852L;
		private static final int COLUMNS=2;
		private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss SS");
		private String columns[]={"A", "B"};
		private String info[][]=new String[ROWS][COLUMNS];
		public ExampleModel() {
			super(ROWS, COLUMNS);
			for (int i=0; i<ROWS;i++){
				info[i][0]=new String(Character.toChars(65+i/5)) +
						new String(Character.toChars(48+i%5));
				info[i][1]=String.valueOf(i)+ " \n " +sdf.format(new Date());
			}
		}
		@Override public Class<?> getColumnClass(int i) {return String.class;}
		@Override public String getColumnName(int c) { return columns[c];}
		@Override public Object getValueAt(int r, int c) { return info[r][c];}
		public void update(int row){
			info[row][0]=":" + info[row][0];
			info[row][1]="updated at "+sdf.format(new Date());
			fireTableRowsUpdated(row, row);
		}
	}
		
    
    public final static void main(String args[]) {
        FilterSettings.autoChoices = AutoChoices.ENABLED;
        FilterSettings.adaptiveChoices = true;
        JFrame frame = new IssuesFrom4_1_4();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setVisible(true);
    }

}
