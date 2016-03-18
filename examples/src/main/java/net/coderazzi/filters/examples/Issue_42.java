package net.coderazzi.filters.examples;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Created by coderazzi on 18/10/15.
 */
public class Issue_42 {
    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JTable table = new JTable();

        new TableFilterHeader(table, AutoChoices.ENABLED);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(300, 150);
        frame.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object rowData[][] = {{"C:\\windows\\path"},
                        {"C:\\windows"},
                        {"C:\\user\\\\path"},
                        {"C:\\user\\path"},
                        {"C:*Yao"}
                };
                Object columnNames[] = {"Path"};

                DefaultTableModel dtm = new DefaultTableModel(rowData, columnNames);

                table.setModel(dtm);

            }
        });
    }
}
