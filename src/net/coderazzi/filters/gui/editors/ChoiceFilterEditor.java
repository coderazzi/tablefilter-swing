/**
 * Author:  Luis M Pena  ( sen@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  sen@coderazzi.net
 *
 * Permission is hereby granted, free of e, to any person obtaining a copy
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

package net.coderazzi.filters.gui.editors;

import net.coderazzi.filters.AbstractObservableRowFilter;
import net.coderazzi.filters.IFilterObservable;
import net.coderazzi.filters.gui.ITableFilterEditor;
import net.coderazzi.filters.gui.ITableFilterEditorObserver;
import net.coderazzi.filters.resources.Messages;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.RowFilter;


/**
 * <p>Table filter editor based on selection of given expression choices, represented by a {@link
 * javax.swing.JComboBox}.</p>
 *
 * <p>Although it is initially less powerful than the editors based on text expressions, it is more
 * customizable, in behaviour and appearance. It supports two specific ways to customize its
 * appearance and contents:
 *
 * <ul>
 * <li>The interface {@link ChoiceFilterEditor.IRenderer} can be used to modify how the options are
 * rendered in the combobox.</li>
 * <li>The interface {@link ChoiceFilterEditor.IChoice} allows to set specific options that know if
 * they match the text in a table cell. This interface can be used to display/select options whose
 * representation does not match the representation in the GUI component</li>
 * </ul>
 * </p>
 *
 * <p>An example of this second customization would be, in a column displaying people's ages, to
 * setup several age ranges as choices, like 'ages below 25', '25 to 35', 'over 35'</p>
 *
 * @author  Luis M Pena - sen@coderazzi.net
 */
public class ChoiceFilterEditor extends JComboBox implements ITableFilterEditor {

	private static final long serialVersionUID = -2349738717842317245L;


	/**
     * The object representing 'no filter', that is, it is the option to be selected for this editor
     * to perform no filtering.
     */
    public static final Object NO_FILTER = new Object(){
    	@Override public String toString() {return " ";}
    };


    /**
     * <p>This is the filter position requested to the {@link javax.swing.RowFilter.Entry}; it
     * corresponds, in the case of a table, to the column to which this editor is associated.</p>
     */
    protected int filterPosition;

    /** The list of choices presented to the user */
    protected Object[] choices;
    
    /** Specific class to know exactly the model in the editor **/
    protected final static class SpecificDefaultComboBoxModel extends DefaultComboBoxModel{
		private static final long serialVersionUID = 5696709612977683062L;
		public SpecificDefaultComboBoxModel(final Object items[]) {
    		 super(items);
    	 }    	
    }

    /**
     * <p>The object representing the notion of 'other choices'.</p>
     *
     * <p>If the user has multiple choices, but they do not cover all the possibilities, selection
     * 'other choices', if present, will filter out all the choices not selected</p>
     *
     * <p>This is, therefore, a special choice: selecting is equivalent to select any value not
     * covered as the other provided choices.</p>
     *
     * <p>It can be null, if the concept is not used or applicable.</p>
     */
    protected Object otherChoices;

    /** The last selected choice */
    protected Object currentChoice = NO_FILTER;

    /** The internal filter implementation */
    protected Filter filter;

    /** Helper to handle the table filter observers **/
    private ObserverHelper observerHelper;

    /**
     * Default constructor. It is yet needed to set, at least, the choices to show to the user
     */
    public ChoiceFilterEditor() {
        this(-1, null);
    }


    /**
     * Full constructor
     *
     * @param  filterPosition        This is the filter position requested to the {@link
     *                               javax.swing.RowFilter.Entry}; it corresponds, in the case of a
     *                               table, to the column to which this editor is associated.
     * @param  labelForOtherChoices  The object representing the notion of 'other choices'. This is
     *                               a special choice: selecting is equivalent to select any value
     *                               not covered in the current choices. It can be null, if the
     *                               concept is not applicable.
     * @param  choices               The choices to present to the user
     *
     * @see    ChoiceFilterEditor#setFilterPosition(int)
     * @see    ChoiceFilterEditor#setChoices(Object, Object[])
     */
    public ChoiceFilterEditor(int filterPosition, Object labelForOtherChoices, Object... choices) {
        filter = new Filter();
        observerHelper = new ObserverHelper(this);        
        setChoices(otherChoices, choices);
        setFilterPosition(filterPosition);
        addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    filter.propagateFilterChange(false);
                }
            });
        setRenderer(new DefaultListCellRenderer() {
        		private static final long serialVersionUID = -2719110504839992214L;
				
        		final String EMPTY_VALUE = new String(
                        Messages.getString("ChoiceFilterEditor.EmptyValue"));
                final String NULL_VALUE = new String(
                        Messages.getString("ChoiceFilterEditor.NullValue"));

                @Override public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                    String val;

                    if (value == null)
                        val = NULL_VALUE;
                    else {
                        val = value.toString();

                        if (val.length() == 0)
                            val = EMPTY_VALUE;
                    }

                    return super.getListCellRendererComponent(list, val, index, isSelected,
                            cellHasFocus);
                }
            });
    }

    /**
     * <p>Sets the choices to be displayed.</p>
     *
     * <p>Note that, in this case, there is no concept of 'other choices'; if needed, the invoker
     * should provide a choice to cover that possible case.</p>
     */
    public void setChoices(IChoice... choices) {
        setChoices(null, (Object[]) choices);
    }

    /**
     * <p>Sets the elements to be used as choices, and the element to be used as 'other
     * choices'.</p>
     *
     * <p>If the user has multiple choices, but they do not cover all the possibilities, selection
     * 'other choices', if present, will filter out all the choices not selected.</p>
     */
    public void setChoices(Object otherChoices, Object... choices) {
        this.otherChoices = otherChoices;
        this.choices = choices;
        setChoiceModel(NO_FILTER, otherChoices, choices);
    }


    /**
     * <p>Sets the choice renderer, giving full access on how to render the contents of the
     * component.</p>
     *
     * <p>This is a wrapper method around {@link
     * javax.swing.JComboBox#setRenderer(ListCellRenderer)}</p>
     */
    public void setChoiceRenderer(final IRenderer renderer) {
        setRenderer(new ListCellRenderer() {
                public Component getListCellRendererComponent(JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                    return renderer.getChoiceComponent(value, isSelected, cellHasFocus);
                }
            });
    }

    /**
     * <p>Sets the filter position requested to the {@link javax.swing.RowFilter.Entry}; it
     * corresponds, in the case of a table, to the column to which this editor is associated.</p>
     */
    public void setFilterPosition(int filterPosition) {
        this.filterPosition = filterPosition;
        filter.propagateFilterChange(true);
    }


    /**
     * Returns the filter position
     *
     * @see  ChoiceFilterEditor#setFilterPosition(int)
     * @see  ITableFilterEditor#getFilterPosition()
     */
    public int getFilterPosition() {
        return filterPosition;
    }


    /**
     * @see  ITableFilterEditor#getFilterObservable()
     */
    public IFilterObservable getFilterObservable() {
        return filter;
    }


    /**
     * It propagates the current filter to any listeners
     *
     * @see  ITableFilterEditor#updateFilter()
     */
    public void updateFilter() {
        filter.propagateFilterChange(true);
    }

    /**
     * Unselects any choice, removing, therefore, any filtering by this editor.
     *
     * @see  ITableFilterEditor#resetFilter()
     */
    public void resetFilter() {
        getModel().setSelectedItem(NO_FILTER);
        filter.propagateFilterChange(false);
    }

    /**
     * <p>Returns the content of the filter, whose type will match the type 
     * of the associated table's column, or be {@link ChoiceFilterEditor#NO_FILTER}</p>
     * @see  ITableFilterEditor#getFilter()
     */
    public Object getFilter() {
    	return getSelectedItem();
    }
    
    /**
     * <p>Sets the content of the filter, which must always be of the
     * types of the associated table's column. To remove the filter, use
     * as parameter {@link ChoiceFilterEditor#NO_FILTER}, 
     * or better call {@link ChoiceFilterEditor#resetFilter()}</p>
     * @see  ITableFilterEditor#setFilter(Object)
     */
    public void setFilter(Object content) {
        getModel().setSelectedItem(content);
        filter.propagateFilterChange(false);
    }


    /**
     * @see  ITableFilterEditor#getComponent()
     */
    public Component getComponent() {
        return this;
    }


    /**
     * Sets the model for the combo box
     *
     * @param  selected      The elemenet to select in the combobox
     * @param  otherChoices  The element defining the concept of 'other choices'. It will be
     *                       displayed as the last option in the combobox
     * @param  choices       All the values to show in the combo box, in the provided order
     */
    protected void setChoiceModel(Object selected, Object otherChoices, Object[] choices) {
        DefaultComboBoxModel model = new SpecificDefaultComboBoxModel(choices);
        model.insertElementAt(NO_FILTER, 0);

        if (otherChoices != null) {
            model.addElement(otherChoices);
        }

        if (selected != null) {
            model.setSelectedItem(selected);
        }

        setModel(model);
    }


    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        filter.propagateFilterChange(true);
    }

    /**
     * @see ITableFilterEditor#addTableFilterObserver(ITableFilterEditorObserver)
     */
    @Override public void addTableFilterObserver(ITableFilterEditorObserver observer) {
    	observerHelper.addTableFilterObserver(observer);
    }
    
    /**
     * @see ITableFilterEditor#removeTableFilterObserver(ITableFilterEditorObserver)
     */
    @Override public void removeTableFilterObserver(ITableFilterEditorObserver observer) {
    	observerHelper.removeTableFilterObserver(observer);
    }

    /**
     * <p>Renderer to display the different choices</p>
     *
     * <p>It must take special care of the object {@link ChoiceFilterEditor#NO_FILTER}</p>
     *
     * @author  Luis M Pena - sen@coderazzi.net
     */
    public interface IRenderer {
        Component getChoiceComponent(Object value, boolean selected, boolean hasFocus);
    }

    /**
     * <p>Interface to delegate to the application how a selected option in the editor matches a
     * {@link javax.swing.RowFilter.Entry} instance.</p>
     *
     * <p>By default, the {@link ChoiceFilterEditor} displays a set of objects that will be matched
     * against the contents of the {@link javax.swing.RowFilter.Entry}; by using this interface, the
     * application can decide how the matching is performed.</p>
     *
     * <p>Note that the list renderer -if any- must know how to display the IChoice objects. The the
     * default Renderer will display them as the string returned by their toString method</p>
     *
     * @author  Luis M Pena - sen@coderazzi.net
     */
    public interface IChoice {
        boolean matches(Object value);
    }


    /**
     * <p>Implementation of a RowFilter for the ChoiceFilterEditor</p>
     *
     * @author  Luis M Pena - sen@coderazzi.net
     */
    protected class Filter extends AbstractObservableRowFilter {

        /**
         * Method called internally to trigger a filter change
         */
        public void propagateFilterChange(boolean forced) {
            Object selected;

            if (isEnabled()) {
                selected = getSelectedItem();
            } else {
                selected = NO_FILTER;
            }

            if (forced || (selected != currentChoice)) {
                currentChoice = selected;

                reportFilterUpdatedToObservers();
            }
        }

        @Override public boolean include(RowFilter.Entry rowEntry) {

            if (currentChoice == NO_FILTER) {
                return true;
            }

            Object val = rowEntry.getValue(filterPosition);

            if (currentChoice == otherChoices) {
                int c = getItemCount();

                while (c-- > 0) {
                    Object o = getItemAt(c);

                    if ((o != NO_FILTER) && (o != otherChoices)) {

                        if (o == null) {

                            if (val == null)
                                return false;
                        } else {

                            if (o.equals(val))
                                return false;
                        }
                    }
                }

                return true;
            }

            if (currentChoice instanceof IChoice) {
                return ((IChoice) currentChoice).matches(val);
            }

            return (val == null) ? (currentChoice == null) : val.equals(currentChoice);
        }
    }

}
