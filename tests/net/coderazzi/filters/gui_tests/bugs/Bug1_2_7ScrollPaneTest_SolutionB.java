package net.coderazzi.filters.gui_tests.bugs;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui_tests.TestTableModel;

/**
 * Solution provided by Eugene Rachitskiy
 */
public class Bug1_2_7ScrollPaneTest_SolutionB extends JPanel {

	public Bug1_2_7ScrollPaneTest_SolutionB() {
		super(new BorderLayout());
		JTable table = new JTable(TestTableModel.createLargeTestTableModel(100));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableFilterHeader filterHeader = new TableFilterHeader();
		filterHeader.setTable(table);
		JScrollPane scrollPane = new JScrollPane(table) {
			@Override
			public void setColumnHeaderView(Component view) {
				if (getColumnHeader() == null) {
					super.setColumnHeaderView(view);
				}
			}
		};
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.add(table.getTableHeader(), BorderLayout.NORTH);
		headerPanel.add(filterHeader, BorderLayout.SOUTH);
		scrollPane.setColumnHeaderView(headerPanel);
		add(scrollPane, BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new Bug1_2_7ScrollPaneTest_SolutionB());
		frame.pack();
		frame.setVisible(true);
	}
}
