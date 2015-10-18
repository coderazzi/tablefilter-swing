package net.coderazzi.filters.examples;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Created by coderazzi on 18/10/15.
 */
public class Issue_41 {
    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JTable table = new JTable();//new DefaultTableModel(rowData, columnNames));

        TableFilterHeader tfh = new TableFilterHeader(table, AutoChoices.ENABLED);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(300, 150);
        frame.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object rowData[][] = {{"Str1", true, new Double(1)},
                        {"Str2", true, new Double(2)},
                        {"Str3", false, new Double(3)},
                        {"Str4", true, new Double(4)},
                        {"Str5", false, new Double(5)}};
                Object columnNames[] = {"String", "BOO", "Double"};

                DefaultTableModel dtm = new DefaultTableModel(rowData, columnNames);

                table.setModel(dtm);

            }
        });
    }
}
