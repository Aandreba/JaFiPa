package org.jafipa;

import java.util.ArrayList;

public class CSV extends ArrayList<ArrayList> {
    public CSV() {
        super();
    }

    public CSV(String string, String separator, char newLine) {
        this();

        StringBuilder builder = new StringBuilder(string);
        int separatorLength = separator.length();
        boolean skip = false;

        ArrayList column = new ArrayList();
        StringBuilder value = new StringBuilder();

        while (builder.length() > 0) {
            char c = builder.charAt(0);
            builder.deleteCharAt(0);

            if (c == '"') {
                skip = !skip;
            }

            if (builder.length() >= separatorLength - 1 && !skip) {
                String separatorComparator = c + builder.substring(0, separatorLength - 1);

                if (separatorComparator.equals(separator)) { // New column
                    builder.delete(0, separatorLength - 1);
                    column.add(processText(value.toString()));
                    value = new StringBuilder();
                    continue;
                }
            }

            if (c == newLine && !skip) { // New row
                column.add(processText(value.toString()));
                value = new StringBuilder();

                add(column);
                column = new ArrayList();
            } else {
                value.append(c);
            }
        }

        if (!value.isEmpty()) {
            column.add(processText(value.toString()));
        }

        if (!column.isEmpty()) {
            add(column);
        }
    }

    public CSV(String string, String separator) {
        this(string, separator, '\n');
    }

    public CSV(String string) {
        this(string, ",");
    }

    public Object get (int row, int col) {
        return get(row).get(col);
    }

    public Object get (int row, Object header) {
        return get(row, headerIndex(header));
    }

    public <T> T getAs (int row, int col) {
        return (T) get(row, col);
    }

    public <T> T getAs (int row, int col, Class<T> clss) {
        return getAs(row, col);
    }

    public <T> T getAs (int row, Object header) {
        return (T) get(row, headerIndex(header));
    }

    public <T> T getAs (int row, Object header, Class<T> clss) {
        return getAs(row, header);
    }

    public Number getNumber (int row, int col) {
        return getAs(row, col);
    }

    public int getInt (int row, int col) {
        return getNumber(row, col).intValue();
    }

    public float getFloat (int row, int col) {
        return getNumber(row, col).floatValue();
    }

    public long getLong (int row, int col) {
        return getNumber(row, col).longValue();
    }

    public double getDouble (int row, int col) {
        return getNumber(row, col).doubleValue();
    }

    public Number getNumber (int row, Object header) {
        return getAs(row, header);
    }

    public int getInt (int row, Object header) {
        return getNumber(row, header).intValue();
    }

    public float getFloat (int row, Object header) {
        return getNumber(row, header).floatValue();
    }

    public long getLong (int row, Object header) {
        return getNumber(row, header).longValue();
    }

    public double getDouble (int row, Object header) {
        return getNumber(row, header).doubleValue();
    }

    private int headerIndex (Object header) {
        ArrayList head = get(0);

        for (int i=0;i<head.size();i++) {
            if (head.get(i).equals(header)) {
                return i;
            }
        }

        return -1;
    }

    private static Object processText (String text) {
        if (text.length() <= 0) {
            return text;
        }

        if (text.substring(0,1).matches("\\d") || text.charAt(0) == '.') { // Number
            if (text.contains(".")) { // Decimal
                try {
                    return Float.parseFloat(text); // Float
                } catch (Exception e) {
                    try {
                        return Double.parseDouble(text); // Double
                    } catch (Exception er) {
                        // Skip, save as String
                    }
                }
            } else { // Integer
                try {
                    return Integer.parseInt(text); // Int
                } catch (Exception e) {
                    try {
                        return Long.parseLong(text); // Long
                    } catch (Exception er) {
                        // Skip, save as String
                    }
                }
            }
        }

        if (text.charAt(0) == '"') {
            text = text.substring(1);
        }

        if (text.length() > 0 && text.charAt(text.length() - 1) == '"') {
            text = text.substring(0, text.length() - 1);
        }

        return text.replace("\"\"", "\"");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (ArrayList row: this) {
            StringBuilder rowStr = new StringBuilder();

            for (Object col: row) {
                rowStr.append(", ");
                if (col instanceof Number) {
                    rowStr.append(col.toString());
                } else {
                    rowStr.append("\"").append(col.toString().replace("\"", "\"\"")).append("\"");
                }
            }

            builder.append(rowStr.substring(2)).append("\n");
        }

        return builder.toString();
    }
}
