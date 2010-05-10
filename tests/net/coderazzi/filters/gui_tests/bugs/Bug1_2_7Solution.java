package com.byteslooser.filters.gui_tests.bugs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import com.byteslooser.filters.gui.TableFilterHeader;
import com.byteslooser.filters.gui.TableFilterHeader.Position;
import com.byteslooser.filters.gui_tests.TestTableModel;

public class Bug1_2_7Solution extends JPanel{
	
	private final static String NONE="  - - - - ";
	private final static String INLINE="INLINE";
	private final static String TOP=" TOP";
	private final static String RIGHT=" > > > > ";
	private final static String LEFT= " < < < < ";

	TableFilterHeader filterHeader;
	
	public Bug1_2_7Solution() {
		super(new BorderLayout());
		final JTable tableA = new JTable(TestTableModel.createLargeTestTableModel(20));
		tableA.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		filterHeader = new TableFilterHeader(tableA);
		final JTable tableB = new JTable(TestTableModel.createLargeTestTableModel(20));
		tableB.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(new JScrollPane(tableA));
		split.setRightComponent(new JScrollPane(tableB));
		add(split, BorderLayout.CENTER);
		
		JPanel top = new JPanel(new GridLayout(1,2)); 
		final JButton buttonLocation = new JButton(NONE);
		final JButton buttonChoice = new JButton(RIGHT);
		buttonLocation.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String now= buttonLocation.getText();
				if (now.equals(NONE)){
					filterHeader.setTable(null);
					filterHeader=null;
					buttonLocation.setText(INLINE);
				} else if (now.equals(INLINE)){
					filterHeader = new TableFilterHeader();
					filterHeader.setPosition(Position.INLINE);
					filterHeader.setTable(buttonChoice.getText().equals(RIGHT)? tableA : tableB);
					buttonLocation.setText(TOP);
				} else {
					filterHeader.setPosition(Position.TOP);
					buttonLocation.setText(NONE);					
				}
			}
		});
		buttonChoice.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JTable table;
				if (buttonChoice.getText().equals(LEFT)){
					table=tableA;
					buttonChoice.setText(RIGHT);
				} else {
					table=tableB;
					buttonChoice.setText(LEFT);
				}
				if (filterHeader!=null){
					filterHeader.setTable(table);
				}
			}
		});
		top.add(buttonLocation);
		top.add(buttonChoice);
		add(top, BorderLayout.NORTH);
	}
	
	public static void main(String[] args) {
		Bug1_2_7Solution testTableFilter = new Bug1_2_7Solution();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(testTableFilter);
		frame.pack();
		frame.setVisible(true);
	}
}
