package net.coderazzi.filters.examples.bugs;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.TableFilterHeader;


/**
 * Variation of Bug1_4_0MissingFilterHeader. Now, the header is associated
 * directly to the table This test run fine on version 1.4.0
 */
public class Bug1_4_0MissingFilterHeaderAltA_Ok extends JPanel {

    private static final long serialVersionUID = -22834158973030319L;

    JTable table;

    @SuppressWarnings("unused")
    public Bug1_4_0MissingFilterHeaderAltA_Ok() {
        super(new BorderLayout());
        table = new JTable();
        new TableFilterHeader(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        SwingUtilities.invokeLater(new Runnable() {

                @Override public void run() {
                    table.setModel(
                        TestTableModel.createLargeTestTableModel(20));
                }
            });
    }

    public static void main(String args[]) {
        Bug1_4_0MissingFilterHeaderAltA_Ok testTableFilter =
            new Bug1_4_0MissingFilterHeaderAltA_Ok();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(testTableFilter);
        frame.pack();
        frame.setVisible(true);
    }
}
