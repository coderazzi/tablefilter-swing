package net.coderazzi.filters.examples.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import net.coderazzi.filters.gui.IFilterHeaderObserver;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.editor.FilterEditor;

@SuppressWarnings("serial")
public class EventsWindow extends JDialog implements IFilterHeaderObserver{
	
	TableModel tableModel;
	private static final String EXCLUDED = "Excluded";
	private static final String CREATED = "Created";
	static String COLUMN_NAMES[]={"Event", "Column", "Content.type", "Content"};
	static Class<?> COLUMN_CLASSES[]=
		{String.class, Integer.class, String.class, String.class};
	
	class Event{
		String name;
		int column;
		String type;
		String content;
	}
	
	class TableModel extends AbstractTableModel{
		private List<Event> events = new ArrayList<Event>();
		
		public void addEvent(Event event){
			events.add(event);
			fireTableRowsInserted(events.size()-1, events.size()-1);
		}
		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return COLUMN_CLASSES[columnIndex];
		}
		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}
		@Override
		public int getRowCount() {
			return events.size();
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Event event=events.get(rowIndex);
			switch(columnIndex){
				case 0: return event.name;
				case 1: return event.column;
				case 2: return event.type;
				case 3: return event.content;
			}
			return null;
		}
	}
	
	class Renderer extends DefaultTableCellRenderer{
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Object o = tableModel.getValueAt(row, 0);
			Color c;
			if (o.equals(CREATED)){
				c=Color.GREEN;
			} else if (o.equals(EXCLUDED)){
				c=Color.red;
			} else {
				c=Color.blue;
			}
			Component ret= super.getTableCellRendererComponent(table, 
					value==null? "" : value, isSelected, hasFocus, row, column);
			ret.setForeground(c);
			return ret;
		}
	}

	public EventsWindow(Frame parent, final TableFilterHeader header) {
		super(parent, "Filter Events", false);
		tableModel = new TableModel();
		JTable table = new JTable(tableModel);
		getContentPane().add(new JScrollPane(table));
		setSize(new Dimension(400, 300));
		header.addHeaderObserver(this);
		Renderer renderer = new Renderer();
		for (int i=0;i<COLUMN_NAMES.length;i++){
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				header.removeHeaderObserver(EventsWindow.this);
			}
		});
	}
	
	@Override
	public void tableFilterEditorCreated(TableFilterHeader header, 
			FilterEditor editor) {
		Event event = new Event();
		event.name=CREATED;
		event.column=editor.getModelPosition();
		tableModel.addEvent(event);
	}
	
	@Override
	public void tableFilterEditorExcluded(TableFilterHeader header, 
			FilterEditor editor) {
		Event event = new Event();
		event.name=EXCLUDED;
		event.column=editor.getModelPosition();
		tableModel.addEvent(event);
	}
	
	@Override
	public void tableFilterUpdated(TableFilterHeader header, 
			FilterEditor editor) {
		Event event = new Event();
		event.name="Updated";
		event.column=editor.getModelPosition();
		event.type = editor.getContent().getClass().getCanonicalName();
		event.content = editor.getContent().toString();
		tableModel.addEvent(event);
	}
	
}
