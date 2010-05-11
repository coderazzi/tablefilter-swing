package net.coderazzi.filters.examples.bugs;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.coderazzi.filters.BaseFilter;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.TableFilterHeader;

/**
 * Adding new filters outside the TableHeader does not work (on release 1.4.0)
 */
public class Bug1_4_0AddingExternalFilter extends JPanel{
	
	private static final long serialVersionUID = 9084957648913273935L;
	TestTableModel model = TestTableModel.createTestTableModel();
	BaseFilter addedFilter = new BaseFilter(){
		@Override
		public boolean include(Entry entry) {
			return -1!=entry.getStringValue(
					model.getColumn(TestTableModel.NAME)).indexOf('e');
		}
	};
	
	public Bug1_4_0AddingExternalFilter() {
		super(new BorderLayout());
		JTable table = new JTable(model);
		final TableFilterHeader filterHeader = new TableFilterHeader(table);
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		JCheckBox check = new JCheckBox(
				"Filter out any row where the name does not contain a lower case 'e'");
		add(check, BorderLayout.SOUTH);
		
		check.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				filterHeader.getTableFilter().removeFilter(addedFilter);					
				if (e.getStateChange()==ItemEvent.SELECTED){
					filterHeader.getTableFilter().addFilter(addedFilter);
				} 
			}
		});
	}
	
	public static void main(String[] args) {
		Bug1_4_0AddingExternalFilter testTableFilter =
			new Bug1_4_0AddingExternalFilter();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(testTableFilter);
		frame.pack();
		frame.setVisible(true);
	}
}
