package net.coderazzi.filters.examples;

import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.TableFilterHeader;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

public class Issue_39 {
    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Object rowData[][] = { { "Str1" , true, new Double(1) },
                { "Str2" , true, new Double(2) },
                { "Str3" , false, new Double(3) },
                { "Str4" , true, new Double(4) },
                { "Str5" , false, new Double(5) } };
        Object columnNames[] = { "String", "BOO", "Double" };


        JTable table = new JTable(new DefaultTableModel(rowData, columnNames));

        TableFilterHeader tfh = new TableFilterHeader(table, AutoChoices.ENABLED);
        //tfh.setInstantFiltering(false);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(300, 150);
        frame.setVisible(true);

    }
}