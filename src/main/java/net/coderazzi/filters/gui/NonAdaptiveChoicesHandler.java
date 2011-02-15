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

import java.util.HashSet;
import java.util.Set;

import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.gui.editor.FilterEditor;


/**
 * Internal class to handle choices without adaptive behaviour<br>
 * Choices are automatically updated as the table model changes.
 */
class NonAdaptiveChoicesHandler extends ChoicesHandler {

    private boolean interrupted = true;

    public NonAdaptiveChoicesHandler(FiltersHandler handler) {
        super(handler);
    }

    @Override public RowFilter getRowFilter() {
        return handler;
    }

    @Override public boolean setInterrupted(boolean interrupted) {
        if (this.interrupted != interrupted) {
            this.interrupted = interrupted;
            if (interrupted) {
                setEnableTableModelEvents(false);
            } else {
                for (FilterEditor editor : handler.getEditors()) {
                    editorUpdated(editor);
                }
            }
        }

        return !interrupted; // filter should be updated
    }

    @Override public void editorUpdated(FilterEditor editor) {
        if (editor.isEnabled()) {
            initEditorChoices(editor);
        }
    }

    @Override public void filterUpdated(IFilter filter) {
        // nothing to do
    }

    @Override public void filterOperation(boolean start) {
        handler.enableNotifications(!start);
    }

    @Override public void filterEnabled(IFilter filter) {
        for (FilterEditor editor : handler.getEditors()) {
            if (editor.getFilter() == filter) {
                initEditorChoices(editor);

                break;
            }
        }
    }

    @Override public void allFiltersDisabled() {
        setEnableTableModelEvents(false);
    }

    @Override public void tableUpdated(TableModel model,
                                       int        eventType,
                                       int        firstRow,
                                       int        lastRow,
                                       int        column) {
        if (column != TableModelEvent.ALL_COLUMNS) {
            // a change in ONE column is always handled as an update
            // (every update is handled by re-extracting the choices
            FilterEditor editor = handler.getEditor(column);
            if ((editor != null) && editor.isEnabled()) {
                setChoicesFromModel(editor, model);
            }
        } else {
            boolean handled = false;
            for (FilterEditor editor : handler.getEditors()) {
                if (editor.isEnabled()
                        && (AutoChoices.ENABLED == editor.getAutoChoices())) {
                    // insert events can be handled by adding the
                    // new model's values.
                    // updates/deletes require reparsing the whole
                    // table to obtain again the available choices
                    if (eventType == TableModelEvent.INSERT) {
                        editor.addChoices(modelExtract(editor, model, firstRow,
                                lastRow, new HashSet<Object>()));
                    } else {
                        setChoicesFromModel(editor, model);
                    }

                    handled = true;
                }
            }

            if (!handled) {
                // lazy mode: if the instance was listening to table model
                // events and all editors became AutoChoices.DISABLED, the
                // instance will keep listening for table model events, until
                // it discovers that it does not need it.
                setEnableTableModelEvents(false);
            }
        }
    }

    /**
     * Initializes the choices in the given editor.<br>
     * It can update the mode of the editor, from ENABLED to ENUMS (in case of
     * enumerations), and from ENUMS to DISABLED (for no enumerations)
     */
    private void initEditorChoices(FilterEditor editor) {
        AutoChoices autoChoices = editor.getAutoChoices();
        if (autoChoices == AutoChoices.DISABLED) {
            editor.setChoices(editor.getCustomChoices());
        } else {
            TableModel model = handler.getTable().getModel();
            Class<?> c = model.getColumnClass(editor.getModelIndex());
            boolean asEnum = c.equals(Boolean.class) || c.isEnum();
            if (asEnum && (autoChoices != AutoChoices.ENUMS)) {
                editor.setAutoChoices(AutoChoices.ENUMS);
            } else if (!asEnum && (autoChoices == AutoChoices.ENUMS)) {
                editor.setAutoChoices(AutoChoices.DISABLED);
            } else if (asEnum) {
                Set choices = editor.getCustomChoices();
                if (c.equals(Boolean.class)) {
                    choices.add(true);
                    choices.add(false);
                } else {
                    for (Object each : c.getEnumConstants()) {
                        choices.add(each);
                    }
                }

                editor.setChoices(choices);
            } else {
                setChoicesFromModel(editor, model);
                setEnableTableModelEvents(true);
            }
        }
    }

    /** Sets the content for the given editor from the model's values. */
    private void setChoicesFromModel(FilterEditor editor, TableModel model) {
        editor.setChoices(modelExtract(editor, model, 0,
                model.getRowCount() - 1, editor.getCustomChoices()));
    }

    /**
     * Extract content from the given range of rows in the model, adding the
     * results to the provided Set, which is then returned.
     */
    private Set modelExtract(FilterEditor editor,
                             TableModel   model,
                             int          firstRow,
                             int          lastRow,
                             Set          fill) {
        int column = editor.getModelIndex();
        for (; lastRow >= firstRow; firstRow++) {
            fill.add(model.getValueAt(firstRow, column));
        }

        return fill;
    }
}
