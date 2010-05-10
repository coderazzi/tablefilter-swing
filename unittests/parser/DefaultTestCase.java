package parser;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;

import junit.framework.TestCase;
import net.coderazzi.filters.parser.FilterTextParser;


public abstract class DefaultTestCase extends TestCase {

    static class TableModel extends AbstractTableModel {
        private static final long serialVersionUID = 8203189653936226308L;
		Class<?> type;

        public TableModel(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return type;
        }

        @Override
        public Object getValueAt(int rowIndex,
                                 int columnIndex) {
            return null;
        }

        @Override
        public int getRowCount() {
            return 0;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }
    }

    static class MyEntry extends RowFilter.Entry {
        Object o;

        MyEntry(Object o) {
            this.o = o;
        }

        @Override
        public Object getIdentifier() {
            return null;
        }

        @Override
        public Object getModel() {
            return null;
        }

        @Override
        public Object getValue(int index) {
            return o;
        }

        @Override
        public int getValueCount() {
            return 1;
        }
    }

    interface ParserCustomizer {
        void customize(FilterTextParser parser);
    }

    ParserCustomizer ignoreCase = new ParserCustomizer() {

            @Override
            public void customize(FilterTextParser parser) {
                parser.setIgnoreCase(true);
            }
        };

    ParserCustomizer integerCustomizer = new ParserCustomizer() {

            @Override
            public void customize(FilterTextParser parser) {
                parser.setFormat(Integer.class, new Format() {
                        private static final long serialVersionUID = -136765772975883655L;

						@Override
                        public StringBuffer format(Object obj,
                                                   StringBuffer toAppendTo,
                                                   FieldPosition pos) {
                            return toAppendTo.append(obj);
                        }

                        @Override
                        public Object parseObject(String source,
                                                  ParsePosition pos) {
                            try {
                                Object ret = Integer.valueOf(source);
                                pos.setIndex(source.length());
                                return ret;
                            } catch (Exception ex) {
                                pos.setErrorIndex(0);
                                return null;
                            }
                        }
                    });
            }
        };

    @SuppressWarnings("unchecked")
    protected boolean check(Class<?> c,
                            Object content,
                            String filter,
                            ParserCustomizer customizer) throws ParseException {
        FilterTextParser use = new FilterTextParser();
        if (customizer != null) {
            customizer.customize(use);
        }
        use.setTableModel(new TableModel(c));
        return use.parseText(filter, 0).include(new MyEntry(content));
    }

}