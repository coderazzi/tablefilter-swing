package net.coderazzi.filters.examples.bugs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;

import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.TableFilterHeader;


/** Solution provided by Eugene Rachitskiy. */
public class Bug1_2_7ScrollPaneTest_SolutionA extends JPanel {

    private static final long serialVersionUID = -5733857124817243985L;

    public Bug1_2_7ScrollPaneTest_SolutionA() {
        super(new BorderLayout());

        JTable table = new JTable(TestTableModel.createLargeTestTableModel(
                    100));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(getFilterTableScollPane(table), BorderLayout.CENTER);
    }

    /**
     * @param   table
     *
     * @return  returns a scroll pane containing a table a table filter header
     *          appearing on the second row
     */
    public static JScrollPane getFilterTableScollPane(JTable table) {
        TableFilterHeader filterHeader = new TableFilterHeader();
        filterHeader.setTable(table);

        return getFilterTableScollPane(table, filterHeader);
    }

    /**
     * Creates a scroll pane from a given table and the table filter header such
     * that the filter appears as the first row of the table and scrolls
     * horizontally along with the table.
     *
     * @param   table
     * @param   filterHeader
     *
     * @return
     */
    public static JScrollPane getFilterTableScollPane(
            JTable            table,
            TableFilterHeader filterHeader) {
        // the overrides are to prevent the column header from being reset by
        // JTable::configureEnclosingScrollPane()
        JScrollPane scrollPane = new JScrollPane(table) {

            private static final long serialVersionUID = -3629676577469115439L;

            /*
             * (non-Javadoc)
             *
             * @see
             * javax.swing.JScrollPane#setColumnHeader(javax.swing.JViewport)
             */
            @Override public void setColumnHeader(JViewport columnHeader) {
                if (getColumnHeader() == null) {
                    super.setColumnHeader(columnHeader);
                }
            }

            /*
             * (non-Javadoc)
             *
             * @see
             * javax.swing.JScrollPane#setColumnHeaderView(java.awt.Component)
             */
            @Override public void setColumnHeaderView(Component view) {
                if (getColumnHeader() == null) {
                    super.setColumnHeaderView(view);
                }
            }
        };

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.add(table.getTableHeader());
        headerPanel.add(filterHeader);
        scrollPane.setColumnHeaderView(headerPanel);

        return scrollPane;
    }

    public static void main(String args[]) {
        Bug1_2_7ScrollPaneTest_SolutionA testTableFilter =
            new Bug1_2_7ScrollPaneTest_SolutionA();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(testTableFilter);
        frame.pack();
        frame.setVisible(true);
    }
}
