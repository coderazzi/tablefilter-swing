package net.coderazzi.filters.gui_tests.bugs;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui_tests.TestTableModel;

/**
 * Running next code in Release 1.4.0 will get the FilterHeader missing
 * The another classes Bug1_4_0MissingFilterHeaderAltX check the behaviour of
 *   similar cases under different setups
 */
public class Bug1_4_0MissingFilterHeader extends JPanel{
	
	private static final long serialVersionUID = -22834158973030319L;

	private JTable table;
	private TableFilterHeader filterHeader;
	
	public Bug1_4_0MissingFilterHeader() {
		super(new BorderLayout());
		table = new JTable();
		filterHeader = new TableFilterHeader();
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				table.setModel(TestTableModel.createLargeTestTableModel(20));
				filterHeader.setTable(table);
			}
		});
	}
	
	public static void main(String[] args) {
		Bug1_4_0MissingFilterHeader testTableFilter = new Bug1_4_0MissingFilterHeader();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(testTableFilter);
		frame.pack();
		frame.setVisible(true);
	}
}
