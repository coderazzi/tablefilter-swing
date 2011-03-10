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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.coderazzi.filters.Filter;
import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.examples.utils.AgeCustomChoice;
import net.coderazzi.filters.examples.utils.CenteredRenderer;
import net.coderazzi.filters.examples.utils.EventsWindow;
import net.coderazzi.filters.examples.utils.FlagRenderer;
import net.coderazzi.filters.examples.utils.TestData;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.CustomChoice;
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.IFilterHeaderObserver;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.TableFilterHeader.Position;


@SuppressWarnings("serial")
public class TableFilterExample extends JFrame {

    private static final String AUTO_CHOICES = "auto choices";
    private static final String AUTO_COMPLETION = "auto completion";
    private static final String EDITABLE = "editable";
    private static final String ENABLED = "enabled";
    private static final String IGNORE_CASE = "ignore case";
    private static final String INSTANT_FILTERING = "instant filtering";
    private static final String MAX_HISTORY_LENGTH = "max history length";
    private static final String USE_TABLE_RENDERER = "use table renderer";

    TestTableModel    tableModel;
    JTable            table;
    JPanel            tablePanel;
    JPanel            filterHeaderPanel;
    TableFilterHeader filterHeader;
    JMenu             filtersMenu;
    JCheckBoxMenuItem allEnabled;
    JCheckBoxMenuItem countrySpecialSorter;
    JCheckBoxMenuItem enableUserFilter;
    IFilter           userFilter;
    boolean           useMaleCustomChoices;

    public TableFilterExample() {
        super("Table Filter Example");
        getContentPane().add(createGui());
        setJMenuBar(createMenu());
        filterHeader.setTable(table);
        customizeTable();
    }

    private JPanel createGui() {
        tableModel = TestTableModel.createTestTableModel();
        table = new JTable(tableModel);
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        filterHeader = new TableFilterHeader();
        filterHeader.addHeaderObserver(new IFilterHeaderObserver() {

                @Override public void tableFilterUpdated(
                        TableFilterHeader header,
                        IFilterEditor     editor,
                        TableColumn       tableColumn) {
                    // no need to react
                }

                @Override public void tableFilterEditorExcluded(
                        TableFilterHeader header,
                        IFilterEditor     editor,
                        TableColumn       tableColumn) {
                    getMenu(filtersMenu, (String) tableColumn.getHeaderValue(),
                        true);
                }

                @Override public void tableFilterEditorCreated(
                        TableFilterHeader header,
                        IFilterEditor     editor,
                        TableColumn       tableColumn) {
                    addColumnToFiltersMenu(editor,
                        (String) tableColumn.getHeaderValue());
                }
            });
        countrySpecialSorter = new JCheckBoxMenuItem(
                "country column sorted by red proportion", false);
        countrySpecialSorter.addItemListener(new ItemListener() {
                @Override public void itemStateChanged(ItemEvent e) {
                    setCountryComparator(countrySpecialSorter.isSelected());
                }
            });

        return tablePanel;
    }

    private JMenuBar createMenu() {
        JMenuBar menu = new JMenuBar();
        menu.add(createTableMenu());
        menu.add(createHeaderMenu());
        menu.add(createFiltersMenu());
        menu.add(createMiscellaneousMenu());

        return menu;
    }

    private JMenu createFiltersMenu() {

        userFilter = new Filter() {
            int nameColumn = tableModel.getColumn(TestTableModel.NAME);

            @Override public boolean include(Entry entry) {
                return -1 != entry.getStringValue(nameColumn).indexOf('e');
            }
        };

        JCheckBoxMenuItem includeUserFilter = new JCheckBoxMenuItem(
                new AbstractAction("include in header") {
                    @Override public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                            e.getSource();
                        if (source.isSelected()) {
                            filterHeader.addFilter(userFilter);
                        } else {
                            filterHeader.removeFilter(userFilter);
                        }

                        enableUserFilter.setSelected(userFilter.isEnabled());
                    }
                });

        enableUserFilter = new JCheckBoxMenuItem(new AbstractAction("enable") {
                    @Override public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                            e.getSource();
                        userFilter.setEnabled(source.isSelected());
                    }
                });
        enableUserFilter.setSelected(userFilter.isEnabled());

        JMenu menu = new JMenu("User filter (name without 'e')");
        menu.add(includeUserFilter);
        menu.add(enableUserFilter);
        filtersMenu = new JMenu("Filters");
        filtersMenu.setMnemonic(KeyEvent.VK_F);
        filtersMenu.add(menu);
        filtersMenu.addSeparator();

        return filtersMenu;

    }

    void reinitFiltersMenu() {
        int pos = filtersMenu.getItemCount();
        while (pos-- > 2) {
            filtersMenu.remove(filtersMenu.getItem(pos));
        }
    }

    private JMenu createTableMenu() {
        JCheckBoxMenuItem autoResize = new JCheckBoxMenuItem(new AbstractAction(
                    "Auto resize") {
                    @Override public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                            e.getSource();
                        table.setAutoResizeMode(
                            source.isSelected() ? JTable.AUTO_RESIZE_ALL_COLUMNS
                                                : JTable.AUTO_RESIZE_OFF);
                        table.doLayout();
                    }
                });
        autoResize.setSelected(table.getAutoResizeMode()
                != JTable.AUTO_RESIZE_OFF);

        final JMenuItem removeElement = new JMenuItem(new AbstractAction("Remove top row") {

            @Override public void actionPerformed(ActionEvent e) {
            	tableModel.removeTestData();
                if (tableModel.getRowCount() == 0) {
                    ((JComponent) e.getSource()).setEnabled(false);
                }
            }
        });

        JMenu tableMenu = new JMenu("Table");
        tableMenu.setMnemonic(KeyEvent.VK_T);

        tableMenu.add(new JMenuItem(
                new AbstractAction("Create male row [first position]") {
                    @Override public void actionPerformed(ActionEvent e) {
                        addTestData(true);
                        removeElement.setEnabled(true);
                    }
                }));
        tableMenu.add(new JMenuItem(
                new AbstractAction("Create female row [first position]") {
                    @Override public void actionPerformed(ActionEvent e) {
                        addTestData(false);
                        removeElement.setEnabled(true);
                    }
                }));

        tableMenu.add(removeElement);
        tableMenu.addSeparator();
        tableMenu.add(autoResize);
        tableMenu.addSeparator();
        tableMenu.add(new JMenuItem(
                new AbstractAction("Change model width") {
                    @Override public void actionPerformed(ActionEvent e) {
                        reinitFiltersMenu();
                        tableModel.changeModel(table);
                        customizeTable();
                        removeElement.setEnabled(true);
                    }
                }));
        tableMenu.add(new JMenuItem(new AbstractAction("Use new model") {
                    @Override public void actionPerformed(ActionEvent e) {
                        tableModel = TestTableModel.createTestTableModel();
                        reinitFiltersMenu();
                        table.setModel(tableModel);
                        customizeTable();
                        removeElement.setEnabled(true);
                    }
                }));

        return tableMenu;
    }

    private JMenu createHeaderMenu() {
        JCheckBoxMenuItem onUse = new JCheckBoxMenuItem(new AbstractAction(
                    "on use") {
                    @Override public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                            e.getSource();
                        if (source.isSelected()) {
                            filterHeader.setTable(table);
                            customizeTable();
                        } else {
                            filterHeader.setTable(null);
                        }
                    }
                });

        JCheckBoxMenuItem visible = new JCheckBoxMenuItem(new AbstractAction(
        "visible") {
        @Override public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                e.getSource();
            filterHeader.setVisible(source.isSelected());
        }
    });

        JCheckBoxMenuItem adaptiveChoices = new JCheckBoxMenuItem(
                new AbstractAction("adaptive choices") {
                    @Override public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                            e.getSource();
                        filterHeader.setAdaptiveChoices(source.isSelected());
                        updateFiltersInfo();
                    }
                });

        JCheckBoxMenuItem autoCompletion = new JCheckBoxMenuItem(
                new AbstractAction(AUTO_COMPLETION) {
                    @Override public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                            e.getSource();
                        filterHeader.setAutoCompletion(source.isSelected());
                        updateFiltersInfo();
                    }
                });

        allEnabled = new JCheckBoxMenuItem(new AbstractAction(ENABLED) {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                    e.getSource();
                filterHeader.setEnabled(source.isSelected());
                updateFiltersInfo();
            }
        });

        JCheckBoxMenuItem filterOnUpdates = new JCheckBoxMenuItem(new AbstractAction("filter on updates") {
                @Override public void actionPerformed(ActionEvent e) {
                    JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
                    filterHeader.setFilterOnUpdates(source.isSelected());
                }
            });

        JCheckBoxMenuItem ignoreCase = new JCheckBoxMenuItem(new AbstractAction(
                    IGNORE_CASE) {
                    @Override public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                            e.getSource();
                        filterHeader.getParserModel().setIgnoreCase(
                            source.isSelected());
                        updateFiltersInfo();
                    }
                });

        JCheckBoxMenuItem instantFiltering = new JCheckBoxMenuItem(
                new AbstractAction(INSTANT_FILTERING) {
                    @Override public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                            e.getSource();
                        filterHeader.setInstantFiltering(source.isSelected());
                        updateFiltersInfo();
                    }
                });

        JMenuItem reset = new JMenuItem(new AbstractAction("reset") {
                    @Override public void actionPerformed(ActionEvent e) {
                        filterHeader.resetFilter();
                        updateFiltersInfo();
                    }
                });
        onUse.setSelected(true);
        ignoreCase.setMnemonic(KeyEvent.VK_C);
        ignoreCase.setSelected(filterHeader.getParserModel().isIgnoreCase());
        adaptiveChoices.setSelected(filterHeader.isAdaptiveChoices());
        instantFiltering.setSelected(filterHeader.isInstantFiltering());
        autoCompletion.setSelected(filterHeader.isAutoCompletion());
        allEnabled.setSelected(filterHeader.isEnabled());
        visible.setSelected(filterHeader.isVisible());
        filterOnUpdates.setSelected(filterHeader.isFilterOnUpdates());

        JMenu ret = new JMenu("Filter Header");
        ret.setMnemonic(KeyEvent.VK_H);
        ret.add(onUse);
        ret.add(visible);
        ret.add(createPositionMenu());
        ret.addSeparator();
        ret.add(adaptiveChoices);
        ret.add(autoCompletion);
        ret.add(allEnabled);
        ret.add(filterOnUpdates);
        ret.add(ignoreCase);
        ret.add(instantFiltering);
        ret.add(createAutoChoicesMenu(filterHeader.getAutoChoices(),
                new AutoChoicesSet() {
                    @Override public void setAutoChoices(AutoChoices ao) {
                        filterHeader.setAutoChoices(ao);
                        updateFiltersInfo();
                    }
                }));
        ret.addSeparator();
        ret.add(createAppearanceMenu());
        ret.add(createMaxRowsMenu());
        ret.add(createMaxHistoryMenu(null));
        ret.addSeparator();
        ret.add(reset);

        return ret;
    }

    private JMenu createAutoChoicesMenu(AutoChoices          preselected,
                                        final AutoChoicesSet iface) {
        JMenu       ret = new JMenu(AUTO_CHOICES);
        ButtonGroup group = new ButtonGroup();
        for (AutoChoices ao : AutoChoices.values()) {
            final AutoChoices    set = ao;
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                    new AbstractAction(ao.toString().toLowerCase()) {
                        @Override public void actionPerformed(ActionEvent e) {
                            iface.setAutoChoices(set);
                        }
                    });
            group.add(item);
            if (preselected == ao) {
                item.setSelected(true);
            }

            ret.add(item);
        }

        return ret;
    }

    private JMenu createlLookAndFeelMenu() {
        JMenu       ret = new JMenu("Look And Feel");
        ButtonGroup group = new ButtonGroup();
        LookAndFeel now = UIManager.getLookAndFeel();
        for (LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()) {
            final String         classname = lfi.getClassName();
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                    new AbstractAction(lfi.getName()) {
                        @Override public void actionPerformed(ActionEvent e) {
                            try {
                                UIManager.setLookAndFeel(classname);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                System.exit(0);
                            }

                            SwingUtilities.updateComponentTreeUI(
                                TableFilterExample.this);
                            TableFilterExample.this.pack();
                        }
                    });
            group.add(item);
            ret.add(item);
            if (lfi.getName().equals(now.getName())) {
                item.setSelected(true);
            }
        }

        if (group.getButtonCount() < 2) {
            ret.setEnabled(false);
        }

        return ret;
    }

    private JMenu createMiscellaneousMenu() {

        JMenuItem events = new JMenuItem(new AbstractAction("events window") {
                    EventsWindow window;

                    @Override public void actionPerformed(ActionEvent e) {
                        if ((window == null) || !window.isVisible()) {
                            window = new EventsWindow(TableFilterExample.this,
                                    filterHeader);
                            window.setVisible(true);
                        } else {
                            window.requestFocus();
                        }
                    }
                });

        JMenu ret = new JMenu("Miscellaneous");
        ret.setMnemonic(KeyEvent.VK_M);
        ret.add(events);
        ret.addSeparator();
        ret.add(createlLookAndFeelMenu());

        return ret;
    }

    private JMenu createPositionMenu() {
        JRadioButtonMenuItem top = new JRadioButtonMenuItem(new AbstractAction(
                    "top (automatic)") {
                    @Override public void actionPerformed(ActionEvent e) {
                        setPosition(Position.TOP);
                    }
                });
        JRadioButtonMenuItem inline = new JRadioButtonMenuItem(
                new AbstractAction("inline (automatic)") {
                    @Override public void actionPerformed(ActionEvent e) {
                        setPosition(Position.INLINE);
                    }
                });
        JRadioButtonMenuItem manual = new JRadioButtonMenuItem(
                new AbstractAction("bottom (manual)") {
                    @Override public void actionPerformed(ActionEvent e) {
                        setPosition(Position.NONE);
                    }
                });
        ButtonGroup          group = new ButtonGroup();
        group.add(top);
        group.add(inline);
        group.add(manual);
        switch (filterHeader.getPosition()) {

        case TOP:
            top.setSelected(true);

            break;

        case INLINE:
            inline.setSelected(true);

            break;

        case NONE:
            manual.setSelected(true);

            break;
        }

        setPosition(filterHeader.getPosition());

        JMenu ret = new JMenu("position");
        ret.add(top);
        ret.add(inline);
        ret.add(manual);

        return ret;
    }

    private JMenu createAppearanceMenu() {
        JMenu ret = new JMenu("appearance");
        ret.add(new JMenuItem(new AbstractAction("background color ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this,
                                "Select background color",
                                filterHeader.getBackground());
                        if (ret != null) {
                            filterHeader.setBackground(ret);
                        }
                    }
                }));
        ret.add(new JMenuItem(new AbstractAction("foreground color ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this,
                                "Select foreground color",
                                filterHeader.getForeground());
                        if (ret != null) {
                            filterHeader.setForeground(ret);
                        }
                    }
                }));
        ret.add(new JMenuItem(new AbstractAction("disabled color ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this,
                                "Select disabled color",
                                filterHeader.getDisabledForeground());
                        if (ret != null) {
                            filterHeader.setDisabledForeground(ret);
                        }
                    }
                }));
        ret.add(new JMenuItem(new AbstractAction("disabled background ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this,
                                "Select disabled backgrouns color",
                                filterHeader.getDisabledBackground());
                        if (ret != null) {
                            filterHeader.setDisabledBackground(ret);
                        }
                    }
                }));
        ret.add(new JMenuItem(new AbstractAction("grid color ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this, "Select grid color",
                                filterHeader.getGridColor());
                        if (ret != null) {
                            filterHeader.setGridColor(ret);
                        }
                    }
                }));
        ret.add(new JMenuItem(new AbstractAction("error color ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this, "Select error color",
                                filterHeader.getErrorForeground());
                        if (ret != null) {
                            filterHeader.setErrorForeground(ret);
                        }
                    }
                }));
        ret.add(new JMenuItem(new AbstractAction("warning color ...") {
            @Override public void actionPerformed(ActionEvent e) {
                Color ret = JColorChooser.showDialog(
                        TableFilterExample.this, "Select warning color",
                        filterHeader.getWarningForeground());
                if (ret != null) {
                    filterHeader.setWarningForeground(ret);
                }
            }
        }));
        ret.add(new JMenuItem(
                new AbstractAction("selection foreground ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this,
                                "Select selection foreground",
                                filterHeader.getSelectionForeground());
                        if (ret != null) {
                            filterHeader.setSelectionForeground(ret);
                        }
                    }
                }));
        ret.add(new JMenuItem(
                new AbstractAction("selection background ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this,
                                "Select selection background",
                                filterHeader.getSelectionBackground());
                        if (ret != null) {
                            filterHeader.setSelectionBackground(ret);
                        }
                    }
                }));
        ret.add(new JMenuItem(
                new AbstractAction("text selection color ...") {
                    @Override public void actionPerformed(ActionEvent e) {
                        Color ret = JColorChooser.showDialog(
                                TableFilterExample.this,
                                "Select text selection color",
                                filterHeader.getTextSelectionColor());
                        if (ret != null) {
                            filterHeader.setTextSelectionColor(ret);
                        }
                    }
                }));
        ret.addSeparator();
        ret.add(createFontSizeMenu());

        ret.addSeparator();
        ret.add(createRowSizeMenu());
        
        return ret;
    }

    void addColumnToFiltersMenu(final IFilterEditor editor, final String name) {
        JMenu menu = (JMenu) getMenu(filtersMenu, name, false);

        JCheckBoxMenuItem editable = new JCheckBoxMenuItem(EDITABLE,
                editor.isEditable());
        editable.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                        e.getSource();
                    editor.setEditable(source.isSelected());
                }
            });
        menu.add(editable);

        JCheckBoxMenuItem enabled = new JCheckBoxMenuItem(ENABLED,
                editor.getFilter().isEnabled());
        enabled.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                        e.getSource();
                    editor.getFilter().setEnabled(source.isSelected());
                    allEnabled.setSelected(filterHeader.isEnabled());
                }
            });
        menu.add(enabled);

        menu.add(createAutoChoicesMenu(editor.getAutoChoices(),
                new AutoChoicesSet() {
                    @Override public void setAutoChoices(AutoChoices ao) {
                        editor.setAutoChoices(ao);
                        updateFilter(editor, name);
                    }
                }));
        menu.addSeparator();

        JCheckBoxMenuItem autoCompletion = new JCheckBoxMenuItem(AUTO_COMPLETION,
                editor.isAutoCompletion());
        autoCompletion.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
                    editor.setAutoCompletion(source.isSelected());
                }
            });
        menu.add(autoCompletion);

        JCheckBoxMenuItem ignoreCase = new JCheckBoxMenuItem(IGNORE_CASE,
                editor.isIgnoreCase());
        ignoreCase.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                        e.getSource();
                    editor.setIgnoreCase(source.isSelected());
                }
            });
        menu.add(ignoreCase);

        JCheckBoxMenuItem instantFiltering = new JCheckBoxMenuItem(INSTANT_FILTERING,
                editor.isInstantFiltering());
        instantFiltering.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JCheckBoxMenuItem source = (JCheckBoxMenuItem)
                        e.getSource();
                    editor.setInstantFiltering(source.isSelected());
                }
            });
        menu.add(instantFiltering);

        menu.addSeparator();

        if (name.equalsIgnoreCase(TestTableModel.COUNTRY)) {
            JCheckBoxMenuItem useFlagRenderer = new JCheckBoxMenuItem(
                    USE_TABLE_RENDERER, true);
            useFlagRenderer.addItemListener(new ItemListener() {

                    @Override public void itemStateChanged(ItemEvent e) {
                        if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                            editor.setRenderer(new FlagRenderer());
                        } else {
                            editor.setRenderer(null);
                        }
                    }
                });

            menu.add(useFlagRenderer);
        }

        if (name.equalsIgnoreCase(TestTableModel.MALE)) {
            JCheckBoxMenuItem maleCC = new JCheckBoxMenuItem(
                    "Specific custom choices", useMaleCustomChoices);
            maleCC.addItemListener(new ItemListener() {

                    @Override public void itemStateChanged(ItemEvent e) {
                    	useMaleCustomChoices = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                    	setupCustomChoicesOnMaleColumn();
                    }
                });

            menu.add(maleCC);
        }

        menu.add(createMaxHistoryMenu(editor));
        menu.addSeparator();
        if (name.equalsIgnoreCase("country")) {
            menu.add(countrySpecialSorter);
            menu.addSeparator();
        }

        menu.add(new JMenuItem(new AbstractAction("Remove this column") {
                    @Override public void actionPerformed(ActionEvent e) {
                        TableColumnModel model = table.getColumnModel();
                        TableColumn      tc = model.getColumn(
                                model.getColumnIndex(name));
                        model.removeColumn(tc);
                        createFilterColumnRecovery(tc);
                    }
                }));
    }

    void createFilterColumnRecovery(final TableColumn tc) {
        final String title = "Recover column " + (String) tc.getHeaderValue();
        JMenuItem    item = new JMenuItem(new AbstractAction(title) {
                    @Override public void actionPerformed(ActionEvent e) {
                        table.getColumnModel().addColumn(tc);
                        getMenu(filtersMenu, title, true);
                        if (TestTableModel.AGE.equals(tc.getHeaderValue())) {
                            customizeAgeColumn();
                        } else if (TestTableModel.COUNTRY.equals(tc.getHeaderValue())) {
                            customizeCountryColumn();
                        } else if (TestTableModel.DATE.equals(tc.getHeaderValue())) {
                            customizeDateColumn();
                        } else if (TestTableModel.MALE.equals(tc.getHeaderValue())) {
                        	customizeMaleColumn();
                        }
                    }
                });
        filtersMenu.add(item);
    }

    void updateFiltersInfo() {
        TableColumnModel model = table.getColumnModel();
        int              n = model.getColumnCount();
        while (n-- > 0) {
            TableColumn tc = model.getColumn(n);
            updateFilter(filterHeader.getFilterEditor(tc.getModelIndex()),
                (String) tc.getHeaderValue());
        }

        enableUserFilter.setSelected(userFilter.isEnabled());
    }

    void updateFilter(IFilterEditor editor, String columnName) {
        JMenu menu = (JMenu) getMenu(filtersMenu, columnName, false);
        ((JCheckBoxMenuItem) getMenu(menu, EDITABLE, false)).setSelected(
            editor.isEditable());
        ((JCheckBoxMenuItem) getMenu(menu, ENABLED, false)).setSelected(
            editor.getFilter().isEnabled());
        ((JCheckBoxMenuItem) getMenu(menu, IGNORE_CASE, false)).setSelected(
            editor.isIgnoreCase());

        JMenu autoChoicesMenu = (JMenu) getMenu(menu, AUTO_CHOICES, false);
        ((JRadioButtonMenuItem) getMenu(autoChoicesMenu,
                editor.getAutoChoices().toString().toLowerCase(), false))
            .setSelected(true);

        JMenu                historyMenu = (JMenu) getMenu(menu,
                MAX_HISTORY_LENGTH, false);
        JRadioButtonMenuItem item = ((JRadioButtonMenuItem) getMenu(historyMenu,
                    String.valueOf(editor.getMaxHistory()), false));
        if (item != null) {
            item.setSelected(true);
        }
    }

    JMenuItem getMenu(JMenu menu, String name, boolean remove) {
        int pos = menu.getItemCount();
        while (pos-- > 0) {
            JMenuItem item = menu.getItem(pos);
            if ((item != null) && item.getText().equals(name)) {
                if (remove) {
                    menu.remove(pos);
                }

                return item;
            }
        }

        if (remove) {
            return null;
        }

        JMenu ret = new JMenu(name);
        menu.add(ret);

        return ret;
    }

    private JMenu createFontSizeMenu() {
        int         RELATIVE_FONT_SIZES[] = { -2, -1, 0, 1, 2, 4, 8, 16 };
        int         size = filterHeader.getFont().getSize();
        JMenu       ret = new JMenu("font size");
        ButtonGroup group = new ButtonGroup();
        for (int i : RELATIVE_FONT_SIZES) {
            JRadioButtonMenuItem item = createFontSizeMenuItem(size + i);
            ret.add(item);
            group.add(item);
            if (i == 0) {
                item.setSelected(true);
            }
        }

        return ret;
    }

    private JRadioButtonMenuItem createFontSizeMenuItem(final int size) {
        return new JRadioButtonMenuItem(new AbstractAction(
                    String.valueOf(size)) {
                    @Override public void actionPerformed(ActionEvent e) {
                        filterHeader.setFont(
                            filterHeader.getFont().deriveFont((float) (size)));
                    }
                });
    }

    private JMenu createRowSizeMenu() {
        int         RELATIVE_SIZES[] = { -2, 0, 4, 10, 20, 40};
        int         size = filterHeader.getRowHeightDelta();
        JMenu       ret = new JMenu("row height delta");
        ButtonGroup group = new ButtonGroup();
        for (int i : RELATIVE_SIZES) {
            JRadioButtonMenuItem item = createRowSizeMenuItem(i);
            ret.add(item);
            group.add(item);
            if (i == size) {
                item.setSelected(true);
            }
        }

        return ret;
    }

    private JRadioButtonMenuItem createRowSizeMenuItem(final int size) {
        return new JRadioButtonMenuItem(new AbstractAction(
                    String.valueOf(size)) {
                    @Override public void actionPerformed(ActionEvent e) {
                        filterHeader.setRowHeightDelta(size);
                    }
                });
    }

    private JMenu createMaxRowsMenu() {
        JMenu       ret = new JMenu("max visible rows on popup");
        ButtonGroup group = new ButtonGroup();
        for (int i = 4; i < 16; i++) {
            JRadioButtonMenuItem item = createMaxRowsMenuItem(i);
            if (i == filterHeader.getMaxVisibleRows()) {
                item.setSelected(true);
            }

            group.add(item);
            ret.add(item);
        }

        return ret;
    }

    private JRadioButtonMenuItem createMaxRowsMenuItem(final int i) {
        return new JRadioButtonMenuItem(new AbstractAction(String.valueOf(i)) {
                    @Override public void actionPerformed(ActionEvent e) {
                        filterHeader.setMaxVisibleRows(i);
                        updateFiltersInfo();
                    }
                });
    }

    private JMenu createMaxHistoryMenu(IFilterEditor editor) {
        JMenu       history = new JMenu(MAX_HISTORY_LENGTH);
        ButtonGroup max = new ButtonGroup();

        for (int i = 0; i < 10; i++) {
            max.add(createMaxHistoryMenuItem(history, editor, i));
        }

        return history;
    }

    private JRadioButtonMenuItem createMaxHistoryMenuItem(
            final JMenu         history,
            final IFilterEditor editor,
            final int           i) {
        JRadioButtonMenuItem ret = new JRadioButtonMenuItem(new AbstractAction(
                    String.valueOf(i)) {
                    @Override public void actionPerformed(ActionEvent e) {
                        if (editor == null) {
                            filterHeader.setMaxHistory(i);
                            updateFiltersInfo();
                        } else {
                            editor.setMaxHistory(i);

                            int set = editor.getMaxHistory();
                            if (set != i) {
                                JRadioButtonMenuItem mode =
                                    (JRadioButtonMenuItem) getMenu(history,
                                        String.valueOf(set), false);
                                if (mode != null) {
                                    mode.setSelected(true);
                                }
                            }
                        }
                    }
                });
        int current = (editor == null) ? filterHeader.getMaxHistory()
                                       : editor.getMaxHistory();
        if (current == i) {
            ret.setSelected(true);
        }

        history.add(ret);

        return ret;
    }

    private int getColumnView(String column) {
        return table.convertColumnIndexToView(tableModel.getColumn(column));

    }

    void setCountryComparator(boolean set) {
        if ((tableModel != null)
                && (getColumnView(TestTableModel.COUNTRY) != -1)) {
            int                       column = tableModel.getColumn(
                    TestTableModel.COUNTRY);
            Comparator<TestData.Flag> comp = set ? new TestData.RedComparator()
                                                 : null;
            filterHeader.getFilterEditor(column).setComparator(comp);
        }
    }

    void customizeCountryColumn() {
        int countryColumn = getColumnView(TestTableModel.COUNTRY);

        if (countryColumn != -1) {
            table.getColumnModel().getColumn(countryColumn).setCellRenderer(
                new FlagRenderer());

            int     column = tableModel.getColumn(TestTableModel.COUNTRY);
            boolean set = true;
            JMenu   menu = (JMenu) getMenu(filtersMenu, TestTableModel.COUNTRY,
                    false);
            if (menu != null) {
                JCheckBoxMenuItem box = (JCheckBoxMenuItem) getMenu(menu,
                        USE_TABLE_RENDERER, false);
                if (box != null) {
                    set = box.isSelected();
                }
            }

            IFilterEditor editor = filterHeader.getFilterEditor(column);
            if (set) {
                editor.setRenderer(new FlagRenderer());
            }

            editor.setEditable(false);
            updateFilter(editor, tableModel.getColumnName(column));
            setCountryComparator(countrySpecialSorter.isSelected());
        }
    }

    void customizeAgeColumn() {
        int agesColumn = tableModel.getColumn(TestTableModel.AGE);
        int agesColumnView = getColumnView(TestTableModel.AGE);

        if (agesColumnView != -1) {
            table.getColumnModel().getColumn(agesColumnView).setCellRenderer(
                new CenteredRenderer());
            filterHeader.getFilterEditor(agesColumn).setCustomChoices(
                AgeCustomChoice.getCustomChoices());
        }
    }

    void customizeMaleColumn() {
        int maleColumnView = getColumnView(TestTableModel.MALE);

        if (maleColumnView != -1) {        	
            table.getColumnModel().getColumn(maleColumnView).setCellRenderer(
                new TableCellRenderer(){
                    TableCellRenderer delegate = table.getDefaultRenderer(Boolean.class);
                    Border redBorder = BorderFactory.createLineBorder(Color.red);

                    @Override public Component getTableCellRendererComponent(JTable table,
                                                                   Object value,
                                                                   boolean isSelected,
                                                                   boolean hasFocus,
                                                                   int row,
                                                                   int column) {
                        JComponent c = (JComponent) delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        int modelRow = table.convertRowIndexToModel(row);
                        if (tableModel.isModified(tableModel.getRow(modelRow))) {
                            c.setBorder(BorderFactory.createCompoundBorder(c.getBorder(), redBorder));
                        }
                        return c;
                    }                	
                });
            setupCustomChoicesOnMaleColumn();
        }
    }

    void setupCustomChoicesOnMaleColumn() {
        int maleColumn = tableModel.getColumn(TestTableModel.MALE);

        if (maleColumn != -1) {
            IFilterEditor editor = filterHeader.getFilterEditor(maleColumn);
            Set<CustomChoice> choices = new HashSet<CustomChoice>();
        	if (useMaleCustomChoices){
            	//specific code.
            	//the checkbox for male/female can be modified.
            	//if the associated filter is set to true or false, updating this
            	//checkbox value would make the row vanish
            	//To avoid that, we setup specific custom choices that do not
            	//filter out modified values
				CustomChoice obsoleteChoice = new CustomChoice("True +") {
	
					@Override public RowFilter getFilter(IFilterEditor fe) {
						return new RowFilter() {
							@Override public boolean include(Entry entry) {
								int row = (Integer) entry.getIdentifier();
								TestData td = tableModel.getRow(row);
								return td.male || tableModel.isModified(td);
							}
						};
					}
				};
	
				CustomChoice nonObsoleteChoice = new CustomChoice("False +") {
	
					@Override public RowFilter getFilter(IFilterEditor fe) {
						return new RowFilter() {
							@Override public boolean include(Entry entry) {
								int row = (Integer) entry.getIdentifier();
								TestData td = tableModel.getRow(row);
								return !td.male || tableModel.isModified(td);
							}
						};
					}
				};
	
	            choices.add(obsoleteChoice);
	            choices.add(nonObsoleteChoice);
	            editor.setAutoChoices(AutoChoices.DISABLED);
	            editor.setEditable(false);
	            editor.setCustomChoices(choices);
        	} else {
	            editor.setCustomChoices(choices);
	            editor.setAutoChoices(AutoChoices.ENUMS);
	            editor.setEditable(true);
        	}
            updateFilter(editor, TestTableModel.MALE);
        }
    }

    void customizeDateColumn() {
        int datesColumnView = getColumnView(TestTableModel.DATE);

        if (datesColumnView != -1) {
            Set<CustomChoice> choices = new HashSet<CustomChoice>();
            choices.add(new Ages60sCustomChoice());
            filterHeader.getFilterEditor(tableModel.getColumn(
                    TestTableModel.DATE)).setCustomChoices(choices);
            table.getColumnModel().getColumn(datesColumnView).setCellRenderer(
                new DefaultTableCellRenderer() {

                    private static final long serialVersionUID =
                        8042527267257156699L;
                    Format                    parser =
                        filterHeader.getParserModel().getFormat(Date.class);

                    @Override public Component getTableCellRendererComponent(
                            JTable  table,
                            Object  value,
                            boolean isSelected,
                            boolean hasFocus,
                            int     row,
                            int     column) {
                        if (value instanceof Date) {
                            value = parser.format(value);
                        }

                        return super.getTableCellRendererComponent(table, value,
                                isSelected, hasFocus, row, column);
                    }
                });
        }
    }

    void customizeTable() {
        if (filterHeader.getTable() != null) {
            customizeMaleColumn();
            customizeCountryColumn();
            customizeAgeColumn();
            customizeDateColumn();
        }
    }

    void addTestData(boolean male) {
        TestData td = new TestData();
        td.male = male;
        tableModel.addTestData(td);
    }

    void setPosition(Position position) {
        if (filterHeader.getPosition() == Position.NONE && filterHeaderPanel!=null) {
            filterHeaderPanel.remove(filterHeader);
            tablePanel.remove(filterHeaderPanel);
            tablePanel.revalidate();
        }

        filterHeader.setPosition(position);
        if (filterHeader.getPosition() == Position.NONE) {
            filterHeaderPanel = new JPanel(new BorderLayout());
            filterHeaderPanel.add(filterHeader, BorderLayout.CENTER);
            filterHeaderPanel.setBorder(BorderFactory.createLineBorder(
                    filterHeader.getDisabledForeground(), 1));
            tablePanel.add(filterHeaderPanel, BorderLayout.SOUTH);
            tablePanel.revalidate();
        }
    }

    interface AutoChoicesSet {
        void setAutoChoices(AutoChoices ao);
    }

    class Ages60sCustomChoice extends CustomChoice {
        Calendar cal = Calendar.getInstance();

        public Ages60sCustomChoice() {
            super("sixties",
                new ImageIcon(
                    TableFilterExample.class.getResource("resources/60.png")));
        }

        @Override public RowFilter getFilter(IFilterEditor editor) {
            final int modelIndex = editor.getModelIndex();

            return new RowFilter() {
                @Override public boolean include(RowFilter.Entry entry) {
                    Object o = entry.getValue(modelIndex);
                    if (o instanceof Date) {
                        cal.setTime((Date) o);

                        int year = cal.get(Calendar.YEAR);

                        return (year >= 1960) && (year < 1969);
                    }

                    return false;
                }
            };
        }

        @Override public void decorateComponent(IFilterEditor editor,
                                                boolean       isSelected,
                                                JComponent    c,
                                                Graphics      g) {
            Icon icon = getIcon();
            if (icon != null) {
                Icon use;
                if (c.isEnabled()) {
                    use = icon;
                } else {
                    use = UIManager.getLookAndFeel().getDisabledIcon(c, icon);
                }

                Font font = editor.getLook().getCustomChoiceDecorator().getFont(
                        this, editor, isSelected);
                FontMetrics metrics = g.getFontMetrics(font);
                int x = c.getWidth() - metrics.stringWidth(this.toString());
                int y = (c.getHeight() - use.getIconHeight()) / 2;
                use.paintIcon(c, g, x, y);
            }
        }
    }

    public final static void main(String args[]) {
        FilterSettings.autoChoices = AutoChoices.ENABLED;

        TableFilterExample frame = new TableFilterExample();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
