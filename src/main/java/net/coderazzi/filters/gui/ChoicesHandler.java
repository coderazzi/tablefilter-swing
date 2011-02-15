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

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * Interface implemented by the classes that handle the choices on each {@link
 * FilterEditor}.
 */
abstract class ChoicesHandler implements TableModelListener {

    private TableModel listenedModel;
    protected FiltersHandler handler;

    protected ChoicesHandler(FiltersHandler handler) {
        this.handler = handler;
    }

    /** Returns the {@link RowFilter} associated to this handler. */
    public abstract RowFilter getRowFilter();

    /**
     * Sets/unsets the handler on interrupt mode<br>
     * On interrupt mode, the associated {@link FiltersHandler} is likely to
     * send many update events, which shouldn't be treated (if possible).
     */
    public abstract boolean setInterrupted(boolean interrupted);

    /** Reports a {@link FilterEditor} update. */
    public abstract void editorUpdated(FilterEditor editor);

    /** Reports a {@link IFilter} update. */
    public abstract void filterUpdated(IFilter filter);

    /**
     * Reports the beginning or end of {@link IFilter} add/remove operations.
     */
    public abstract void filterOperation(boolean start);

    /** Call triggered after a filter becomes enabled. */
    public abstract void filterEnabled(IFilter filter);

    /** Call triggered after all filters become disabled. */
    public abstract void allFiltersDisabled();

    /** Reports a table update. */
    protected abstract void tableUpdated(TableModel model,
                                         int        eventType,
                                         int        firstRow,
                                         int        lastRow,
                                         int        column);

    @Override public void tableChanged(TableModelEvent e) {
        int firstRow = e.getFirstRow();
        if (firstRow != TableModelEvent.HEADER_ROW) {
            TableModel model = (TableModel) e.getSource();
            int lastRow = Math.min(model.getRowCount() - 1, e.getLastRow());
            tableUpdated(model, e.getType(), firstRow, lastRow, e.getColumn());
        }
    }

    /**
     * Sets whether to send table model events to the {@link ChoicesHandler}.
     */
    protected void setEnableTableModelEvents(boolean set) {
        if (set) {
            JTable table = handler.getTable();
            if (table != null) {
                if (listenedModel != null) {
                    if (listenedModel == table.getModel()) {
                        return;
                    }

                    setEnableTableModelEvents(false);
                }

                listenedModel = table.getModel();
                listenedModel.addTableModelListener(this);
            }
        } else if (listenedModel != null) {
            listenedModel.removeTableModelListener(this);
            listenedModel = null;
        }
    }

}
