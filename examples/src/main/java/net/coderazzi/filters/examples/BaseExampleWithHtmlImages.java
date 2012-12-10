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
package net.coderazzi.filters.examples;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.coderazzi.filters.examples.utils.AgeCustomChoice;
import net.coderazzi.filters.examples.utils.CenteredRenderer;
import net.coderazzi.filters.examples.utils.DateRenderer;
import net.coderazzi.filters.examples.utils.FlagRenderer;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.HtmlChoiceRenderer;
import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.TableFilterHeader;


/** Main example showing the {@link TableFilterHeader} functionality. */
public class BaseExampleWithHtmlImages extends JFrame {

    private static final long serialVersionUID = 382439526043424492L;
    
    public BaseExampleWithHtmlImages() {
        super("Table Filter Example");
    	TestTableModel.setFullModel();
        JTable table = new JTable(TestTableModel.createTestTableModel(20));
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        getContentPane().add(tablePanel);
        
        setupHeader(new TableFilterHeader(table, AutoChoices.ENABLED));
    }
    
    private void setupHeader(TableFilterHeader header){
    	getEditor(header, TestTableModel.COUNTRY, new FlagRenderer())
    		.setRenderer(new FlagRenderer());
    	getEditor(header, TestTableModel.HTML_COUNTRY, null)
    		.setRenderer(new HtmlChoiceRenderer());
    	getEditor(header, TestTableModel.NOTE, null)
			.setEditable(false);
    	getEditor(header, TestTableModel.AGE, new CenteredRenderer())
    		.setCustomChoices(AgeCustomChoice.getCustomChoices());    	
    	getEditor(header, TestTableModel.DATE, 
    		new DateRenderer(header.getParserModel().getFormat(Date.class)));
    }
    
    private IFilterEditor getEditor(TableFilterHeader header, String name, 
    		TableCellRenderer renderer){
    	TableColumnModel model = header.getTable().getColumnModel();
    	for (int i=model.getColumnCount(); --i>=0;){
    		TableColumn tc = model.getColumn(i);
    		if (name.equals(tc.getHeaderValue())){
    			if (renderer!=null){
    				tc.setCellRenderer(renderer);
    			}
    			return header.getFilterEditor(i);
    		}
    	}
    	return null;
    }
       
    public final static void main(String args[]) {
        BaseExampleWithHtmlImages frame = new BaseExampleWithHtmlImages();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
    	frame.setSize(1200, frame.getSize().height);
        frame.setVisible(true);
    }

}
