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

package net.coderazzi.filters.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.IFilterObserver;
import net.coderazzi.filters.IFilterTextParser;
import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * <p>Implementation of a table filter that displays a set of editors 
 * associated to each table's column. 
 * This is the main Gui component in this library.</p>
 *
 * <p>These editors are moved and resized as the table's columns are resized, 
 * so this Swing component is better suited to be displayed atop, inline the 
 * {@link JTable}, or just below, using the same size -and resizing- as the 
 * table itself. The position can be automatically handled by the header 
 * itself -that is the default behavior-</p>
 *
 * <p>The editor associated to each column has the type {@link FilterEditor}, 
 * and can be manipulated separately.</p>
 *
 * <p>The implementation relies on the {@link net.coderazzi.filters.gui.TableFilter} 
 * class, please read its documentation to understand the requirements on the 
 * table and its model, and how it is affected by this filter</p>
 * 
 * <p>The default settings can be modified by using system properties or by 
 * setting values on the singleton {@link FilterSettings} instance</p>
 * 
 * <p>Providing a filter header to an existing table is as easy as doing:</p>
 * <code>TableFilterHeader filter = new TableFilterHeader(table);</code>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class TableFilterHeader extends JPanel {

	private static final long serialVersionUID = 5217701111228491294L;

    /**
     * <p>Location of the header in relation to the table</p>
     * <p>Note that this location is only meaningful when the table is set 
     * inside a scroll pane, and this header instance is not explicitely 
     * included in a container</p>
     * <ul>
     * <li>TOP: the filter is placed automatically above the table header.</li>
     * <li>INLINE: the filter is placed below the table header, 
     * above the table's content.</li>
     * <li>NONE: the filter is not automatically placed.</li>
     * </ul>
     *
     * @author  Luis M Pena - lu@coderazzi.net
     */
    public enum Position {
    	TOP, INLINE, NONE
    }

    /** Colors for the filters */
    private Color errorColor, disabledForeground = Color.lightGray;

    /** whether the user has explicitly provided colors/font */
    private boolean backgroundSet, foregroundSet, disabledSet, fontSet;
    
    /** 
     * If true, filter editors will automatically extract 
     * their content from the table's content 
     **/
    private boolean autoOptions=FilterSettings.autoOptions;

    /** The helper to handle the location of the filter in the table header */
    private PositionHelper positionHelper = new PositionHelper(this);
        
    /** Whether the component is enabled (cannot be delegated to the parent **/
    boolean enabled=true;

	/** This is the total max number of visible rows (history PLUS options) */
	int maxVisibleRows = FilterSettings.maxVisiblePopupRows;
    
    /**
     * The columnsController is a glue component, controlling the filters 
     * associated to each column
     */
    FilterColumnsControllerPanel columnsController;

    /** The filterTextParser to use on text based editors */
    IFilterTextParser filterTextParser;

    /** The associated table */
    JTable table;
    
    /**
     * The privately owned instance of TableFilter that conforms the filter 
     * defined by the TableFilterHeader
     */
    TableFilter filtersHandler = new TableFilter();

    /** The set of currently subscribed observers */
    Set<IFilterHeaderObserver> observers = new HashSet<IFilterHeaderObserver>();

    /** Helper to revalidate the controller when the table changes size */
    private ComponentAdapter resizer = new ComponentAdapter(){
        @Override
        public void componentResized(ComponentEvent e) {
        	if (columnsController!=null){
        		columnsController.revalidate();
        	}
        }  
    };

    /**
     * Constructor; the object is functional after a table is attached
     *
     * @see  TableFilterHeader#setTable(JTable)
     */
    public TableFilterHeader() {
        this(null);
    }

    /**
     * Constructor, using the default location
     *
     * @see  TableFilterHeader#setPosition(net.coderazzi.filters.gui.TableFilterHeader.Position)
     */
    public TableFilterHeader(JTable table) {
    	this(table, FilterSettings.headerPosition);
    }

    /**
     * Full constructor
     *
     * @see  TableFilterHeader#setTable(JTable)
     * @see  TableFilterHeader#setPosition(net.coderazzi.filters.gui.TableFilterHeader.Position)
     */
    public TableFilterHeader(JTable table, Position location) {
        super(new BorderLayout());
        backgroundSet = foregroundSet = fontSet = false;
        setPosition(location);
        setTable(table);
    }

    /**
     * <p>Attaches the table where the filtering will be applied.</p>
     *
     * <p>It will be created a row of editors, that follow the size and 
     * position of each of the columns in the table.</p>
     */
    public void setTable(JTable table) {
    	positionHelper.changeTable(this.table, table);
        if (this.table!=null){
            this.table.removeComponentListener(resizer);
        }
        filtersHandler.setTable(table);
        this.table = table;
        if (table==null){
            removeController();
        }
        else{
            if (!backgroundSet){
            	setBackground(suggestBackground());
            	backgroundSet=false;
            }
            if (!foregroundSet){
            	Color foreground = FilterSettings.headerForeground;
            	if (foreground==null){
            		foreground =table.getTableHeader().getForeground(); 
            	}
            	setForeground(foreground);
            	foregroundSet=false;
            }
            if (!disabledSet){
            	Color color = table.getGridColor();
            	if (color.equals(getBackground())){
            		color=Color.lightGray;
            	}
            	setDisabledForeground(color);
            	disabledSet=false;
            }
            if (!fontSet){
            	Font header=table.getTableHeader().getFont();
            	setFont(header.deriveFont(header.getSize()*.9f));
            	fontSet=false;
            }
            recreateController();
            this.table.addComponentListener(resizer);
            getTextParser().setTableModel(table.getModel());
        }
    }
    
    /** Suggests a background color, unless there is already one defined **/
    private Color suggestBackground(){
    	Color background = FilterSettings.headerBackground;
    	if (background==null){
    		Color header = table.getTableHeader().getBackground();
    		Color cells = table.getBackground();
    		background = new Color((header.getRed() + cells.getRed())/2,
    				(header.getGreen() + cells.getGreen())/2,
    				(header.getBlue() + cells.getBlue())/2);
    	}
    	return background;
    }

    /** Returns the table currently attached */
    public JTable getTable() {
        return table;
    }

    /** Adds a filter -user specified- to the filter header */
    public void addFilter(IFilter filter) {
        filtersHandler.addFilter(filter);
    }

    /** Adds a filter -user specified- to the filter header */
    public void removeFilter(IFilter filter) {
        filtersHandler.removeFilter(filter);
    }

    /**
     * <p>Defines the behaviour of the header concerning 
     * its position related to the table.</p>
     */
    public void setPosition(Position location) {
        positionHelper.setPosition(location);
    }

    /** Returns the mode currently associated to the TableHeader */
    public Position getPosition() {
        return positionHelper.getPosition();
    }

    /**
     * <p>Invokes resetFilter on all the editor filters.</p>
     *
     * This does not only implies invoking {@link FilterEditor#resetFilter()},
     * as it tries to reset the original editor options for enums 
     * and boolean types 
     *
     * @see  FilterEditor#resetFilter()
     */
    public void resetFilter() {

        if (columnsController != null) {
            filtersHandler.enableNotifications(false);
            columnsController.resetFilters();
            filtersHandler.enableNotifications(true);
        }
    }

    /** Method automatically invoked when the class ancestor changes */
    @Override public void addNotify() {
    	super.addNotify();
    	positionHelper.filterHeaderContainmentUpdate();
    }
    
    /**
     * removes the current columnsController
     * @return true if there was a controller
     */
    private boolean removeController() {

        if (columnsController != null) {
            columnsController.detach();
            remove(columnsController);
            columnsController = null;
            return true;
        }
        return false;
    }

    /**
     * creates/recreates the current columnsController
     */
    void recreateController() {
        filtersHandler.enableNotifications(false);
        removeController();
        columnsController = new FilterColumnsControllerPanel(getFont(), 
        		getForeground(), 
        		getBackground());
        columnsController.setEnabled(isEnabled());
        add(columnsController, BorderLayout.CENTER);
        revalidate();

        filtersHandler.enableNotifications(true);
    }

    /** Sets the background color used by the parsed-based editors. */
    @Override public void setBackground(Color bg) {
    	super.setBackground(bg);
    	backgroundSet=true;

        if (columnsController != null){
            columnsController.setBackground(bg);
        }
    }

    /** Sets the foreground color used by the editors. */
    @Override public void setForeground(Color fg) {
    	super.setForeground(fg);
    	foregroundSet=true;

        if (columnsController != null){
            columnsController.setForeground(fg);
        }
    }

    /** Sets the color used for disabled fields */
    public void setDisabledForeground(Color dfg) {
    	disabledForeground = dfg;
    	disabledSet = true;

        if (columnsController != null){
            columnsController.setDisabledForeground(dfg);
        }
    }

    /** Sets the color used for disabled fields */
    public Color getDisabledForeground() {
    	return disabledForeground;
    }

    /**
     * Sets the foreground color used by the parsing text editors 
     * when there are errors on the filter expressions.
     */
    public void setErrorForeground(Color fg) {
        this.errorColor = fg;

        if (columnsController != null)
            columnsController.setErrorForeground(fg);
    }

    /**
     * <p>Returns the color set by default as foreground on each text editor 
     * when the user commits any error on the filter expression.</p>
     *
     * <p>Note that the color of each specific editor can be different, 
     * if the user customizes it directly</p>
     */
    public Color getErrorForeground() {
        return errorColor;
    }

	/** 
	 * Sets the maximum number of visible rows in the popup menu 
	 * (a minimum is always enforced) 
	 **/
	public void setMaxVisibleRows(int maxVisibleRows) {
		this.maxVisibleRows = maxVisibleRows;
		if (columnsController!=null){
			columnsController.setMaxVisibleRows(maxVisibleRows);
		}
	}

	/** Returns the maximum number of visible rows in the popup menu*/
	public int getMaxVisibleRows() {
		return maxVisibleRows;
	}

    /** Creates an editor for the given column, customized to the associated type */
    FilterEditor createEditor(int modelColumn) {
        
        FilterEditor ret = new FilterEditor(filtersHandler, modelColumn);
        ret.setFormat(getTextParser().getFormat(
        		table.getModel().getColumnClass(modelColumn)));
        ret.setTextParser(getTextParser());
        
        if (!populateBasicEditorOptions(ret, true) && autoOptions){
        	ret.setAutoOptions(table.getModel());
        }
        return ret;
    }

    /** Customizes the editor, can be overridden for custom appearance */
    protected void customizeEditor(FilterEditor editor) {
        editor.setForeground(getForeground());
        editor.setBackground(getBackground());
        editor.setDisabledForeground(getDisabledForeground());
        editor.setMaxVisibleRows(maxVisibleRows);
        Color color =  getDisabledForeground();
        if (color!=null){
        	editor.setDisabledForeground(color);
        }
        color = getErrorForeground();
        if (color!=null){
        	editor.setErrorForeground(color);
        }
        editor.setFont(getFont());
    }

    /**
     * Populates basic editor options (boolean, enums).<br>
     * Returns false if the editor is not a boolean or enumeration
     */
    boolean populateBasicEditorOptions(FilterEditor editor, 
    		                           boolean fixMaxHistory) {
        
        Class<?> c = table.getModel().getColumnClass(editor.getFilterPosition());
        List<Object> options;
        if (c.equals(Boolean.class)){
        	options = new ArrayList<Object>(3);
        	options.add(true);
        	options.add(false);
        } else if (c.isEnum()){
        	Object[] enums = c.getEnumConstants();
        	options = new ArrayList<Object>(enums.length+1);
        	for (Object each : enums){
        		options.add(each);
        	}
        } else {
        	return false;
        }
    	editor.setOptions(options);
    	editor.setEditable(false);
    	if (fixMaxHistory && (options.size() <= 8)){
    		editor.setMaxHistory(0);
    	}
    	return true;
    }

    /** Returns the filter editor for the given column in the table model */
    public FilterEditor getFilterEditor(int modelColumn) {
        return (columnsController == null) ? null : 
        	columnsController.getFilterEditor(
        			table.convertColumnIndexToView(modelColumn));
    }

    /**
     * <p>Sets as renderer for the editor a generic {@link TableCellRenderer}, 
     * as used by the {@link JTable}</p>
     *
     * <p>This method allows reusing a renderer already written for a table 
     * as the editor's renderer, but it has an important restriction: it only 
     * works if the renderer does not depend on the cell coordinates</p>
     */
    public void setTableCellRenderer(final int modelColumn, 
                                     final TableCellRenderer renderer) {
    	getFilterEditor(modelColumn).setListCellRenderer(
    			new DefaultListCellRenderer() {

    		private static final long serialVersionUID = -5990815893475331934L;

			@Override public Component getListCellRendererComponent(JList list, 
					Object value, int index, boolean isSelected, 
					boolean cellHasFocus) {

				Component ret =  renderer.getTableCellRendererComponent(table, 
						value, isSelected, cellHasFocus, 1, modelColumn);
				if (!isSelected){
					ret.setBackground(list.getBackground());
					ret.setForeground(list.getForeground());
				}
				return ret;
            }
        });
    }

    /**
     * <p>Sets the parser to be used on text filter editors.</p>
     *
     * <p>This parser overrides any parser already set 
     * on the separate columns filters.</p>
     */
    public void setTextParser(IFilterTextParser parser) {
        filterTextParser = parser;

        if (columnsController != null) {
            filtersHandler.enableNotifications(false);
            columnsController.updateTextParser();
            filtersHandler.enableNotifications(true);
        }
    }

    /**
     * <p>Returns the parser used on plain text filters.</p>
     *
     * <p>Each column can have its own Parser, if setTextParser is used on 
     * the associated filter. In that case, it is needed to access each 
     * filter to obtain the used parser.</p>
     */
    public IFilterTextParser getTextParser() {
        if (filterTextParser == null) {
            filterTextParser = FilterSettings.newTextParser();
            if (table != null){
                filterTextParser.setTableModel(table.getModel());
            }
        }

        return filterTextParser;
    }

    /** Hides / makes visible the header */
	@Override public void setVisible(boolean flag) {
    	if (isVisible()!=flag){
    		positionHelper.headerVisibilityChanged(flag);
    	}
    	super.setVisible(flag);
		positionHelper.headerVisibilityChanged(flag);
	}
	
	/**
	 * Sets the auto options flag. When set, all editors are automatically 
	 * populated with options extracted from the table's content 
	 * -and updated as the table is updated-.
	 */
	public void setAutoOptions(boolean set){
		if (autoOptions!=set){
			autoOptions=set;
	        if (columnsController != null) {
	            filtersHandler.enableNotifications(false);
	            columnsController.setAutoOptions(set);
	            filtersHandler.enableNotifications(true);
	        }
		}
	}
	
	/** Returns the auto options flag */
	public boolean isAutoOptions(){
		return autoOptions;
	}
	
    /** Enables/Disables the filters */
    @Override public void setEnabled(boolean enabled) {
    	//it is not possible to call to super.setEnabled(enabled);
    	//the filter header can embed the the header of the table, which 
    	//would then become also disabled.
    	this.enabled=enabled;

        if (columnsController != null) {
            filtersHandler.enableNotifications(false);
            columnsController.setEnabled(enabled);
            filtersHandler.enableNotifications(true);
        }
    }
    
    /** Returns the current enable status */
    @Override public boolean isEnabled() {
    	return enabled;
    }

    /** Sets the font used on all the editors. */
    @Override public void setFont(Font font) {
        super.setFont(font);
        fontSet=true;
        if (columnsController != null) {
            columnsController.setFont(font);
            revalidate();
        }
    }
    
    /** Adds a new observer to the header */
    public void addHeaderObserver(IFilterHeaderObserver observer) {
        observers.add(observer);
    }

    /** Removes an existing observer from the header */
    public void removeHeaderObserver(IFilterHeaderObserver observer) {
    	observers.remove(observer);
    }
    
    /**
     * Class setting up together all the column filters<br>
     * Note that, while the TableFilterHeader handles columns using their 
     * model numbering, the FilterColumnsControllerPanel manages the columns 
     * as they are sorted in the Table. That is, if the user changes the 
     * order of two or more columns, this class reacts by 
     * reordering internal data structures
     */
    private class FilterColumnsControllerPanel extends JPanel 
    		implements TableColumnModelListener, Runnable {

		private static final long serialVersionUID = -5183169239497633085L;

		/** The list of columns, sorted in the view way */
        private List<FilterColumnPanel> columns;

        /** Preferred size of the component */
        private Dimension preferredSize;

        /**
         * The panel must keep a reference to the TableColumnModel, to be able 
         * to 'unregister' when the controller is destroyed.
         */
        private TableColumnModel tableColumnModel;

        /**
         * Variable keeping track of the number of times that the run() 
         * method is going to be invoked from the gui thread
         */
        private int autoRun;

        /**
         * Creates the controller for all the columns<br>
         * It will automatically create an editor of the current EditorMode 
         * for each column
         */
        public FilterColumnsControllerPanel(Font font, Color foreground, 
        		Color background) {
            super(null);
            super.setFont(font);
            super.setForeground(foreground);
            super.setBackground(background);
            this.tableColumnModel = table.getColumnModel();

            int count = tableColumnModel.getColumnCount();
            columns = new ArrayList<FilterColumnPanel>(count);

            for (int i = 0; i < count; i++) {
                createColumn(i);
            }

            preferredSize = new Dimension(0, (count == 0) ? 0 : columns.get(0).h);
            placeComponents();
            tableColumnModel.addColumnModelListener(this);
        }

        /** Creates the FilterColumnPanel for the given column number */
        private void createColumn(int columnView) {
            int columnModel = table.convertColumnIndexToModel(columnView);
            FilterEditor editor = createEditor(columnModel);
            filtersHandler.addFilter(editor.getFilter());

            FilterColumnPanel column = new FilterColumnPanel(
            		tableColumnModel.getColumn(columnView), editor);
            customizeEditor(column.editor);
            column.updateHeight();
            columns.add(column);
            add(column);
        }

        /** Detaches the current instance from any registered listeners */
        public void detach() {

            for (FilterColumnPanel column : columns)
                column.detach();

            tableColumnModel.removeColumnModelListener(this);
        }

        /** 
         * Returns the editor for the given column, 
         * or null if such column does not exist 
         **/
        public FilterEditor getFilterEditor(int viewColumn) {
            return (columns.size() > viewColumn) ? columns.get(viewColumn).editor : null;
        }

        /** Computes the proper preferred height -width is not important- */
        private void updateHeight() {
            int h = 0;

            for (FilterColumnPanel c : columns)
                h = Math.max(h, c.h);

            preferredSize.height = h;
            placeComponents();
            repaint();
        }

        /** Sets a new FilterTextParser on all the editors */
        public void updateTextParser() {

            for (FilterColumnPanel column : columns) {
                column.editor.setTextParser(filterTextParser);
            }
        }

        /**
         * Invokes resetFilter on all the editors.<br?
         * This does not only implies invoking 
         * {@link FilterEditor#resetFilter()}, as it tries to reset the 
         * original editor options for enums and boolean types 
		 */
        public void resetFilters() {
            for (FilterColumnPanel column : columns) {
                column.editor.resetFilter();
                populateBasicEditorOptions(column.editor, false);
            }
        }
        
    	/** Sets the maximum number of visible rows in the popup menu*/
    	public void setMaxVisibleRows(int maxVisibleRows) {
            for (FilterColumnPanel column : columns) {
                column.editor.setMaxVisibleRows(maxVisibleRows);
            }
    	}

        /** 
         * Sets the auto options flag, 
         * but not on editors associated to boolean/enumerations 
         **/
        public void setAutoOptions(boolean set) {
        	
            for (FilterColumnPanel column : columns) {
                Class<?> c = table.getModel().getColumnClass(
                		column.editor.getFilterPosition());
                if (!c.equals(Boolean.class) && !c.isEnum()){
                	column.editor.setAutoOptions(set? table.getModel() : null);
                }
            }
        }

        @Override public void setFont(Font font) {
            super.setFont(font);

            if ((this.columns != null) && !this.columns.isEmpty()) {

                for (FilterColumnPanel panel : this.columns)
                    panel.setFont(font);

                updateHeight();
            }
        }
        
        @Override public void setBackground(Color bg) {
        	super.setBackground(bg);

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    panel.editor.setBackground(bg);
        }

        public void setDisabledForeground(Color bg) {
            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    panel.editor.setDisabledForeground(bg);
        }

        @Override public void setForeground(Color fg) {
        	super.setForeground(fg);

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                	panel.editor.setForeground(fg);
        }

        public void setErrorForeground(Color fg) {

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                	panel.editor.setErrorForeground(fg);
        }

        @Override public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);

            for (FilterColumnPanel filter : this.columns)
                filter.setEnabled(enabled);
        }

        /** {@link TableColumnModelListener} interface */
        @Override
		public void columnMarginChanged(ChangeEvent e) {
            placeComponents();
        }

        /** {@link TableColumnModelListener} interface */
        @Override
		public void columnMoved(TableColumnModelEvent e) {

            if (e.getFromIndex() != e.getToIndex()) {
                FilterColumnPanel fcp = columns.remove(e.getFromIndex());
                columns.add(e.getToIndex(), fcp);
                placeComponents();
            }
        }

        /** {@link TableColumnModelListener} interface */
        @Override
		public void columnAdded(TableColumnModelEvent e) {

            //when adding or removing columns to the table model, or, in
            //general, when fireTableStructureChanged() is invoked on a
            //table model, all columns are removed, and the definitive ones
            //added.
            //To avoid sending update notifications to the table, which 
        	//may be quite time/CPU consuming, it is better to disable 
        	//the notifications and only send them after all columns 
        	//have been added or removed.
            //As there is no way to know when the last column is added 
        	//(or removed), the implementation disables the notifications 
        	//and request to be auto called eventually. This call (run()) 
        	//will happen when all the column modifications have concluded, 
        	//so then it is safe to reactivate the notifications
            filtersHandler.enableNotifications(false);
            createColumn(e.getToIndex());
            updateHeight();
            update();
        }

        /** {@link TableColumnModelListener} interface */
        @Override
		public void columnRemoved(TableColumnModelEvent e) {

            //see the comment on columnAdded
            filtersHandler.enableNotifications(false);
            FilterColumnPanel fcp = columns.remove(e.getFromIndex());
            fcp.detach();
            updateHeight();
            remove(fcp);
            update();
        }

        /** {@link TableColumnModelListener} interface */
        @Override
		public void columnSelectionChanged(ListSelectionEvent e) {
        	//nothing needed here
        }
        
        /**
         * Updates the columns. If this is the GUI thread, better wait
         * until all the events have been handled. Otherwise, do it
         * immediately, as it is not known how the normal/Gui thread
         * can interact
         */
        private void update(){
            autoRun += 1;
        	if (SwingUtilities.isEventDispatchThread()){
        		SwingUtilities.invokeLater(this);
        	} else {
        		run();
        	}
        }

        @Override
		public void run() {

            //see the comment on columnAdded
            if (--autoRun == 0 && table != null)
                getTextParser().setTableModel(table.getModel());
            filtersHandler.enableNotifications(true);
        }

        /**
         * Places all the components in line, respecting their preferred widths
         */
        void placeComponents() {
            int x = 0;

            for (FilterColumnPanel fcp : columns) {
                fcp.setBounds(x, 0, fcp.w, preferredSize.height);
                x += fcp.w;
            }

            revalidate();
        }

        @Override public Dimension getPreferredSize() {
            return preferredSize;
        }
        
        /**
         * Class controlling the filter applied to one specific column<br>
         * It resizes itself automatically as the associated table column is resized
         */
        private class FilterColumnPanel extends JPanel 
        		implements PropertyChangeListener, IFilterObserver {

			private static final long serialVersionUID = 6858728575542289815L;

			/** The associated editor */
            FilterEditor editor;

            /** Dimensions of the component */
            int w, h;

            /**
             * The TableColumn object, to which is registered to get property 
             * changes, in order to keep the same width.
             */
            TableColumn tc;

            /**
             * Constructor
             */
            public FilterColumnPanel(TableColumn tc, FilterEditor editor) {
                super(new BorderLayout());
                this.tc = tc;
                w = tc.getWidth();
                setFilterEditor(editor);
                tc.addPropertyChangeListener(this);
            }

            /**
             * Performs any cleaning required before removing this component
             */
            public void detach() {

                if (this.editor != null) {
                    filtersHandler.removeFilter(this.editor.getFilter());
                    removeEditor(this.editor);
                }

                tc.removePropertyChangeListener(this);
            }

            public void setFilterEditor(FilterEditor editor) {
                if (this.editor != null) {
                	removeEditor(this.editor);
                }
                this.editor = editor;
                add(this.editor, BorderLayout.CENTER);
                editor.setEnabled(enabled);
                h = getPreferredSize().height;
            	editor.getFilter().addFilterObserver(this);
                for (IFilterHeaderObserver observer : observers){
                	observer.tableFilterEditorCreated(TableFilterHeader.this, 
	                          editor);
                }            	
                repaint();
            }
            
            private void removeEditor(FilterEditor editorToRemove){
                remove(this.editor);
                editorToRemove.detach();
            	editorToRemove.getFilter().removeFilterObserver(this);
                for (IFilterHeaderObserver observer : observers){
                	observer.tableFilterEditorExcluded(TableFilterHeader.this, 
                			                           editorToRemove);
                }            	
            }

            @Override public void setFont(Font font) {
            	super.setFont(font);
            	if (editor!=null){
	                editor.setFont(font);
	                updateHeight();
            	}
            }
            
            public void updateHeight() {
                h = getPreferredSize().height;
                revalidate();
            }
            
            @Override public void filterUpdated(IFilter obs) {
                for (IFilterHeaderObserver observer : observers){
                	observer.tableFilterUpdated(TableFilterHeader.this, editor);
                }            	
            }
            
            @Override public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                editor.setEnabled(enabled);
            }

            /** Listening for changes on the width of the table' column */
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                int newW = tc.getWidth();

                if (w != newW) {
                    w = newW;
                    placeComponents();
                }
            }
        }
    }

}
