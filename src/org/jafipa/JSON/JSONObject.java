package org.jafipa.JSON;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class JSONObject implements Map<String, Object>, Iterable<JSONEntry> {
    final private ArrayList<JSONEntry> entries;

    public JSONObject () {
        super();
        this.entries = new ArrayList<>();
    }

    public JSONObject (Map<? extends String, ?> map) {
        this();
        putAll(map);
    }

    public JSONObject (String string) {
        this();

        if (string == null || string.length() <= 0) {
            return;
        }

        // Clean up text
        string = string.strip();
        string = string.replaceAll("\\s*\\n\\s*", "");
        string = string.replaceAll("\"\\s*:\\s*", "\":");
        string = string.replaceAll("\\,\\n*\\s*\"", ",\"");

        if (string.charAt(0) == '[') {
            string = "{\"\":"+string+"}";
        }

        // Generate String Builder
        StringBuilder builder = new StringBuilder(string);

        while (builder.length() > 0) {
            String key = stringFinder(builder);
            if (key == null) {
                break;
            }

            builder.deleteCharAt(0);
            JSONEntry entry = entryFinder(builder, key);
            entries.add(entry);
        }
    }

    private int indexOf (Object key) {
        if (!(key instanceof String)) { return -1; }
        String string = (String) key;

        for (int i=0;i<size();i++) {
            if (entries.get(i).key.equals(string)) {
                return i;
            }
        }

        return -1;
    }

    public <T> T getAs (String key) {
        return (T) get(key);
    }

    public <T> T getAs (String key, Class<T> clss) {
        return getAs(key);
    }

    public String getString (String key) {
        return getAs(key);
    }

    public boolean getBool (String key) {
        return getAs(key);
    }

    public JSONObject getObject (String key) {
        return getAs(key);
    }

    public ArrayList getArray (String key) {
        return getAs(key);
    }

    public Number getNumber (String key) {
        return getAs(key);
    }

    public int getInt (String key) {
        return getNumber(key).intValue();
    }

    public float getFloat (String key) {
        return getNumber(key).floatValue();
    }

    public long getLong (String key) {
        return getNumber(key).longValue();
    }

    public double getDouble (String key) {
        return getNumber(key).doubleValue();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public Object remove(Object key) {
        return entries.remove(indexOf(key));
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public Iterator<JSONEntry> iterator() {
        return new Iterator<JSONEntry>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i+1 < size();
            }

            @Override
            public JSONEntry next() {
                return entries.get(i++);
            }
        };
    }

    @Override
    public boolean containsKey(Object key) {
        return indexOf(key) != -1;
    }

    @Override
    public boolean containsValue (Object value) {
        for (int i=0;i<size();i++) {
            if (entries.get(i).value.equals(value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object get (Object key) {
        return get(indexOf(key));
    }

    @Override
    public Object put (String key, Object value) {
        JSONEntry entry = new JSONEntry(key, value);
        int index = indexOf(key);

        if (index == -1) {
            entries.add(entry);
        } else {
            entries.set(index, entry);
        }

        return value;
    }

    @Override
    public void putAll (Map<? extends String, ?> m) {
        for (Entry<? extends String,?> entry: m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Set<String> keySet() {
        HashSet<String> set = new HashSet<>();
        for (JSONEntry entry: this) {
            set.add(entry.key);
        }

        return set;
    }

    @Override
    public Collection<Object> values() {
        ArrayList<Object> set = new ArrayList<>();
        for (JSONEntry entry: this) {
            set.add(entry.value);
        }

        return set;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        HashSet<Entry<String, Object>> set = new HashSet<>();
        for (JSONEntry entry: this) {
            set.add(entry);
        }

        return set;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        for (JSONEntry entry: this) {
            builder.append(entry.toString()+", ");
        }

        if (builder.length() < 2) {
            return "{}";
        } else {
            return builder.substring(0, builder.length() - 2) + "}";
        }
    }

    public static JSONObject parseObject (Object object) {
        JSONObject result = new JSONObject();
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field: fields) {
            try {
                result.put(field.getName(), field.get(object));
            } catch (Exception e){};
        }

        return result;
    }

    public static JSONObject parseStatics (Class clss) {
        JSONObject result = new JSONObject();
        Field[] fields = clss.getDeclaredFields();

        for (Field field: fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            try {
                result.put(field.getName(), field.get(null));
            } catch (Exception e){};
        }

        return result;
    }

    private JSONEntry entryFinder (StringBuilder builder, String key) {
        if (builder.length() <= 0) {
            return null;
        }

        // String
        if (builder.charAt(0) == '"') {
            String value = stringFinder(builder);
            return new JSONEntry(key, value);
        }

        // Number
        else if (builder.substring(0,1).matches("\\d")) {
            String value = numberFinder(builder);

            if (value.contains(".")) { // Decimal
                try {
                    return new JSONEntry(key, Float.parseFloat(value)); // Float
                } catch (Exception e) {
                    return new JSONEntry(key, Double.parseDouble(value)); // Double
                }
            } else { // Integer
                try {
                    return new JSONEntry(key, Integer.parseInt(value)); // Int
                } catch (Exception e) {
                    return new JSONEntry(key, Long.parseLong(value)); // Long
                }
            }
        }

        // Boolean (true)
        else if (builder.length() >= 4 && builder.substring(0, 4).equals("true")) {
            builder.delete(0, 4);
            return new JSONEntry(key, true);
        }

        // Boolean (false)
        else if (builder.length() >= 5 && builder.substring(0, 5).equals("false")) {
            builder.delete(0, 5);
            return new JSONEntry(key, false);
        }

        // Null
        else if (builder.length() >= 4 && builder.substring(0, 4).equals("null")) {
            builder.delete(0, 4);
            return new JSONEntry(key, null);
        }

        // Array
        else if (builder.charAt(0) == '[') {
            String inside = arrayFinder(builder);
            if (inside == null) {
                return new JSONEntry(key, new ArrayList<>());
            }

            StringBuilder value = new StringBuilder(inside);
            ArrayList entries = new ArrayList<>();

            while (value.length() > 0) {
                JSONEntry entry = entryFinder(value, "");
                if (entry == null) {
                    break;
                }

                if (value.length() > 0 && value.charAt(0) == ',') {
                    value.deleteCharAt(0);
                }

                entries.add(entry.value);
            }

            return new JSONEntry(key, entries);
        }

        // Object
        else if (builder.charAt(0) == '{') {
            String value = objectFinder(builder);
            return new JSONEntry(key, new JSONObject(value));
        }

        return null;
    }

    private String objectFinder (StringBuilder builder) {
        String result = null;
        int skips = 0;

        builder.deleteCharAt(0);
        while (true) {
            char c = builder.charAt(0);
            builder.deleteCharAt(0);

            if (c == '{') {
                skips++;
            } else if (c == '}' && skips <= 0) {
                break;
            } else if (c == '}' && skips > 0) {
                skips--;
            }

            if (result == null) {
                result = ""+c;
            } else {
                result += c;
            }
        }

        return result;
    }

    private String arrayFinder (StringBuilder builder) {
        String result = null;
        int skips = 0;

        builder.deleteCharAt(0);
        while (true) {
            char c = builder.charAt(0);
            builder.deleteCharAt(0);

            if (c == '[') {
                skips++;
            } else if (c == ']' && skips <= 0) {
                break;
            } else if (c == ']' && skips > 0) {
                skips--;
            }

            if (result == null) {
                result = ""+c;
            } else {
                result += c;
            }
        }

        return result;
    }

    private String numberFinder (StringBuilder builder) {
        String result = "";

        while (builder.length() > 0) {
            String c = builder.substring(0,1);
            builder.deleteCharAt(0);

            if (!c.equals(".") && !c.matches("\\d")) {
                break;
            }

            result += c;
        }

        return result;
    }

    private String stringFinder (StringBuilder builder) {
        if (builder.length() <= 0) {
            return null;
        }

        String result = null;

        while (builder.length() > 0) {
            char c = builder.charAt(0);
            builder.deleteCharAt(0);

            if (c == '"' && result == null) {
                result = "";
            } else if (c == '"') {
                break;
            } else if (result != null) {
                result += c;
            }
        }

        return result;
    }
}