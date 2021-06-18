import org.jafipa.JSON.JSONObject;

public class Main {
    public static void main (String... args) {
        JSONObject obj = new JSONObject();
        obj.put("methods", JSONObject.class.getMethods());

        System.out.println(obj);
    }
}
