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

import java.text.Format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * Internal class to handle choices under adaptive behaviour<br>
 * Choices are automatically updated as the table model changes; Choices are
 * automatically hidden (removed, in fact) when the existing filters exclude
 * them.
 */
class AdaptiveChoicesHandler extends ChoicesHandler {

    private AdaptiveChoicesSupport adaptiveSupport;
    private boolean interrupted = true;

    public AdaptiveChoicesHandler(FiltersHandler handler) {
        super(handler);
    }

    @Override public RowFilter getRowFilter() {
        return adaptiveSupport;
    }

    @Override public boolean setInterrupted(boolean interrupted) {
        // while interrupted, remove the adaptive support, and create it
        // only when the interruption finishes. There is no sense trying to
        // 'pause' the AdaptiveChoicesSupport, as the interruption can be
        // related to new editors or filters
        boolean ret = false;
        if (this.interrupted != interrupted) {
            this.interrupted = interrupted;
            if (interrupted) {
                ret = removeAdaptiveChoicesSupport();
            } else {
                ret = (handler.getTable() != null) && handler.isEnabled();
                if (ret) {
                    createAdaptiveChoicesSupport();
                }
            }
        }

        return ret;
    }

    @Override public void editorUpdated(FilterEditor editor) {
        if (adaptiveSupport != null) {
            adaptiveSupport.editorUpdated(editor);
        }
    }

    @Override public boolean filterUpdated(IFilter filter,
                                           boolean retInfoRequired) {
        // nothing to do with retInfoRequired, always return the value
        return (adaptiveSupport == null) || adaptiveSupport.update(filter);
    }

    @Override public void filterOperation(boolean start) {
        if (start) {
            removeAdaptiveChoicesSupport();
        } else if (!interrupted) {
            createAdaptiveChoicesSupport();
            handler.updateTableFilter();
        }
    }

    @Override public void filterEnabled(IFilter filter) {
        if (adaptiveSupport == null) {
            if (!interrupted) {
                createAdaptiveChoicesSupport();
                handler.updateTableFilter();
            }
        } else {
            adaptiveSupport.initChoices(filter);
        }
    }

    @Override public void allFiltersDisabled() {
        if (removeAdaptiveChoicesSupport()) {
            handler.updateTableFilter();
        }
    }

    @Override public void consolidateFilterChanges(int modelIndex) {
        if (adaptiveSupport != null) {
            adaptiveSupport.propagateChanges(modelIndex);
        }
    }

    @Override public void tableUpdated(TableModel model,
                                       int        eventType,
                                       int        firstRow,
                                       int        lastRow,
                                       int        column) {
        if (adaptiveSupport != null) {
            adaptiveSupport.tableChanged(eventType, firstRow, lastRow, column);
        }
    }

    /** Creates the associated {@link AdaptiveChoicesSupport} instance. */
    private void createAdaptiveChoicesSupport() {
        Collection<FilterEditor> eds = handler.getEditors();
        FilterEditor array[] = eds.toArray(new FilterEditor[eds.size()]);
        adaptiveSupport = new AdaptiveChoicesSupport(handler.getTable()
                    .getModel(), array, handler.getFilters());
        setEnableTableModelEvents(true);
    }

    /** Deletes the {@link AdaptiveChoicesSupport} instance. */
    private boolean removeAdaptiveChoicesSupport() {
        if (adaptiveSupport == null) {
            return false;
        }

        adaptiveSupport = null;
        setEnableTableModelEvents(false);

        return true;
    }

    /**
     * Helper class, holding, for each row and filter (editor or user defined),
     * a bit defining whether it is filtered in or out.<br>
     */
    static class AdaptiveChoicesSupport extends RowFilter {

        /** A RowInfo for each row on the table model. */
        private ArrayList<RowInfo> rows;

        /** A single instance to check the filters of every row/column. */
        private RowEntry rowEntry;

        /**
         * An EditorHandle per editor (table model's column)<br>
         * The order is not important -it changes continuously while performing
         * updates.
         */
        private EditorHandle editorHandles[];

        /**
         * Each of the defined .<br>
         * Elsewhere, it is kept as a Set, but here, it is needed to associate a
         * column to each specific filter, so an array is chosen<br>
         * The first N filters correspond to the N filters of each editor,
         * sorted by column order.<br>
         * Note that any of the first N filters could be null, if there is no
         * column on that position (column removed from model)
         */
        private RowInfo.Filter filters[];

        /**
         * Only constructor; note: the parameter allFilters set is modified on
         * the constructor.
         */
        public AdaptiveChoicesSupport(TableModel   model,
                                      FilterEditor editors[],
                                      Set<IFilter> allFilters) {
            // note that the allFilters set will be modified
            int columns = model.getColumnCount();
            int edLen = editors.length;
            rows = new ArrayList<RowInfo>(model.getRowCount() + 1);
            editorHandles = new EditorHandle[edLen];

            // note: columns could be different from editors.length if some
            // column has been removed from the model
            filters = new RowInfo.Filter[allFilters.size() + columns - edLen];
            for (int i = 0; i < columns; i++) {
                filters[i] = null;
            }

            for (FilterEditor editor : editors) {
                int column = editor.getModelIndex();
                this.editorHandles[--edLen] = new EditorHandle(editor, model);

                IFilter filter = editor.getFilter();
                allFilters.remove(filter);
                filters[column] = new RowInfo.Filter(filter, column);
            }

            for (IFilter filter : allFilters) {
                filters[columns] = new RowInfo.Filter(filter, columns);
                columns++;
            }

            rowEntry = new RowEntry(model, editors);
            rowsAdded(0, model.getRowCount() - 1);
        }

        /** Handles an table model event. */
        public void tableChanged(int event,
                                 int firstRow,
                                 int lastRow,
                                 int column) {
            if (column != TableModelEvent.ALL_COLUMNS) {
                rowsUpdated(firstRow, lastRow, column);
            } else if (event == TableModelEvent.UPDATE) {
                // an update can signal that all cells have changed
                // http://code.google.com/p/tablefilter-swing/issues/detail?id=8
                if (lastRow >= rows.size()) {
                    rows.clear();
                    rowsAdded(0, rowEntry.getModel().getRowCount() - 1);
                } else {
                    rowsUpdated(firstRow, lastRow, TableModelEvent.ALL_COLUMNS);
                }
            } else if (event == TableModelEvent.INSERT) {
                rowsAdded(firstRow, lastRow);
            } else if (event == TableModelEvent.DELETE) {
                rowsDeleted(firstRow, lastRow);
            }
        }

        /** Handles a table model event after some rows are added. */
        private void rowsAdded(int firstRow, int lastRow) {
            rows.ensureCapacity(rows.size() + lastRow - firstRow + 1);
            for (int r = firstRow; r <= lastRow; r++) {
                RowInfo row = new RowInfo(filters.length);
                rows.add(r, row);
                rowEntry.row = r;

                for (RowInfo.Filter filter : filters) {
                    if ((filter != null) && !filter.include(rowEntry)) {
                        filter.set(row, false);
                    }
                }
            }

            extractChoices(editorHandles.length, firstRow, lastRow);
        }

        /**
         * Handles a table model event after some rows are updated on 1 column.
         */
        private void rowsUpdated(int firstRow, int lastRow, int column) {

            RowInfo.Filter filter = (column == TableModelEvent.ALL_COLUMNS)
                ? null : filters[column];
            while (firstRow <= lastRow) {
                RowInfo row = rows.get(firstRow);
                rowEntry.row = firstRow++;
                if (filter == null) {
                    for (RowInfo.Filter f : filters) {
                        if (f != null) {
                            f.set(row, f.include(rowEntry));
                        }
                    }
                } else {
                    filter.set(row, filter.include(rowEntry));
                }
            }

            // reread all the model
            extractChoices(editorHandles.length, 0, -1);
        }

        /** Handles a table model event after some rows are deleted. */
        private void rowsDeleted(int firstRow, int lastRow) {
            rows.subList(firstRow, lastRow + 1).clear();
            extractChoices(editorHandles.length, 0, -1);
        }

        /**
         * Handles a change on a filter.
         *
         * @return  true if the update leaves any row in the filter
         */
        public boolean update(IFilter iFilter) {
            RowInfo.Filter filter = getFilter(iFilter);
            int update = updateRowInfo(filter, iFilter);
            boolean changed = 1 == (update & 1);

            if (changed) {
                // only propagate changes if this is not an editor
                // or the editor has no focus (is still editing)
                // http://code.google.com/p/tablefilter-swing/issues/detail?id=11
                int editorHandle = getEditorHandle(filter.column);
                if ((editorHandle == -1)
                        || !editorHandles[editorHandle].editor.isEditing()) {
                    propagateChanges(filter.column);
                }
            }

            return (update & 2) == 2;
        }

        /** Extract the choices due to a filter update on the given position. */
        public void propagateChanges(int modelPosition) {
            int width = editorHandles.length;
            int handle = getEditorHandle(modelPosition);
            if (handle >= 0) {
                switchHandle(handle, --width);
            }

            extractChoices(width, 0, -1);
        }

        /** Reports an update on the properties of an editor. */
        public void editorUpdated(FilterEditor fe) {
            int column = fe.getModelIndex();
            int editorHandle = getEditorHandle(column);

            // invoke the editor update call
            editorHandles[editorHandle].updateFormatter(rowEntry.getModel(),
                rowEntry.getFormatters());

            int width;

            // and update the filter for this editor
            int updateRowInfo = updateRowInfo(filters[fe.getModelIndex()],
                    fe.getFilter());
            if (1 == (1 & updateRowInfo)) {
                // if changed, update all editor choices
                width = editorHandles.length;
            } else {
                // update only the associated editor, move it at the beginning
                switchHandle(editorHandle, 0);
                width = 1;
            }

            extractChoices(width, 0, -1);
        }

        /**
         * Handles a change on a filter, updating the RowInfo array.
         *
         * @return  an integer where the lower bit is 0 if the update implies no
         *          changes, and the next bit is 0 is the filter clears the
         *          whole table (i.e: no row passes the filter)
         */
        private int updateRowInfo(RowInfo.Filter filter, IFilter iFilter) {
            int changedBit = 0;
            int anyBitSet = 1;
            rowEntry.row = 0;
            for (RowInfo ri : rows) {
                boolean set = !iFilter.isEnabled() || iFilter.include(rowEntry);
                if (filter.set(ri, set)) {
                    changedBit = 1;
                }

                if (set) {
                    anyBitSet = 2;
                }

                rowEntry.row++;
            }

            return changedBit | anyBitSet;
        }

        /** Forces the initialisation of the choices of a editor filter. */
        public void initChoices(IFilter iFilter) {
            RowInfo.Filter filter = getFilter(iFilter);
            if (filter.column < editorHandles.length) {
                // update only the associated editor, move it at the beginning
                switchHandle(getEditorHandle(filter.column), 0);
                extractChoices(1, 0, -1);
            }
        }

        /** Returns the filter with the given {@link IFilter}. */
        private RowInfo.Filter getFilter(IFilter filter) {
            for (RowInfo.Filter f : filters) {
                if ((f != null) && (f.filter == filter)) {
                    return f;
                }
            }

            return null;
        }

        /**
         * Handles all the rows between firstRow and lastRow, updating the
         * choices of the first handles in the editorHandles instance' variable.
         * <br>
         * That is, the order of the variables in the editorHandlers variable is
         * modified, so that only the first handles are updated
         *
         * @param  lastRow  can be -1 to represent the whole model
         */
        private void extractChoices(int handles, int firstRow, int lastRow) {
            int rows = rowEntry.getModelRowCount() - 1;
            if (lastRow == -1) {
                lastRow = rows;
            }

            boolean fullMode = (firstRow == 0) && (lastRow == rows);
            int check = handles;
            for (int i = 0; i < check;) {
                if (editorHandles[i].startIteration(fullMode)) {
                    // if startIteration returns true, this editor will require
                    // no additional iteration (move it to the end)
                    switchHandle(i, --check);
                } else {
                    ++i;
                }
            }

            if (check > 0) {
                iterateRows(check, firstRow, lastRow);
            }
            while (handles-- > 0) {
                editorHandles[handles].iterationCompleted(fullMode);
            }
        }

        /** handle all the rows in [firstRow, lastRow). */
        private void iterateRows(int handles, int firstRow, int lastRow) {
            for (; firstRow <= lastRow; firstRow++) {
                rowEntry.row = firstRow;

                RowInfo row = rows.get(firstRow);
                for (int i = 0; i < handles;) {
                    EditorHandle handle = editorHandles[i++];
                    if (filters[handle.column].is(row)) {
                        if (handle.handleRow(rowEntry)) {
                            // if handleRow returns true, this editor will
                            // require no additional iteration (move it to the
                            // end) if no handles remain, just return
                            switchHandle(--i, --handles);
                            if (handles == 0) {
                                return;
                            }
                        }
                    }
                }
            }
        }

        /** Moves the handle at the given position to the target. */
        private void switchHandle(int source, int target) {
            if (target != source) {
                EditorHandle move = editorHandles[target];
                editorHandles[target] = editorHandles[source];
                editorHandles[source] = move;
            }
        }

        /** Returns the EditorHandle with the given column. */
        private int getEditorHandle(int column) {
            int len = editorHandles.length;
            while (len-- > 0) {
                if (editorHandles[len].column == column) {
                    return len;
                }
            }

            return len;
        }

        @Override public boolean include(RowFilter.Entry entry) {
            return rows.get((Integer) entry.getIdentifier()).is();
        }

        public boolean include(int row) {
            return rows.get(row).is();
        }


        /**
         * Helper class to handle an editor. It is very associated to the
         * algorithm used in {@link AdaptiveChoicesSupport#extractChoices(int,
         * int, int)}
         */
        static class EditorHandle {

            /** the model position of the associated editor. */
            int column;

            /** The associated FilterEditor. */
            FilterEditor editor;

            /** The maximum number of choices the editor can have (enums). */
            private int maxChoices;

            /** Temporal variable for maxChoices, inside an iteration. */
            private int maxIterationChoices;

            /** The choices defined for the editor, with its filter. */
            private Map<CustomChoice, RowFilter> customChoices;

            /** On an iteration, the choices not yet added. */
            private Map<CustomChoice, RowFilter> missingChoices;

            /** The choices that will be set on the editor. */
            private Set choices = new HashSet();

            /** Single constructor. */
            public EditorHandle(FilterEditor editor, TableModel model) {
                this.editor = editor;
                this.column = editor.getModelIndex();
                init(model);
            }

            /** Updates the formatter associated to this editor. */
            public void updateFormatter(TableModel model, Format formatters[]) {
                formatters[column] = editor.getFormat();
                init(model);

            }

            /** Initializes the member's variables. */
            private void init(TableModel model) {
                Set<CustomChoice> choices = editor.getCustomChoices();
                if (AutoChoices.DISABLED == editor.getAutoChoices()) {
                    maxChoices = 1;     // consider empty
                } else {
                    Class<?> c = model.getColumnClass(column);
                    if (c.equals(Boolean.class)) {
                        maxChoices = 4; // consider empty and null
                    } else {
                        Object o[] = c.getEnumConstants();
                        maxChoices = (o == null) ? Integer.MAX_VALUE
                                                 : (o.length + 2);
                    }
                }

                if (choices.isEmpty()) {
                    customChoices = null;
                } else {
                    customChoices = new HashMap<CustomChoice, RowFilter>();
                    for (CustomChoice cc : choices) {
                        customChoices.put(cc, cc.getFilter(editor));
                    }

                    if (maxChoices != Integer.MAX_VALUE) {
                        maxChoices += customChoices.size();
                    }
                }
            }

            /**
             * Starts an iteration in {@link
             * AdaptiveChoicesSupport#extractChoices(int, int, int)}.
             *
             * @param   fullMode  set to true if the outcome of the iteration is
             *                    to set choices to the editor, or false if the
             *                    outcome is to add choices.
             *
             * @return  true if the handle requires no further iteration steps
             */
            public boolean startIteration(boolean fullMode) {
                if (!editor.isEnabled()) { // do nothing if not enabled
                    return true;
                }

                choices.clear();
                maxIterationChoices = maxChoices;
                if (fullMode) {
                    missingChoices = (customChoices == null)
                        ? Collections.EMPTY_MAP
                        : new HashMap<CustomChoice, RowFilter>(customChoices);
                } else {
                    maxIterationChoices -= editor.getChoices().size();
                }

                return maxIterationChoices == 0;
            }

            /**
             * Handles a given row during the iteration.
             *
             * @param   entry
             *
             * @return  true if the handle requires no further iteration steps
             */
            public boolean handleRow(RowEntry entry) {
                if (!missingChoices.isEmpty()) {
                    Iterator<Map.Entry<CustomChoice, RowFilter>> it =
                        missingChoices.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<CustomChoice, RowFilter> o = it.next();
                        if (o.getValue().include(entry)) {
                            choices.add(o.getKey());
                            it.remove();
                        }
                    }
                }

                if (AutoChoices.DISABLED != editor.getAutoChoices()) {
                    choices.add(entry.getValue(column));
                }

                return maxIterationChoices == choices.size();
            }

            /**
             * Final step on the iteration process, updating the editor'
             * choices.
             */
            public void iterationCompleted(boolean fullMode) {
                if (editor.isEnabled()) {
                    if (fullMode) {
                        editor.setChoices(choices);
                    } else {
                        editor.addChoices(choices);
                    }
                }
            }
        }

        /**
         * Class to hold the filter information on a single row.<br>
         * This information is a bit per column, defining whether the column is
         * filtered out or not
         */
        static class RowInfo {
            static final byte SET = (byte) 255;
            byte info[];

            RowInfo(int columns) {
                int length = 1 + (columns >> 3);
                this.info = new byte[length];
                while (length-- > 0) {
                    this.info[length] = SET;
                }
            }

            /** returns true if all the bits are set to 1. */
            public boolean is() {
                int length = info.length;
                while (length-- > 0) {
                    if (info[length] != SET) {
                        return false;
                    }
                }

                return true;
            }

            /** Defines a column in the RowInfo, associated to a filter. */
            static class Filter {
                private int col;
                private int bit;
                int column;
                IFilter filter;

                Filter(IFilter filter, int column) {
                    this.column = column;
                    this.filter = filter;
                    col = column >> 3;
                    bit = 1 << (column & 7);
                }

                public boolean include(RowFilter.Entry rowEntry) {
                    return !filter.isEnabled() || filter.include(rowEntry);
                }

                /**
                 * Sets or unsets the given row.
                 *
                 * @return  true if it implies a change
                 */
                public boolean set(RowInfo row, boolean set) {
                    byte info[] = row.info;
                    byte now = info[col];
                    if (set) {
                        info[col] |= bit;
                    } else {
                        info[col] &= (SET ^ bit);
                    }

                    return now != info[col];
                }

                /**
                 * returns true if all the bits in the row, with the possible
                 * exception of THIS column, are set to 1.
                 */
                public boolean is(RowInfo row) {
                    byte info[] = row.info;
                    boolean now = 0 != (info[col] & bit);
                    set(row, true);

                    boolean ret = row.is();
                    set(row, now);

                    return ret;
                }
            }
        }

    }
}
