/*
 * ViewPanelMarketData.java
 *
 * Created on December 14, 2007, 1:48 PM
 */

package net.coderazzi.filters.examples.bugs;

/** @author  int1634 */
public class Bug1_2_4_ResizingView extends javax.swing.JPanel {

    private static final long serialVersionUID = -8267958152880405776L;

    public Bug1_2_4_ResizingView() {
        initComponents();
    }

    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        jPanel6 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        tableFilterHeader = new net.coderazzi.filters.gui.TableFilterHeader();
        jScrollPane12 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        splitPane.setDividerSize(25);
        splitPane.setResizeWeight(1.0);
        splitPane.setContinuousLayout(true);
        splitPane.setDoubleBuffered(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setOpaque(false);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel9.setLayout(new java.awt.BorderLayout());
        jPanel9.add(tableFilterHeader, java.awt.BorderLayout.NORTH);

        jScrollPane12.setViewportView(table);

        jPanel9.add(jScrollPane12, java.awt.BorderLayout.CENTER);

        jPanel6.add(jPanel9, java.awt.BorderLayout.CENTER);

        splitPane.setLeftComponent(jPanel6);

        jPanel11.setLayout(new java.awt.BorderLayout());

        javax.swing.JPanel gray = new javax.swing.JPanel();
        gray.setBackground(java.awt.Color.darkGray);
        jPanel11.add(gray, java.awt.BorderLayout.CENTER);

        splitPane.setRightComponent(jPanel11);

        jPanel1.add(splitPane, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);

    }

    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JTable table;
    private javax.swing.JSplitPane splitPane;
    private net.coderazzi.filters.gui.TableFilterHeader tableFilterHeader;

    public javax.swing.JTable getTable() {
        return table;
    }

    public net.coderazzi.filters.gui.TableFilterHeader getTableFilterHeader() {
        return tableFilterHeader;
    }

    public javax.swing.JSplitPane getSplitPane() {
        return splitPane;
    }

}
