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

package net.coderazzi.filters.examples.menu;

import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.coderazzi.filters.examples.ActionHandler;


/** Removes the elements from the model (top row). */
public class MenuModelRemove extends AbstractMenuAction
    implements PropertyChangeListener, TableModelListener {

    private static final long serialVersionUID = 9137226745345048519L;
    private TableModel registeredModel;

    public MenuModelRemove(ActionHandler main) {
        super("Remove top row", main);
        main.getTable().addPropertyChangeListener("model", this);
        registerModel();
    }

    @Override public void actionPerformed(ActionEvent e) {
        main.getTableModel().removeTestData();
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        registeredModel.removeTableModelListener(this);
        registerModel();
    }

    @Override public void tableChanged(TableModelEvent e) {
        if (registeredModel.getRowCount() == 0) {
            setEnabled(false);
        } else if (!isEnabled()) {
            setEnabled(true);
        }
    }

    private void registerModel() {
        registeredModel = main.getTable().getModel();
        registeredModel.addTableModelListener(this);
    }

}
