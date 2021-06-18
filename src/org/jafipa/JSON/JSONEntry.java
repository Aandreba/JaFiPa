package org.jafipa.JSON;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class JSONEntry implements Map.Entry<String,Object> {
    final public String key;
    final public Object value;

    public JSONEntry (String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey () {
        return key;
    }

    @Override
    public Object getValue () {
        return value;
    }

    @Override
    public Object setValue (Object value) {
        return null;
    }

    public Class<?> getType () {
        return value.getClass();
    }

    @Override
    public String toString () {
        return "\""+key+"\": "+(value == null ? "null" : toString(value));
    }

    private String toString (Object object) {
        if (object instanceof JSONObject || isPrimitive(object.getClass())) {
            return object.toString();
        } else if (object instanceof List) {
            StringBuilder result = new StringBuilder();
            List list = (List) object;
            list.forEach(x -> result.append(", ").append(toString(x)));

            return "[" + (result.length() >= 2 ? result.substring(2) : result) + "]";
        } else if (object.getClass().isArray()) {
            StringBuilder result = new StringBuilder();

            int i = 0;
            while (true) {
                Object val = null;
                try {
                    val = Array.get(object, i);
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }

                result.append(", ").append(toString(val));
                i++;
            }

            return "[" + (result.length() >= 2 ? result.substring(2) : result) + "]";
        }

        return "\""+object.toString()+"\"";
    }

    private static boolean isPrimitive (Class clss) {
        return clss.isPrimitive() || clss.equals(Boolean.class) || clss.equals(Byte.class) || clss.equals(Short.class) || clss.equals(Character.class) || clss.equals(Integer.class) || clss.equals(Float.class) || clss.equals(Long.class) || clss.equals(Double.class);
    }
}
