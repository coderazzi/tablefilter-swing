/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.coderazzi.filters.examples.bugs;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.coderazzi.filters.examples.utils.CenteredRenderer;
import net.coderazzi.filters.examples.utils.FlagRenderer;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.TableFilterHeader.Position;


/**
 * Previously to release 2.1.1, setting the header as non visible, would
 * normally hide as well the header of the table (if autoplace was in order).
 * Disabling it would also disable the header of the table (this had not so much
 * impact, but it was not properly done, anyway)
 */
@SuppressWarnings({ "serial" })
public class Bug2_1_0_SetVisible extends JFrame {

    private static final long serialVersionUID = 4521016108974569405L;

    JTable table;
    TestTableModel tableModel;
    TableFilterHeader filterHeader;

    JPanel mainPanel;

    public Bug2_1_0_SetVisible(Position position,
                               boolean  visible,
                               boolean  enabled) {
        super("Bug2_1_0_SetVisible");
        tableModel = TestTableModel.createTestTableModel(20);
        createGui(position, visible, enabled);
        customizeTable();
    }

    private void createGui(Position position,
                           boolean  visible,
                           boolean  enabled) {
        mainPanel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane();
        ButtonsPanel actions = new ButtonsPanel();

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(actions, BorderLayout.NORTH);

        table = new JTable(tableModel);
        scrollPane.setViewportView(table);
        filterHeader = new TableFilterHeader();
        filterHeader.setPosition(position);
        filterHeader.setTable(table);
        filterHeader.setVisible(visible);
        filterHeader.setEnabled(enabled);
        if (position == Position.NONE) {
            mainPanel.add(filterHeader, BorderLayout.SOUTH);
        }

        actions.positionComboBox.setSelectedItem(position);
        actions.visibleCheckBox.setSelected(visible);
        actions.enabledCheckBox.setSelected(enabled);

        actions.positionComboBox.addItemListener(new ItemListener() {

                @Override public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        Position position = (Position) e.getItem();
                        if (position == Position.NONE) {
                            filterHeader.setPosition(position);
                            mainPanel.add(filterHeader, BorderLayout.SOUTH);
                        } else {
                            mainPanel.remove(filterHeader);
                            filterHeader.setPosition(position);
                        }

                        mainPanel.revalidate();
                    }
                }
            });

        actions.visibleCheckBox.addItemListener(new ItemListener() {
                @Override public void itemStateChanged(ItemEvent e) {
                    filterHeader.setVisible(
                        e.getStateChange() == ItemEvent.SELECTED);
                }
            });

        actions.enabledCheckBox.addItemListener(new ItemListener() {
                @Override public void itemStateChanged(ItemEvent e) {
                    filterHeader.setEnabled(
                        e.getStateChange() == ItemEvent.SELECTED);
                }
            });

        getContentPane().add(mainPanel);
    }

    void customizeTable() {
        table.getColumnModel().getColumn(table.convertColumnIndexToView(
                tableModel.getColumn(TestTableModel.COUNTRY))).setCellRenderer(
            new FlagRenderer());
        table.getColumnModel().getColumn(table.convertColumnIndexToView(
                tableModel.getColumn(TestTableModel.AGE))).setCellRenderer(
            new CenteredRenderer());
        filterHeader.getFilterEditor(tableModel.getColumn(
                TestTableModel.COUNTRY)).setRenderer(new FlagRenderer());
    }

    static class ButtonsPanel extends JPanel {
        private static final long serialVersionUID = -4221513653085366887L;
        JComboBox positionComboBox = new JComboBox(new DefaultComboBoxModel(
                    Position.values()));
        JCheckBox visibleCheckBox = new JCheckBox("Visible", true);
        JCheckBox enabledCheckBox = new JCheckBox("Enabled", true);

        public ButtonsPanel() {
            super(new GridLayout(3, 1, 0, 8));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(8, 8, 8, 8),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(),
                        BorderFactory.createEmptyBorder(8, 24, 8, 24))));
            add(positionComboBox);
            add(visibleCheckBox);
            add(enabledCheckBox);
        }
    }


    static class StartDialog extends JDialog {

        ButtonsPanel actions = new ButtonsPanel();

        public StartDialog() {
            super((Frame) null, "Bug2_1_0_SetVisible", true);
            setContentPane(actions);
        }
    }


    public static final void main(String args[]) {

        StartDialog dialog = new StartDialog();
        dialog.pack();
        dialog.setVisible(true);

        Bug2_1_0_SetVisible frame = new Bug2_1_0_SetVisible((Position)
                dialog.actions.positionComboBox.getSelectedItem(),
                dialog.actions.visibleCheckBox.isSelected(),
                dialog.actions.enabledCheckBox.isSelected());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setBounds(100, 100, 600, 450);
        frame.setVisible(true);
    }
}
