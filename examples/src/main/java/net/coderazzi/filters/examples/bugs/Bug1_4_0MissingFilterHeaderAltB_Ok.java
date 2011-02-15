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
 * Variation of Bug1_4_0MissingFilterHeader. The only change is to set the model
 * after the header is setup. This test run fine on version 1.4.0
 */
public class Bug1_4_0MissingFilterHeaderAltB_Ok extends JPanel {

    private static final long serialVersionUID = -22834158973030319L;

    JTable table;
    TableFilterHeader filterHeader;

    public Bug1_4_0MissingFilterHeaderAltB_Ok() {
        super(new BorderLayout());
        table = new JTable();
        filterHeader = new TableFilterHeader();
        add(new JScrollPane(table), BorderLayout.CENTER);

        SwingUtilities.invokeLater(new Runnable() {

                @Override public void run() {
                    filterHeader.setTable(table);
                    table.setModel(
                        TestTableModel.createLargeTestTableModel(20));
                }
            });
    }

    public static void main(String args[]) {
        Bug1_4_0MissingFilterHeaderAltB_Ok testTableFilter =
            new Bug1_4_0MissingFilterHeaderAltB_Ok();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(testTableFilter);
        frame.pack();
        frame.setVisible(true);
    }
}
