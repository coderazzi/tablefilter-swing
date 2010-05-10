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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.coderazzi.filters.IFilterObservable;
import net.coderazzi.filters.IFilterObserver;
import net.coderazzi.filters.TableFilter;
import net.coderazzi.filters.gui.editors.TableChoiceFilterEditor;
import net.coderazzi.filters.gui.editors.TextChoiceFilterEditor;
import net.coderazzi.filters.gui.editors.TextFilterEditor;
import net.coderazzi.filters.parser.IFilterTextParser;
import net.coderazzi.filters.parser.IdentifierInfo;
import net.coderazzi.filters.parser.generic.TableFilterHelper;
import net.coderazzi.filters.resources.Messages;


/**
 * <p>Implementation of a table filter that displays a set of editors associated to each table's
 * column. This is the main Gui component in this library.</p>
 *
 * <p>These editors are moved and resized as the table's columns are resized, so this Swing
 * component is better suited to be displayed atop or inline the {@link JTable}, or just below, 
 * using the same size -and resizing- as the table itself. Starting on version 1.3, the 
 * position can be automatically handled by the header itself, check the {@link Position}
 * enumeration.</p>
 *
 * <p>Each column can have a different type of editor associated, and there are four predefined
 * editors:
 *
 * <ul>
 * <li>BASIC editor: editor with {@link javax.swing.JComboBox} appearance. The user can enter free
 * text format, which is parsed using a given {@link
 * net.coderazzi.filters.parser.IFilterTextParser}. The combobox keeps track of the last used
 * entries, and, in case of columns with enumeration or boolean types, it is prepopulated with the
 * possible values.</li>
 * <li>SLIM editor: editor with {@link javax.swing.JTextField} appearance. It behaves as the BASIC
 * editor, but represented with a different (slimmer) swing component. It uses a contextual menu to
 * facilitate the input of last used expressions</li>
 * <li>CHOICE editor: editor with {@link javax.swing.JComboBox} appearance, where only predefined
 * filters can be chosen. The list of options is obtained directly from the table, or, if is it an
 * enumeration or a Boolean type, directly from the type itself.</li>
 * <li>NULL editor: this is a flat panel, used to disable filters in one or more columns.</li>
 * </ul>
 * </p>
 *
 * <p>Users can also provide customized editors, or modify the behaviour or appearance of the
 * created ones</p>
 *
 * <p>The implementation relies on the {@link net.coderazzi.filters.TableFilter} class, please
 * read its documentation to understand the requirements on the table and its model, and how it is
 * affected by this filter</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class TableFilterHeader extends JPanel {

	private static final long serialVersionUID = 5217701111228491294L;

	/** Default proportion size related to 'normal' cell fonts. */
    public static final float DEFAULT_FONT_PROPORTION = .9f; //in comparison to normal font

    private final static String[] BOOLEAN_CHOICES = {
            Messages.getString("TextParser.BooleanTrue"), Messages.getString("TextParser.BooleanFalse")
        };

    /**
     * <p>Editor mode for the filter editors associated to each column</p>
     *
     * <ul>
     * <li>NULL: no default filter.</li>
     * <li>SLIM: text-based, based on text field.</li>
     * <li>BASIC: like SLIM, but based on combobox. This is the default mode.</li>
     * <li>CHOICE: combobox where user can only select one of the provided expressions.</li>
     * </ul>
     *
     * @author  Luis M Pena - lu@coderazzi.net
     */
    public enum EditorMode {
        NULL, SLIM, BASIC, CHOICE
    }

    /**
     * <p>Location of the header in relation to the table</p>
     * <p>Note that this location is only meaningful when the table is set inside a scroll pane,
     * and this header instance is not explicitely included in a container</p>
     * <ul>
     * <li>TOP: the filter is placed automatically above the table header.</li>
     * <li>INLINE: the filter is placed below the table header, above the table's content.</li>
     * <li>NONE: the filter is not automatically placed. @since 2.1.2</li>
     * </ul>
     *
     * @author  Luis M Pena - lu@coderazzi.net
     * @since 1.3
     */
    public enum Position {
    	TOP, INLINE, NONE
    }

    /** Colors for the filters */
    private Color fg, bg, errorFg, errorBg;
    
    /** Whether the component is enabled (cannot be delegated to the parent **/
    private boolean enabled=true;

    /**
     * The columnsController is a glue component, controlling the filters associated to each column
     */
    FilterColumnsControllerPanel columnsController;

    /** The filterTextParser to use on text based editors */
    IFilterTextParser filterTextParser;

    /** This variable defines the type of editors to create by default */
    EditorMode mode;

    /** The associated table */
    JTable table;

    /**
     * The privately owned instance of TableFilter that conforms the filter defined by the
     * TableFilterHeader
     */
    TableFilter filtersHandler = new TableFilter();

    /** The set of currently subscribed observers */
    private Set<ITableFilterHeaderObserver> observers = new HashSet<ITableFilterHeaderObserver>();

    /** The associated locator to handle the location of the filter in the table header */
    private PositionHelper positionHelper = new PositionHelper(this);
        
    /**
     * Revalidate automatically the controller when the table changes size
     */
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
     * The default filter is the basic filter, with inline location,
     * unless Header.Mode / Header.Position system variables override it
     *
     * @see  TableFilterHeader#setTable(JTable)
     */
    public TableFilterHeader() {
        this((JTable)null);
    }

    /**
     * Constructor, using the basic filter, with default location.
     *
     * @see  TableFilterHeader#setTable(JTable)
     * 
     * @since 1.3
     */
    public TableFilterHeader(JTable table) {
        this(table, TableFilter.Settings.headerMode);
    }

    /**
     * Constructor; the object is functional after a table is attached
     * It uses default inline location
     *
     * @see  TableFilterHeader#setTable(JTable)
     */
    public TableFilterHeader(EditorMode mode) {
        this(null, mode);
    }

    /**
     * Constructor, using the default location
     *
     * @see  TableFilterHeader#setMode(net.coderazzi.filters.gui.TableFilterHeader.EditorMode)
     * @see  TableFilterHeader#setPosition(net.coderazzi.filters.gui.TableFilterHeader.Position)
     * 
     * @since 1.3
     */
    public TableFilterHeader(JTable table, EditorMode mode) {
    	this(table, mode, TableFilter.Settings.headerPosition);
    }

    /**
     * Full constructor
     *
     * @see  TableFilterHeader#setTable(JTable)
     * @see  TableFilterHeader#setMode(net.coderazzi.filters.gui.TableFilterHeader.EditorMode)
     * @see  TableFilterHeader#setPosition(net.coderazzi.filters.gui.TableFilterHeader.Position)
     * 
     * @since 1.3
     */
    public TableFilterHeader(JTable table, EditorMode mode, Position location) {
        super(new BorderLayout());
        
        Font italicFont = UIManager.getFont("TableHeader.font").deriveFont(Font.ITALIC);
        setFont(italicFont.deriveFont(italicFont.getSize2D() * DEFAULT_FONT_PROPORTION));
        setMode(mode);
        setPosition(location);
        setTable(table);
    }

    /**
     * <p>Attaches the table where the filtering will be applied.</p>
     *
     * <p>It will be created a row of editors, of the type currently set {@link
     * TableFilterHeader.EditorMode} that follow the size and position of each of the columns in the
     * table.</p>
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
            recreateController();
            this.table.addComponentListener(resizer);
            columnsController.updateIdentifiers();
        }
    }

    /**
     * Returns the table currently attached
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Returns the internal table filter. This can be used to attach additional filters.
     */
    public TableFilter getTableFilter() {
        return filtersHandler;
    }

    /**
     * <p>Sets a new table filter.</p>
     *
     * <p>The filters associated to the initial {@link TableFilter} are transferred to the new
     * one.</p>
     */
    public void setTableFilter(TableFilter filter) {
        for (IFilterObservable obs : filtersHandler.getFilterObservables()) {
            filter.addFilterObservable(obs);
        }
        filter.setTable(filtersHandler.getTable());
        filtersHandler.detach();
        filtersHandler = filter;
    }


    /**
     * <p>Defines the behaviour of the header concerning its position related to the table.</p>
     * @since 1.3
     */
    public void setPosition(Position location) {
        positionHelper.setPosition(location);
    }


    /**
     * <p>Returns the mode currently associated to the TableHeader</p>
     * @since 1.3
     */
    public Position getPosition() {
        return positionHelper.getPosition();
    }

    /**
     * <p>Defines the type of the filter editors associated by default to the columns.</p>
     *
     * <p>If the filter editors have been already created, they are not removed, the mode will only
     * be used on newly created editors (like when adding columns to the table)</p>
     *
     * @see  TableFilterHeader#resetMode(net.coderazzi.filters.gui.TableFilterHeader.EditorMode)
     */
    public void setMode(EditorMode mode) {
        this.mode = mode;
    }


    /**
     * Returns the mode currently associated to the TableHeader
     */
    public EditorMode getMode() {
        return mode;
    }

    /**
     * Defines the type of filter editors associated by default to the columns, and recreates all
     * the editors with the given type
     *
     * @see  TableFilterHeader#setMode(net.coderazzi.filters.gui.TableFilterHeader.EditorMode)
     */
    public void resetMode(EditorMode mode) {
        this.mode = mode;
        filtersHandler.enableNotifications(false);
        if (removeController()){

        	if (mode == EditorMode.CHOICE) {
        		filtersHandler.sendPendingNotifications();
        	}

        	recreateController();
        }
        filtersHandler.enableNotifications(true);
    }

    /**
     * <p>Invokes resetFilter on all the editor filters.</p>
     *
     * <p>Note that the exact semantics depend on the exact editor type.</p>
     *
     * @see  ITableFilterEditor#resetFilter()
     * @since 2.1 -before it was called resetFilters-
     */
    public void resetFilter() {

        if (columnsController != null) {
            filtersHandler.enableNotifications(false);
            columnsController.resetFilters();
            filtersHandler.enableNotifications(true);
        }
    }

    /**
     * <p>Invokes updateFilter on all the editor filters.</p>
     *
     * <p>Note that the exact semantics depend on the exact editor type.</p>
     *
     * @see  ITableFilterEditor#updateFilter()
     */
    public void updateFilter() {

        if (columnsController != null) {
            filtersHandler.enableNotifications(false);
            columnsController.updateFilters();
            filtersHandler.enableNotifications(true);
        }
    }

    /**
     * Method automatically invoked when the class ancestor changes
     */
    @Override
    public void addNotify() {
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
        columnsController = new FilterColumnsControllerPanel();
        columnsController.setFont(getFont());
        columnsController.setEnabled(isEnabled());
        add(columnsController, BorderLayout.CENTER);
        revalidate();

        //columnsController.repaint();
        updateFilter();
        filtersHandler.enableNotifications(true);
    }

    /**
     * <p>Sets the background color used by the parsed-based editors.</p>
     */
    public void setFilterBackground(Color bg) {
        this.bg = bg;

        if (columnsController != null)
            columnsController.setFilterBackground(bg);
    }

    /**
     * <p>Returns the color set by default as background on each editor.</p>
     *
     * <p>Note that the color of each specific editor can be different, if the user customizes it
     * directly.</p>
     */
    public Color getFilterBackground() {
        return bg;
    }


    /**
     * <p>Sets the foreground color used by the editors.</p>
     *
     * <p>This method is a helper, invoking {@link JComponent#setForeground(Color)} on each editor
     * component, already created or not.</p>
     */
    public void setFilterForeground(Color fg) {
        this.fg = fg;

        if (columnsController != null)
            columnsController.setFilterForeground(fg);
    }

    /**
     * <p>Returns the color set by default as foreground on each editor.</p>
     *
     * <p>Note that the color of each specific editor can be different, if the user customizes it
     * directly.</p>
     */
    public Color getFilterForeground() {
        return fg;
    }


    /**
     * <p>Sets the foreground color used by the parsing text editors when there are error on the
     * filter expressions.</p>
     *
     * @see  ITableFilterTextBasedEditor#setErrorForeground(Color)
     */
    public void setErrorForeground(Color fg) {
        this.errorFg = fg;

        if (columnsController != null)
            columnsController.setErrorForeground(fg);
    }

    /**
     * <p>Returns the color set by default as foreground on each text editor when the user commits
     * any error on the filter expression.</p>
     *
     * <p>Note that the color of each specific editor can be different, if the user customizes it
     * directly</p>
     */
    public Color getErrorForeground() {
        return errorFg;
    }


    /**
     * <p>Sets the background color used by the parsing text editors when there are error on the
     * filter expressions.</p>
     *
     * @see  ITableFilterTextBasedEditor#setErrorBackground(Color)
     */
    public void setErrorBackground(Color bg) {
        this.errorBg = bg;

        if (columnsController != null)
            columnsController.setErrorBackground(bg);
    }

    /**
     * <p>Returns the color set by default as background on each text editor when the user commits
     * any error on the filter expression.</p>
     *
     * <p>Note that the color of each specific editor can be different, if the user customizes it
     * directly</p>
     */
    public Color getErrorBackground() {
        return errorFg;
    }


    /**
     * Creates an editor, if needed, for the given column and with the specified mode If there is
     * already such an editor, it is just returned the existing one
     */
    ITableFilterEditor createEditor(EditorMode mode, int modelColumn) {
        ITableFilterEditor old = getFilterEditor(modelColumn);

        switch (mode) {

        case NULL:
            return createNullEditor(old, modelColumn);

        case SLIM:
            return createSlimEditor(old, modelColumn);

        case BASIC:
            return createThickEditor(old, modelColumn);

        case CHOICE:
            return createChoiceEditor(old, modelColumn);
        }

        return null;
    }

    /**
     * Creates a TextChoiceFilterEditor editor, if needed, for the given column If there is already
     * a TextChoiceFilterEditor editor in this column, it is just returned the existing one
     */
    private ITableFilterEditor createThickEditor(ITableFilterEditor old, int modelColumn) {

        if (old instanceof TextChoiceFilterEditor)
            return old;

        TextChoiceFilterEditor ret = new TextChoiceFilterEditor(getTextParser());
        ret.setFilterPosition(modelColumn);

        Class<?> c = table.getModel().getColumnClass(modelColumn);

        //prepopulate values for Boolean and Enumeration types
        if (c == Boolean.class) {
        	ret.suggestChoices(BOOLEAN_CHOICES);
        } else if (c.isEnum()) {
            Object[] values = c.getEnumConstants();
            String[] choices = new String[values.length];

            for (int i = 0; i < values.length; i++){
                choices[i] = values[i].toString();
            }
        	ret.suggestChoices(choices);
        }

        formatEditor(ret);

        return ret;
    }

    /**
     * Creates a TextFilterEditor editor, if needed, for the given column If there is already a
     * TextFilterEditor editor in this column, it is just returned the existing one
     */
    private ITableFilterEditor createSlimEditor(ITableFilterEditor old, int modelColumn) {

        if (old instanceof TextFilterEditor)
            return old;

        ITableFilterEditor ret = new TextFilterEditor(getTextParser());
        ret.setFilterPosition(modelColumn);
        formatEditor(ret);

        return ret;
    }

    /**
     * Creates a TableChoiceFilterEditor editor, if needed, for the given column If there is already
     * a TableChoiceFilterEditor editor in this column, it is just returned the existing one
     */
    private ITableFilterEditor createChoiceEditor(ITableFilterEditor old, int modelColumn) {

        if (old instanceof TableChoiceFilterEditor) {
            return old;
        }

        Class<?> c = table.getModel().getColumnClass(modelColumn);
        TableChoiceFilterEditor ret;

        // for boolean and enumerations, the created type should be, in fact, a ChoiceFilterEditor,
        // but creating TableChoiceFilterEditor instances simplify the interface
        if (c == Boolean.class) {
            ret = new TableChoiceFilterEditor(true, false);
            ret.setFilterPosition(modelColumn);
        } else if (c.isEnum()) {
            ret = new TableChoiceFilterEditor(c.getEnumConstants());
            ret.setFilterPosition(modelColumn);
        } else {
            ret = new TableChoiceFilterEditor(table, modelColumn);
        }

        formatEditor(ret);

        return ret;
    }

    /**
     * Creates a NullFilterEditor editor, if needed, for the given column If there is already a
     * NullFilterEditor editor in this column, it is just returned the existing one
     */
    private ITableFilterEditor createNullEditor(ITableFilterEditor old, int modelColumn) {
        return (old instanceof NullFilterEditor) ? old : new NullFilterEditor(modelColumn);
    }

    /**
     * Applies any color/font customizations to the passed editor
     */
    private void formatEditor(ITableFilterEditor editor) {

        if (editor instanceof ITableFilterTextBasedEditor) {

            ITableFilterTextBasedEditor peditor=(ITableFilterTextBasedEditor)editor;
            if (bg != null) {
                peditor.setFilterBackground(bg);
            }

            if (fg != null) {
                peditor.setFilterForeground(fg);
            }

            if (errorBg != null) {
                peditor.setErrorBackground(errorBg);
            }

            if (errorFg != null) {
                peditor.setErrorForeground(errorFg);
            }
        }

        editor.getComponent().setFont(getFont());
    }


    /**
     * <p>Returns the filter editor for the given column.</p>
     *
     * <p>By default, all FilterEditors are instances of {@link
     * net.coderazzi.filters.gui.editors.TextChoiceFilterEditor}</p>
     *
     * @param  modelColumn  The column number in the table model
     */
    public ITableFilterEditor getFilterEditor(int modelColumn) {
        return (columnsController == null)
            ? null : columnsController.getFilterEditor(table.convertColumnIndexToView(modelColumn));
    }


    /**
     * Sets a specific filter editor for a given column.</p>
     * The editor receives the associated filter position, and, if it is
     * text based and has no associated text parser, the current filter text
     * parser.</p>
     *
     * @param  modelColumn  The column number in the table model
     * @param  editor       The filter editor, which can be null to place a {@link EditorMode} NULL
     *                      editor.
     */
    public void setFilterEditor(int modelColumn, ITableFilterEditor editor) {
        if (editor == null) {
            editor = createEditor(EditorMode.NULL, modelColumn);
        } else {
        	editor.setFilterPosition(modelColumn);
        	if (editor instanceof ITableFilterTextBasedEditor){
        		ITableFilterTextBasedEditor textEditor = (ITableFilterTextBasedEditor) editor;
        		if (textEditor.getTextParser()==null){
        			textEditor.setTextParser(getTextParser());
        		}
        	}
        }
        columnsController.setFilterEditor(table.convertColumnIndexToView(modelColumn), editor);
    }


    /**
     * <p>Sets a filter editor of type {@link
     * net.coderazzi.filters.gui.editors.TableChoiceFilterEditor} on the given column; this editor
     * is a {@link javax.swing.JComboBox} which only allows the user to enter predefined options.
     * The initial options are the values in the table model for the given column.</p>
     *
     * <p>If the given column already contains an editor of the given type, the existing editor is
     * returned.</p>
     *
     * @param   modelColumn  The column number in the table model
     *
     * @return  the created {@link net.coderazzi.filters.gui.editors.TableChoiceFilterEditor}
     *          editor, which can be then directly manipulated by the user
     */
    public TableChoiceFilterEditor setChoiceFilterEditor(int modelColumn) {
        TableChoiceFilterEditor editor = (TableChoiceFilterEditor) createEditor(EditorMode.CHOICE,
                modelColumn);
        setFilterEditor(modelColumn, editor);

        return editor;
    }


    /**
     * <p>Sets a filter editor of type {@link net.coderazzi.filters.gui.editors.TextFilterEditor}
     * on the given column; this editor is a {@link javax.swing.JTextField} which allows the user to
     * enter complex filter expressions, and includes a contextual menu to facilitate the input of
     * older entries</p>
     *
     * <p>If the given column already contains an editor of the given type, the existing editor is
     * returned.</p>
     *
     * @param   modelColumn  The column number in the table model
     *
     * @return  the created {@link net.coderazzi.filters.gui.editors.TextFilterEditor} editor,
     *          which can be then directly manipulated by the user
     */
    public TextFilterEditor setSlimFilterEditor(int modelColumn) {
        TextFilterEditor editor = (TextFilterEditor) createEditor(EditorMode.SLIM, modelColumn);
        setFilterEditor(modelColumn, editor);

        return editor;
    }


    /**
     * <p>Sets a filter editor of type {@link
     * net.coderazzi.filters.gui.editors.TextChoiceFilterEditor} on the given column; this editor
     * is a {@link javax.swing.JComboBox} which allows the user to enter complex filter expressions,
     * with a historic of the last entries</p>
     *
     * <p>If the given column already contains an editor of the given type, the existing editor is
     * returned.</p>
     *
     * @param   modelColumn  The column number in the table model
     *
     * @return  the created {@link net.coderazzi.filters.gui.editors.TextChoiceFilterEditor}
     *          editor, which can be then directly manipulated by the user
     */
    public TextChoiceFilterEditor setBasicFilterEditor(int modelColumn) {
        TextChoiceFilterEditor editor = (TextChoiceFilterEditor) createEditor(EditorMode.BASIC,
                modelColumn);
        setFilterEditor(modelColumn, editor);

        return editor;
    }


    /**
     * <p>Sets the parser to be used on text filter editors.</p>
     *
     * <p>This parser overrides any parser already set on the separate columns filters.</p>
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
     * <p>By default, it is an instance of {@link
     * net.coderazzi.filters.parser.generic.FilterTextParser}</p>
     *
     * <p>Each column can have its own Parser, if setTextParser is used on the associated filter. In
     * that case, it is needed to access each filter to obtain the used parser.</p>
     *
     * @see  ITableFilterTextBasedEditor#setTextParser(IFilterTextParser)
     */
    public IFilterTextParser getTextParser() {
        if (filterTextParser == null) {
            filterTextParser = TableFilter.Settings.newTextParser();
            if (table != null){
                filterTextParser.setIdentifiers(
                    TableFilterHelper.extractIdentifiersFromTableColumnNames(table.getModel()));
            }
        }

        return filterTextParser;
    }


	@Override public void setVisible(boolean flag) {
    	if (isVisible()!=flag){
    		positionHelper.headerVisibilityChanged(flag);
    	}
    	super.setVisible(flag);
		positionHelper.headerVisibilityChanged(flag);
	}
	
    /**
     * Enables/Disables programatically the filters
     *
     * @see  JComponent#setEnabled(boolean)
     */
    @Override public void setEnabled(boolean enabled) {
    	//it is not possible to call to super.setEnabled(enabled);
    	//the filter header can embed the the header of the table, which would then
    	//become also disabled.
    	this.enabled=enabled;

        if (columnsController != null) {
            filtersHandler.enableNotifications(false);
            columnsController.setEnabled(enabled);
            filtersHandler.enableNotifications(true);
        }
    }
    
    @Override
    public boolean isEnabled() {
    	return enabled;
    }

    /**
     * Sets the font used on all the editors.
     *
     * @see  JComponent#setFont
     */
    @Override public void setFont(Font font) {
        super.setFont(font);

        if (columnsController != null) {
            columnsController.setFont(font);
            revalidate();
        }
    }
    
    /**
     * Adds a new observer to the header
     * @param observer
     * @since version 2.0
     */
    public void addHeaderObserver(ITableFilterHeaderObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an existing observer from the header
     * @param observer
     * @since version 2.0
     */
    public void removeHeaderObserver(ITableFilterHeaderObserver observer) {
    	observers.remove(observer);
    }
    
    /**
     * Class setting up together all the column filters Note that, while the TableFilterHeader
     * handles columns using their model numbering, the FilterColumnsControllerPanel manages the
     * columns as they are sorted in the Table. That is, if the user changes the order of two or
     * more columns, this class reacts by reordering internal data structures
     */
    private class FilterColumnsControllerPanel extends JPanel implements TableColumnModelListener,
        Runnable {

		private static final long serialVersionUID = -5183169239497633085L;

		/** The list of columns, sorted in the view way */
        private List<FilterColumnPanel> columns;

        /** Preferred size of the component */
        private Dimension preferredSize;

        /**
         * The panel must keep a reference to the TableColumnModel, to be able to 'unregister' when
         * the controller is destroyed.
         */
        private TableColumnModel tableColumnModel;

        /**
         * Variable keeping track of the number of times that the run() method is going to be
         * invoked from the gui thread
         */
        private int autoRun;


        /**
         * Creates the controller for all the columns It will automatically create an editor of the
         * current EditorMode for each column
         */
        public FilterColumnsControllerPanel() {
            super(null);
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


        /**
         * Creates the FilterColumnPanel for the given column number
         */
        private void createColumn(int columnView) {
            int columnModel = table.convertColumnIndexToModel(columnView);
            ITableFilterEditor editor = createEditor(mode, columnModel);
            filtersHandler.addFilterObservable(editor.getFilterObservable());

            FilterColumnPanel column = new FilterColumnPanel(tableColumnModel.getColumn(columnView),
                    editor);
            columns.add(column);
            add(column);
        }


        /**
         * Detachs the current instance from any registered listeners
         */
        public void detach() {

            for (FilterColumnPanel column : columns)
                column.detach();

            tableColumnModel.removeColumnModelListener(this);
        }

        /**
         * Returns the editor for the given column, or null if such editor/column does not exist
         */
        public ITableFilterEditor getFilterEditor(int viewColumn) {
            return (columns.size() > viewColumn) ? columns.get(viewColumn).editor : null;
        }


        /**
         * Sets a new FilterEditor on a column, cleaning properly the one before.
         */
        public void setFilterEditor(int viewColumn, ITableFilterEditor editor) {

            ITableFilterEditor oldEditor = getFilterEditor(viewColumn);
            if (oldEditor != editor) {

                if (oldEditor != null)
                    filtersHandler.removeFilterObservable(oldEditor.getFilterObservable());

                filtersHandler.addFilterObservable(editor.getFilterObservable());

                columns.get(viewColumn).setFilterEditor(editor);

                updateHeight();
            }
        }

        /**
         * Computes the proper preferred size -only the height, the width is not important-
         */
        private void updateHeight() {
            int h = 0;

            for (FilterColumnPanel c : columns)
                h = Math.max(h, c.h);

            preferredSize.height = h;
            placeComponents();
            repaint();
        }

        /**
         * Sets a new FilterTextParser on any ITableFilterTextBasedEditor editors
         */
        public void updateTextParser() {

            for (FilterColumnPanel column : columns) {
                ITableFilterEditor editor = column.editor;

                if (editor instanceof ITableFilterTextBasedEditor) {
                    ((ITableFilterTextBasedEditor) editor).setTextParser(filterTextParser);
                }
            }
        }

        /**
         * Invokes updateFilter on all the editors
         */
        public void updateFilters() {

            for (FilterColumnPanel column : columns) {
                column.editor.updateFilter();
            }
        }

        /**
         * Invokes resetFilter on all the editors
         */
        public void resetFilters() {

            for (FilterColumnPanel column : columns) {
                column.editor.resetFilter();
            }
        }

        /**
         * Updates the identifier information, on the current text parser, if any, and on all the
         * ITableFilterTextBasedEditor editors
         */
        public void updateIdentifiers() {
            List<IdentifierInfo> info = TableFilterHelper.extractIdentifiersFromTableColumnNames(
                    table.getModel());
            if (filterTextParser != null) {
                filterTextParser.setIdentifiers(info);
            }
            for (FilterColumnPanel column : columns) {
                if (column.editor instanceof ITableFilterTextBasedEditor)
                    ((ITableFilterTextBasedEditor) column.editor).getTextParser().setIdentifiers(
                        info);
            }
        }


        @Override public void setFont(Font font) {
            super.setFont(font);

            if ((this.columns != null) && !this.columns.isEmpty()) {

                for (FilterColumnPanel panel : this.columns)
                    panel.setFilterFont(font);

                preferredSize.height = columns.get(0).h;
            }
        }

        public void setFilterBackground(Color bg) {

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    if ((panel.editor instanceof ITableFilterTextBasedEditor))
                        ((ITableFilterTextBasedEditor)panel.editor).setFilterBackground(bg);
        }

        public void setFilterForeground(Color fg) {

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    if ((panel.editor instanceof ITableFilterTextBasedEditor))
                        ((ITableFilterTextBasedEditor)panel.editor).setFilterForeground(fg);
        }

        public void setErrorForeground(Color fg) {

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    if (panel.editor instanceof ITableFilterTextBasedEditor)
                        ((ITableFilterTextBasedEditor) panel.editor).setErrorForeground(fg);
        }

        public void setErrorBackground(Color bg) {

            if (columns != null)
                for (FilterColumnPanel panel : this.columns)
                    if (panel.editor instanceof ITableFilterTextBasedEditor)
                        ((ITableFilterTextBasedEditor) panel.editor).setErrorBackground(bg);
        }

        @Override public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);

            for (FilterColumnPanel filter : this.columns)
                filter.setEnabled(enabled);
        }


        /**
         * TableColumnModelListener interface
         */
        public void columnMarginChanged(ChangeEvent e) {
            placeComponents();
        }


        /**
         * TableColumnModelListener interface
         */
        public void columnMoved(TableColumnModelEvent e) {

            if (e.getFromIndex() != e.getToIndex()) {
                FilterColumnPanel fcp = columns.remove(e.getFromIndex());
                columns.add(e.getToIndex(), fcp);
                placeComponents();
            }
        }


        /**
         * TableColumnModelListener interface
         */
        public void columnAdded(TableColumnModelEvent e) {

            //when adding or removing columns to the table model, or, in
            //general, when fireTableStructureChanged() is invoked on a
            //table model, all columns are removed, and the definitive ones
            //added.
            //To avoid sending update notifications to the table, which can be
            //quite time/CPU consuming, it is better to disable the notifications
            //and only send them after all columns have been added or removed.
            //As there is no way to know when the last column is added (or removed),
            //the implementation disables the notifications and request to be
            //auto called eventually. This call (run()) will happen when all the
            //column modifications have concluded, so then it is safe to reactivate
            //the notifications
            filtersHandler.enableNotifications(false);
            createColumn(e.getToIndex());
            updateHeight();
            update();
        }


        /**
         * TableColumnModelListener interface
         */
        public void columnRemoved(TableColumnModelEvent e) {

            //see the comment on columnAdded
            filtersHandler.enableNotifications(false);
            FilterColumnPanel fcp = columns.remove(e.getFromIndex());
            fcp.detach();
            updateHeight();
            remove(fcp);
            update();
        }


        /**
         * TableColumnModelListener interface
         */
        public void columnSelectionChanged(ListSelectionEvent e) {
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


        /**
         * See the comments on columnAdded
         */
        public void run() {

            //see the comment on columnAdded
            if (--autoRun == 0 && table != null)
                updateIdentifiers();
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
         * Class controlling the filter applied to one specific column It resizes itself
         * automatically as the associated table column is resized
         */
        private class FilterColumnPanel extends JPanel implements PropertyChangeListener, ITableFilterEditorObserver {

			private static final long serialVersionUID = 6858728575542289815L;

			/** The associated editor */
            ITableFilterEditor editor;

            /** Dimensions of the component */
            int w, h;

            /**
             * The TableColumn object, to which is registered to get property changes, in order to
             * keep the same width.
             */
            TableColumn tc;


            /**
             * Constructor
             */
            public FilterColumnPanel(TableColumn tc, ITableFilterEditor editor) {
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
                    filtersHandler.removeFilterObservable(this.editor.getFilterObservable());
                    removeEditor(this.editor);
                }

                tc.removePropertyChangeListener(this);
            }

            public void setFilterEditor(ITableFilterEditor editor) {
                if (this.editor != null) {
                	removeEditor(this.editor);
                }
                this.editor = editor;
                add(this.editor.getComponent(), BorderLayout.CENTER);
                h = getPreferredSize().height;
            	editor.addTableFilterObserver(this);
                for (ITableFilterHeaderObserver observer : observers){
                	observer.tableFilterEditorCreated(TableFilterHeader.this, editor);
                }            	
                repaint();
            }
            
            private void removeEditor(ITableFilterEditor editor){
                remove(this.editor.getComponent());
                editor.detach();
            	editor.removeTableFilterObserver(this);
                for (ITableFilterHeaderObserver observer : observers){
                	observer.tableFilterEditorExcluded(TableFilterHeader.this, editor);
                }            	
            }

            public void setFilterFont(Font font) {
                editor.getComponent().setFont(font);
                h = getPreferredSize().height;
            }

            public void tableFilterUpdated(ITableFilterEditor editor, Object newValue) {
                for (ITableFilterHeaderObserver observer : observers){
                	observer.tableFilterUpdated(TableFilterHeader.this, editor, newValue);
                }            	
            }
            
            @Override public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                editor.getComponent().setEnabled(enabled);
            }

            /**
             * PropertyChangeListener, listening for changes on the width of the table' column
             */
            public void propertyChange(PropertyChangeEvent evt) {
                int newW = tc.getWidth();

                if (w != newW) {
                    w = newW;
                    placeComponents();
                }
            }
        }
    }

    /**
     * Class to handle null (unused) editors
     */
    private static class NullFilterEditor extends JPanel implements ITableFilterEditor,
        IFilterObservable {

		private static final long serialVersionUID = 1802486919152113003L;

		private int modelColumn;

		public NullFilterEditor(int modelColumn) {
			this.modelColumn=modelColumn;
            setBorder(BorderFactory.createEtchedBorder());
        }
		
		public int getFilterPosition() {
			return modelColumn;
		}
		
		public void setFilterPosition(int filterPosition) {
			modelColumn = filterPosition;
		}

        public void addFilterObserver(IFilterObserver listener) {
        }

        public Component getComponent() {
            return this;
        }

        public void updateFilter() {
        }

        public void resetFilter() {
        }
        
        public Object getFilter() {
        	return null;
        }

        public void setFilter(Object content) {
        }

        public IFilterObservable getFilterObservable() {
            return this;
        }

        public void removeFilterObserver(IFilterObserver listener) {
        }

        @Override public void setEnabled(boolean enabled) {
        }
        
        public void addTableFilterObserver(ITableFilterEditorObserver observer) {
        }
        
        public void removeTableFilterObserver(ITableFilterEditorObserver observer) {
        }
        
        @Override public void detach() {
        }
    }
}
