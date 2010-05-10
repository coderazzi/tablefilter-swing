package net.coderazzi.filters.gui_tests.bugs;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui_tests.TestTableModel;

/**
 * Bug: placing the table inside a JScrollPane, with the table not automatically
 * resizing, provocates the header not to move
 */
public class Bug1_2_7ScrollPaneTest extends JPanel {

	public Bug1_2_7ScrollPaneTest() {
		super(new BorderLayout());
		JTable table = new JTable(TestTableModel.createLargeTestTableModel(100));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		add(getFilterTableScollPane(table), BorderLayout.CENTER);
	}

	public static JComponent getFilterTableScollPane(JTable table) {
		JPanel panel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.SOUTH);
		TableFilterHeader filterHeader = new TableFilterHeader();
		filterHeader.setTable(table);
		panel.add(filterHeader, BorderLayout.NORTH);
		return panel;
	}

	public static void main(String[] args) {
		Bug1_2_7ScrollPaneTest testTableFilter = new Bug1_2_7ScrollPaneTest();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(testTableFilter);
		frame.pack();
		frame.setVisible(true);
	}
}
