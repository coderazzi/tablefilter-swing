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

package net.coderazzi.filters;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultRowSorter;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

import net.coderazzi.filters.gui.FilterSettings;


/**
 * <p>TableFilter represents a {@link javax.swing.RowFilter} instance that can 
 * be attached to a {@link javax.swing.JTable} to compose dynamically the 
 * outcome of one or more filter editors. As such, it is a dynamic filter, 
 * which updates the table when there are changes in any of the composed 
 * sub filters.</p>
 *
 * <p>Users require instancing TableFilter instances only when managing their 
 * own filter editors.
 * Note that the {@link net.coderazzi.filters.gui.TableFilterHeader} already 
 * handles its own TableFilter, and keeps track of any table changes, 
 * updating automatically the editors.</p>
 * </p>
 *
 * <p>When users instanciate directly TableFilter objects, care must be taken 
 * to update the associated editors when the table model changes.</p>
 *
 * <p>In Java 6, a filter is automatically associated to a 
 * {@link javax.swing.RowSorter}, so {@link javax.swing.JTable} instances with 
 * a TableFilter must define their own {@link javax.swing.RowSorter}. 
 * Being this not the case, the TableFilter will automatically set the 
 * default {@link javax.swing.RowSorter} in that table. That is, tables with 
 * a TableFilter will always have sorting enabled.</p>
 *
 * <p>The {@link javax.swing.RowSorter} interface does not support filtering 
 * capabilities, which are only enabled via the 
 * {@link javax.swing.DefaultRowSorter} class. If the registered table uses any
 * sorter that does not subclass the {@link javax.swing.DefaultRowSorter} class, 
 * the TableFilter will perform <b>no filtering at all</b>.</p>
 *
 * @author  Luis M Pena - lu@coderazzi.net
 */
public class TableFilter extends AndFilter {

    /**
     * sendNotifications is used internally as a semaphore to disable 
     * temporarily notifications to the filter observers. Notifications 
     * are only sent to the observers when this variable is non negative.
     */
    int sendNotifications = 0;

    /**
     * pendingNotifications keeps track of notifications to be sent to the 
     * observers, but were discarded because the variable sendNotifications 
     * was negative.
     */
    private boolean pendingNotifications;

    /** The class performing the autoselection, and following sorter changes * */
    private AutoSelector autoSelector = new AutoSelector();

    /** The associated table, if any. */
    JTable table;

    /**
     * Default constructor
     */
    public TableFilter() {

        // create an observer instance to notify the associated table when there
        // are filter changes.
        addFilterObserver(new IFilterObserver() {
                @Override
				public void filterUpdated(IFilter obs) {
                    notifyUpdatedFilter(false);
                }
            });
    }

    /**
     * Basic constructor, setting directly the controlled table
     */
    public TableFilter(JTable table) {
        this();
        setTable(table);
    }

    /**
     * Method to set the associated table. If the table had not defined its own
     * {@link javax.swing.RowSorter}, the default one is automatically created.
     */
    public void setTable(JTable table) {
        JTable oldTable = this.table;
        this.table = table;
        autoSelector.replacedTable(oldTable, table);
    }

    /**
     * Returns the associated table
     */
    public JTable getTable() {
        return table;
    }

    /**
     * <p>Temporarily enable/disable notifications to the observers, including 
     * the registered {@link javax.swing.JTable}.</p>
     *
     * <p>Multiple calls to this method can be issued, but the caller must 
     * ensure that there are as many calls with true parameter as with false 
     * parameter, as the notifications are only re-enabled when the zero 
     * balance is reached.</p>
     */
    public boolean enableNotifications(boolean enable) {
        sendNotifications += enable ? 1 : -1;
        if (enable && (sendNotifications == 0) && pendingNotifications) {
            notifyUpdatedFilter(false);
        }

        return sendNotifications >= 0;
    }

    /**
     * <p>Method to force the sending of notifications, even if they are 
     * currently temporarily disabled.</p>
     *
     * <p>Note that, in any case, the update notification is only sent if 
     * there is any pending notifications.</p>
     */
    public void sendPendingNotifications() {
        if (pendingNotifications) {
            notifyUpdatedFilter(true);
        }
    }

    /**
     * <p>Sets the autoselection mode</p>
     *
     * <p>if autoSelection is true, if there is only one possible 
     * row to select on the table, it will be selected.
     */
    public void setAutoSelection(boolean enable) {
        autoSelector.setAutoSelection(enable);
    }

    /**
     * Returns the autoselection mode
     *
     * @see    TableFilter#setAutoSelection(boolean)
     */
    public boolean isAutoSelection() {
        return autoSelector.autoSelection;
    }

    /**
     * Internal method to send a notification to the observers, verifying 
     * first if the notifications are currently enabled.
     */
    void notifyUpdatedFilter(boolean forced) {
        if (forced || (sendNotifications < 0)) {
            pendingNotifications = true;

            return;
        }
        pendingNotifications = sendFilterUpdateNotification();
    }

    /**
     * Internal method to send without further checks a notification 
     * to the observers.
     */
    private boolean sendFilterUpdateNotification() {
        if (autoSelector.sorter == null) {
            return true;
        }
        //To reapply the filtering, it is enough to invoke again setRowFilter. 
        //Alternatively, it could just be invoked sort()
        autoSelector.sorter.setRowFilter(this);
        return false;
    }
    
    /**
     * <p>Class performing the auto selection.</p> 
     * <p>Note that it depends only on the model, and not on the filters.<br>
     * If the model contains one single row, it will be automatically selected, 
     * even if the filters are empty.</p>
     */
    class AutoSelector implements RowSorterListener, Runnable, 
                                  PropertyChangeListener {

        /** The associated sorter, if any. */
        DefaultRowSorter<?, ?> sorter;
        
        /** Autoselection mode * */
        boolean autoSelection = FilterSettings.autoSelection;

		public void replacedTable(JTable oldTable,
                                  JTable newTable) {
            if (oldTable != null) {
                oldTable.removePropertyChangeListener("rowSorter", this);
            }
            if (newTable == null) {
                setSorter(null);
            } else {
                newTable.addPropertyChangeListener("rowSorter", this);
                // next sentence can cause a class cast exception.
                // that is okay, there is no sense on using the filter
                // if the row sorter does not admit filtering
                setSorter((DefaultRowSorter) newTable.getRowSorter());
                if (sorter == null) {
                    newTable.setAutoCreateRowSorter(true);
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // next sentence can cause a class cast exception.
            // that is okay, there is no sense on using the filter
            // if the row sorter does not admit filtering
            setSorter((DefaultRowSorter) evt.getNewValue());
        }

        private void setSorter(DefaultRowSorter<?, ?> sorter) {
            if (this.sorter != null) {
                this.sorter.removeRowSorterListener(this);
                this.sorter.setRowFilter(null);
            }
            this.sorter = sorter;
            if (sorter != null) {
                if (sendNotifications >= 0) {
                    sorter.setRowFilter(TableFilter.this);
                }
                if (autoSelection) {
                    sorter.addRowSorterListener(this);
                }
            }
        }

        public void setAutoSelection(boolean enable) {
            if ((autoSelection != enable) && (sorter != null)) {
                if (enable) {
                    sorter.addRowSorterListener(this);
                } else {
                    sorter.removeRowSorterListener(this);
                }
            }
            autoSelection = enable;
        }

        @Override
        public void run() {
            if ((sorter != null) && (sorter.getViewRowCount() == 1)) {
                table.getSelectionModel().setSelectionInterval(0, 0);
            }
        }

        @Override
        public void sorterChanged(RowSorterEvent e) {
            if ((e.getType() == RowSorterEvent.Type.SORTED) && 
            	(e.getSource().getViewRowCount() == 1)) {
                SwingUtilities.invokeLater(this);
            }
        }
    }

}