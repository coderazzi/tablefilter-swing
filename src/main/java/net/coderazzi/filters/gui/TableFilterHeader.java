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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.IFilterObserver;
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
 * <p>The editor associated to each column has the type {@link IFilterEditor}, 
 * and can be manipulated separately.</p>
 *
 * <p>The implementation relies on the {@link net.coderazzi.filters.gui.FiltersHandler} 
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
     * inside a scroll pane, and this header instance is not explicitly 
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
    private Color errorColor, gridColor, disabledForeground;
    
    /** Colors for selections on filter editors */
    private Color selectionBackground, selectionForeground, selectionColor;

    /** whether the user has explicitly provided colors/font */
    private boolean backgroundSet, foregroundSet, disabledColorSet;
    private boolean selectionBackgroundSet, selectionForegroundSet;
    private boolean selectionColorSet, gridColorSet, errorColorSet, fontSet;
    
    /** The helper to handle the location of the filter in the table header */
    private PositionHelper positionHelper = new PositionHelper(this);
        
	/** This is the total max number of visible rows (history PLUS choices) */
	int maxVisibleRows = FilterSettings.maxVisiblePopupRows;
    
    /**
     * The columnsController is a glue component, controlling the filters 
     * associated to each column
     */
    FilterColumnsControllerPanel columnsController;

    /**
     * The privately owned instance of FiltersHandler that conforms the filter 
     * defined by the TableFilterHeader
     */
    FiltersHandler filtersHandler = new FiltersHandler();

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
        this(null, null);
    }

    /** Basic constructor, using default {@link IParserModel} */
    public TableFilterHeader(JTable table) {
    	this(table, null);
    }

    /** Full constructor */
    public TableFilterHeader(JTable table, IParserModel parserModel) {
        super(new BorderLayout());
        if (parserModel==null) parserModel = FilterSettings.newParserModel();
        filtersHandler.setParserModel(parserModel);
        backgroundSet = foregroundSet = fontSet = false;
        setPosition(FilterSettings.headerPosition);
        setTable(table);
    }

    /**
     * <p>Attaches the table where the filtering will be applied.</p>
     *
     * <p>It will be created a row of editors, that follow the size and 
     * position of each of the columns in the table.</p>
     */
    public void setTable(JTable table) {
    	filtersHandler.enableNotifications(false);
    	JTable oldTable = getTable();
    	positionHelper.changeTable(oldTable, table);
        if (oldTable!=null){
        	oldTable.removeComponentListener(resizer);
        }
        filtersHandler.setTable(table);
        if (table==null){
            removeController();
            revalidate();
        }
        else{
        	updateAppearance();
            recreateController();
            table.addComponentListener(resizer);
        }
    	filtersHandler.enableNotifications(true);    	
    }
    
    /** Returns the table currently attached */
    public JTable getTable() {
        return filtersHandler==null? null : filtersHandler.getTable();
    }
    
    /** 
     * Sets the {@link IParserModel}, used to define the parsing of text
     * on the filter editors.
     */
    public void setParserModel(IParserModel parserModel){
    	filtersHandler.setParserModel(parserModel);
    }

    /**
     * Retrieves the current {@link IParserModel};
     * The returned reference is required to update properties like 
     * {@link Format} or {@link Comparator} instances associated to each
     * class, or whether to ignore case.
     */
    public IParserModel getParserModel(){
    	return filtersHandler.getParserModel();
    }
    
    /** Adds a filter -user specified- to the filter header */
    public void addFilter(IFilter... filter) {
        filtersHandler.addFilter(filter);
    }

    /** Adds a filter -user specified- to the filter header */
    public void removeFilter(IFilter... filter) {
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
     * @see  IFilterEditor#resetFilter()
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

    /** Updates the background on all components */
    @Override public Color getBackground(){
		Color c;
		if (backgroundSet){
			c=super.getBackground();
		} else {
			c = FilterSettings.backgroundColor;
			if (c==null){
				JTable table = getTable();
				if (table==null){
					c=super.getBackground();
				} else {
			    	Color background = table.getBackground();
		    		Color header = table.getTableHeader().getBackground();
		    		c = new Color((header.getRed() + background.getRed())/2,
		    				(header.getGreen() + background.getGreen())/2,
		    				(header.getBlue() + background.getBlue())/2);
				}
			}
		}
		return c;
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
    	disabledColorSet = true;

        if (columnsController != null){
            columnsController.setDisabledForeground(dfg);
        }
    }

    /** Sets the color used for disabled fields */
    public Color getDisabledForeground() {
    	return disabledForeground;
    }

    /** Sets the foreground color used to represent selected state */
    public void setSelectionForeground(Color fg) {
        this.selectionForeground = fg;
        this.selectionForegroundSet = true;

        if (columnsController != null)
            columnsController.setSelectionForeground(fg);
    }

    /**
     * <p>Returns the color set by default as foreground on selected filters</p>
     *
     * <p>Note that the color of each specific editor can be different, 
     * if the user customizes it directly</p>
     */
    public Color getSelectionForeground() {
        return selectionForeground;
    }

    /** Sets the background color used to represent selected state */
    public void setSelectionBackground(Color bg) {
        this.selectionBackground = bg;
        this.selectionBackgroundSet = true;
        
        if (columnsController != null)
            columnsController.setSelectionBackground(bg);
    }

    /**
     * <p>Returns the color set by default as foreground on selected filters</p>
     *
     * <p>Note that the color of each specific editor can be different, 
     * if the user customizes it directly</p>
     */
    public Color getSelectionBackground() {
        return selectionBackground;
    }

    /** Sets the color set by default as text selection on filters */
    public void setTextSelectionColor(Color c) {    	
        this.selectionColor = c;
        this.selectionColorSet = true;
        
        if (columnsController != null)
            columnsController.setTextSelectionColor(c);
    }

    /**
     * <p>Returns the color set by default as text selection on filters</p>
     *
     * <p>Note that the color of each specific editor can be different, 
     * if the user customizes it directly</p>
     */
    public Color getTextSelectionColor() {
        return selectionColor;
    }

    /**
     * Sets the foreground color used by the parsing text editors 
     * when there are errors on the filter expressions.
     */
    public void setErrorForeground(Color fg) {
        this.errorColor = fg;
        this.errorColorSet = true;

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

    /** Sets the color used to draw the header's grid */
    public void setGridColor(Color c) {
        this.gridColor = c;
        this.gridColorSet = true;

        if (columnsController != null)
            columnsController.setGridColor(c);
    }

    /**
     * <p>Returns the color set by default for the header's grid</p>
     */
    public Color getGridColor() {
        return gridColor;
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

    /** Customizes the editor, can be overridden for custom appearance */
    protected void customizeEditor(IFilterEditor editor) {
        editor.setForeground(getForeground());
        editor.setBackground(getBackground());
        editor.setErrorForeground(getErrorForeground());
        editor.setDisabledForeground(getDisabledForeground());
    	editor.setSelectionBackground(getSelectionBackground());
    	editor.setSelectionForeground(getSelectionForeground());
    	editor.setTextSelectionColor(getTextSelectionColor());
    	editor.setGridColor(getGridColor());
        editor.setMaxVisibleRows(maxVisibleRows);
        editor.setFont(getFont());
    }

    /** Returns the filter editor for the given column in the table model */
    public IFilterEditor getFilterEditor(int modelColumn) {
        return (columnsController == null) ? null : 
        	columnsController.getFilterEditor(
        			getTable().convertColumnIndexToView(modelColumn));
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
	 * Sets the auto choices flag. When set, all editors are automatically 
	 * populated with choices extracted from the table's content 
	 * -and updated as the table is updated-.
	 */
	public void setAutoChoices(AutoChoices set){
		filtersHandler.setAutoChoices(set);
	}
	
	/** Returns the auto choices flag */
	public AutoChoices getAutoChoices(){
		return filtersHandler.getAutoChoices();
	}

    /** Enables/Disables the filters */
    @Override public void setEnabled(boolean enabled) {
    	//it is not possible to call to super.setEnabled(enabled);
    	//the filter header can embed the the header of the table, which 
    	//would then become also disabled.
    	if (filtersHandler!=null){
        	filtersHandler.setEnabled(enabled);
    	}
    }
    
    /** Returns the current enable status */
    @Override public boolean isEnabled() {
    	return filtersHandler==null || filtersHandler.isEnabled();
    }

    /**
     * Sets the adaptive choices mode
     */
    public void setAdaptiveChoices(boolean enable) {
    	filtersHandler.setAdaptiveChoices(enable);
    }

    /**
     * Returns the adaptive choices mode
     */
    public boolean isAdaptiveChoices() {
        return filtersHandler.isAdaptiveChoices();
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
    
    @Override public void updateUI() {
    	super.updateUI();
    	if (columnsController!=null){
    		SwingUtilities.invokeLater(new Runnable() {				
				@Override public void run() {
					updateAppearance();
				}
			});
    	}
    }
    
    /** Updates the whole appearance: colors and font*/
    void updateAppearance(){
    	updateBackground();
    	updateForeground();
    	updateSelectionBackground();
    	updateSelectionForeground();
    	updateSelectionColor();
    	updateDisabledForeground();
    	updateErrorForeground();
    	updateGridColor();
    	updateFont();
    }
    
    /** Updates the font on all components */
    private void updateFont(){
		boolean set = fontSet;
		Font f;
		if (set){
			f=getFont();
		} else {
			f = FilterSettings.font;
			if (f==null){
        		f=getTable().getTableHeader().getFont();
        		f = f.deriveFont(f.getSize()*.9f);
			}
		}
		setFont(f);
		fontSet=set;
    }
    
    /** Updates the background on all components */
    private void updateBackground(){
		boolean set = backgroundSet;
		Color c;
		if (set){
			c=getBackground();
		} else {
			c = FilterSettings.backgroundColor;
			if (c==null){
				JTable table = getTable();
		    	Color background = table.getBackground();
	    		Color header = table.getTableHeader().getBackground();
	    		c = new Color((header.getRed() + background.getRed())/2,
	    				(header.getGreen() + background.getGreen())/2,
	    				(header.getBlue() + background.getBlue())/2);    				
			}
		}
		setBackground(c);
		backgroundSet=set;
    }
    
    /** Updates the foreground on all components */
    private void updateForeground(){
		boolean set = foregroundSet;
		Color c;
		if (set){
			c=getForeground();
		} else {
			c = FilterSettings.foregroundColor;
			if (c==null){
				c = getTable().getForeground();
			}
		}
		setForeground(c);
		foregroundSet=set;
    }
    
    /** Updates the selection background on all components */
    private void updateSelectionBackground(){
		boolean set = selectionBackgroundSet;
		Color c;
		if (set){
			c=getSelectionBackground();
		} else {
			c = FilterSettings.selectionBackgroundColor;
			if (c==null){
				c = getTable().getSelectionBackground();
			}
		}
		setSelectionBackground(c);
		selectionBackgroundSet=set;
    }

    /** Updates the selection foreground on all components */
    private void updateSelectionForeground(){
		boolean set = selectionForegroundSet;
		Color c;
		if (set){
			c=getSelectionForeground();
		} else {
			c = FilterSettings.selectionForegroundColor;
			if (c==null){
				c = getTable().getSelectionForeground();
			}
		}
		setSelectionForeground(c);
		selectionForegroundSet=set;
    }
    
    /** Updates the selection color on all components */
    private void updateSelectionColor(){
		boolean set = selectionColorSet;
		Color c;
		if (set){
			c=getTextSelectionColor();
		} else {
			c = FilterSettings.backgroundColor;
			if (c==null){
		    	Color a = getBackground();
	    		Color b = getSelectionBackground();
	    		c = new Color((a.getRed() + b.getRed())/2,
	    				(a.getGreen() + b.getGreen())/2,
	    				(a.getBlue() + b.getBlue())/2);    				
			}
		}
		setTextSelectionColor(c);
		selectionColorSet=set;
    }
    
    /** Updates the disabled foreground on all components */
    private void updateDisabledForeground(){
		boolean set = disabledColorSet;
		Color c;
		if (set){
			c=getDisabledForeground();
		} else {
			c = FilterSettings.disabledColor;
			if (c==null){
				c = getTable().getGridColor();
	        	if (c.equals(getBackground())){
	        		c=Color.lightGray;
	        	}
			}
		}
		setDisabledForeground(c);
		disabledColorSet=set;
    }

    /** Updates the grid color on all components */
    private void updateGridColor(){
		boolean set = gridColorSet;
		Color c;
		if (set){
			c=getGridColor();
		} else {
			c = FilterSettings.gridColor;
			if (c==null){
				c = getTable().getGridColor();
			}
		}
		setGridColor(c);
		gridColorSet=set;
    }

    /** Updates the error foreground on all components */
    private void updateErrorForeground(){
		boolean set = errorColorSet;
		Color c;
		if (set){
			c=getErrorForeground();
		} else {
			c = FilterSettings.errorColor;
			if (c==null){
        		c=Color.red;
			}
		}
		setErrorForeground(c);
		errorColorSet=set;
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
         * When a new model is set, all columns are first removed, and the new
         * ones then created. While columns are removed, the state of the filter
         * (filtersHandler) can changed between enabled and not enabled, but
         * it is needed to remember the state at the start of the cycle, to
         * create the new editors with the expected enable state.
         */
        private Boolean handlerEnabled;

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
            this.tableColumnModel = getTable().getColumnModel();

            boolean enabled=filtersHandler.isEnabled();
            int count = tableColumnModel.getColumnCount();
            columns = new ArrayList<FilterColumnPanel>(count);

            for (int i = 0; i < count; i++) {
                createColumn(i, enabled);
            }

            preferredSize = new Dimension(0, (count == 0) ? 0 : columns.get(0).h);
            placeComponents();
            tableColumnModel.addColumnModelListener(this);
        }

        /** Creates the FilterColumnPanel for the given column number */
        private void createColumn(int columnView, boolean enableIt) {
            int columnModel = getTable().convertColumnIndexToModel(columnView);
            FilterEditor editor = createEditor(columnModel, enableIt);
            FilterColumnPanel column = new FilterColumnPanel(
            		tableColumnModel.getColumn(columnView), editor);
            customizeEditor(column.editor);
            column.updateHeight();
            columns.add(column);
            add(column);
        }

        /** Creates an editor for the given column */
        private FilterEditor createEditor(int modelColumn, boolean enableIt) {        	
            FilterEditor ret = new FilterEditor(filtersHandler, 
            		modelColumn, getTable().getModel().getColumnClass(modelColumn));
            ret.getFilter().setEnabled(enableIt);
            filtersHandler.addFilterEditor(ret);
            return ret;
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

        /** Invokes resetFilter on all the editors. */
        public void resetFilters() {
            for (FilterColumnPanel column : columns) {
                column.editor.resetFilter();
            }
        }
        
    	/** Sets the maximum number of visible rows in the popup menu*/
    	public void setMaxVisibleRows(int maxVisibleRows) {
            for (FilterColumnPanel column : columns) {
                column.editor.setMaxVisibleRows(maxVisibleRows);
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

        public void setSelectionBackground(Color bg) {
        	super.setBackground(bg);

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    panel.editor.setSelectionBackground(bg);
        }

        public void setDisabledForeground(Color bg) {
            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    panel.editor.setDisabledForeground(bg);
        }

        public void setSelectionForeground(Color fg) {
            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    panel.editor.setSelectionForeground(fg);
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

        public void setTextSelectionColor(Color c) {

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                	panel.editor.setTextSelectionColor(c);
        }
        
        public void setGridColor(Color c) {

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                	panel.editor.setGridColor(c);
        }

        /** {@link TableColumnModelListener} interface */
        @Override public void columnMarginChanged(ChangeEvent e) {
            placeComponents();
        }

        /** {@link TableColumnModelListener} interface */
        @Override public void columnMoved(TableColumnModelEvent e) {

            if (e.getFromIndex() != e.getToIndex()) {
                FilterColumnPanel fcp = columns.remove(e.getFromIndex());
                columns.add(e.getToIndex(), fcp);
                placeComponents();
            }
        }

        /** {@link TableColumnModelListener} interface */
        @Override public void columnAdded(TableColumnModelEvent e) {

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
            if (handlerEnabled==null){
            	handlerEnabled=filtersHandler.isEnabled();
            }
            createColumn(e.getToIndex(), handlerEnabled);
            update();
        }

        /** {@link TableColumnModelListener} interface */
        @Override public void columnRemoved(TableColumnModelEvent e) {

            //see the comment on columnAdded
            filtersHandler.enableNotifications(false);
        	if (handlerEnabled==null){
        		handlerEnabled = filtersHandler.isEnabled();
        	}
            FilterColumnPanel fcp = columns.remove(e.getFromIndex());
            fcp.detach();
            remove(fcp);
            update();
        }

        /** {@link TableColumnModelListener} interface */
        @Override public void columnSelectionChanged(ListSelectionEvent e) {
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

        @Override public void run() {
            //see the comment on columnAdded
            if (--autoRun == 0 && getTable() != null){
            	handlerEnabled=null;
                updateHeight();
            }
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
                add(editor, BorderLayout.CENTER);
                h = getPreferredSize().height;
            	editor.getFilter().addFilterObserver(this);
                for (IFilterHeaderObserver observer : observers){
                	observer.tableFilterEditorCreated(TableFilterHeader.this, 
	                          editor, tc);
                }            	
                this.editor = editor;
                tc.addPropertyChangeListener(this);
            }

            /**
             * Performs any cleaning required before removing this component
             */
            public void detach() {

                if (editor != null) {
                    filtersHandler.removeFilterEditor(editor);
                    remove(editor);
                    editor.getFilter().removeFilterObserver(this);
                    for (IFilterHeaderObserver observer : observers){
                    	observer.tableFilterEditorExcluded(TableFilterHeader.this, 
                    			editor, tc);
                    }            	
                }
                tc.removePropertyChangeListener(this);
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
            	if (editor!=null){ //avoid sending the first update
	                for (IFilterHeaderObserver observer : observers){
	                	observer.tableFilterUpdated(TableFilterHeader.this, 
	                			editor, tc);
	                }
            	}
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
