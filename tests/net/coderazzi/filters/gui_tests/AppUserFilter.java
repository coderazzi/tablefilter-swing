package net.coderazzi.filters.gui_tests;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.coderazzi.filters.UserFilter;
import net.coderazzi.filters.gui.TableFilterHeader;

/**
 * Adding new filters outside the TableHeader does not work (on release 1.4.0)
 */
public class AppUserFilter extends JPanel{
	
	private static final long serialVersionUID = 9084957648913273935L;
	
	public AppUserFilter() {
		super(new BorderLayout());
		TestTableModel model = TestTableModel.createTestTableModel();
		JTable table = new JTable(model);
		TableFilterHeader filterHeader = new TableFilterHeader(table);
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		final int nameColumn=model.getColumn(TestTableModel.NAME);
		final UserFilter userFilter = new UserFilter(filterHeader){
			@Override
			public boolean include(Entry entry) {
				return -1!=entry.getStringValue(nameColumn).indexOf('e');
			}
		};

		JCheckBox check = new JCheckBox("Filter out any row where the name does not contain a lower case 'e'");
		add(check, BorderLayout.SOUTH);
		
		check.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				
				userFilter.setEnabled(e.getStateChange()==ItemEvent.SELECTED);
			}
		});
	}
	
	public static void main(String[] args) {
		AppUserFilter testTableFilter = new AppUserFilter();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(testTableFilter);
		frame.pack();
		frame.setVisible(true);
	}
}
