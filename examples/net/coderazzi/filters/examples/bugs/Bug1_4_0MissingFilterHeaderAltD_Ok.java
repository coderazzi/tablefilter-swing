package net.coderazzi.filters.gui_tests.bugs;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui_tests.TestTableModel;

/**
 * Variation of Bug1_4_0MissingFilterHeader. 
 * It codes the 'normal' setup -table and header created/populated before 
 *   any additional containment operation-
 * This test run fine on version 1.4.0
 */
public class Bug1_4_0MissingFilterHeaderAltD_Ok extends JPanel{
	
	private static final long serialVersionUID = -22834158973030319L;

	private JTable table;
	
	public Bug1_4_0MissingFilterHeaderAltD_Ok() {
		super(new BorderLayout());
		table = new JTable(TestTableModel.createLargeTestTableModel(20));
		new TableFilterHeader(table);
		add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	public static void main(String[] args) {
		Bug1_4_0MissingFilterHeaderAltD_Ok testTableFilter = new Bug1_4_0MissingFilterHeaderAltD_Ok();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(testTableFilter);
		frame.pack();
		frame.setVisible(true);
	}
}
